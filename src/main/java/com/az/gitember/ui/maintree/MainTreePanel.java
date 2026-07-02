package com.az.gitember.ui.maintree;

import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.BranchContextMenuFactory;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.maintree.MainTreeCellRenderer.NodeType;
import org.eclipse.jgit.lib.RepositoryState;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTreePanel extends JPanel {

    private static final Logger log = Logger.getLogger(MainTreePanel.class.getName());

    final JTree tree;
    private final DefaultTreeModel treeModel;
    final DefaultMutableTreeNode rootNode;

    private DefaultMutableTreeNode workspaceNode;
    DefaultMutableTreeNode workingCopyNode;
    private DefaultMutableTreeNode historyNode;
    DefaultMutableTreeNode localBranchesNode;
    DefaultMutableTreeNode remoteBranchesNode;
    DefaultMutableTreeNode tagsNode;
    DefaultMutableTreeNode stashesNode;
    private DefaultMutableTreeNode pullRequestsNode;  // null when no PRs
    private DefaultMutableTreeNode submodulesNode;    // null when no submodules
    private DefaultMutableTreeNode worktreesNode;     // null when no linked worktrees

    /** Linked (non-main) worktrees — kept in sync so branch labels can show the indicator. */
    private List<WorktreeInfo> linkedWorktrees = List.of();

    private BranchContextMenuFactory contextMenuFactory;
    private JLabel statusLabel;

    public MainTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));

        rootNode = new DefaultMutableTreeNode(new TreeNodeData("Repository", NodeType.ROOT, null));
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new MainTreeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        //tree.setBackground();
        // Make tree transparent
        tree.setOpaque(false);
        tree.setBorder(
                BorderFactory.createMatteBorder(0,0,0, 0,Color.GREEN)
        );

        buildInitialTree();

        // Right-click context menu
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handlePopup(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handlePopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handlePopup(e);
                }
            }
        });

        Color panelBorder = UIManager.getColor("controlShadow");

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(                BorderFactory.createMatteBorder(
                1, 0, 0, 1,  panelBorder
        ));
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("");
        statusLabel.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        1,
                        1,
                        1,
                        panelBorder
                )
        );
        add(statusLabel, BorderLayout.SOUTH);

        // Listen for Context changes
        MainTreePanelOnBranchChanged onBranchesChanged = new MainTreePanelOnBranchChanged(this);
        Context.addPropertyChangeListener(Context.PROP_LOCAL_BRANCHES, onBranchesChanged);
        Context.addPropertyChangeListener(Context.PROP_REMOTE_BRANCHES, onBranchesChanged);

        Context.addPropertyChangeListener(Context.PROP_TAGS, new MainTreePanelOnTagsChanged(this));

        Context.addPropertyChangeListener(Context.PROP_STASH, new MainTreePanelOnStashChanged(this));

        Context.addPropertyChangeListener(Context.PROP_REPOSITORY_PATH, new MainTreePanelOnRepoChanged(this));

        Context.addPropertyChangeListener(Context.PROP_PULL_REQUESTS, new MainTreePanelOnPullRequestsChanged(this));

        Context.addPropertyChangeListener(Context.PROP_SUBMODULES,    new MainTreePanelOnSubmodulesChanged(this));
        Context.addPropertyChangeListener(Context.PROP_STATUS_LIST,   new MainTreePanelOnStatusListChanged(this));
    }

    void updateStateLabel() {
        if (Context.getGitRepoService() == null) {
            statusLabel.setText("");
            statusLabel.setVisible(false);
            return;
        }
        RepositoryState state = Context.getGitRepoService().getRepositoryState();
        String text = toHumanState(state);
        if (text == null) {
            statusLabel.setText("");
            statusLabel.setVisible(false);
        } else {
            statusLabel.setText(" " + text);
            Color fg = switch (state) {
                case MERGING_RESOLVED, REVERTING_RESOLVED, CHERRY_PICKING_RESOLVED ->
                        new Color(0x2E7D32); // green
                default -> new Color(0xE65100); // orange
            };
            statusLabel.setForeground(fg);
            statusLabel.setVisible(true);
        }
    }

    private static String toHumanState(RepositoryState state) {
        if (state == null) return null;
        return switch (state) {
            case SAFE                     -> null;
            case MERGING                  -> "Merge in progress";
            case MERGING_RESOLVED         -> "Merge resolved — commit to finish";
            case REBASING                 -> "Rebase in progress";
            case REBASING_INTERACTIVE     -> "Interactive rebase in progress";
            case REBASING_MERGE           -> "Rebase in progress (merge)";
            case REBASING_REBASING        -> "Rebase in progress";
            case APPLY                    -> "Applying patches (git am)";
            case REVERTING                -> "Revert in progress";
            case REVERTING_RESOLVED       -> "Revert resolved — commit to finish";
            case BISECTING                -> "Bisecting";
            case CHERRY_PICKING           -> "Cherry-pick in progress";
            case CHERRY_PICKING_RESOLVED  -> "Cherry-pick resolved — commit to finish";
            default -> state.getDescription();
        };
    }
    public void setContextMenuFactory(BranchContextMenuFactory factory) {
        this.contextMenuFactory = factory;
    }

    private void handlePopup(MouseEvent e) {
        if (contextMenuFactory == null) return;

        int row = tree.getRowForLocation(e.getX(), e.getY());
        if (row < 0) {
            // Click was in the row's empty space — find row by Y coordinate only
            TreePath closestPath = tree.getClosestPathForLocation(e.getX(), e.getY());
            if (closestPath != null) {
                Rectangle rowBounds = tree.getPathBounds(closestPath);
                if (rowBounds != null && e.getY() >= rowBounds.y && e.getY() < rowBounds.y + rowBounds.height) {
                    row = tree.getRowForPath(closestPath);
                }
            }
        }
        if (row < 0) return;

        TreePath path = tree.getPathForRow(row);
        if (path == null) return;

        tree.setSelectionPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof TreeNodeData data) {
            try {
                JPopupMenu menu = createMenuForNode(data);
                if (menu != null) {
                    menu.show(tree, e.getX(), e.getY());
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to create context menu", ex);
            }
        }
    }

    private JPopupMenu createMenuForNode(TreeNodeData data) {
        return switch (data.type()) {
            case BRANCH, TAG -> {
                if (data.data() instanceof ScmBranch branch) {
                    yield contextMenuFactory.createBranchContextMenu(branch);
                }
                yield null;
            }
            case TAGS -> contextMenuFactory.createTagsCategoryMenu();
            case STASH -> {
                if (data.data() instanceof ScmRevisionInformation stash) {
                    yield contextMenuFactory.createStashContextMenu(stash);
                }
                yield null;
            }
            case WORKTREE, WORKTREE_MAIN -> {
                if (data.data() instanceof WorktreeInfo wt) {
                    yield contextMenuFactory.createWorktreeContextMenu(wt);
                }
                yield null;
            }
            default -> null;
        };
    }

    /**
     * (Re)builds the tree from scratch, choosing the layout based on whether a workspace is
     * currently open. Safe to call whenever {@link Context#getWorkspace()} changes.
     */
    public void rebuild() {
        SwingUtilities.invokeLater(this::buildInitialTree);
    }

    private void buildInitialTree() {
        rootNode.removeAllChildren();
        pullRequestsNode = null;
        submodulesNode   = null;
        worktreesNode    = null;

        Workspace workspace = Context.getWorkspace();
        if (workspace != null) {
            buildWorkspaceTree(workspace);
        } else {
            workspaceNode = null;
            assignSharedCategoryNodes(addRepositoryCategories(rootNode, () -> Context.getCurrentProject()));
        }

        treeModel.reload();

        if (workspace != null && workspaceNode != null) {
            TreePath workspacePath = new TreePath(new Object[]{rootNode, workspaceNode});
            tree.expandPath(workspacePath);
            tree.setSelectionPath(workspacePath);
        }
    }

    /**
     * Workspace layout: the workspace is the single top-level node; each project/repository
     * is an expandable child whose own children are the standard repository categories
     * (Working Copy, History, branches, …), populated asynchronously from that repo.
     */
    private void buildWorkspaceTree(Workspace workspace) {
        workspaceNode = new DefaultMutableTreeNode(
                new TreeNodeData(workspace.getName(), NodeType.WORKSPACE, workspace));
        rootNode.add(workspaceNode);

        for (Project project : workspace.getProjects()) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(
                    new TreeNodeData(projectLabel(project), NodeType.REPOSITORY, project));
            RepoCategoryNodes nodes = addRepositoryCategories(projectNode, () -> Optional.of(project));
            workspaceNode.add(projectNode);
            loadProjectData(project, nodes);
        }
    }

    private static String projectLabel(Project project) {
        String folder = project.getProjectHomeFolder();
        if (folder == null || folder.isBlank()) {
            return "(unknown)";
        }
        String name = new java.io.File(folder).getName();
        return name.isEmpty() ? folder : name;
    }

    /**
     * Creates the standard per-repository category nodes under {@code parent} and returns
     * references to them so the caller can populate them (per project in workspace mode, or
     * via the shared node fields in single-repository mode).
     */
    private RepoCategoryNodes addRepositoryCategories(DefaultMutableTreeNode parent,
                                                      Supplier<Optional<Project>> projectSupplier) {
        DefaultMutableTreeNode workingCopy = new DefaultMutableTreeNode(
                new TreeNodeData("Working Copy", NodeType.WORKING_COPY, projectSupplier));
        DefaultMutableTreeNode history = new DefaultMutableTreeNode(
                new TreeNodeData("History", NodeType.HISTORY, projectSupplier));
        DefaultMutableTreeNode localBranches = new DefaultMutableTreeNode(
                new TreeNodeData("Local Branches", NodeType.LOCAL_BRANCHES, projectSupplier));
        DefaultMutableTreeNode remoteBranches = new DefaultMutableTreeNode(
                new TreeNodeData("Remote Branches", NodeType.REMOTE_BRANCHES, projectSupplier));
        DefaultMutableTreeNode tags = new DefaultMutableTreeNode(
                new TreeNodeData("Tags", NodeType.TAGS, projectSupplier));
        DefaultMutableTreeNode stashes = new DefaultMutableTreeNode(
                new TreeNodeData("Stashes", NodeType.STASHES, projectSupplier));

        parent.add(workingCopy);
        parent.add(history);
        parent.add(localBranches);
        parent.add(remoteBranches);
        parent.add(tags);
        parent.add(stashes);

        return new RepoCategoryNodes(workingCopy, history, localBranches, remoteBranches, tags, stashes);
    }

    private void assignSharedCategoryNodes(RepoCategoryNodes nodes) {
        workingCopyNode    = nodes.workingCopy();
        historyNode        = nodes.history();
        localBranchesNode  = nodes.localBranches();
        remoteBranchesNode = nodes.remoteBranches();
        tagsNode           = nodes.tags();
        stashesNode        = nodes.stashes();
    }

    /**
     * Loads a workspace project's branches/tags/stashes off the EDT (using a throwaway
     * {@link GitRepoService} for that repo) and fills the project's subtree on completion.
     */
    private void loadProjectData(Project project, RepoCategoryNodes nodes) {
        String home = project.getProjectHomeFolder();
        if (home == null || home.isBlank()) {
            return;
        }
        String gitFolder = home + java.io.File.separator + Const.GIT_FOLDER;

        new SwingWorker<RepoData, Void>() {
            @Override
            protected RepoData doInBackground() throws Exception {
                GitRepoService svc = new GitRepoService(gitFolder);
                try {
                    return new RepoData(
                            svc.getBranches(),
                            svc.getRemoteBranches(),
                            svc.getTags(),
                            svc.getStashList());
                } finally {
                    svc.shutdown();
                }
            }

            @Override
            protected void done() {
                try {
                    RepoData data = get();
                    populateBranches(nodes.localBranches(),  data.localBranches(),  NodeType.BRANCH);
                    populateBranches(nodes.remoteBranches(), data.remoteBranches(), NodeType.BRANCH);
                    populateBranches(nodes.tags(),           data.tags(),           NodeType.TAG);
                    populateStashes(nodes.stashes(),         data.stashes());
                } catch (Exception ex) {
                    log.log(Level.FINE, "Cannot load workspace project data: " + home, ex);
                }
            }
        }.execute();
    }

    /** References to one repository's category nodes, so each can be populated independently. */
    private record RepoCategoryNodes(
            DefaultMutableTreeNode workingCopy,
            DefaultMutableTreeNode history,
            DefaultMutableTreeNode localBranches,
            DefaultMutableTreeNode remoteBranches,
            DefaultMutableTreeNode tags,
            DefaultMutableTreeNode stashes) {
    }

    /** Data read from a single repository for its workspace subtree. */
    private record RepoData(
            List<ScmBranch> localBranches,
            List<ScmBranch> remoteBranches,
            List<ScmBranch> tags,
            List<ScmRevisionInformation> stashes) {
    }

    public void refreshTree() {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() -> {
            populateBranches(localBranchesNode, Context.getLocalBranches(), NodeType.BRANCH);
            populateBranches(remoteBranchesNode, Context.getRemoteBranches(), NodeType.BRANCH);
            populateBranches(tagsNode, Context.getTags(), NodeType.TAG);
            populateStashes(stashesNode, Context.getStash());
            tree.expandRow(2); //ugly , expant local branches only
            //expandAllNodes();
        });
    }

    void populateBranches(DefaultMutableTreeNode parent, List<ScmBranch> branches, NodeType type) {
        parent.removeAllChildren();
        if (branches != null) {
            for (ScmBranch branch : branches) {
                String name = branch.getShortName() != null ? branch.getShortName() : branch.getFullName();
                String[] parts = name.split("/");

                DefaultMutableTreeNode target = parent;
                if (parts.length > 1) {
                    for (int i = 0; i < parts.length - 1; i++) {
                        target = findOrCreateFolder(target, parts[i]);
                    }
                }

                String leafName = parts[parts.length - 1];
                // Remote-tracking indicator: (origin/branch)
                if (branch.getRemoteMergeName() != null) {
                    leafName = leafName + " (" + branch.getRemoteMergeName() + ")";
                }
                // Worktree indicator: [folder-name]
                if (type == NodeType.BRANCH && branch.getBranchType() == ScmBranch.BranchType.LOCAL) {
                    WorktreeInfo wt = linkedWorktrees.stream()
                            .filter(w -> branch.getShortName().equals(w.getBranch()))
                            .findFirst().orElse(null);
                    if (wt != null) {
                        String wtFolder = wt.getPath() != null
                                ? java.nio.file.Paths.get(wt.getPath()).getFileName().toString()
                                : wt.getPath();
                        leafName = leafName + " [" + wtFolder + "]";
                    }
                }
                target.add(new DefaultMutableTreeNode(new TreeNodeData(leafName, type, branch)));
            }
        }
        treeModel.reload(parent);
    }

    private DefaultMutableTreeNode findOrCreateFolder(DefaultMutableTreeNode parent, String folderName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (child.getUserObject() instanceof TreeNodeData data
                    && data.type() == NodeType.BRANCH_FOLDER
                    && folderName.equals(data.displayName())) {
                return child;
            }
        }
        DefaultMutableTreeNode folder = new DefaultMutableTreeNode(
                new TreeNodeData(folderName, NodeType.BRANCH_FOLDER, Context.getCurrentProject()));
        parent.add(folder);
        return folder;
    }

    void populateStashes(DefaultMutableTreeNode parent, List<ScmRevisionInformation> stashes) {
        parent.removeAllChildren();
        if (stashes != null) {
            for (ScmRevisionInformation stash : stashes) {
                String name = stash.getShortMessage() != null ? stash.getShortMessage() : "stash";
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                        new TreeNodeData(name, NodeType.STASH, stash));
                parent.add(node);
            }
        }
        treeModel.reload(parent);
    }

    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public JTree getTree() {
        return tree;
    }

    public DefaultMutableTreeNode getWorkingCopyNode() {
        return workingCopyNode;
    }

    // Property change handlers
    /**
     * Reloads linked worktrees in the background and updates the Worktrees section.
     * Called on repo change and after the Worktrees dialog closes.
     */
    public void refreshWorktrees() {
        if (Context.isWorkspaceMode()) return;
        if (Context.getGitRepoService() == null) return;
        new SwingWorker<List<WorktreeInfo>, Void>() {
            @Override protected List<WorktreeInfo> doInBackground() throws Exception {
                return Context.getGitRepoService().listWorktrees();
            }
            @Override protected void done() {
                try { updateWorktreesNode(get()); }
                catch (Exception ex) { log.log(Level.FINE, "Cannot load worktrees", ex); }
            }
        }.execute();
    }

    private void updateWorktreesNode(List<WorktreeInfo> worktrees) {
        List<WorktreeInfo> all    = worktrees == null ? List.of() : worktrees;
        List<WorktreeInfo> linked = all.stream().filter(w -> !w.isMain()).toList();

        // Keep the snapshot so populateBranches can show the worktree indicator
        linkedWorktrees = linked;
        // Re-render local branches to add/remove the [worktree] suffix
        populateBranches(localBranchesNode, Context.getLocalBranches(), NodeType.BRANCH);

        // Show the section only when there are linked worktrees
        if (linked.isEmpty()) {
            if (worktreesNode != null) {
                treeModel.removeNodeFromParent(worktreesNode);
                worktreesNode = null;
            }
            return;
        }

        boolean isNew = (worktreesNode == null);
        if (isNew) {
            worktreesNode = new DefaultMutableTreeNode(
                    new TreeNodeData("Worktrees", NodeType.WORKTREES, Context.getCurrentProject()));
        }

        worktreesNode.removeAllChildren();

        // First item: main worktree (open-only)
        WorktreeInfo main = all.stream().filter(WorktreeInfo::isMain).findFirst().orElse(null);
        if (main != null) {
            java.nio.file.Path mp = main.getPath() != null
                    ? java.nio.file.Paths.get(main.getPath()).getFileName() : null;
            String mainFolder = mp != null ? mp.toString() : main.getPath();
            String mainBranch = main.getBranch();
            String mainLabel  = mainFolder + " (" + (mainBranch != null ? mainBranch : "detached:" + main.getShortHead()) + ") [main]";
            worktreesNode.add(new DefaultMutableTreeNode(
                    new TreeNodeData(mainLabel, NodeType.WORKTREE_MAIN, main)));
        }

        // Remaining linked worktrees
        for (WorktreeInfo wt : linked) {
            java.nio.file.Path p = wt.getPath() != null
                    ? java.nio.file.Paths.get(wt.getPath()).getFileName() : null;
            String folderName = p != null ? p.toString() : wt.getPath();

            String branch    = wt.getBranch();
            String branchPart = branch != null ? branch : "detached:" + wt.getShortHead();

            String label = folderName + " (" + branchPart + ")";
            if (wt.isLocked())   label += " [locked]";
            if (wt.isPrunable()) label += " [prunable]";
            worktreesNode.add(new DefaultMutableTreeNode(
                    new TreeNodeData(label, NodeType.WORKTREE, wt)));
        }

        if (isNew) {
            int historyIdx = rootNode.getIndex(historyNode);
            treeModel.insertNodeInto(worktreesNode, rootNode, historyIdx + 1);
        } else {
            treeModel.reload(worktreesNode);
        }
        tree.expandPath(new TreePath(new Object[]{rootNode, worktreesNode}));
    }

    void updatePullRequestsNode(List<PullRequest> prs) {
        if (prs == null || prs.isEmpty()) {
            if (pullRequestsNode != null) {
                treeModel.removeNodeFromParent(pullRequestsNode);
                pullRequestsNode = null;
            }
            return;
        }

        boolean isNew = pullRequestsNode == null;
        if (isNew) {
            pullRequestsNode = new DefaultMutableTreeNode(
                    new TreeNodeData("Pull Requests", NodeType.PULL_REQUESTS, Context.getCurrentProject()));
        }

        pullRequestsNode.removeAllChildren();
        for (PullRequest pr : prs) {
            String label = pr.isOpen() ? pr.toString() : pr.toString() + "  [" + pr.state() + "]";
            pullRequestsNode.add(new DefaultMutableTreeNode(
                    new TreeNodeData(label, NodeType.PULL_REQUEST, pr)));
        }

        if (isNew) {
            treeModel.insertNodeInto(pullRequestsNode, rootNode, rootNode.getChildCount());
        } else {
            treeModel.reload(pullRequestsNode);
        }
        //tree.expandPath(new TreePath(new Object[]{rootNode, pullRequestsNode}));
    }

    void updateWorkingCopyLabel() {
        List<ScmItem> items = Context.getStatusList();
        if (items == null || items.isEmpty()) {
            workingCopyNode.setUserObject(
                    new TreeNodeData("Working Copy", NodeType.WORKING_COPY, Context.getCurrentProject())
            );
            treeModel.nodeChanged(workingCopyNode);
            return;
        }

        int modified = 0, added = 0, deleted = 0, untracked = 0;
        for (ScmItem item : items) {
            String status = item.getAttribute().getStatus();
            if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.CHANGED.equals(status)) {
                modified++;
            } else if (ScmItem.Status.ADDED.equals(status)) {
                added++;
            } else if (ScmItem.Status.REMOVED.equals(status) || ScmItem.Status.MISSED.equals(status)) {
                deleted++;
            } else if (ScmItem.Status.UNTRACKED.equals(status) || ScmItem.Status.UNTRACKED_FOLDER.equals(status)) {
                untracked++;
            }
        }

        if (modified == 0 && added == 0 && deleted == 0 && untracked == 0) {
            workingCopyNode.setUserObject(new TreeNodeData("Working Copy", NodeType.WORKING_COPY, Context.getCurrentProject()));
            treeModel.nodeChanged(workingCopyNode);
            return;
        }

        StringBuilder label = new StringBuilder("<html>Working Copy &nbsp;");
        String sep = "/";
        boolean any = false;


        if (modified > 0) {
            label.append("<font color='" + SyntaxStyleUtil.toWebColor(SyntaxStyleUtil.fgChangedExt()) + "'>").append(modified).append("&plusmn;</font>");
            any = true;
        }
        if (added > 0) {
            if (any) label.append(sep);
            label.append("<font color='" + SyntaxStyleUtil.toWebColor(SyntaxStyleUtil.fgAddedExt()) + "'>").append(added).append("+</font>");
            any = true;
        }
        if (deleted > 0) {
            if (any) label.append(sep);
            label.append("<font color='" + SyntaxStyleUtil.toWebColor(SyntaxStyleUtil.fgDeletedExt()) + "'>").append(deleted).append("-</font>");
            any = true;
        }
        if (untracked > 0) {
            if (any) label.append(sep);
            label.append("<font color='" + SyntaxStyleUtil.toWebColor(SyntaxStyleUtil.fgUntrackedExt()) + "'>").append(untracked).append("?</font>");
        }
        label.append("</html>");

        workingCopyNode.setUserObject(new TreeNodeData(label.toString(), NodeType.WORKING_COPY, Context.getCurrentProject()));
        treeModel.nodeChanged(workingCopyNode);
    }

    void updateSubmodulesNode(List<Submodule> subs) {
        if (subs == null || subs.isEmpty()) {
            if (submodulesNode != null) {
                treeModel.removeNodeFromParent(submodulesNode);
                submodulesNode = null;
            }
            return;
        }

        boolean isNew = submodulesNode == null;
        if (isNew) {
            submodulesNode = new DefaultMutableTreeNode(
                    new TreeNodeData("Submodules", NodeType.SUBMODULES, Context.getCurrentProject()));
        }

        submodulesNode.removeAllChildren();
        for (Submodule sub : subs) {
            String label = sub.getPath();
            if (sub.getStatus() == Submodule.Status.UNINITIALIZED) {
                label += " [uninit]";
            } else if (sub.getStatus() == Submodule.Status.MODIFIED) {
                label += " [modified]";
            } else if (sub.getStatus() == Submodule.Status.MISSING) {
                label += " [missing]";
            }
            submodulesNode.add(new DefaultMutableTreeNode(
                    new TreeNodeData(label, NodeType.SUBMODULE, sub)));
        }

        if (isNew) {
            treeModel.insertNodeInto(submodulesNode, rootNode, rootNode.getChildCount());
        } else {
            treeModel.reload(submodulesNode);
        }
    }
}
