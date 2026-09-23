package com.az.gitember.handler;

import com.az.gitember.dialog.AddSubmoduleDialog;
import com.az.gitember.service.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Runs {@code git submodule add} asynchronously and refreshes the submodule list.
 */
public class AddSubmoduleHandler extends AbstractAsyncHandler<Void> {

    private final String url;
    private final String path;

    public AddSubmoduleHandler(Component parent, String url, String path) {
        super(parent);
        this.url = url;
        this.path = path;
    }

    @Override
    protected String getOperationName() {
        return "Add submodule " + path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().addSubmodule(url, path, progressMonitor);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
        Context.refreshWorkingCopy();
    }

    public static void showAndExecute(Component parent) {
        Window ancestor = SwingUtilities.getWindowAncestor(parent);
        Frame frame = ancestor instanceof Frame f ? f : null;
        AddSubmoduleDialog dialog = new AddSubmoduleDialog(frame);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new AddSubmoduleHandler(parent, dialog.getUrl(), dialog.getPath()).execute();
        }
    }
}
