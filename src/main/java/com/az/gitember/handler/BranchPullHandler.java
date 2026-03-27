package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.PullOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.PullResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.Optional;

public class BranchPullHandler extends AbstractAsyncHandler<PullOperationResult> {

    private final ScmBranch branch;

    public BranchPullHandler(Component parent, StatusBar statusBar, ScmBranch branch) {
        super(parent, statusBar);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return "Pull " + branch.getShortName();
    }

    @Override
    protected PullOperationResult doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        PullOperationResult result = Context.getGitRepoService().remoteRepositoryPull(
                params, branch.getRemoteMergeName(), progressMonitor);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(PullOperationResult result) {
        statusBar.setStatus("Pull completed: " + result.toStatusString());
        Context.refreshHistory();
        new PullResultDialog(parent, result).setVisible(true);
    }
}
