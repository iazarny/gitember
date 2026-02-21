package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class OpenRepoHandler extends AbstractAsyncHandler<Void> {

    public OpenRepoHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Open Repository";
    }

    @Override
    public void execute() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Git Repository");

        // Start from last known location
        if (Context.getRepositoryPath() != null) {
            chooser.setCurrentDirectory(new File(Context.getProjectFolder()));
        }

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            statusBar.setStatus("Opening " + path + "...");
            statusBar.showProgress(true);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Context.init(path);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        statusBar.clearProgress();
                        statusBar.setStatus("Repository opened");
                    } catch (Exception e) {
                        onError(e.getCause() instanceof Exception ? (Exception) e.getCause() : e);
                    }
                }
            };
            worker.execute();
        }
    }

    @Override
    protected Void doInBackground() {
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
    }
}
