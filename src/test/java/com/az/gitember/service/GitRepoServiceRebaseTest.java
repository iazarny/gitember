package com.az.gitember.service;

import com.az.gitember.dialog.InteractiveRebaseDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GitRepoService#rebaseContinue()} and
 * {@link GitRepoService#rebaseAbort()}.
 *
 * <p>Each test creates a real on-disk git repository in a temp directory,
 * sets up a two-branch history that produces a cherry-pick conflict when
 * rebased interactively, and then exercises continue / abort.
 *
 * <p>Repository topology used by {@link #startConflictingRebase()}:
 * <pre>
 *   master : A ──► C   (C: file.txt = "master version")
 *   feature: A ──► B   (B: file.txt = "feature version")
 *
 *   git checkout feature
 *   git rebase -i master   →  conflict applying B onto C
 * </pre>
 */
class GitRepoServiceRebaseTest {

    private Path        repoDir;
    private Repository  repository;
    private GitRepoService service;

    // ── Fixture ───────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        repoDir    = Files.createTempDirectory("gitember-rebase-test-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        service    = new GitRepoService(repository);

        // Identity required for commits
        repository.getConfig().setString("user", null, "name",  "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
    }

    @AfterEach
    void tearDown() throws Exception {
        repository.close();
        deleteDirectory(repoDir);
    }

    // ── Tests: getRepositoryState ─────────────────────────────────────────────

    @Test
    void getRepositoryState_freshRepo_returnsSafe() {
        assertEquals(RepositoryState.SAFE, service.getRepositoryState());
    }

    @Test
    void getRepositoryState_duringPausedInteractiveRebase_returnsRebasingInteractive()
            throws Exception {
        RebaseResult result = startConflictingRebase();

        assertEquals(RebaseResult.Status.STOPPED, result.getStatus(),
                "Expected rebase to stop on conflict");
        assertEquals(RepositoryState.REBASING_INTERACTIVE,
                service.getRepositoryState());
    }

    // ── Tests: rebaseAbort ────────────────────────────────────────────────────

    @Test
    void rebaseAbort_fromStoppedState_returnsAbortedStatus() throws Exception {
        startConflictingRebase();

        RebaseResult result = service.rebaseAbort();

        assertEquals(RebaseResult.Status.ABORTED, result.getStatus());
    }

    @Test
    void rebaseAbort_fromStoppedState_repositoryReturnedToSafeState() throws Exception {
        startConflictingRebase();

        service.rebaseAbort();

        assertEquals(RepositoryState.SAFE, service.getRepositoryState());
    }

    @Test
    void rebaseAbort_fromStoppedState_workingTreeRestoredToFeatureBranchContent()
            throws Exception {
        startConflictingRebase();

        service.rebaseAbort();

        // Normalise line endings: JGit on Windows may restore CRLF
        String content = Files.readString(repoDir.resolve("file.txt"))
                .replace("\r\n", "\n");
        assertEquals("feature version\n", content,
                "Abort must restore the working tree to the feature-branch state");
    }

    @Test
    void rebaseAbort_fromStoppedState_headRestoredToOriginalCommit() throws Exception {
        // Capture the feature-branch HEAD *after* commits are created but
        // *before* the rebase command runs
        String[] headBefore = new String[1];
        startConflictingRebaseCapturingHead(headBefore);

        service.rebaseAbort();

        String headAfter = repository.resolve("HEAD").getName();
        assertEquals(headBefore[0], headAfter,
                "Abort must restore HEAD to the commit it pointed to before the rebase");
    }

    // ── Tests: rebaseContinue ─────────────────────────────────────────────────

    @Test
    void rebaseContinue_afterResolvingConflict_returnsSuccessfulStatus() throws Exception {
        startConflictingRebase();
        resolveConflict("resolved content\n");

        RebaseResult result = service.rebaseContinue();

        assertTrue(result.getStatus().isSuccessful(),
                "Expected successful rebase after conflict resolution, got: "
                + result.getStatus());
    }

    @Test
    void rebaseContinue_afterResolvingConflict_repositoryReturnedToSafeState()
            throws Exception {
        startConflictingRebase();
        resolveConflict("resolved content\n");

        service.rebaseContinue();

        assertEquals(RepositoryState.SAFE, service.getRepositoryState());
    }

    @Test
    void rebaseContinue_afterResolvingConflict_workingTreeContainsResolvedContent()
            throws Exception {
        startConflictingRebase();
        resolveConflict("resolved content\n");

        service.rebaseContinue();

        assertEquals("resolved content\n",
                Files.readString(repoDir.resolve("file.txt")));
    }

    @Test
    void rebaseContinue_afterResolvingConflict_featureCommitIsOnTopOfMaster()
            throws Exception {
        startConflictingRebase();
        resolveConflict("resolved content\n");

        service.rebaseContinue();

        // After a successful rebase the feature commit's parent must be master HEAD
        try (RevWalk rw = new RevWalk(repository)) {
            RevCommit featureHead = rw.parseCommit(repository.resolve("HEAD"));
            String    masterHead  = repository.resolve("master").getName();

            assertEquals(masterHead, featureHead.getParent(0).getId().getName(),
                    "Rebased feature commit must be directly on top of master");
        }
    }

    // ── Tests: service.interactiveRebase() entry point ───────────────────────

    /**
     * The application always starts an interactive rebase through
     * {@link GitRepoService#interactiveRebase}, not via the raw JGit API.
     * This test verifies that the service method correctly stops on a conflict.
     */
    @Test
    void interactiveRebase_viaService_stopsOnConflict() throws Exception {
        String masterSha = setupTwoBranchConflictHistory();
        List<InteractiveRebaseDialog.RebaseStep> steps = buildPickSteps(masterSha);

        RebaseResult result = service.interactiveRebase(masterSha, steps);

        assertEquals(RebaseResult.Status.STOPPED, result.getStatus(),
                "service.interactiveRebase() must stop with STOPPED when a conflict is encountered");
    }

    /**
     * Full round-trip: start via service → resolve conflict → continue via service → success.
     * This is the exact code path exercised by InteractiveRebaseHandler +
     * InteractiveContinueAbortDialog in the real application.
     */
    @Test
    void interactiveRebase_viaService_continueAfterResolve_returnsSuccessful() throws Exception {
        String masterSha = setupTwoBranchConflictHistory();
        List<InteractiveRebaseDialog.RebaseStep> steps = buildPickSteps(masterSha);

        service.interactiveRebase(masterSha, steps);
        resolveConflict("resolved content\n");

        RebaseResult result = service.rebaseContinue();

        assertTrue(result.getStatus().isSuccessful(),
                "rebaseContinue() after service.interactiveRebase() must succeed, got: "
                + result.getStatus());
    }

    @Test
    void interactiveRebase_viaService_continueAfterResolve_repositoryReturnedToSafeState()
            throws Exception {
        String masterSha = setupTwoBranchConflictHistory();
        List<InteractiveRebaseDialog.RebaseStep> steps = buildPickSteps(masterSha);

        service.interactiveRebase(masterSha, steps);
        resolveConflict("resolved content\n");
        service.rebaseContinue();

        assertEquals(RepositoryState.SAFE, service.getRepositoryState());
    }

    // ── Tests: continue called before conflicts are staged ────────────────────

    /**
     * When the user clicks "Continue" before staging all resolved files, JGit
     * throws {@code UnmergedPathsException} (unmerged paths remain in the index).
     *
     * <p>This is the root cause of the bug: the dialog's {@code performOperation(true)}
     * caught this exception and called {@code disposeAndNotify()}, dismissing the dialog
     * while the rebase was still paused.  The fix is to detect that the repository is
     * still in {@link RepositoryState#REBASING_INTERACTIVE} after the exception and
     * keep the dialog open so the user can retry.
     */
    @Test
    void rebaseContinue_withUnresolvedConflicts_throwsException() throws Exception {
        startConflictingRebase();
        // Intentionally do NOT stage the resolved file — conflict markers still in the index

        assertThrows(Exception.class, () -> service.rebaseContinue(),
                "rebaseContinue() with unresolved conflicts must throw (UnmergedPathsException)");
    }

    /**
     * After the exception from a failed continue the repository must still be in
     * {@link RepositoryState#REBASING_INTERACTIVE} — the rebase has not been finished
     * or abandoned, so the dialog must stay open for the user to fix and retry.
     */
    @Test
    void rebaseContinue_withUnresolvedConflicts_repositoryRemainsInRebasingInteractiveState()
            throws Exception {
        startConflictingRebase();
        // Intentionally do NOT stage the resolved file

        try { service.rebaseContinue(); } catch (Exception ignored) { /* expected throw */ }

        assertEquals(RepositoryState.REBASING_INTERACTIVE, service.getRepositoryState(),
                "Repository must remain in REBASING_INTERACTIVE after a failed continue");
    }

    // ── Tests: multiple conflicting commits ───────────────────────────────────

    /**
     * Two feature commits both conflict with master.  After resolving the first
     * conflict, {@code rebaseContinue()} must return {@code STOPPED} again (not
     * {@code SUCCESSFUL}) so the dialog can handle the second conflict.
     *
     * <pre>
     *   master : A ──► C  (file.txt = "master version")
     *   feature: A ──► B  (file.txt = "feature-B") ──► D (file.txt = "feature-D")
     *
     *   rebase feature onto master:
     *     1st continue resolves B → STOPPED (D also conflicts)
     *     2nd continue resolves D → SUCCESSFUL
     * </pre>
     */
    @Test
    void rebaseContinue_firstOfTwoConflicts_returnsStoppedNotSuccessful() throws Exception {
        startTwoConflictRebase();
        resolveConflict("resolved-B\n");

        RebaseResult firstContinue = service.rebaseContinue();

        assertEquals(RebaseResult.Status.STOPPED, firstContinue.getStatus(),
                "After resolving the first of two conflicts, rebaseContinue() must return STOPPED "
                + "(second conflict pending), not SUCCESSFUL");
    }

    @Test
    void rebaseContinue_bothConflictsResolved_returnsSuccessful() throws Exception {
        startTwoConflictRebase();
        resolveConflict("resolved-B\n");
        service.rebaseContinue();           // first continue → STOPPED (second conflict)
        resolveConflict("resolved-D\n");

        RebaseResult secondContinue = service.rebaseContinue();

        assertTrue(secondContinue.getStatus().isSuccessful(),
                "After resolving both conflicts, second rebaseContinue() must succeed, got: "
                + secondContinue.getStatus());
    }

    @Test
    void rebaseContinue_bothConflictsResolved_repositoryReturnedToSafeState() throws Exception {
        startTwoConflictRebase();
        resolveConflict("resolved-B\n");
        service.rebaseContinue();
        resolveConflict("resolved-D\n");
        service.rebaseContinue();

        assertEquals(RepositoryState.SAFE, service.getRepositoryState());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the two-branch history and kicks off an interactive rebase that
     * stops with a conflict ({@link RebaseResult.Status#STOPPED}).
     *
     * <pre>
     *   master : A ──► C  (file.txt = "master version")
     *   feature: A ──► B  (file.txt = "feature version")
     *
     *   checkout feature → rebase -i master → conflict
     * </pre>
     */
    private RebaseResult startConflictingRebase() throws Exception {
        return startConflictingRebaseCapturingHead(null);
    }

    /**
     * Same as {@link #startConflictingRebase()} but also records the feature
     * branch HEAD SHA (before the rebase starts) into {@code headHolder[0]}.
     * Pass {@code null} if you don't need the SHA.
     */
    private RebaseResult startConflictingRebaseCapturingHead(String[] headHolder)
            throws Exception {
        try (Git git = new Git(repository)) {

            // Commit A on master — shared ancestor
            writeFile("file.txt", "original content\n");
            git.add().addFilepattern("file.txt").call();
            RevCommit commitA = git.commit().setMessage("initial commit").call();

            // Commit C on master — changes file.txt
            writeFile("file.txt", "master version\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("master change").call();

            // Commit B on feature branch (from A) — conflicting change to file.txt
            git.checkout().setCreateBranch(true).setName("feature")
                    .setStartPoint(commitA).call();
            writeFile("file.txt", "feature version\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("feature change").call();

            // Capture HEAD now — after all commits, before the rebase command
            if (headHolder != null) {
                headHolder[0] = repository.resolve("HEAD").getName();
            }

            // Interactive rebase of feature onto master → conflict applying B onto C
            return git.rebase()
                    .setUpstream("master")
                    .runInteractively(new RebaseCommand.InteractiveHandler() {
                        @Override
                        public void prepareSteps(List<RebaseTodoLine> steps) {
                            // Keep all steps as PICK — the conflict happens naturally
                        }
                        @Override
                        public String modifyCommitMessage(String commit) { return commit; }
                    })
                    .call();
        }
    }

    /**
     * Writes {@code content} to {@code file.txt}, then stages it — simulating
     * what a developer does after manually resolving a merge conflict.
     */
    private void resolveConflict(String content) throws Exception {
        writeFile("file.txt", content);
        try (Git git = new Git(repository)) {
            git.add().addFilepattern("file.txt").call();
        }
    }

    /**
     * Creates the two-branch history (A→C on master, A→B on feature) but does
     * <em>not</em> start the rebase.  Returns the full SHA of master HEAD (C)
     * for use as the upstream in {@link GitRepoService#interactiveRebase}.
     *
     * <pre>
     *   master : A ──► C  (file.txt = "master version")
     *   feature: A ──► B  (file.txt = "feature version")   ← HEAD
     * </pre>
     */
    private String setupTwoBranchConflictHistory() throws Exception {
        try (Git git = new Git(repository)) {
            writeFile("file.txt", "original content\n");
            git.add().addFilepattern("file.txt").call();
            RevCommit commitA = git.commit().setMessage("initial commit").call();

            writeFile("file.txt", "master version\n");
            git.add().addFilepattern("file.txt").call();
            RevCommit commitC = git.commit().setMessage("master change").call();
            String masterSha = commitC.getName();

            git.checkout().setCreateBranch(true).setName("feature")
                    .setStartPoint(commitA).call();
            writeFile("file.txt", "feature version\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("feature change").call();

            return masterSha;
        }
    }

    /**
     * Builds a list of {@code PICK} {@link InteractiveRebaseDialog.RebaseStep}s for
     * all commits between HEAD and {@code masterSha} (i.e. the commits that would be
     * rebased), in the newest-first order expected by
     * {@link GitRepoService#interactiveRebase}.
     */
    private List<InteractiveRebaseDialog.RebaseStep> buildPickSteps(String masterSha)
            throws Exception {
        return service.getCommitsInRange(masterSha).stream()
                .map(c -> new InteractiveRebaseDialog.RebaseStep(
                        InteractiveRebaseDialog.RebaseAction.PICK,
                        c.getName(), c.getShortMessage()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a history where <em>two</em> feature commits both conflict with master,
     * and starts the interactive rebase (stops at the first conflict).
     *
     * <pre>
     *   master : A ──► C  (file.txt = "master version")
     *   feature: A ──► B  (file.txt = "feature-B")
     *              └──► D  (file.txt = "feature-D")   ← HEAD
     *
     *   rebase feature onto master → stops at B (conflict)
     * </pre>
     *
     * After resolving B, a second {@link GitRepoService#rebaseContinue()} stops at D
     * (which also conflicts because file.txt baseline shifted under it).
     */
    private void startTwoConflictRebase() throws Exception {
        try (Git git = new Git(repository)) {
            writeFile("file.txt", "original content\n");
            git.add().addFilepattern("file.txt").call();
            RevCommit commitA = git.commit().setMessage("initial commit").call();

            writeFile("file.txt", "master version\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("master change").call();

            git.checkout().setCreateBranch(true).setName("feature")
                    .setStartPoint(commitA).call();

            writeFile("file.txt", "feature-B\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("feature change B").call();

            writeFile("file.txt", "feature-D\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("feature change D").call();

            git.rebase()
                    .setUpstream("master")
                    .runInteractively(new RebaseCommand.InteractiveHandler() {
                        @Override
                        public void prepareSteps(List<RebaseTodoLine> steps) { /* keep all as PICK */ }
                        @Override
                        public String modifyCommitMessage(String msg) { return msg; }
                    })
                    .call();
        }
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
