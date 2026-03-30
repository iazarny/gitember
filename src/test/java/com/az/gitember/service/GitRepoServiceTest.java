package com.az.gitember.service;

import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GitRepoService} covering staging, commits,
 * branching, merge, stash, cherry-pick, reset, revert, tags, file history,
 * statuses, and commit-range queries.
 *
 * <p>Each test uses a real on-disk git repository in a temp directory.
 */
class GitRepoServiceTest {

    private Path           repoDir;
    private Repository     repository;
    private GitRepoService service;

    // ── Fixture ───────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        repoDir    = Files.createTempDirectory("gitember-test-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        service    = new GitRepoService(repository);

        repository.getConfig().setString("user", null, "name",  "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
    }

    @AfterEach
    void tearDown() throws Exception {
        repository.close();
        deleteDirectory(repoDir);
    }

    // ── Staging ───────────────────────────────────────────────────────────────

    @Test
    void addFileToCommitStage_newFile_fileAppearsAsAdded() throws Exception {
        writeFile("a.txt", "hello\n");

        service.addFileToCommitStage("a.txt");

        try (Git git = new Git(repository)) {
            assertTrue(git.status().call().getAdded().contains("a.txt"),
                    "Staged file should appear in 'added' set");
        }
    }

    @Test
    void removeFileFromCommitStage_stagedFile_fileMovesToUntracked() throws Exception {
        // First make an initial commit so HEAD exists
        makeInitialCommit();

        writeFile("b.txt", "world\n");
        service.addFileToCommitStage("b.txt");

        service.removeFileFromCommitStage("b.txt");

        try (Git git = new Git(repository)) {
            assertFalse(git.status().call().getAdded().contains("b.txt"),
                    "Unstaged file must no longer be in 'added' set");
            assertTrue(git.status().call().getUntracked().contains("b.txt"),
                    "Unstaged file must be 'untracked' again");
        }
    }

    // ── Commit ────────────────────────────────────────────────────────────────

    @Test
    void commit_stagedFile_createsNewCommit() throws Exception {
        writeFile("c.txt", "content\n");
        service.addFileToCommitStage("c.txt");

        RevCommit commit = service.commit("first commit", "Author", "author@test.com");

        assertNotNull(commit);
        assertEquals("first commit", commit.getShortMessage());
    }

    @Test
    void commit_withCustomAuthor_storesCorrectAuthorIdentity() throws Exception {
        writeFile("d.txt", "data\n");
        service.addFileToCommitStage("d.txt");

        RevCommit commit = service.commit("custom author", "Alice", "alice@example.com");

        assertEquals("Alice", commit.getAuthorIdent().getName());
        assertEquals("alice@example.com", commit.getAuthorIdent().getEmailAddress());
    }

    @Test
    void commit_withSeparateCommitter_storesDistinctCommitterIdentity() throws Exception {
        writeFile("e.txt", "data\n");
        service.addFileToCommitStage("e.txt");

        RevCommit commit = service.commit(
                "committer test", "Alice", "alice@example.com",
                "Bob", "bob@example.com");

        assertEquals("Alice", commit.getAuthorIdent().getName());
        assertEquals("Bob", commit.getCommitterIdent().getName());
    }

    // ── getAllFiles ───────────────────────────────────────────────────────────

    @Test
    void getAllFiles_afterSingleCommit_returnsCommittedFileName() throws Exception {
        writeFile("readme.txt", "README\n");
        service.addFileToCommitStage("readme.txt");
        service.commit("add readme", "U", "u@u.com");

        Set<String> files = service.getAllFiles();

        assertTrue(files.contains("readme.txt"));
    }

    @Test
    void getAllFiles_afterTwoCommits_returnsBothFiles() throws Exception {
        writeFile("f1.txt", "one\n");
        service.addFileToCommitStage("f1.txt");
        service.commit("add f1", "U", "u@u.com");

        writeFile("f2.txt", "two\n");
        service.addFileToCommitStage("f2.txt");
        service.commit("add f2", "U", "u@u.com");

        Set<String> files = service.getAllFiles();

        assertTrue(files.contains("f1.txt"));
        assertTrue(files.contains("f2.txt"));
    }

