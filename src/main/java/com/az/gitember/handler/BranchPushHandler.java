package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.transport.RefSpec;

import java.awt.*;
import java.util.Optional;

public class BranchPushHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch branch;
    private String remoteUrl;

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
        // If no remote tracking, set it up first
        if (branch.getRemoteMergeName() == null && branch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            Context.getGitRepoService().trackRemote(branch.getShortName(), branch.getShortName());
        }

        Optional<Project> project = Context.getCurrentProject();
        RemoteRepoParameters params = new RemoteRepoParameters();
        project.ifPresent(p -> {
            params.setUserName(p.getUserName());
            params.setUserPwd(p.getUserPwd());
            params.setAccessToken(p.getAccessToken());
            params.setKeyPassPhrase(p.getKeyPass());
        });

        remoteUrl = Context.getGitRepoService().getRepository()
                .getConfig().getString("remote", "origin", "url");
        params.setUrl(remoteUrl != null ? remoteUrl : "");

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
}
