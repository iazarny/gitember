package com.az.gitember.ui;

import com.az.gitember.service.Context;
import org.eclipse.jgit.diff.DiffEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Window that shows all files differing between two branches.
 * Double-clicking a row opens a side-by-side file diff viewer.
 */
public class BranchDiffWindow extends JFrame {

    private static final Logger log = Logger.getLogger(BranchDiffWindow.class.getName());

    // Colors are computed at call time so they adapt to the active theme
    private static Color statusBg(String label) {
        boolean dark = SyntaxStyleUtil.isDarkTheme();
        return switch (label) {
            case "Added"   -> dark ? new Color( 20,  80,  20) : new Color(210, 245, 210);
            case "Deleted" -> dark ? new Color( 90,  20,  20) : new Color(250, 210, 210);
            case "Renamed" -> dark ? new Color( 80,  65,  10) : new Color(255, 245, 200);
            default        -> dark ? new Color( 20,  45,  90) : new Color(210, 225, 250);
        };
    }

    private static Color statusFg(String label) {
        if (!SyntaxStyleUtil.isDarkTheme()) {
            return UIManager.getColor("Table.foreground");
        }
        return switch (label) {
            case "Added"   -> new Color(120, 220, 120);
            case "Deleted" -> new Color(220, 120, 120);
            case "Renamed" -> new Color(220, 200,  90);
            default        -> new Color(120, 170, 255);
        };
    }

    private final String branchARef;   // "old" / left branch
    private final String branchBRef;   // "new" / right branch
    private final String branchALabel;
    private final String branchBLabel;

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel statusLabel;
    private List<DiffEntry> entries;

    public BranchDiffWindow(Component parent, String branchARef, String branchBRef) {
        this.branchARef   = branchARef;
        this.branchBRef   = branchBRef;
        this.branchALabel = shortName(branchARef);
        this.branchBLabel = shortName(branchBRef);

        setTitle("Branch Diff: " + branchALabel + " ↔ " + branchBLabel);
        setSize(750, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ---- table ----
        model = new DefaultTableModel(new String[]{"File", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String st = (String) getValueAt(row, 1);
                    c.setBackground(statusBg(st));
                    c.setForeground(statusFg(st));
                }
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(20);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedFileDiff();
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        // ---- header ----
        JLabel header = new JLabel("  " + branchALabel + "  ↔  " + branchBLabel);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setBorder(BorderFactory.createEmptyBorder(6, 6, 4, 6));

        // ---- status / legend ----
        statusLabel = new JLabel("Loading…");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        legendPanel.add(makeBadge("Added"));
        legendPanel.add(makeBadge("Deleted"));
        legendPanel.add(makeBadge("Modified"));
        legendPanel.add(makeBadge("Renamed"));

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.add(legendPanel,  BorderLayout.WEST);
        bottomBar.add(statusLabel,  BorderLayout.EAST);

        // ---- buttons ----
        JButton viewBtn = new JButton("View Diff");
        viewBtn.addActionListener(e -> openSelectedFileDiff());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(viewBtn);
        btnPanel.add(closeBtn);

        // ---- layout ----
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(bottomBar, BorderLayout.NORTH);
        southPanel.add(btnPanel,  BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(closeBtn);

        // kick off background load
        loadDiff();
    }

    private void loadDiff() {
        statusLabel.setText("Computing diff…");
        new SwingWorker<List<DiffEntry>, Void>() {
            @Override
            protected List<DiffEntry> doInBackground() throws Exception {
                return Context.getGitRepoService().getBranchDiff(branchARef, branchBRef);
            }

            @Override
            protected void done() {
                try {
                    entries = get();
                    model.setRowCount(0);
                    for (DiffEntry e : entries) {
                        model.addRow(new Object[]{displayPath(e), statusLabel(e.getChangeType())});
                    }
                    statusLabel.setText(entries.size() + " file" + (entries.size() == 1 ? "" : "s") + " differ");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Branch diff failed", ex);
                    statusLabel.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(BranchDiffWindow.this,
                            "Cannot compute branch diff:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openSelectedFileDiff() {
        int row = table.getSelectedRow();
        if (row < 0 || entries == null || row >= entries.size()) return;

        DiffEntry entry = entries.get(row);
        String filePath = displayPath(entry);

        statusLabel.setText("Loading file…");
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String leftContent  = Context.getGitRepoService().getFileContentAtRef(branchARef, leftPath(entry));
                String rightContent = Context.getGitRepoService().getFileContentAtRef(branchBRef, rightPath(entry));
                return new String[]{leftContent, rightContent};
            }

            @Override
            protected void done() {
                statusLabel.setText(entries.size() + " files differ");
                try {
                    String[] contents = get();
                    DiffViewerWindow viewer = new DiffViewerWindow(
                            filePath,
                            branchALabel, contents[0],
                            branchBLabel, contents[1]);
                    viewer.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open file diff", ex);
                    JOptionPane.showMessageDialog(BranchDiffWindow.this,
                            "Cannot open file diff:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ---- helpers ----

    private static String shortName(String ref) {
        if (ref == null) return "";
        if (ref.startsWith("refs/heads/"))  return ref.substring("refs/heads/".length());
        if (ref.startsWith("refs/remotes/")) return ref.substring("refs/remotes/".length());
        if (ref.startsWith("refs/tags/"))   return ref.substring("refs/tags/".length());
        return ref;
    }

    private static String displayPath(DiffEntry e) {
        if (e.getChangeType() == DiffEntry.ChangeType.DELETE) return e.getOldPath();
        return e.getNewPath();
    }

    private static String leftPath(DiffEntry e) {
        String p = e.getOldPath();
        return DiffEntry.DEV_NULL.equals(p) ? e.getNewPath() : p;
    }

    private static String rightPath(DiffEntry e) {
        String p = e.getNewPath();
        return DiffEntry.DEV_NULL.equals(p) ? e.getOldPath() : p;
    }

    private static String statusLabel(DiffEntry.ChangeType type) {
        return switch (type) {
            case ADD    -> "Added";
            case DELETE -> "Deleted";
            case RENAME -> "Renamed";
            default     -> "Modified";
        };
    }

    private static JLabel makeBadge(String label) {
        Color bg = statusBg(label);
        Color fg = statusFg(label);
        JLabel lbl = new JLabel(label);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(1, 5, 1, 5)));
        return lbl;
    }
}
