package com.az.gitember.ui;

import com.az.gitember.data.Submodule;
import com.az.gitember.handler.UpdateSubmodulesHandler;
import com.az.gitember.service.Context;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Panel that shows all Git submodules with their status,
 * and provides Update / Sync / Refresh actions.
 */
public class SubmodulePanel extends JPanel {

    private final StatusBar statusBar;
    private final JTable table;
    private final SubmoduleTableModel tableModel;

    public SubmodulePanel(StatusBar statusBar) {
        this.statusBar  = statusBar;
        this.tableModel = new SubmoduleTableModel();

        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ── Toolbar ───────────────────────────────────────────────────────────
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton updateBtn = new JButton("Update All");
        updateBtn.setToolTipText("Run git submodule init + update for all submodules");
        updateBtn.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new UpdateSubmodulesHandler(owner, statusBar).execute();
        });

        JButton syncBtn = new JButton("Sync URLs");
        syncBtn.setToolTipText("Run git submodule sync — update recorded remote URLs from .gitmodules");
        syncBtn.addActionListener(e -> syncSubmodules());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setToolTipText("Re-read submodule status from disk");
        refreshBtn.addActionListener(e -> Context.updateSubmodules());

        toolbar.add(updateBtn);
        toolbar.addSeparator();
        toolbar.add(syncBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(refreshBtn);

        // ── Table ─────────────────────────────────────────────────────────────
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180);  // Path
        table.getColumnModel().getColumn(1).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(2).setPreferredWidth(90);   // Index SHA
        table.getColumnModel().getColumn(3).setPreferredWidth(90);   // HEAD SHA
        table.getColumnModel().getColumn(4).setPreferredWidth(320);  // URL

        // Colour the Status column by status value
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /** Called from Context PROP_SUBMODULES listener to refresh the table. */
    public void setSubmodules(List<Submodule> submodules) {
        tableModel.setSubmodules(submodules);
    }

    /** Public entry point so the menu item in MainFrame can trigger sync without selecting the panel first. */
    public void syncSubmoduleUrls() {
        syncSubmodules();
    }

    private void syncSubmodules() {
        statusBar.setStatus("Syncing submodule URLs...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Context.getGitRepoService().syncSubmodules();
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Submodule URLs synced");
                    Context.updateSubmodules();
                } catch (Exception ex) {
                    statusBar.setStatus("Sync failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(SubmodulePanel.this,
                            "Sync failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private static class SubmoduleTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Path", "Status", "Index SHA", "HEAD SHA", "URL"};
        private List<Submodule> rows = Collections.emptyList();

        void setSubmodules(List<Submodule> list) {
            this.rows = list != null ? list : Collections.emptyList();
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }
        @Override public boolean isCellEditable(int row, int col) { return false; }

        @Override
        public Object getValueAt(int row, int col) {
            Submodule s = rows.get(row);
            return switch (col) {
                case 0 -> s.getPath();
                case 1 -> s.getStatus();
                case 2 -> s.getIndexSha();
                case 3 -> s.getHeadSha();
                case 4 -> s.getUrl();
                default -> "";
            };
        }
    }

    // ── Status cell renderer ──────────────────────────────────────────────────

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Submodule.Status status && !isSelected) {
                setForeground(switch (status) {
                    case UP_TO_DATE    -> new Color(0, 140, 0);
                    case MODIFIED      -> new Color(180, 100, 0);
                    case UNINITIALIZED -> Color.GRAY;
                    case MISSING       -> Color.RED;
                });
            } else if (!isSelected) {
                setForeground(table.getForeground());
            }
            setText(value instanceof Submodule.Status s ? statusLabel(s) : String.valueOf(value));
            return this;
        }

        private String statusLabel(Submodule.Status s) {
            return switch (s) {
                case UP_TO_DATE    -> "Up to date";
                case MODIFIED      -> "Modified";
                case UNINITIALIZED -> "Uninitialized";
                case MISSING       -> "Missing";
            };
        }
    }
}
