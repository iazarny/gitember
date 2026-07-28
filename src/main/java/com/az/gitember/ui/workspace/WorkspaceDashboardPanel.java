package com.az.gitember.ui.workspace;

import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import com.az.gitember.service.GetRepoStatService;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.WorkspaceSearchService;
import com.az.gitember.ui.StatusBar;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.WorkingCopyOps;
import org.apache.commons.lang3.ObjectUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Workspace view organized into three tabs:
 * <ol>
 *   <li><b>Main</b> — aggregated dashboard: a summary header and a per-repository table.</li>
 *   <li><b>Working Copy</b> — one folders/files tree per repository, showing the working-copy
 *       status of each project in the workspace.</li>
 *   <li><b>Search</b> — reserved for workspace-wide search (empty for now).</li>
 * </ol>
 *
 * <p>The dashboard is deliberately data-driven so it can grow without structural change — append
 * a {@link Column} to surface new per-repository information, or a {@link Summary} for a new
 * aggregate figure. Values that are not computed yet render the {@value #PLACEHOLDER} placeholder.
 */
public class WorkspaceDashboardPanel extends WorkingCopyOps {

    private static final Logger log = Logger.getLogger(WorkspaceDashboardPanel.class.getName());

    static final String PLACEHOLDER = " ";

    /**
     * Shown in a cell while its repository's stats are still being read off the EDT.
     */
    static final String LOADING = "…";

    private record Column(String title, Function<Project, Object> value) {
    }

    private record Summary(String label, Function<Workspace, Object> value) {
    }


    /**
     * Computed stats keyed by project; absent while a project is still loading.
     */
    private final Map<Project, RepoStats> statsByProject = new HashMap<>();

    private final List<Column> columns = List.of(
            new Column("Repository", p -> new File(ObjectUtils.getIfNull(p.getProjectHomeFolder(), "")).getName()),
            new Column("Branch", p -> cell(p, RepoStats::branch)),
            new Column("Status", this::statusCell),
            new Column("Modified", p -> cellInt(p, RepoStats::modified)),
            new Column("Ahead", p -> cellInt(p, RepoStats::ahead)),
            new Column("Behind", p -> cellInt(p, RepoStats::behind))
    );

    private final JLabel titleLabel = new JLabel("Workspace");
    private final JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
    private final RepoTableModel tableModel = new RepoTableModel();
    private final JTable table = new JTable(tableModel);

    /**
     * Single combined working-copy tree: top-level nodes are the projects, and each project's
     * changes hang beneath it as a folders/files hierarchy.
     */
    private final DefaultMutableTreeNode workingCopyRoot = new DefaultMutableTreeNode("Workspace");
    private final DefaultTreeModel workingCopyModel = new DefaultTreeModel(workingCopyRoot);
    private final JTree workingCopyTree = new JTree(workingCopyModel);
    /**
     * Projects the working-copy tree's top level currently reflects; lets {@link #rebuildWorkingCopy()}
     * skip tearing down and rebuilding the whole tree (which would reset selection/expansion) when
     * the project set hasn't changed.
     */
    private List<Project> workingCopyTreeProjects = new ArrayList<>();

    /**
     * Workspace-wide content search over each project's working copy, plus the tree that presents
     * the results grouped by project (mirroring the Working Copy tab's project-rooted layout).
     */
    private final WorkspaceSearchService searchService = new WorkspaceSearchService();
    private final DefaultMutableTreeNode searchRoot = new DefaultMutableTreeNode("Search");
    private final DefaultTreeModel searchModel = new DefaultTreeModel(searchRoot);
    private final JTree searchTree = new JTree(searchModel);
    /** Coalesces rapid keystrokes into a single search. */
    private final Timer searchDebounce;
    /** Guards against overlapping (re)index runs. */
    private boolean indexing = false;

    private static final String TAB_MAIN = "Main";
    private static final String TAB_WORKING_COPY = "Working Copy";
    private static final String TAB_SEARCH = "Search";

    private final JTabbedPane tabs = new JTabbedPane();

    private Workspace workspace;

    /**
     * Notified with whether <em>any</em> project in the workspace currently has staged changes,
     * so the owner (main frame) can keep the Commit button in sync with dashboard stage/unstage
     * actions. Recomputed by {@link #updateButtonStates()} whenever the staging state may change.
     */
    private java.util.function.Consumer<Boolean> onCommitStateChanged;

    public WorkspaceDashboardPanel(StatusBar statusBar) {
        super(statusBar);

        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        tabs.addTab(TAB_MAIN, buildMainTab());
        tabs.addTab(TAB_WORKING_COPY, buildWorkingCopyTab());
        tabs.addTab(TAB_SEARCH, buildSearchTab());
        // Reload the tab's content each time the user switches to it.
        tabs.addChangeListener(e -> reloadSelectedTab());

        // Toolbar
        searchField.putClientProperty("JTextField.placeholderText", "Search...");

        searchDebounce = new Timer(300, e -> performSearch());
        searchDebounce.setRepeats(false);

        add(tabs, BorderLayout.CENTER);

        searchField.setEnabled(false);
    }

    public DefaultTreeModel getWorkingCopyModel() {
        return workingCopyModel;
    }

    public DefaultMutableTreeNode getWorkingCopyRoot() {
        return workingCopyRoot;
    }

    public JTree getWorkingCopyTree() {
        return workingCopyTree;
    }

    public void setOnCommitStateChanged(java.util.function.Consumer<Boolean> onCommitStateChanged) {
        this.onCommitStateChanged = onCommitStateChanged;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        titleLabel.setText(workspace == null ? "" : workspace.getName());
        searchRoot.removeAllChildren();
        searchModel.reload();
        // Reload whatever tab is currently visible when the dashboard is (re)opened.
        reloadSelectedTab();
        // Kick off (or refresh) the working-copy search index for this workspace.
        ensureIndex();
    }

    @Override
    protected void applyFilter() {
        // Search runs against the Lucene index, which only exists once indexing has enabled the
        // field; debounce so we don't fire a query on every keystroke.
        if (searchField.isEnabled()) {
            searchDebounce.restart();
        }
    }

    @Override
    protected void stageAll() {
        stageAllAcrossWorkspace(true);
    }

    @Override
    protected void unstageAll() {
        stageAllAcrossWorkspace(false);
    }

    /**
     * Walks every project in the workspace and, using each project's <em>own</em>
     * {@link GitRepoService}, stages (when {@code stage} is true) all unstaged items or unstages
     * all staged items. Runs off the EDT, then refreshes the button states and the working-copy tree.
     */
    private void stageAllAcrossWorkspace(boolean stage) {
        List<Project> projects = new ArrayList<>(
                workspace == null ? List.of() : workspace.getProjects());
        if (projects.isEmpty()) return;

        statusBar.setStatus(stage ? "Staging all..." : "Unstaging all...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (Project project : projects) {
                    try (GitRepoService svc = GitRepoService.of(project)) {
                        for (ScmItem item : svc.getStatuses(null)) {

                            statusBar.setStatus(stage ?
                                    "Staging "  + item.getShortName():
                                    "Unstaging " +  item.getShortName());

                            boolean staged = ScmItem.isStaged(
                                    item.getAttribute() != null ? item.getAttribute().getStatus() : null);
                            if (stage && !staged) {
                                svc.stageItem(item);
                            } else if (!stage && staged) {
                                svc.unstageItem(item);
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus(stage ? "All files staged" : "All files unstaged");
                } catch (Exception ex) {
                    log.log(Level.WARNING, (stage ? "Stage all" : "Unstage all") + " failed", ex);
                    statusBar.setStatus("Error: " + ex.getMessage());
                }
                refresh();
            }
        }.execute();
    }

    @Override
    public void refresh() {
        reloadSelectedTab(); // recomputes button states as part of the reload
        reindexIfIndexed();  // keep the search index current with the working copy
    }

    /**
     * Recomputes the Stage All / Unstage All buttons and (via the commit callback) the Commit
     * button from the aggregate staged/unstaged state of every project. Exposed so the main frame
     * can refresh the workspace toolbar/menu the moment the workspace view becomes active.
     */
    public void refreshButtonStates() {
        updateButtonStates();
    }

    /**
     * Recomputes the enabled state of the Stage All / Unstage All buttons from the working-copy
     * status of <em>every</em> project in the workspace: Stage All is enabled when any project has
     * an unstaged item, Unstage All when any project has a staged item. The per-project git reads
     * run off the EDT; the buttons are updated on the EDT once the scan completes.
     */
    @Override
    public void updateButtonStates() {
        if (!Context.isWorkspaceMode() || Context.getActiveView() != Context.ActiveView.WORKSPACE) {
            return;
        }
        List<Project> projects = new ArrayList<>(
                workspace == null ? List.of() : workspace.getProjects());
        stageAllBtn.setEnabled(false);
        unstageAllBtn.setEnabled(false);
        if (projects.isEmpty()) return;

        new SwingWorker<boolean[], Void>() {
            @Override
            protected boolean[] doInBackground() {
                boolean hasUnstaged = false;
                boolean hasStaged = false;
                for (Project project : projects) {
                    try (GitRepoService svc = GitRepoService.of(project)) {
                        hasUnstaged |= svc.hasUntaged();
                        hasStaged |= svc.hasStaged();
                    } catch (Exception ex) {
                        log.log(Level.FINE, "Cannot read status for " + project, ex);
                    }
                    if (hasUnstaged && hasStaged) break; // nothing left to learn
                }
                return new boolean[]{hasUnstaged, hasStaged};
            }

            @Override
            protected void done() {
                try {
                    boolean[] state = get();
                    stageAllBtn.setEnabled(state[0]);
                    unstageAllBtn.setEnabled(state[1]);
                    // Commit is possible when at least one project has a staged item.
                    if (onCommitStateChanged != null) {
                        onCommitStateChanged.accept(state[1]);
                    }
                } catch (Exception ex) {
                    log.log(Level.FINE, "Cannot update workspace button states", ex);
                }
            }
        }.execute();
    }


    /**
     * Reloads the currently visible tab — invoked when the main window regains focus while the
     * dashboard is showing, mirroring the repository view's focus-driven refresh.
     */
    public void reloadActiveTab() {
        if (workspace != null) {
            reloadSelectedTab();
        }
    }

    /**
     * Reloads the content of the currently selected tab.
     */
    private void reloadSelectedTab() {
        int index = tabs.getSelectedIndex();
        String title = index >= 0 ? tabs.getTitleAt(index) : null;
        if (TAB_WORKING_COPY.equals(title)) {
            reloadWorkingCopyTab();
            // Switching to the working copy is a natural point to pick up on-disk edits.
            reindexIfIndexed();
        } else if (TAB_MAIN.equals(title)) {
            reloadMainTab();
        }
        // Stage All / Unstage All / Commit apply to the whole workspace regardless of which tab
        // is visible, so their enabled state must be recomputed on every (re)load — including the
        // Main tab shown when a workspace is first opened.
        updateButtonStates();
    }

    /**
     * Recomputes the per-repository table and the header summaries.
     */
    private void reloadMainTab() {
        statsByProject.clear();
        if (workspace == null) {
            metricsPanel.removeAll();
            tableModel.setRows(List.of());
        } else {
            List<Project> projects = new ArrayList<>(workspace.getProjects());
            tableModel.setRows(projects);
            loadAllStats(projects);
        }
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }

    /**
     * Rebuilds the combined working-copy tree.
     */
    private void reloadWorkingCopyTab() {
        rebuildWorkingCopy();
    }

    // ── Per-repository stats (async) ──────────────────────────────────────────────

    /**
     * Kicks off one background read per project; the table and header update as each returns.
     */
    private void loadAllStats(List<Project> projects) {
        for (Project project : projects) {
            String home = project.getProjectHomeFolder();
            if (home == null || home.isBlank()) {
                statsByProject.put(project, RepoStats.failed());
                continue;
            }
            new SwingWorker<RepoStats, Void>() {
                @Override
                protected RepoStats doInBackground() {
                    try {
                        return new GetRepoStatService().computeStats(project.getProjectHomeFolder());
                    } catch (Exception ex) {
                        log.log(Level.FINE, "Cannot read stats for " + home, ex);
                        return RepoStats.failed();
                    }
                }

                @Override
                protected void done() {
                    RepoStats stats;
                    try {
                        stats = get();
                    } catch (Exception ex) {
                        stats = RepoStats.failed();
                    }
                    statsByProject.put(project, stats);
                    int row = tableModel.indexOf(project);
                    if (row >= 0) {
                        tableModel.fireTableRowsUpdated(row, row);
                    }
                    metricsPanel.revalidate();
                    metricsPanel.repaint();
                }
            }.execute();
        }
    }


    // ── Cell / summary formatting ─────────────────────────────────────────────────

    private Object cell(Project project, Function<RepoStats, Object> mapper) {
        RepoStats stats = statsByProject.get(project);
        if (stats == null) return LOADING;
        if (stats.error()) return PLACEHOLDER;
        return mapper.apply(stats);
    }

    private Object cellInt(Project project, ToIntFunction<RepoStats> mapper) {
        RepoStats stats = statsByProject.get(project);
        if (stats == null) return LOADING;
        if (stats.error()) return PLACEHOLDER;
        return mapper.applyAsInt(stats);
    }

    private Object statusCell(Project project) {
        RepoStats stats = statsByProject.get(project);
        if (stats == null) return LOADING;
        if (stats.error()) return PLACEHOLDER;
        if (stats.conflicts() > 0) return stats.conflicts() + (stats.conflicts() > 1 ? " conflicts" : " conflict");
        if (stats.modified() > 0) return "Modified";
        return "Clean";
    }

    // ── Main tab (dashboard) ─────────────────────────────────────────────────────

    private JComponent buildMainTab() {
        JPanel main = new JPanel(new BorderLayout());
        //main.add(buildHeader(), BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        main.add(new JScrollPane(table), BorderLayout.CENTER);
        return main;
    }

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

    // ── Working Copy tab ─────────────────────────────────────────────────────────

    private JComponent buildWorkingCopyTab() {
        workingCopyTree.setRootVisible(false);
        workingCopyTree.setShowsRootHandles(true);
        workingCopyTree.setRowHeight(22);
        workingCopyTree.setCellRenderer(new FileNodeRenderer());

        // Left-click on the leading checkbox toggles stage/unstage.
        // Right-click shows a context menu identical to the working-copy panel's.
        workingCopyTree.addMouseListener(new WorkingCopyMouseAdapter(this, statusBar) );

        JScrollPane scroll = new JScrollPane(workingCopyTree);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /**
     * Rebuilds the combined working-copy tree: one top-level node per project (shown with a
     * "Loading…" placeholder), each populated asynchronously with its working-copy changes.
     */
    private void rebuildWorkingCopy() {

        List<Project> projects = new ArrayList<>(workspace.getProjects());

        if (projects.equals(workingCopyTreeProjects)) {
            // Same set of projects as last time — refresh each project's subtree in place instead
            // of tearing down and rebuilding the whole tree, which would reset selection/expansion.
            for (int i = 0; i < projects.size(); i++) {
                loadProjectWorkingCopy(projects.get(i), (DefaultMutableTreeNode) workingCopyRoot.getChildAt(i));
            }
            return;
        }
        workingCopyTreeProjects = projects;

        workingCopyRoot.removeAllChildren();

        for (Project project : projects) {
            String name = new File(ObjectUtils.getIfNull(project.getProjectHomeFolder(), "")).getName();
            DefaultMutableTreeNode projectNode =
                    new DefaultMutableTreeNode(name.isEmpty() ? "(unknown)" : name);
            projectNode.add(new DefaultMutableTreeNode("Loading…"));
            workingCopyRoot.add(projectNode);
            loadProjectWorkingCopy(project, projectNode);
        }

        workingCopyModel.reload();
        expandIfSense(workingCopyTree);
    }

    /**
     * Reads a project's working-copy status off the EDT (using a throwaway
     * {@link GitRepoService}) and fills the project node with a folders/files hierarchy.
     */
    public void loadProjectWorkingCopy(Project project, DefaultMutableTreeNode projectNode) {
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            protected DefaultMutableTreeNode doInBackground() throws Exception {
                try (GitRepoService svc = GitRepoService.of(project)) {
                    DefaultMutableTreeNode holder = new DefaultMutableTreeNode();
                    populateFileTree(project, holder, svc.getStatuses(null));
                    return holder;
                }
            }

            @Override
            protected void done() {
                DefaultMutableTreeNode holder;
                try {
                    holder = get();
                } catch (Exception ex) {
                    log.log(Level.FINE, "Cannot load working copy for " + project, ex);
                    holder = new DefaultMutableTreeNode();
                    holder.add(new DefaultMutableTreeNode("(cannot read working copy)"));
                }
                moveChildren(holder, projectNode);
                workingCopyModel.reload(projectNode);
                //expand ???
            }
        }.execute();
    }


    /**
     * Moves all children from {@code from} to {@code to}, replacing {@code to}'s existing children.
     */
    private static void moveChildren(DefaultMutableTreeNode from, DefaultMutableTreeNode to) {
        to.removeAllChildren();
        while (from.getChildCount() > 0) {
            to.add((DefaultMutableTreeNode) from.getChildAt(0));
        }
    }

    /**
     * Populates {@code parent} with a folders/files hierarchy from a flat list of items.
     */
    private void populateFileTree(Project project, DefaultMutableTreeNode parent, java.util.List<ScmItem> items) {
        if (items == null || items.isEmpty()) {
            parent.add(new DefaultMutableTreeNode("(no changes)"));
            return;
        }

        List<ScmItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(i -> ObjectUtils.getIfNull(i.getShortName(), "")));

        for (ScmItem item : sorted) {
            String path = item.getShortName();
            if (path == null || path.isEmpty()) continue;

            String[] parts = path.replace('\\', '/').split("/");
            DefaultMutableTreeNode current = parent;
            for (int i = 0; i < parts.length - 1; i++) {
                current = findOrCreateFolder(current, parts[i]);
            }

            current.add(new DefaultMutableTreeNode(new FileNode(project, item, parts[parts.length - 1])));
        }
    }



    public DefaultMutableTreeNode findOrCreateFolder(DefaultMutableTreeNode parent, String folderName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (folderName.equals(child.getUserObject()) && !child.isLeaf()) {
                return child;
            }
        }
        DefaultMutableTreeNode folder = new DefaultMutableTreeNode(folderName);
        parent.add(folder);
        return folder;
    }

    private static void expandIfSense(JTree tree) {
       /* for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }*/
    }





    /**
     * Renders file nodes as {@code [checkbox] name  [status]} with a status-colored label;
     * all other nodes fall back to the default tree renderer.
     */
    private class FileNodeRenderer implements TreeCellRenderer {

        private final DefaultTreeCellRenderer delegate = new DefaultTreeCellRenderer();
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel label = new JLabel();

        FileNodeRenderer() {
            panel.setOpaque(false);
            checkBox.setOpaque(false);
            checkBox.setBorder(BorderFactory.createEmptyBorder());
            panel.add(checkBox);
            panel.add(label);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object userObject = value instanceof DefaultMutableTreeNode n ? n.getUserObject() : null;
            if (!(userObject instanceof FileNode fileNode)) {
                return delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }

            String status = fileNode.status();
            checkBox.setSelected(ScmItem.isStaged(status));

            String text = fileNode.getLeafName();
            if (status != null && !status.isEmpty()) {
                text = text + "  [" + status + "]";
            }
            label.setText(text);
            label.setForeground(selected
                    ? UIManager.getColor("Tree.selectionForeground")
                    : SyntaxStyleUtil.scmItemColor(status));

            panel.setOpaque(selected);
            panel.setBackground(UIManager.getColor("Tree.selectionBackground"));
            return panel;
        }
    }

    // ── Search tab ───────────────────────────────────────────────────────────────

    private JComponent buildSearchTab() {
        searchTree.setRootVisible(false);
        searchTree.setShowsRootHandles(true);
        searchTree.setRowHeight(22);
        searchTree.addMouseListener(
                new SearchTreeMouseAdapter(searchTree, new SearchItemContextMenu(this, searchTree))
        );

        JScrollPane scroll = new JScrollPane(searchTree);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Runs the current query across all projects' working-copy indexes and shows the results. */
    private void performSearch() {
        if (workspace == null) {
            return;
        }
        String term = searchField.getText().trim();
        if (term.length() < 3) {
            searchRoot.removeAllChildren();
            searchModel.reload();
            return;
        }

        selectSearchTab();
        List<Project> projects = new ArrayList<>(workspace.getProjects());
        statusBar.setStatus("Searching…");

        new SwingWorker<Map<Project, Set<String>>, Void>() {
            @Override
            protected Map<Project, Set<String>> doInBackground() {
                return searchService.search(projects, term);
            }

            @Override
            protected void done() {
                Map<Project, Set<String>> results;
                try {
                    results = get();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Workspace search failed", ex);
                    statusBar.setStatus("Search error: " + ex.getMessage());
                    return;
                }
                populateSearchResults(results);
                int count = results.values().stream().mapToInt(Set::size).sum();
                statusBar.setStatus(count + " file" + (count != 1 ? "s" : "") + " found");
            }
        }.execute();
    }

    /**
     * Rebuilds the search tree: one top-level node per project that had matches, each holding a
     * folders/files hierarchy of the matching paths.
     */
    private void populateSearchResults(Map<Project, Set<String>> results) {
        searchRoot.removeAllChildren();
        if (results.isEmpty()) {
            searchRoot.add(new DefaultMutableTreeNode("No matches."));
        } else {
            for (Map.Entry<Project, Set<String>> entry : results.entrySet()) {
                Project project = entry.getKey();
                String name = new File(ObjectUtils.getIfNull(project.getProjectHomeFolder(), "")).getName();
                DefaultMutableTreeNode projectNode =
                        new DefaultMutableTreeNode(name.isEmpty() ? "(unknown)" : name);

                List<String> paths = new ArrayList<>(entry.getValue());


                paths.sort(Comparator.naturalOrder());
                for (String path : paths) {
                    String[] parts = path.replace('\\', '/').split("/");
                    DefaultMutableTreeNode current = projectNode;
                    for (int i = 0; i < parts.length - 1; i++) {
                        current = findOrCreateFolder(current, parts[i]);
                    }


                    current.add(new DefaultMutableTreeNode(
                            new SearchHit(project, path, parts[parts.length - 1])));
                }
                searchRoot.add(projectNode);
            }
        }
        searchModel.reload();
        expandIfSense(searchTree);
    }

    private void selectSearchTab() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (TAB_SEARCH.equals(tabs.getTitleAt(i))) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }



    // ── Indexing lifecycle ─────────────────────────────────────────────────────────

    /**
     * Ensures the workspace working-copy index is ready. When every project is already indexed the
     * search field is enabled immediately and a lightweight incremental refresh runs; otherwise the
     * search field stays disabled until the initial background index completes.
     */
    private void ensureIndex() {
        if (workspace == null) {
            searchField.setEnabled(false);
            return;
        }
        List<Project> projects = new ArrayList<>(workspace.getProjects());
        if (projects.isEmpty()) {
            searchField.setEnabled(false);
            return;
        }
        if (searchService.allIndexed(projects)) {
            searchField.setEnabled(true);
            runIndexing(projects, false);
        } else {
            searchField.setEnabled(false);
            runIndexing(projects, true);
        }
    }

    /** Reindexes changed files only when the workspace is already fully indexed (a no-op otherwise). */
    private void reindexIfIndexed() {
        if (workspace == null || indexing) {
            return;
        }
        List<Project> projects = new ArrayList<>(workspace.getProjects());
        if (searchService.allIndexed(projects)) {
            runIndexing(projects, false);
        }
    }

    /**
     * Runs the (incremental) working-copy indexing off the EDT. On the initial run the search field
     * is enabled once indexing completes.
     */
    private void runIndexing(List<Project> projects, boolean initial) {
        if (indexing || projects.isEmpty()) {
            return;
        }
        indexing = true;
        statusBar.setStatus(initial ? "Indexing workspace…" : "Updating search index…");
        statusBar.showProgress(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                searchService.indexAll(projects, (project, processed) ->
                        statusBar.setStatus("Indexing "
                                + new File(ObjectUtils.getIfNull(project.getProjectHomeFolder(), "")).getName()
                                + " (" + processed + "/" + projects.size() + ")…"));
                return null;
            }

            @Override
            protected void done() {
                indexing = false;
                statusBar.clearProgress();
                if (initial) {
                    searchField.setEnabled(true);
                    statusBar.setStatus("Workspace indexed — search enabled.");
                } else {
                    statusBar.setStatus("Search index updated.");
                }
            }
        }.execute();
    }


    // ── Table model ──────────────────────────────────────────────────────────────

    private class RepoTableModel extends AbstractTableModel {
        private List<Project> rows = new ArrayList<>();

        void setRows(List<Project> rows) {
            this.rows = rows != null ? rows : new ArrayList<>();
            fireTableDataChanged();
        }

        int indexOf(Project project) {
            return rows.indexOf(project);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column).title();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return columns.get(column).value().apply(rows.get(row));
        }
    }
}
