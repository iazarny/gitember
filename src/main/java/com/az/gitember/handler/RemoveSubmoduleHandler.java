package com.az.gitember.handler;

import com.az.gitember.data.Submodule;
import com.az.gitember.service.Context;

import javax.swing.*;
import java.awt.*;

/**
 * De-registers and removes a submodule after confirmation.
 */
public class RemoveSubmoduleHandler extends AbstractAsyncHandler<Void> {

    private final String path;
    private final boolean force;

    public RemoveSubmoduleHandler(Component parent, String path, boolean force) {
        super(parent);
        this.path = path;
        this.force = force;
    }

    @Override
    protected String getOperationName() {
        return "Remove submodule " + path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().removeSubmodule(path, force);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
        Context.refreshWorkingCopy();
    }

    public static void showAndExecute(Component parent, Submodule submodule) {
        if (submodule != null) {
            confirmAndRemove(parent, submodule);
        }
    }

    private static void confirmAndRemove(Component parent, Submodule submodule) {
        String message = "<html>Remove submodule <b>" + submodule.getPath() + "</b>?<br><br>"
                + "This de-registers the module, removes it from the index,<br>"
                + "and deletes its working tree.</html>";
        String[] options = {"Remove", "Remove (force)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, message, "Remove Submodule",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        if (choice == 0 || choice == 1) {
            new RemoveSubmoduleHandler(parent, submodule.getPath(), choice == 1).execute();
        }
    }
}