    // ── Branching ─────────────────────────────────────────────────────────────

    @Test
    void createBranch_fromMaster_branchRefExists() throws Exception {
        makeInitialCommit();

        service.createBranch("master", "feature");

        assertNotNull(repository.exactRef("refs/heads/feature"),
                "Feature branch ref must be present in repository");
    }

    @Test
    void checkoutBranch_existingBranch_headPointsToNewBranch() throws Exception {
        makeInitialCommit();
        service.createBranch("master", "feature");

        service.checkoutBranch("feature", null);

        assertEquals("feature", repository.getBranch());
    }

    @Test
    void deleteLocalBranch_existingBranch_branchRefRemoved() throws Exception {
        makeInitialCommit();
        service.createBranch("master", "to-delete");

        service.deleteLocalBranch("to-delete");

        assertNull(repository.exactRef("refs/heads/to-delete"),
                "Deleted branch ref must not exist");
    }

    // ── Merge ─────────────────────────────────────────────────────────────────

    @Test
    void mergeBranch_fastForward_branchHeadAdvances() throws Exception {
        makeInitialCommit();
        // Create and checkout feature branch
        service.checkoutBranch("master", "feature", null);
        writeFile("feature.txt", "feature\n");
        service.addFileToCommitStage("feature.txt");
        RevCommit featureCommit = service.commit("feature commit", "U", "u@u.com");

        // Switch back to master and merge
        service.checkoutBranch("master", null);
        var result = service.mergeBranch(
                "refs/heads/feature", "merge feature",
                false, MergeCommand.FastForwardMode.FF);

        assertTrue(result.getMergeStatus().isSuccessful(),
                "Fast-forward merge must succeed");
        assertEquals(featureCommit.getId(),
                repository.resolve("HEAD"),
                "After FF merge master HEAD must equal the feature commit");
    }

    // ── branchDiff ────────────────────────────────────────────────────────────

    @Test
    void branchDiff_twoBranchesWithDifferentFiles_returnsOneDiffEntry() throws Exception {
        makeInitialCommit();
        service.checkoutBranch("master", "branch-a", null);
        writeFile("only-in-a.txt", "a\n");
        service.addFileToCommitStage("only-in-a.txt");
        service.commit("branch-a commit", "U", "u@u.com");
        service.checkoutBranch("master", null);

        var diffs = service.branchDiff(
                "refs/heads/master", "refs/heads/branch-a", null);

        assertEquals(1, diffs.size());
        assertTrue(diffs.get(0).getNewPath().contains("only-in-a.txt")
                || diffs.get(0).getOldPath().contains("only-in-a.txt"));
    }

    // ── getCommitsInRange ─────────────────────────────────────────────────────

    @Test
    void getCommitsInRange_twoCommitsAheadOfBase_returnsTwo() throws Exception {
        RevCommit base = makeInitialCommit();

        writeFile("x.txt", "x\n");
        service.addFileToCommitStage("x.txt");
        service.commit("second", "U", "u@u.com");

        writeFile("y.txt", "y\n");
        service.addFileToCommitStage("y.txt");
        service.commit("third", "U", "u@u.com");

        List<RevCommit> range = service.getCommitsInRange(base.getName());

        assertEquals(2, range.size());
    }

    @Test
    void getCommitsInRange_noCommitsAheadOfBase_returnsEmpty() throws Exception {
        RevCommit base = makeInitialCommit();

        List<RevCommit> range = service.getCommitsInRange(base.getName());

        assertTrue(range.isEmpty());
    }

    // ── Stash ─────────────────────────────────────────────────────────────────

    @Test
    void stash_modifiedFile_stashListHasOneEntry() throws Exception {
        makeInitialCommit();
        writeFile("s.txt", "stash me\n");
        service.addFileToCommitStage("s.txt");
        service.commit("add s.txt", "U", "u@u.com");
        writeFile("s.txt", "modified\n");

        service.stash("my stash");

        assertEquals(1, service.getStashList().size());
    }

