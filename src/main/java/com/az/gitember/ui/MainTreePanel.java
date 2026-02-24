package com.az.gitember.ui;

import com.az.gitember.data.PullRequest;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainTreeCellRenderer.NodeType;
import com.az.gitember.ui.MainTreeCellRenderer.TreeNodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTreePanel extends JPanel {

    private static final Logger log = Logger.getLogger(MainTreePanel.class.getName());

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    private DefaultMutableTreeNode workingCopyNode;
    private DefaultMutableTreeNode historyNode;
    private DefaultMutableTreeNode localBranchesNode;
    private DefaultMutableTreeNode remoteBranchesNode;
    private DefaultMutableTreeNode tagsNode;
    private DefaultMutableTreeNode stashesNode;
    private DefaultMutableTreeNode pullRequestsNode;  // null when no PRs

    private BranchContextMenuFactory contextMenuFactory;

    public MainTreePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));

        rootNode = new DefaultMutableTreeNode(new TreeNodeData("Repository", NodeType.ROOT));
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new MainTreeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

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

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        // Listen for Context changes
        Context.addPropertyChangeListener(Context.PROP_LOCAL_BRANCHES, this::onBranchesChanged);
        Context.addPropertyChangeListener(Context.PROP_REMOTE_BRANCHES, this::onBranchesChanged);
        Context.addPropertyChangeListener(Context.PROP_TAGS, this::onTagsChanged);
        Context.addPropertyChangeListener(Context.PROP_STASH, this::onStashChanged);
        Context.addPropertyChangeListener(Context.PROP_REPOSITORY_PATH, this::onRepoChanged);
        Context.addPropertyChangeListener(Context.PROP_PULL_REQUESTS, this::onPullRequestsChanged);
    }

    public void setContextMenuFactory(BranchContextMenuFactory factory) {
        this.contextMenuFactory = factory;
    }

    private void handlePopup(MouseEvent e) {
        if (contextMenuFactory == null) return;

        int row = tree.getRowForLocation(e.getX(), e.getY());
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
            default -> null;
        };
    }

    private void buildInitialTree() {
        rootNode.removeAllChildren();
        pullRequestsNode = null;

        workingCopyNode = new DefaultMutableTreeNode(new TreeNodeData("Working Copy", NodeType.WORKING_COPY));
        historyNode = new DefaultMutableTreeNode(new TreeNodeData("History", NodeType.HISTORY));
        localBranchesNode = new DefaultMutableTreeNode(new TreeNodeData("Local Branches", NodeType.LOCAL_BRANCHES));
        remoteBranchesNode = new DefaultMutableTreeNode(new TreeNodeData("Remote Branches", NodeType.REMOTE_BRANCHES));
        tagsNode = new DefaultMutableTreeNode(new TreeNodeData("Tags", NodeType.TAGS));
        stashesNode = new DefaultMutableTreeNode(new TreeNodeData("Stashes", NodeType.STASHES));

        rootNode.add(workingCopyNode);
        rootNode.add(historyNode);
        rootNode.add(localBranchesNode);
        rootNode.add(remoteBranchesNode);
        rootNode.add(tagsNode);
        rootNode.add(stashesNode);

        treeModel.reload();
    }

    public void refreshTree() {
        SwingUtilities.invokeLater(() -> {
            populateBranches(localBranchesNode, Context.getLocalBranches(), NodeType.BRANCH);
            populateBranches(remoteBranchesNode, Context.getRemoteBranches(), NodeType.BRANCH);
            populateBranches(tagsNode, Context.getTags(), NodeType.TAG);
            populateStashes(stashesNode, Context.getStash());
            expandAllNodes();
        });
    }

    private void populateBranches(DefaultMutableTreeNode parent, List<ScmBranch> branches, NodeType type) {
        parent.removeAllChildren();
        if (branches != null) {
            for (ScmBranch branch : branches) {
                String name = branch.getShortName() != null ? branch.getShortName() : branch.getFullName();
                if (branch.getRemoteMergeName() != null) {
                    name = name + " (" + branch.getRemoteMergeName() + ")";
                }
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                        new TreeNodeData(name, type, branch));
                parent.add(node);
            }
        }
        treeModel.reload(parent);
    }

    private void populateStashes(DefaultMutableTreeNode parent, List<ScmRevisionInformation> stashes) {
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
    private void onBranchesChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            populateBranches(localBranchesNode, Context.getLocalBranches(), NodeType.BRANCH);
            populateBranches(remoteBranchesNode, Context.getRemoteBranches(), NodeType.BRANCH);
        });
    }

    private void onTagsChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            populateBranches(tagsNode, Context.getTags(), NodeType.TAG);
        });
    }

    private void onStashChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            populateStashes(stashesNode, Context.getStash());
        });
    }

    private void onRepoChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            refreshTree();
            tree.setSelectionPath(new TreePath(new Object[]{rootNode, workingCopyNode}));
        });
    }

    private void onPullRequestsChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> updatePullRequestsNode(Context.getPullRequests()));
    }

    private void updatePullRequestsNode(List<PullRequest> prs) {
        if (prs == null || prs.isEmpty()) {
            if (pullRequestsNode != null) {
                rootNode.remove(pullRequestsNode);
                treeModel.reload(rootNode);
                pullRequestsNode = null;
            }
            return;
        }

        if (pullRequestsNode == null) {
            pullRequestsNode = new DefaultMutableTreeNode(
                    new TreeNodeData("Pull Requests", NodeType.PULL_REQUESTS));
            rootNode.add(pullRequestsNode);
        }

        pullRequestsNode.removeAllChildren();
        for (PullRequest pr : prs) {
            pullRequestsNode.add(new DefaultMutableTreeNode(
                    new TreeNodeData(pr.toString(), NodeType.PULL_REQUEST, pr)));
        }
        treeModel.reload(rootNode);
        tree.expandPath(new TreePath(new Object[]{rootNode, pullRequestsNode}));
    }
}
