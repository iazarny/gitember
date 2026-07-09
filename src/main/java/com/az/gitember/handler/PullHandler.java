package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.data.PullOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PullResultDialog;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PullHandler extends AbstractAsyncHandler<PullOperationResult> {

    /** Non-null only when the pull ran across a workspace (aggregated per-project results). */
    private List<ProjectOperationResult<PullOperationResult>> workspaceResults;

    public PullHandler(Component parent, StatusBar statusBar) {
        super(parent, statusBar);
    }

    @Override
    protected String getOperationName() {
        return "Pull";
    }

    @Override
    protected PullOperationResult doInBackground() throws Exception {

        if (Context.isWorkspaceActive()) {
            workspaceResults = pullWorkspace();
            return null;
        } else {
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
    }

    /**
     * Pulls every workspace project that has a remote, using each project's own repository and
     * credentials. The remote branch is left to JGit (tracking branch of the checked-out branch).
     * Per-project failures are captured so one failing repo does not abort the rest.
     */
    private List<ProjectOperationResult<PullOperationResult>> pullWorkspace() {
        List<ProjectOperationResult<PullOperationResult>> results = new ArrayList<>();
        for (Project project : Context.getWorkspace().getProjects()) {
            try (GitRepoService svc = GitRepoService.of(project)) {
                if (!svc.isRepositoryHasRemoteUrl()) {
                    continue;
                }
                RemoteRepoParameters params = RemoteRepoParameters.forProject(project, svc);
                PullOperationResult res = svc.remoteRepositoryPull(params, null, progressMonitor);
                results.add(ProjectOperationResult.ok(project, params.getUrl(), res));
            } catch (Exception ex) {
                results.add(ProjectOperationResult.failed(project, ex));
            }
        }
        return results;
    }

    @Override
    protected void onSuccess(PullOperationResult result) {
        if (workspaceResults != null) {
            long ok = workspaceResults.stream().filter(ProjectOperationResult::isSuccess).count();
            statusBar.setStatus("Pull completed for " + ok + " of "
                    + workspaceResults.size() + " repositories");
            new PullResultDialog(parent, workspaceResults).setVisible(true);
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceView();
            }
            return;
        }

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
