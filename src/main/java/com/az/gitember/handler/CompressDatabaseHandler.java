package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

/**
 * Runs {@code git gc} (garbage collection / pack compression) on the current repository.
 */
public class CompressDatabaseHandler extends AbstractAsyncHandler<Void> {

    public CompressDatabaseHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Compress database";
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().compressDatabase(progressMonitor);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("Database compressed");
    }
}
