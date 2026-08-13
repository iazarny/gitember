package com.az.gitember.handler;

import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.MergeConflictOptionsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.MergeDialog;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.MergeResult;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeBranchHandler extends AbstractAsyncHandler<MergeResult> {

    private final String branchFullName;
    private final MergeDialogResult dialogResult;

    public MergeBranchHandler(Component parent,
                              String branchFullName, MergeDialogResult dialogResult) {
        super(parent);
        this.branchFullName = branchFullName;
        this.dialogResult   = dialogResult;
    }

    @Override
    protected String getOperationName() {
        return "Merge";
    }

    @Override
    protected MergeResult doInBackground() throws Exception {
        MergeResult result = Context.getGitRepoService().mergeBranch(
                branchFullName,
                dialogResult.getCommitMessage(),
                dialogResult.isSquash(),
                dialogResult.getFastForwardMode());
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(MergeResult result) {
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
        if (Context.isWorkspaceMode()) {
            if (MainFrame.getInstance().isWorkspaceActive()) {
                //showAndExecuteWorkspace(parent, sourceScmBranch);
            }
        } else {
            showAndExecuteSingleRepo(parent, sourceScmBranch);
        }

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
