package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.transport.RefSpec;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class DeleteBranchHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch branch;

    public DeleteBranchHandler(Component parent, StatusBar statusBar, ScmBranch branch) {
        super(parent, statusBar);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return "Delete " + branch.getShortName();
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().deleteLocalBranch(branch.getFullName());

        // If remote branch or tag, also push deletion to remote
        if (branch.getBranchType() != ScmBranch.BranchType.LOCAL) {
            Optional<Project> project = Context.getCurrentProject();
            if (project.isPresent()) {
                RemoteRepoParameters params = new RemoteRepoParameters(project.get());
                RefSpec refSpec = new RefSpec().setSource(null).setDestination(branch.getFullName());
                Context.getGitRepoService().remoteRepositoryPush(params, refSpec, null);
            }
        }

        Context.updateBranches();
        Context.updateWorkingBranch();
        return branch.getShortName();
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Deleted " + result);
    }

    /**
     * Shows confirmation dialog and executes if confirmed.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar, ScmBranch branch) {
        int result = JOptionPane.showConfirmDialog(parent,
                "Delete branch \"" + branch.getShortName() + "\"?",
                "Delete Branch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            new DeleteBranchHandler(parent, statusBar, branch).execute();
        }
    }
}
