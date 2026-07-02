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
public class MainTreePanelOnRepoChanged implements PropertyChangeListener {

    private final MainTreePanel panel;

    public MainTreePanelOnRepoChanged(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) {
            // The repository categories belong to per-project subtrees in workspace mode;
            // populating the shared node fields here is handled in a later step.
            SwingUtilities.invokeLater(panel::updateStateLabel);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            panel.refreshTree();
            panel.updateStateLabel();
            panel.tree.setSelectionPath(new TreePath(new Object[]{panel.rootNode, panel.workingCopyNode}));
        });
        panel.refreshWorktrees();
    }
}
