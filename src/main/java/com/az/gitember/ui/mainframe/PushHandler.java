package com.az.gitember.ui.mainframe;

import com.az.gitember.data.CommitInfo;
import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.dialog.PushResultDialog;
import com.az.gitember.handler.AbstractAsyncHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PushHandler extends AbstractAsyncHandler<String> {

    private String remoteUrl;
    private boolean credentialsPrompted = false;

    /** Non-null only when the push ran across a workspace (aggregated per-project results). */
    private List<ProjectOperationResult<String>> workspaceResults;

    public PushHandler(Component parent) {
        super(parent);
    }

    @Override
    protected String getOperationName() {
        return "Push";
    }

    @Override
    protected String doInBackground() throws Exception {
        if (Context.isWorkspaceActive()) {
            workspaceResults = pushWorkspace();
            return null;
        } else {

            RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
            remoteUrl = params.getUrl();

            String result = Context.getGitRepoService().remoteRepositoryPush(params, null, progressMonitor);
            Context.updateBranches();
            Context.updateWorkingBranch();
            return result;
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
            try (GitRepoService svc = GitRepoService.of(project)) {
                if (!svc.isRepositoryHasRemoteUrl()) {
                    continue;
                }
                CommitInfo head = svc.getHead();
                boolean unpushed = head.getSha() == null || svc.isCommitUnpushed(head.getSha());
                if (!unpushed) {
                    continue;
                }
                RemoteRepoParameters params = RemoteRepoParameters.forProject(project, svc);
                String msg = svc.remoteRepositoryPush(params, null, progressMonitor);
                results.add(ProjectOperationResult.ok(project, params.getUrl(), msg));
            } catch (Exception ex) {
                results.add(ProjectOperationResult.failed(project, ex));
            }
        }
        return results;
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
            }
            return;
        }
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
