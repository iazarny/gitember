package com.az.gitember.handler;

import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractAsyncHandler<T> {

    private static final Logger log = Logger.getLogger(AbstractAsyncHandler.class.getName());

    protected final Component parent;
    protected final StatusBar statusBar;

    protected AbstractAsyncHandler(Component parent, StatusBar statusBar) {
        this.parent = parent;
        this.statusBar = statusBar;
    }

    protected abstract String getOperationName();

    protected abstract T doInBackground() throws Exception;

    protected abstract void onSuccess(T result);

    protected void onError(Exception e) {
        log.log(Level.SEVERE, getOperationName() + " failed", e);
        statusBar.setStatus(getOperationName() + " failed: " + e.getMessage());
        statusBar.clearProgress();
        JOptionPane.showMessageDialog(parent,
                getOperationName() + " failed:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void execute() {
        statusBar.setStatus(getOperationName() + "...");
        statusBar.showProgress(true);

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return AbstractAsyncHandler.this.doInBackground();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    statusBar.clearProgress();
                    statusBar.setStatus(getOperationName() + " completed");
                    onSuccess(result);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    onError(cause instanceof Exception ? (Exception) cause : new Exception(cause));
                }
            }
        };
        worker.execute();
    }
}