    @Test
    void stash_modifiedFile_workingTreeReverted() throws Exception {
        makeInitialCommit();
        writeFile("s2.txt", "original\n");
        service.addFileToCommitStage("s2.txt");
        service.commit("add s2", "U", "u@u.com");
        writeFile("s2.txt", "dirty\n");

        service.stash(null);

        String content = Files.readString(repoDir.resolve("s2.txt"))
                .replace("\r\n", "\n");
        assertEquals("original\n", content,
                "Stash must restore the working tree to the last committed state");
    }

    @Test
    void deleteStash_onlyEntry_stashListBecomesEmpty() throws Exception {
        makeInitialCommit();
        writeFile("del.txt", "v1\n");
        service.addFileToCommitStage("del.txt");
        service.commit("v1", "U", "u@u.com");
        writeFile("del.txt", "v2\n");
        service.stash("to delete");

        service.deleteStash(0);

        assertTrue(service.getStashList().isEmpty());
    }

    @Test
    void applyStash_previouslySaved_restoresModifiedContent() throws Exception {
        makeInitialCommit();
        writeFile("ap.txt", "base\n");
        service.addFileToCommitStage("ap.txt");
        service.commit("base", "U", "u@u.com");
        writeFile("ap.txt", "stashed change\n");
        service.stash("apply-me");

        service.applyStash("stash@{0}");

        String content = Files.readString(repoDir.resolve("ap.txt"))
                .replace("\r\n", "\n");
        assertEquals("stashed change\n", content,
                "Apply stash must restore the stashed content");
    }

    // ── Cherry-pick ───────────────────────────────────────────────────────────

