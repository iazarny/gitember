package com.az.gitember.ui;

import com.az.gitember.service.I18n;
import com.az.gitember.data.Submodule;
import com.az.gitember.handler.AddSubmoduleHandler;
import com.az.gitember.handler.CommitSubmoduleHandler;
import com.az.gitember.handler.InitSubmodulesHandler;
import com.az.gitember.handler.RemoveSubmoduleHandler;
import com.az.gitember.handler.UpdateSubmodulesHandler;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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

        JButton addBtn = Util.createButton(I18n.t("worktreesDialog.add"), I18n.t("cloneRepositoryRegisterAsSubmodule"), FontAwesomeSolid.FOLDER_OPEN);
        addBtn.addActionListener(e -> AddSubmoduleHandler.showAndExecute(this));

        JButton initBtn = Util.createButton(I18n.t("init"),I18n.t("runGitSubmoduleInitAll"), FontAwesomeSolid.STAR);
        initBtn.addActionListener(e -> new InitSubmodulesHandler(this).execute());

        //JButton updateBtn = Util.createButton("Update All", "Run git submodule init + update for all submodules");
        //updateBtn.addActionListener(e -> new UpdateSubmodulesHandler(this).execute());

        JButton recursiveBtn = Util.createButton(I18n.t("update"),I18n.t("recursiveUpdateRepositoryNestedSubmodules"));
        recursiveBtn.addActionListener(e -> new UpdateSubmodulesHandler(this, true).execute());

        JButton syncBtn = Util.createButton(I18n.t("syncUrls"),I18n.t("runGitSubmoduleSyncUpdate"));
        syncBtn.addActionListener(e -> syncSubmodules());

        JButton refreshBtn = Util.createButton(I18n.t("refresh"),I18n.t("reReadSubmoduleStatusFrom"), FontAwesomeSolid.SYNC);
        refreshBtn.addActionListener(e -> Context.updateSubmodules());

        toolbar.add(addBtn);
        toolbar.add(initBtn);
        //toolbar.add(updateBtn);
        toolbar.add(recursiveBtn);
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

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showRowMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showRowMenu(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedSubmodule();
                }
            }
        });

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

    private void showRowMenu(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
            Submodule selected = tableModel.getSubmodule(row);
            if (selected != null) {
                JPopupMenu menu = createSubmoduleMenu(this, selected);
                menu.show(table, e.getX(), e.getY());
            }
        }
    }

    private void openSelectedSubmodule() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            openSubmodule(this, tableModel.getSubmodule(row));
        }
    }

    public static JPopupMenu createSubmoduleMenu(Component parent, Submodule selected) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem initItem = new JMenuItem(I18n.t("initialize"));
        initItem.addActionListener(ev -> new InitSubmodulesHandler(parent, selected.getPath()).execute());
        menu.add(initItem);

        JMenuItem updateItem = new JMenuItem(I18n.t("update"));
        updateItem.addActionListener(ev ->
                new UpdateSubmodulesHandler(parent, false, selected.getPath()).execute());
        menu.add(updateItem);

        JMenuItem recursiveItem = new JMenuItem(I18n.t("recursiveUpdate"));
        recursiveItem.addActionListener(ev -> new UpdateSubmodulesHandler(parent, true).execute());
        menu.add(recursiveItem);

        menu.addSeparator();

        JMenuItem commitItem = new JMenuItem(I18n.t("submodulePanel.commitSubmoduleChange"));
        commitItem.addActionListener(ev -> CommitSubmoduleHandler.showAndExecute(parent, selected));
        menu.add(commitItem);

        JMenuItem diffItem = new JMenuItem(I18n.t("showDiff"));
        diffItem.addActionListener(ev -> showSubmoduleDiff(parent, selected));
        menu.add(diffItem);

        JMenuItem statusItem = new JMenuItem(I18n.t("showStatus"));
        statusItem.addActionListener(ev -> showSubmoduleStatus(parent, selected));
        menu.add(statusItem);

        menu.addSeparator();

        JMenuItem openItem = new JMenuItem(I18n.t("open"));
        openItem.addActionListener(ev -> openSubmodule(parent, selected));
        menu.add(openItem);

        JMenuItem removeItem = new JMenuItem(I18n.t("branchContextMenuFactory.remove"));
        removeItem.addActionListener(ev -> RemoveSubmoduleHandler.showAndExecute(parent, selected));
        menu.add(removeItem);

        return menu;
    }

    public static void showSubmoduleDiff(Component parent, Submodule selected) {
        try {
            String diff = Context.getGitRepoService().getSubmoduleDiff(selected.getPath());
            JTextArea area = new JTextArea(diff, 14, 72);
            area.setEditable(false);
            area.setFont(SyntaxStyleUtil.monoFont());
            JOptionPane.showMessageDialog(parent, new JScrollPane(area),
                    I18n.t("submoduleDiff", selected.getPath()), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    I18n.t("cannotComputeSubmoduleDiff", ex.getMessage()),
                    I18n.t("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showSubmoduleStatus(Component parent, Submodule selected) {
        String text = I18n.t("pathNameUrlStatusIndex", selected.getPath(), selected.getName(), selected.getUrl(), selected.getStatus(), selected.getIndexSha(), selected.getHeadSha());
        JTextArea area = new JTextArea(text, 8, 56);
        area.setEditable(false);
        JOptionPane.showMessageDialog(parent, new JScrollPane(area),
                I18n.t("submoduleStatus", selected.getPath()), JOptionPane.INFORMATION_MESSAGE);
    }

    public static void openSubmodule(Component parent, Submodule selected) {
        if (selected != null && selected.getStatus() != Submodule.Status.UNINITIALIZED
                && Context.getGitRepoService().getRepository() != null) {
            File workTree = Context.getGitRepoService().getRepository().getWorkTree();
            File subDir = new File(workTree, selected.getPath());
            File gitMarker = new File(subDir, ".git");
            if (subDir.isDirectory() && gitMarker.exists()) {
                new com.az.gitember.ui.mainframe.ReopenRepoHandler(
                        com.az.gitember.ui.MainFrame.getInstance())
                        .accept(new com.az.gitember.data.Project(subDir.getAbsolutePath(), new java.util.Date()));
            } else {
                JOptionPane.showMessageDialog(parent,
                        I18n.t("submoduleWorkingTreeNotAvailable"),
                        I18n.t("openSubmodule"), JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parent,
                    I18n.t("initializeUpdateSubmoduleBeforeOpening"),
                    I18n.t("openSubmodule"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void syncSubmodules() {
        statusBar.setStatus(I18n.t("syncingSubmoduleUrls"));
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
                    statusBar.setStatus(I18n.t("submoduleUrlsSynced"));
                    Context.updateSubmodules();
                } catch (Exception ex) {
                    statusBar.setStatus(I18n.t("syncFailed", ex.getMessage()));
                    JOptionPane.showMessageDialog(SubmodulePanel.this,
                            I18n.t("submodulePanel.syncFailed", ex.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private static class SubmoduleTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {I18n.t("worktreesDialog.path"), I18n.t("status"), I18n.t("indexSha"), I18n.t("headSha"), "URL"};
        private List<Submodule> rows = Collections.emptyList();

        void setSubmodules(List<Submodule> list) {
            this.rows = list != null ? list : Collections.emptyList();
            fireTableDataChanged();
        }

        Submodule getSubmodule(int row) {
            Submodule item = null;
            if (row >= 0 && row < rows.size()) {
                item = rows.get(row);
            }
            return item;
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
                case UP_TO_DATE    -> I18n.t("upToDate");
                case MODIFIED      -> I18n.t("submodulePanel.modified");
                case UNINITIALIZED -> I18n.t("uninitialized");
                case MISSING       -> I18n.t("missing");
            };
        }
    }
}
