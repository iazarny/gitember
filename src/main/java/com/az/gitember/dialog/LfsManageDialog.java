package com.az.gitember.dialog;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;

import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog for managing Git LFS settings: enable LFS, track/untrack patterns,
 * view LFS files, and fetch LFS objects.
 */
public class LfsManageDialog extends JDialog {

    private static final Logger log = Logger.getLogger(LfsManageDialog.class.getName());

    private final JLabel statusLabel;
    private final JButton enableBtn;
    private final DefaultListModel<String> patternListModel;
    private final JList<String> patternList;
    private final LfsFilesTableModel filesTableModel;
    private final JButton fetchBtn;

    public LfsManageDialog(Frame owner) {
        super(owner, "Manage Git LFS", true);
        setSize(600, 520);
        setLocationRelativeTo(owner);
        setResizable(true);

        // ── Status header ──────────────────────────────────────────────────
        boolean lfsEnabled = Context.isLfsRepo();

        statusLabel = new JLabel();
        enableBtn = new JButton("Enable LFS");
        enableBtn.addActionListener(e -> doEnableLfs());
        updateStatusHeader(lfsEnabled);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        headerPanel.add(statusLabel);
        if (!lfsEnabled) {
            headerPanel.add(enableBtn);
        }

        // ── Tracked patterns ──────────────────────────────────────────────
        patternListModel = new DefaultListModel<>();
        patternList = new JList<>(patternListModel);
        patternList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patternList.setVisibleRowCount(6);
        loadPatterns();

        JButton addBtn = new JButton("+");
        addBtn.setToolTipText("Track a new file pattern");
        addBtn.addActionListener(e -> doAddPattern());

        JButton removeBtn = new JButton("−");
        removeBtn.setToolTipText("Untrack selected pattern");
        removeBtn.addActionListener(e -> doRemovePattern());

        JPanel patternBtnPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        patternBtnPanel.add(addBtn);
        patternBtnPanel.add(removeBtn);

        JPanel patternsPanel = new JPanel(new BorderLayout(4, 0));
        patternsPanel.setBorder(new TitledBorder("Tracked patterns (.gitattributes)"));
        patternsPanel.add(new JScrollPane(patternList), BorderLayout.CENTER);
        patternsPanel.add(patternBtnPanel, BorderLayout.EAST);

        // ── LFS files table ───────────────────────────────────────────────
        filesTableModel = new LfsFilesTableModel();
        JTable filesTable = new JTable(filesTableModel);
        filesTable.setRowHeight(22);
        filesTable.setShowGrid(false);
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        filesTable.getColumnModel().getColumn(1).setMaxWidth(120);
        JScrollPane filesScroll = new JScrollPane(filesTable);
        filesScroll.setPreferredSize(new Dimension(400, 180));

        JPanel filesPanel = new JPanel(new BorderLayout());
        filesPanel.setBorder(new TitledBorder("LFS files in HEAD"));
        filesPanel.add(filesScroll, BorderLayout.CENTER);
        loadLfsFiles();

        // ── Button row ────────────────────────────────────────────────────
        fetchBtn = new JButton("Fetch LFS Objects");
        fetchBtn.setToolTipText("Download LFS file content from the remote LFS server");
        fetchBtn.setEnabled(lfsEnabled);
        fetchBtn.addActionListener(e -> doFetchLfsObjects());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(closeBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnPanel.add(fetchBtn);
        btnPanel.add(closeBtn);

        // ── Main layout ───────────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        centerPanel.add(patternsPanel);
        centerPanel.add(filesPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(headerPanel,  BorderLayout.NORTH);
        getContentPane().add(centerPanel,  BorderLayout.CENTER);
        getContentPane().add(btnPanel,     BorderLayout.SOUTH);
        Util.bindEscapeToDispose(this);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void updateStatusHeader(boolean lfsEnabled) {
        if (lfsEnabled) {
            statusLabel.setText("LFS status: ENABLED");
            statusLabel.setForeground(new Color(0x006600));
        } else {
            statusLabel.setText("LFS status: not initialized");
            statusLabel.setForeground(new Color(0xCC3300));
        }
    }

    private void loadPatterns() {
        patternListModel.clear();
        try {
            List<String> patterns = Context.getGitRepoService().getLfsTrackedPatterns();
            patterns.forEach(patternListModel::addElement);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot load LFS patterns", ex);
        }
    }

    private void loadLfsFiles() {
        SwingWorker<List<ScmItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScmItem> doInBackground() {
                try {
                    return Context.getGitRepoService().getLfsFiles(org.eclipse.jgit.lib.Constants.HEAD);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Cannot load LFS files", ex);
                    return Collections.emptyList();
                }
            }
            @Override
            protected void done() {
                try {
                    filesTableModel.setItems(get());
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void doEnableLfs() {
        try {
            Context.getGitRepoService().enableLfsOnExistingRepo();
            Context.setLfsRepo(true);
            updateStatusHeader(true);
            enableBtn.setVisible(false);
            fetchBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                    "LFS enabled. Use '+' to track file patterns (e.g. *.psd, *.png).",
                    "LFS Enabled", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot enable LFS", ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot enable LFS:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAddPattern() {
        String pattern = JOptionPane.showInputDialog(this,
                "Enter file pattern to track with LFS:\n(e.g.  *.psd  or  *.mp4  or  assets/**)",
                "Track Pattern", JOptionPane.PLAIN_MESSAGE);
        if (pattern == null || pattern.isBlank()) return;
        pattern = pattern.trim();
        try {
            Context.getGitRepoService().lfsTrack(pattern);
            Context.setLfsRepo(true);
            loadPatterns();
            loadLfsFiles();
            Context.updateStatus(null, true);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot track LFS pattern: " + pattern, ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot track pattern:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemovePattern() {
        String selected = patternList.getSelectedValue();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Stop tracking '" + selected + "' with LFS?",
                "Untrack Pattern", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Context.getGitRepoService().lfsUntrack(selected);
            loadPatterns();
            loadLfsFiles();
            Context.updateStatus(null, true);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot untrack LFS pattern: " + selected, ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot untrack pattern:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doFetchLfsObjects() {
        fetchBtn.setEnabled(false);
        fetchBtn.setText("Fetching…");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().fetchLfsObjects(
                        com.az.gitember.data.RemoteRepoParameters.forCurrentRepo());
                return null;
            }
            @Override
            protected void done() {
                fetchBtn.setEnabled(true);
                fetchBtn.setText("Fetch LFS Objects");
                try {
                    get();
                    loadLfsFiles();
                    JOptionPane.showMessageDialog(LfsManageDialog.this,
                            "LFS objects fetched successfully.",
                            "Fetch LFS", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, "Fetch LFS failed", cause);
                    JOptionPane.showMessageDialog(LfsManageDialog.this,
                            "Fetch failed:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Table model ────────────────────────────────────────────────────────

    private static class LfsFilesTableModel extends AbstractTableModel {
        private static final String[] COLS = {"File", "State"};
        private List<ScmItem> items = new ArrayList<>();

        void setItems(List<ScmItem> list) {
            items = list != null ? new ArrayList<>(list) : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override public int getRowCount()     { return items.size(); }
        @Override public int getColumnCount()  { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ScmItem item = items.get(row);
            return switch (col) {
                case 0 -> item.getShortName();
                case 1 -> ScmItem.Status.LFS_FILE.equals(item.getAttribute().getSubstatus())
                        ? "downloaded" : "pointer only";
                default -> "";
            };
        }
    }
}