    @Test
    void cherryPick_commitFromAnotherBranch_changesAppliedWithoutCommit() throws Exception {
        makeInitialCommit();
        // Create feature branch and add a commit
        service.checkoutBranch("master", "cherry-src", null);
        writeFile("cherry.txt", "cherry content\n");
        service.addFileToCommitStage("cherry.txt");
        RevCommit cherry = service.commit("cherry commit", "U", "u@u.com");

        // Switch back to master, cherry-pick (setNoCommit=true)
        service.checkoutBranch("master", null);
        var result = service.cherryPick(cherry);

        assertEquals(org.eclipse.jgit.api.CherryPickResult.CherryPickStatus.OK,
                result.getStatus(),
                "Cherry-pick must succeed");
        assertTrue(Files.exists(repoDir.resolve("cherry.txt")),
                "Cherry-picked file must be present in working tree");
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    @Test
    void resetBranch_hardResetToParent_headMovesBack() throws Exception {
        RevCommit parent = makeInitialCommit();
        writeFile("extra.txt", "extra\n");
        service.addFileToCommitStage("extra.txt");
        service.commit("extra commit", "U", "u@u.com");

        service.resetBranch(parent, ResetCommand.ResetType.HARD, null);

        assertEquals(parent.getId(), repository.resolve("HEAD"),
                "After hard reset HEAD must equal the target commit");
    }

    @Test
    void resetBranch_hardReset_removesFileFromWorkingTree() throws Exception {
        RevCommit parent = makeInitialCommit();
        writeFile("gone.txt", "gone\n");
        service.addFileToCommitStage("gone.txt");
        service.commit("add gone", "U", "u@u.com");

        service.resetBranch(parent, ResetCommand.ResetType.HARD, null);

        assertFalse(Files.exists(repoDir.resolve("gone.txt")),
                "Hard reset must remove files not present in target commit");
    }

    // ── Revert ────────────────────────────────────────────────────────────────

    @Test
    void revertCommit_singleCommit_createsRevertCommit() throws Exception {
        makeInitialCommit();
        writeFile("revert-me.txt", "value\n");
        service.addFileToCommitStage("revert-me.txt");
        RevCommit toRevert = service.commit("add revert-me", "U", "u@u.com");
        String headBeforeRevert = repository.resolve("HEAD").getName();

        service.revertCommit(toRevert, null);

        String headAfterRevert = repository.resolve("HEAD").getName();
        assertNotEquals(headBeforeRevert, headAfterRevert,
                "Revert must produce a new commit on HEAD");
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @Test
    void createTag_validName_tagAppearsInTagList() throws Exception {
        makeInitialCommit();

        service.createTag("v1.0");

        List<com.az.gitember.data.ScmBranch> tags = service.getTags();
        assertTrue(tags.stream().anyMatch(t -> t.getShortName().equals("refs/tags/v1.0")
                || t.getFullName().equals("refs/tags/v1.0")),
                "Created tag must be returned by getTags()");
    }

    @Test
    void deleteLocalTag_existingTag_tagNoLongerInTagList() throws Exception {
        makeInitialCommit();
        service.createTag("v2.0");

        service.deleteLocalTag("refs/tags/v2.0");

        List<com.az.gitember.data.ScmBranch> tags = service.getTags();
        assertTrue(tags.stream().noneMatch(t -> t.getFullName().contains("v2.0")),
                "Deleted tag must not appear in tag list");
    }

    @Test
    void getTags_freshRepo_returnsEmptyList() throws Exception {
        makeInitialCommit();

        assertTrue(service.getTags().isEmpty());
    }

    // ── File history ──────────────────────────────────────────────────────────

    @Test
    void getFileHistory_fileModifiedTwice_returnsTwoRevisions() throws Exception {
        writeFile("hist.txt", "v1\n");
        service.addFileToCommitStage("hist.txt");
        service.commit("hist v1", "U", "u@u.com");

        writeFile("hist.txt", "v2\n");
        service.addFileToCommitStage("hist.txt");
        service.commit("hist v2", "U", "u@u.com");

        List<ScmRevisionInformation> history = service.getFileHistory("hist.txt");

        assertEquals(2, history.size());
    }

    @Test
    void getFileHistory_fileNeverChanged_returnsOneRevision() throws Exception {
        writeFile("stable.txt", "once\n");
        service.addFileToCommitStage("stable.txt");
        service.commit("stable", "U", "u@u.com");

        List<ScmRevisionInformation> history = service.getFileHistory("stable.txt");

        assertEquals(1, history.size());
    }

    // ── getStatuses ───────────────────────────────────────────────────────────

    @Test
    void getStatuses_untrackedFile_containsUntrackedEntry() throws Exception {
        makeInitialCommit();
        writeFile("untracked.txt", "untracked\n");

        List<ScmItem> statuses = service.getStatuses(null, false);

        assertTrue(statuses.stream().anyMatch(
                s -> s.getShortName().equals("untracked.txt")
                        && s.getAttribute().getStatus() == ScmItem.Status.UNTRACKED));
    }

    @Test
    void getStatuses_stagedNewFile_containsAddedEntry() throws Exception {
        makeInitialCommit();
        writeFile("staged.txt", "staged\n");
        service.addFileToCommitStage("staged.txt");

        List<ScmItem> statuses = service.getStatuses(null, false);

        assertTrue(statuses.stream().anyMatch(
                s -> s.getShortName().equals("staged.txt")
                        && s.getAttribute().getStatus() == ScmItem.Status.ADDED));
    }

    @Test
    void getStatuses_modifiedTrackedFile_containsModifiedEntry() throws Exception {
        makeInitialCommit();
        writeFile("mod.txt", "original\n");
        service.addFileToCommitStage("mod.txt");
        service.commit("add mod", "U", "u@u.com");
        writeFile("mod.txt", "changed\n");

        List<ScmItem> statuses = service.getStatuses(null, false);

        assertTrue(statuses.stream().anyMatch(
                s -> s.getShortName().equals("mod.txt")
                        && s.getAttribute().getStatus() == ScmItem.Status.MODIFIED));
    }

    @Test
    void getStatuses_cleanWorkingTree_returnsEmptyList() throws Exception {
        makeInitialCommit();

        List<ScmItem> statuses = service.getStatuses(null, false);

        assertTrue(statuses.isEmpty(), "Clean working tree must yield no status entries");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates an initial commit with a single file so HEAD is valid. */
    private RevCommit makeInitialCommit() throws Exception {
        writeFile("init.txt", "init\n");
        service.addFileToCommitStage("init.txt");
        return service.commit("initial commit", "Test User", "test@example.com");
    }

    private void writeFile(String name, String content) throws IOException {
        Files.writeString(repoDir.resolve(name), content);
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
