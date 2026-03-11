package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.Component;

/**
 * Runs {@code git submodule init} + {@code git submodule update} asynchronously,
 * then refreshes the submodule list in Context.
 */
public class UpdateSubmodulesHandler extends AbstractAsyncHandler<Void> {

    public UpdateSubmodulesHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() { return "Update Submodules"; }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().updateSubmodules(progressMonitor);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
    }
}
