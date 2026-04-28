package com.az.gitember.dialog;

import com.az.gitember.service.Context;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modal dialog that indexes the git history into a Lucene index.
 *
 * <p>The user can choose how many of the most-recent commits to index
 * (default 1 000, or "All"). A progress bar and status label update as
 * indexing proceeds in a background {@link SwingWorker}.
 */
public class IndexHistoryDialog extends JDialog {

    private static final Logger log = Logger.getLogger(IndexHistoryDialog.class.getName());

    private final JSpinner maxCommitsSpinner;
    private final JCheckBox indexAllCheck;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final JButton startBtn;
    private final JButton closeBtn;

    private SwingWorker<Void, String> worker;
    private Runnable onComplete;

    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @SuppressWarnings("unchecked")
    public IndexHistoryDialog(Frame owner) {
        super(owner, "Index History", true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if (worker != null && !worker.isDone()) worker.cancel(true);
                dispose();
            }
        });
        setResizable(false);

        // ── Input row ────────────────────────────────────────────────────────
        int totalCommits = Context.getPlotCommitList() != null
                ? Context.getPlotCommitList().size() : 0;

        maxCommitsSpinner = new JSpinner(new SpinnerNumberModel(
                Math.min(1000, Math.max(totalCommits, 1)),
                1, Math.max(totalCommits, 1), 100));
        maxCommitsSpinner.setPreferredSize(new Dimension(100,
                maxCommitsSpinner.getPreferredSize().height));

        indexAllCheck = new JCheckBox("Index all (" + totalCommits + " commits)");
        indexAllCheck.addActionListener(e ->
                maxCommitsSpinner.setEnabled(!indexAllCheck.isSelected()));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inputRow.add(new JLabel("Max commits:"));
        inputRow.add(maxCommitsSpinner);
        inputRow.add(indexAllCheck);

        // ── Progress area ─────────────────────────────────────────────────────
        progressBar = new JProgressBar(0, Math.max(totalCommits, 1));
        progressBar.setStringPainted(true);
        progressBar.setString("");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel progressPanel = new JPanel(new BorderLayout(0, 4));
        progressPanel.add(progressBar,  BorderLayout.NORTH);
        progressPanel.add(statusLabel,  BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────────────────────
        startBtn = new JButton("Start Indexing");
        closeBtn  = new JButton("Close");
        closeBtn.setEnabled(true);

        startBtn.addActionListener(e -> startIndexing());
        closeBtn.addActionListener(e -> {
            if (worker != null && !worker.isDone()) worker.cancel(true);
            dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.add(startBtn);
        btnRow.add(closeBtn);

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        content.add(inputRow,     BorderLayout.NORTH);
        content.add(progressPanel, BorderLayout.CENTER);
        content.add(btnRow,       BorderLayout.SOUTH);

        if (totalCommits == 0) {
            statusLabel.setText("Load the commit history first (open Working Copy or History).");
            startBtn.setEnabled(false);
        }

        getContentPane().add(content);
        pack();
        setMinimumSize(new Dimension(480, getPreferredSize().height));
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);
    }

    @SuppressWarnings("unchecked")
    private void startIndexing() {
        List<PlotCommit<PlotLane>> commits = new ArrayList<>();
        if (Context.getPlotCommitList() != null) {
            for (var pc : Context.getPlotCommitList()) {
                commits.add((PlotCommit<PlotLane>) pc);
            }
        }

        if (commits.isEmpty()) {
            statusLabel.setText("No commits to index.");
            return;
        }

        int maxCommits = indexAllCheck.isSelected()
                ? 0
                : (int) maxCommitsSpinner.getValue();

        startBtn.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Starting…");

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().indexHistory(
                        (List) commits,
                        maxCommits,
                        (done, total) -> {
                            publish("Indexed commit " + done + " of " + total + "…");
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setMaximum(total);
                                progressBar.setValue(done);
                                progressBar.setString(done + " / " + total);
                            });
                        });
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Indexing complete. You can now search file content.");
                    progressBar.setString("Done");
                    if (onComplete != null) onComplete.run();
                } catch (java.util.concurrent.CancellationException ex) {
                    statusLabel.setText("Indexing cancelled.");
                    progressBar.setString("Cancelled");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Indexing failed", ex);
                    statusLabel.setText("Error: " + ex.getCause().getMessage());
                    progressBar.setString("Error");
                }
                startBtn.setEnabled(true);
            }
        };
        worker.execute();
    }
}
