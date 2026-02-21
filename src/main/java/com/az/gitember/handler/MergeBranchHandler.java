package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;

import javax.swing.*;
import java.awt.*;

public class MergeBranchHandler extends AbstractAsyncHandler<MergeResult> {

    private final String branchFullName;
    private final String commitMessage;

    public MergeBranchHandler(Component parent, StatusBar statusBar,
                              String branchFullName, String commitMessage) {
        super(parent, statusBar);
        this.branchFullName = branchFullName;
        this.commitMessage = commitMessage;
    }

    @Override
    protected String getOperationName() {
        return "Merge";
    }

    @Override
    protected MergeResult doInBackground() throws Exception {
        MergeResult result = Context.getGitRepoService().mergeBranch(
                branchFullName, commitMessage, false, MergeCommand.FastForwardMode.FF);
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
     * Shows merge dialog and executes if confirmed.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String branchFullName, String branchShortName) {
        String workingBranchName = Context.getWorkingBranch() != null
                ? Context.getWorkingBranch().getShortName() : "current";

        String message = JOptionPane.showInputDialog(parent,
                "Merge " + branchShortName + " into " + workingBranchName + "\nCommit message:",
                "Merge Branch", JOptionPane.PLAIN_MESSAGE);
        if (message != null) {
            if (message.isBlank()) {
                message = "Merge " + branchShortName + " into " + workingBranchName;
            }
            new MergeBranchHandler(parent, statusBar, branchFullName, message).execute();
        }
    }
}
