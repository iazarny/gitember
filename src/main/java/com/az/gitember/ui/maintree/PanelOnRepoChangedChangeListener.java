package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for repository-path changes on {@link Context} and refreshes the
 * {@link MainTreePanel} tree, state label and worktrees. Extracted from {@code MainTreePanel}.
 */
public class PanelOnRepoChangedChangeListener implements PropertyChangeListener {

    private final MainTreePanel panel;

    public PanelOnRepoChangedChangeListener(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) {
            // The repository categories belong to per-project subtrees in workspace mode;
            // populating the shared node fields here is handled in a later step.
            SwingUtilities.invokeLater(panel::updateStateLabel);
            //Context.getWorkspace().getProjects()
            //panel.refreshWorktrees();
        } else {
            // If the tree was previously built for a workspace, its structure is stale: the
            // shared category-node fields don't point into the visible tree, so refreshTree()
            // alone would keep showing the old workspace layout. Rebuild the single-repo tree
            // first (buildInitialTree resets workspaceNode and re-creates the shared nodes).
            if (panel.workspaceNode != null) {
                panel.rebuild();
            }
            SwingUtilities.invokeLater(() -> {
                panel.refreshTree();
                panel.updateStateLabel();
                panel.tree.setSelectionPath(new TreePath(new Object[]{panel.rootNode, panel.workingCopyNode}));
            });
            panel.refreshWorktrees();
        }

    }
}
