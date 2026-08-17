package com.az.gitember.ui.workspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchTreeMouseAdapter extends MouseAdapter {

    private final JTree tree;
    private final SearchItemContextMenu popup;

    public SearchTreeMouseAdapter(JTree tree, SearchItemContextMenu popup) {
        this.tree = tree;
        this.popup = popup;
    }


    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }

        TreePath path = tree.getPathForLocation(e.getX(), e.getY());

        if (path != null) {
            DefaultMutableTreeNode node =   (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.isLeaf()) {
                tree.setSelectionPath(path);
                popup.setSelectedNode(node);
                popup.show(tree, e.getX(), e.getY());
            }
        }

    }

}
