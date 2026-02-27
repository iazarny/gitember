package com.az.gitember.dialog;

import com.az.gitember.data.ScmStat;
import com.az.gitember.service.Context;
import com.az.gitember.ui.stat.MonthlyBarChartPanel;
import com.az.gitember.ui.stat.PieChartPanel;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Modal Statistics dialog.
 *
 * <p>Layout (BorderLayout):</p>
 * <ul>
 *   <li>NORTH  – control bar: depth spinner + Compute button + progress bar</li>
 *   <li>CENTER – JSplitPane: pie chart (left) | summary table (right)</li>
 *   <li>SOUTH  – monthly stacked-bar chart (fixed ~200 px height)</li>
 * </ul>
 */
public class StatDialog extends JFrame {

    private static final Logger log = Logger.getLogger(StatDialog.class.getName());

    // Controls
    private final JSpinner     depthSpinner;
    private final JButton      computeBtn;
    private final JProgressBar progressBar;
    private final JLabel       statusLabel;

    // Charts / table
    private final PieChartPanel        pieChart;
    private final StatTableModel       tableModel;
    private final JTable               table;
    private final MonthlyBarChartPanel barChart;

    private SwingWorker<List<ScmStat>, String> worker;

    public StatDialog(Frame owner) {
        super("Statistics");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (worker != null && !worker.isDone()) worker.cancel(true);
                dispose();
            }
        });
        setSize(900, 680);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
        setResizable(true);

        // ── Control bar ──────────────────────────────────────────────────────
        depthSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));
        depthSpinner.setPreferredSize(new Dimension(70, depthSpinner.getPreferredSize().height));

        computeBtn = new JButton("Compute");
        computeBtn.addActionListener(e -> startCompute());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setPreferredSize(new Dimension(200, progressBar.getPreferredSize().height));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            if (worker != null && !worker.isDone()) worker.cancel(true);
            dispose();
        });

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controlBar.add(new JLabel("Depth:"));
        controlBar.add(depthSpinner);
        controlBar.add(new JLabel("months"));
        controlBar.add(computeBtn);
        controlBar.add(progressBar);
        controlBar.add(statusLabel);
        controlBar.add(Box.createHorizontalGlue());
        controlBar.add(closeBtn);

        // ── Pie chart ────────────────────────────────────────────────────────
        pieChart = new PieChartPanel();
        pieChart.setBorder(BorderFactory.createTitledBorder("Codebase Ownership (HEAD)"));

        // ── Summary table ────────────────────────────────────────────────────
        tableModel = new StatTableModel();
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Summary"));

        // ── Split pane (pie | table) ─────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pieChart, tableScroll);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.45);

        // ── Monthly bar chart ─────────────────────────────────────────────────
        barChart = new MonthlyBarChartPanel();
        barChart.setBorder(BorderFactory.createTitledBorder("Monthly Contributions"));
        barChart.setPreferredSize(new Dimension(800, 220));

        // ── Main layout ───────────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.add(controlBar, BorderLayout.NORTH);
        content.add(splitPane,  BorderLayout.CENTER);
        content.add(barChart,   BorderLayout.SOUTH);

        getContentPane().add(content);

        // If there are no commits loaded, disable compute
        if (Context.getPlotCommitList() == null || Context.getPlotCommitList().isEmpty()) {
            computeBtn.setEnabled(false);
            statusLabel.setText("Load commit history first (open History or Working Copy).");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SwingWorker
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void startCompute() {
        List<PlotCommit> all = Context.getPlotCommitList();
        if (all == null || all.isEmpty()) {
            statusLabel.setText("No commit history loaded.");
            return;
        }

        int depth = (int) depthSpinner.getValue();
        computeBtn.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("0 / " + depth);
        statusLabel.setText("Starting…");

        worker = new SwingWorker<>() {

            @Override
            protected List<ScmStat> doInBackground() throws Exception {
                // Step 1: pick one commit per month
                @SuppressWarnings("unchecked")
                List<PlotCommit<PlotLane>> monthly =
                        Context.getGitRepoService().getLastCommitPerMonth(depth, all);

                if (monthly.isEmpty()) {
                    publish("No commits found.");
                    return Collections.emptyList();
                }

                int total = monthly.size();

                // Step 2: blame each month snapshot
                ProgressMonitor monitor = new ProgressMonitor() {
                    @Override public void start(int totalTasks) {}
                    @Override public void beginTask(String title, int tTasks) {}
                    @Override public void update(int completed) {}
                    @Override public void endTask() {}
                    @Override public boolean isCancelled() {
                        return Thread.currentThread().isInterrupted();
                    }
                    @Override public void showDuration(boolean enabled) {}
                };

                List<ScmStat> stats = new ArrayList<>();
                for (int i = 0; i < monthly.size(); i++) {
                    if (isCancelled()) break;
                    PlotCommit<PlotLane> pc = monthly.get(i);
                    // blame single commit
                    List<PlotCommit<PlotLane>> single = Collections.singletonList(pc);
                    List<ScmStat> oneMonth = Context.getGitRepoService().blameList(single, monitor);
                    stats.addAll(oneMonth);

                    final int idx = i + 1;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setMaximum(total);
                        progressBar.setValue(idx);
                        progressBar.setString(idx + " / " + total);
                    });
                    publish("Processed " + idx + " / " + total + " months…");
                }
                return stats;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                computeBtn.setEnabled(true);
                try {
                    List<ScmStat> stats = get();
                    applyResults(stats);
                    progressBar.setString("Done");
                    statusLabel.setText("Complete.");
                } catch (java.util.concurrent.CancellationException ex) {
                    progressBar.setString("Cancelled");
                    statusLabel.setText("Cancelled.");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Statistics computation failed", ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    progressBar.setString("Error");
                    statusLabel.setText("Error: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Pushes the computed stats into the chart panels and table. */
    private void applyResults(List<ScmStat> stats) {
        if (stats == null || stats.isEmpty()) return;

        // Head stat = last entry (most recent month)
        ScmStat headStat = stats.get(stats.size() - 1);

        // Build sorted author list (descending by lines at HEAD)
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(headStat.getTotalLines().entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> authors = sorted.stream().map(Map.Entry::getKey).collect(Collectors.toList());

        // Build colour palette (shared between pie and bar)
        Color[] colors = PieChartPanel.buildColors(authors.size());

        // Pie chart
        Map<String, Integer> lines = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : sorted) lines.put(e.getKey(), e.getValue());
        pieChart.setData(lines, colors);

        // Summary table
        tableModel.setData(headStat, authors);

        // Monthly bar chart
        barChart.setData(stats, authors, colors);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Table model
    // ─────────────────────────────────────────────────────────────────────────

    private static final class StatTableModel extends AbstractTableModel {

        private static final String[] COLS = {"Developer", "Commits", "Lines", "Avg Lines/Commit"};

        private List<Object[]> rows = Collections.emptyList();

        void setData(ScmStat stat, List<String> authors) {
            rows = new ArrayList<>();
            for (String author : authors) {
                int lines   = stat.getTotalLines().getOrDefault(author, 0);
                int commits = stat.getLogMap().getOrDefault(author, 0);
                int avg     = commits > 0 ? lines / commits : 0;
                rows.add(new Object[]{author, commits, lines, avg});
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }
        @Override public Class<?> getColumnClass(int col) {
            return col == 0 ? String.class : Integer.class;
        }
        @Override public Object getValueAt(int row, int col) { return rows.get(row)[col]; }
    }
}
