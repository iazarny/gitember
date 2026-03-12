package com.az.gitember.handler;

import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MergeDialog;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.MergeResult;

import javax.swing.*;
import java.awt.*;

public class MergeBranchHandler extends AbstractAsyncHandler<MergeResult> {

    private final String branchFullName;
    private final MergeDialogResult dialogResult;

    public MergeBranchHandler(Component parent, StatusBar statusBar,
                              String branchFullName, MergeDialogResult dialogResult) {
        super(parent, statusBar);
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
        JOptionPane.showMessageDialog(parent,
                "Merge result: " + status,
                "Merge", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the merge options dialog and executes if confirmed.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String branchFullName, String branchShortName) {
        String workingBranchName = Context.getWorkingBranch() != null
                ? Context.getWorkingBranch().getShortName() : "current";

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(parent);
        MergeDialog dialog = new MergeDialog(frame, branchShortName, workingBranchName);
        dialog.setVisible(true);

        MergeDialogResult result = dialog.getResult();
        if (result != null) {
            new MergeBranchHandler(parent, statusBar, branchFullName, result).execute();
        }
    }
}
