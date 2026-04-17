package com.az.gitember.ui;

import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class MainTreeCellRenderer extends DefaultTreeCellRenderer {

    public enum NodeType {
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
        WORKTREE
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
            }
        }

        return this;
    }

    private Icon getIconForType(NodeType type) {
        return switch (type) {
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
            case WORKTREE       -> Util.themeAwareIcon(FontAwesomeSolid.FOLDER_OPEN, 14);
        };
    }

    public record TreeNodeData(String displayName, NodeType type, Object data) {
        public TreeNodeData(String displayName, NodeType type) {
            this(displayName, type, null);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
