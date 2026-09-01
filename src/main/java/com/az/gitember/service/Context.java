package com.az.gitember.service;

import com.az.gitember.data.*;
import com.az.gitember.service.avatar.AvatarService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    private static ObjectMapper objectMapper = null;
    public static synchronized ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return objectMapper;
    }
    private final static SettingService settingService = new SettingService(getObjectMapper());

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

    /** Test-only: resets session state so test cases don't leak an active project/workspace. */
    static void reset() {
        setActiveProject(null);
        workspace = null;
        settings = null;
    }

    /** Signals listeners to reload the working-copy status list. */
    public static void refreshWorkingCopy() {
        fire(getActiveProject(), PROP_WORKING_COPY_REFRESH, false, true);
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

    private static  void setActiveProject(Project project) {
        activeProject = project;
    }

    /**
     * @deprecated legacy path-based setter, retained for its one remaining caller, which uses
     * it to clear the active project when the aggregated workspace view (rather than a single
     * repository) becomes active. A non-null value is a no-op -- open a repository via
     * {@link #init} or {@link #initRepoOnly} instead.
     */
    @Deprecated
    public static void setRepositoryPath(String value) {
        if (value == null) {
            Project old = getActiveProject();
            setActiveProject(null);
            fire(old, PROP_REPOSITORY_PATH, old == null ? null : old.getGitDir(), null);
        }
    }

    public static String getProjectFolder() {
        return getActiveProject() == null ? "" : getActiveProject().getProjectFolder();
    }

    /**
     * O(1): the currently active {@link Project}, if any. Unlike the historic implementation
     * (a path-based scan of {@code settings.getProjects()}), this also finds a project reached
     * only through a workspace.
     */
    public static Optional<Project> getCurrentProject() {
        return Optional.ofNullable(getActiveProject());
    }

    public static ScmBranch getWorkingBranch() {
        return getActiveProject() == null ? null : getActiveProject().getWorkingBranch();
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
                if (p != getActiveProject()) {
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
        return getActiveProject() != null && getActiveProject().isLfsRepo();
    }

    public static void setLfsRepo(boolean value) {
        if (getActiveProject() != null) {
            getActiveProject().setLfsRepo(value);
        }
    }

    public static List<ScmBranch> getRemoteBranches() {
        return getActiveProject() == null ? List.of() : getActiveProject().getRemoteBranches();
    }

    public static List<ScmBranch> getLocalBranches() {
        return getActiveProject() == null ? List.of() : getActiveProject().getLocalBranches();
    }

    public static List<ScmBranch> getTags() {
        return getActiveProject() == null ? List.of() : getActiveProject().getTags();
    }

    public static List<ScmRevisionInformation> getStash() {
        return getActiveProject() == null ? List.of() : getActiveProject().getStash();
    }

    public static List<ScmItem> getStatusList() {
        return getActiveProject() == null ? List.of() : getActiveProject().getStatusList();
    }

    public static List<PlotCommit> getPlotCommitList() {
        return getActiveProject() == null ? List.of() : getActiveProject().getPlotCommitList();
    }

    private static Project resolveProject(String pathOrGitDir) {
        if (settings == null) {
            settings = new Settings();
        }
        return settings.getOrCreateProject(pathOrGitDir);
    }

    public static void initRepoOnly(Project project) throws Exception {
        Project old = getActiveProject();
        String oldPath = old == null ? null : old.getGitDir();
        project.openRepoService();             // may throw -- nothing mutated yet
        if (old != null && old != project) {
            old.stopWatcher();
            old.closeRepoService();
        }
        setActiveProject(project);               // assign before firing
        fire(project, PROP_REPOSITORY_PATH, oldPath, project.getGitDir());
    }

    public static void initRepoOnly(String gitFolder) throws Exception {
        initRepoOnly(resolveProject(gitFolder));
    }

    public static void init(Project project) throws Exception {
        AvatarService.clearCache();
        Project old = getActiveProject();
        String oldPath = old == null ? null : old.getGitDir();
        project.openRepoService();             // may throw -- nothing mutated yet
        if (old != null && old != project) {
            old.stopWatcher();
        }
        setActiveProject(project);               // assign before firing
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
        if (getActiveProject() != null) {
            getActiveProject().updatePlotCommitList(treeName, allHistory, progressMonitor);
        }
    }

    /** Signals listeners (e.g. HistoryPanel) to reload the commit history. */
    public static void refreshHistory() {
        fire(getActiveProject(), PROP_HISTORY_REFRESH, false, true);
    }

    /**
     * Asks the main frame to switch to the history view and scroll to / select
     * the commit identified by {@code sha} (full or abbreviated SHA-1).
     * Call this on the EDT after a successful pull.
     */
    public static void navigateToHistory(String sha) {
        fire(getActiveProject(), PROP_NAVIGATE_TO_HISTORY, null, sha);
    }

    /**
     * Asks the main frame to switch to the working copy view.
     * Call this on the EDT after a pull that produced conflicts.
     */
    public static void navigateToWorkingCopy() {
        fire(getActiveProject(), PROP_NAVIGATE_TO_WORKING_COPY, false, true);
    }

    public static void updateAll() {
        if (getActiveProject() != null) {
            getActiveProject().updateAll();
        }
    }

    public static void updateStatus(ProgressMonitor progressMonitor) {
        updateStatus(progressMonitor, false);
    }

    public static void updateStatus(ProgressMonitor progressMonitor, boolean workingCopyOnly) {
        if (getActiveProject() != null) {
            getActiveProject().updateStatus(progressMonitor, workingCopyOnly);
        }
    }

    public static void updateWorkingBranch() {
        if (getActiveProject() != null) {
            getActiveProject().updateWorkingBranch();
        }
    }

    public static void updateBranches() {
        if (getActiveProject() != null) {
            getActiveProject().updateBranches();
        }
    }

    public static void updateTags() {
        if (getActiveProject() != null) {
            getActiveProject().updateTags();
        }
    }

    public static Settings readSettings() {
        Settings s = settingService.read();
        // Seed the ignore list with built-in defaults on first run (or after migration
        // from a version that stored an empty set as the "use defaults" sentinel).
        if (s.getIgnoreCompareFiles() == null || s.getIgnoreCompareFiles().isEmpty()) {
            s.setIgnoreCompareFiles(new java.util.TreeSet<>(com.az.gitember.data.Settings.DEFAULT_IGNORE_COMPARE_FILES));
            settingService.write(s);
        }
        s.internAll();
        setSettings(s);
        return s;
    }

    public static void updateStash() {
        if (getActiveProject() != null) {
            getActiveProject().updateStash();
        }
    }

    public static List<PullRequest> getPullRequests() {
        return getActiveProject() == null ? List.of() : getActiveProject().getPullRequests();
    }

    public static List<Submodule> getSubmodules() {
        return getActiveProject() == null ? List.of() : getActiveProject().getSubmodules();
    }

    /** Refreshes the submodule list in a daemon thread; fires PROP_SUBMODULES on the EDT when done. */
    public static void updateSubmodules() {
        if (getActiveProject() != null) {
            getActiveProject().updateSubmodules();
        }
    }

    /** Fetches open PRs in a daemon thread; fires PROP_PULL_REQUESTS on the EDT when done. */
    public static void updatePullRequests() {
        if (getActiveProject() != null) {
            getActiveProject().updatePullRequests();
        }
    }

    public static GitRepoService getGitRepoService() {
        return getActiveProject() == null ? NO_REPO : getActiveProject().getGitRepoService();
    }

}
