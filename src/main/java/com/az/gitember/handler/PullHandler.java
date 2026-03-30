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
        new PullResultDialog(parent, result).setVisible(true);
    }
}
