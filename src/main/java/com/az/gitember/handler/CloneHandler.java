package com.az.gitember.handler;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

public class CloneHandler extends AbstractAsyncHandler<Void> {

    private final RemoteRepoParameters params;

    public CloneHandler(Component parent, StatusBar statusBar, RemoteRepoParameters params) {
        super(parent, statusBar);
        this.params = params;
    }

    @Override
    protected String getOperationName() {
        return "Clone";
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().cloneRepository(params, null);
        Context.init(params.getDestinationFolder());
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("Clone completed successfully");
    }
}
