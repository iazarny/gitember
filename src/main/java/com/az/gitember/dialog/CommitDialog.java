package com.az.gitember.dialog;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.service.detector.DetectorService;
import com.az.gitember.service.detector.FileType;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import com.az.gitember.ui.FileViewerWindow;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Logger;

public class CommitDialog extends JDialog {

    private static final Logger log = Logger.getLogger(CommitDialog.class.getName());

    private static final java.util.Set<String> STAGED_STATUSES = java.util.Set.of(
            ScmItem.Status.ADDED,
            ScmItem.Status.CHANGED,
            ScmItem.Status.REMOVED,
            ScmItem.Status.RENAMED
    );

    private final JTextArea messageArea;
    private final JTable filesTable;
    private final DefaultTableModel tableModel;
    private final DefaultTableModel findingsModel;
    private final JPanel findingsPanel;
    private final List<Finding> findings = new ArrayList<>();

    // Scan progress UI
    private final JLabel       scanStatusLabel;
    private final JProgressBar scanProgress;
    private final JPanel       scanStatusPanel;

    public CommitDialog(Frame parent) {
        super(parent, "Commit" + (Context.getWorkingBranch() != null
                ? " [" + Context.getWorkingBranch().getShortName() + "]" : ""),
                java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setMinimumSize(new Dimension(800, 400));
        setSize(800, 580);
        setLocationRelativeTo(parent);

        // Files table
        tableModel = new DefaultTableModel(new String[]{"Status", "File"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        filesTable = new JTable(tableModel);
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        filesTable.getColumnModel().getColumn(0).setMaxWidth(150);

        populateFiles();

        JScrollPane tableScroll = new JScrollPane(filesTable);
        tableScroll.setPreferredSize(new Dimension(0, 160));

        // Message area
        JLabel msgLabel = new JLabel("Commit message:");
        messageArea = new JTextArea(5, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);

        // Scan status panel (shown while LLM scan is in progress)
        scanStatusLabel = new JLabel("Scanning for secrets…");
        scanStatusLabel.setFont(scanStatusLabel.getFont().deriveFont(Font.ITALIC));
        scanProgress = new JProgressBar();
        scanProgress.setIndeterminate(true);
        scanProgress.setPreferredSize(new Dimension(0, 6));
        scanStatusPanel = new JPanel(new BorderLayout(4, 2));
        scanStatusPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        scanStatusPanel.add(scanProgress,    BorderLayout.NORTH);
        scanStatusPanel.add(scanStatusLabel, BorderLayout.CENTER);
        scanStatusPanel.setVisible(false);

        // Findings table (hidden until results arrive)
        findingsModel = new DefaultTableModel(new String[]{"File", "Line", "Type", "Confidence", "Details", ""}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
            @Override public Class<?> getColumnClass(int col) { return col == 5 ? JButton.class : Object.class; }
        };
        JTable findingsTable = new JTable(findingsModel);
        findingsTable.setRowHeight(findingsTable.getRowHeight() + 4);
        findingsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        findingsTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        findingsTable.getColumnModel().getColumn(1).setMaxWidth(60);
        findingsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        findingsTable.getColumnModel().getColumn(2).setMaxWidth(140);
        findingsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        findingsTable.getColumnModel().getColumn(3).setMaxWidth(90);
        findingsTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        findingsTable.getColumnModel().getColumn(5).setMaxWidth(70);
        findingsTable.setDefaultRenderer(Object.class, new FindingsCellRenderer());
        findingsTable.getColumnModel().getColumn(5).setCellRenderer(new OpenButtonRenderer());
        findingsTable.getColumnModel().getColumn(5).setCellEditor(new OpenButtonEditor(findingsTable));
        findingsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = findingsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) openFinding(row);
                }
            }
        });

        JScrollPane findingsScroll = new JScrollPane(findingsTable);
        findingsScroll.setPreferredSize(new Dimension(0, 120));

        JLabel findingsLabel = new JLabel("⚠ Potential secrets / sensitive data detected:");
        findingsLabel.setForeground(SyntaxStyleUtil.statusColor("DELETE"));
        findingsLabel.setFont(findingsLabel.getFont().deriveFont(Font.BOLD));

        findingsPanel = new JPanel(new BorderLayout(3, 3));
        findingsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        findingsPanel.add(findingsLabel, BorderLayout.NORTH);
        findingsPanel.add(findingsScroll, BorderLayout.CENTER);
        findingsPanel.setVisible(false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton commitBtn = new JButton("Commit");
        commitBtn.addActionListener(e -> onCommit());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(commitBtn);
        buttonPanel.add(cancelBtn);

        // Layout
        JPanel southOfMessage = new JPanel(new BorderLayout());
        southOfMessage.add(scanStatusPanel, BorderLayout.NORTH);
        southOfMessage.add(findingsPanel,   BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(msgLabel,        BorderLayout.NORTH);
        messagePanel.add(msgScroll,       BorderLayout.CENTER);
        messagePanel.add(southOfMessage,  BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(tableScroll,   BorderLayout.NORTH);
        mainPanel.add(messagePanel,  BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel,    BorderLayout.CENTER);
        getContentPane().add(buttonPanel,  BorderLayout.SOUTH);

        getRootPane().setDefaultButton(commitBtn);
        Util.bindEscapeToDispose(this);

        // Run detector after dialog is laid out
        SwingUtilities.invokeLater(this::startDetector);
    }

    // -------------------------------------------------------------------------

    private void populateFiles() {
        tableModel.setRowCount(0);
        List<ScmItem> items = Context.getStatusList();
        if (items != null) {
            for (ScmItem item : items) {
                String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                if (STAGED_STATUSES.contains(status)) {
                    tableModel.addRow(new Object[]{ status, item.getShortName() });
                }
            }
        }
    }

    private boolean isLeakDetectorEnabled() {
        return Context.getSettings() != null
                && Boolean.TRUE.equals(Context.getSettings().getEnableLeakDetector());
    }

    private String llmModel() {
        return Context.getSettings() != null
                ? Context.getSettings().getLlmDetectorModel()
                : "llama3.2";
    }

    // -------------------------------------------------------------------------
    //  Async detector
    // -------------------------------------------------------------------------

    private void startDetector() {
        if (!isLeakDetectorEnabled()) return;

        List<ScmItem> items = Context.getStatusList();
        if (items == null || items.isEmpty()) return;

        // Collect paths to scan up front on EDT
        String repoPath = Context.getProjectFolder();
        List<Path> toScan = new ArrayList<>();
        for (ScmItem item : items) {
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            if (!STAGED_STATUSES.contains(status) || ScmItem.Status.REMOVED.equals(status)) continue;
            Path p = Paths.get(repoPath, item.getShortName());
            if (Files.exists(p) && Files.isRegularFile(p)) toScan.add(p);
        }
        if (toScan.isEmpty()) return;

        scanStatusPanel.setVisible(true);
        pack();

        String model = llmModel();

        new SwingWorker<List<Finding>, String>() {

            @Override
            protected List<Finding> doInBackground() throws Exception {
                // ---- Try to set up Ollama (best-effort) ----
                boolean llmReady = false;
                try {
                    OllamaManager.Status status = OllamaManager.getStatus();

                    if (status == OllamaManager.Status.STOPPED) {
                        publish("Starting Ollama…");
                        OllamaManager.startServerAndWait(20_000);
                        status = OllamaManager.Status.RUNNING;
                    }

                    if (status == OllamaManager.Status.RUNNING) {
                        if (!OllamaManager.isModelAvailable(model)) {
                            publish("Pulling model \"" + model + "\" (first run, please wait)...");
                            Process pull = OllamaManager.startModelPull(model);
                            try (java.io.BufferedReader br = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(pull.getInputStream()))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    String trimmed = line.trim();
                                    if (!trimmed.isEmpty()) publish("Pulling: " + trimmed);
                                }
                            }
                            pull.waitFor();
                        }
                        llmReady = OllamaManager.isRunning() && OllamaManager.isModelAvailable(model);
                    }
                } catch (Exception e) {
                    log.fine("Ollama setup failed, falling back to empirical scan: " + e.getMessage());
                }

                // ---- Scan files ----
                publish(llmReady ? "LLM secret scan in progress…" : "Scanning for secrets…");

                DetectorService service = llmReady
                        ? new DetectorService(OllamaManager.BASE_URL, model)
                        : new DetectorService();

                List<Finding> all = new ArrayList<>();
                for (Path filePath : toScan) {
                    try {
                        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                        ScanContext ctx = new ScanContext(filePath, lines, new FileType());
                        all.addAll(service.detect(ctx));
                    } catch (Exception ex) {
                        log.fine("Skipping " + filePath + ": " + ex.getMessage());
                    }
                }
                return all;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    scanStatusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                scanStatusPanel.setVisible(false);
                try {
                    List<Finding> all = get();
                    if (!all.isEmpty()) {
                        findings.clear();
                        findings.addAll(all);
                        findingsModel.setRowCount(0);
                        for (Finding f : all) {
                            String fileName = f.getFile() != null ? f.getFile().getFileName().toString() : "";
                            findingsModel.addRow(new Object[]{
                                    fileName,
                                    f.getLineNo(),
                                    f.getType(),
                                    f.getConfidence() != null ? f.getConfidence().name() : "",
                                    f.getMessage(),
                                    "Open"
                            });
                        }
                        findingsPanel.setVisible(true);
                    }
                } catch (Exception ex) {
                    log.fine("Detector worker failed: " + ex.getMessage());
                }
                pack();
                setLocationRelativeTo(getOwner());
            }
        }.execute();
    }

    // -------------------------------------------------------------------------

    private void onCommit() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Commit message is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            var project = Context.getCurrentProject().orElse(null);
            String authorName     = StringUtils.trimToNull(project.getUserCommitName());
            String authorEmail    = StringUtils.trimToNull(project.getUserCommitEmail());
            String committerName  = StringUtils.trimToNull(project.getCommitterName());
            String committerEmail = StringUtils.trimToNull(project.getCommitterEmail());
            Context.getGitRepoService().commit(message, authorName, authorEmail, committerName, committerEmail);
            Context.updateStatus(null);
            Context.updateBranches();
            Context.updateWorkingBranch();
            dispose();
        } catch (Exception e) {
            log.warning("Commit failed: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Commit failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFinding(int row) {
        if (row < 0 || row >= findings.size()) return;
        Finding f = findings.get(row);
        if (f.getFile() == null) return;
        try {
            String content = Files.readString(f.getFile(), StandardCharsets.UTF_8);
            FileViewerWindow viewer = new FileViewerWindow(
                    f.getFile().getFileName().toString(), content,
                    f.getFile().getFileName().toString());
            viewer.setVisible(true);
            viewer.toFront();
            viewer.requestFocus();
            SwingUtilities.invokeLater(() -> viewer.scrollToAndHighlight(f.getLineNo()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot open file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    //  Cell renderers / editors
    // -------------------------------------------------------------------------

    private static class OpenButtonRenderer implements TableCellRenderer {
        private final JButton btn = new JButton("Open");
        { btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f)); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return btn;
        }
    }

    private class OpenButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton("Open");
        private int clickedRow = -1;

        OpenButtonEditor(JTable table) {
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f));
            btn.addActionListener(e -> {
                fireEditingStopped();
                openFinding(clickedRow);
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            clickedRow = row;
            return btn;
        }
        @Override public Object getCellEditorValue() { return "Open"; }
        @Override public boolean isCellEditable(EventObject e) { return true; }
    }

    /** Colours findings rows by confidence level. */
    private static class FindingsCellRenderer extends DefaultTableCellRenderer {

        private static final Color COLOR_CRITICAL = new Color(0xFF, 0xCC, 0xCC);
        private static final Color COLOR_HIGH     = new Color(0xFF, 0xE8, 0xCC);
        private static final Color COLOR_MEDIUM   = new Color(0xFF, 0xF8, 0xCC);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object confObj = table.getModel().getValueAt(row, 3);
                String conf = confObj != null ? confObj.toString() : "";
                c.setBackground(switch (conf) {
                    case "CRITICAL" -> COLOR_CRITICAL;
                    case "HIGH"     -> COLOR_HIGH;
                    case "MEDIUM"   -> COLOR_MEDIUM;
                    default         -> table.getBackground();
                });
            }
            return c;
        }
    }
}
