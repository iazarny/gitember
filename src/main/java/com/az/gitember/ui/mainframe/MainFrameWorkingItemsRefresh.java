package com.az.gitember.ui.mainframe;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Reloads the working-copy status list off the EDT in response to
 * {@link Context#PROP_WORKING_COPY_REFRESH}. The refreshed statuses are published back through
 * {@link Context#PROP_STATUS_LIST}, so the UI update itself is handled by
 * {@link MainFrameWorkingItemsChanged}.
 */
public class MainFrameWorkingItemsRefresh implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public MainFrameWorkingItemsRefresh(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Context.updateStatus(null, true);
                return null;
            }
        }.execute();
    }
}
