package com.az.gitember.ui.mainframe;

import com.az.gitember.data.Workspace;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import java.util.Date;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class OpenRecentWorkspaceHanlder implements Consumer<Workspace> {

    private static final Logger log = Logger.getLogger(OpenRecentWorkspaceHanlder.class.getName());

    private final MainFrame mainFrame;

    public OpenRecentWorkspaceHanlder(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }


    @Override
    public void accept(Workspace workspace) {
        if (workspace != null)  {
            workspace.setOpenTime(new Date());
            Context.saveSettings();

            Context.setWorkspace(workspace);
            mainFrame.getTreePanel().rebuild();                       // build workspace tree (selects workspace node)
            mainFrame.getWorkspaceDashboardPanel().setWorkspace(workspace);
            mainFrame.getContentPanel().setContent(mainFrame.getWorkspaceDashboardPanel());
            mainFrame.getMainCardLayout().show(mainFrame.getMainCardPanel(), MainFrame.CARD_REPO);
            mainFrame.setTitle("Gitember - " + workspace.getName());
            mainFrame.getStatusBar().setStatus("Workspace opened: " + workspace.getName());
            MainFrame.getInstance().getToolBar().setVisible(true);

        }
    }
}
