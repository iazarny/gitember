package com.az.gitember.handler;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.errors.CheckoutConflictException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CheckoutBranchHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch branch;

    public CheckoutBranchHandler(Component parent, StatusBar statusBar, ScmBranch branch) {
        super(parent, statusBar);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return "Checkout " + branch.getShortName();
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().checkoutBranch(branch.getFullName(), null);
        Context.updateBranches();
        Context.updateWorkingBranch();
        Context.updateStatus(null);
        return branch.getShortName();
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Checked out " + result);
    }

    @Override
    protected void onError(Exception e) {
        CheckoutConflictException conflict = findConflictException(e);
        if (conflict == null) {
            super.onError(e);
            return;
        }

        List<String> files = conflict.getConflictingPaths();
        statusBar.clearProgress();
        statusBar.setStatus("Checkout failed: local changes would be overwritten");

        StringBuilder sb = new StringBuilder();
        sb.append("Cannot checkout '").append(branch.getShortName()).append("'.\n\n");
        sb.append("The following file").append(files.size() == 1 ? " has" : "s have")
          .append(" local changes that would be overwritten:\n\n");
        for (String f : files) {
            sb.append("  \u2022 ").append(f).append("\n");
        }
        sb.append("\nCommit or stash your changes before switching branches.");

        JOptionPane.showMessageDialog(parent, sb.toString(),
                "Cannot Checkout Branch", JOptionPane.WARNING_MESSAGE);
    }

    /** Walks the cause chain to find a {@link CheckoutConflictException}. */
    private static CheckoutConflictException findConflictException(Throwable t) {
        while (t != null) {
            if (t instanceof CheckoutConflictException cce) {
                return cce;
            }
            t = t.getCause();
        }
        return null;
    }
}
