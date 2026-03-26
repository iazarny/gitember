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
        if (branch.getBranchType() == ScmBranch.BranchType.REMOTE) {
            // Push deletion to remote: fullName is "refs/remotes/origin/foo",
            // but the remote server expects "refs/heads/foo"
            Optional<Project> project = Context.getCurrentProject();
            if (project.isPresent()) {
                RemoteRepoParameters params = new RemoteRepoParameters(project.get());
                String remoteRef = branch.getFullName()
                        .replaceFirst("^refs/remotes/[^/]+/", "refs/heads/");
                RefSpec refSpec = new RefSpec().setSource(null).setDestination(remoteRef);
                Context.getGitRepoService().remoteRepositoryPush(params, refSpec, null);
            }
            // Remove the local remote-tracking ref
            Context.getGitRepoService().deleteRemoteTrackingBranch(branch.getFullName());
        } else {
            Context.getGitRepoService().deleteLocalBranch(branch.getFullName());
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
