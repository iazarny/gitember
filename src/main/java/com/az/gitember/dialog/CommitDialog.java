package com.az.gitember.dialog;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

public class CommitDialog extends JDialog {

    private static final Logger log = Logger.getLogger(CommitDialog.class.getName());

    private final JTextArea messageArea;
    private final JTable filesTable;
    private final DefaultTableModel tableModel;

    public CommitDialog(Frame parent) {
        super(parent, "Commit" + (Context.getWorkingBranch() != null
                ? " [" + Context.getWorkingBranch().getShortName() + "]" : ""), true);
        setSize(600, 500);
        setLocationRelativeTo(parent);

        // Files table
        tableModel = new DefaultTableModel(new String[]{"File", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        filesTable = new JTable(tableModel);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        filesTable.getColumnModel().getColumn(1).setMaxWidth(150);

        populateFiles();

        JScrollPane tableScroll = new JScrollPane(filesTable);
        tableScroll.setPreferredSize(new Dimension(0, 200));

        // Message area
        JLabel msgLabel = new JLabel("Commit message:");
        messageArea = new JTextArea(5, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton commitBtn = new JButton("Commit");
        commitBtn.addActionListener(e -> onCommit());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(commitBtn);
        buttonPanel.add(cancelBtn);

        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(tableScroll, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(msgLabel, BorderLayout.NORTH);
        messagePanel.add(msgScroll, BorderLayout.CENTER);
        mainPanel.add(messagePanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(commitBtn);
    }

    private static final java.util.Set<String> STAGED_STATUSES = java.util.Set.of(
            ScmItem.Status.ADDED,
            ScmItem.Status.CHANGED,
            ScmItem.Status.REMOVED,
            ScmItem.Status.RENAMED
    );

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
}
