package com.az.gitember.ui;

import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitemberUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detail panel shown when a commit is selected in the history.
 * Has two tabs: "Main" (header + changed files) and "Diff" (raw diff text).
 * Mirrors the original gitember HistoryDetailController.
 */
public class CommitDetailPanel extends JPanel {

    private static final Logger log = Logger.getLogger(CommitDetailPanel.class.getName());

    // Header fields
    private final JTextField msgField = createReadOnlyField();
    private final JTextField authorField = createReadOnlyField();
    private final JTextField emailField = createReadOnlyField();
    private final JTextField dateField = createReadOnlyField();
    private final JTextField shaField = createReadOnlyField();
    private final JTextField parentField = createReadOnlyField();
    private final JTextField refsField = createReadOnlyField();

    // Changed files table
    private final ChangedFilesTableModel filesTableModel = new ChangedFilesTableModel();
    private final JTable filesTable;

    // Diff tab
    private final RSyntaxTextArea diffArea;
    private final SearchBar searchBar;
    private final JTabbedPane tabbedPane;

    private ScmRevisionInformation currentRevision;

    public CommitDetailPanel() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

        // === Main tab ===
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header grid
        JPanel headerPanel = buildHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Changed files table
        filesTable = new JTable(filesTableModel);
        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filesTable.setShowVerticalLines(false);
        filesTable.setIntercellSpacing(new Dimension(0, 0));
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        filesTable.getColumnModel().getColumn(0).setMaxWidth(80);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(700);

        // Status column renderer with color coding
        filesTable.getColumnModel().getColumn(0).setCellRenderer(new StatusCellRenderer());

        // Context menu for changed files
        setupFilesContextMenu();

