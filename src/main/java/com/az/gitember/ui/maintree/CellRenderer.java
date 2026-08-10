package com.az.gitember.ui.maintree;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.TreeNodeData;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class CellRenderer extends DefaultTreeCellRenderer {

    public enum NodeType {
        WORKSPACE,
        REPOSITORY,
        WORKING_COPY,
        HISTORY,
        LOCAL_BRANCHES,
        REMOTE_BRANCHES,
        TAGS,
        STASHES,
        BRANCH,
        BRANCH_FOLDER,
        TAG,
        STASH,
        ROOT,
        PULL_REQUESTS,
        PULL_REQUEST,
        SUBMODULES,
        SUBMODULE,
        WORKTREES,
        WORKTREE,
        WORKTREE_MAIN
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode node) {
            Object userObject = node.getUserObject();

            if (userObject instanceof TreeNodeData data) {
                setText(data.displayName());
                setIcon(getIconForType(data.type()));

                boolean isCurrentLocalBranch = data.type() == NodeType.BRANCH
                        && data.data() instanceof ScmBranch branch
                        && branch.getBranchType() == ScmBranch.BranchType.LOCAL
                        && branch.isHead();

                Font baseFont = tree.getFont();
                setFont(isCurrentLocalBranch ? baseFont.deriveFont(Font.BOLD) : baseFont.deriveFont(Font.PLAIN));
                if (isCurrentLocalBranch && !sel) {
                    setForeground(currentBranchColor());
                }
            }
        }

        return this;
    }

    /** Highlight color for the current local branch node; falls back if the L&F doesn't define an accent color. */
    private static Color currentBranchColor() {
        Color accent = UIManager.getColor("Component.accentColor");
        return accent != null ? accent : new Color(0x2E7D32);
    }

    private Icon getIconForType(NodeType type) {
        return switch (type) {
            case WORKSPACE      -> Util.themeAwareIcon(FontAwesomeSolid.LAYER_GROUP, 14);
            case REPOSITORY     -> Util.themeAwareIcon(FontAwesomeSolid.DATABASE,    14);
            case WORKING_COPY   -> Util.themeAwareIcon(FontAwesomeSolid.EDIT,        14);
            case HISTORY        -> Util.themeAwareIcon(FontAwesomeSolid.HISTORY,     14);
            case LOCAL_BRANCHES -> Util.themeAwareIcon(FontAwesomeSolid.CODE_BRANCH, 14);
            case REMOTE_BRANCHES-> Util.themeAwareIcon(FontAwesomeSolid.CLOUD,       14);
            case TAGS           -> Util.themeAwareIcon(FontAwesomeSolid.TAGS,        14);
            case STASHES        -> Util.themeAwareIcon(FontAwesomeSolid.ARCHIVE,     14);
            case BRANCH         -> Util.themeAwareIcon(FontAwesomeSolid.CODE_BRANCH, 14);
            case BRANCH_FOLDER  -> Util.themeAwareIcon(FontAwesomeSolid.FOLDER,      14);
            case TAG            -> Util.themeAwareIcon(FontAwesomeSolid.TAG,         14);
            case STASH          -> Util.themeAwareIcon(FontAwesomeRegular.FILE,        14);
            case ROOT           -> Util.themeAwareIcon(FontAwesomeSolid.DATABASE,    14);
            case PULL_REQUESTS  -> Util.themeAwareIcon(FontAwesomeSolid.TASKS,       14);
            case PULL_REQUEST   -> Util.themeAwareIcon(FontAwesomeSolid.EXCHANGE_ALT,14);
            case SUBMODULES     -> Util.themeAwareIcon(FontAwesomeSolid.CUBES,       14);
            case SUBMODULE      -> Util.themeAwareIcon(FontAwesomeSolid.CUBE,        14);
            case WORKTREES      -> Util.themeAwareIcon(FontAwesomeSolid.SITEMAP,     14);
            case WORKTREE       -> Util.themeAwareIcon(FontAwesomeSolid.TREE, 14);
            case WORKTREE_MAIN  -> Util.themeAwareIcon(FontAwesomeSolid.HOME,        14);
        };
    }

}
