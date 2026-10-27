package com.az.gitember.dialog;

import com.az.gitember.service.I18n;
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
    private final JTable filesTable;
    private final JButton fetchBtn;
    private final JButton uploadBtn;

    public LfsManageDialog(Frame owner) {
        super(owner, I18n.t("manageGitLfs"), true);
        setSize(600, 520);
        setLocationRelativeTo(owner);
        setResizable(true);

        // ── Status header ──────────────────────────────────────────────────
        boolean lfsEnabled = Context.isLfsRepo();

        statusLabel = new JLabel();
        enableBtn = new JButton(I18n.t("enableLfs"));
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
        addBtn.setToolTipText(I18n.t("trackNewFilePattern"));
        addBtn.addActionListener(e -> doAddPattern());

        JButton removeBtn = new JButton("−");
        removeBtn.setToolTipText(I18n.t("untrackSelectedPattern"));
        removeBtn.addActionListener(e -> doRemovePattern());

        JPanel patternBtnPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        patternBtnPanel.add(addBtn);
        patternBtnPanel.add(removeBtn);

        JPanel patternsPanel = new JPanel(new BorderLayout(4, 0));
        patternsPanel.setBorder(new TitledBorder(I18n.t("trackedPatternsGitattributes")));
        patternsPanel.add(new JScrollPane(patternList), BorderLayout.CENTER);
        patternsPanel.add(patternBtnPanel, BorderLayout.EAST);

        // ── LFS files table ───────────────────────────────────────────────
        filesTableModel = new LfsFilesTableModel();
        filesTable = new JTable(filesTableModel);
        filesTable.setRowHeight(22);
        filesTable.setShowGrid(false);
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        filesTable.getColumnModel().getColumn(1).setMaxWidth(120);
        JScrollPane filesScroll = new JScrollPane(filesTable);
        filesScroll.setPreferredSize(new Dimension(400, 180));

        JPanel filesPanel = new JPanel(new BorderLayout());
        filesPanel.setBorder(new TitledBorder(I18n.t("lfsFilesInHead")));
        filesPanel.add(filesScroll, BorderLayout.CENTER);
        loadLfsFiles();

        // ── Button row ────────────────────────────────────────────────────
        fetchBtn = new JButton(I18n.t("fetchLfsObjects"));
        fetchBtn.setToolTipText(I18n.t("downloadLfsFileContentFrom"));
        fetchBtn.setEnabled(lfsEnabled);
        fetchBtn.addActionListener(e -> doFetchLfsObjects());

        uploadBtn = new JButton(I18n.t("uploadLfsObjects"));
        uploadBtn.setToolTipText(I18n.t("uploadLocalLfsObjectsRemote"));
        uploadBtn.setEnabled(lfsEnabled);
        uploadBtn.addActionListener(e ->
                new com.az.gitember.handler.LfsUploadHandler(this).execute());

        JButton lockBtn = new JButton(I18n.t("lock"));
        lockBtn.setToolTipText(I18n.t("lockSelectedLfsFileRemote"));
        lockBtn.addActionListener(e -> lockSelected(true, false));

        JButton unlockBtn = new JButton(I18n.t("unlock"));
        unlockBtn.setToolTipText(I18n.t("unlockSelectedLfsFile"));
        unlockBtn.addActionListener(e -> lockSelected(false, false));

        JButton closeBtn = new JButton(I18n.t("close"));
        closeBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(closeBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnPanel.add(lockBtn);
        btnPanel.add(unlockBtn);
        btnPanel.add(fetchBtn);
        btnPanel.add(uploadBtn);
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
            statusLabel.setText(I18n.t("lfsStatusEnabled"));
            statusLabel.setForeground(new Color(0x006600));
        } else {
            statusLabel.setText(I18n.t("lfsStatusNotInitialized"));
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
            uploadBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                    I18n.t("lfsEnabledUseTrackFile"),
                    I18n.t("lfsEnabled"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot enable LFS", ex);
            JOptionPane.showMessageDialog(this,
                    I18n.t("cannotEnableLfs", ex.getMessage()),
                    I18n.t("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAddPattern() {
        String pattern = JOptionPane.showInputDialog(this,
                I18n.t("enterFilePatternTrackLfs"),
                I18n.t("trackPattern"), JOptionPane.PLAIN_MESSAGE);
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
                    I18n.t("cannotTrackPattern", ex.getMessage()),
                    I18n.t("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemovePattern() {
        String selected = patternList.getSelectedValue();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                I18n.t("stopTrackingWithLfs", selected),
                I18n.t("untrackPattern"), JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Context.getGitRepoService().lfsUntrack(selected);
            loadPatterns();
            loadLfsFiles();
            Context.updateStatus(null, true);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot untrack LFS pattern: " + selected, ex);
            JOptionPane.showMessageDialog(this,
                    I18n.t("cannotUntrackPattern", ex.getMessage()),
                    I18n.t("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lockSelected(boolean lock, boolean force) {
        int row = filesTable.getSelectedRow();
        ScmItem item = filesTableModel.getItem(row);
        if (item == null) {
            JOptionPane.showMessageDialog(this, I18n.t("selectLfsFileFirst"),
                    I18n.t("gitLfs"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            new com.az.gitember.handler.LfsLockHandler(this, item.getShortName(), lock, force)
                    .execute();
        }
    }

    private void doFetchLfsObjects() {
        fetchBtn.setEnabled(false);
        fetchBtn.setText(I18n.t("fetching"));
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
                fetchBtn.setText(I18n.t("fetchLfsObjects"));
                try {
                    get();
                    loadLfsFiles();
                    Context.updateStatus(null, true);
                    JOptionPane.showMessageDialog(LfsManageDialog.this,
                            I18n.t("lfsObjectsFetchedSuccessfully"),
                            I18n.t("fetchLfs"), JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, "Fetch LFS failed", cause);
                    JOptionPane.showMessageDialog(LfsManageDialog.this,
                            I18n.t("fetchFailed", cause.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Table model ────────────────────────────────────────────────────────

    private static class LfsFilesTableModel extends AbstractTableModel {
        private static final String[] COLS = {I18n.t("file"), I18n.t("state")};
        private List<ScmItem> items = new ArrayList<>();

        void setItems(List<ScmItem> list) {
            items = list != null ? new ArrayList<>(list) : new ArrayList<>();
            fireTableDataChanged();
        }

        ScmItem getItem(int row) {
            ScmItem item = null;
            if (row >= 0 && row < items.size()) {
                item = items.get(row);
            }
            return item;
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
                        ? "downloaded" : I18n.t("pointerOnly");
                default -> "";
            };
        }
    }
}
