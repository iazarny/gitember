package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;
import com.az.gitember.ui.maintree.MainTreeCellRenderer.NodeType;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for local/remote branch changes on {@link Context} and refreshes the
 * corresponding {@link MainTreePanel} nodes. Extracted from {@code MainTreePanel}.
 */
public class MainTreePanelOnBranchChanged implements PropertyChangeListener {

    private final MainTreePanel panel;

    public MainTreePanelOnBranchChanged(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() -> {
            panel.populateBranches(panel.localBranchesNode, Context.getLocalBranches(), NodeType.BRANCH);
            panel.populateBranches(panel.remoteBranchesNode, Context.getRemoteBranches(), NodeType.BRANCH);
            panel.updateStateLabel();
        });
    }
}
