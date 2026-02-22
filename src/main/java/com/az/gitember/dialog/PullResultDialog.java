package com.az.gitember.dialog;

import com.az.gitember.data.PullOperationResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog that shows the result of a pull operation:
 * added, deleted, and changed files in a colour-coded table.
 */
public class PullResultDialog extends JDialog {

    private static final Color COLOR_ADDED   = new Color(210, 245, 210);
    private static final Color COLOR_DELETED = new Color(250, 210, 210);
    private static final Color COLOR_CHANGED = new Color(210, 225, 250);

    public PullResultDialog(Component parent, PullOperationResult result) {
        super(SwingUtilities.getWindowAncestor(parent), "Pull Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 440);
        setLocationRelativeTo(parent);

        // ---- header ----
        JPanel headerPanel = new JPanel(new BorderLayout(6, 6));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));

        JLabel titleLabel = new JLabel("Pull completed — " + result.getStatus());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // summary badges
        if (result.hasChanges()) {
            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            badges.add(makeBadge("+" + result.getAddedFiles().size()   + "  added",   COLOR_ADDED));
            badges.add(makeBadge("-" + result.getDeletedFiles().size() + "  deleted", COLOR_DELETED));
            badges.add(makeBadge("~" + result.getChangedFiles().size() + "  changed", COLOR_CHANGED));
            headerPanel.add(badges, BorderLayout.CENTER);
        } else {
            JLabel noChanges = new JLabel("No file changes.");
            noChanges.setForeground(UIManager.getColor("Label.disabledForeground"));
            headerPanel.add(noChanges, BorderLayout.CENTER);
        }

        // ---- table ----
        DefaultTableModel model = new DefaultTableModel(new String[]{"File", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        addRows(model, result.getAddedFiles(),   "Added");
        addRows(model, result.getDeletedFiles(), "Deleted");
        addRows(model, result.getChangedFiles(), "Changed");

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(switch ((String) getValueAt(row, 1)) {
                        case "Added"   -> COLOR_ADDED;
                        case "Deleted" -> COLOR_DELETED;
                        default        -> COLOR_CHANGED;
                    });
                }
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // center-align the Status column
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(72);
        table.getColumnModel().getColumn(1).setMaxWidth(90);

        JScrollPane scroll = new JScrollPane(table);

        // ---- buttons ----
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        // ---- layout ----
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
    }

    private static void addRows(DefaultTableModel model, List<String> files, String status) {
        for (String f : files) {
            model.addRow(new Object[]{f, status});
        }
    }

    private static JLabel makeBadge(String text, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        return lbl;
    }
}
