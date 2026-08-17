package com.az.gitember.dialog;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmItem;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Shows unresolved conflicted files grouped by project, so a workspace commit
 * can be blocked until every repository is conflict-free.
 */
public class ConflictedFilesDialog extends JDialog {

    public ConflictedFilesDialog(Component parent, Map<Project, List<ScmItem>> conflictedFiles) {
        super(SwingUtilities.getWindowAncestor(parent), "Unresolved Conflicts",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(parent);

        long fileCount = conflictedFiles.values().stream().mapToLong(List::size).sum();
        long repoCount = conflictedFiles.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .count();

        JLabel titleLabel = new JLabel(fileCount + " unresolved conflict(s) in " + repoCount
                + " repositor" + (repoCount == 1 ? "y" : "ies") + ". Resolve them before committing.");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Repo", "File"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Map.Entry<Project, List<ScmItem>> entry : conflictedFiles.entrySet()) {
            String repoName = new java.io.File(entry.getKey().getProjectHomeFolder()).getName();
            for (ScmItem item : entry.getValue()) {
                tableModel.addRow(new Object[]{repoName, item.getShortName()});
            }
        }
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(0).setMaxWidth(300);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Conflicted files"));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }
}
