package com.az.gitember.service;

import com.az.gitember.data.*;
import com.az.gitember.service.avatar.AvatarService;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revplot.PlotCommit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Optional;

/**
 * Application/session state: the loaded {@link Settings}, the currently open {@link Workspace}
 * (if any), the currently active {@link Project}, and the single JVM-wide property-change bus.
 *
 * <p>Everything specific to <em>one</em> repository (branches, tags, stashes, status list, plot
 * commits, PRs, submodules, the LFS flag, the file watcher, the commit-detail cache, and the
 * {@link GitRepoService} itself) lives on {@link Project} now. The static methods below that look
 * repo-scoped (e.g. {@link #getGitRepoService()}, {@link #updateBranches()}) are thin, null-safe
 * delegates to {@link #getActiveProject()} kept so the many existing call sites keep compiling.
 */
public class Context {

    private static Workspace workspace = null;
    private static Project   activeProject;
    private final static SettingService settingService = new SettingService();

    /** Returned by {@link #getGitRepoService()} when no project is active. */
    private static final GitRepoService NO_REPO = new GitRepoService();

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());
    private static final Object BUS_OWNER = new Object();

    // Property names
    public static final String PROP_REPOSITORY_PATH = "repositoryPath";
    public static final String PROP_WORKING_BRANCH = "workingBranch";
    public static final String PROP_SETTINGS = "settings";
    public static final String PROP_LFS_REPO = "lfsRepo";
    public static final String PROP_REMOTE_BRANCHES = "remoteBranches";
    public static final String PROP_LOCAL_BRANCHES = "localBranches";
    public static final String PROP_TAGS = "tags";
    public static final String PROP_STASH = "stash";
    public static final String PROP_STATUS_LIST = "statusList";
    public static final String PROP_PLOT_COMMIT_LIST = "plotCommitList";
    public static final String PROP_HISTORY_REFRESH      = "historyRefresh";
    public static final String PROP_WORKING_COPY_REFRESH = "workingCopyRefresh";

    /**
     * Fired after a successful pull to ask the main frame to switch to the
     * history view and select the commit at the given SHA.
     * The property's new value is the full or abbreviated SHA string.
     */
    public static final String PROP_NAVIGATE_TO_HISTORY      = "navigateToHistory";

    /**
     * Fired after a conflicting pull to ask the main frame to switch to the
     * working copy view so the user sees the conflicted files immediately.
     */
    public static final String PROP_NAVIGATE_TO_WORKING_COPY = "navigateToWorkingCopy";
    public static final String PROP_PULL_REQUESTS = "pullRequests";
    public static final String PROP_SUBMODULES    = "submodules";

    // Fields
    private static Settings settings;

    /** Signals listeners to reload the working-copy status list. */
    public static void refreshWorkingCopy() {
        fire(activeProject, PROP_WORKING_COPY_REFRESH, false, true);
    }

    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /** Lets {@link Project} publish on the single global bus with itself as the event source. */
    public static void fire(Object source, String prop, Object oldValue, Object newValue) {
        pcs.firePropertyChange(new PropertyChangeEvent(source != null ? source : BUS_OWNER, prop, oldValue, newValue));
    }

    public static Project getActiveProject() {
        return activeProject;
    }

    /**
     * @deprecated legacy path-based setter, retained for its one remaining caller, which uses
     * it to clear the active project when the aggregated workspace view (rather than a single
     * repository) becomes active. A non-null value is a no-op -- open a repository via
     * {@link #init} or {@link #initRepoOnly} instead.
     */
    @Deprecated
    public static void setRepositoryPath(String value) {
        if (value != null) {
            return;
        }
        Project old = activeProject;
        activeProject = null;
        fire(old, PROP_REPOSITORY_PATH, old == null ? null : old.getGitDir(), null);
    }

    // Getters and setters with property change firing
    public static String getRepositoryPath() {
        return activeProject == null ? null : activeProject.getGitDir();
    }

    public static String getProjectFolder() {
        return activeProject == null ? "" : activeProject.getProjectFolder();
    }

    /**
     * @deprecated superseded by interning (see {@link Settings#internAll()}): every repo now has
     * exactly one {@link Project} instance shared by the flat recent list and every workspace, so
     * there is nothing left to reconcile across "similar" copies. Kept only for its one remaining
     * caller until that is updated.
     */
    @Deprecated
    public static List<Project> getSimilarToCurrentProjects() {
        return activeProject == null ? List.of() : List.of(activeProject);
    }

    /**
     * O(1): the currently active {@link Project}, if any. Unlike the historic implementation
     * (a path-based scan of {@code settings.getProjects()}), this also finds a project reached
     * only through a workspace.
     */
    public static Optional<Project> getCurrentProject() {
        return Optional.ofNullable(activeProject);
    }

    public static ScmBranch getWorkingBranch() {
        return activeProject == null ? null : activeProject.getWorkingBranch();
    }

    public static void setWorkingBranch(ScmBranch value) {
        if (activeProject != null) {
            activeProject.setWorkingBranch(value);
        }
    }

    public static Settings getSettings() {
        return settings;
    }

    public static void setSettings(Settings value) {
        Settings old = settings;
        settings = value;
        fire(BUS_OWNER, PROP_SETTINGS, old, value);
    }

    public static Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Opens {@code w} (or closes the current workspace when {@code w} is {@code null}).
     * Any project of the outgoing workspace that is not the active project has its
     * {@link GitRepoService} released — projects can otherwise stay open indefinitely
     * while a workspace is browsed.
     */
    public static void setWorkspace(Workspace w) {
        Workspace old = workspace;
        workspace = w;
        if (old != null && old != w) {
            for (Project p : old.getProjects()) {
                if (p != activeProject) {
                    p.closeRepoService();
                }
            }
        }
    }

    /** True when the tree should represent a workspace (a set of repositories). */
    public static boolean isWorkspaceMode() {
        return Context.getWorkspace() != null;
    }

    public static boolean isLfsRepo() {
        return activeProject != null && activeProject.isLfsRepo();
    }

    public static void setLfsRepo(boolean value) {
        if (activeProject != null) {
            activeProject.setLfsRepo(value);
        }
    }

    public static List<ScmBranch> getRemoteBranches() {
        return activeProject == null ? List.of() : activeProject.getRemoteBranches();
    }

    public static List<ScmBranch> getLocalBranches() {
        return activeProject == null ? List.of() : activeProject.getLocalBranches();
    }

    public static List<ScmBranch> getTags() {
        return activeProject == null ? List.of() : activeProject.getTags();
    }

    public static List<ScmRevisionInformation> getStash() {
        return activeProject == null ? List.of() : activeProject.getStash();
    }

    public static List<ScmItem> getStatusList() {
        return activeProject == null ? List.of() : activeProject.getStatusList();
    }

    public static List<PlotCommit> getPlotCommitList() {
        return activeProject == null ? List.of() : activeProject.getPlotCommitList();
    }

    private static Project resolveProject(String pathOrGitDir) {
        if (settings == null) {
            settings = new Settings();
        }
        return settings.getOrCreateProject(pathOrGitDir);
    }

    public static void initRepoOnly(Project project) throws Exception {
        Project old = activeProject;
        String oldPath = old == null ? null : old.getGitDir();
        project.openRepoService();             // may throw -- nothing mutated yet
        if (old != null && old != project) {
            old.stopWatcher();
        }
        activeProject = project;               // assign before firing
        fire(project, PROP_REPOSITORY_PATH, oldPath, project.getGitDir());
    }

    public static void initRepoOnly(String gitFolder) throws Exception {
        initRepoOnly(resolveProject(gitFolder));
    }

    public static void init(Project project) throws Exception {
        AvatarService.clearCache();
        Project old = activeProject;
        String oldPath = old == null ? null : old.getGitDir();
        project.openRepoService();             // may throw -- nothing mutated yet
        if (old != null && old != project) {
            old.stopWatcher();
        }
        activeProject = project;               // assign before firing
        fire(project, PROP_REPOSITORY_PATH, oldPath, project.getGitDir());
        project.initAfterOpen();
    }

    public static void init(String gitFolder) throws Exception {
        init(resolveProject(gitFolder));
    }

    public static void saveSettings() {
        settingService.write(settings);
    }

    public static void updatePlotCommitList(final String treeName,
                                            final boolean allHistory,
                                            final ProgressMonitor progressMonitor) {
        if (activeProject != null) {
            activeProject.updatePlotCommitList(treeName, allHistory, progressMonitor);
        }
    }

    /** Signals listeners (e.g. HistoryPanel) to reload the commit history. */
    public static void refreshHistory() {
        fire(activeProject, PROP_HISTORY_REFRESH, false, true);
    }

    /**
     * Asks the main frame to switch to the history view and scroll to / select
     * the commit identified by {@code sha} (full or abbreviated SHA-1).
     * Call this on the EDT after a successful pull.
     */
    public static void navigateToHistory(String sha) {
        fire(activeProject, PROP_NAVIGATE_TO_HISTORY, null, sha);
    }

    /**
     * Asks the main frame to switch to the working copy view.
     * Call this on the EDT after a pull that produced conflicts.
     */
    public static void navigateToWorkingCopy() {
        fire(activeProject, PROP_NAVIGATE_TO_WORKING_COPY, false, true);
    }

    public static void updateAll() {
        if (activeProject != null) {
            activeProject.updateAll();
        }
    }

    public static void updateStatus(ProgressMonitor progressMonitor) {
        updateStatus(progressMonitor, false);
    }

    public static void updateStatus(ProgressMonitor progressMonitor, boolean workingCopyOnly) {
        if (activeProject != null) {
            activeProject.updateStatus(progressMonitor, workingCopyOnly);
        }
    }

    public static void updateWorkingBranch() {
        if (activeProject != null) {
            activeProject.updateWorkingBranch();
        }
    }

    public static void updateBranches() {
        if (activeProject != null) {
            activeProject.updateBranches();
        }
    }

    public static void updateTags() {
        if (activeProject != null) {
            activeProject.updateTags();
        }
    }

    public static void readSettings() {
        Settings s = settingService.read();
        // Seed the ignore list with built-in defaults on first run (or after migration
        // from a version that stored an empty set as the "use defaults" sentinel).
        if (s.getIgnoreCompareFiles() == null || s.getIgnoreCompareFiles().isEmpty()) {
            s.setIgnoreCompareFiles(new java.util.TreeSet<>(com.az.gitember.data.Settings.DEFAULT_IGNORE_COMPARE_FILES));
            settingService.write(s);
        }
        s.internAll();
        setSettings(s);
    }

    public static void updateStash() {
        if (activeProject != null) {
            activeProject.updateStash();
        }
    }

    public static List<PullRequest> getPullRequests() {
        return activeProject == null ? List.of() : activeProject.getPullRequests();
    }

    public static List<Submodule> getSubmodules() {
        return activeProject == null ? List.of() : activeProject.getSubmodules();
    }

    /** Refreshes the submodule list in a daemon thread; fires PROP_SUBMODULES on the EDT when done. */
    public static void updateSubmodules() {
        if (activeProject != null) {
            activeProject.updateSubmodules();
        }
    }

    /** Fetches open PRs in a daemon thread; fires PROP_PULL_REQUESTS on the EDT when done. */
    public static void updatePullRequests() {
        if (activeProject != null) {
            activeProject.updatePullRequests();
        }
    }

    public static GitRepoService getGitRepoService() {
        return activeProject == null ? NO_REPO : activeProject.getGitRepoService();
    }

}
