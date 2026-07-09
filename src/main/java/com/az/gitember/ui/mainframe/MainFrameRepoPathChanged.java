package com.az.gitember.ui.mainframe;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainFrameRepoPathChanged implements PropertyChangeListener {

    private final MainFrame mainFrame;


    public MainFrameRepoPathChanged(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            boolean hasRepo = evt.getNewValue() != null;
            mainFrame.setRepoActionsEnabled(hasRepo);
            if (hasRepo || Context.isWorkspaceMode()) {
                mainFrame.getMainMenuBar().setVisible(true);
                mainFrame.addCurrentProjectToSettings();
                mainFrame.refreshProjectLists();
                // Switch from welcome to repo view
                mainFrame.getMainCardLayout().show(mainFrame.getMainCardPanel(), MainFrame.CARD_REPO);
            } else {
                mainFrame.getMainCardLayout().show(mainFrame.getMainCardPanel(), MainFrame.CARD_WELCOME);
            }
            mainFrame.updateTitle();
        });

    }
}
