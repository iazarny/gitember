package com.az.gitember.ui.mainframe;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class WorkingBranchChangedChangeListener implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public WorkingBranchChangedChangeListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            ScmBranch branch = (ScmBranch) evt.getNewValue();
            String name = branch != null ? branch.getShortName() : "";
            mainFrame.getToolBar().setBranchName(name);
            mainFrame.getToolBar().updateSyncCounts(branch);
            mainFrame.updateTitle();
        });
    }
}
