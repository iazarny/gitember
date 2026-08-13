package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.mainframe.ActiveView;
import com.sun.tools.javac.Main;
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
        return "Create branch " + newBranchName;
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
            message = "Branch is created. But not possible to checkout it.\n" + e.getCause().getMessage();
            messageType = JOptionPane.WARNING_MESSAGE;
            messageTitle = "Warning";
        } else {
            message = getOperationName() + " failed:\n" + e.getMessage();
            messageType = JOptionPane.ERROR_MESSAGE;
            messageTitle = "Error";
        }
        log.log(Level.SEVERE, getOperationName() + " failed", e);
        statusBar.setStatus(getOperationName() + " failed: " + message);
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
            statusBar.setStatus("Created and checked out branch " + result + " in " + ok + " of "
                    + workspaceResults.size() + " repositories" + (failed > 0 ? " (" + failed + " failed)" : ""));
            if (parent instanceof MainFrame mf) {
                mf.refreshWorkspaceProjectBranches(workspaceResults);
                mf.refreshWorkspaceView();
            }
            return;
        }
        statusBar.setStatus("Created and checked out branch " + result);
    }

    /**
     * Prompts user for branch name and executes if confirmed. Used from the branch/tag tree
     * context menu, where {@code baseBranchFullName} is the right-clicked branch.
     */
    public static void showAndExecute(Component parent, String baseBranchFullName, boolean isRemote) {
        String newBranchName = "New branch name:";
        String branchTitle = "Create Branch";

        if (isRemote) {
            newBranchName = "New local branch name:";
            branchTitle = "Create local branch";
        }

        if (MainFrame.getInstance().getActiveView() == ActiveView.WORKSPACE) {
            branchTitle = "Create Branch in each repository";
            if (isRemote) {
                newBranchName = "New local branch name in each repo:";
            }
        }

        String namePreset = "";
        if (isRemote) {
            namePreset = GitemberUtil.getLastPart(baseBranchFullName);
        }

        String name = (String) JOptionPane.showInputDialog(
                parent,
                newBranchName,
                branchTitle,
                JOptionPane.PLAIN_MESSAGE,
                null,        // icon
                null,        // selectionValues (null = free text)
                namePreset
        );

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
        String name = (String) JOptionPane.showInputDialog(
                parent,
                "New branch name:",
                "Create Branch",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );

        if (StringUtils.isNotBlank(name)) {
            new CreateBranchHandler(parent, null, name.trim()).execute();
        }
    }
}
