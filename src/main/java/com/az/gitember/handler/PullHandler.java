package com.az.gitember.handler;

import com.az.gitember.data.PullOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PullResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import java.awt.*;

public class PullHandler extends AbstractAsyncHandler<PullOperationResult> {

    public PullHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Pull";
    }

    @Override
    protected PullOperationResult doInBackground() throws Exception {
        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();

        String remoteBranch = null;
        if (Context.getWorkingBranch() != null) {
            remoteBranch = Context.getWorkingBranch().getRemoteMergeName();
        }
        PullOperationResult result = Context.getGitRepoService().remoteRepositoryPull(
                params, remoteBranch, progressMonitor);

        Context.updateAll();
        Context.updateWorkingBranch();
        return result;
    }

    @Override
    protected void onSuccess(PullOperationResult result) {
        statusBar.setStatus("Pull completed: " + result.toStatusString());
        if (Context.getActiveView() == Context.ActiveView.WORKING_COPY) {
            Context.refreshWorkingCopy();
        } else {
            Context.refreshHistory();
        }
        // Show the result dialog first (it's modal, so this blocks until closed)
        new PullResultDialog(parent, result).setVisible(true);
        // After the user closes the dialog, navigate to the appropriate view
        navigateAfterPull(result);
    }

    private void navigateAfterPull(PullOperationResult result) {
        if (result.isConflicting()) {
            // Conflicts need the user's attention — take them straight to working copy
            Context.navigateToWorkingCopy();
        } else if (!result.isAlreadyUpToDate() && result.getNewHeadSha() != null) {
            // Successful pull with new commits — show the pulled commit in history
            Context.navigateToHistory(result.getNewHeadSha());
        }
    }
}
