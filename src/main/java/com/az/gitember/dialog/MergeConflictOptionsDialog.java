package com.az.gitember.dialog;

import com.az.gitember.ui.misc.Util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Shows next-step options after a merge stops with conflicts.
 */
public class MergeConflictOptionsDialog extends JDialog {

    public enum Option {
        MANUAL,
        USE_OURS,
        USE_THEIRS,
        ABORT
    }

    private Option selectedOption = Option.MANUAL;

    public MergeConflictOptionsDialog(Component parent, List<String> conflictedFiles) {
        super(SwingUtilities.getWindowAncestor(parent), "Merge Conflicts",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(620, 420);
        setLocationRelativeTo(parent);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JLabel titleLabel = new JLabel(conflictedFiles.size()
                + " conflicted file(s). Choose how to continue.");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel hintLabel = new JLabel("Manual resolving keeps the merge in progress and opens the working copy.");
        headerPanel.add(hintLabel, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Conflicted file"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String file : conflictedFiles) {
            model.addRow(new Object[]{file});
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Files"));

        JButton manualBtn = new JButton("Resolve manually");
        manualBtn.addActionListener(e -> choose(Option.MANUAL));

        JButton oursBtn = new JButton("Use ours for all");
        oursBtn.addActionListener(e -> choose(Option.USE_OURS));

        JButton theirsBtn = new JButton("Use theirs for all");
        theirsBtn.addActionListener(e -> choose(Option.USE_THEIRS));

        JButton abortBtn = new JButton("Abort merge");
        abortBtn.addActionListener(e -> choose(Option.ABORT));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.add(manualBtn);
        btnPanel.add(oursBtn);
        btnPanel.add(theirsBtn);
        btnPanel.add(abortBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(manualBtn);
        Util.bindEscapeToDispose(this);
    }

    public Option getSelectedOption() {
        return selectedOption;
    }

    private void choose(Option option) {
        selectedOption = option;
        dispose();
    }
}
