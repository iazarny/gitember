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
 * <p>Call {@link #showAndExecute} from the history-panel context menu or the
 * Branch menu.  It will:
 * <ol>
 *   <li>Load all commits between HEAD and the chosen base commit on a background
 *       thread (history panel remains intact during this step).</li>
 *   <li>Open {@link InteractiveRebaseDialog} for the user to edit the plan.</li>
 *   <li>If confirmed, run the interactive rebase asynchronously.</li>
 *   <li>Invoke the supplied {@code onComplete} callback on the EDT so the caller
 *       can refresh whatever UI it owns (e.g. reload the history panel).</li>
 * </ol>
 */
public class InteractiveRebaseHandler extends AbstractAsyncHandler<RebaseResult> {

    private final String   baseCommitSha;
    private final Runnable onComplete;
    private List<InteractiveRebaseDialog.RebaseStep> steps;

    private InteractiveRebaseHandler(Component parent, StatusBar statusBar,
                                     String baseCommitSha, Runnable onComplete) {
        super(parent, statusBar);
        this.baseCommitSha = baseCommitSha;
        this.onComplete    = onComplete;
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

        // Always refresh the caller's view after the operation completes
        if (onComplete != null) onComplete.run();

        if (status.isSuccessful()) {
            JOptionPane.showMessageDialog(parent,
                    "Interactive rebase completed successfully.",
                    "Interactive Rebase", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Non-fatal situations that need manual follow-up
        StringBuilder msg = new StringBuilder("Interactive rebase finished with status: ")
                .append(status);

        if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
            msg.append("\n\nConflicts detected in:");
            result.getConflicts().forEach(f -> msg.append("\n  \u2022 ").append(f));
            msg.append("\n\nResolve the conflicts, stage the files, then run"
                     + "\n  git rebase --continue"
                     + "\nor abort with"
                     + "\n  git rebase --abort");
        } else if (status == RebaseResult.Status.STOPPED) {
            msg.append("\n\nThe rebase was paused (edit step or conflict)."
                     + "\nResolve the issue, stage changes, then run"
                     + "\n  git rebase --continue"
                     + "\nor abort with"
                     + "\n  git rebase --abort");
        }

        JOptionPane.showMessageDialog(parent, msg.toString(),
                "Interactive Rebase", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    protected void onError(Exception e) {
        // Still refresh so the history panel shows the repo's current state
        if (onComplete != null) onComplete.run();
        super.onError(e);
    }

    // ── Static entry points ───────────────────────────────────────────────────

    /**
     * Convenience overload — no post-completion callback.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String baseCommitSha, String baseDisplaySha) {
        showAndExecute(parent, statusBar, baseCommitSha, baseDisplaySha, null);
    }

    /**
     * Loads commits in range, opens the dialog, and (if confirmed) runs the
     * interactive rebase.  The history panel is <em>not</em> touched until
     * {@code onComplete} is called after the rebase finishes.
     *
     * @param parent         component used for dialog parenting
     * @param statusBar      status bar for progress feedback
     * @param baseCommitSha  full SHA of the upstream commit (excluded from rebase)
     * @param baseDisplaySha abbreviated SHA shown in the dialog header
     * @param onComplete     called on the EDT after success <em>or</em> error;
     *                       may be {@code null}
     */
    public static void showAndExecute(Component parent, StatusBar statusBar,
                                      String baseCommitSha, String baseDisplaySha,
                                      Runnable onComplete) {
        Frame owner = parent instanceof Frame f
                ? f : (Frame) SwingUtilities.getWindowAncestor(parent);

        statusBar.setStatus("Loading commits for interactive rebase\u2026");
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

                    // Build initial plan: newest-first, all PICK
                    List<InteractiveRebaseDialog.RebaseStep> initialSteps = new ArrayList<>();
                    for (RevCommit c : commits) {
                        initialSteps.add(new InteractiveRebaseDialog.RebaseStep(
                                RebaseAction.PICK,
                                c.getName(),
                                c.getShortMessage()));
                    }

                    // Show the dialog — blocks the EDT until the user closes it
                    InteractiveRebaseDialog dialog =
                            new InteractiveRebaseDialog(owner, baseDisplaySha, initialSteps);
                    dialog.setVisible(true);

                    if (!dialog.isConfirmed()) {
                        statusBar.setStatus("Interactive rebase cancelled.");
                        return;
                    }

                    InteractiveRebaseHandler handler =
                            new InteractiveRebaseHandler(parent, statusBar,
                                                         baseCommitSha, onComplete);
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
