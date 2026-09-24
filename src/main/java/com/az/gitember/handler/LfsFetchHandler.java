package com.az.gitember.handler;

import com.az.gitember.data.LfsException;
import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.CredentialsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

/**
 * Fetches (downloads) LFS objects for the current HEAD from the remote LFS server.
 * When constructed with a path, only that working-copy file is fetched.
 *
 * <p>If the first attempt fails with an authentication error the handler prompts
 * for credentials (access token or username/password), saves them to the project,
 * and retries automatically.</p>
 */
public class LfsFetchHandler extends AbstractAsyncHandler<Void> {

    /** Prevents an infinite prompt loop if the user supplies wrong credentials. */
    private boolean credentialsPrompted = false;

    /** Repo-relative path to fetch, or {@code null} for every LFS pointer on HEAD. */
    private final String path;

    public LfsFetchHandler(Component parent) {
        this(parent, null);
    }

    public LfsFetchHandler(Component parent, String path) {
        super(parent);
        this.path = path;
    }

    @Override
    protected String getOperationName() {
        String name = "Fetch LFS objects";
        if (path != null && !path.isBlank()) {
            name = "Fetch LFS file " + path;
        }
        return name;
    }

    @Override
    protected Void doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        Context.getGitRepoService().fetchLfsObjects(params, path);
        Context.updateStatus(null, true);
        Context.updateWorkingBranch();
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        if (path != null && !path.isBlank()) {
            statusBar.setStatus("LFS file fetched: " + path);
        } else {
            statusBar.setStatus("LFS objects fetched");
        }
        Context.refreshWorkingCopy();
    }

    @Override
    protected void onError(Exception e) {
        if (e instanceof LfsException lfs && lfs.getKind() == LfsException.Kind.NO_REMOTE) {
            statusBar.clearProgress();
            statusBar.setStatus(getOperationName() + " failed: no HTTP remote");
            JOptionPane.showMessageDialog(parent, lfs.getMessage(),
                    "Git LFS", JOptionPane.WARNING_MESSAGE);
        } else if (!credentialsPrompted && isAuthError(e)) {
            credentialsPrompted = true;
            if (promptAndSaveCredentials()) {
                execute(); // retry with the new credentials
            } else {
                super.onError(e);
            }
        } else {
            super.onError(e);
        }
    }

    /** LFS-specific credential prompt explaining why HTTPS credentials are needed. */
    @Override
    protected boolean promptAndSaveCredentials() {
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
