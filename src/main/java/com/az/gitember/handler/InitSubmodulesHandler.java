package com.az.gitember.handler;

import com.az.gitember.service.Context;

import java.awt.*;

/**
 * Runs {@code git submodule init} for all submodules or a single path.
 */
public class InitSubmodulesHandler extends AbstractAsyncHandler<Void> {

    private final String path;

    public InitSubmodulesHandler(Component parent) {
        this(parent, null);
    }

    public InitSubmodulesHandler(Component parent, String path) {
        super(parent);
        this.path = path;
    }

    @Override
    protected String getOperationName() {
        return path == null ? "Initialize submodules" : "Initialize submodule " + path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (path == null) {
            Context.getGitRepoService().initSubmodules();
        } else {
            Context.getGitRepoService().initSubmodule(path);
        }
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
    }
}
