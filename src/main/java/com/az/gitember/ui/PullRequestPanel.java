package com.az.gitember.ui;

import com.az.gitember.data.PullRequest;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.ui.CommitDetailPanel.ChangedFilesTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel shown when a Pull Request node is selected in the tree.
 * Displays PR metadata and the list of files changed in the PR.
 */
public class PullRequestPanel extends JPanel {

    private static final Logger log = Logger.getLogger(PullRequestPanel.class.getName());

    // Header fields
    private final JTextField titleField   = readOnlyField();
    private final JTextField authorField  = readOnlyField();
    private final JTextField stateField   = readOnlyField();
    private final JTextField branchField  = readOnlyField();
    private final JButton    openUrlBtn   = new JButton("Open in Browser");

    // Files table
    private final ChangedFilesTableModel filesModel = new ChangedFilesTableModel();
    private final JTable filesTable;

    private PullRequest currentPr;

    public PullRequestPanel() {
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // --- Files table ---
        filesTable = new JTable(filesModel);
        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filesTable.setShowVerticalLines(false);
        filesTable.setIntercellSpacing(new Dimension(0, 0));
        filesTable.setRowHeight(22);
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        filesTable.getColumnModel().getColumn(0).setMaxWidth(90);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(700);
        filesTable.getColumnModel().getColumn(0).setCellRenderer(new StatusCellRenderer());

        filesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = filesTable.rowAtPoint(e.getPoint());
                    if (row >= 0) openFileDiff(filesModel.getItemAt(row));
                }
            }
        });

        JPanel filesPanel = new JPanel(new BorderLayout());
        JLabel filesLabel = new JLabel("  Changed files");
        filesLabel.setFont(filesLabel.getFont().deriveFont(Font.BOLD));
        filesLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        filesPanel.add(filesLabel, BorderLayout.NORTH);
        filesPanel.add(new JScrollPane(filesTable), BorderLayout.CENTER);
        add(filesPanel, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(2, 2, 2, 6);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(2, 0, 2, 10);

        // Row 0: Title (spans all columns)
        lc.gridx = 0; lc.gridy = 0;
        panel.add(new JLabel("Title:"), lc);
        fc.gridx = 1; fc.gridy = 0; fc.gridwidth = 5;
        panel.add(titleField, fc);
        fc.gridwidth = 1;

        // Row 1: Author | State | Branches
        lc.gridx = 0; lc.gridy = 1;
        panel.add(new JLabel("Author:"), lc);
        fc.gridx = 1; fc.gridy = 1;
        panel.add(authorField, fc);

        lc.gridx = 2; lc.gridy = 1;
        panel.add(new JLabel("State:"), lc);
        fc.gridx = 3; fc.gridy = 1;
        panel.add(stateField, fc);

        lc.gridx = 4; lc.gridy = 1;
        panel.add(new JLabel("Branches:"), lc);
        fc.gridx = 5; fc.gridy = 1;
        panel.add(branchField, fc);

        // Row 2: Open button
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 2; bc.gridwidth = 6;
        bc.anchor = GridBagConstraints.WEST;
        bc.insets = new Insets(4, 0, 2, 0);
        openUrlBtn.setEnabled(false);
        openUrlBtn.addActionListener(e -> openInBrowser());
        panel.add(openUrlBtn, bc);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, panel.getBackground().darker()),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return panel;
    }

    public void showPullRequest(PullRequest pr) {
        this.currentPr = pr;
        filesModel.clear();

        if (pr == null) {
            titleField.setText("");
            authorField.setText("");
            stateField.setText("");
            branchField.setText("");
            openUrlBtn.setEnabled(false);
            return;
        }

        titleField.setText("#" + pr.number() + "  " + pr.title());
        authorField.setText(pr.author() != null ? pr.author() : "");
        stateField.setText(pr.state() != null ? pr.state() : "");
        branchField.setText(pr.sourceBranch() + "  →  " + pr.targetBranch());
        openUrlBtn.setEnabled(pr.webUrl() != null && !pr.webUrl().isBlank());

        loadChangedFiles(pr);
    }

    private void loadChangedFiles(PullRequest pr) {
        SwingWorker<List<ScmItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScmItem> doInBackground() throws Exception {
                return Context.getGitRepoService()
                        .getPrChangedFiles(pr.sourceBranch(), pr.targetBranch());
            }

            @Override
            protected void done() {
                try {
                    filesModel.setData(get());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load PR files", ex);
                    JOptionPane.showMessageDialog(PullRequestPanel.this,
                            "Could not load changed files:\n" + ex.getMessage()
                            + "\n\nNote: branches must be available locally or as remote-tracking refs.",
                            "PR files", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openFileDiff(ScmItem item) {
        if (item == null || currentPr == null) return;
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String targetContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.targetBranch(), item.getShortName());
                String sourceContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.sourceBranch(), item.getShortName());
                return new String[]{targetContent, sourceContent};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    DiffViewerWindow w = new DiffViewerWindow(
                            item.getShortName(),
                            currentPr.targetBranch() + " → " + currentPr.sourceBranch(),
                            texts[0], texts[1]);
                    w.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to show PR file diff", ex);
                    JOptionPane.showMessageDialog(PullRequestPanel.this,
                            "Cannot show diff: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openInBrowser() {
        if (currentPr == null || currentPr.webUrl() == null) return;
        try {
            Desktop.getDesktop().browse(new URI(currentPr.webUrl()));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot open browser", ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot open browser: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JTextField readOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        return f;
    }

    // --- Renderers ---
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            if (!isSelected && value instanceof String status) {
                switch (status) {
                    case "ADDED",    "ADD"    -> setForeground(new Color(0, 150, 0));
                    case "MODIFIED", "MODIFY" -> setForeground(new Color(0, 100, 200));
                    case "REMOVED",  "DELETE" -> setForeground(new Color(200, 0, 0));
                    case "RENAMED",  "RENAME",
                         "COPY"              -> setForeground(new Color(150, 100, 0));
                    default                   -> setForeground(table.getForeground());
                }
            }
            return this;
        }
    }
}
