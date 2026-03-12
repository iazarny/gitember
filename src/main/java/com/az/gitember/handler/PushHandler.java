package com.az.gitember.handler;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

public class PushHandler extends AbstractAsyncHandler<String> {

    private String remoteUrl;
    private boolean credentialsPrompted = false;

    public PushHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Push";
    }

    @Override
    protected String doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        remoteUrl = params.getUrl();

        String result = Context.getGitRepoService().remoteRepositoryPush(params, null, progressMonitor);
        Context.updateBranches();
        Context.updateWorkingBranch();
        return result;
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Push completed");
        new PushResultDialog(parent, remoteUrl, result).setVisible(true);
    }

    @Override
    protected void onError(Exception e) {
        if (!credentialsPrompted && isAuthError(e)) {
            credentialsPrompted = true;
            if (promptAndSaveCredentials()) {
                execute();
                return;
            }
        }
        super.onError(e);
    }
}
