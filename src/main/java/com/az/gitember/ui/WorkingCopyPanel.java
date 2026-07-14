package com.az.gitember.ui;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Working copy panel with stage/unstage checkboxes, colored status,
 * context menu, and toolbar actions.
 */
public class WorkingCopyPanel extends WorkingCopyOps {

    private static final Logger log = Logger.getLogger(WorkingCopyPanel.class.getName());

    private final JTable table;
    private final WorkingCopyTableModel tableModel;
    private WorkingCopyContextMenu contextMenu;

    public WorkingCopyPanel(StatusBar statusBar) {
        super(statusBar);

        contextMenu = new WorkingCopyContextMenu(
                this,
                statusBar,
                action -> action.execute(Context.getGitRepoService()),
                Context::getProjectFolder,
                () -> Context.updateStatus(null, true));

        setLayout(new BorderLayout());

        tableModel = new WorkingCopyTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        TableColumn colorCol = table.getColumnModel().getColumn(0);
        colorCol.setPreferredWidth(10);
        colorCol.setMaxWidth(10);
        colorCol.setMinWidth(10);
        colorCol.setCellRenderer(new ColorStatusRenderer());

        TableColumn checkCol = table.getColumnModel().getColumn(1);
        checkCol.setPreferredWidth(30);
        checkCol.setMaxWidth(30);
        checkCol.setMinWidth(30);

        TableColumn statusCol = table.getColumnModel().getColumn(2);
        statusCol.setPreferredWidth(90);
        statusCol.setMaxWidth(120);
        statusCol.setCellRenderer(new StatusTextRenderer());

        table.getColumnModel().getColumn(3).setPreferredWidth(500);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 1) {
                    toggleStage(row);
                } else if (row >= 0 && e.getClickCount() == 2 && col >= 2) {
                    handleDoubleClick(row);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }
        });

        searchField.putClientProperty("JTextField.placeholderText", "Filter files...");

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        statusBar.setStatus("Refreshing working copy...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Context.updateStatus(null, true);
                return null;
            }
            @Override
            protected void done() {
                statusBar.setStatus("Working copy refreshed");
            }
        }.execute();
    }

    public void setItems(List<ScmItem> items) {
        tableModel.setItems(items);
        applyFilter();
        updateButtonStates();
    }

    public boolean hasStagedItems() {
        return tableModel.getAllItems().stream().anyMatch(ScmItem::isStaged);
    }

    @Override
    public void updateButtonStates() {
        List<ScmItem> items = tableModel.getAllItems();
        stageAllBtn.setEnabled(items.stream().anyMatch(i -> !i.isStaged()));
        unstageAllBtn.setEnabled(items.stream().anyMatch(ScmItem::isStaged));
    }

    @Override
    protected void applyFilter() {
        tableModel.applyFilter(searchField.getText().trim().toLowerCase());
    }

    private void toggleStage(int row) {
        ScmItem item = tableModel.getItemAt(row);
        if (item == null) return;
        String status = item.getAttribute().getStatus();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (ScmItem.isStaged(status)) {
                    Context.getGitRepoService().unstageItem(item);
                } else {
                    Context.getGitRepoService().stageItem(item);
                }
                return null;
            }
            @Override
            protected void done() {
                try { get(); }
                catch (Exception ex) {
                    log.log(Level.WARNING, "Stage/unstage failed", ex);
                    statusBar.setStatus("Error: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        }.execute();
    }

    private void handleDoubleClick(int row) {
        ScmItem item = tableModel.getItemAt(row);
        if (item == null) return;
        String status = item.getAttribute().getStatus();
        if (ScmItem.Status.MISSED.equals(status) || ScmItem.Status.REMOVED.equals(status)) return;
        if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.CHANGED.equals(status)) {
            contextMenu.showDiffWithRepo(item);
        } else {
            contextMenu.openFile(item);
        }
    }

    @Override
    protected void stageAll() {
        List<ScmItem> toStage = tableModel.getAllItems().stream().filter(i -> !i.isStaged()).toList();
        if (toStage.isEmpty()) return;
        statusBar.setStatus("Staging all...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : toStage) Context.getGitRepoService().stageItem(item);
                return null;
            }
            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("All files staged");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Stage all failed", ex);
                    statusBar.setStatus("Error staging: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        }.execute();
    }

    @Override
    protected void unstageAll() {
        List<ScmItem> toUnstage = tableModel.getAllItems().stream().filter(ScmItem::isStaged).toList();
        if (toUnstage.isEmpty()) return;
        statusBar.setStatus("Unstaging all...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : toUnstage) Context.getGitRepoService().unstageItem(item);
                return null;
            }
            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("All files unstaged");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Unstage all failed", ex);
                    statusBar.setStatus("Error unstaging: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        }.execute();
    }

    private void showContextMenu(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int[] selectedRows = table.getSelectedRows();

        if (row >= 0 && Arrays.stream(selectedRows).noneMatch(r -> r == row)) {
            table.setRowSelectionInterval(row, row);
            selectedRows = new int[]{row};
        }
        if (selectedRows.length == 0) return;

        List<ScmItem> selected = new ArrayList<>();
        for (int r : selectedRows) {
            ScmItem item = tableModel.getItemAt(r);
            if (item != null) selected.add(item);
        }
        if (selected.isEmpty()) return;

        contextMenu.show(selected, table, e.getX(), e.getY());
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private static class WorkingCopyTableModel extends AbstractTableModel {

        private List<ScmItem> allItems = new ArrayList<>();
        private List<ScmItem> filteredItems = new ArrayList<>();

        private static final String[] COLUMNS = {"", "", "Status", "File"};

        void setItems(List<ScmItem> items) {
            allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
            filteredItems = new ArrayList<>(allItems);
            fireTableDataChanged();
        }

        void applyFilter(String filter) {
            if (filter == null || filter.isEmpty()) {
                filteredItems = new ArrayList<>(allItems);
            } else {
                filteredItems = allItems.stream()
                        .filter(i -> i.getShortName().toLowerCase().contains(filter))
                        .collect(java.util.stream.Collectors.toList());
            }
            fireTableDataChanged();
        }

        List<ScmItem> getAllItems() { return new ArrayList<>(allItems); }

        ScmItem getItemAt(int row) {
            return (row >= 0 && row < filteredItems.size()) ? filteredItems.get(row) : null;
        }

        @Override public int getRowCount()    { return filteredItems.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 1 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) { return false; }

        @Override
        public Object getValueAt(int row, int col) {
            ScmItem item = filteredItems.get(row);
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            return switch (col) {
                case 0 -> status;
                case 1 -> item.isStaged();
                case 2 -> {
                    if (ScmItem.Status.LFS.equals(status) && item.getAttribute().getSubstatus() != null) {
                        yield "LFS:" + item.getAttribute().getSubstatus();
                    }
                    yield status;
                }
                case 3 -> item.getShortName();
                default -> "";
            };
        }
    }

    // ── Renderers ─────────────────────────────────────────────────────────────

    private static class ColorStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, col);
            String status = value != null ? value.toString() : "";
            label.setOpaque(true);
            if (!isSelected) label.setBackground(SyntaxStyleUtil.scmItemColor(status));
            return label;
        }
    }

    private static class StatusTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = value != null ? value.toString() : "";
            if (!isSelected) label.setForeground(SyntaxStyleUtil.scmItemColor(status).darker());
            return label;
        }
    }


}
