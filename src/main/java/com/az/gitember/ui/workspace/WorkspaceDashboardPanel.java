package com.az.gitember.ui.workspace;

import com.az.gitember.data.Const;
import com.az.gitember.data.Project;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.SyntaxStyleUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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
public class WorkspaceDashboardPanel extends JPanel {

    private static final Logger log = Logger.getLogger(WorkspaceDashboardPanel.class.getName());

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

    /**
     * Single combined working-copy tree: top-level nodes are the projects, and each project's
     * changes hang beneath it as a folders/files hierarchy.
     */
    private final DefaultMutableTreeNode workingCopyRoot = new DefaultMutableTreeNode("Workspace");
    private final DefaultTreeModel workingCopyModel = new DefaultTreeModel(workingCopyRoot);
    private final JTree workingCopyTree = new JTree(workingCopyModel);

    /** Pixel width of the leading checkbox, used to hit-test stage/unstage clicks. */
    private final int checkboxWidth = new JCheckBox().getPreferredSize().width;


    private Workspace workspace;

    public WorkspaceDashboardPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Main", buildMainTab());
        tabs.addTab("Working Copy", buildWorkingCopyTab());
        tabs.addTab("Search", buildSearchTab());
        add(tabs, BorderLayout.CENTER);
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

        rebuildWorkingCopy();
    }

    // ── Main tab (dashboard) ─────────────────────────────────────────────────────

    private JComponent buildMainTab() {
        JPanel main = new JPanel(new BorderLayout());
        main.add(buildHeader(), BorderLayout.NORTH);

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

    // ── Working Copy tab ─────────────────────────────────────────────────────────

    private JComponent buildWorkingCopyTab() {
        workingCopyTree.setRootVisible(false);
        workingCopyTree.setShowsRootHandles(true);
        workingCopyTree.setRowHeight(22);
        workingCopyTree.setCellRenderer(new FileNodeRenderer());

        // A single click on a file node's leading checkbox toggles stage/unstage,
        // mirroring the checkbox in WorkingCopyPanel.
        workingCopyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                TreePath path = workingCopyTree.getPathForLocation(e.getX(), e.getY());
                if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) {
                    return;
                }
                if (!(node.getUserObject() instanceof FileNode fileNode)) return;

                Rectangle bounds = workingCopyTree.getPathBounds(path);
                if (bounds != null && e.getX() <= bounds.x + checkboxWidth) {
                    toggleStage(node, fileNode);
                }
            }
        });

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
        workingCopyRoot.removeAllChildren();

        java.util.Collection<Project> projects =
                workspace == null ? List.of() : workspace.getProjects();
        if (projects.isEmpty()) {
            workingCopyRoot.add(new DefaultMutableTreeNode("No repositories in this workspace."));
            workingCopyModel.reload();
            return;
        }

        for (Project project : projects) {
            String name = new File(nz(project.getProjectHomeFolder())).getName();
            DefaultMutableTreeNode projectNode =
                    new DefaultMutableTreeNode(name.isEmpty() ? "(unknown)" : name);
            projectNode.add(new DefaultMutableTreeNode("Loading…"));
            workingCopyRoot.add(projectNode);
            loadProjectWorkingCopy(project, projectNode);
        }

        workingCopyModel.reload();
        expandAll(workingCopyTree);
    }

    /**
     * Reads a project's working-copy status off the EDT (using a throwaway
     * {@link GitRepoService}) and fills the project node with a folders/files hierarchy.
     */
    private void loadProjectWorkingCopy(Project project, DefaultMutableTreeNode projectNode) {
        String home = project.getProjectHomeFolder();
        if (home == null || home.isBlank()) {
            setChildren(projectNode, List.of(new DefaultMutableTreeNode("(unknown location)")));
            return;
        }
        String gitFolder = home + File.separator + Const.GIT_FOLDER;

        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            protected DefaultMutableTreeNode doInBackground() throws Exception {
                GitRepoService svc = new GitRepoService(gitFolder);
                try {
                    DefaultMutableTreeNode holder = new DefaultMutableTreeNode();
                    populateFileTree(project, holder, svc.getStatuses(null, false));
                    return holder;
                } finally {
                    svc.shutdown();
                }
            }

            @Override
            protected void done() {
                DefaultMutableTreeNode holder;
                try {
                    holder = get();
                } catch (Exception ex) {
                    log.log(Level.FINE, "Cannot load working copy for " + home, ex);
                    holder = new DefaultMutableTreeNode();
                    holder.add(new DefaultMutableTreeNode("(cannot read working copy)"));
                }
                moveChildren(holder, projectNode);
                workingCopyModel.reload(projectNode);
                expandAll(workingCopyTree);
            }
        }.execute();
    }

    /** Populates {@code parent} with a folders/files hierarchy from a flat list of items. */
    private void populateFileTree(Project project, DefaultMutableTreeNode parent, List<ScmItem> items) {
        if (items == null || items.isEmpty()) {
            parent.add(new DefaultMutableTreeNode("(no changes)"));
            return;
        }

        List<ScmItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(i -> nz(i.getShortName())));

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

    /** Replaces {@code node}'s children with the supplied nodes. */
    private void setChildren(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> children) {
        node.removeAllChildren();
        children.forEach(node::add);
        workingCopyModel.reload(node);
    }

    /** Moves all children from {@code from} to {@code to}, replacing {@code to}'s existing children. */
    private static void moveChildren(DefaultMutableTreeNode from, DefaultMutableTreeNode to) {
        to.removeAllChildren();
        while (from.getChildCount() > 0) {
            to.add((DefaultMutableTreeNode) from.getChildAt(0));
        }
    }

    private DefaultMutableTreeNode findOrCreateFolder(DefaultMutableTreeNode parent, String folderName) {
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

    private static void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    // ── Stage / unstage ──────────────────────────────────────────────────────────

    /**
     * Toggles the staged state of a single file using its <em>own</em> project's repository
     * (a throwaway {@link GitRepoService}), then reloads that project's subtree.
     */
    private void toggleStage(DefaultMutableTreeNode node, FileNode fileNode) {
        Project project = fileNode.project();
        ScmItem item = fileNode.item();
        boolean staged = isStaged(fileNode.status());

        String home = project.getProjectHomeFolder();
        if (home == null || home.isBlank()) return;
        String gitFolder = home + File.separator + Const.GIT_FOLDER;

        DefaultMutableTreeNode projectNode = projectNodeOf(node);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                GitRepoService svc = new GitRepoService(gitFolder);
                try {
                    if (staged) {
                        svc.removeFileFromCommitStage(item.getShortName());
                    } else {
                        stageItem(svc, item);
                    }
                } finally {
                    svc.shutdown();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Stage/unstage failed for " + item.getShortName(), ex);
                }
                if (projectNode != null) {
                    loadProjectWorkingCopy(project, projectNode);
                }
            }
        }.execute();
    }

    /** Stages {@code item} on the supplied service, mirroring WorkingCopyPanel's stage semantics. */
    private void stageItem(GitRepoService svc, ScmItem item) throws Exception {
        String status = item.getAttribute() != null ? item.getAttribute().getStatus() : null;
        String fileName = item.getShortName();

        if (ScmItem.Status.RENAMED.equals(status)) {
            String oldName = item.getAttribute().getOldName();
            if (oldName != null) {
                svc.renameFile(oldName, fileName);
            }
        } else if (ScmItem.Status.MISSED.equals(status)) {
            svc.removeFile(fileName);
        } else {
            svc.addFileToCommitStage(fileName);
        }
    }

    /** Walks up from a file node to its owning project node (direct child of the hidden root). */
    private DefaultMutableTreeNode projectNodeOf(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode current = node;
        while (current.getParent() != null && current.getParent() != workingCopyRoot) {
            current = (DefaultMutableTreeNode) current.getParent();
        }
        return current.getParent() == workingCopyRoot ? current : null;
    }

    private static boolean isStaged(String status) {
        return ScmItem.Status.ADDED.equals(status)
                || ScmItem.Status.CHANGED.equals(status)
                || ScmItem.Status.RENAMED.equals(status)
                || ScmItem.Status.REMOVED.equals(status);
    }

    private static Color statusColor(String status) {
        if (status == null) {
            return SyntaxStyleUtil.UNSTAGED_COLOR;
        }
        if (isStaged(status)) {
            return SyntaxStyleUtil.STAGED_COLOR.darker();
        } else if (status.startsWith("Conflict")) {
            return SyntaxStyleUtil.CONFLICT_COLOR;
        } else if (ScmItem.Status.UNTRACKED.equals(status) || ScmItem.Status.UNTRACKED_FOLDER.equals(status)) {
            return SyntaxStyleUtil.UNTRACKED_COLOR;
        } else if (ScmItem.Status.LFS.equals(status)) {
            return SyntaxStyleUtil.LFS_COLOR;
        }
        return SyntaxStyleUtil.UNSTAGED_COLOR;
    }

    /** Leaf tree data: a working-copy change tied to the project it belongs to. */
    private record FileNode(Project project, ScmItem item, String leafName) {
        String status() {
            return item.getAttribute() != null ? item.getAttribute().getStatus() : "";
        }
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
            checkBox.setSelected(isStaged(status));

            String text = fileNode.leafName();
            if (status != null && !status.isEmpty()) {
                text = text + "  [" + status + "]";
            }
            label.setText(text);
            label.setForeground(statusColor(status));
            return panel;
        }
    }

    // ── Search tab ───────────────────────────────────────────────────────────────

    private JComponent buildSearchTab() {
        // Reserved for workspace-wide search — intentionally empty for now.
        return new JPanel();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

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
