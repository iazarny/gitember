package com.az.gitember.handler;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

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
}
