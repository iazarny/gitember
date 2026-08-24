package com.az.gitember.dialog;

import com.az.gitember.data.WorktreeInfo;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
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
        WorktreeCreateDialog dlg = new WorktreeCreateDialog(this, currentWorktrees);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
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

}
