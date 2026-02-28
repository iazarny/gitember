package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

/**
 * Fetches (downloads) LFS objects for the current HEAD from the remote LFS server.
 */
public class LfsFetchHandler extends AbstractAsyncHandler<Void> {

    public LfsFetchHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Fetch LFS objects";
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().fetchLfsObjects();
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("LFS objects fetched");
    }
}
