package com.az.gitember.handler;

import com.az.gitember.service.I18n;
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
        return I18n.t("removeSubmodule", path);
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
        String message = I18n.t("removeSubmoduleDeRegistersModule", submodule.getPath());
        String[] options = {I18n.t("remove"), I18n.t("removeForce"), I18n.t("cancel")};
        int choice = JOptionPane.showOptionDialog(parent, message, I18n.t("removeSubmoduleHandler.removeSubmodule"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        if (choice == 0 || choice == 1) {
            new RemoveSubmoduleHandler(parent, submodule.getPath(), choice == 1).execute();
        }
    }
}
