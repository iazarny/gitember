package com.az.gitember.ui;

import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmItemAttribute;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Working copy panel with stage/unstage checkboxes, colored status,
 * context menu, and toolbar actions similar to the original gitember.
 */
public class WorkingCopyPanel extends JPanel {

    private static final Logger log = Logger.getLogger(WorkingCopyPanel.class.getName());

    private final JTable table;
    private final WorkingCopyTableModel tableModel;
    private final JButton stageAllBtn;
    private final JButton unstageAllBtn;
    private final JButton refreshBtn;
    private final JTextField searchField;
    private final StatusBar statusBar;

    // Colors matching original gitember
    private static final Color STAGED_COLOR = new Color(16, 234, 16);     // green - staged
    private static final Color UNSTAGED_COLOR = new Color(234, 16, 16);   // red - unstaged
    private static final Color CONFLICT_COLOR = new Color(211, 48, 255);  // purple
    private static final Color UNTRACKED_COLOR = new Color(128, 128, 128); // gray

    public WorkingCopyPanel(StatusBar statusBar) {
        this.statusBar = statusBar;
        setLayout(new BorderLayout());

        tableModel = new WorkingCopyTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Column setup
        // Col 0: Color indicator (narrow)
        TableColumn colorCol = table.getColumnModel().getColumn(0);
        colorCol.setPreferredWidth(10);
        colorCol.setMaxWidth(10);
        colorCol.setMinWidth(10);
        colorCol.setCellRenderer(new ColorStatusRenderer());

        // Col 1: Checkbox for stage/unstage
        TableColumn checkCol = table.getColumnModel().getColumn(1);
        checkCol.setPreferredWidth(30);
        checkCol.setMaxWidth(30);
        checkCol.setMinWidth(30);

        // Col 2: Status text
        TableColumn statusCol = table.getColumnModel().getColumn(2);
        statusCol.setPreferredWidth(90);
        statusCol.setMaxWidth(120);
        statusCol.setCellRenderer(new StatusTextRenderer());

        // Col 3: File name
        table.getColumnModel().getColumn(3).setPreferredWidth(500);

        // Handle checkbox clicks
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

        // Context menu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
            }
        });

        // Toolbar
        stageAllBtn = Util.createButton("Stage All", null, FontAwesomeSolid.PLUS);
        stageAllBtn.setEnabled(false);
        stageAllBtn.addActionListener(e -> stageAll());

        unstageAllBtn = Util.createButton("Unstage All", null, FontAwesomeSolid.MINUS);
        unstageAllBtn.setEnabled(false);
        unstageAllBtn.addActionListener(e -> unstageAll());

        refreshBtn = Util.createButton("Refresh", null, FontAwesomeSolid.SYNC);
        refreshBtn.addActionListener(e -> refresh());

        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Filter files...");
        searchField.addActionListener(e -> applyFilter());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public JButton getStageAllBtn() { return stageAllBtn; }
    public JButton getUnstageAllBtn() { return unstageAllBtn; }
    public JButton getRefreshBtn() { return refreshBtn; }
    public JTextField getSearchField() { return searchField; }

    public void refresh() {
        statusBar.setStatus("Refreshing working copy...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Context.updateStatus(null, true);
                return null;
            }

            @Override
            protected void done() {
                statusBar.setStatus("Working copy refreshed");
            }
        };
        worker.execute();
    }

    public void setItems(List<ScmItem> items) {
        tableModel.setItems(items);
        applyFilter();
        updateButtonStates();
    }

    public boolean hasStagedItems() {
        return tableModel.getAllItems().stream()
                .anyMatch(i -> isStaged(i.getAttribute().getStatus()));
    }

    private void updateButtonStates() {
        List<ScmItem> items = tableModel.getAllItems();
        boolean hasUnstaged = items.stream().anyMatch(i -> !isStaged(i.getAttribute().getStatus()));
        boolean hasStaged = items.stream().anyMatch(i -> isStaged(i.getAttribute().getStatus()));
        stageAllBtn.setEnabled(hasUnstaged);
        unstageAllBtn.setEnabled(hasStaged);
    }

    private void applyFilter() {
        String filter = searchField.getText().trim().toLowerCase();
        tableModel.applyFilter(filter);
    }

    private void toggleStage(int viewRow) {
        int modelRow = viewRow; // we manage filtering in model
        ScmItem item = tableModel.getItemAt(modelRow);
        if (item == null) return;

        String status = item.getAttribute().getStatus();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isStaged(status)) {
                    unstageItem(item);
                } else {
                    stageItem(item);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Stage/unstage failed", ex);
                    statusBar.setStatus("Error: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        };
        worker.execute();
    }

    private void stageItem(ScmItem item) throws Exception {
        String status = item.getAttribute().getStatus();
        String fileName = item.getShortName();

        if (ScmItem.Status.RENAMED.equals(status)) {
            String oldName = item.getAttribute().getOldName();
            if (oldName != null) {
                Context.getGitRepoService().renameFile(oldName, fileName);
            }
        } else if (ScmItem.Status.MISSED.equals(status)) {
            Context.getGitRepoService().removeFile(fileName);
        } else {
            // MODIFIED, UNTRACKED, UNTRACKED_FOLDER, CONFLICT
            Context.getGitRepoService().addFileToCommitStage(fileName);
        }
    }

    private void unstageItem(ScmItem item) throws Exception {
        String fileName = item.getShortName();
        Context.getGitRepoService().removeFileFromCommitStage(fileName);
    }

    private void stageAll() {
        List<ScmItem> items = tableModel.getAllItems();
        List<ScmItem> toStage = items.stream()
                .filter(i -> !isStaged(i.getAttribute().getStatus()))
                .toList();
        if (toStage.isEmpty()) return;

        statusBar.setStatus("Staging all...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : toStage) {
                    stageItem(item);
                }
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
        };
        worker.execute();
    }

    private void unstageAll() {
        List<ScmItem> items = tableModel.getAllItems();
        List<ScmItem> toUnstage = items.stream()
                .filter(i -> isStaged(i.getAttribute().getStatus()))
                .toList();
        if (toUnstage.isEmpty()) return;

        statusBar.setStatus("Unstaging all...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : toUnstage) {
                    unstageItem(item);
                }
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
        };
        worker.execute();
    }

    private void handleDoubleClick(int row) {
        ScmItem item = tableModel.getItemAt(row);
        if (item == null) return;

        String status = item.getAttribute().getStatus();
        String fileName = item.getShortName();

        if (ScmItem.Status.MISSED.equals(status) || ScmItem.Status.REMOVED.equals(status)) {
            return; // File doesn't exist on disk
        }

        // For modified/changed files, show diff with repository
        if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.CHANGED.equals(status)) {
            showDiffWithRepo(item);
        } else {
            // For others, open the file
            openFile(item);
        }
    }

    private void showContextMenu(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int[] selectedRows = table.getSelectedRows();

        // Ensure clicked row is selected
        if (row >= 0) {
            boolean isSelected = false;
            for (int r : selectedRows) {
                if (r == row) { isSelected = true; break; }
            }
            if (!isSelected) {
                table.setRowSelectionInterval(row, row);
                selectedRows = new int[]{row};
            }
        }

        if (selectedRows.length == 0) return;

        List<ScmItem> selected = new ArrayList<>();
        for (int r : selectedRows) {
            ScmItem item = tableModel.getItemAt(r);
            if (item != null) selected.add(item);
        }
        if (selected.isEmpty()) return;

        JPopupMenu menu = new JPopupMenu();

        if (selected.size() == 1) {
            buildSingleItemMenu(menu, selected.get(0));
        } else {
            buildMultiItemMenu(menu, selected);
        }

        menu.show(table, e.getX(), e.getY());
    }

    private void buildSingleItemMenu(JPopupMenu menu, ScmItem item) {
        String status = item.getAttribute().getStatus();
        String fileName = item.getShortName();

        // Stage / Unstage
        if (isStaged(status)) {
            JMenuItem unstage = new JMenuItem("Unstage");
            unstage.addActionListener(e -> doUnstage(item));
            menu.add(unstage);
        } else {
            JMenuItem stage = new JMenuItem("Stage");
            stage.addActionListener(e -> doStage(item));
            menu.add(stage);
        }

        menu.addSeparator();

        // Diff with repository
        if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.CHANGED.equals(status)) {
            JMenuItem diff = new JMenuItem("Diff with repository");
            diff.addActionListener(e -> showDiffWithRepo(item));
            menu.add(diff);
        }

        // Revert
        if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.MISSED.equals(status)) {
            JMenuItem revert = new JMenuItem("Revert...");
            revert.addActionListener(e -> revertItem(item));
            menu.add(revert);
        }

        // Conflict resolution
        if (status != null && status.startsWith("Conflict")) {
            menu.addSeparator();
            JMenu resolveMenu = new JMenu("Resolve conflict");

            JMenuItem markResolved = new JMenuItem("Mark resolved");
            markResolved.addActionListener(e -> resolveConflict(item, null));
            resolveMenu.add(markResolved);

            JMenuItem useOurs = new JMenuItem("Using mine (OURS)");
            useOurs.addActionListener(e -> resolveConflict(item, org.eclipse.jgit.api.CheckoutCommand.Stage.OURS));
            resolveMenu.add(useOurs);

            JMenuItem useTheirs = new JMenuItem("Using theirs (THEIRS)");
            useTheirs.addActionListener(e -> resolveConflict(item, org.eclipse.jgit.api.CheckoutCommand.Stage.THEIRS));
            resolveMenu.add(useTheirs);

            menu.add(resolveMenu);
        }

        // History
        if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.MISSED.equals(status)
                || ScmItem.Status.CHANGED.equals(status)) {
            menu.addSeparator();
            JMenuItem history = new JMenuItem("History");
            history.addActionListener(e -> showHistory(item));
            menu.add(history);
        }

        // Open
        if (!ScmItem.Status.MISSED.equals(status) && !ScmItem.Status.REMOVED.equals(status)) {
            JMenuItem open = new JMenuItem("Open");
            open.addActionListener(e -> openFile(item));
            menu.add(open);
        }

        // Physical delete
        if (!ScmItem.Status.MISSED.equals(status) && !ScmItem.Status.REMOVED.equals(status)) {
            menu.addSeparator();
            JMenuItem delete = new JMenuItem("Physical delete...");
            delete.addActionListener(e -> physicalDelete(item));
            menu.add(delete);
        }
    }

    private void buildMultiItemMenu(JPopupMenu menu, List<ScmItem> items) {
        boolean hasUnstaged = items.stream().anyMatch(i -> !isStaged(i.getAttribute().getStatus()));
        boolean hasStaged = items.stream().anyMatch(i -> isStaged(i.getAttribute().getStatus()));
        boolean hasRevertable = items.stream().anyMatch(i -> {
            String s = i.getAttribute().getStatus();
            return ScmItem.Status.MODIFIED.equals(s) || ScmItem.Status.MISSED.equals(s);
        });
        boolean hasDeletable = items.stream().anyMatch(i -> {
            String s = i.getAttribute().getStatus();
            return !ScmItem.Status.MISSED.equals(s) && !ScmItem.Status.REMOVED.equals(s);
        });

        if (hasUnstaged) {
            JMenuItem stage = new JMenuItem("Stage selected (" + items.stream().filter(i -> !isStaged(i.getAttribute().getStatus())).count() + ")");
            stage.addActionListener(e -> doStageMultiple(items.stream().filter(i -> !isStaged(i.getAttribute().getStatus())).toList()));
            menu.add(stage);
        }

        if (hasStaged) {
            JMenuItem unstage = new JMenuItem("Unstage selected (" + items.stream().filter(i -> isStaged(i.getAttribute().getStatus())).count() + ")");
            unstage.addActionListener(e -> doUnstageMultiple(items.stream().filter(i -> isStaged(i.getAttribute().getStatus())).toList()));
            menu.add(unstage);
        }

        if (hasRevertable) {
            menu.addSeparator();
            JMenuItem revert = new JMenuItem("Revert selected...");
            revert.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Revert " + items.size() + " selected files?",
                        "Revert", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    doRevertMultiple(items.stream().filter(i -> {
                        String s = i.getAttribute().getStatus();
                        return ScmItem.Status.MODIFIED.equals(s) || ScmItem.Status.MISSED.equals(s);
                    }).toList());
                }
            });
            menu.add(revert);
        }

        if (hasDeletable) {
            menu.addSeparator();
            JMenuItem delete = new JMenuItem("Physical delete selected...");
            delete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Physically delete " + items.size() + " selected files?",
                        "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (ScmItem item : items) {
                        String s = item.getAttribute().getStatus();
                        if (!ScmItem.Status.MISSED.equals(s) && !ScmItem.Status.REMOVED.equals(s)) {
                            deleteFileFromDisk(item.getShortName());
                        }
                    }
                    Context.updateStatus(null, true);
                }
            });
            menu.add(delete);
        }
    }

    // --- Actions ---

    private void doStage(ScmItem item) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                stageItem(item);
                return null;
            }

            @Override
            protected void done() {
                handleDone("Staged: " + item.getShortName());
            }
        };
        worker.execute();
    }

    private void doUnstage(ScmItem item) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                unstageItem(item);
                return null;
            }

            @Override
            protected void done() {
                handleDone("Unstaged: " + item.getShortName());
            }
        };
        worker.execute();
    }

    private void doStageMultiple(List<ScmItem> items) {
        statusBar.setStatus("Staging " + items.size() + " files...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : items) stageItem(item);
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                handleDone("Staged " + items.size() + " files");
            }
        };
        worker.execute();
    }

    private void doUnstageMultiple(List<ScmItem> items) {
        statusBar.setStatus("Unstaging " + items.size() + " files...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : items) unstageItem(item);
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                handleDone("Unstaged " + items.size() + " files");
            }
        };
        worker.execute();
    }

    private void handleDone(String message) {
        try {
            statusBar.setStatus(message);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Operation failed", ex);
            statusBar.setStatus("Error: " + ex.getMessage());
        }
        Context.updateStatus(null, true);
    }

    private void revertItem(ScmItem item) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Revert '" + item.getShortName() + "' to last committed version?\nAll local changes will be lost.",
                "Revert", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().checkoutFile(item.getShortName(), null);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusBar.setStatus("Reverted: " + item.getShortName());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Revert failed", ex);
                    statusBar.setStatus("Revert failed: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        };
        worker.execute();
    }

    private void doRevertMultiple(List<ScmItem> items) {
        statusBar.setStatus("Reverting " + items.size() + " files...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ScmItem item : items) {
                    Context.getGitRepoService().checkoutFile(item.getShortName(), null);
                }
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Reverted " + items.size() + " files");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Revert failed", ex);
                    statusBar.setStatus("Revert failed: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        };
        worker.execute();
    }

    private void resolveConflict(ScmItem item, org.eclipse.jgit.api.CheckoutCommand.Stage stage) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (stage != null) {
                    Context.getGitRepoService().checkoutFile(item.getShortName(), stage);
                }
                Context.getGitRepoService().addFileToCommitStage(item.getShortName());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusBar.setStatus("Conflict resolved: " + item.getShortName());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Resolve failed", ex);
                    statusBar.setStatus("Resolve failed: " + ex.getMessage());
                }
                Context.updateStatus(null, true);
            }
        };
        worker.execute();
    }

    private void showDiffWithRepo(ScmItem item) {
        String fileName = item.getShortName();
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String headSha = "HEAD";
                String tempPath = Context.getGitRepoService().saveFile(headSha, fileName);
                String repoContent = Files.readString(Paths.get(tempPath));
                String diskPath = Context.getProjectFolder() + fileName;
                String diskContent = Files.readString(Paths.get(diskPath));
                return new String[]{repoContent, diskContent};
            }

            @Override
            protected void done() {
                try {
                    String[] contents = get();
                    DiffViewerWindow diffWin = new DiffViewerWindow(fileName, "HEAD", contents[0], contents[1]);
                    diffWin.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to show diff", ex);
                    JOptionPane.showMessageDialog(WorkingCopyPanel.this,
                            "Cannot show diff: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showHistory(ScmItem item) {
        // Open a history panel in a new window for the file
        JFrame historyFrame = new JFrame("History: " + item.getShortName());
        historyFrame.setSize(1000, 600);
        historyFrame.setLocationRelativeTo(this);
        HistoryPanel hp = new HistoryPanel(statusBar);
        historyFrame.getContentPane().add(hp);
        historyFrame.setVisible(true);
        hp.loadFileHistory(item.getShortName());
    }

    private void openFile(ScmItem item) {
        String filePath = Context.getProjectFolder() + item.getShortName();
        if (ExtensionMap.isTextExtension(item.getShortName())) {
            try {
                String content = Files.readString(Paths.get(filePath));
                FileViewerWindow viewer = new FileViewerWindow(item.getShortName(), content, item.getShortName());
                viewer.setVisible(true);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file", ex);
                JOptionPane.showMessageDialog(this, "Cannot open: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                Desktop.getDesktop().open(new File(filePath));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file with system", ex);
            }
        }
    }

    private void physicalDelete(ScmItem item) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Physically delete '" + item.getShortName() + "'?\nThis cannot be undone.",
                "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        deleteFileFromDisk(item.getShortName());
        Context.updateStatus(null, true);
    }

    private void deleteFileFromDisk(String fileName) {
        try {
            java.nio.file.Path path = Paths.get(Context.getProjectFolder(), fileName);
            Files.deleteIfExists(path);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot delete file: " + fileName, ex);
        }
    }

    private static boolean isStaged(String status) {
        return ScmItem.Status.ADDED.equals(status)
                || ScmItem.Status.CHANGED.equals(status)
                || ScmItem.Status.RENAMED.equals(status)
                || ScmItem.Status.REMOVED.equals(status);
    }

    // --- Table Model ---

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

        List<ScmItem> getAllItems() {
            return new ArrayList<>(allItems);
        }

        ScmItem getItemAt(int row) {
            if (row >= 0 && row < filteredItems.size()) {
                return filteredItems.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return filteredItems.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // We handle checkbox click manually
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScmItem item = filteredItems.get(rowIndex);
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            return switch (columnIndex) {
                case 0 -> status; // color indicator
                case 1 -> isStaged(status); // checkbox
                case 2 -> status;
                case 3 -> item.getShortName();
                default -> "";
            };
        }
    }

    // --- Renderers ---

    private static class ColorStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            label.setOpaque(true);

            Color color;
            if (isStaged(status)) {
                color = STAGED_COLOR;
            } else if (status.startsWith("Conflict")) {
                color = CONFLICT_COLOR;
            } else if (ScmItem.Status.UNTRACKED.equals(status) || ScmItem.Status.UNTRACKED_FOLDER.equals(status)) {
                color = UNTRACKED_COLOR;
            } else {
                color = UNSTAGED_COLOR;
            }

            if (!isSelected) {
                label.setBackground(color);
            }
            return label;
        }
    }

    private static class StatusTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";

            if (!isSelected) {
                if (isStaged(status)) {
                    label.setForeground(STAGED_COLOR.darker());
                } else if (status.startsWith("Conflict")) {
                    label.setForeground(CONFLICT_COLOR);
                } else if (ScmItem.Status.UNTRACKED.equals(status) || ScmItem.Status.UNTRACKED_FOLDER.equals(status)) {
                    label.setForeground(UNTRACKED_COLOR);
                } else {
                    label.setForeground(UNSTAGED_COLOR);
                }
            }

            return label;
        }
    }
}
