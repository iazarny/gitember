package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for submodule changes on {@link Context} and refreshes the corresponding
 * {@link MainTreePanel} node. Extracted from {@code MainTreePanel}.
 */
public class MainTreePanelOnSubmodulesChanged implements PropertyChangeListener {

    private final MainTreePanel panel;

    public MainTreePanelOnSubmodulesChanged(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() -> panel.updateSubmodulesNode(Context.getSubmodules()));
    }
}
