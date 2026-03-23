package com.az.gitember.dialog;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import com.az.gitember.ui.Util;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.RepositoryState;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Non-modal, always-on-top dialog shown while an interactive rebase is paused
 * (repository state {@link RepositoryState#REBASING_INTERACTIVE}).
 *
 * <p>The dialog provides two actions:
 * <ul>
 *   <li><b>Continue</b> – stage resolved files first, then runs
 *       {@code git rebase --continue}.</li>
 *   <li><b>Abort</b> – discards in-progress work and runs
 *       {@code git rebase --abort}.</li>
 * </ul>
 *
 * Both operations run on a background thread; the dialog disposes itself and
 * invokes the supplied {@code onComplete} callback on the EDT when done.
 *
 * <p>Use {@link #showIfRebaseInProgress} to check state and open the dialog
 * only when appropriate.
 */
public class InteractiveContinueAbortDialog extends JDialog {

    private static final Logger log = Logger.getLogger(InteractiveContinueAbortDialog.class.getName());

    /** Singleton reference so we never open more than one at a time. */
    private static InteractiveContinueAbortDialog instance;

    private final StatusBar statusBar;
    private final Runnable  onComplete;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param owner       owning frame (may be {@code null})
     * @param conflicts   list of conflicting file paths, or {@code null} / empty
     * @param statusBar   application status bar
     * @param onComplete  called on the EDT after the operation finishes
     *                    (success, error, or abort); may be {@code null}
     */
    public InteractiveContinueAbortDialog(Frame owner,
                                          List<String> conflicts,
                                          StatusBar statusBar,
                                          Runnable onComplete) {
        super(owner, "Interactive Rebase In Progress", false); // non-modal
        this.statusBar  = statusBar;
        this.onComplete = onComplete;

        setAlwaysOnTop(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // prevent accidental close
        setResizable(false);
        setIconImages(Util.appIcons());

        // ── Message area ─────────────────────────────────────────────────────
        JPanel messagePanel = new JPanel(new BorderLayout(0, 6));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));

        JLabel titleLabel = new JLabel(
                "<html><b>Interactive rebase paused.</b></html>");
        titleLabel.setForeground(new Color(160, 80, 0));

        String bodyText;
        if (conflicts != null && !conflicts.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    "<html>Conflicts detected in the following files:<br><ul>");
            for (String f : conflicts) {
                sb.append("<li><tt>").append(f).append("</tt></li>");
            }
            sb.append("</ul>")
              .append("Resolve the conflicts, <b>stage</b> the files,<br>")
              .append("then click <b>Continue</b>.</html>");
            bodyText = sb.toString();
        } else {
            bodyText = "<html>Resolve any conflicts, <b>stage</b> the files,<br>"
                     + "then click <b>Continue</b>.<br>"
                     + "Click <b>Abort</b> to restore the original state.</html>";
        }

        JLabel bodyLabel = new JLabel(bodyText);

        messagePanel.add(titleLabel, BorderLayout.NORTH);
        messagePanel.add(bodyLabel,  BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────────────────────
        JButton continueBtn = Util.createButton(
                "Continue",
                "Stage all resolved files, then continue the rebase",
                FontAwesomeSolid.PLAY);

        JButton abortBtn = Util.createButton(
                "Abort",
                "Abort the interactive rebase and restore the original branch state",
                FontAwesomeSolid.BAN);

        continueBtn.addActionListener(e -> performOperation(true));
        abortBtn   .addActionListener(e -> performOperation(false));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btnPanel.add(continueBtn);
        btnPanel.add(abortBtn);

        // ── Assembly ──────────────────────────────────────────────────────────
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(messagePanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel,     BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(380, getHeight()));

        // Position: bottom-right of the owner (or screen centre as fallback)
        if (owner != null) {
            Rectangle ob = owner.getBounds();
            setLocation(ob.x + ob.width  - getWidth()  - 20,
                        ob.y + ob.height - getHeight() - 60);
        } else {
            setLocationRelativeTo(null);
        }
    }

    // ── Operations ────────────────────────────────────────────────────────────

    /**
     * Runs the continue or abort operation on a background thread.
     *
     * @param doContinue {@code true} → continue; {@code false} → abort
     */
    private void performOperation(boolean doContinue) {
        String opName = doContinue ? "Rebase continue" : "Rebase abort";
        statusBar.setStatus(opName + "\u2026");
        statusBar.showProgress(true);

        // Disable buttons to prevent double-click
        for (Component c : ((JPanel) getContentPane().getComponent(1)).getComponents()) {
            c.setEnabled(false);
        }

        new SwingWorker<RebaseResult, Void>() {
            @Override
            protected RebaseResult doInBackground() throws Exception {
                RebaseResult result = doContinue
                        ? Context.getGitRepoService().rebaseContinue()
                        : Context.getGitRepoService().rebaseAbort();
                Context.updateAll();
                return result;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    RebaseResult result = get();
                    RebaseResult.Status status = result.getStatus();
                    statusBar.setStatus(opName + ": " + status);

                    if (status == RebaseResult.Status.STOPPED) {
                        // Still paused – update the conflict list and stay open
                        List<String> newConflicts = result.getConflicts();
                        reopenWith(newConflicts);
                        return;
                    }

                    disposeAndNotify();

                    if (!status.isSuccessful() && doContinue) {
                        JOptionPane.showMessageDialog(
                                InteractiveContinueAbortDialog.this,
                                "Rebase continue finished with status: " + status,
                                "Interactive Rebase", JOptionPane.WARNING_MESSAGE);
                    }

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, opName + " failed", cause);
                    statusBar.setStatus(opName + " failed: " + cause.getMessage());
                    disposeAndNotify();
                    JOptionPane.showMessageDialog(
                            null,
                            opName + " failed:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Disposes this dialog, clears the singleton, then fires the onComplete callback. */
    private void disposeAndNotify() {
        instance = null;
        dispose();
        if (onComplete != null) onComplete.run();
    }

    /**
     * Closes the current dialog and opens a fresh one with an updated conflict list.
     * Used when "Continue" results in another STOPPED state (i.e. more conflicts).
     */
    private void reopenWith(List<String> newConflicts) {
        Frame owner   = (Frame) SwingUtilities.getWindowAncestor(this);
        Runnable cb   = onComplete;
        StatusBar sb  = statusBar;
        instance = null;
        dispose();
        showIfRebaseInProgress(owner, newConflicts, sb, cb);
    }

    // ── Static factory ────────────────────────────────────────────────────────

    /**
     * Opens the dialog only if the repository is currently in
     * {@link RepositoryState#REBASING_INTERACTIVE} state.
     * If the dialog is already showing, this is a no-op.
     *
     * @param owner     owning frame
     * @param statusBar application status bar
     * @param onComplete called after the operation completes; may be {@code null}
     */
    public static void showIfRebaseInProgress(Frame owner, StatusBar statusBar,
                                              Runnable onComplete) {
        showIfRebaseInProgress(owner, null, statusBar, onComplete);
    }

    /**
     * Same as {@link #showIfRebaseInProgress(Frame, StatusBar, Runnable)} but also
     * accepts a pre-built conflict list (avoids a redundant state query when the
     * caller already has this information).
     */
    public static void showIfRebaseInProgress(Frame owner,
                                              List<String> conflicts,
                                              StatusBar statusBar,
                                              Runnable onComplete) {
        if (instance != null && instance.isDisplayable()) return; // already open

        RepositoryState state = Context.getGitRepoService().getRepositoryState();
        if (state != RepositoryState.REBASING_INTERACTIVE) return;

        instance = new InteractiveContinueAbortDialog(owner, conflicts, statusBar, onComplete);
        instance.setVisible(true);
    }
}
