package com.az.gitember.handler;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

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
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();

        Context.getGitRepoService().remoteRepositoryFetch(params, null, progressMonitor);
        Context.updateBranches();
        Context.updateTags();
        Context.updateWorkingBranch();
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("Fetch completed");
    }
}
