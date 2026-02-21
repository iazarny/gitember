package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.RebaseResult;

import javax.swing.*;
import java.awt.*;

public class RebaseBranchHandler extends AbstractAsyncHandler<RebaseResult> {

    private final String branchFullName;

    public RebaseBranchHandler(Component parent, StatusBar statusBar, String branchFullName) {
        super(parent, statusBar);
        this.branchFullName = branchFullName;
    }

    @Override
    protected String getOperationName() {
        return "Rebase";
    }

    @Override
    protected RebaseResult doInBackground() throws Exception {
        RebaseResult result = Context.getGitRepoService().rebaseBranch(branchFullName);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(RebaseResult result) {
        String status = result.getStatus().toString();
        statusBar.setStatus("Rebase: " + status);
        JOptionPane.showMessageDialog(parent,
                "Rebase result: " + status,
                "Rebase", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows confirmation and executes.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String branchFullName, String branchShortName) {
        String workingBranchName = Context.getWorkingBranch() != null
                ? Context.getWorkingBranch().getShortName() : "current";

        int result = JOptionPane.showConfirmDialog(parent,
                "Rebase " + branchShortName + " onto " + workingBranchName + "?",
                "Rebase Branch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            new RebaseBranchHandler(parent, statusBar, branchFullName).execute();
        }
    }
}
