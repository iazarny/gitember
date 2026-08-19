package com.az.gitember.ui.workspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchTreeMouseAdapter extends MouseAdapter {

    private final JTree tree;
    private final SearchItemContextMenu popup;
    private JTextField searchField;

    public SearchTreeMouseAdapter(JTree tree, SearchItemContextMenu popup, JTextField searchField) {
        this.tree = tree;
        this.popup = popup;
        this.searchField = searchField;
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
        if (e.isPopupTrigger()) {
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

    @Override
    public void mouseClicked(MouseEvent e) {
        // Check for double left-click
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());

            // Ensure the click happened on an actual node, not empty space
            if (path != null) {
                DefaultMutableTreeNode node =   (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof SearchHit hit) {
                    tree.setSelectionPath(path);
                    SearchHitOpener.open(hit, searchField.getText());
                }
            }
        }
    }
}
