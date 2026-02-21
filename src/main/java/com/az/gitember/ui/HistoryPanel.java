package com.az.gitember.ui;

import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
        commitTable.getColumnModel().getColumn(0).setPreferredWidth(600);  // Graph + message
        commitTable.getColumnModel().getColumn(0).setCellRenderer(new GraphCellRenderer());
        commitTable.getColumnModel().getColumn(1).setPreferredWidth(140);  // Date
        commitTable.getColumnModel().getColumn(1).setMaxWidth(160);
        commitTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Author
        commitTable.getColumnModel().getColumn(2).setMaxWidth(200);

        // Detail panel for selected commit (tabbed: Main + Diff)
        detailPanel = new CommitDetailPanel();

        commitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = commitTable.getSelectedRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    detailPanel.showRevision(tableModel.getRevisionAt(row));
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(commitTable),
                detailPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.65);

        add(splitPane, BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    public void loadHistory(String treeName, boolean allHistory) {
        tableModel.clear();
        detailPanel.showRevision(null);
        statusBar.setStatus("Loading history...");
        statusBar.showProgress(true);

        SwingWorker<List<PlotCommit<PlotLane>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PlotCommit<PlotLane>> doInBackground() {
                Context.updatePlotCommitList(treeName, allHistory, null);
                // Copy the list - PlotCommit objects hold lane/passing lane info needed for graph
                List<PlotCommit<PlotLane>> commits = new ArrayList<>();
                for (var pc : Context.getPlotCommitList()) {
                    commits.add((PlotCommit<PlotLane>) pc);
                }
                // Pre-adapt to cache ScmRevisionInformation
                for (var pc : commits) {
                    Context.getGitRepoService().adapt(pc, null);
                }
                return commits;
            }

            @Override
            protected void done() {
                try {
                    List<PlotCommit<PlotLane>> commits = get();
                    tableModel.setData(commits);
                    statusBar.clearProgress();
                    statusBar.setStatus(commits.size() + " commits loaded");
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

    // Custom cell renderer that draws the commit graph + message
    private class GraphCellRenderer extends JPanel implements TableCellRenderer {

        private PlotCommit<PlotLane> commit;
        private boolean selected;
        private boolean hasFocus;

        GraphCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            this.commit = tableModel.getCommitAt(row);
            this.selected = isSelected;
            this.hasFocus = hasFocus;
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (commit == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            int h = getHeight();
            int w = getWidth();

            // Draw graph into offscreen image (renderer may draw outside our bounds upward/downward)
            BufferedImage graphImage = new BufferedImage(
                    Math.max(w, 800), h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gImg = graphImage.createGraphics();
            gImg.setFont(g2.getFont());
            int graphWidth = graphRenderer.render(gImg, commit, h);
            gImg.dispose();

            // Paint the graph image
            g2.drawImage(graphImage, 0, 0, null);

            // Draw the commit message after the graph
            int textX = Math.max(graphWidth + 4, 10);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String msg = commit.getShortMessage();
            if (msg != null) {
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(msg, textX, textY);
            }

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
