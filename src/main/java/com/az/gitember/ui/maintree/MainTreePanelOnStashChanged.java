package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for stash changes on {@link Context} and refreshes the corresponding
 * {@link MainTreePanel} node. Extracted from {@code MainTreePanel}.
 */
public class MainTreePanelOnStashChanged implements PropertyChangeListener {

    private final MainTreePanel panel;

    public MainTreePanelOnStashChanged(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() ->
                panel.populateStashes(panel.stashesNode, Context.getStash()));
    }
}
