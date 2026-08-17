package com.az.gitember.ui.mainframe;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * After a successful pull, switches to the history view and highlights the pulled commit,
 * in response to {@link Context#PROP_NAVIGATE_TO_HISTORY} (new value = commit SHA).
 */
public class NavigateToHistoryChangeListener implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public NavigateToHistoryChangeListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        mainFrame.showCommitInHistory((String) evt.getNewValue());
    }
}
