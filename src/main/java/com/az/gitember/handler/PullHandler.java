package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.Optional;

public class PullHandler extends AbstractAsyncHandler<String> {

    public PullHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Pull";
    }

    @Override
    protected String doInBackground() throws Exception {
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
        String result = Context.getGitRepoService().remoteRepositoryPull(params, remoteBranch, null);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Pull completed: " + result);
    }
}
