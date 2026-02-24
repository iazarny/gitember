package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.PullOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PullResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.Optional;

public class PullHandler extends AbstractAsyncHandler<PullOperationResult> {

    public PullHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Pull";
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
        String remoteUrl = Context.getGitRepoService().getRepository()
                .getConfig().getString("remote", "origin", "url");
        params.setUrl(remoteUrl != null ? remoteUrl : "");

        String remoteBranch = null;
        if (Context.getWorkingBranch() != null) {
            remoteBranch = Context.getWorkingBranch().getRemoteMergeName();
        }
        PullOperationResult result = Context.getGitRepoService().remoteRepositoryPull(params, remoteBranch, null);
        Context.updateAll();
        Context.updateWorkingBranch();
        return result;
    }

    @Override
    protected void onSuccess(PullOperationResult result) {
        statusBar.setStatus("Pull completed: " + result.toStatusString());
        new PullResultDialog(parent, result).setVisible(true);
    }
}
