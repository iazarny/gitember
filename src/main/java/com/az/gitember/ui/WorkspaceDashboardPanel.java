package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Aggregated overview of every repository in a {@link Workspace}: a summary header and a
 * per-repository table. Shown when the workspace (root) node is selected in the tree.
 *
 * <p>Deliberately data-driven so it can grow without structural change — append a
 * {@link Column} to surface new per-repository information, or a {@link Summary} for a new
 * aggregate figure. Values that are not computed yet render the {@value #PLACEHOLDER}
 * placeholder.
 */
public class WorkspaceDashboardPanel extends JPanel {

    static final String PLACEHOLDER = "—";

    private record Column(String title, Function<Project, Object> value) {}

    private record Summary(String label, Function<Workspace, Object> value) {}

    private final List<Column> columns = List.of(
            new Column("Repository", p -> new File(nz(p.getProjectHomeFolder())).getName()),
            new Column("Branch",     p -> PLACEHOLDER),
            new Column("Status",     p -> PLACEHOLDER),
            new Column("Modified",   p -> PLACEHOLDER),
            new Column("Ahead",      p -> PLACEHOLDER),
            new Column("Behind",     p -> PLACEHOLDER),
            new Column("Last Fetch", p -> PLACEHOLDER)
    );

    private final List<Summary> summaries = List.of(
            new Summary("Repositories", ws -> ws.getProjects().size()),
            new Summary("Modified",     ws -> PLACEHOLDER),
            new Summary("Ahead",        ws -> PLACEHOLDER),
            new Summary("Behind",       ws -> PLACEHOLDER),
            new Summary("Conflicts",    ws -> PLACEHOLDER),
            new Summary("Last Fetch",   ws -> PLACEHOLDER)
    );

    private final JLabel titleLabel = new JLabel("Workspace");
    private final JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
    private final RepoTableModel tableModel = new RepoTableModel();
    private final JTable table = new JTable(tableModel);

    private Workspace workspace;

    public WorkspaceDashboardPanel() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        refresh();
    }

    public void refresh() {
        if (workspace == null) {
            titleLabel.setText("Workspace");
            metricsPanel.removeAll();
            tableModel.setRows(List.of());
        } else {
            titleLabel.setText("Workspace: " + workspace.getName());
            rebuildMetrics();
            tableModel.setRows(new ArrayList<>(workspace.getProjects()));
        }
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }

    // ── UI construction ────────────────────────────────────────────────────────

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        metricsPanel.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.NORTH);
        top.add(metricsPanel, BorderLayout.CENTER);

        header.add(top, BorderLayout.CENTER);
        header.add(new JSeparator(), BorderLayout.SOUTH);
        return header;
    }

    private void rebuildMetrics() {
        metricsPanel.removeAll();
        for (Summary summary : summaries) {
            metricsPanel.add(buildMetric(summary.label(), String.valueOf(summary.value().apply(workspace))));
        }
    }

    private JComponent buildMetric(String label, String value) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 16f));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        nameLabel.setFont(nameLabel.getFont().deriveFont(11f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(valueLabel);
        cell.add(nameLabel);
        return cell;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    // ── Table model ──────────────────────────────────────────────────────────────

    private class RepoTableModel extends AbstractTableModel {
        private List<Project> rows = new ArrayList<>();

        void setRows(List<Project> rows) {
            this.rows = rows != null ? rows : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.size(); }
        @Override public String getColumnName(int column) { return columns.get(column).title(); }
        @Override public boolean isCellEditable(int row, int column) { return false; }

        @Override
        public Object getValueAt(int row, int column) {
            return columns.get(column).value().apply(rows.get(row));
        }
    }
}
