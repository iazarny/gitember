package com.az.gitember.handler;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

public class PushTagHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch tag;
    private String remoteUrl;
    private boolean credentialsPrompted = false;

    public PushTagHandler(Component parent, StatusBar statusBar, ScmBranch tag) {
        super(parent, statusBar);
        this.tag = tag;
    }

    @Override
    protected String getOperationName() {
        return "Push tag " + tag.getShortName();
    }

    @Override
    protected String doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        remoteUrl = params.getUrl();
        return Context.getGitRepoService().pushTag(tag.getFullName(), progressMonitor);
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Tag pushed: " + tag.getShortName());
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
