package com.az.gitember.data;

import com.az.gitember.service.GitRepoService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises two real, independent repositories through two {@link Project} instances, to confirm
 * the state each now owns (branches, the cached {@link GitRepoService}, the commit-detail cache)
 * is genuinely per-project and not accidentally shared through statics -- the central risk of the
 * Context -> Project refactor.
 */
class ProjectStateIsolationTest {

    private Path repoADir;
    private Path repoBDir;
    private Project projectA;
    private Project projectB;

    @BeforeEach
    void setUp() throws Exception {
        repoADir = Files.createTempDirectory("gitember-test-a-");
        repoBDir = Files.createTempDirectory("gitember-test-b-");

        initRepoWithBranch(repoADir, "feature-a");
        initRepoWithBranch(repoBDir, "feature-b");

        projectA = new Project(repoADir.toString(), new Date());
        projectB = new Project(repoBDir.toString(), new Date());
    }

    @AfterEach
    void tearDown() throws Exception {
        projectA.closeRepoService();
        projectB.closeRepoService();
        deleteDirectory(repoADir);
        deleteDirectory(repoBDir);
    }

    private static void initRepoWithBranch(Path dir, String branchName) throws Exception {
        try (Git git = Git.init().setDirectory(dir.toFile()).call()) {
            Repository repository = git.getRepository();
            repository.getConfig().setString("user", null, "name", "Test User");
            repository.getConfig().setString("user", null, "email", "test@example.com");
            repository.getConfig().save();
            // Distinct content per repo guarantees the two initial commits get distinct SHAs.
            Files.writeString(dir.resolve("README.md"), "hello from " + branchName);
            git.add().addFilepattern(".").call();
            git.commit().setMessage("initial").call();
            git.branchCreate().setName(branchName).call();
        }
    }

    @Test
    void branchState_isIsolatedPerProject() throws Exception {
        projectA.openRepoService();
        projectA.updateBranches();
        projectB.openRepoService();
        projectB.updateBranches();

        List<String> aNames = projectA.getLocalBranches().stream().map(ScmBranch::getShortName).toList();
        List<String> bNames = projectB.getLocalBranches().stream().map(ScmBranch::getShortName).toList();

        assertTrue(aNames.contains("feature-a"), "project A should see its own branch");
        assertFalse(aNames.contains("feature-b"), "project A must not see project B's branch");
        assertTrue(bNames.contains("feature-b"), "project B should see its own branch");
        assertFalse(bNames.contains("feature-a"), "project B must not see project A's branch");
    }

    @Test
    void gitRepoService_isCachedPerProject_andReopensAfterClose() {
        GitRepoService svc1 = projectA.getGitRepoService();
        GitRepoService svc2 = projectA.getGitRepoService();
        assertSame(svc1, svc2, "repeated calls on the same project must return the same cached instance");

        GitRepoService svcB = projectB.getGitRepoService();
        assertNotSame(svc1, svcB, "two different projects must never share a service instance");

        projectA.closeRepoService();
        assertFalse(projectA.isRepoServiceOpen());

        GitRepoService svc3 = projectA.getGitRepoService();
        assertNotSame(svc1, svc3, "closing must force a fresh service on next use");
        assertTrue(projectA.isRepoServiceOpen());
    }

    @Test
    void commitDetailCache_isDisjointBetweenProjects() throws Exception {
        GitRepoService svcA = projectA.getGitRepoService();
        GitRepoService svcB = projectB.getGitRepoService();

        RevCommit headA = svcA.getRevCommitBySha(svcA.getHead().getSha());
        RevCommit headB = svcB.getRevCommitBySha(svcB.getHead().getSha());
        svcA.adapt(headA, null);
        svcB.adapt(headB, null);

        assertEquals(1, projectA.getScmRevisionInformationCache().size());
        assertEquals(1, projectB.getScmRevisionInformationCache().size());
        assertTrue(projectA.getScmRevisionInformationCache().keySet().stream()
                .noneMatch(sha -> projectB.getScmRevisionInformationCache().containsKey(sha)));
    }

    @Test
    void gitRepoService_ownerIsSetForProjectObtainedService_andNullForTransientOne() throws Exception {
        assertSame(projectA, projectA.getGitRepoService().getOwner());

        GitRepoService transientSvc = GitRepoService.of(repoADir.toString());
        try {
            assertNull(transientSvc.getOwner());
        } finally {
            transientSvc.shutdown();
        }
    }

    private static void deleteDirectory(Path dir) throws Exception {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
