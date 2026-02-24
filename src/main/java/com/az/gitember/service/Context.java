package com.az.gitember.service;

import com.az.gitember.data.*;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Context {

    private final static Logger log = Logger.getLogger(Context.class.getName());

    private final static String OS = System.getProperty("os.name").toLowerCase();

    private static GitRepoService gitRepoService = new GitRepoService();
    private final static SettingService settingService = new SettingService();

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());

    // Property names
    public static final String PROP_REPOSITORY_PATH = "repositoryPath";
    public static final String PROP_WORKING_BRANCH = "workingBranch";
    public static final String PROP_SETTINGS = "settings";
    public static final String PROP_SCM_REV_COMMIT_DETAILS = "scmRevCommitDetails";
    public static final String PROP_LAST_CHANGES = "lastChanges";
    public static final String PROP_LFS_REPO = "lfsRepo";
    public static final String PROP_SHOW_LFS_FILES = "showLfsFiles";
    public static final String PROP_SELECTED_TREE_NAME = "selectedTreeName";
    public static final String PROP_BRANCH_FILTER = "branchFilter";
    public static final String PROP_REMOTE_BRANCHES = "remoteBranches";
    public static final String PROP_LOCAL_BRANCHES = "localBranches";
    public static final String PROP_TAGS = "tags";
    public static final String PROP_STASH = "stash";
    public static final String PROP_STATUS_LIST = "statusList";
    public static final String PROP_PLOT_COMMIT_LIST = "plotCommitList";
    public static final String PROP_FILE_HISTORY_TREE = "fileHistoryTree";
    public static final String PROP_FILE_HISTORY_NAME = "fileHistoryName";
    public static final String PROP_MAIN_PANE_NAME = "mainPaneName";
    public static final String PROP_SCM_STAT = "scmStat";
    public static final String PROP_SCM_STAT_LIST = "scmStatList";
    public static final String PROP_SCM_STAT_BRANCH_LIVE_TIME = "scmStatBranchLiveTime";
    public static final String PROP_SCM_STAT_LIST_PARAM = "scmStatListParam";
    public static final String PROP_SEARCH_VALUE = "searchValue";
    public static final String PROP_SEARCH_RESULT = "searchResult";
    public static final String PROP_PULL_REQUESTS = "pullRequests";

    // Fields
    private static String repositoryPath;
    private static ScmBranch workingBranch;
    private static Settings settings;
    private static ScmRevisionInformation scmRevCommitDetails;
    private static boolean lastChanges;
    private static boolean lfsRepo;
    private static boolean showLfsFiles;
    private static String selectedTreeName;
    private static String branchFilter = "";

    private static final List<ScmBranch> remoteBranchesRaw = new ArrayList<>();
    private static final List<ScmBranch> localBranchesRaw = new ArrayList<>();
    private static final List<ScmBranch> tagsRaw = new ArrayList<>();

    private static List<ScmBranch> remoteBranches = Collections.emptyList();
    private static List<ScmBranch> localBranches = Collections.emptyList();
    private static List<ScmBranch> tags = Collections.emptyList();
    private static List<ScmRevisionInformation> stash = Collections.emptyList();

    private static List<ScmItem> statusList = new ArrayList<>();
    private static List<PlotCommit> plotCommitList = new ArrayList<>();

    private static String fileHistoryTree;
    private static String fileHistoryName;
    private static String mainPaneName;
    private static ScmStat scmStat = new ScmStat();
    private static List<ScmStat> scmStatList = Collections.emptyList();
    private static List<AverageLiveTime> scmStatBranchLiveTime = Collections.emptyList();
    private static StatWPParameters scmStatListParam;
    private static String searchValue;
    private static Map<String, Set<String>> searchResult = Collections.emptyMap();
    private static List<PullRequest> pullRequests = Collections.emptyList();

    public static final Map<String, ScmRevisionInformation> scmRevisionInformationCache =
            new ConcurrentHashMap<>();

    private static ProjectWatcher projectWatcher;
    private static Thread projectWatcherThread;

    // Property change listener support
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    // Getters and setters with property change firing
    public static String getRepositoryPath() {
        return repositoryPath;
    }

    public static void setRepositoryPath(String value) {
        String old = repositoryPath;
        repositoryPath = value;
        pcs.firePropertyChange(PROP_REPOSITORY_PATH, old, value);
    }

    public static ScmBranch getWorkingBranch() {
        return workingBranch;
    }

    public static void setWorkingBranch(ScmBranch value) {
        ScmBranch old = workingBranch;
        workingBranch = value;
        pcs.firePropertyChange(PROP_WORKING_BRANCH, old, value);
    }

    public static Settings getSettings() {
        return settings;
    }

    public static void setSettings(Settings value) {
        Settings old = settings;
        settings = value;
        pcs.firePropertyChange(PROP_SETTINGS, old, value);
    }

    public static ScmRevisionInformation getScmRevCommitDetails() {
        return scmRevCommitDetails;
    }

    public static void setScmRevCommitDetails(ScmRevisionInformation value) {
        ScmRevisionInformation old = scmRevCommitDetails;
        scmRevCommitDetails = value;
        pcs.firePropertyChange(PROP_SCM_REV_COMMIT_DETAILS, old, value);
    }

    public static boolean isLastChanges() {
        return lastChanges;
    }

    public static void setLastChanges(boolean value) {
        boolean old = lastChanges;
        lastChanges = value;
        pcs.firePropertyChange(PROP_LAST_CHANGES, old, value);
    }

    public static boolean isLfsRepo() {
        return lfsRepo;
    }

    public static void setLfsRepo(boolean value) {
        boolean old = lfsRepo;
        lfsRepo = value;
        pcs.firePropertyChange(PROP_LFS_REPO, old, value);
    }

    public static boolean isShowLfsFiles() {
        return showLfsFiles;
    }

    public static void setShowLfsFiles(boolean value) {
        boolean old = showLfsFiles;
        showLfsFiles = value;
        pcs.firePropertyChange(PROP_SHOW_LFS_FILES, old, value);
    }

    public static String getSelectedTreeName() {
        return selectedTreeName;
    }

    public static void setSelectedTreeName(String value) {
        String old = selectedTreeName;
        selectedTreeName = value;
        pcs.firePropertyChange(PROP_SELECTED_TREE_NAME, old, value);
    }

    public static String getBranchFilter() {
        return branchFilter;
    }

    public static void setBranchFilter(String value) {
        String old = branchFilter;
        branchFilter = value != null ? value : "";
        pcs.firePropertyChange(PROP_BRANCH_FILTER, old, branchFilter);
        filterBranches();
        filterTags();
    }

    public static List<ScmBranch> getRemoteBranches() {
        return remoteBranches;
    }

    public static List<ScmBranch> getLocalBranches() {
        return localBranches;
    }

    public static List<ScmBranch> getTags() {
        return tags;
    }

    public static List<ScmRevisionInformation> getStash() {
        return stash;
    }

    public static List<ScmItem> getStatusList() {
        return statusList;
    }

    public static List<PlotCommit> getPlotCommitList() {
        return plotCommitList;
    }

    public static String getFileHistoryTree() {
        return fileHistoryTree;
    }

    public static void setFileHistoryTree(String value) {
        String old = fileHistoryTree;
        fileHistoryTree = value;
        pcs.firePropertyChange(PROP_FILE_HISTORY_TREE, old, value);
    }

    public static String getFileHistoryName() {
        return fileHistoryName;
    }

    public static void setFileHistoryName(String value) {
        String old = fileHistoryName;
        fileHistoryName = value;
        pcs.firePropertyChange(PROP_FILE_HISTORY_NAME, old, value);
    }

    public static String getMainPaneName() {
        return mainPaneName;
    }

    public static void setMainPaneName(String value) {
        String old = mainPaneName;
        mainPaneName = value;
        pcs.firePropertyChange(PROP_MAIN_PANE_NAME, old, value);
    }

    public static ScmStat getScmStat() {
        return scmStat;
    }

    public static void setScmStat(ScmStat value) {
        ScmStat old = scmStat;
        scmStat = value;
        pcs.firePropertyChange(PROP_SCM_STAT, old, value);
    }

    public static String getSearchValue() {
        return searchValue;
    }

    public static void setSearchValue(String value) {
        String old = searchValue;
        searchValue = value;
        pcs.firePropertyChange(PROP_SEARCH_VALUE, old, value);
    }

    public static Map<String, Set<String>> getSearchResult() {
        return searchResult;
    }

    public static void setSearchResult(Map<String, Set<String>> value) {
        Map<String, Set<String>> old = searchResult;
        searchResult = value;
        pcs.firePropertyChange(PROP_SEARCH_RESULT, old, value);
    }

    // Init
    private static void initProjectWatcher(String gitFolder) throws Exception {
        String projFolder = gitFolder
                .replace("/.git", "")
                .replace("\\.git", "");
        if (projectWatcherThread != null) {
            projectWatcherThread.interrupt();
        }
        projectWatcher = new ProjectWatcher(projFolder, (kind, fileName) -> {
            SwingUtilities.invokeLater(() -> Context.updateStatus(null, true));
        });
        projectWatcherThread = new Thread(projectWatcher);
        projectWatcherThread.setDaemon(true);
        projectWatcherThread.start();
    }

    public static void init(RemoteRepoParameters remoteRepoParameters) throws Exception {
        String gitFolder = remoteRepoParameters.getDestinationFolder();

        if (!gitFolder.endsWith(Const.GIT_FOLDER)) {
            gitFolder += File.separator + Const.GIT_FOLDER;
        }

        if (!new File(gitFolder).exists()) {
            throw new RuntimeException("Git folder " + gitFolder + " not found");
        }

        gitRepoService = new GitRepoService(gitFolder);
        setRepositoryPath(gitFolder);
        scmRevisionInformationCache.clear();
        setStash(gitRepoService.getStashList());
        setLfsRepo(getGitRepoService().isLfsRepo());
        setShowLfsFiles(false);

        updateBranches();
        updateTags();
        updateWorkingBranch();
        updateStatus(null);

        // Fill cache in background
        new Thread(() -> {
            gitRepoService.getCommitsByTree(null, true, 100, null).stream().forEach(
                    s -> gitRepoService.adapt(s, null)
            );
        }).start();

        initProjectWatcher(gitFolder);
        updatePullRequests();
    }

    public static void init(String gitFolder) throws Exception {
        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setDestinationFolder(gitFolder);
        init(params);
    }

    public static void saveSettings() {
        if (projectWatcherThread != null) {
            projectWatcherThread.interrupt();
        }
        settingService.write(settings);
    }

    public static Optional<Project> getCurrentProject() {
        String folder = getProjectFolder().replaceAll("[/\\\\]+$", "");
        return settings.getProjects().stream()
                .filter(p -> p.getProjectHomeFolder().replaceAll("[/\\\\]+$", "").equalsIgnoreCase(folder))
                .findFirst();
    }

    private static String lastTreeName;
    private static boolean lastAllHistory;

    public static void updatePlotCommitList(final ProgressMonitor progressMonitor) {
        updatePlotCommitList(lastTreeName, lastAllHistory, progressMonitor);
    }

    public static void updatePlotCommitList(final String treeName,
                                            final boolean allHistory,
                                            final ProgressMonitor progressMonitor) {
        final PlotCommitList<PlotLane> plotCommits = gitRepoService.getCommitsByTree(treeName, allHistory, -1, progressMonitor);

        List<PlotCommit> old = new ArrayList<>(plotCommitList);
        plotCommitList = new ArrayList<>(plotCommits);
        pcs.firePropertyChange(PROP_PLOT_COMMIT_LIST, old, plotCommitList);

        setSelectedTreeName(treeName);

        lastTreeName = treeName;
        lastAllHistory = allHistory;
    }

    public static void updateAll() {
        updateStatus(null);
        try {
            updateBranches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateTags();
        updateStash();
    }

    public static synchronized void updateStatus(ProgressMonitor progressMonitor) {
        updateStatus(progressMonitor, false);
    }

    public static synchronized void updateStatus(ProgressMonitor progressMonitor, boolean workingCopyOnly) {
        List<ScmItem> statuses = gitRepoService.getStatuses(progressMonitor, lastChanges);
        List<ScmItem> old = statusList;
        if (!workingCopyOnly) {
            List<PlotCommit> oldPlot = plotCommitList;
            plotCommitList = new ArrayList<>();
            pcs.firePropertyChange(PROP_PLOT_COMMIT_LIST, oldPlot, plotCommitList);
        }
        statusList = new ArrayList<>(statuses);
        pcs.firePropertyChange(PROP_STATUS_LIST, old, statusList);
    }

    public static void updateWorkingBranch() {
        setWorkingBranch(
                localBranches.stream().filter(ScmBranch::isHead).findFirst().orElse(null)
        );
    }

    private static final Object branchLock = new Object();
    private static final Object tagLock = new Object();

    public static void updateBranches() {
        synchronized (branchLock) {
            localBranchesRaw.clear();
            remoteBranchesRaw.clear();
            try {
                localBranchesRaw.addAll(gitRepoService.getBranches());
                remoteBranchesRaw.addAll(gitRepoService.getRemoteBranches());

                List<ScmBranch> oldLocal = localBranches;
                localBranches = new ArrayList<>(localBranchesRaw);
                pcs.firePropertyChange(PROP_LOCAL_BRANCHES, oldLocal, localBranches);

                List<ScmBranch> oldRemote = remoteBranches;
                remoteBranches = new ArrayList<>(remoteBranchesRaw);
                pcs.firePropertyChange(PROP_REMOTE_BRANCHES, oldRemote, remoteBranches);

                filterBranchesInternal();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot update branch information");
            }
        }
    }

    public static void filterBranches() {
        synchronized (branchLock) {
            filterBranchesInternal();
        }
    }

    private static void filterBranchesInternal() {
        String filter = (branchFilter != null ? branchFilter : "").toLowerCase(Locale.ROOT);
        List<ScmBranch> oldLocal = localBranches;
        localBranches = localBranchesRaw.stream()
                .filter(b -> b.getNameExt().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        pcs.firePropertyChange(PROP_LOCAL_BRANCHES, oldLocal, localBranches);

        List<ScmBranch> oldRemote = remoteBranches;
        remoteBranches = remoteBranchesRaw.stream()
                .filter(b -> b.getNameExt().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        pcs.firePropertyChange(PROP_REMOTE_BRANCHES, oldRemote, remoteBranches);
    }

    public static void updateTags() {
        synchronized (tagLock) {
            tagsRaw.clear();
            tagsRaw.addAll(gitRepoService.getTags());
            filterTagsInternal();
        }
    }

    public static void filterTags() {
        synchronized (tagLock) {
            filterTagsInternal();
        }
    }

    private static void filterTagsInternal() {
        String filter = (branchFilter != null ? branchFilter : "").toLowerCase();
        List<ScmBranch> old = tags;
        tags = tagsRaw.stream()
                .filter(b -> b.getNameExt().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        pcs.firePropertyChange(PROP_TAGS, old, tags);
    }

    public static void readSettings() {
        Settings s = settingService.read();
        setSettings(s);
    }

    public static void updateStash() {
        setStash(gitRepoService.getStashList());
    }

    public static List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    private static void setPullRequests(List<PullRequest> value) {
        List<PullRequest> old = pullRequests;
        pullRequests = value;
        pcs.firePropertyChange(PROP_PULL_REQUESTS, old, value);
    }

    /** Fetches open PRs in a daemon thread; fires PROP_PULL_REQUESTS on the EDT when done. */
    public static void updatePullRequests() {
        Thread t = new Thread(() -> {
            try {
                String remoteUrl = gitRepoService.getRepositoryRemoteUrl();
                String token = getCurrentProject().map(Project::getAccessToken).orElse(null);
                List<PullRequest> prs = PullRequestService.fetch(remoteUrl, token);
                SwingUtilities.invokeLater(() -> setPullRequests(prs));
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to update pull requests", e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static void setStash(List<ScmRevisionInformation> value) {
        List<ScmRevisionInformation> old = stash;
        stash = value;
        pcs.firePropertyChange(PROP_STASH, old, value);
    }

    public static GitRepoService getGitRepoService() {
        return gitRepoService;
    }

    public static SettingService getSettingService() {
        return settingService;
    }

    public static String getProjectFolder() {
        return (repositoryPath != null ? repositoryPath : "").replace(Const.GIT_FOLDER, "");
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isLinux() {
        return (OS.contains("linux"));
    }
}
