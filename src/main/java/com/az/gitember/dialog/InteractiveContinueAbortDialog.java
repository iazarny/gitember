package com.az.gitember.dialog;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import com.az.gitember.ui.misc.Util;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.lib.RepositoryState;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Non-modal, always-on-top floating dialog shown while an interactive rebase
 * is paused ({@link RepositoryState#REBASING_INTERACTIVE}).
 *
 * <p>Has three buttons:
 * <ul>
 *   <li><b>Continue</b> – after the user stages resolved files, runs
 *       {@code git rebase --continue}.</li>
 *   <li><b>Skip</b> – runs {@code git rebase --skip}, discarding the current
 *       empty/unneeded commit (needed when {@code --continue} returns
 *       {@link RebaseResult.Status#NOTHING_TO_COMMIT}).</li>
 *   <li><b>Abort</b> – runs {@code git rebase --abort} and restores the
 *       original branch state.</li>
 * </ul>
 *
 * All operations run on a background thread.  When done the dialog disposes
 * itself and invokes the {@code onComplete} callback on the EDT (typically a
 * history-panel refresh).
 *
 * <p>Use {@link #showIfRebaseInProgress} to open the dialog only when
 * the repository is actually in the interactive-rebase paused state.
 */
public class InteractiveContinueAbortDialog extends JDialog {

    private static final Logger log =
            Logger.getLogger(InteractiveContinueAbortDialog.class.getName());

    /** Singleton guard – never open more than one at a time. */
    private static InteractiveContinueAbortDialog instance;

    private final StatusBar statusBar;
    private final Runnable  onComplete;

    /** Which git rebase sub-command to invoke. */
    private enum Op { CONTINUE, SKIP, ABORT }

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param owner           owning frame (may be {@code null})
     * @param conflicts       list of conflicting file paths, or {@code null}/empty
     * @param currentRevision short description of the commit that caused the stop
     *                        (e.g. "abc1234 – Commit message"), or {@code null}
     * @param statusBar       application status bar for progress feedback
     * @param onComplete      called on the EDT after the operation finishes; may be {@code null}
     */
    public InteractiveContinueAbortDialog(Frame owner,
                                          List<String> conflicts,
                                          String currentRevision,
                                          StatusBar statusBar,
                                          Runnable onComplete) {
        super(owner, "Interactive Rebase In Progress", false); // non-modal
        this.statusBar  = statusBar;
        this.onComplete = onComplete;

        setAlwaysOnTop(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // prevent accidental dismiss
        setResizable(false);
        setIconImages(Util.appIcons());

        // ── Message ───────────────────────────────────────────────────────────
        JLabel titleLabel = new JLabel(
                "<html><b>Interactive rebase paused.</b></html>");
        titleLabel.setForeground(new Color(160, 80, 0));

        StringBuilder sb = new StringBuilder("<html>");
        if (currentRevision != null && !currentRevision.isBlank()) {
            sb.append("Stopped at revision: <tt><b>").append(currentRevision).append("</b></tt><br><br>");
        }
        if (conflicts != null && !conflicts.isEmpty()) {
            sb.append("Conflicts detected in:<br><ul>");
            for (String f : conflicts) {
                sb.append("<li><tt>").append(f).append("</tt></li>");
            }
            sb.append("</ul>Resolve, <b>stage</b> the files, then click <b>Continue</b>.<br>"
                    + "If the commit becomes empty after resolution, use <b>Skip</b>.");
        } else {
            sb.append("Resolve any conflicts and <b>stage</b> the files,<br>"
                    + "then click <b>Continue</b> — or <b>Skip</b> if the commit is empty,<br>"
                    + "or <b>Abort</b> to cancel.");
        }
        sb.append("</html>");
        JLabel bodyLabel = new JLabel(sb.toString());

        JPanel messagePanel = new JPanel(new BorderLayout(0, 6));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        messagePanel.add(titleLabel, BorderLayout.NORTH);
        messagePanel.add(bodyLabel,  BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton continueBtn = Util.createButton(
                "Continue",
                "Stage resolved files, then continue the rebase",
                FontAwesomeSolid.PLAY);

        JButton skipBtn = Util.createButton(
                "Skip",
                "Discard the current (empty) commit and continue with the next one",
                FontAwesomeSolid.FORWARD);

        JButton abortBtn = Util.createButton(
                "Abort",
                "Abort the interactive rebase and restore the original branch",
                FontAwesomeSolid.BAN);

        continueBtn.addActionListener(e -> performOperation(Op.CONTINUE));
        skipBtn    .addActionListener(e -> performOperation(Op.SKIP));
        abortBtn   .addActionListener(e -> performOperation(Op.ABORT));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btnPanel.add(continueBtn);
        btnPanel.add(skipBtn);
        btnPanel.add(abortBtn);

        // ── Layout ────────────────────────────────────────────────────────────
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(messagePanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel,     BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(380, getHeight()));

        // Bottom-right of owner, or screen-centre as fallback
        if (owner != null) {
            Rectangle b = owner.getBounds();
            setLocation(b.x + b.width  - getWidth()  - 20,
                        b.y + b.height - getHeight() - 60);
        } else {
            setLocationRelativeTo(null);
        }
        Util.bindEscapeToDispose(this);

        // On macOS + FlatLaf window decorations, setAlwaysOnTop() alone is not
        // enough — clicking the main window can bury this dialog behind it.
        // Re-assert our position whenever the owner regains focus.
        if (owner != null) {
            java.awt.event.WindowFocusListener bringToFront =
                    new java.awt.event.WindowFocusListener() {
                @Override
                public void windowGainedFocus(java.awt.event.WindowEvent e) {
                    if (isDisplayable() && isVisible()) {
                        toFront();
                    }
                }
                @Override public void windowLostFocus(java.awt.event.WindowEvent e) {}
            };
            owner.addWindowFocusListener(bringToFront);
            // Remove the listener when this dialog is gone so we don't leak it
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    owner.removeWindowFocusListener(bringToFront);
                }
            });
        }
    }

    // ── Private operations ────────────────────────────────────────────────────

    private void performOperation(Op op) {
        String label = switch (op) {
            case CONTINUE -> "Rebase continue";
            case SKIP     -> "Rebase skip";
            case ABORT    -> "Rebase abort";
        };
        statusBar.setStatus(label + "\u2026");
        statusBar.showProgress(true);

        // Disable all buttons to prevent double-click
        disableButtons();

        new SwingWorker<RebaseResult, Void>() {
            @Override
            protected RebaseResult doInBackground() throws Exception {
                RebaseResult r = switch (op) {
                    case CONTINUE -> Context.getGitRepoService().rebaseContinue();
                    case SKIP     -> Context.getGitRepoService().rebaseSkip();
                    case ABORT    -> Context.getGitRepoService().rebaseAbort();
                };
                Context.updateAll();
                return r;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    RebaseResult result = get();
                    RebaseResult.Status status = result.getStatus();
                    statusBar.setStatus(label + ": " + status);

                    if (op != Op.ABORT && status == RebaseResult.Status.STOPPED) {
                        // Another conflict — reopen with updated info
                        reopenWith(result);
                        return;
                    }

                    // When continue produces an empty commit, skip it automatically
                    // instead of showing the dialog again.
                    if (op == Op.CONTINUE && status == RebaseResult.Status.NOTHING_TO_COMMIT) {
                        statusBar.setStatus("Empty commit \u2014 skipping automatically\u2026");
                        performOperation(Op.SKIP);
                        return;
                    }

                    // Guard: if the rebase is still paused for any other reason, reopen
                    if (op != Op.ABORT
                            && Context.getGitRepoService().getRepositoryState()
                               == RepositoryState.REBASING_INTERACTIVE) {
                        reopenWith(result);
                        return;
                    }

                    disposeAndNotify();

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, label + " failed", cause);
                    statusBar.setStatus(label + " failed: " + cause.getMessage());

                    // If continue/skip threw but the rebase is still in progress
                    // (e.g. UnmergedPathsException — user clicked Continue before
                    // staging all resolved files), keep the dialog open so the
                    // user can fix the issue and retry.
                    if (op != Op.ABORT
                            && Context.getGitRepoService().getRepositoryState()
                               == RepositoryState.REBASING_INTERACTIVE) {
                        enableButtons();
                        String msg = cause instanceof UnmergedPathsException
                                ? "Some conflict markers are still present.<br>"
                                  + "Resolve and <b>stage</b> all conflicted files, then click <b>Continue</b>."
                                : label + " failed: " + cause.getMessage();
                        JOptionPane.showMessageDialog(InteractiveContinueAbortDialog.this,
                                "<html>" + msg + "</html>",
                                "Rebase Operation Failed", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    disposeAndNotify();
                    JOptionPane.showMessageDialog(null,
                            label + " failed:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void disableButtons() {
        for (Component c : ((JPanel) getContentPane().getComponent(1)).getComponents()) {
            c.setEnabled(false);
        }
    }

    private void enableButtons() {
        for (Component c : ((JPanel) getContentPane().getComponent(1)).getComponents()) {
            c.setEnabled(true);
        }
    }

    private void disposeAndNotify() {
        instance = null;
        dispose();
        if (onComplete != null) onComplete.run();
    }

    private void reopenWith(RebaseResult newResult) {
        Frame owner  = (Frame) SwingUtilities.getWindowAncestor(this);
        Runnable cb  = onComplete;
        StatusBar sb = statusBar;
        instance = null;
        dispose();
        showIfRebaseInProgress(owner, newResult, sb, cb);
    }

    // ── Static factory ────────────────────────────────────────────────────────

    /**
     * Opens the dialog only when the repository is in
     * {@link RepositoryState#REBASING_INTERACTIVE} state.
     * If one is already showing this call is a no-op.
     */
    public static void showIfRebaseInProgress(Frame owner,
                                              StatusBar statusBar,
                                              Runnable onComplete) {
        showIfRebaseInProgress(owner, null, null, statusBar, onComplete);
    }

    /**
     * Same as {@link #showIfRebaseInProgress(Frame, StatusBar, Runnable)} but
     * also accepts a {@link RebaseResult} to display conflict files and the
     * revision that caused the stop.
     */
    public static void showIfRebaseInProgress(Frame owner,
                                              RebaseResult result,
                                              StatusBar statusBar,
                                              Runnable onComplete) {
        String revision = null;
        if (result != null && result.getCurrentCommit() != null) {
            revision = result.getCurrentCommit().getName().substring(0, 7)
                    + " \u2013 " + result.getCurrentCommit().getShortMessage();
        }
        showIfRebaseInProgress(owner,
                result != null ? result.getConflicts() : null,
                revision, statusBar, onComplete);
    }

    private static void showIfRebaseInProgress(Frame owner,
                                               List<String> conflicts,
                                               String currentRevision,
                                               StatusBar statusBar,
                                               Runnable onComplete) {
        if (instance != null && instance.isDisplayable()) return;
        if (Context.getGitRepoService().getRepositoryState()
                != RepositoryState.REBASING_INTERACTIVE) return;

        instance = new InteractiveContinueAbortDialog(
                owner, conflicts, currentRevision, statusBar, onComplete);
        instance.setVisible(true);
    }
}
