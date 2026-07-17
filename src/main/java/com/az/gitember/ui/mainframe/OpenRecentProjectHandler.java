package com.az.gitember.ui.mainframe;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.dialog.InteractiveContinueAbortDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenRecentProjectHandler implements Consumer<Project>  {

    private static final Logger log = Logger.getLogger(OpenRecentProjectHandler.class.getName());

    private final MainFrame mainFrame;

    public OpenRecentProjectHandler(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }




    @Override
    public void accept(Project project) {
        String folder = project.getProjectHomeFolder();
        mainFrame.getStatusBar().setStatus("Opening " + folder + "...");
        mainFrame.getStatusBar().showProgress(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.setWorkspace(null);
                Context.init(folder);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    mainFrame.addCurrentProjectToSettings();
                    mainFrame.refreshProjectLists();
                    mainFrame.getStatusBar().clearProgress();
                    mainFrame.getStatusBar().setStatus("Repository opened");
                    InteractiveContinueAbortDialog.showIfRebaseInProgress(
                            mainFrame, mainFrame.getStatusBar(),
                            () -> mainFrame.getHistoryPanel().loadHistory(null, true));
                    //MainFrame.getMainMenuBar().setVisible(true);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    log.log(Level.WARNING, "Failed to open project", cause);
                    mainFrame.getStatusBar().clearProgress();
                    mainFrame.getStatusBar().setStatus("Failed to open: " + cause.getMessage());

                    // Remove invalid project from list
                    Settings settings = Context.getSettings();
                    if (settings != null) {
                        settings.getProjects().remove(project);
                        Context.saveSettings();
                        mainFrame.refreshProjectLists();
                    }

                    JOptionPane.showMessageDialog(mainFrame,
                            "Cannot open repository: " + folder
                                    + "\nIt will be removed from the list of recent projects.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

}
