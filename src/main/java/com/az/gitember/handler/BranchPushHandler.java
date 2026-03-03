package com.az.gitember.handler;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.transport.RefSpec;

import java.awt.*;

public class BranchPushHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch branch;
    private String remoteUrl;
    private boolean credentialsPrompted = false;

    public BranchPushHandler(Component parent, StatusBar statusBar, ScmBranch branch) {
        super(parent, statusBar);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return "Push " + branch.getShortName();
    }

    @Override
    protected String doInBackground() throws Exception {
        if (branch.getRemoteMergeName() == null && branch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            Context.getGitRepoService().trackRemote(branch.getShortName(), branch.getShortName());
        }

        RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
        remoteUrl = params.getUrl();

        RefSpec refSpec = new RefSpec(branch.getFullName() + ":" + branch.getFullName());
        String result = Context.getGitRepoService().remoteRepositoryPush(params, refSpec, null);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Push completed");
        new PushResultDialog(parent, remoteUrl, result).setVisible(true);
    }

    @Override
    protected void onError(Exception e) {
        if (!credentialsPrompted && isAuthError(e)) {
            credentialsPrompted = true;
            if (promptAndSaveCredentials()) {
                execute();
                return;
            }
        }
        super.onError(e);
    }
}
