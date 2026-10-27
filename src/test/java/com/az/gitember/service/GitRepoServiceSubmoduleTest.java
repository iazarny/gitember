package com.az.gitember.service;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.Submodule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleStatusType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * On-disk tests for submodule add / init / update / clone / diff / commit / remove.
 */
class GitRepoServiceSubmoduleTest {

    private Path parentDir;
    private Path childDir;
    private Path nestedDir;
    private Repository parentRepo;
    private GitRepoService parentService;

    @BeforeEach
    void setUp() throws Exception {
        parentDir = Files.createTempDirectory("gitember-parent-");
        childDir  = Files.createTempDirectory("gitember-child-");
        nestedDir = Files.createTempDirectory("gitember-nested-");

        parentRepo = initRepo(parentDir, "parent.txt", "parent\n", "parent initial");
        initRepo(childDir, "child.txt", "child\n", "child initial");
        initRepo(nestedDir, "nested.txt", "nested\n", "nested initial");

        parentService = new GitRepoService(parentRepo);
        configureIdentity(parentRepo);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (parentRepo != null) {
            parentRepo.close();
        }
        deleteDirectory(parentDir);
        deleteDirectory(childDir);
        deleteDirectory(nestedDir);
    }

    @Test
    void addSubmodule_registersPathAndIsUpToDate() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);

        List<Submodule> subs = parentService.getSubmodules();
        assertEquals(1, subs.size());
        assertEquals("libs/child", subs.get(0).getPath());
        assertEquals(Submodule.Status.UP_TO_DATE, subs.get(0).getStatus());
        assertTrue(Files.exists(parentDir.resolve("libs/child/child.txt")));
        assertTrue(Files.exists(parentDir.resolve(".gitmodules")));

        Map<String, SubmoduleStatus> status = parentService.getSubmoduleStatus();
        assertTrue(status.containsKey("libs/child"));
        assertNotEquals(SubmoduleStatusType.UNINITIALIZED, status.get("libs/child").getType());
    }

    @Test
    void commitSubmoduleChange_recordsNewGitlink() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child submodule",
                "Test User", "test@example.com");

        advanceSubmoduleHead(parentDir.resolve("libs/child"), "child.txt", "changed\n", "child second");

        List<Submodule> before = parentService.getSubmodules();
        assertEquals(Submodule.Status.MODIFIED, before.get(0).getStatus());

        RevCommit commit = parentService.commitSubmoduleChange(
                "libs/child", "bump child", "Test User", "test@example.com");
        assertNotNull(commit);
        assertEquals("bump child", commit.getShortMessage());

        List<Submodule> after = parentService.getSubmodules();
        assertEquals(Submodule.Status.UP_TO_DATE, after.get(0).getStatus());
        assertEquals(before.get(0).getHeadSha(), after.get(0).getIndexSha());
    }

    @Test
    void updateSubmodule_restoresRecordedCommit() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child",
                "Test User", "test@example.com");
        String recorded = parentService.getSubmodules().get(0).getIndexSha();

        advanceSubmoduleHead(parentDir.resolve("libs/child"), "child.txt", "drift\n", "local drift");
        assertEquals(Submodule.Status.MODIFIED, parentService.getSubmodules().get(0).getStatus());

        parentService.updateSubmodule("libs/child", null);

        Submodule after = parentService.getSubmodules().get(0);
        assertEquals(Submodule.Status.UP_TO_DATE, after.getStatus());
        assertEquals(recorded, after.getHeadSha());
    }

    @Test
    void submoduleDiff_showsIndexVersusHead() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child",
                "Test User", "test@example.com");

        String upToDate = parentService.getSubmoduleDiff("libs/child");
        assertTrue(upToDate.contains("up to date"), upToDate);

        advanceSubmoduleHead(parentDir.resolve("libs/child"), "child.txt", "diff me\n", "diff commit");
        String diff = parentService.getSubmoduleDiff("libs/child");
        assertTrue(diff.contains("Subproject commit"), diff);
        assertTrue(diff.contains("diff --git a/libs/child"), diff);
    }

    @Test
    void removeSubmodule_dropsGitlinkAndWorkingTree() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child",
                "Test User", "test@example.com");

        parentService.removeSubmodule("libs/child", true);

        assertTrue(parentService.getSubmodules().isEmpty());
        assertFalse(Files.exists(parentDir.resolve("libs/child/child.txt")));
        assertFalse(Files.exists(parentDir.resolve(".gitmodules")));
    }

    @Test
    void cloneWithSubmodules_initializesCheckout() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child",
                "Test User", "test@example.com");

        Path cloneDir = Files.createTempDirectory("gitember-clone-sub-");
        GitRepoService cloned = null;
        try {
            GitRepoService cloner = new GitRepoService();
            RemoteRepoParameters params = new RemoteRepoParameters();
            params.setUrl(parentDir.toUri().toString());
            params.setDestinationFolder(cloneDir.toString());
            params.setWithSubmodules(true);
            cloner.cloneRepository(params, null);

            cloned = new GitRepoService(cloneDir.resolve(".git").toString());
            List<Submodule> subs = cloned.getSubmodules();
            assertEquals(1, subs.size());
            assertEquals(Submodule.Status.UP_TO_DATE, subs.get(0).getStatus());
            assertTrue(Files.exists(cloneDir.resolve("libs/child/child.txt")));
        } finally {
            if (cloned != null) {
                cloned.shutdown();
            }
            deleteDirectory(cloneDir);
        }
    }

    @Test
    void cloneWithoutSubmodules_thenInitAndUpdate() throws Exception {
        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child",
                "Test User", "test@example.com");

        Path cloneDir = Files.createTempDirectory("gitember-clone-nosub-");
        GitRepoService cloned = null;
        try {
            GitRepoService cloner = new GitRepoService();
            RemoteRepoParameters params = new RemoteRepoParameters();
            params.setUrl(parentDir.toUri().toString());
            params.setDestinationFolder(cloneDir.toString());
            params.setWithSubmodules(false);
            cloner.cloneRepository(params, null);

            cloned = new GitRepoService(cloneDir.resolve(".git").toString());
            assertEquals(Submodule.Status.UNINITIALIZED, cloned.getSubmodules().get(0).getStatus());
            assertTrue(cloned.getSubmoduleDiff("libs/child").contains("uninitialized"));

            cloned.initSubmodules();
            cloned.updateSubmodule("libs/child", null);

            Submodule after = cloned.getSubmodules().get(0);
            assertEquals(Submodule.Status.UP_TO_DATE, after.getStatus());
            assertTrue(Files.exists(cloneDir.resolve("libs/child/child.txt")));
        } finally {
            if (cloned != null) {
                cloned.shutdown();
            }
            deleteDirectory(cloneDir);
        }
    }

    @Test
    void recursiveUpdate_initializesNestedSubmodule() throws Exception {
        GitRepoService childService = new GitRepoService(
                Git.open(childDir.toFile()).getRepository());
        try {
            configureIdentity(childService.getRepository());
            childService.addSubmodule(nestedDir.toUri().toString(), "vendor/nested", null);
            childService.commitSubmoduleChange("vendor/nested", "add nested",
                    "Test User", "test@example.com");
        } finally {
            childService.getRepository().close();
        }

        parentService.addSubmodule(childDir.toUri().toString(), "libs/child", null);
        parentService.commitSubmoduleChange("libs/child", "add child with nested",
                "Test User", "test@example.com");

        Path cloneDir = Files.createTempDirectory("gitember-clone-nested-");
        GitRepoService cloned = null;
        try {
            GitRepoService cloner = new GitRepoService();
            RemoteRepoParameters params = new RemoteRepoParameters();
            params.setUrl(parentDir.toUri().toString());
            params.setDestinationFolder(cloneDir.toString());
            params.setWithSubmodules(false);
            cloner.cloneRepository(params, null);

            cloned = new GitRepoService(cloneDir.resolve(".git").toString());
            cloned.updateSubmodules(null, true);

            assertEquals(Submodule.Status.UP_TO_DATE, cloned.getSubmodules().get(0).getStatus());
            assertTrue(Files.exists(cloneDir.resolve("libs/child/child.txt")));
            assertTrue(Files.exists(cloneDir.resolve("libs/child/vendor/nested/nested.txt")),
                    "Recursive update must check out the nested submodule");
        } finally {
            if (cloned != null) {
                cloned.shutdown();
            }
            deleteDirectory(cloneDir);
        }
    }

    private static Repository initRepo(Path dir, String file, String content, String message)
            throws Exception {
        Repository repo = Git.init().setDirectory(dir.toFile()).call().getRepository();
        configureIdentity(repo);
        Files.writeString(dir.resolve(file), content);
        try (Git git = new Git(repo)) {
            git.add().addFilepattern(file).call();
            git.commit().setMessage(message)
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }
        return repo;
    }

    private static void configureIdentity(Repository repo) throws Exception {
        repo.getConfig().setString("user", null, "name", "Test User");
        repo.getConfig().setString("user", null, "email", "test@example.com");
        repo.getConfig().save();
    }

    private static void advanceSubmoduleHead(Path submoduleDir, String file, String content, String message)
            throws Exception {
        Files.writeString(submoduleDir.resolve(file), content);
        try (Git git = Git.open(submoduleDir.toFile())) {
            git.add().addFilepattern(file).call();
            git.commit().setMessage(message)
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }
}
