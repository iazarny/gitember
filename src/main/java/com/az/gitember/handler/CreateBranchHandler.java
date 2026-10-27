package com.az.gitember.handler;

import com.az.gitember.service.I18n;
import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.dialog.CreateBranchDialog;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.service.tracker.IssueTrackerService;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.mainframe.ActiveView;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.CheckoutConflictException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateBranchHandler extends AbstractAsyncHandler<String> {

    private static final Logger log = Logger.getLogger(CreateBranchHandler.class.getName());

    /**
     * Explicit base ref (tree context menu); {@code null} means "current HEAD" (menu / toolbar).
     */
    private final String baseBranchFullName;
    private final String newBranchName;

    /**
     * Non-null only when the branch was created across a workspace (aggregated per-project results).
     */
    private List<ProjectOperationResult<Void>> workspaceResults;

    public CreateBranchHandler(Component parent,
                               String baseBranchFullName, String newBranchName) {
        super(parent);
        this.baseBranchFullName = baseBranchFullName;
        this.newBranchName = newBranchName;
    }

    @Override
    protected String getOperationName() {
        return I18n.t("createBranch", newBranchName);
    }

    @Override
    protected String doInBackground() throws Exception {
        if (MainFrame.getInstance().isWorkspaceActive()) {
            workspaceResults = createBranchWorkspace();
        } else {
            GitRepoService svc = Context.getGitRepoService();
            String base = baseBranchFullName != null ? baseBranchFullName : svc.getCurrentScmBranch().getSha();
            svc.createBranch(base, newBranchName);

            svc.checkoutBranch(newBranchName, null);
            Context.updateBranches();

            if (Context.isWorkspaceMode()) {
                //it is possible to create branch for  single project in the workspac mode
                //so need to refresh
                if (parent instanceof MainFrame mf) {
                    List<ProjectOperationResult<Void>> res = new ArrayList<>();
                    for (Project project : Context.getWorkspace().getProjects()) { //TODO ugly
                        res.add(ProjectOperationResult.ok(project, null, null));
                    }
                    mf.refreshWorkspaceProjectBranches(res);
                    mf.refreshWorkspaceView();
                }
            } else {
                Context.updateWorkingBranch();
            }

        }
        return newBranchName;
    }

    protected void onError(Exception e) {
        final String messageTitle;
        final String message;
        final int messageType;
        if (e.getCause() instanceof CheckoutConflictException cce) {
            message = I18n.t("branchCreatedButNotPossible", e.getCause().getMessage());
            messageType = JOptionPane.WARNING_MESSAGE;
            messageTitle = I18n.t("warning");
        } else {
            message = I18n.t("opFailedDialog", getOperationName(), e.getMessage());
            messageType = JOptionPane.ERROR_MESSAGE;
            messageTitle = I18n.t("error");
        }
        log.log(Level.SEVERE, getOperationName() + " failed", e);
        statusBar.setStatus(I18n.t("opFailedStatus", getOperationName(), message));
        statusBar.clearProgress();
        JOptionPane.showMessageDialog(parent,  message,  messageTitle, messageType);
    }

    /**
     * Creates and checks out {@code newBranchName} in every workspace project, one repository
     * at a time (synchronously) so a failure in one project can't interleave with or abort the
     * rest. Each project branches from its own current HEAD.
     */
    private List<ProjectOperationResult<Void>> createBranchWorkspace() {
        List<ProjectOperationResult<Void>> results = new ArrayList<>();
        for (Project project : Context.getWorkspace().getProjects()) {
            try {
                GitRepoService svc = project.getGitRepoService();
                String base = svc.getCurrentScmBranch().getSha();
                svc.createBranch(base, newBranchName);
                svc.checkoutBranch(newBranchName, null);
                results.add(ProjectOperationResult.ok(project, null, null));
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
            long failed = workspaceResults.size() - ok;
            statusBar.setStatus(I18n.t("createdCheckedOutBranchRepositories", result, ok, workspaceResults.size(), (failed > 0 ? I18n.t("failedCountSuffix", failed) : "")));
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceProjectBranches(workspaceResults);
                mf.refreshWorkspaceView();
            }
            return;
        }
        statusBar.setStatus(I18n.t("createdCheckedOutBranch", result));
    }

    /**
     * Prompts user for branch name and executes if confirmed. Used from the branch/tag tree
     * context menu, where {@code baseBranchFullName} is the right-clicked branch.
     */
    public static void showAndExecute(Component parent, String baseBranchFullName, boolean isRemote) {
        String newBranchName = I18n.t("newBranchName");
        String branchTitle = I18n.t("createBranchHandler.createBranch");

        if (isRemote) {
            newBranchName = I18n.t("newLocalBranchName");
            branchTitle = I18n.t("createLocalBranch");
        }

        if (MainFrame.getInstance().getActiveView() == ActiveView.WORKSPACE) {
            branchTitle = I18n.t("createBranchEachRepository");
            if (isRemote) {
                newBranchName = I18n.t("newLocalBranchNameEach");
            }
        }

        String namePreset = "";
        if (isRemote) {
            namePreset = GitemberUtil.getLastPart(baseBranchFullName);
        }

        String name = promptBranchName(parent, branchTitle, newBranchName, namePreset);

        if (StringUtils.isNotBlank(name)) {
            new CreateBranchHandler(parent, baseBranchFullName, name.trim()).execute();
        }
    }

    /**
     * Prompts for a branch name and executes if confirmed. Used from the Branch menu / toolbar,
     * where there is no preselected base branch: the new branch starts from the current HEAD,
     * or — in workspace-active view — from each project's own current HEAD.
     */
    public static void showAndExecuteFromCurrent(Component parent) {
        String title = I18n.t("createBranchHandler.createBranch");
        String prompt = I18n.t("newBranchName");
        if (MainFrame.getInstance().getActiveView() == ActiveView.WORKSPACE) {
            title = I18n.t("createBranchEachRepository");
        }
        String name = promptBranchName(parent, title, prompt, "");

        if (StringUtils.isNotBlank(name)) {
            new CreateBranchHandler(parent, null, name.trim()).execute();
        }
    }

    private static String promptBranchName(Component parent, String title, String prompt, String namePreset) {
        String name = null;
        if (IssueTrackerService.isConfigured()) {
            Window owner = parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
            CreateBranchDialog dialog = new CreateBranchDialog(owner, title, prompt, namePreset);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                name = dialog.getBranchName();
                if (dialog.getSelectedIssue() != null) {
                    Context.setCurrentIssue(dialog.getSelectedIssue());
                }
            }
        } else {
            name = (String) JOptionPane.showInputDialog(
                    parent,
                    prompt,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    namePreset != null ? namePreset : ""
            );
        }
        return name;
    }
}
