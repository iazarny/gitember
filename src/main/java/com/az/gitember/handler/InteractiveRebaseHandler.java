package com.az.gitember.handler;

import com.az.gitember.dialog.InteractiveRebaseDialog;
import com.az.gitember.dialog.InteractiveRebaseDialog.RebaseAction;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for the interactive rebase workflow.
 *
 * <p>Call {@link #showAndExecute(Component, StatusBar, String, String)} from the
 * history-panel context menu or the Branch menu.  It will:
 * <ol>
 *   <li>Load all commits between HEAD (exclusive) and the chosen base commit (inclusive)
 *       in newest-first order.</li>
 *   <li>Open {@link InteractiveRebaseDialog} for the user to edit the plan.</li>
 *   <li>If confirmed, run the interactive rebase asynchronously and report the result.</li>
 * </ol>
 */
public class InteractiveRebaseHandler extends AbstractAsyncHandler<RebaseResult> {

    private final String baseCommitSha;
    private List<InteractiveRebaseDialog.RebaseStep> steps;

    private InteractiveRebaseHandler(Component parent, StatusBar statusBar,
                                     String baseCommitSha) {
        super(parent, statusBar);
        this.baseCommitSha = baseCommitSha;
    }

    // ── AbstractAsyncHandler contract ─────────────────────────────────────────

    @Override
    protected String getOperationName() { return "Interactive Rebase"; }

    @Override
    protected RebaseResult doInBackground() throws Exception {
        RebaseResult result = Context.getGitRepoService().interactiveRebase(baseCommitSha, steps);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(RebaseResult result) {
        RebaseResult.Status status = result.getStatus();
        statusBar.setStatus("Interactive rebase: " + status);

        if (status.isSuccessful()) {
            JOptionPane.showMessageDialog(parent,
                    "Interactive rebase completed successfully.",
                    "Interactive Rebase", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Non-fatal situations that require manual intervention
        StringBuilder msg = new StringBuilder("Interactive rebase finished with status: ");
        msg.append(status);

        if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
            msg.append("\n\nConflicts detected in:");
            result.getConflicts().forEach(f -> msg.append("\n  • ").append(f));
            msg.append("\n\nResolve the conflicts, stage the files, then run\n"
                     + "  git rebase --continue\n"
                     + "or abort with\n"
                     + "  git rebase --abort");
        } else if (status == RebaseResult.Status.STOPPED) {
            msg.append("\n\nThe rebase was paused (edit step or conflict).\n"
                     + "Resolve the issue, stage changes, then run\n"
                     + "  git rebase --continue\n"
                     + "or abort with\n"
                     + "  git rebase --abort");
        }

        JOptionPane.showMessageDialog(parent, msg.toString(),
                "Interactive Rebase", JOptionPane.WARNING_MESSAGE);
    }

    // ── Static entry point ────────────────────────────────────────────────────

    /**
     * Loads the commits in range, shows the dialog, then (if confirmed) runs
     * the interactive rebase.
     *
     * @param parent          component for dialog parenting
     * @param statusBar       status bar for progress messages
     * @param baseCommitSha   full SHA of the upstream / base commit (excluded from rebase)
     * @param baseDisplaySha  short SHA shown in the dialog title (may be abbreviated)
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String baseCommitSha, String baseDisplaySha) {
        Frame owner = parent instanceof Frame f
                ? f : (Frame) SwingUtilities.getWindowAncestor(parent);

        statusBar.setStatus("Loading commits for interactive rebase…");
        statusBar.showProgress(true);

        new SwingWorker<List<RevCommit>, Void>() {

            @Override
            protected List<RevCommit> doInBackground() throws Exception {
                return Context.getGitRepoService().getCommitsInRange(baseCommitSha);
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    List<RevCommit> commits = get();

                    if (commits.isEmpty()) {
                        JOptionPane.showMessageDialog(parent,
                                "No commits found between HEAD and " + baseDisplaySha + ".\n"
                                + "Nothing to rebase.",
                                "Interactive Rebase", JOptionPane.INFORMATION_MESSAGE);
                        statusBar.setStatus("Interactive rebase: nothing to do.");
                        return;
                    }

                    // Build the initial plan (newest-first, all actions = PICK)
                    List<InteractiveRebaseDialog.RebaseStep> initialSteps = new ArrayList<>();
                    for (RevCommit c : commits) {
                        initialSteps.add(new InteractiveRebaseDialog.RebaseStep(
                                RebaseAction.PICK,
                                c.getName(),          // full SHA for matching
                                c.getShortMessage())); // display message
                    }

                    InteractiveRebaseDialog dialog =
                            new InteractiveRebaseDialog(owner, baseDisplaySha, initialSteps);
                    dialog.setVisible(true);  // blocks until closed

                    if (!dialog.isConfirmed()) {
                        statusBar.setStatus("Interactive rebase cancelled.");
                        return;
                    }

                    // Wire up the background execution
                    InteractiveRebaseHandler handler =
                            new InteractiveRebaseHandler(parent, statusBar, baseCommitSha);
                    handler.steps = dialog.getSteps();
                    handler.execute();

                } catch (Exception ex) {
                    statusBar.setStatus("Interactive rebase failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Could not prepare interactive rebase:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
