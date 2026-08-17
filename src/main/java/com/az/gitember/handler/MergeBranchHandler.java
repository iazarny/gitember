package com.az.gitember.handler;

import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.MergeConflictOptionsDialog;
import com.az.gitember.dialog.MergeResultDialog;
import com.az.gitember.dialog.WorkspaceMergeDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.MergeDialog;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.MergeResult;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MergeBranchHandler extends AbstractAsyncHandler<MergeResult> {

    private static final Logger log = Logger.getLogger(MergeBranchHandler.class.getName());

    private final String branchFullName;
    private final MergeDialogResult dialogResult;

    /**
     * Per-project merge parameters; non-null only for a workspace-wide merge, in which case
     * {@link #branchFullName} / {@link #dialogResult} are unused.
     */
    private final Map<Project, MergeDialogResult> workspaceSelections;

    /** Aggregated per-project outcomes, populated only by a workspace-wide merge. */
    private List<ProjectOperationResult<MergeResult>> workspaceResults;

    public MergeBranchHandler(Component parent,
                              String branchFullName, MergeDialogResult dialogResult) {
        super(parent);
        this.branchFullName = branchFullName;
        this.dialogResult   = dialogResult;
        this.workspaceSelections = null;
    }

    public MergeBranchHandler(Component parent, Map<Project, MergeDialogResult> workspaceSelections) {
        super(parent);
        this.branchFullName = null;
        this.dialogResult   = null;
        this.workspaceSelections = workspaceSelections;
    }

    @Override
    protected String getOperationName() {
        return "Merge";
    }

    @Override
    protected MergeResult doInBackground() throws Exception {
        if (workspaceSelections != null) {
            workspaceResults = mergeWorkspace();
            return null;
        }
        MergeResult result = Context.getGitRepoService().mergeBranch(
                branchFullName,
                dialogResult.getCommitMessage(),
                dialogResult.isSquash(),
                dialogResult.getFastForwardMode());
        Context.updateAll();
        return result;
    }

    /**
     * Merges the branch the user picked for each project into that project's current branch, one
     * repository at a time (synchronously) so a failure or conflict in one repository can't
     * interleave with or abort the rest. Conflicts are captured as results, not thrown.
     */
    private List<ProjectOperationResult<MergeResult>> mergeWorkspace() {
        List<ProjectOperationResult<MergeResult>> results = new ArrayList<>();
        for (Map.Entry<Project, MergeDialogResult> entry : workspaceSelections.entrySet()) {
            Project project = entry.getKey();
            MergeDialogResult params = entry.getValue();
            try {
                MergeResult res = project.getGitRepoService().mergeBranch(
                        params.getBranchName(),
                        params.getCommitMessage(),
                        params.isSquash(),
                        params.getFastForwardMode());
                results.add(ProjectOperationResult.ok(project, null, res));
            } catch (Exception ex) {
                results.add(ProjectOperationResult.failed(project, ex));
            }
        }
        return results;
    }

    @Override
    protected void onSuccess(MergeResult result) {
        if (workspaceResults != null) {
            long ok = workspaceResults.stream()
                    .filter(r -> r.isSuccess() && r.getResult() != null
                            && r.getResult().getMergeStatus().isSuccessful())
                    .count();
            statusBar.setStatus("Merge completed for " + ok + " of "
                    + workspaceResults.size() + " repositories");
            new MergeResultDialog(parent, workspaceResults).setVisible(true);
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceProjectBranches(workspaceResults);
                mf.refreshWorkspaceView();
            }
            return;
        }

        String status = result.getMergeStatus().toString();
        statusBar.setStatus("Merge: " + status);
        if (result.getMergeStatus() == MergeResult.MergeStatus.CONFLICTING) {
            showConflictOptions(result);
        } else {
            JOptionPane.showMessageDialog(parent,
                    "Merge result: " + status,
                    "Merge", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showConflictOptions(MergeResult result) {
        List<String> conflictedFiles = result.getConflicts() != null
                ? new ArrayList<>(result.getConflicts().keySet())
                : Collections.emptyList();
        MergeConflictOptionsDialog dialog = new MergeConflictOptionsDialog(parent, conflictedFiles);
        dialog.setVisible(true);

        switch (dialog.getSelectedOption()) {
            case MANUAL -> {
                statusBar.setStatus("Merge conflicts need manual resolution");
                Context.navigateToWorkingCopy();
            }
            case USE_OURS -> resolveAllConflicts(conflictedFiles, CheckoutCommand.Stage.OURS);
            case USE_THEIRS -> resolveAllConflicts(conflictedFiles, CheckoutCommand.Stage.THEIRS);
            case ABORT -> abortMerge();
        }
    }

    private void resolveAllConflicts(List<String> conflictedFiles, CheckoutCommand.Stage stage) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (String file : conflictedFiles) {
                    Context.getGitRepoService().checkoutFile(file, stage);
                    Context.getGitRepoService().addFileToCommitStage(file);
                }
                Context.updateAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    String side = stage == CheckoutCommand.Stage.OURS ? "ours" : "theirs";
                    statusBar.setStatus("Resolved " + conflictedFiles.size()
                            + " conflict(s) using " + side);
                    Context.navigateToWorkingCopy();
                } catch (Exception e) {
                    statusBar.setStatus("Resolve conflicts failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Resolve conflicts failed:\n" + e.getMessage(),
                            "Merge", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void abortMerge() {
        int choice = JOptionPane.showConfirmDialog(parent,
                "Abort the merge and discard all merge changes?",
                "Abort Merge", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            Context.navigateToWorkingCopy();
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().abortMerge(progressMonitor);
                Context.updateAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusBar.setStatus("Merge aborted");
                    Context.navigateToWorkingCopy();
                } catch (Exception e) {
                    statusBar.setStatus("Abort merge failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Abort merge failed:\n" + e.getMessage(),
                            "Merge", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Shows the merge options dialog and executes if confirmed.
     */
    public static void showAndExecute(Frame parent, ScmBranch sourceScmBranch ) {
        if (MainFrame.getInstance().isWorkspaceActive()) {
            showAndExecuteWorkspace(parent, sourceScmBranch);
        } else {
            showAndExecuteSingleRepo(parent, sourceScmBranch);
        }
    }

    /**
     * Workspace-wide merge: the user picks a branch per repository, then each selected repository
     * merges its own branch into its own current branch. Repositories left on "skip" are untouched.
     * <p>
     * Each repository's branches are read on a worker thread first — {@code Project}'s cached
     * branch list is only maintained for the active project — and the dialog is opened once they
     * are all available.
     */
    public static void showAndExecuteWorkspace(Frame parent, ScmBranch sourceScmBranch) {
        new SwingWorker<Map<Project, List<ScmBranch>>, Void>() {
            @Override
            protected Map<Project, List<ScmBranch>> doInBackground() {
                Map<Project, List<ScmBranch>> branchesByProject = new LinkedHashMap<>();
                for (Project project : Context.getWorkspace().getProjects()) {
                    try {
                        branchesByProject.put(project, project.getGitRepoService().getBranches());
                    } catch (Exception ex) {
                        log.log(Level.FINE, "Cannot list branches of " + project, ex);
                        branchesByProject.put(project, Collections.emptyList());
                    }
                }
                return branchesByProject;
            }

            @Override
            protected void done() {
                Map<Project, List<ScmBranch>> branchesByProject;
                try {
                    branchesByProject = get();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Cannot prepare workspace merge", ex);
                    JOptionPane.showMessageDialog(parent,
                            "Cannot read workspace branches:\n" + ex.getMessage(),
                            "Merge", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                WorkspaceMergeDialog dialog =
                        new WorkspaceMergeDialog(parent, sourceScmBranch, branchesByProject);
                dialog.setVisible(true);

                Map<Project, MergeDialogResult> selections = dialog.getResult();
                if (selections != null) {
                    new MergeBranchHandler(parent, selections).execute();
                }
            }
        }.execute();
    }

    public static void showAndExecuteSingleRepo(Frame parent, ScmBranch sourceScmBranch ) {
        MergeDialog dialog = new MergeDialog(parent, sourceScmBranch);
        dialog.setVisible(true);

        MergeDialogResult result = dialog.getResult();

        if (result != null) {
            new MergeBranchHandler(parent,  result.getBranchName(), result).execute();
        }
    }
}
