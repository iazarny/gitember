package com.az.gitember.data;

import com.az.gitember.ui.maintree.CellRenderer;

public record TreeNodeData(String displayName, CellRenderer.NodeType type, Object data) {

    //public TreeNodeData(String displayName, MainTreeCellRenderer.NodeType type) {
    //    this(displayName, type, null);
    //}

    @Override
    public String toString() {
        return displayName;
    }
}