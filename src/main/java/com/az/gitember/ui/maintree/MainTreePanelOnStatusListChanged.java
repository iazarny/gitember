package com.az.gitember.ui.maintree;

import com.az.gitember.service.Context;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for working-copy status changes on {@link Context} and refreshes the
 * Working Copy label and state label of {@link MainTreePanel}. Extracted from
 * {@code MainTreePanel}.
 */
public class MainTreePanelOnStatusListChanged implements PropertyChangeListener {

    private final MainTreePanel panel;

    public MainTreePanelOnStatusListChanged(MainTreePanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Context.isWorkspaceMode()) return;
        SwingUtilities.invokeLater(() -> {
            panel.updateWorkingCopyLabel();
            panel.updateStateLabel();
        });
    }
}
