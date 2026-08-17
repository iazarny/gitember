package com.az.gitember.ui.mainframe;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * After a conflicting pull, switches to the working-copy view so the conflicts are visible,
 * in response to {@link Context#PROP_NAVIGATE_TO_WORKING_COPY}.
 */
public class NavigateToWorkingCopyChangeListener implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public NavigateToWorkingCopyChangeListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        mainFrame.showWorkingCopy();
    }
}
