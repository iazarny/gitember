package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.dialog.CredentialsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

/**
 * Fetches (downloads) LFS objects for the current HEAD from the remote LFS server.
 *
 * <p>If the first attempt fails with an authentication error the handler prompts
 * for credentials (access token or username/password), saves them to the project,
 * and retries automatically.</p>
 */
public class LfsFetchHandler extends AbstractAsyncHandler<Void> {

    /** Prevents an infinite prompt loop if the user supplies wrong credentials. */
    private boolean credentialsPrompted = false;

    public LfsFetchHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Fetch LFS objects";
    }

    @Override
    protected Void doInBackground() throws Exception {
        Context.getGitRepoService().fetchLfsObjects();
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("LFS objects fetched");
    }

    @Override
    protected void onError(Exception e) {
        if (!credentialsPrompted && isAuthError(e)) {
            credentialsPrompted = true;
            if (promptAndSaveCredentials()) {
                execute(); // retry with the new credentials
                return;
            }
        }
        super.onError(e);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns true if {@code t} or any cause in the chain looks like an auth failure. */
    private static boolean isAuthError(Throwable t) {
        if (t == null) return false;
        if (t instanceof org.eclipse.jgit.lfs.errors.LfsUnauthorized) return true;
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        if (msg.contains("401") || msg.contains("unauthorized")
                || msg.contains("auth fail") || msg.contains("authentication")) {
            return true;
        }
        return isAuthError(t.getCause());
    }

    /**
     * Shows a confirmation prompt explaining why HTTPS credentials are needed,
     * then opens {@link CredentialsDialog}.  Saves accepted credentials to the
     * current project and returns {@code true} so the caller can retry.
     */
    private boolean promptAndSaveCredentials() {
        Project project = Context.getCurrentProject().orElse(null);
        if (project == null) {
            JOptionPane.showMessageDialog(parent,
                    "LFS authentication failed and no project is open.\n"
                    + "Please configure credentials via Repository → Credentials…",
                    "LFS Authentication", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int choice = JOptionPane.showConfirmDialog(parent,
                "<html><b>LFS authentication required</b><br><br>"
                + "Git LFS always transfers files over <b>HTTPS</b>, even when git itself<br>"
                + "uses SSH. An access token (or username + password) is therefore<br>"
                + "needed to download LFS objects.<br><br>"
                + "Would you like to enter credentials now?</html>",
                "LFS Authentication Required",
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
