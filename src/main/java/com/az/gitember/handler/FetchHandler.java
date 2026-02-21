package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.Optional;

public class FetchHandler extends AbstractAsyncHandler<Void> {

    public FetchHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Fetch";
    }

    @Override
    protected Void doInBackground() throws Exception {
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

        Context.getGitRepoService().remoteRepositoryFetch(params, null, null);
        Context.updateBranches();
        Context.updateTags();
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("Fetch completed");
    }
}
