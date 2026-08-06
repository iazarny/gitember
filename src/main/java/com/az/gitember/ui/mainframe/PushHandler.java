package com.az.gitember.ui.mainframe;

import com.az.gitember.data.*;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.handler.AbstractAsyncHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.transport.RefSpec;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PushHandler extends AbstractAsyncHandler<String> {

    private String remoteUrl;
    private boolean credentialsPrompted = false;
    private ScmBranch branch;

    /** Non-null only when the push ran across a workspace (aggregated per-project results). */
    private List<ProjectOperationResult<String>> workspaceResults;

    public PushHandler(Component parent, ScmBranch branch) {
        super(parent);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return "Push";
    }

    @Override
    protected String doInBackground() throws Exception {
        if (MainFrame.getInstance().isWorkspaceActive()) {
            workspaceResults = pushWorkspace();
            return null;
        } else {
            GitRepoService svc = Context.getGitRepoService();
            if (svc.isRepositoryHasRemoteUrl()) {
                if (branch == null) { // from menu or tool bar, so need to get currentBranch
                    //has not remote url
                    this.branch = svc.getCurrentScmBranch();
                }
                trackRemoteIfPosible(branch, svc);
                RefSpec refSpec =  getRefSpec(branch);
                RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
                remoteUrl = params.getUrl();
                String result = Context.getGitRepoService().remoteRepositoryPush(params, refSpec, progressMonitor);
                Context.updateBranches();
                Context.updateWorkingBranch();
                return result;
            } else {
                throw new org.eclipse.jgit.api.errors.InvalidRemoteException("Invalid remote: origin");
            }
        }
    }

    /**
     * Pushes every workspace project that has a remote and at least one unpushed commit,
     * using each project's own repository and credentials. Per-project failures are captured
     * so one failing repo does not abort the rest.
     */
    private List<ProjectOperationResult<String>> pushWorkspace() {
        List<ProjectOperationResult<String>> results = new ArrayList<>();
        for (Project project : Context.getWorkspace().getProjects()) {
            try {
                GitRepoService svc = project.getGitRepoService();
                if (!svc.isRepositoryHasRemoteUrl()) {
                    continue;
                }
                CommitInfo head = svc.getHead();
                boolean unpushed = head.getSha() == null || svc.isCommitUnpushed(head.getSha());
                if (!unpushed) {
                    continue;
                }
                ScmBranch currentBranch = svc.getCurrentScmBranch();
                trackRemoteIfPosible(currentBranch, svc);
                RemoteRepoParameters params = RemoteRepoParameters.forProject(project, svc);
                String msg = svc.remoteRepositoryPush(params, getRefSpec(currentBranch), progressMonitor);
                results.add(ProjectOperationResult.ok(project, params.getUrl(), msg));
            } catch (Exception ex) {
                results.add(ProjectOperationResult.failed(project, ex));
            }
        }
        return results;
    }

    private RefSpec getRefSpec(ScmBranch branch) {
        if (branch != null) {
            return new RefSpec(branch.getFullName() + ":" + branch.getFullName());
        }
        return null;
    }

    private void trackRemoteIfPosible(ScmBranch branch, GitRepoService svc) throws IOException {
        if (branch.getRemoteMergeName() == null
                && branch.getBranchType() == ScmBranch.BranchType.LOCAL
                && svc.isRepositoryHasRemoteUrl()
        ) {
            svc.trackRemote(branch.getShortName(), branch.getShortName());
        }
    }

    @Override
    protected void onSuccess(String result) {
        if (workspaceResults != null) {
            long ok = workspaceResults.stream().filter(ProjectOperationResult::isSuccess).count();
            statusBar.setStatus("Push completed for " + ok + " of "
                    + workspaceResults.size() + " repositories");
            new PushResultDialog(parent, workspaceResults).setVisible(true);
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceView();
                mf.refreshWorkspaceProjectBranches(workspaceResults);
            }
            return;
        }
        statusBar.setStatus("Push completed");
        new PushResultDialog(parent, remoteUrl, result).setVisible(true);
    }

    @Override
    protected void onError(Exception e) {
        if (e instanceof org.eclipse.jgit.api.errors.InvalidRemoteException cgfException) {
            statusBar.setStatus(getOperationName() + " failed: " + e.getMessage());
            statusBar.clearProgress();
            JOptionPane.showMessageDialog(parent,
                    "Need to configure remote url\n" + e.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        } else  if (!credentialsPrompted && isAuthError(e)) {
            credentialsPrompted = true;
            if (promptAndSaveCredentials()) {
                execute();
                return;
            }
        }
        super.onError(e);
    }

}
