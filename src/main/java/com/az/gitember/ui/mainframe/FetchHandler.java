package com.az.gitember.ui.mainframe;

import com.az.gitember.service.I18n;
import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.handler.AbstractAsyncHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FetchHandler extends AbstractAsyncHandler<Void> {

    /** Non-null only when the fetch ran across a workspace (aggregated per-project results). */
    private List<ProjectOperationResult<Void>> workspaceResults;

    public FetchHandler(Component parent) {
        super(parent);
    }

    @Override
    protected String getOperationName() {
        return I18n.t("fetch");
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (MainFrame.getInstance().isWorkspaceActive()) {
            workspaceResults = fetchWorkspace();
        } else {
            RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();
            Context.getGitRepoService().remoteRepositoryFetch(params, null, progressMonitor);
            Context.updateBranches();
            Context.updateTags();
            Context.updateWorkingBranch();
        }
        return null;
    }

    /**
     * Fetches every workspace project that has a remote, using each project's own repository and
     * credentials. Per-project failures are captured so one failing repo does not abort the rest.
     */
    private List<ProjectOperationResult<Void>> fetchWorkspace() {
        List<ProjectOperationResult<Void>> results = new ArrayList<>();
        for (Project project : Context.getWorkspace().getProjects()) {
            try {
                GitRepoService svc = project.getGitRepoService();
                if (!svc.isRepositoryHasRemoteUrl()) {
                    continue;
                }
                RemoteRepoParameters params = RemoteRepoParameters.forProject(project, svc);
                svc.remoteRepositoryFetch(params, null, progressMonitor);
                results.add(ProjectOperationResult.ok(project, params.getUrl(), null));
            } catch (Exception ex) {
                results.add(ProjectOperationResult.failed(project, ex));
            }
        }
        return results;
    }

    @Override
    protected void onSuccess(Void result) {
        if (workspaceResults != null) {
            long ok = workspaceResults.stream().filter(ProjectOperationResult::isSuccess).count();
            long failed = workspaceResults.size() - ok;
            statusBar.setStatus(I18n.t("fetchCompletedRepositories", ok, workspaceResults.size(), (failed > 0 ? I18n.t("failedCountSuffix", failed) : "")));
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceProjectBranches(workspaceResults);
                mf.refreshWorkspaceView();
            }
            return;
        }
        statusBar.setStatus(I18n.t("fetchCompleted"));
    }

    protected void onError(Exception e) {
        if (e instanceof org.eclipse.jgit.api.errors.InvalidRemoteException cgfException) {
            String msg = e.getMessage();
            if (StringUtils.contains(msg,"Invalid remote: origin")) {
                statusBar.setStatus(I18n.t("opFailedStatus", getOperationName(), e.getMessage()));
                statusBar.clearProgress();
                JOptionPane.showMessageDialog(parent,
                        I18n.t("needConfigureRemoteUrl", e.getMessage()),
                        I18n.t("warning"), JOptionPane.WARNING_MESSAGE);
            } else  {
                super.onError(e);
            }
        } else {
            super.onError(e);
        }

    }
}
