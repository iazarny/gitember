package com.az.gitember.ui.mainframe;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Refreshes the submodule panel in response to {@link Context#PROP_SUBMODULES}.
 */
public class SubmodulesChangedChangeListener implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public SubmodulesChangedChangeListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        mainFrame.getSubmodulePanel().setSubmodules(Context.getSubmodules());
    }
}
