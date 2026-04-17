package com.az.gitember.dialog;

import com.az.gitember.data.WorktreeInfo;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog for managing Git worktrees.
 * Shows a table of all linked worktrees and provides Add / Remove / Open / Prune actions.
 */
public class WorktreesDialog extends JDialog {

    private static final Logger log = Logger.getLogger(WorktreesDialog.class.getName());

    private final WorktreeTableModel model = new WorktreeTableModel();
    private final JTable table;
    private final JButton addBtn    = new JButton("Add…");
    private final JButton removeBtn = new JButton("Remove");
    private final JButton openBtn   = new JButton("Open");
    private final JButton pruneBtn  = new JButton("Prune");
    private final JLabel  statusLbl = new JLabel(" ");

    /** Set to the worktree the user wants to open; {@code null} if none chosen. */
    private WorktreeInfo selectedToOpen;

    /** Latest worktree snapshot – passed to AddWorktreeDialog to derive next sequence number. */
    private List<WorktreeInfo> currentWorktrees = new ArrayList<>();

    public WorktreesDialog(Frame parent) {
        super(parent, "Worktrees", true);
        setSize(700, 350);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(500, 250));

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        JScrollPane scroll = new JScrollPane(table);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(openBtn);
       // btnPanel.add(pruneBtn);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
       // statusPanel.add(statusLbl, BorderLayout.WEST);

