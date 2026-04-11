package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.WorktreeInfo;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.transport.RefSpec;

import javax.swing.*;
import java.awt.*;
import java.util.List;
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
        // Refuse deletion if the branch is checked out in a linked worktree
        if (branch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            try {
                List<WorktreeInfo> worktrees = Context.getGitRepoService().listWorktrees();
                WorktreeInfo conflict = worktrees.stream()
                        .filter(wt -> !wt.isMain())
                        .filter(wt -> branch.getShortName().equals(wt.getBranch()))
                        .findFirst()
                        .orElse(null);
                if (conflict != null) {
                    JOptionPane.showMessageDialog(parent,
                            "<html>Cannot delete branch <b>" + branch.getShortName() + "</b>.<br><br>"
                            + "It is currently checked out in a linked worktree at:<br>"
                            + "<tt>" + conflict.getPath() + "</tt><br><br>"
                            + "Remove or switch the worktree first.</html>",
                            "Branch In Use", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ignored) {
                // If listing worktrees fails, fall through and let git decide
            }
        }

        String message;
        if (branch.getRemoteMergeName() != null && branch.getAheadCount() > 0) {
            message = "<html>Delete branch <b>" + branch.getShortName() + "</b>?<br><br>"
                    + "<b style='color:orange'>\u26a0 " + branch.getAheadCount()
                    + " unpushed commit(s)</b> will be lost<br>"
                    + "(not yet pushed to <tt>" + branch.getRemoteMergeName() + "</tt>).</html>";
        } else {
            message = "Delete branch \"" + branch.getShortName() + "\"?";
        }
        int result = JOptionPane.showConfirmDialog(parent, message,
                "Delete Branch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            new DeleteBranchHandler(parent, statusBar, branch).execute();
        }
    }
}
