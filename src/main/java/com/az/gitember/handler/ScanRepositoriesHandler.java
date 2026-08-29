package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.az.gitember.service.RepositoryScanService;
import com.az.gitember.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background scan of the conventional project folders under $HOME for git repositories,
 * adding each one found to the recent-projects list. Started on first run only — when the
 * settings file holds no repositories yet — see {@code MainFrame#scanForRepositoriesIfNoneKnown}.
 *
 * <p>Repositories are added with no open time: they have never been opened in Gitember, so
 * the welcome screen shows them without a date and sorts them after the ones that were.
 */
public class ScanRepositoriesHandler extends AbstractAsyncHandler<List<String>> {

    private static final Logger log = Logger.getLogger(ScanRepositoriesHandler.class.getName());

    private final MainFrame mainFrame;

    public ScanRepositoriesHandler(MainFrame mainFrame) {
        super(mainFrame);
        this.mainFrame = mainFrame;
    }

    @Override
    protected String getOperationName() {
        return "Looking for git repositories";
    }

    @Override
    protected List<String> doInBackground() {
        return new RepositoryScanService().scan(
                home -> SwingUtilities.invokeLater(() -> addFoundProject(home)));
    }

    /** Adds one discovered repository to the recent list and refreshes the welcome screen. */
    private void addFoundProject(String projectHomeFolder) {
        Settings settings = Context.getSettings();
        if (settings == null) {
            return;
        }
        settings.addRecentProject(new Project(projectHomeFolder, null));
        mainFrame.refreshProjectLists();
    }

    @Override
    protected void onSuccess(List<String> found) {
        if (found.isEmpty()) {
            statusBar.setStatus("No git repositories found in the usual project folders");
            return;
        }
        // The projects themselves were added as they were found; persist them once, at the end.
        Context.saveSettings();
        mainFrame.refreshProjectLists();
        statusBar.setStatus("Found " + found.size()
                + (found.size() == 1 ? " git repository" : " git repositories"));
    }

    /**
     * Log only — this scan is a startup convenience the user did not ask for, so a failure
     * must not raise the modal error dialog of the base class.
     */
    @Override
    protected void onError(Exception e) {
        log.log(Level.WARNING, "Scan for git repositories failed", e);
        statusBar.clearProgress();
        statusBar.setStatus("Ready");
    }
}
