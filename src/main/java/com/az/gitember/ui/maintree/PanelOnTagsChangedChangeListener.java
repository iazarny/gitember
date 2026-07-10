package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;
import com.az.gitember.ui.maintree.CellRenderer.NodeType;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for tag changes on {@link Context} and refreshes the corresponding
 * {@link MainTreePanel} node. Extracted from {@code MainTreePanel}.
 */
public class PanelOnTagsChangedChangeListener implements PropertyChangeListener {

    private final MainTreePanel panel;

    public PanelOnTagsChangedChangeListener(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() ->
                panel.populateBranches(panel.tagsNode, Context.getTags(), NodeType.TAG));
    }
}
