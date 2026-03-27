package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.handler.CreateTagHandler;
import com.az.gitember.handler.InteractiveRebaseHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // Search components (merged into main toolbar when entire history is shown)
    private final JTextField searchField = new JTextField(20);
    private boolean          useLucene   = false;
    private Timer            searchDebounce;

    /** Full unfiltered commit list kept for repeated searches. */
    @SuppressWarnings("rawtypes")
    private List<PlotCommit<PlotLane>> allCommits = new ArrayList<>();

    /** Last Lucene search results: revision SHA → set of matched file names. */
    private Map<String, Set<String>> lastSearchResults = new java.util.HashMap<>();

    /** Parameters of the most recent loadHistory call, used to refresh after mutating actions. */
    private String  lastTreeName   = null;
    private boolean lastAllHistory = true;

    public HistoryPanel(StatusBar statusBar) {
        this.statusBar = statusBar;
        setLayout(new BorderLayout());

        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.setMinimumSize(new Dimension(100, 25));
        searchField.setMaximumSize(new Dimension(150, 25));

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
                    ScmRevisionInformation rev = tableModel.getRevisionAt(row);
                    detailPanel.showRevision(rev);
                    if (rev != null && !lastSearchResults.isEmpty()) {
                        detailPanel.setMatchedFiles(
                                lastSearchResults.get(rev.getRevisionFullName()));
                    }
                }
            }
        });

        // ── Commit context menu ───────────────────────────────────────────
        JPopupMenu commitMenu = new JPopupMenu();
        JMenuItem checkoutItem         = new JMenuItem("Checkout");
        JMenuItem checkoutAsItem       = new JMenuItem("Checkout as…");
        JMenuItem createTagItem        = new JMenuItem("Create tag …");
        JMenuItem cherryPickItem       = new JMenuItem("Cherry-pick…");
        JMenuItem revertItem           = new JMenuItem("Revert commit");
        JMenuItem resetItem            = new JMenuItem("Reset to commit…");
        JMenuItem interactiveRebaseItem = new JMenuItem("Interactive Rebase onto here…");

        checkoutItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            runCommitAction("Checkout", () -> {
                Context.getGitRepoService().checkoutRevCommit(commit, null);
                Context.updateBranches();
                Context.updateWorkingBranch();
                return "Checked out " + commit.name().substring(0, 7);
            }, true);
        });

        checkoutAsItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String branchName = JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "New branch name:", "Checkout as Branch",
                    JOptionPane.PLAIN_MESSAGE);
            if (branchName == null || branchName.isBlank()) return;
            runCommitAction("Checkout as branch", () -> {
                Context.getGitRepoService().checkoutRevCommit(commit, branchName.trim(), null);
                Context.updateBranches();
                Context.updateWorkingBranch();
                return "Created and checked out branch '" + branchName.trim() + "'";
            }, true);
        });

        createTagItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            CreateTagHandler.showAndExecute(
                    SwingUtilities.getWindowAncestor(this), statusBar, commit.getName());
        });

        cherryPickItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            int ok = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Cherry-pick commit " + commit.name().substring(0, 7) + "?",
                    "Cherry-pick", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            runCommitAction("Cherry-pick", () -> {
                Context.getGitRepoService().cherryPick((RevCommit) commit);
                return "Cherry-picked " + commit.name().substring(0, 7);
            });
        });

        revertItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            int ok = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Revert commit " + commit.name().substring(0, 7) + "?",
                    "Revert commit", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            runCommitAction("Revert", () -> {
                Context.getGitRepoService().revertCommit((RevCommit) commit, null);
                return "Reverted " + commit.name().substring(0, 7);
            }, true);
        });

        resetItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String[] modes = {"Soft", "Mixed", "Hard"};
            String choice = (String) JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Reset mode:", "Reset to Commit",
                    JOptionPane.PLAIN_MESSAGE, null, modes, "Mixed");
            if (choice == null) return;
            ResetCommand.ResetType mode = switch (choice) {
                case "Soft"  -> ResetCommand.ResetType.SOFT;
                case "Hard"  -> ResetCommand.ResetType.HARD;
                default      -> ResetCommand.ResetType.MIXED;
            };
            runCommitAction("Reset", () -> {
                Context.getGitRepoService().resetBranch((RevCommit) commit, mode, null);
                return "Reset (" + choice + ") to " + commit.name().substring(0, 7);
            }, true);
        });

        interactiveRebaseItem.setToolTipText(
                "Interactively rebase all commits from HEAD down to (but not including) this commit");
        interactiveRebaseItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String fullSha    = commit.getName();
            String displaySha = fullSha.substring(0, 7);
            // Pass a callback so history reloads only after the rebase completes
            InteractiveRebaseHandler.showAndExecute(
                    SwingUtilities.getWindowAncestor(this), statusBar,
                    fullSha, displaySha,
                    () -> loadHistory(lastTreeName, lastAllHistory));
        });

        commitMenu.add(checkoutItem);
        commitMenu.add(checkoutAsItem);
        commitMenu.addSeparator();
        commitMenu.add(createTagItem);
        commitMenu.addSeparator();
        commitMenu.add(cherryPickItem);
        commitMenu.addSeparator();
        commitMenu.add(revertItem);
        commitMenu.add(resetItem);
        commitMenu.addSeparator();
        commitMenu.add(interactiveRebaseItem);

        commitTable.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent ev)  { maybeShowMenu(ev); }
            @Override public void mouseReleased(MouseEvent ev) { maybeShowMenu(ev); }

            private void maybeShowMenu(MouseEvent ev) {
                if (!ev.isPopupTrigger()) return;
                int row = commitTable.rowAtPoint(ev.getPoint());
                if (row < 0) return;
                PlotCommit<PlotLane> commit = tableModel.getCommitAt(row);
                if (commit == null) return; // file-history mode
                commitTable.setRowSelectionInterval(row, row);
                boolean unpushed = false;
                try {
                    unpushed = Context.getGitRepoService().isCommitUnpushed(commit.getName());
                } catch (Exception ignored) {}
                interactiveRebaseItem.setEnabled(unpushed);
                commitMenu.show(commitTable, ev.getX(), ev.getY());
            }
        });

        // ── Search toolbar ────────────────────────────────────────────────
        searchField.putClientProperty("JTextField.placeholderText",
                "Search commits (message, author, SHA, file name…)");

        // Auto-search: trigger 400 ms after the user stops typing (min 3 chars)
        searchDebounce = new Timer(400, e -> performSearch());
        searchDebounce.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchTextChanged(); }
        });

        // ── Layout ────────────────────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(commitTable), detailPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.65);

        add(splitPane, BorderLayout.CENTER);

        // Reload history whenever a pull (or other background operation) requests it
        Context.addPropertyChangeListener(Context.PROP_HISTORY_REFRESH, evt ->
                SwingUtilities.invokeLater(() -> {
                    if (lastTreeName != null) {
                        loadHistory(lastTreeName, lastAllHistory);
                    }
                })
        );
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
            lastSearchResults = new java.util.HashMap<>();
            detailPanel.setMatchedFiles(null);
            tableModel.setData(allCommits);
            statusBar.setStatus(allCommits.size() + " commits loaded");
            return;
        }

        boolean useLucene = this.useLucene;
        List<PlotCommit<PlotLane>> base = new ArrayList<>(allCommits);

        if (base.isEmpty()) {
            statusBar.setStatus("No commits loaded.");
            return;
        }

        statusBar.setStatus("Searching…");

        new SwingWorker<Map<String, Set<String>>, Void>() {
            @Override
            protected Map<String, Set<String>> doInBackground() {
                return Context.getGitRepoService().search((List) base, term, useLucene);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Set<String>> results = get();
                    lastSearchResults = results;
                    List<PlotCommit<PlotLane>> filtered = base.stream()
                            .filter(pc -> results.containsKey(pc.getName()))
                            .toList();
                    tableModel.setData(filtered);
                    statusBar.setStatus(filtered.size() + " commit" +
                            (filtered.size() != 1 ? "s" : "") + " found");
                    // Update highlight for currently selected commit
                    int row = commitTable.getSelectedRow();
                    if (row >= 0 && row < tableModel.getRowCount()) {
                        ScmRevisionInformation rev = tableModel.getRevisionAt(row);
                        if (rev != null) {
                            detailPanel.setMatchedFiles(results.get(rev.getRevisionFullName()));
                        }
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Search failed", ex);
                    statusBar.setStatus("Search error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    public JTextField getSearchField() { return searchField; }

    public void loadHistory(String treeName, boolean allHistory) {
        loadHistory(treeName, allHistory, null);
    }

    /** Loads all history and, once loaded, selects the commit whose SHA starts with {@code sha}. */
    public void loadHistoryAndSelect(String sha) {
        loadHistory(null, true, () -> selectCommitBySha(sha));
    }

    /**
     * Selects the row whose commit SHA starts with {@code sha} and scrolls to it.
     * Works in both full-history (PlotCommit) and file-history (revision) modes.
     */
    public void selectCommitBySha(String sha) {
        if (sha == null || sha.isBlank()) return;
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            PlotCommit<PlotLane> pc = tableModel.getCommitAt(i);
            if (pc != null && pc.getName().startsWith(sha)) {
                commitTable.setRowSelectionInterval(i, i);
                commitTable.scrollRectToVisible(commitTable.getCellRect(i, 0, true));
                return;
            }
            if (!tableModel.revisions.isEmpty() && i < tableModel.revisions.size()) {
                ScmRevisionInformation rev = tableModel.revisions.get(i);
                if (rev != null) {
                    String fullName = rev.getRevisionFullName();
                    if (fullName != null && fullName.startsWith(sha)) {
                        commitTable.setRowSelectionInterval(i, i);
                        commitTable.scrollRectToVisible(commitTable.getCellRect(i, 0, true));
                        return;
                    }
                }
            }
        }
        log.fine("Commit with SHA prefix '" + sha + "' not found in loaded history");
    }

    @SuppressWarnings("unchecked")
    private void loadHistory(String treeName, boolean allHistory, Runnable afterLoad) {
        lastTreeName   = treeName;
        lastAllHistory = allHistory;
        if (!allHistory) {
            searchField.setText("");
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
                    if (afterLoad != null) afterLoad.run();
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

    // ── Context-menu helpers ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private PlotCommit<PlotLane> selectedCommit() {
        int row = commitTable.getSelectedRow();
        return row >= 0 ? tableModel.getCommitAt(row) : null;
    }

    /** Runs a git action on a background thread, showing status bar progress. */
    private void runCommitAction(String label, CommitAction action) {
        runCommitAction(label, action, false);
    }

    private void runCommitAction(String label, CommitAction action, boolean refreshAfter) {
        statusBar.setStatus(label + "…");
        statusBar.showProgress(true);
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return action.run(); }
            @Override protected void done() {
                statusBar.clearProgress();
                try {
                    statusBar.setStatus(get());
                    if (refreshAfter) {
                        loadHistory(lastTreeName, lastAllHistory);
                    }
                } catch (java.util.concurrent.CancellationException ex) {
                    statusBar.setStatus(label + " cancelled.");
                } catch (Exception ex) {
                    log.log(Level.WARNING, label + " failed", ex);
                    CheckoutConflictException conflict = null;
                    for (Throwable t = ex; t != null; t = t.getCause()) {
                        if (t instanceof CheckoutConflictException cce) {
                            conflict = cce;
                            break;
                        }
                    }
                    String message;
                    if (conflict != null && !conflict.getConflictingPaths().isEmpty()) {
                        message = "Checkout blocked by local changes in:\n\n"
                                + String.join("\n", conflict.getConflictingPaths());
                        statusBar.setStatus(label + " failed: checkout conflict in "
                                + conflict.getConflictingPaths().size() + " file(s)");
                    } else {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        message = cause.getMessage();
                        statusBar.setStatus(label + " failed: " + cause.getMessage());
                    }
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(HistoryPanel.this),
                            message, label + " Error",
                            JOptionPane.ERROR_MESSAGE);
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() {
                            Context.updateBranches();
                            Context.updateWorkingBranch();
                            return null;
                        }
                    }.execute();
                }
            }
        }.execute();
    }

    @FunctionalInterface
    private interface CommitAction { String run() throws Exception; }

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
