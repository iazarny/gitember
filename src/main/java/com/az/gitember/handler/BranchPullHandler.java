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
        Optional<Project> project = Context.getCurrentProject();
        RemoteRepoParameters params = new RemoteRepoParameters();
        project.ifPresent(p -> {
            params.setUserName(p.getUserName());
            params.setUserPwd(p.getUserPwd());
            params.setAccessToken(p.getAccessToken());
            params.setKeyPassPhrase(p.getKeyPass());
        });

        PullOperationResult result = Context.getGitRepoService().remoteRepositoryPull(
                params, branch.getRemoteMergeName(), progressMonitor);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(PullOperationResult result) {
        statusBar.setStatus("Pull completed: " + result.toStatusString());
        new PullResultDialog(parent, result).setVisible(true);
    }
}
