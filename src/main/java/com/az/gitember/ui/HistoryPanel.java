package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HistoryPanel extends JPanel {

    private static final Logger log = Logger.getLogger(HistoryPanel.class.getName());
    private static final int ROW_HEIGHT = 24;

    private final JTable commitTable;
    private final CommitTableModel tableModel;
    private final CommitDetailPanel detailPanel;
    private final StatusBar statusBar;
    private final CommitGraphRenderer graphRenderer = new CommitGraphRenderer();

    // Search toolbar (only shown for entire/all-branch history)
    private final JTextField searchField = new JTextField(25);
    private final JLabel     resultLabel = new JLabel(" ");
    private boolean          useLucene   = false;
    private Timer            searchDebounce;
    private JPanel           searchBar;

    /** Full unfiltered commit list kept for repeated searches. */
    @SuppressWarnings("rawtypes")
    private List<PlotCommit<PlotLane>> allCommits = new ArrayList<>();

    public HistoryPanel(StatusBar statusBar) {
        this.statusBar = statusBar;
        setLayout(new BorderLayout());

        tableModel = new CommitTableModel();
        commitTable = new JTable(tableModel);
        commitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commitTable.setRowHeight(ROW_HEIGHT);
        commitTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        commitTable.setShowVerticalLines(false);
        commitTable.setIntercellSpacing(new Dimension(0, 0));

        // Column widths: Graph+Message | Date | Author
        commitTable.getColumnModel().getColumn(0).setPreferredWidth(600);
        commitTable.getColumnModel().getColumn(0).setCellRenderer(new GraphCellRenderer());
        commitTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        commitTable.getColumnModel().getColumn(1).setMaxWidth(160);
        commitTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        commitTable.getColumnModel().getColumn(2).setMaxWidth(200);

        detailPanel = new CommitDetailPanel();
        commitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = commitTable.getSelectedRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    detailPanel.showRevision(tableModel.getRevisionAt(row));
                }
            }
        });

        // ── Search toolbar ────────────────────────────────────────────────
        searchField.putClientProperty("JTextField.placeholderText",
                "Search commits (message, author, SHA, file name…)");

        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.PLAIN, 11f));

        // Auto-search: trigger 400 ms after the user stops typing (min 3 chars)
        searchDebounce = new Timer(400, e -> performSearch());
        searchDebounce.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchTextChanged(); }
        });

        searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(new JSeparator(SwingConstants.VERTICAL));
        searchBar.add(Box.createHorizontalStrut(8));
        searchBar.add(resultLabel);
        searchBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                UIManager.getColor("Separator.foreground")));

        // ── Layout ────────────────────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(commitTable), detailPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.65);

        add(searchBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    // ── Search logic ──────────────────────────────────────────────────────────

    public void refreshLuceneState() {
        boolean indexed = Context.getCurrentProject()
                .map(Project::isIndexed)
                .orElse(false);
        if (indexed) {
            try {
                indexed = Context.getGitRepoService().getSearchService().hasIndex();
            } catch (Exception ignored) {
                indexed = false;
            }
        }
        useLucene = indexed;
        if (indexed) {
            searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 150, 0), 2),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        } else {
            searchField.setBorder(UIManager.getBorder("TextField.border"));
        }
    }

    private void onSearchTextChanged() {
        searchDebounce.restart();
    }

    @SuppressWarnings("unchecked")
    private void performSearch() {
        String term = searchField.getText().trim();
        if (term.length() < 3) {
            resultLabel.setText(" ");
            tableModel.setData(allCommits);
            return;
        }

        boolean useLucene = this.useLucene;
        List<PlotCommit<PlotLane>> base = new ArrayList<>(allCommits);

        if (base.isEmpty()) {
            resultLabel.setText("No commits loaded.");
            return;
        }

        resultLabel.setText("Searching…");

        new SwingWorker<Map<String, Set<String>>, Void>() {
            @Override
            protected Map<String, Set<String>> doInBackground() {
                return Context.getGitRepoService().search((List) base, term, useLucene);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Set<String>> results = get();
                    List<PlotCommit<PlotLane>> filtered = base.stream()
                            .filter(pc -> results.containsKey(pc.getName()))
                            .toList();
                    tableModel.setData(filtered);
                    resultLabel.setText(filtered.size() + " commit" +
                            (filtered.size() != 1 ? "s" : "") + " found");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Search failed", ex);
                    resultLabel.setText("Search error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    public void loadHistory(String treeName, boolean allHistory) {
        // Search is only meaningful over the entire multi-branch history
        searchBar.setVisible(allHistory);
        if (!allHistory) {
            searchField.setText("");
            resultLabel.setText(" ");
        }

        tableModel.clear();
        allCommits = new ArrayList<>();
        detailPanel.showRevision(null);
        statusBar.setStatus("Loading history...");
        statusBar.showProgress(true);
        if (allHistory) refreshLuceneState();

        SwingWorker<List<PlotCommit<PlotLane>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PlotCommit<PlotLane>> doInBackground() {
                Context.updatePlotCommitList(treeName, allHistory, null);
                List<PlotCommit<PlotLane>> commits = new ArrayList<>();
                for (var pc : Context.getPlotCommitList()) {
                    commits.add((PlotCommit<PlotLane>) pc);
                }
                return commits;
            }

            @Override
            protected void done() {
                try {
                    List<PlotCommit<PlotLane>> commits = get();
                    allCommits = commits;
                    tableModel.setData(commits);
                    statusBar.clearProgress();
                    statusBar.setStatus(commits.size() + " commits loaded");
                    refreshLuceneState();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load history", e);
                    statusBar.clearProgress();
                    statusBar.setStatus("Failed to load history");
                }
            }
        };
        worker.execute();
    }

    /**
     * Load history for a specific file.
     */
    public void loadFileHistory(String fileName) {
        tableModel.clear();
        detailPanel.showRevision(null);
        statusBar.setStatus("Loading file history...");
        statusBar.showProgress(true);

        SwingWorker<List<ScmRevisionInformation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScmRevisionInformation> doInBackground() throws Exception {
                return Context.getGitRepoService().getFileHistory(fileName);
            }

            @Override
            protected void done() {
                try {
                    List<ScmRevisionInformation> revisions = get();
                    // Convert to simple PlotCommit-less display
                    tableModel.setRevisions(revisions);
                    statusBar.clearProgress();
                    statusBar.setStatus(revisions.size() + " revisions loaded for " + fileName);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load file history", e);
                    statusBar.clearProgress();
                    statusBar.setStatus("Failed to load file history");
                }
            }
        };
        worker.execute();
    }

    // Custom cell renderer: graph canvas on the left, commit message label on the right
    private class GraphCellRenderer extends JPanel implements TableCellRenderer {

        private final GraphCanvas graphCanvas = new GraphCanvas();
        private final JLabel messageLabel = new JLabel();

        GraphCellRenderer() {
            setLayout(new BorderLayout(4, 0));
            messageLabel.setVerticalAlignment(SwingConstants.CENTER);
            add(graphCanvas, BorderLayout.WEST);
            add(messageLabel, BorderLayout.CENTER);
            setOpaque(true);
            graphCanvas.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
            Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
            setBackground(bg);
            messageLabel.setFont(table.getFont());
            messageLabel.setForeground(fg);

            PlotCommit<PlotLane> commit = tableModel.getCommitAt(row);
            graphCanvas.setCommit(commit, bg);

            if (commit != null) {
                messageLabel.setText(commit.getShortMessage() != null ? commit.getShortMessage() : "");
            } else {
                // file-history mode: value is already the short-message string
                messageLabel.setText(value != null ? value.toString() : "");
            }

            doLayout();
            return this;
        }
    }

    // Paints only the graph portion (lines, dots, ref labels) for a single commit row
    private class GraphCanvas extends JComponent {

        private PlotCommit<PlotLane> commit;

        GraphCanvas() {
            setOpaque(true);
        }

        void setCommit(PlotCommit<PlotLane> commit, Color bg) {
            this.commit = commit;
            setBackground(bg);
            if (commit != null) {
                // Quick off-screen pass to measure how wide the graph portion is.
                // The image is 1 px wide so no real pixels are written; only the
                // graphWidth side-effect from drawText() is captured.
                BufferedImage probe = new BufferedImage(1, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D mg = probe.createGraphics();
                mg.setFont(commitTable.getFont());
                int w = graphRenderer.render(mg, commit, ROW_HEIGHT);
                mg.dispose();
                setPreferredSize(new Dimension(Math.max(w + 4, 20), ROW_HEIGHT));
            } else {
                setPreferredSize(new Dimension(0, ROW_HEIGHT));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (commit == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            graphRenderer.render(g2, commit, getHeight());
            g2.dispose();
        }
    }

    // Table model backed by PlotCommit list
    static class CommitTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Graph / Message", "Date", "Author"};
        private List<PlotCommit<PlotLane>> commits = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public void setData(List<PlotCommit<PlotLane>> data) {
            this.commits = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        public void clear() {
            commits = new ArrayList<>();
            revisions = new ArrayList<>();
            fireTableDataChanged();
        }

        private List<ScmRevisionInformation> revisions = new ArrayList<>();

        public void setRevisions(List<ScmRevisionInformation> data) {
            this.commits = new ArrayList<>();
            this.revisions = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        public PlotCommit<PlotLane> getCommitAt(int row) {
            return row >= 0 && row < commits.size() ? commits.get(row) : null;
        }

        public ScmRevisionInformation getRevisionAt(int row) {
            if (!revisions.isEmpty()) {
                return row >= 0 && row < revisions.size() ? revisions.get(row) : null;
            }
            PlotCommit<PlotLane> pc = getCommitAt(row);
            if (pc == null) return null;
            return Context.getGitRepoService().adapt(pc, null);
        }

        @Override
        public int getRowCount() {
            return !revisions.isEmpty() ? revisions.size() : commits.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (!revisions.isEmpty()) {
                ScmRevisionInformation rev = revisions.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> rev.getShortMessage() != null ? rev.getShortMessage() : "";
                    case 1 -> rev.getDate() != null ? GitemberUtil.formatDate(rev.getDate()) : "";
                    case 2 -> rev.getAuthorName() != null ? rev.getAuthorName() : "";
                    default -> "";
                };
            }
            PlotCommit<PlotLane> pc = commits.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> pc; // rendered by GraphCellRenderer
                case 1 -> GitemberUtil.formatDate(GitemberUtil.intToDate(pc.getCommitTime()));
                case 2 -> pc.getAuthorIdent() != null ? pc.getAuthorIdent().getName() : "";
                default -> "";
            };
        }
    }
}
