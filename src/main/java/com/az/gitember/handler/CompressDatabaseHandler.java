package com.az.gitember.handler;

import com.az.gitember.service.I18n;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

/**
 * Runs {@code git gc} (garbage collection / pack compression) on the current repository.
 */
public class CompressDatabaseHandler extends AbstractAsyncHandler<Void> {

    public CompressDatabaseHandler(Component parent) {
        super(parent);
    }

    @Override
    public void execute() {
        int choice = JOptionPane.showConfirmDialog(
                parent,
                I18n.t("databaseCompressionGitGcCan"),
                I18n.t("compressDatabase"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            super.execute();
        }
    }

    @Override
    protected String getOperationName() {
        return I18n.t("compressDatabaseHandler.compressDatabase");
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().compressDatabase(progressMonitor);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus(I18n.t("databaseCompressed"));
        JOptionPane.showMessageDialog(
                parent,
                I18n.t("databaseCompressedSuccessfully"),
                I18n.t("compressDatabase"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
