package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.mainframe.ActiveView;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.CheckoutConflictException;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenameBranchHandler extends AbstractAsyncHandler<String> {

    private static final Logger log = Logger.getLogger(RenameBranchHandler.class.getName());

    /**
     * Explicit base ref (tree context menu); {@code null} means "current HEAD" (menu / toolbar).
     */
    private final String oldBranchName;
    private final String newBranchName;
    private final boolean remote;

    /**
     * Non-null only when the branch was created across a workspace (aggregated per-project results).
     */
    private List<ProjectOperationResult<Void>> workspaceResults;

    public RenameBranchHandler(Component parent,
                               String oldBranchName, String newBranchName, boolean remote) {
        super(parent);
        this.remote = remote;
        this.oldBranchName = oldBranchName;
        this.newBranchName = newBranchName;
    }

    @Override
    protected String getOperationName() {
        return "Rename branch " + oldBranchName + " to "  + newBranchName;
    }

    @Override
    protected String doInBackground() throws Exception {
        GitRepoService svc = Context.getGitRepoService();
        if (remote) {
            svc.renameRemoteBranch(oldBranchName, newBranchName);
        } else {
            svc.renameBranch(oldBranchName, newBranchName);
        }
        Context.updateBranches();
        Context.updateWorkingBranch();
        return newBranchName;
    }

    protected void onError(Exception e) {
        final String messageTitle;
        final String message;
        final int messageType;
        message = getOperationName() + " failed:\n" + e.getMessage();
        messageType = JOptionPane.ERROR_MESSAGE;
        messageTitle = "Error";
        log.log(Level.SEVERE, getOperationName() + " failed", e);
        statusBar.setStatus(getOperationName() + " failed: " + message);
        statusBar.clearProgress();
        JOptionPane.showMessageDialog(parent,  message,  messageTitle, messageType);
    }


    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Renamed  branch to " + result + " is ok ");
    }

    /**
     * Prompts user for branch name and executes if confirmed. Used from the branch/tag tree
     * context menu, where {@code baseBranchFullName} is the right-clicked branch.
     */
    public static void showAndExecute(Component parent, String oldName, boolean remote) {

        String name = (String) JOptionPane.showInputDialog(
                parent,
                "New branch name:",
                MessageFormat.format("Rename {0} ", oldName),
                JOptionPane.PLAIN_MESSAGE
        );

        if (StringUtils.isNotBlank(name)) {

            String fullName = name;
            if (remote) {
                String parentPath = StringUtils.substringBeforeLast(oldName, "/");
                fullName = parentPath + "/" + name;
            }

            new RenameBranchHandler(parent, oldName, fullName.trim(), remote).execute();
        }
    }

}
