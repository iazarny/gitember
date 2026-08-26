package com.az.gitember.ui.workspace;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.dialog.CommitDialog;
import com.az.gitember.dialog.MergeResultDialog;
import com.az.gitember.dialog.WorkspaceDialog;
import com.az.gitember.dialog.WorkspaceMergeDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end coverage of the workspace feature over two real repositories: create a workspace
 * from the welcome screen, create a branch in every repository at once, commit the whole
 * workspace, commit again on {@code master}, then merge the branch back in every repository —
 * all through the UI — and assert both repositories end up with both files.
 *
 * <p>Steps that are prerequisites rather than the behaviour under test are done off-UI, the same
 * way {@link SwingUiTestBase#openRepo(Path)} skips the {@code JFileChooser}: the repositories
 * themselves, registering them as known projects (so the workspace dialog's
 * <em>Add Existing Project…</em> can list them instead of going through a directory chooser),
 * writing/staging the files, and checking {@code master} back out (there is no workspace-wide
 * checkout action in the UI — only a per-repository one in the branch tree).
 */
class WorkspaceLifecycleUiTest extends SwingUiTestBase {

    private static final String WORKSPACE_NAME = "ws-test-1";
    private static final String BRANCH = "branch1";
    private static final String MASTER = "master";
    private static final String BRANCH_FILE = "branch1-file1.txt";
    private static final String MASTER_FILE = "file1.txt";

    private Path repoOne;
    private Path repoTwo;



    @AfterEach
    void deleteWorkspaceRepos() throws Exception {
        GitFixtures.delete(repoOne);
        GitFixtures.delete(repoTwo);
    }

    @Test
    void workspace_createBranch_commit_andMerge_acrossAllRepositories() throws Exception {
        repoOne = newRepoWithInitialCommit("gitember-ws-repo-one-");
        repoTwo = newRepoWithInitialCommit("gitember-ws-repo-two-");
        registerAsKnownProjects(repoOne, repoTwo);

        openWorkspaceWithBothRepos();
        createBranchInEveryRepository();

        // Commit branch1-file1.txt on branch1 in both repositories, in a single workspace commit.
        GitFixtures.writeAndStageFile(repoOne, BRANCH_FILE, "one on " + BRANCH + "\n");
        GitFixtures.writeAndStageFile(repoTwo, BRANCH_FILE, "two on " + BRANCH + "\n");
        commitWorkspace("Add " + BRANCH_FILE);
        assertCommitted(repoOne, BRANCH_FILE);
        assertCommitted(repoTwo, BRANCH_FILE);

        checkoutInEveryRepository(MASTER);

        // Commit file1.txt on master, so master and branch1 have diverged in both repositories.
        GitFixtures.writeAndStageFile(repoOne, MASTER_FILE, "one on " + MASTER + "\n");
        GitFixtures.writeAndStageFile(repoTwo, MASTER_FILE, "two on " + MASTER + "\n");
        commitWorkspace("Add " + MASTER_FILE);
        assertCommitted(repoOne, MASTER_FILE);
        assertCommitted(repoTwo, MASTER_FILE);

        mergeBranchInEveryRepository();

        assertBothFilesPresentOnMaster(repoOne);
        assertBothFilesPresentOnMaster(repoTwo);
    }

    // ── Steps ────────────────────────────────────────────────────────────────────

    private Path newRepoWithInitialCommit(String namePrefix) throws Exception {
        Path dir = GitFixtures.newInitializedRepo(namePrefix, MASTER);
        GitFixtures.commitFile(dir, "readme.txt", "initial\n", "Initial commit");
        return dir;
    }

    /**
     * Puts both repositories in the recent-projects list the workspace dialog offers under
     * <em>Add Existing Project…</em>. Without this the only way to add a repository is the
     * directory {@code JFileChooser}, which needs a real filesystem click to select a folder.
     */
    private void registerAsKnownProjects(Path... repos) {
        execute(() -> {
            Settings settings = Context.getSettings();
            for (Path repo : repos) {
                settings.addRecentProject(settings.getOrCreateProject(repo.toString()));
            }
            return null;
        });
    }

    /** File -> Init Workspace... -> name it, add both repositories, Open. */
    private void openWorkspaceWithBothRepos() {
        window.menuItem("initWorkspaceItem").click();

        DialogFixture workspaceDialog = WindowFinder.findDialog(WorkspaceDialog.class)
                .withTimeout(15_000).using(robot);
        workspaceDialog.textBox("workspaceNameField").setText(WORKSPACE_NAME);

        workspaceDialog.button("addExistingProjectButton").click();
        JOptionPaneFixture projectChooser = JOptionPaneFinder.findOptionPane().using(robot);
        projectChooser.list().requireItemCount(2);
        projectChooser.list().selectItems(0, 1);
        projectChooser.okButton().click();

        workspaceDialog.list("workspaceProjectList").requireItemCount(2);
        workspaceDialog.button("openWorkspaceButton").click();

        Pause.pause(new Condition("workspace " + WORKSPACE_NAME + " to become the active view") {
            @Override
            public boolean test() {
                return Context.getWorkspace() != null
                        && WORKSPACE_NAME.equals(Context.getWorkspace().getName())
                        && Context.getWorkspace().getProjects().size() == 2
                        && execute(() -> MainFrame.getInstance().isWorkspaceActive());
            }
        }, Timeout.timeout(20_000));
    }

    /** Workspace -> Branch ...: creates and checks out {@value #BRANCH} in every repository. */
    private void createBranchInEveryRepository() {
        window.menuItem("workspaceBranchCreateItem").click();

        JOptionPaneFixture branchNameInput = JOptionPaneFinder.findOptionPane().using(robot);
        branchNameInput.textBox().enterText(BRANCH);
        branchNameInput.okButton().click();

        awaitBranchCheckedOut(BRANCH);
    }

    /**
     * Workspace -> Commit ...: commits every repository's staged changes with one shared message.
     * The Commit action is only enabled once the dashboard has seen the staged changes, so the
     * dashboard is refreshed first and the item's enabled state is waited on.
     */
    private void commitWorkspace(String message) {
        refreshDashboard();

        JMenuItemFixture commitItem = window.menuItem("workspaceCommitItem");
        Pause.pause(new Condition("workspace Commit action to be enabled") {
            @Override
            public boolean test() {
                return execute(() -> commitItem.target().isEnabled());
            }
        }, Timeout.timeout(20_000));
        commitItem.click();

        DialogFixture commitDialog = WindowFinder.findDialog(CommitDialog.class)
                .withTimeout(15_000).using(robot);
        commitDialog.textBox("commitMessageArea").enterText(message);
        commitDialog.button("commitButton").click();

        Pause.pause(new Condition("commit dialog to close") {
            @Override
            public boolean test() {
                return !commitDialog.target().isShowing();
            }
        }, Timeout.timeout(20_000));
    }

    /**
     * Checks {@code branch} out in every workspace repository through each project's own
     * {@link com.az.gitember.service.GitRepoService} — the same instance the UI uses, so the
     * dashboard and the merge dialog see the switch — because the UI has no workspace-wide
     * checkout action.
     */
    private void checkoutInEveryRepository(String branch) throws Exception {
        for (Project project : Context.getWorkspace().getProjects()) {
            project.getGitRepoService().checkoutBranch(branch, null);
        }
        awaitBranchCheckedOut(branch);
    }

    /** Workspace -> Merge ...: merges {@value #BRANCH} into the current branch of every repository. */
    private void mergeBranchInEveryRepository() {
        window.menuItem("workspaceMergeItem").click();

        DialogFixture mergeDialog = WindowFinder.findDialog(WorkspaceMergeDialog.class)
                .withTimeout(20_000).using(robot);
        mergeDialog.comboBox(branchComboName(repoOne)).selectItem(BRANCH);
        mergeDialog.comboBox(branchComboName(repoTwo)).selectItem(BRANCH);
        mergeDialog.textBox("mergeMessageArea").setText("Merge " + BRANCH + " into " + MASTER);
        mergeDialog.button("mergeButton").click();

        DialogFixture mergeResult = WindowFinder.findDialog(MergeResultDialog.class)
                .withTimeout(30_000).using(robot);
        mergeResult.button("closeButton").click();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Switches dashboard tabs to force a reload: the tab change listener recomputes the
     * Stage All / Unstage All / Commit enabled states from every repository's status. Selecting
     * both tabs guarantees at least one real change event whichever tab is currently showing.
     */
    private void refreshDashboard() {
        window.tabbedPane("workspaceTabs").selectTab("Main");
        window.tabbedPane("workspaceTabs").selectTab("Working Copy");
    }

    private void awaitBranchCheckedOut(String branch) {
        Pause.pause(new Condition(branch + " to be checked out in both repositories") {
            @Override
            public boolean test() {
                return branch.equals(GitFixtures.currentBranchOrNull(repoOne))
                        && branch.equals(GitFixtures.currentBranchOrNull(repoTwo));
            }
        }, Timeout.timeout(20_000));
    }

    /** {@link ProjectBranchSelector} names its combo after the repository folder. */
    private static String branchComboName(Path repoDir) {
        return "branchCombo-" + repoDir.getFileName();
    }

    private static void assertCommitted(Path repoDir, String fileName) throws Exception {
        assertTrue(GitFixtures.filesInHead(repoDir).contains(fileName),
                fileName + " should be committed in " + repoDir.getFileName());
    }

    private static void assertBothFilesPresentOnMaster(Path repoDir) throws Exception {
        assertEquals(MASTER, GitFixtures.getBranchName(repoDir),
                repoDir.getFileName() + " should be back on " + MASTER);

        Set<String> committed = GitFixtures.filesInHead(repoDir);
        assertTrue(committed.contains(MASTER_FILE),
                MASTER_FILE + " missing from " + MASTER + " of " + repoDir.getFileName()
                        + ", found " + committed);
        assertTrue(committed.contains(BRANCH_FILE),
                BRANCH_FILE + " was not merged into " + MASTER + " of " + repoDir.getFileName()
                        + ", found " + committed);

        assertTrue(Files.exists(repoDir.resolve(MASTER_FILE)),
                MASTER_FILE + " missing from the working copy of " + repoDir.getFileName());
        assertTrue(Files.exists(repoDir.resolve(BRANCH_FILE)),
                BRANCH_FILE + " missing from the working copy of " + repoDir.getFileName());
    }
}
