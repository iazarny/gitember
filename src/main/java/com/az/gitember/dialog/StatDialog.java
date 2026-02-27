package com.az.gitember.dialog;

import com.az.gitember.data.ScmStat;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
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
 * Statistics window (JFrame – maximizable).
 *
 * <p>Layout (BorderLayout):</p>
 * <ul>
 *   <li>NORTH  – control bar: depth spinner + Compute button + month progress bar</li>
 *   <li>CENTER – JSplitPane: scalable pie chart (left) | summary table (right)</li>
 *   <li>SOUTH  – monthly stacked-bar chart (~220 px)</li>
 * </ul>
 * Status and progress messages are routed to the application's main {@link StatusBar}.
 */
public class StatDialog extends JFrame {

    private static final Logger log = Logger.getLogger(StatDialog.class.getName());

    private final JSpinner     depthSpinner;
    private final JButton      computeBtn;
    private final JProgressBar progressBar;   // month-level progress inside the dialog

    private final PieChartPanel        pieChart;
    private final StatTableModel       tableModel;
    private final JTable               table;
    private final MonthlyBarChartPanel barChart;

    private final StatusBar statusBar;
    private SwingWorker<List<ScmStat>, String> worker;

    public StatDialog(Frame owner, StatusBar statusBar) {
        super("Statistics");
        this.statusBar = statusBar;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                cancelWorker();
                dispose();
            }
        });
        setSize(900, 680);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
        setResizable(true);

        // ── Control bar (no Close button – use window X) ─────────────────────
        depthSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));
        depthSpinner.setPreferredSize(new Dimension(70, depthSpinner.getPreferredSize().height));

        computeBtn = new JButton("Compute");
        computeBtn.addActionListener(e -> startCompute());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controlBar.add(new JLabel("Depth:"));
        controlBar.add(depthSpinner);
        controlBar.add(new JLabel("months"));
        controlBar.add(computeBtn);

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

        // ── Split pane ───────────────────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pieChart, tableScroll);
        splitPane.setDividerLocation(420);
        splitPane.setResizeWeight(0.50);

        // ── Monthly bar chart ─────────────────────────────────────────────────
        barChart = new MonthlyBarChartPanel();
        barChart.setBorder(BorderFactory.createTitledBorder("Monthly Contributions"));
        barChart.setPreferredSize(new Dimension(800, 220));

        // ── South: bar chart above full-width progress bar ────────────────────
        JPanel southPanel = new JPanel(new BorderLayout(0, 2));
        southPanel.add(barChart,    BorderLayout.CENTER);
        southPanel.add(progressBar, BorderLayout.SOUTH);

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.add(controlBar, BorderLayout.NORTH);
        content.add(splitPane,  BorderLayout.CENTER);
        content.add(southPanel, BorderLayout.SOUTH);
        getContentPane().add(content);

        if (Context.getPlotCommitList() == null || Context.getPlotCommitList().isEmpty()) {
            computeBtn.setEnabled(false);
            statusBar.setStatus("Statistics: load commit history first (open History or Working Copy).");
        }
    }

    private void cancelWorker() {
        if (worker != null && !worker.isDone()) worker.cancel(true);
    }

    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void startCompute() {
        List<PlotCommit> all = Context.getPlotCommitList();
        if (all == null || all.isEmpty()) {
            statusBar.setStatus("Statistics: no commit history loaded.");
            return;
        }

        int depth = (int) depthSpinner.getValue();
        computeBtn.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("0 / " + depth);
        statusBar.setStatus("Computing statistics…");
        statusBar.showProgress(true);

        worker = new SwingWorker<>() {

            @Override
            protected List<ScmStat> doInBackground() throws Exception {
                @SuppressWarnings("unchecked")
                List<PlotCommit<PlotLane>> monthly =
                        Context.getGitRepoService().getLastCommitPerMonth(depth, all);

                if (monthly.isEmpty()) {
                    publish("No commits found.");
                    return Collections.emptyList();
                }

                int total = monthly.size();

                ProgressMonitor monitor = new ProgressMonitor() {
                    @Override public void start(int n) {}
                    @Override public void beginTask(String t, int n) {}
                    @Override public void update(int n) {}
                    @Override public void endTask() {}
                    @Override public boolean isCancelled() { return Thread.currentThread().isInterrupted(); }
                    @Override public void showDuration(boolean e) {}
                };

                List<ScmStat> stats = new ArrayList<>();
                for (int i = 0; i < monthly.size(); i++) {
                    if (isCancelled()) break;
                    List<PlotCommit<PlotLane>> single = Collections.singletonList(monthly.get(i));
                    stats.addAll(Context.getGitRepoService().blameList(single, monitor));

                    final int idx = i + 1;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setMaximum(total);
                        progressBar.setValue(idx);
                        progressBar.setString(idx + " / " + total);
                    });
                    publish(idx + " / " + total + " months…");
                }
                return stats;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty())
                    statusBar.setStatus("Statistics: " + chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                computeBtn.setEnabled(true);
                try {
                    List<ScmStat> stats = get();
                    applyResults(stats);
                    progressBar.setString("Done");
                    statusBar.setStatus("Statistics: complete.");
                } catch (java.util.concurrent.CancellationException ex) {
                    progressBar.setString("Cancelled");
                    statusBar.setStatus("Statistics: cancelled.");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Statistics computation failed", ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    progressBar.setString("Error");
                    statusBar.setStatus("Statistics error: " + cause.getMessage());
                } finally {
                    statusBar.clearProgress();
                }
            }
        };
        worker.execute();
    }

    private void applyResults(List<ScmStat> stats) {
        if (stats == null || stats.isEmpty()) return;

        ScmStat headStat = stats.get(stats.size() - 1);

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(headStat.getTotalLines().entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> authors = sorted.stream().map(Map.Entry::getKey).collect(Collectors.toList());

        Color[] colors = PieChartPanel.buildColors(authors.size());

        Map<String, Integer> lines = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : sorted) lines.put(e.getKey(), e.getValue());
        pieChart.setData(lines, colors);

        tableModel.setData(headStat, authors);
        barChart.setData(stats, authors, colors);
    }

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
