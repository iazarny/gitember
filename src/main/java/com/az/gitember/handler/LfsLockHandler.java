package com.az.gitember.handler;

import com.az.gitember.data.LfsException;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Locks or unlocks a path through the Git LFS locking API.
 */
public class LfsLockHandler extends AbstractAsyncHandler<Void> {

    private final String path;
    private final boolean lock;
    private final boolean force;

    public LfsLockHandler(Component parent, String path, boolean lock, boolean force) {
        super(parent);
        this.path = path;
        this.lock = lock;
        this.force = force;
    }

    @Override
    protected String getOperationName() {
        return (lock ? "Lock " : "Unlock ") + path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        if (lock) {
            Context.getGitRepoService().lockLfsFile(path, params);
        } else {
            Context.getGitRepoService().unlockLfsFile(path, force, params);
        }
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        statusBar.setStatus(getOperationName() + " completed");
        Context.refreshWorkingCopy();
    }

    @Override
    protected void onError(Exception e) {
        if (e instanceof LfsException lfs
                && (lfs.getKind() == LfsException.Kind.NO_REMOTE
                || lfs.getKind() == LfsException.Kind.LOCKED
                || lfs.getKind() == LfsException.Kind.NOT_LOCKED)) {
            statusBar.clearProgress();
            statusBar.setStatus(getOperationName() + " failed");
            JOptionPane.showMessageDialog(parent, lfs.getMessage(),
                    "Git LFS", JOptionPane.WARNING_MESSAGE);
        } else {
            super.onError(e);
        }
    }
}
