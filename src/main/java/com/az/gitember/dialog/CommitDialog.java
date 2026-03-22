package com.az.gitember.dialog;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.detector.DetectorService;
import com.az.gitember.service.detector.FileType;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import com.az.gitember.ui.SyntaxStyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public CommitDialog(Frame parent) {
        super(parent, "Commit" + (Context.getWorkingBranch() != null
                ? " [" + Context.getWorkingBranch().getShortName() + "]" : ""), true);
        setSize(640, 580);
        setLocationRelativeTo(parent);

        // Files table
        tableModel = new DefaultTableModel(new String[]{"File", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        filesTable = new JTable(tableModel);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        filesTable.getColumnModel().getColumn(1).setMaxWidth(150);

        populateFiles();

        JScrollPane tableScroll = new JScrollPane(filesTable);
        tableScroll.setPreferredSize(new Dimension(0, 160));

        // Message area
        JLabel msgLabel = new JLabel("Commit message:");
        messageArea = new JTextArea(5, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);

        // Findings table (hidden until there are results)
        findingsModel = new DefaultTableModel(new String[]{"File", "Line", "Type", "Confidence", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable findingsTable = new JTable(findingsModel);
        findingsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        findingsTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        findingsTable.getColumnModel().getColumn(1).setMaxWidth(60);
        findingsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        findingsTable.getColumnModel().getColumn(2).setMaxWidth(140);
        findingsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        findingsTable.getColumnModel().getColumn(3).setMaxWidth(90);
        findingsTable.setDefaultRenderer(Object.class, new FindingsCellRenderer());

        JScrollPane findingsScroll = new JScrollPane(findingsTable);
        findingsScroll.setPreferredSize(new Dimension(0, 120));

        JLabel findingsLabel = new JLabel("⚠ Potential secrets / sensitive data detected:");
        //findingsLabel.setForeground(new Color(0xB00020));
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
        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(msgLabel, BorderLayout.NORTH);
        messagePanel.add(msgScroll, BorderLayout.CENTER);
        messagePanel.add(findingsPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(tableScroll, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(commitBtn);

        // Run detector after dialog is laid out
        SwingUtilities.invokeLater(this::runDetector);
    }

    private void populateFiles() {
        tableModel.setRowCount(0);
        List<ScmItem> items = Context.getStatusList();
        if (items != null) {
            for (ScmItem item : items) {
                String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                if (STAGED_STATUSES.contains(status)) {
                    tableModel.addRow(new Object[]{item.getShortName(), status});
                }
            }
        }
    }

    private void runDetector() {
        if (!isLeakDetectorEnabled()) return;

        List<ScmItem> items = Context.getStatusList();
        if (items == null || items.isEmpty()) return;

        String repoPath = Context.getProjectFolder();
        DetectorService service = new DetectorService();
        List<Finding> allFindings = new ArrayList<>();

        for (ScmItem item : items) {
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            if (!STAGED_STATUSES.contains(status)) continue;
            if (ScmItem.Status.REMOVED.equals(status)) continue; // deleted files have no content

            Path filePath = Paths.get(repoPath, item.getShortName());
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) continue;

            try {
                List<String> lines = Files.readAllLines(filePath);
                ScanContext ctx = new ScanContext(filePath, lines, new FileType());
                allFindings.addAll(service.detect(ctx));
            } catch (IOException | RuntimeException ex) {
                log.fine("Skipping " + item.getShortName() + ": " + ex.getMessage());
            }
        }

        if (!allFindings.isEmpty()) {
            findingsModel.setRowCount(0);
            for (Finding f : allFindings) {
                String fileName = f.getFile() != null ? f.getFile().getFileName().toString() : "";
                findingsModel.addRow(new Object[]{
                        fileName,
                        f.getLineNo(),
                        f.getType(),
                        f.getConfidence() != null ? f.getConfidence().name() : "",
                        f.getMessage()
                });
            }
            findingsPanel.setVisible(true);
            pack();
            setLocationRelativeTo(getOwner());
        }
    }

    private boolean isLeakDetectorEnabled() {
        return Context.getSettings() == null
                || !Boolean.FALSE.equals(Context.getSettings().getEnableLeakDetector());
    }

    private void onCommit() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Commit message is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Context.getGitRepoService().commit(message, null, null);
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

    /** Colours findings rows by confidence level. */
    private static class FindingsCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            /*if (!isSelected) {
                String confidence = String.valueOf(table.getValueAt(row, 3));
                c.setBackground(switch (confidence) {
                    case "CRITICAL" -> CRITICAL_BG;
                    case "HIGH"     -> HIGH_BG;
                    case "MEDIUM"   -> MEDIUM_BG;
                    default         -> table.getBackground();
                });
                c.setBackground(table.getBackground());
            }*/
            return c;
        }
    }
}