        JPanel south = new JPanel(new BorderLayout());
        south.add(btnPanel, BorderLayout.NORTH);
        south.add(statusPanel, BorderLayout.SOUTH);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        closePanel.add(closeBtn);
        south.add(closePanel, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(4, 4));
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> onAdd());
        removeBtn.addActionListener(e -> onRemove());
        openBtn.addActionListener(e -> onOpen());
        pruneBtn.addActionListener(e -> onPrune());

        Util.bindEscapeToDispose(this);
        updateButtons();
        loadWorktrees();
    }

    private void loadWorktrees() {
        statusLbl.setText("Loading…");
        SwingWorker<List<WorktreeInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<WorktreeInfo> doInBackground() throws Exception {
                return Context.getGitRepoService().listWorktrees();
            }
            @Override
            protected void done() {
                try {
                    currentWorktrees = get();
                    model.setData(currentWorktrees);
                    statusLbl.setText(model.getRowCount() + " worktree(s)");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to list worktrees", ex);
                    statusLbl.setText("Error: " + ex.getMessage());
                }
                updateButtons();
            }
        };
        worker.execute();
    }

    private void onAdd() {
        AddWorktreeDialog dlg = new AddWorktreeDialog(this, currentWorktrees);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        String path       = dlg.getPath();
        String branchName = dlg.getBranch();
        boolean newBranch = dlg.isNewBranch();

        statusLbl.setText("Adding worktree…");
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().addWorktree(path, branchName, newBranch);
                return null;
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    get();
                    loadWorktrees();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Add worktree failed", ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLbl.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(WorktreesDialog.this,
                            "Cannot add worktree:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onRemove() {
        WorktreeInfo wt = getSelected();
        if (wt == null) return;
        if (wt.isMain()) {
            JOptionPane.showMessageDialog(this,
                    "The main worktree cannot be removed.",
                    "Remove Worktree", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Remove worktree at:\n" + wt.getPath() + "\n\nForce remove if there are local changes?",
                "Remove Worktree",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
        boolean force = (choice == JOptionPane.YES_OPTION);

        statusLbl.setText("Removing worktree…");
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().removeWorktree(wt.getPath(), force);
                return null;
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    get();
                    loadWorktrees();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Remove worktree failed", ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLbl.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(WorktreesDialog.this,
                            "Cannot remove worktree:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onOpen() {
        WorktreeInfo wt = getSelected();
        if (wt == null) return;
        selectedToOpen = wt;
        dispose();
    }

    private void onPrune() {
        statusLbl.setText("Pruning…");
        setButtonsEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().pruneWorktrees();
                return null;
            }
            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    get();
                    loadWorktrees();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Prune worktrees failed", ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLbl.setText("Error: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private WorktreeInfo getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return model.getRow(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        WorktreeInfo sel = getSelected();
        boolean hasSelection = sel != null;
        boolean isMain       = sel != null && sel.isMain();
        removeBtn.setEnabled(hasSelection && !isMain);
        openBtn.setEnabled(hasSelection);
    }

    private void setButtonsEnabled(boolean enabled) {
        addBtn.setEnabled(enabled);
        removeBtn.setEnabled(enabled);
        openBtn.setEnabled(enabled);
        pruneBtn.setEnabled(enabled);
    }

    /** The worktree the user chose to open, or {@code null}. */
    public WorktreeInfo getSelectedToOpen() {
        return selectedToOpen;
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private static class WorktreeTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = { "Path", "Branch", "HEAD", "Status" };

        private List<WorktreeInfo> data = new ArrayList<>();

        void setData(List<WorktreeInfo> list) {
            this.data = new ArrayList<>(list);
            fireTableDataChanged();
        }

        WorktreeInfo getRow(int modelRow) {
            return data.get(modelRow);
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            WorktreeInfo wt = data.get(row);
            return switch (col) {
                case 0 -> wt.getPath();
                case 1 -> wt.getBranch() != null ? wt.getBranch() : "(detached)";
                case 2 -> wt.getShortHead();
                case 3 -> wt.isMain()     ? "main"
                        : wt.isLocked()   ? "locked"
                        : wt.isPrunable() ? "prunable"
                        :                   "";
                default -> "";
            };
        }
    }

    // ── Inner dialog for adding a worktree ────────────────────────────────────

    private static class AddWorktreeDialog extends JDialog {

        private final JTextField pathField;
        private final JTextField branchField;
        private final JCheckBox  newBranchCb = new JCheckBox("Create new branch", true);
        private boolean confirmed;

        AddWorktreeDialog(Dialog parent, List<WorktreeInfo> existingWorktrees) {
            super(parent, "Add Worktree", true);
            setSize(540, 210);
            setLocationRelativeTo(parent);
            setResizable(false);

            int    nextN   = nextFeatureNumber(existingWorktrees);
            String paddedN = String.format("%03d", nextN);

            pathField   = new JTextField(suggestedPath(paddedN), 34);
            branchField = new JTextField("feature/" + paddedN, 20);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets  = new Insets(4, 4, 4, 4);
            gbc.fill    = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
            form.add(new JLabel("Path:"), gbc);
            JPanel pathRow = new JPanel(new BorderLayout(4, 0));
            pathRow.add(pathField, BorderLayout.CENTER);
            JButton browseBtn = new JButton("…");
            browseBtn.addActionListener(e -> browseFolder());
            pathRow.add(browseBtn, BorderLayout.EAST);
            gbc.gridx = 1; gbc.weightx = 1;
            form.add(pathRow, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            form.add(new JLabel("Branch:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            form.add(branchField, gbc);

            gbc.gridx = 1; gbc.gridy = 2;
            form.add(newBranchCb, gbc);

            JButton addBtn    = new JButton("Add");
            JButton cancelBtn = new JButton("Cancel");
            addBtn.addActionListener(e -> onConfirm());
            cancelBtn.addActionListener(e -> dispose());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.add(addBtn);
            btns.add(cancelBtn);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(form, BorderLayout.CENTER);
            getContentPane().add(btns, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(addBtn);
            Util.bindEscapeToDispose(this);
        }

        /**
         * Scans existing worktree branches for {@code feature/NNN} or {@code feature_NNN},
         * returns max N + 1. Returns 1 if none found.
         */
        private static int nextFeatureNumber(List<WorktreeInfo> worktrees) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("feature[/_](\\d+)");
            int max = 0;
            for (WorktreeInfo wt : worktrees) {
                String branch = wt.getBranch();
                if (branch == null) continue;
                java.util.regex.Matcher m = p.matcher(branch);
                if (m.find()) {
                    try { max = Math.max(max, Integer.parseInt(m.group(1))); }
                    catch (NumberFormatException ignored) {}
                }
            }
            return max + 1;
        }

        /**
         * Suggested path: sibling of the repo folder, named {@code <repoName>_feature_<nnn>}.
         * Example: repo at {@code /home/user/myproject} → {@code /home/user/myproject_feature_001}.
         */
        private static String suggestedPath(String paddedN) {
            String repoPath = Context.getProjectFolder();
            if (repoPath == null || repoPath.isBlank()) {
                return "";
            } else {
                Path repoDir = Paths.get(repoPath);
                Path parent  = repoDir.getParent();
                if (parent == null)  {
                    return "";
                } else {
                    String repoName = repoDir.getFileName().toString();
                    return parent.resolve(repoName + "_feature_" + paddedN).toString();
                }
            }
        }

        private void browseFolder() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Worktree Folder");
            String current = pathField.getText().trim();
            if (StringUtils.isNotBlank(current)) {
                java.io.File f = new java.io.File(current).getParentFile();
                if (f != null && f.exists()) {
                    chooser.setCurrentDirectory(f);
                }
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }

        private void onConfirm() {
            if (pathField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Path is required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (branchField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Branch name is required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        }

        boolean isConfirmed()  { return confirmed; }
        String  getPath()      { return pathField.getText().trim(); }
        String  getBranch()    { return branchField.getText().trim(); }
        boolean isNewBranch()  { return newBranchCb.isSelected(); }
    }
}
