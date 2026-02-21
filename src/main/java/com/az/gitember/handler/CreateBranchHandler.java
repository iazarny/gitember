package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

public class CreateBranchHandler extends AbstractAsyncHandler<String> {

    private final String baseBranchFullName;
    private final String newBranchName;

    public CreateBranchHandler(Component parent, StatusBar statusBar,
                               String baseBranchFullName, String newBranchName) {
        super(parent, statusBar);
        this.baseBranchFullName = baseBranchFullName;
        this.newBranchName = newBranchName;
    }

    @Override
    protected String getOperationName() {
        return "Create branch " + newBranchName;
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().createBranch(baseBranchFullName, newBranchName);
        Context.getGitRepoService().checkoutBranch(newBranchName, null);
        Context.updateBranches();
        Context.updateWorkingBranch();
        return newBranchName;
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Created and checked out branch " + result);
    }

    /**
     * Prompts user for branch name and executes if confirmed.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar, String baseBranchFullName) {
        String name = JOptionPane.showInputDialog(parent,
                "New branch name:", "Create Branch", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isBlank()) {
            new CreateBranchHandler(parent, statusBar, baseBranchFullName, name.trim()).execute();
        }
    }
}
