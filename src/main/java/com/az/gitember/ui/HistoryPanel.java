package com.az.gitember.ui;

import com.az.gitember.service.I18n;
import com.az.gitember.data.Project;
import com.az.gitember.data.ScmPlotCommit;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.dialog.InteractiveRebaseDialog;
import com.az.gitember.handler.CreateTagHandler;
import com.az.gitember.handler.InteractiveRebaseHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.beans.PropertyChangeListener;
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
    private final boolean disableDetails;

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

    private final PropertyChangeListener historyRefreshListener;
    private final PropertyChangeListener repoPathListener;

    public HistoryPanel(StatusBar statusBar) {
        this(statusBar, false);
    }

    public HistoryPanel(StatusBar statusBar, boolean disableDetails) {
        this.statusBar = statusBar;
        this.disableDetails = disableDetails;
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
        commitTable.getColumnModel().getColumn(0).setPreferredWidth(800);
        commitTable.getColumnModel().getColumn(0).setMinWidth(800);
        commitTable.getColumnModel().getColumn(0).setCellRenderer(new GraphCellRenderer());
        commitTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        commitTable.getColumnModel().getColumn(1).setMinWidth(180);
        commitTable.getColumnModel().getColumn(1).setMaxWidth(200);
        commitTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        commitTable.getColumnModel().getColumn(2).setMaxWidth(220);
        commitTable.getColumnModel().getColumn(2).setCellRenderer(new AuthorSignatureCellRenderer());

        detailPanel = new CommitDetailPanel(statusBar);
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

        commitTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = commitTable.rowAtPoint(e.getPoint());
                int viewCol = commitTable.columnAtPoint(e.getPoint());
                if (viewRow >= 0 && viewCol == 2 && tableModel.isSignedAt(viewRow)) {
                    Rectangle cell = commitTable.getCellRect(viewRow, viewCol, false);
                    if (e.getX() - cell.x <= 18) {
                        ScmRevisionInformation rev = tableModel.getRevisionAt(viewRow);
                        CommitSignatureDialog.open(SwingUtilities.getWindowAncestor(HistoryPanel.this),
                                rev, () -> commitTable.repaint());
                    }
                }
            }
        });

        // ── Commit context menu ───────────────────────────────────────────
        JPopupMenu commitMenu = new JPopupMenu();
        JMenuItem checkoutItem         = new JMenuItem(I18n.t("branchContextMenuFactory.checkout"));
        JMenuItem checkoutAsItem       = new JMenuItem(I18n.t("checkoutAs"));
        JMenuItem createTagItem        = new JMenuItem(I18n.t("historyPanel.createTag"));
        JMenuItem cherryPickItem       = new JMenuItem(I18n.t("cherryPick"));
        JMenuItem undoItem             = new JMenuItem(I18n.t("undoCommit"));
        JMenuItem revertItem           = new JMenuItem(I18n.t("revertCommit"));
        JMenuItem resetItem            = new JMenuItem(I18n.t("resetToCommit"));
        JMenuItem addNoteItem          = new JMenuItem(I18n.t("addNote"));
        JMenuItem removeNoteItem       = new JMenuItem(I18n.t("removeNote"));
        JMenuItem changeMessageItem      = new JMenuItem(I18n.t("changeCommitMessage"));
        JMenuItem interactiveRebaseItem = new JMenuItem(I18n.t("interactiveRebaseOntoHere"));

        checkoutItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            runCommitAction(I18n.t("branchContextMenuFactory.checkout"), () -> {
                Context.getGitRepoService().checkoutRevCommit(commit, null);
                Context.updateBranches();
                Context.updateWorkingBranch();
                return I18n.t("checkedOut", commit.name().substring(0, 7));
            }, true);
        });

        checkoutAsItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String branchName = JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    I18n.t("newBranchName"), I18n.t("checkoutAsBranch"),
                    JOptionPane.PLAIN_MESSAGE);
            if (branchName == null || branchName.isBlank()) return;
            runCommitAction(I18n.t("historyPanel.checkoutAsBranch"), () -> {
                Context.getGitRepoService().checkoutRevCommit(commit, branchName.trim(), null);
                Context.updateBranches();
                Context.updateWorkingBranch();
                return I18n.t("historyPanel.createdCheckedOutBranch", branchName.trim());
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
                    I18n.t("cherryPickCommit", commit.name().substring(0, 7)),
                    I18n.t("historyPanel.cherryPick"), JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            runCommitAction(I18n.t("historyPanel.cherryPick"), () -> {
                Context.getGitRepoService().cherryPick((RevCommit) commit);
                return I18n.t("cherryPicked", commit.name().substring(0, 7));
            });
        });

        undoItem.setToolTipText(I18n.t("removeUnpushedCommitFromCurrent"));
        undoItem.addActionListener(e -> undoSelectedCommit());

        revertItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit != null) {
                int ok = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(this),
                        I18n.t("historyPanel.revertCommit", commit.name().substring(0, 7)),
                        I18n.t("historyPanel.revertCommit2"), JOptionPane.OK_CANCEL_OPTION);
                if (ok != JOptionPane.OK_OPTION) return;
                runCommitAction(I18n.t("revert"), () -> {
                    Context.getGitRepoService().revertCommit((RevCommit) commit, null);
                    return I18n.t("reverted", commit.name().substring(0, 7));
                }, true);
            }

        });

        resetItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String[] modes = {"Soft", "Mixed", "Hard"};
            String choice = (String) JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    I18n.t("resetMode"), I18n.t("historyPanel.resetToCommit"),
                    JOptionPane.PLAIN_MESSAGE, null, modes, "Mixed");
            if (choice == null) return;
            ResetCommand.ResetType mode = switch (choice) {
                case "Soft"  -> ResetCommand.ResetType.SOFT;
                case "Hard"  -> ResetCommand.ResetType.HARD;
                default      -> ResetCommand.ResetType.MIXED;
            };
            runCommitAction(I18n.t("reset"), () -> {
                Context.getGitRepoService().resetBranch((RevCommit) commit, mode, null);
                return I18n.t("resetTo", choice, commit.name().substring(0, 7));
            }, true);
        });

        addNoteItem.setToolTipText(I18n.t("addEditGitNoteCommit"));
        addNoteItem.addActionListener(e -> editSelectedCommitNote());

        removeNoteItem.setToolTipText(I18n.t("removeGitNoteFromCommit"));
        removeNoteItem.addActionListener(e -> removeSelectedCommitNote());

        changeMessageItem.setToolTipText(I18n.t("editCommitMessageUnpushedCommit"));
        changeMessageItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            if (commit.getParentCount() == 0) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        I18n.t("cannotChangeMessageInitialRoot"),
                        I18n.t("historyPanel.changeCommitMessage"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            // The plot walk drops commit bodies, so the message is read back on demand.
            String currentMessage = Context.getGitRepoService().getFullMessage(commit);
            JTextArea textArea = new JTextArea(currentMessage != null ? currentMessage : "", 6, 50);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(textArea);
            sp.setPreferredSize(new Dimension(520, 150));
            int choice = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp, I18n.t("historyPanel.changeCommitMessage"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;
            String newMessage = textArea.getText().trim();
            if (newMessage.isEmpty()) return;
            if (newMessage.equals(currentMessage != null ? currentMessage.trim() : "")) return;

            String parentSha = commit.getParent(0).getName();
            runCommitAction(I18n.t("historyPanel.changeCommitMessage2"), () -> {
                List<RevCommit> range = Context.getGitRepoService().getCommitsInRange(parentSha);
                List<InteractiveRebaseDialog.RebaseStep> steps = new ArrayList<>();
                for (RevCommit c : range) {
                    boolean isTarget = c.getName().equals(commit.getName());
                    steps.add(new InteractiveRebaseDialog.RebaseStep(
                            isTarget ? InteractiveRebaseDialog.RebaseAction.REWORD
                                     : InteractiveRebaseDialog.RebaseAction.PICK,
                            c.getName(),
                            isTarget ? newMessage : c.getShortMessage()));
                }
                RebaseResult result = Context.getGitRepoService().interactiveRebase(parentSha, steps);
                Context.updateAll();
                if (!result.getStatus().isSuccessful()) {
                    throw new RuntimeException(I18n.t("rebaseFailedWithStatus", result.getStatus()));
                }
                return I18n.t("commitMessageUpdated");
            }, true);
        });

        interactiveRebaseItem.setToolTipText(
                I18n.t("interactivelyRebaseAllCommitsFrom"));
        interactiveRebaseItem.addActionListener(e -> {
            PlotCommit<PlotLane> commit = selectedCommit();
            if (commit == null) return;
            String fullSha    = commit.getName();
            String displaySha = fullSha.substring(0, 7);
            // Pass a callback so history reloads only after the rebase completes
            InteractiveRebaseHandler.showAndExecute(
                    SwingUtilities.getWindowAncestor(this),
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
        commitMenu.add(undoItem);
        commitMenu.add(revertItem);
        commitMenu.add(resetItem);
        commitMenu.addSeparator();
        commitMenu.add(addNoteItem);
        commitMenu.add(removeNoteItem);
        commitMenu.addSeparator();
        commitMenu.add(changeMessageItem);
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
                changeMessageItem.setEnabled(unpushed && commit.getParentCount() > 0);
                interactiveRebaseItem.setEnabled(unpushed);
                boolean canUndo = unpushed && commit.getParentCount() > 0;
                try {
                    canUndo = canUndo && Context.getGitRepoService().isCommitOnHeadBranch(commit.getName())
                            && (Context.getGitRepoService().isHeadCommit(commit.getName())
                            || commit.getParentCount() == 1);
                } catch (Exception ignored) {
                    canUndo = false;
                }
                undoItem.setEnabled(canUndo);
                boolean hasNote = false;
                try {
                    String note = Context.getGitRepoService().getCommitNote(commit.getName());
                    hasNote = note != null && !note.isBlank();
                } catch (Exception ignored) {}
                addNoteItem.setText(hasNote ? I18n.t("editNote") : I18n.t("addNote"));
                removeNoteItem.setEnabled(hasNote);
                commitMenu.show(commitTable, ev.getX(), ev.getY());
            }
        });

        // ── Search toolbar ────────────────────────────────────────────────
        searchField.putClientProperty("JTextField.placeholderText",
                I18n.t("searchCommitsMessageAuthorSha"));

        // Auto-search: trigger 400 ms after the user stops typing (min 3 chars)
        searchDebounce = new Timer(400, e -> performSearch());
        searchDebounce.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearchTextChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchTextChanged(); }
        });

        // ── Layout ────────────────────────────────────────────────────────

        if (disableDetails) {
            add(new JScrollPane(commitTable), BorderLayout.CENTER);
        } else {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(commitTable), detailPanel);
            splitPane.setDividerLocation(400);
            splitPane.setResizeWeight(0.65);

            add(splitPane, BorderLayout.CENTER);
        }



        historyRefreshListener = evt ->
                SwingUtilities.invokeLater(() -> {
                    if (lastTreeName != null) {
                        loadHistory(lastTreeName, lastAllHistory);
                    }
                });
        repoPathListener = evt -> resetViewState();
        Context.addPropertyChangeListener(Context.PROP_HISTORY_REFRESH, historyRefreshListener);
        Context.addPropertyChangeListener(Context.PROP_REPOSITORY_PATH, repoPathListener);
    }

    /**
     * Drops Context listeners and commit data so this panel can be collected
     * when the history view is no longer visible.
     */
    public void dispose() {
        Context.removePropertyChangeListener(Context.PROP_HISTORY_REFRESH, historyRefreshListener);
        Context.removePropertyChangeListener(Context.PROP_REPOSITORY_PATH, repoPathListener);
        if (searchDebounce != null) {
            searchDebounce.stop();
        }
        tableModel.clear();
        allCommits = new ArrayList<>();
        lastSearchResults = new java.util.HashMap<>();
        lastTreeName = null;
        if (detailPanel != null) {
            detailPanel.showRevision(null);
        }
    }

    /** Clears history-view state specific to the previously active repository. */
    public void resetViewState() {
        lastTreeName = null;
        lastAllHistory = true;
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
            statusBar.setStatus(I18n.t("commitsLoaded", allCommits.size()));
            return;
        }

        boolean useLucene = this.useLucene;
        List<PlotCommit<PlotLane>> base = new ArrayList<>(allCommits);

        if (base.isEmpty()) {
            statusBar.setStatus(I18n.t("noCommitsLoaded"));
            return;
        }

        statusBar.setStatus(I18n.t("searching"));

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
                    statusBar.setStatus(I18n.plural("commitsFound", filtered.size(), filtered.size()));
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
                    statusBar.setStatus(I18n.t("searchError", ex.getMessage()));
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
        statusBar.setStatus(I18n.t("loadingHistory"));
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
                    statusBar.setStatus(I18n.t("commitsLoaded", commits.size()));
                    refreshLuceneState();
                    if (afterLoad != null) {
                        afterLoad.run();
                    } else if (!commits.isEmpty()) {
                        commitTable.setRowSelectionInterval(0, 0);
                        commitTable.scrollRectToVisible(commitTable.getCellRect(0, 0, true));
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load history", e);
                    statusBar.clearProgress();
                    statusBar.setStatus(I18n.t("failedToLoadHistory"));
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
        statusBar.setStatus(I18n.t("loadingFileHistory"));
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
                    statusBar.setStatus(I18n.t("revisionsLoadedFor", revisions.size(), fileName));
                    if (!revisions.isEmpty()) {
                        commitTable.setRowSelectionInterval(0, 0);
                        commitTable.scrollRectToVisible(commitTable.getCellRect(0, 0, true));
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load file history", e);
                    statusBar.clearProgress();
                    statusBar.setStatus(I18n.t("historyPanel.failedLoadFileHistory"));
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

    private void undoSelectedCommit() {
        PlotCommit<PlotLane> commit = selectedCommit();
        if (commit != null) {
            if (commit.getParentCount() == 0) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        I18n.t("cannotUndoInitialRootCommit"),
                        I18n.t("historyPanel.undoCommit"), JOptionPane.WARNING_MESSAGE);
            } else {
                boolean dirty = Context.getGitRepoService().hasWorkingCopyChanges();
                boolean proceed = true;
                if (dirty) {
                    int ok = JOptionPane.showConfirmDialog(
                            SwingUtilities.getWindowAncestor(this),
                            I18n.t("youHaveSomeChangesProgress"),
                            I18n.t("historyPanel.undoCommit"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    proceed = ok == JOptionPane.YES_OPTION;
                } else {
                    int ok = JOptionPane.showConfirmDialog(
                            SwingUtilities.getWindowAncestor(this),
                            I18n.t("undoUnpushedCommitCommitRemoved", commit.name().substring(0, 7)),
                            I18n.t("historyPanel.undoCommit"),
                            JOptionPane.OK_CANCEL_OPTION);
                    proceed = ok == JOptionPane.OK_OPTION;
                }
                if (proceed) {
                    runCommitAction(I18n.t("historyPanel.undoCommit"), () -> {
                        Context.getGitRepoService().undoUnpushedCommit(commit);
                        Context.updateAll();
                        return I18n.t("undidCommit", commit.name().substring(0, 7));
                    }, true);
                }
            }
        }
    }

    private void editSelectedCommitNote() {
        PlotCommit<PlotLane> commit = selectedCommit();
        if (commit != null) {
            String sha = commit.getName();
            String existing = Context.getGitRepoService().getCommitNote(sha);
            boolean editing = existing != null && !existing.isBlank();
            JTextArea textArea = new JTextArea(existing != null ? existing : "", 8, 50);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(textArea);
            sp.setPreferredSize(new Dimension(520, 180));
            String title = editing ? I18n.t("historyPanel.editNote") : I18n.t("historyPanel.addNote");
            int choice = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                String text = textArea.getText();
                if (text == null || text.isBlank()) {
                    if (editing) {
                        runCommitAction(I18n.t("removeNote"), () -> {
                            Context.getGitRepoService().removeCommitNote(sha);
                            return I18n.t("noteRemovedFrom", sha.substring(0, 7));
                        }, false, true);
                    }
                } else {
                    runCommitAction(editing ? I18n.t("historyPanel.editNote2") : I18n.t("historyPanel.addNote2"), () -> {
                        Context.getGitRepoService().addCommitNote(sha, text);
                        return (editing ? I18n.t("noteUpdatedOn") : I18n.t("noteAddedTo")) + sha.substring(0, 7);
                    }, false, true);
                }
            }
        }
    }

    private void removeSelectedCommitNote() {
        PlotCommit<PlotLane> commit = selectedCommit();
        if (commit != null) {
            String sha = commit.getName();
            int ok = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    I18n.t("removeNoteFromCommit", sha.substring(0, 7)),
                    I18n.t("removeNote"), JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                runCommitAction(I18n.t("removeNote"), () -> {
                    Context.getGitRepoService().removeCommitNote(sha);
                    return I18n.t("noteRemovedFrom", sha.substring(0, 7));
                }, false, true);
            }
        }
    }

    private void refreshSelectedCommitDetails() {
        int row = commitTable.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            ScmRevisionInformation rev = tableModel.getRevisionAt(row);
            detailPanel.showRevision(rev);
        }
    }

    /** Runs a git action on a background thread, showing status bar progress. */
    private void runCommitAction(String label, CommitAction action) {
        runCommitAction(label, action, false, false);
    }

    private void runCommitAction(String label, CommitAction action, boolean refreshAfter) {
        runCommitAction(label, action, refreshAfter, false);
    }

    private void runCommitAction(String label, CommitAction action, boolean refreshAfter,
                                 boolean refreshDetails) {
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
                    } else if (refreshDetails) {
                        refreshSelectedCommitDetails();
                    }
                } catch (java.util.concurrent.CancellationException ex) {
                    statusBar.setStatus(I18n.t("opCancelled", label));
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
                        message = I18n.t("checkoutBlockedByLocalChanges", String.join("\n", conflict.getConflictingPaths()));
                        statusBar.setStatus(I18n.t("failedCheckoutConflictFileS", label, conflict.getConflictingPaths().size()));
                    } else {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        message = cause.getMessage();
                        statusBar.setStatus(I18n.t("opFailedStatus", label, cause.getMessage()));
                    }
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(HistoryPanel.this),
                            message, I18n.t("historyPanel.error", label),
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
                String shortMessage = ScmPlotCommit.shortMessageOf(commit);
                messageLabel.setText(shortMessage != null ? shortMessage : "");
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
    public static class CommitTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {I18n.t("graphMessage"), I18n.t("historyPanel.date"), I18n.t("historyPanel.author")};
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

        public boolean isSignedAt(int row) {
            boolean signed = false;
            if (!revisions.isEmpty()) {
                if (row >= 0 && row < revisions.size()) {
                    ScmRevisionInformation rev = revisions.get(row);
                    signed = rev != null && rev.isSigned();
                }
            } else {
                PlotCommit<PlotLane> pc = getCommitAt(row);
                signed = ScmPlotCommit.signedOf(pc);
            }
            return signed;
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
                case 2 -> ScmPlotCommit.authorNameOf(pc) != null ? ScmPlotCommit.authorNameOf(pc) : "";
                default -> "";
            };
        }
    }
}
