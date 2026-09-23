package com.az.gitember.handler;

import com.az.gitember.service.Context;

import java.awt.Component;

/**
 * Runs {@code git submodule init} + {@code git submodule update} asynchronously,
 * then refreshes the submodule list in Context.
 */
public class UpdateSubmodulesHandler extends AbstractAsyncHandler<Void> {

    private final boolean recursive;
    private final String path;

    public UpdateSubmodulesHandler(Component parent) {
        this(parent, false, null);
    }

    public UpdateSubmodulesHandler(Component parent, boolean recursive) {
        this(parent, recursive, null);
    }

    public UpdateSubmodulesHandler(Component parent, boolean recursive, String path) {
        super(parent);
        this.recursive = recursive;
        this.path = path;
    }

    @Override
    protected String getOperationName() {
        String name;
        if (path != null) {
            name = "Update submodule " + path;
        } else if (recursive) {
            name = "Update submodules recursively";
        } else {
            name = "Update Submodules";
        }
        return name;
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (path != null) {
            Context.getGitRepoService().updateSubmodule(path, progressMonitor);
        } else {
            Context.getGitRepoService().updateSubmodules(progressMonitor, recursive);
        }
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
    }
}
