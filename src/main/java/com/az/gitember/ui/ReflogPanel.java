package com.az.gitember.ui;

import com.az.gitember.data.ScmReflogEntry;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lists Git reflog entries and offers recover actions for deleted branches,
 * hard resets, deleted commits, and dropped stashes.
 */
public class ReflogPanel extends JPanel {

    private static final Logger log = Logger.getLogger(ReflogPanel.class.getName());

    private final StatusBar statusBar;
    private final JTable table;
    private final ReflogTableModel tableModel;
    private final JButton refreshBtn;
    private final JTextField searchField;

    public ReflogPanel(StatusBar statusBar) {
        this.statusBar = statusBar;
        setLayout(new BorderLayout());

        refreshBtn = Util.createButton("Refresh", "Reload reflog", FontAwesomeSolid.SYNC);
        refreshBtn.addActionListener(e -> reload());

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.setMinimumSize(new Dimension(100, 25));
        searchField.setMaximumSize(new Dimension(150, 25));
        searchField.putClientProperty("JTextField.placeholderText", "Filter reflog...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        tableModel = new ReflogTableModel();
        table = new JTable(tableModel);
        table.setName("reflogTable");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(420);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public JButton getRefreshBtn() {
        return refreshBtn;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public void reload() {
        statusBar.setStatus("Loading reflog...");
        new SwingWorker<List<ScmReflogEntry>, Void>() {
            @Override
            protected List<ScmReflogEntry> doInBackground() {
                return Context.getGitRepoService().getReflogEntries();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setItems(get());
                    applyFilter();
                    statusBar.setStatus("Reflog: " + tableModel.getRowCount() + " entries");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Cannot load reflog", ex);
                    statusBar.setStatus("Cannot load reflog: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        tableModel.applyFilter(searchField.getText());
    }

    private void showContextMenu(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) {
            return;
        }
        table.setRowSelectionInterval(row, row);
        ScmReflogEntry entry = tableModel.getItemAt(row);
        if (entry == null) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        JMenuItem recoverBranch = new JMenuItem("Recover deleted branch...");
        recoverBranch.addActionListener(ev -> recoverDeletedBranch(entry));
        menu.add(recoverBranch);

        JMenuItem recoverReset = new JMenuItem("Recover hard reset...");
        recoverReset.addActionListener(ev -> recoverHardReset(entry));
        menu.add(recoverReset);

        JMenuItem recoverCommit = new JMenuItem("Recover deleted commit...");
        recoverCommit.addActionListener(ev -> recoverDeletedCommit(entry));
        menu.add(recoverCommit);

        JMenuItem recoverStash = new JMenuItem("Recover stash...");
        recoverStash.addActionListener(ev -> recoverStash(entry));
        menu.add(recoverStash);

        menu.addSeparator();
        JMenuItem copySha = new JMenuItem("Copy commit SHA");
        copySha.addActionListener(ev -> {
            String sha = entry.recoveryCommitId();
            if (sha != null && !sha.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(sha), null);
                statusBar.setStatus("Copied " + entry.getShortNewId());
            }
        });
        menu.add(copySha);
        menu.show(table, e.getX(), e.getY());
    }

    private void recoverDeletedBranch(ScmReflogEntry entry) {
        String suggested = entry.suggestedBranchName();
        String name = JOptionPane.showInputDialog(this,
                "Recreate local branch at " + shortOrEmpty(entry.recoveryCommitId()) + ":",
                suggested);
        if (name != null && !name.isBlank()) {
            runRecover("Recover deleted branch", () -> {
                Context.getGitRepoService().recoverDeletedBranch(name.trim(), entry.recoveryCommitId());
                Context.updateBranches();
                return "Recovered branch '" + name.trim() + "'";
            });
        }
    }

    private void recoverHardReset(ScmReflogEntry entry) {
        String sha = entry.recoveryCommitId();
        int ok = JOptionPane.showConfirmDialog(this,
                "Hard-reset HEAD and the working tree to " + shortOrEmpty(sha) + "?\n"
                        + "Uncommitted changes will be lost.",
                "Recover hard reset",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            runRecover("Recover hard reset", () -> {
                Context.getGitRepoService().recoverHardReset(sha, null);
                Context.updateWorkingBranch();
                Context.updateStatus(null, true);
                return "Reset to " + shortOrEmpty(sha);
            });
        }
    }

    private void recoverDeletedCommit(ScmReflogEntry entry) {
        String sha = entry.getNewId();
        String suggested = "recovered-" + (sha != null && sha.length() >= 7 ? sha.substring(0, 7) : "commit");
        String name = JOptionPane.showInputDialog(this,
                "Create a branch pointing at commit " + shortOrEmpty(sha) + ":",
                suggested);
        if (name != null && !name.isBlank()) {
            runRecover("Recover deleted commit", () -> {
                Context.getGitRepoService().recoverDeletedCommit(name.trim(), sha);
                Context.updateBranches();
                return "Created branch '" + name.trim() + "' at " + shortOrEmpty(sha);
            });
        }
    }

    private void recoverStash(ScmReflogEntry entry) {
        String sha = entry.getNewId();
        int ok = JOptionPane.showConfirmDialog(this,
                "Restore this commit onto the stash list?\n" + shortOrEmpty(sha),
                "Recover stash",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            runRecover("Recover stash", () -> {
                Context.getGitRepoService().recoverStash(sha);
                Context.updateStash();
                return "Stash restored at " + shortOrEmpty(sha);
            });
        }
    }

    private void runRecover(String title, RecoverAction action) {
        statusBar.setStatus(title + "...");
        statusBar.showProgress(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return action.run();
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    String msg = get();
                    statusBar.setStatus(msg);
                    reload();
                } catch (Exception ex) {
                    log.log(Level.WARNING, title + " failed", ex);
                    statusBar.setStatus(title + " failed");
                    JOptionPane.showMessageDialog(ReflogPanel.this,
                            title + " failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String shortOrEmpty(String sha) {
        String shortId = "";
        if (sha != null && sha.length() >= 7) {
            shortId = sha.substring(0, 7);
        }
        return shortId;
    }

    @FunctionalInterface
    private interface RecoverAction {
        String run() throws Exception;
    }

    private static class ReflogTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Selector", "Action", "Commit", "When", "Message"};
        private final List<ScmReflogEntry> all = new ArrayList<>();
        private final List<ScmReflogEntry> shown = new ArrayList<>();
        private String filter = "";

        void setItems(List<ScmReflogEntry> items) {
            all.clear();
            if (items != null) {
                all.addAll(items);
            }
            applyFilter(filter);
        }

        void applyFilter(String text) {
            filter = text != null ? text.trim().toLowerCase(Locale.ROOT) : "";
            shown.clear();
            for (ScmReflogEntry e : all) {
                if (filter.isEmpty() || matches(e, filter)) {
                    shown.add(e);
                }
            }
            fireTableDataChanged();
        }

        ScmReflogEntry getItemAt(int row) {
            ScmReflogEntry item = null;
            if (row >= 0 && row < shown.size()) {
                item = shown.get(row);
            }
            return item;
        }

        private static boolean matches(ScmReflogEntry e, String f) {
            return contains(e.getSelector(), f)
                    || contains(e.getKind(), f)
                    || contains(e.getComment(), f)
                    || contains(e.getNewId(), f)
                    || contains(e.getOldId(), f)
                    || contains(e.getWhoName(), f);
        }

        private static boolean contains(String value, String f) {
            return value != null && value.toLowerCase(Locale.ROOT).contains(f);
        }

        @Override
        public int getRowCount() {
            return shown.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScmReflogEntry e = shown.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.getSelector();
                case 1 -> e.getKind();
                case 2 -> e.getShortNewId();
                case 3 -> e.getWhen() != null ? GitemberUtil.formatDate(e.getWhen()) : "";
                case 4 -> e.getComment();
                default -> "";
            };
        }
    }
}
