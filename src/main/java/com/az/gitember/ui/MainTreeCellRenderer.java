package com.az.gitember.ui;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.swing.FontIcon;

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
        TAG,
        STASH,
        ROOT
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
            case WORKING_COPY -> FontIcon.of(FontAwesomeSolid.EDIT, 14);
            case HISTORY -> FontIcon.of(FontAwesomeSolid.HISTORY, 14);
            case LOCAL_BRANCHES -> FontIcon.of(FontAwesomeSolid.CODE_BRANCH, 14);
            case REMOTE_BRANCHES -> FontIcon.of(FontAwesomeSolid.CLOUD, 14);
            case TAGS -> FontIcon.of(FontAwesomeSolid.TAGS, 14);
            case STASHES -> FontIcon.of(FontAwesomeSolid.ARCHIVE, 14);
            case BRANCH -> FontIcon.of(FontAwesomeSolid.CODE_BRANCH, 14);
            case TAG -> FontIcon.of(FontAwesomeSolid.TAG, 14);
            case STASH -> FontIcon.of(FontAwesomeRegular.FILE, 14);
            case ROOT -> FontIcon.of(FontAwesomeSolid.DATABASE, 14);
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
