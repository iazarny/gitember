package com.az.gitember.ui.mainframe;

import com.az.gitember.service.I18n;
import com.az.gitember.data.*;
import com.az.gitember.dialog.PullResultDialog;
import com.az.gitember.handler.AbstractAsyncHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PullHandler extends AbstractAsyncHandler<PullOperationResult> {

    /** Non-null only when the pull ran across a workspace (aggregated per-project results). */
    private List<ProjectOperationResult<PullOperationResult>> workspaceResults;

    private ScmBranch branch;

    public PullHandler(Component parent, ScmBranch branch) {
        super(parent);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return I18n.t("mainMenuBar.pull");
    }

    @Override
    protected PullOperationResult doInBackground() throws Exception {

        if (MainFrame.getInstance().isWorkspaceActive()) {
            workspaceResults = pullWorkspace();
            return null;
        } else {
            RemoteRepoParameters params = RemoteRepoParameters.forCurrentRepo();

            String remoteBranch = null;
            if (Context.getWorkingBranch() != null) {
                remoteBranch = Context.getWorkingBranch().getRemoteMergeName();
            } else if (branch != null) {
                remoteBranch =  branch.getRemoteMergeName();
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
            try {
                GitRepoService svc = project.getGitRepoService();
                if (svc.isRepositoryHasRemoteUrl()) {
                    RemoteRepoParameters params = RemoteRepoParameters.forProject(project, svc);
                    PullOperationResult res = svc.remoteRepositoryPull(params, null, progressMonitor);
                    results.add(ProjectOperationResult.ok(project, params.getUrl(), res));
                } else {
                    PullOperationResult res = new PullOperationResult(
                            I18n.t("skipOperation"), "",
                            Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),""
                    );
                    results.add(ProjectOperationResult.ok(project, "N/A", res));
                }

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
            statusBar.setStatus(I18n.t("pullCompletedRepositories", ok, workspaceResults.size()));
            new PullResultDialog(parent, workspaceResults).setVisible(true);
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceProjectBranches(workspaceResults);
                mf.refreshWorkspaceView();
            }
            return;
        }

        statusBar.setStatus(I18n.t("pullHandler.pullCompleted", result.toStatusString()));
        if (MainFrame.getInstance().isWorkspaceActive()) {
            Context.refreshWorkingCopy();
        } else {
            Context.refreshHistory();
        }
        // Show the result dialog first (it's modal, so this blocks until closed)
        new PullResultDialog(parent, result).setVisible(true);
        // After the user closes the dialog, navigate to the appropriate view
        navigateAfterPull(result);
    }

    @Override
    protected void onError(Exception e) {
        if (e instanceof org.eclipse.jgit.api.errors.InvalidConfigurationException cgfException) {
            String msg = e.getMessage();
            if (StringUtils.contains(msg,"remote.origin.url")
                && StringUtils.contains(msg,"No value for key")) {
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
