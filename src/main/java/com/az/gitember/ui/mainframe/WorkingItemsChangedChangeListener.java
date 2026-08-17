package com.az.gitember.ui.mainframe;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class WorkingItemsChangedChangeListener implements PropertyChangeListener {

    private final MainFrame mainFrame;

    public WorkingItemsChangedChangeListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            List<ScmItem> statusList = Context.getStatusList();
            mainFrame.getWorkingCopyPanel().setItems(statusList);
            boolean commitEnabled = mainFrame.isCommitEnabled();
            mainFrame.getToolBar().setCommitEnabled( commitEnabled);
            mainFrame.getMainMenuBar().setCommitEnabled( commitEnabled);
            mainFrame.getMainMenuBar().setCreateDiffEnabled(statusList != null && !statusList.isEmpty());
        });
    }
}
