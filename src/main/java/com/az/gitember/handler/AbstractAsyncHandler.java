package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.dialog.CredentialsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.lib.ProgressMonitor;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractAsyncHandler<T> {

    private static final Logger log = Logger.getLogger(AbstractAsyncHandler.class.getName());

    protected final Component parent;
    protected final StatusBar statusBar;
    protected final ProgressMonitor progressMonitor;

    protected AbstractAsyncHandler(Component parent, StatusBar statusBar) {
        this.parent = parent;
        this.statusBar = statusBar;
        this.progressMonitor = new StatusBarProgressMonitor(statusBar);
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

    // ── Auth helpers (shared by all handlers that talk to a remote) ───────────

    /** Returns true if {@code t} or any cause in the chain looks like an auth failure. */
    protected static boolean isAuthError(Throwable t) {
        if (t == null) return false;
        try {
            if (t instanceof org.eclipse.jgit.lfs.errors.LfsUnauthorized) return true;
        } catch (NoClassDefFoundError ignored) {}
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        if (msg.contains("401") || msg.contains("403")
                || msg.contains("unauthorized") || msg.contains("forbidden")
                || msg.contains("auth fail") || msg.contains("authentication")) {
            return true;
        }
        return isAuthError(t.getCause());
    }

    /**
     * Opens {@link CredentialsDialog}, saves accepted credentials to the current project,
     * and returns {@code true} so the caller can retry the operation.
     */
    protected boolean promptAndSaveCredentials() {
        Project project = Context.getCurrentProject().orElse(null);
        if (project == null) {
            JOptionPane.showMessageDialog(parent,
                    "Authentication failed and no project is open.\n"
                            + "Please configure credentials via Repository → Credentials…",
                    "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int choice = JOptionPane.showConfirmDialog(parent,
                "Authentication is required to complete this operation.\n"
                        + "Would you like to enter credentials now?",
                "Authentication Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return false;

        Frame owner = parent instanceof Frame f ? f
                : (Frame) SwingUtilities.getWindowAncestor(parent);

        CredentialsDialog dlg = new CredentialsDialog(owner, project);
        dlg.setAccessToken(project.getAccessToken());
        dlg.setUserName(project.getUserName());
        dlg.setPassword(project.getUserPwd());
        dlg.setVisible(true);

        if (!dlg.isConfirmed()) return false;

        project.setAccessToken(dlg.getAccessToken());
        project.setUserName(dlg.getUserName());
        project.setUserPwd(dlg.getPassword());
        Context.saveSettings();
        return true;
    }
}