        // Double-click on changed file
        filesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = filesTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        handleFileDoubleClick(filesTableModel.getItemAt(row));
                    }
                }
            }
        });

        mainPanel.add(new JScrollPane(filesTable), BorderLayout.CENTER);
        tabbedPane.addTab("Main", mainPanel);

        // === Diff tab ===
        JPanel diffPanel = new JPanel(new BorderLayout());

        diffArea = new RSyntaxTextArea();
        diffArea.setEditable(false);
        diffArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        diffArea.setCodeFoldingEnabled(false);
        diffArea.setAntiAliasingEnabled(true);
        diffArea.setFont(SyntaxStyleUtil.monoFont());
        SyntaxStyleUtil.applyTheme(diffArea);

        searchBar = new SearchBar(diffArea);

        JButton saveBtn = new JButton("Save as...");
        saveBtn.addActionListener(e -> saveDiff());
        JPanel diffToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        diffToolbar.add(saveBtn);

        JPanel northDiff = new JPanel(new BorderLayout());
        northDiff.add(diffToolbar, BorderLayout.NORTH);
        northDiff.add(searchBar,   BorderLayout.SOUTH);

        // Ctrl/Cmd+F activates search bar when focus is anywhere in the diff panel
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        diffPanel.registerKeyboardAction(
                e -> searchBar.activate(), ctrlF, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        diffPanel.add(northDiff, BorderLayout.NORTH);
        diffPanel.add(new RTextScrollPane(diffArea), BorderLayout.CENTER);
        tabbedPane.addTab("Diff", diffPanel);

        // Lazy load diff when tab selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1 && currentRevision != null) {
                loadRawDiff();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Context menu for changed files ---
    private void setupFilesContextMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openSelectedFile());

        JMenuItem showRawDiffItem = new JMenuItem("Show raw diff");
        showRawDiffItem.addActionListener(e -> showRawDiffForFile());

        JMenuItem diffPrevItem = new JMenuItem("Difference with previous version");
        diffPrevItem.addActionListener(e -> diffWithPreviousVersion());

        JMenuItem diffLatestItem = new JMenuItem("Difference with latest version");
        diffLatestItem.addActionListener(e -> diffWithLatestVersion());

        JMenuItem diffDiskItem = new JMenuItem("Difference with file on disk");
        diffDiskItem.addActionListener(e -> diffWithDiskVersion());

        popup.add(openItem);
        popup.addSeparator();
        popup.add(showRawDiffItem);
        popup.add(diffPrevItem);
        popup.add(diffLatestItem);
        popup.add(diffDiskItem);

        // Dynamic enable/disable based on selected file
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                int row = filesTable.getSelectedRow();
                ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
                if (item == null) {
                    openItem.setEnabled(false);
                    showRawDiffItem.setEnabled(false);
                    diffPrevItem.setEnabled(false);
                    diffLatestItem.setEnabled(false);
                    diffDiskItem.setEnabled(false);
                    return;
                }

                boolean isText = ExtensionMap.isTextExtension(item.getShortName());
                String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                boolean isAdded = "ADDED".equals(status) || "ADD".equals(status);
                boolean isRemoved = "REMOVED".equals(status) || "DELETE".equals(status);

                openItem.setEnabled(true);
                showRawDiffItem.setEnabled(isText && !isAdded);
                diffPrevItem.setEnabled(isText && !isAdded);
                diffLatestItem.setEnabled(isText && !isRemoved);
                diffDiskItem.setEnabled(isText && !isRemoved);
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        filesTable.setComponentPopupMenu(popup);
    }

    // --- File action handlers ---

    /**
     * Double-click behavior:
     * - Modified/text: open diff with previous version
     * - Added/text: open file content
     * - Deleted/text: open last version before deletion
     * - Non-text: do nothing
     */
    private void handleFileDoubleClick(ScmItem item) {
        if (item == null || currentRevision == null) return;
        boolean isText = ExtensionMap.isTextExtension(item.getShortName());
        if (!isText) return;

        String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
        switch (status) {
            case "ADDED", "ADD" -> openFileFromCommit(item, currentRevision.getRevisionFullName());
            case "REMOVED", "DELETE" -> openFileFromParentCommit(item);
            default -> diffWithPreviousVersion();  // MODIFIED, RENAMED, etc.
        }
    }

    private void openSelectedFile() {
        int row = filesTable.getSelectedRow();
        ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
        if (item == null || currentRevision == null) return;

        String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
        if ("REMOVED".equals(status) || "DELETE".equals(status)) {
            openFileFromParentCommit(item);
        } else {
            openFileFromCommit(item, currentRevision.getRevisionFullName());
        }
    }

    private void openFileFromCommit(ScmItem item, String commitSha) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String tempPath = Context.getGitRepoService().saveFile(commitSha, item.getShortName());
                return Files.readString(Paths.get(tempPath));
            }

            @Override
            protected void done() {
                try {
                    String content = get();
                    FileViewerWindow viewer = new FileViewerWindow(
                            item.getShortName() + " @ " + commitSha.substring(0, Math.min(8, commitSha.length())),
                            content);
                    viewer.setVisible(true);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to open file", e);
                    JOptionPane.showMessageDialog(CommitDetailPanel.this,
                            "Failed to open file: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openFileFromParentCommit(ScmItem item) {
        if (currentRevision == null) return;
        List<String> parents = currentRevision.getParents();
        if (parents != null && !parents.isEmpty()) {
            openFileFromCommit(item, parents.get(0));
        } else {
            JOptionPane.showMessageDialog(this,
                    "No parent commit available", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRawDiffForFile() {
        int row = filesTable.getSelectedRow();
        ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
        if (item == null || currentRevision == null) return;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return Context.getGitRepoService().getRawDiff(
                        currentRevision.getRevisionFullName(), item.getShortName());
            }

            @Override
            protected void done() {
                try {
                    String diff = get();
                    FileViewerWindow viewer = new FileViewerWindow(
                            "Diff: " + item.getShortName(), diff);
                    viewer.setVisible(true);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load raw diff", e);
                    JOptionPane.showMessageDialog(CommitDetailPanel.this,
                            "Failed to load diff: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void diffWithPreviousVersion() {
        int row = filesTable.getSelectedRow();
        ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
        if (item == null || currentRevision == null) return;

        SwingWorker<List<ScmRevisionInformation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScmRevisionInformation> doInBackground() throws Exception {
                return Context.getGitRepoService().getFileHistory(
                        item.getShortName(), currentRevision.getRevisionFullName());
            }

            @Override
            protected void done() {
                try {
                    List<ScmRevisionInformation> fileRevs = get();
                    if (fileRevs.size() >= 2) {
                        String newSha = fileRevs.get(0).getRevisionFullName();
                        String oldSha = fileRevs.get(1).getRevisionFullName();
                        DiffViewerWindow diffWindow = new DiffViewerWindow(
                                item.getShortName(), fileRevs, oldSha, newSha);
                        diffWindow.setVisible(true);
                    } else if (fileRevs.size() == 1) {
                        // Only one revision - just open the file
                        openFileFromCommit(item, fileRevs.get(0).getRevisionFullName());
                    } else {
                        JOptionPane.showMessageDialog(CommitDetailPanel.this,
                                "No history found for this file",
                                "Diff", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to get file history", e);
                    JOptionPane.showMessageDialog(CommitDetailPanel.this,
                            "Failed to load file history: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void diffWithLatestVersion() {
        int row = filesTable.getSelectedRow();
        ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
        if (item == null || currentRevision == null) return;

        SwingWorker<List<ScmRevisionInformation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScmRevisionInformation> doInBackground() throws Exception {
                // Get full file history (from HEAD) and also from current commit tree
                List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                        item.getShortName(), null);
                List<ScmRevisionInformation> treeRevs = Context.getGitRepoService().getFileHistory(
                        item.getShortName(), currentRevision.getRevisionFullName());
                // Merge and deduplicate
                for (ScmRevisionInformation r : treeRevs) {
                    boolean exists = fileRevs.stream()
                            .anyMatch(fr -> fr.getRevisionFullName().equals(r.getRevisionFullName()));
                    if (!exists) fileRevs.add(r);
                }
                return fileRevs;
            }

            @Override
            protected void done() {
                try {
                    List<ScmRevisionInformation> fileRevs = get();
                    if (!fileRevs.isEmpty()) {
                        String newSha = fileRevs.get(0).getRevisionFullName(); // latest
                        String oldSha = currentRevision.getRevisionFullName();
                        DiffViewerWindow diffWindow = new DiffViewerWindow(
                                item.getShortName(), fileRevs, oldSha, newSha);
                        diffWindow.setVisible(true);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load file history", e);
                    JOptionPane.showMessageDialog(CommitDetailPanel.this,
                            "Failed: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void diffWithDiskVersion() {
        int row = filesTable.getSelectedRow();
        ScmItem item = row >= 0 ? filesTableModel.getItemAt(row) : null;
        if (item == null || currentRevision == null) return;

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                // Read file from commit
                String tempPath = Context.getGitRepoService().saveFile(
                        currentRevision.getRevisionFullName(), item.getShortName());
                String commitContent = Files.readString(Paths.get(tempPath));

                // Read file from working directory
                String workDir = Context.getRepositoryPath().replace("/.git", "").replace("\\.git", "");
                String diskPath = workDir + "/" + item.getShortName();
                String diskContent;
                try {
                    diskContent = Files.readString(Paths.get(diskPath));
                } catch (Exception e) {
                    diskContent = "(File not found on disk)";
                }
                return new String[]{commitContent, diskContent};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    // Show side-by-side diff using JGit HISTOGRAM algorithm
                    DiffViewerWindow window = new DiffViewerWindow(
                            item.getShortName(),
                            currentRevision.getRevisionFullName(),
                            texts[0], texts[1]);
                    window.setVisible(true);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to diff with disk", e);
                    JOptionPane.showMessageDialog(CommitDetailPanel.this,
                            "Failed: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // --- Header panel ---
    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        GridBagConstraints lbl = new GridBagConstraints();
        lbl.anchor = GridBagConstraints.WEST;
        lbl.insets = new Insets(1, 2, 1, 4);

        GridBagConstraints fld = new GridBagConstraints();
        fld.fill = GridBagConstraints.HORIZONTAL;
        fld.weightx = 1.0;
        fld.insets = new Insets(1, 0, 1, 8);

        // Row 0: Message (spans full width)
        lbl.gridx = 0; lbl.gridy = 0;
        panel.add(new JLabel("Message:"), lbl);
        fld.gridx = 1; fld.gridy = 0; fld.gridwidth = 5;
        panel.add(msgField, fld);
        fld.gridwidth = 1;

        // Row 1: Author | Email | Date
        lbl.gridx = 0; lbl.gridy = 1;
        panel.add(new JLabel("Author:"), lbl);
        fld.gridx = 1; fld.gridy = 1;
        panel.add(authorField, fld);

        lbl.gridx = 2; lbl.gridy = 1;
        panel.add(new JLabel("Email:"), lbl);
        fld.gridx = 3; fld.gridy = 1;
        panel.add(emailField, fld);

        lbl.gridx = 4; lbl.gridy = 1;
        panel.add(new JLabel("Date:"), lbl);
        fld.gridx = 5; fld.gridy = 1;
        panel.add(dateField, fld);

        // Row 2: SHA | Parent | Refs
        lbl.gridx = 0; lbl.gridy = 2;
        panel.add(new JLabel("SHA:"), lbl);
        fld.gridx = 1; fld.gridy = 2;
        panel.add(shaField, fld);

        lbl.gridx = 2; lbl.gridy = 2;
        panel.add(new JLabel("Parent:"), lbl);
        fld.gridx = 3; fld.gridy = 2;
        panel.add(parentField, fld);

        lbl.gridx = 4; lbl.gridy = 2;
        panel.add(new JLabel("Refs:"), lbl);
        fld.gridx = 5; fld.gridy = 2;
        panel.add(refsField, fld);

        return panel;
    }

    public void showRevision(ScmRevisionInformation rev) {
        this.currentRevision = rev;
        diffArea.setText("");
        diffArea.removeAllLineHighlights();
        searchBar.close();

        if (rev == null) {
            clearFields();
            filesTableModel.clear();
            return;
        }

        // Populate header fields
        msgField.setText(rev.getFullMessage() != null
                ? rev.getFullMessage().replace("\n", " ") : rev.getShortMessage());
        authorField.setText(rev.getAuthorName());
        emailField.setText(rev.getAuthorEmail() != null ? rev.getAuthorEmail() : "");
        dateField.setText(rev.getDate() != null ? GitemberUtil.formatDate(rev.getDate()) : "");
        shaField.setText(rev.getRevisionFullName());
        parentField.setText(rev.getParents() != null
                ? String.join(", ", rev.getParents()) : "");
        refsField.setText(rev.getRef() != null
                ? String.join(", ", rev.getRef()) : "");

        // Populate changed files table
        filesTableModel.setData(rev.getAffectedItems());

        // Switch to Main tab
        tabbedPane.setSelectedIndex(0);
    }

    private void clearFields() {
        msgField.setText("");
        authorField.setText("");
        emailField.setText("");
        dateField.setText("");
        shaField.setText("");
        parentField.setText("");
        refsField.setText("");
    }

    private void loadRawDiff() {
        if (currentRevision == null) return;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    return Context.getGitRepoService().getRawDiff(
                            currentRevision.getRevisionFullName(), null);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load raw diff", e);
                    return "Failed to load diff: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    diffArea.setText(get());
                    diffArea.setCaretPosition(0);
                    applyDiffHighlights();
                } catch (Exception e) {
                    diffArea.setText("Error loading diff");
                }
            }
        };
        worker.execute();
    }

    private void applyDiffHighlights() {
        diffArea.removeAllLineHighlights();
        Color addedColor   = SyntaxStyleUtil.addedBg();
        Color deletedColor = SyntaxStyleUtil.deletedBg();
        Color hunkColor    = SyntaxStyleUtil.changedBg();
        String[] lines = diffArea.getText().split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            try {
                if (line.startsWith("+++") || line.startsWith("---")) {
                    diffArea.addLineHighlight(i, hunkColor);
                } else if (line.startsWith("+")) {
                    diffArea.addLineHighlight(i, addedColor);
                } else if (line.startsWith("-")) {
                    diffArea.addLineHighlight(i, deletedColor);
                } else if (line.startsWith("@@")) {
                    diffArea.addLineHighlight(i, hunkColor);
                }
            } catch (Exception ignored) {}
        }
    }

    private void saveDiff() {
        if (diffArea.getText().isBlank()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save diff");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.writeString(
                        chooser.getSelectedFile().toPath(), diffArea.getText());
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to save diff", e);
                JOptionPane.showMessageDialog(this,
                        "Failed to save: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        return field;
    }

    // --- Status cell renderer with color-coded status ---
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            if (!isSelected && value instanceof String status) {
                switch (status) {
                    case "ADDED", "ADD" -> setForeground(new Color(0, 150, 0));
                    case "MODIFIED", "MODIFY" -> setForeground(new Color(0, 100, 200));
                    case "REMOVED", "DELETE" -> setForeground(new Color(200, 0, 0));
                    case "RENAMED", "RENAME", "COPY" -> setForeground(new Color(150, 100, 0));
                    default -> setForeground(table.getForeground());
                }
            }
            return this;
        }
    }

    // --- Table model for changed files ---
    static class ChangedFilesTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Status", "File"};
        private List<ScmItem> items = new ArrayList<>();

        public void setData(List<ScmItem> data) {
            this.items = data != null ? new ArrayList<>(data) : new ArrayList<>();
            fireTableDataChanged();
        }

        public void clear() {
            items = new ArrayList<>();
            fireTableDataChanged();
        }

        public ScmItem getItemAt(int row) {
            return row >= 0 && row < items.size() ? items.get(row) : null;
        }

        @Override
        public int getRowCount() {
            return items.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScmItem item = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.getAttribute() != null ? item.getAttribute().getStatus() : "?";
                case 1 -> item.getViewRepresentation();
                default -> "";
            };
        }
    }
}
