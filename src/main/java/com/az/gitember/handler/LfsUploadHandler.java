package com.az.gitember.handler;

import com.az.gitember.data.LfsException;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Uploads local LFS objects for the current branch via the Git LFS Batch API.
 */
public class LfsUploadHandler extends AbstractAsyncHandler<Void> {

    private boolean credentialsPrompted;

    public LfsUploadHandler(Component parent) {
        super(parent);
    }

    @Override
    protected String getOperationName() {
        return "Upload LFS objects";
    }

    @Override
    protected Void doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        Context.getGitRepoService().uploadLfsObjects(params);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus("LFS objects uploaded");
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
                execute();
            } else {
                super.onError(e);
            }
        } else {
            super.onError(e);
        }
    }
}
