package com.az.gitember.service;

import com.az.gitember.data.ScmReflogEntry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitRepoServiceReflogTest {

    private Path repoDir;
    private Repository repository;
    private GitRepoService service;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-reflog-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        service = new GitRepoService(repository);
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
        deleteDirectory(repoDir);
    }

    @Test
    void getReflogEntries_afterCommit_containsHeadEntry() throws Exception {
        makeInitialCommit();
        List<ScmReflogEntry> entries = service.getReflogEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.stream().anyMatch(e ->
                e.getSelector() != null && e.getSelector().startsWith("HEAD@{")));
    }

    @Test
    void recoverDeletedBranch_recreatesBranchAtCheckoutTip() throws Exception {
        makeInitialCommit();
        String head = repository.getBranch();
        service.checkoutBranch(head, "feature", null);
        writeFile("feat.txt", "on feature\n");
        service.addFileToCommitStage("feat.txt");
        RevCommit featureTip = service.commit("feature work", "Test User", "test@example.com");
        service.checkoutBranch(head, null);
        service.deleteLocalBranch("feature");
        assertNull(repository.exactRef("refs/heads/feature"));

        service.recoverDeletedBranch("feature", featureTip.getName());

        assertNotNull(repository.exactRef("refs/heads/feature"));
        assertEquals(featureTip.getId(), repository.exactRef("refs/heads/feature").getObjectId());
    }

    @Test
    void recoverHardReset_restoresPreviousHead() throws Exception {
        makeInitialCommit();
        writeFile("n.txt", "second\n");
        service.addFileToCommitStage("n.txt");
        RevCommit second = service.commit("second", "Test User", "test@example.com");
        service.resetBranch(second.getParent(0), ResetCommand.ResetType.HARD, null);
        assertFalse(Files.exists(repoDir.resolve("n.txt")));

        List<ScmReflogEntry> afterReset = service.getReflogEntries();
        assertFalse(afterReset.isEmpty());
        String recoverSha = afterReset.stream()
                .filter(e -> ScmReflogEntry.KIND_RESET.equals(e.getKind()))
                .map(ScmReflogEntry::recoveryCommitId)
                .findFirst()
                .orElse(second.getName());
        service.recoverHardReset(recoverSha, null);

        assertEquals(second.getId(), repository.resolve("HEAD"));
        assertTrue(Files.exists(repoDir.resolve("n.txt")));
    }

    @Test
    void recoverDeletedCommit_createsBranchAtLostCommit() throws Exception {
        makeInitialCommit();
        writeFile("lost.txt", "gone\n");
        service.addFileToCommitStage("lost.txt");
        RevCommit lost = service.commit("will reset", "Test User", "test@example.com");
        service.resetBranch(lost.getParent(0), ResetCommand.ResetType.HARD, null);

        service.recoverDeletedCommit("recovered-lost", lost.getName());

        assertNotNull(repository.exactRef("refs/heads/recovered-lost"));
        assertEquals(lost.getId(), repository.exactRef("refs/heads/recovered-lost").getObjectId());
    }

    @Test
    void recoverStash_putsDroppedStashBackOnStack() throws Exception {
        makeInitialCommit();
        writeFile("s.txt", "base\n");
        service.addFileToCommitStage("s.txt");
        service.commit("base", "Test User", "test@example.com");
        writeFile("s.txt", "stashed\n");
        service.stash("keep me");
        String sha = repository.resolve("refs/stash").getName();
        service.deleteStash(0);
        assertNull(repository.exactRef("refs/stash"));

        service.recoverStash(sha);

        assertNotNull(repository.exactRef("refs/stash"));
        assertEquals(sha, repository.exactRef("refs/stash").getObjectId().getName());
    }

    @Test
    void classify_parsesCheckoutAndResetComments() {
        assertEquals(ScmReflogEntry.KIND_RESET,
                ScmReflogEntry.classify("HEAD", "reset: moving to HEAD~1"));
        assertEquals(ScmReflogEntry.KIND_CHECKOUT,
                ScmReflogEntry.classify("HEAD", "checkout: moving from feature to master"));
        assertEquals(ScmReflogEntry.KIND_STASH,
                ScmReflogEntry.classify("refs/stash", "WIP on master: abc"));
        ScmReflogEntry checkout = new ScmReflogEntry();
        checkout.setComment("checkout: moving from feature to master");
        checkout.setKind(ScmReflogEntry.KIND_CHECKOUT);
        assertEquals("feature", checkout.suggestedBranchName());
    }

    private RevCommit makeInitialCommit() throws Exception {
        writeFile("init.txt", "init\n");
        service.addFileToCommitStage("init.txt");
        return service.commit("initial commit", "Test User", "test@example.com");
    }

    private void writeFile(String name, String content) throws IOException {
        Files.writeString(repoDir.resolve(name), content);
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (dir != null && Files.exists(dir)) {
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
}
