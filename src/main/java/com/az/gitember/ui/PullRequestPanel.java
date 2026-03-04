package com.az.gitember.ui;

import com.az.gitember.data.PullRequest;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel shown when a Pull Request node is selected in the tree.
 * Displays PR metadata and the list of files changed in the PR,
 * colour-coded by status (added / deleted / changed).
 */
public class PullRequestPanel extends JPanel {

    private static final Logger log = Logger.getLogger(PullRequestPanel.class.getName());

    // Header fields
    private final JTextField titleField  = readOnlyField();
    private final JTextField authorField = readOnlyField();
    private final JTextField stateField  = readOnlyField();
    private final JTextField branchField = readOnlyField();
    private final JButton    openUrlBtn  = new JButton("Open in Browser");

    // Files area
    private final FilesTableModel filesModel  = new FilesTableModel();
    private final JTable          filesTable;
    private final JPanel          badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JLabel          statusLabel = new JLabel();

    private PullRequest currentPr;

    public PullRequestPanel() {
        filesTable = buildFilesTable();

        setLayout(new BorderLayout());
        add(buildHeader(),     BorderLayout.NORTH);
        add(buildFilesPanel(), BorderLayout.CENTER);
    }

    // ---- table construction ----

    private JTable buildFilesTable() {
        JTable table = new JTable(filesModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String cat = filesModel.categoryAt(row);
                    c.setBackground(colorFor(cat));
                    c.setForeground(fgFor(cat));
                }
                return c;
            }
        };

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status column: narrow + centred
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(72);
        table.getColumnModel().getColumn(1).setMaxWidth(90);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) openFileDiff(filesModel.getItemAt(row));
                }
            }
        });

        return table;
    }

    // ---- panel layout ----

    private JPanel buildFilesPanel() {
        JLabel heading = new JLabel("  Changed files");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        heading.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel headRow = new JPanel(new BorderLayout());
        headRow.add(heading,     BorderLayout.WEST);
        headRow.add(statusLabel, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.add(headRow,     BorderLayout.NORTH);
        top.add(badgesPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top,                        BorderLayout.NORTH);
        panel.add(new JScrollPane(filesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(2, 2, 2, 6);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(2, 0, 2, 10);

        // Row 0: Title (spans all columns)
        lc.gridx = 0; lc.gridy = 0;
        panel.add(new JLabel("Title:"), lc);
        fc.gridx = 1; fc.gridy = 0; fc.gridwidth = 5;
        panel.add(titleField, fc);
        fc.gridwidth = 1;

        // Row 1: Author | State | Branches
        lc.gridx = 0; lc.gridy = 1; panel.add(new JLabel("Author:"),   lc);
        fc.gridx = 1; fc.gridy = 1; panel.add(authorField, fc);
        lc.gridx = 2; lc.gridy = 1; panel.add(new JLabel("State:"),    lc);
        fc.gridx = 3; fc.gridy = 1; panel.add(stateField,  fc);
        lc.gridx = 4; lc.gridy = 1; panel.add(new JLabel("Branches:"), lc);
        fc.gridx = 5; fc.gridy = 1; panel.add(branchField, fc);

        // Row 2: Open button
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 2; bc.gridwidth = 6;
        bc.anchor = GridBagConstraints.WEST;
        bc.insets = new Insets(4, 0, 2, 0);
        openUrlBtn.setEnabled(false);
        openUrlBtn.addActionListener(e -> openInBrowser());
        panel.add(openUrlBtn, bc);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, panel.getBackground().darker()),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return panel;
    }

    // ---- public API ----

    public void showPullRequest(PullRequest pr) {
        this.currentPr = pr;
        filesModel.clear();
        badgesPanel.removeAll();
        badgesPanel.revalidate();
        badgesPanel.repaint();

        if (pr == null) {
            titleField.setText("");
            authorField.setText("");
            stateField.setText("");
            branchField.setText("");
            statusLabel.setText("");
            openUrlBtn.setEnabled(false);
            return;
        }

        titleField.setText("#" + pr.number() + "  " + pr.title());
        authorField.setText(pr.author()      != null ? pr.author()      : "");
        stateField.setText(pr.state()        != null ? pr.state()       : "");
        branchField.setText(pr.sourceBranch() + "  →  " + pr.targetBranch());
        openUrlBtn.setEnabled(pr.webUrl() != null && !pr.webUrl().isBlank());
        statusLabel.setText("Loading…");

        loadChangedFiles(pr);
    }

    // ---- background loading ----

    private void loadChangedFiles(PullRequest pr) {
        new SwingWorker<List<ScmItem>, Void>() {
            @Override
            protected List<ScmItem> doInBackground() throws Exception {
                return Context.getGitRepoService()
                        .getPrChangedFiles(pr.sourceBranch(), pr.targetBranch());
            }

            @Override
            protected void done() {
                try {
                    List<ScmItem> items = get();
                    filesModel.setData(items);
                    updateBadges(items);
                    statusLabel.setText("");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load PR files", ex);
                    statusLabel.setText("Could not load files: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateBadges(List<ScmItem> items) {
        int added = 0, deleted = 0, changed = 0;
        for (ScmItem item : items) {
            switch (filesModel.categoryOf(item)) {
                case "Added"   -> added++;
                case "Deleted" -> deleted++;
                default        -> changed++;
            }
        }

        badgesPanel.removeAll();
        if (added   > 0) badgesPanel.add(makeBadge("+" + added   + "  added",   SyntaxStyleUtil.rowBgAdded(),   SyntaxStyleUtil.rowFgAdded()));
        if (deleted > 0) badgesPanel.add(makeBadge("-" + deleted  + "  deleted", SyntaxStyleUtil.rowBgDeleted(), SyntaxStyleUtil.rowFgDeleted()));
        if (changed > 0) badgesPanel.add(makeBadge("~" + changed  + "  changed", SyntaxStyleUtil.rowBgChanged(), SyntaxStyleUtil.rowFgChanged()));
        if (added == 0 && deleted == 0 && changed == 0) {
            JLabel none = new JLabel("  No file changes.");
            none.setForeground(UIManager.getColor("Label.disabledForeground"));
            badgesPanel.add(none);
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
    }

    // ---- diff viewer ----

    private void openFileDiff(ScmItem item) {
        if (item == null || currentPr == null) return;
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String targetContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.targetBranch(), item.getShortName());
                String sourceContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.sourceBranch(), item.getShortName());
                return new String[]{targetContent, sourceContent};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    DiffViewerWindow w = new DiffViewerWindow(
                            item.getShortName(),
                            currentPr.targetBranch() + " → " + currentPr.sourceBranch(),
                            texts[0], texts[1]);
                    w.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to show PR file diff", ex);
                    JOptionPane.showMessageDialog(PullRequestPanel.this,
                            "Cannot show diff: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ---- browser ----

    private void openInBrowser() {
        if (currentPr == null || currentPr.webUrl() == null) return;
        try {
            Desktop.getDesktop().browse(new URI(currentPr.webUrl()));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot open browser", ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot open browser: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- helpers ----

    private static Color colorFor(String category) {
        return switch (category) {
            case "Added"   -> SyntaxStyleUtil.rowBgAdded();
            case "Deleted" -> SyntaxStyleUtil.rowBgDeleted();
            default        -> SyntaxStyleUtil.rowBgChanged();
        };
    }

    private static Color fgFor(String category) {
        return switch (category) {
            case "Added"   -> SyntaxStyleUtil.rowFgAdded();
            case "Deleted" -> SyntaxStyleUtil.rowFgDeleted();
            default        -> SyntaxStyleUtil.rowFgChanged();
        };
    }

    private static JLabel makeBadge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        return lbl;
    }

private static JTextField readOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        return f;
    }

    // ---- table model ----

    private static class FilesTableModel extends AbstractTableModel {
        private static final String[] COLS = {"File", "Status"};
        private List<ScmItem> items = new ArrayList<>();

        void setData(List<ScmItem> data) {
            items = data != null ? new ArrayList<>(data) : new ArrayList<>();
            fireTableDataChanged();
        }

        void clear() {
            items = new ArrayList<>();
            fireTableDataChanged();
        }

        ScmItem getItemAt(int row) {
            return row >= 0 && row < items.size() ? items.get(row) : null;
        }

        String categoryAt(int row) {
            return row >= 0 && row < items.size() ? categoryOf(items.get(row)) : "Changed";
        }

        String categoryOf(ScmItem item) {
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            if (ScmItem.Status.ADDED.equals(status))   return "Added";
            if (ScmItem.Status.REMOVED.equals(status)) return "Deleted";
            return "Changed";
        }

        @Override public int    getRowCount()              { return items.size(); }
        @Override public int    getColumnCount()           { return COLS.length; }
        @Override public String getColumnName(int col)     { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ScmItem item = items.get(row);
            return switch (col) {
                case 0 -> item.getViewRepresentation();
                case 1 -> categoryOf(item);
                default -> "";
            };
        }
    }
}
