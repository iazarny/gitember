package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.Optional;

public class PushHandler extends AbstractAsyncHandler<String> {

    private String remoteUrl;

    public PushHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Push";
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
        remoteUrl = Context.getGitRepoService().getRepository()
                .getConfig().getString("remote", "origin", "url");
        params.setUrl(remoteUrl != null ? remoteUrl : "");

        return Context.getGitRepoService().remoteRepositoryPush(params, null, null);
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Push completed");
        new PushResultDialog(parent, remoteUrl, result).setVisible(true);
    }
}
