package com.az.gitember.data;

import com.az.gitember.service.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A single git repository/work-tree: its persisted identity and credentials, plus (once opened)
 * its runtime state -- branches, tags, stash, working-copy status, plot commits, pull requests,
 * submodules, the LFS flag, the file watcher, the per-repo commit-detail cache, and the
 * {@link GitRepoService} itself.
 *
 * <p>The runtime fields are {@code @JsonIgnore}/{@code transient}: only the fields declared above
 * {@link #normalizeHome} are persisted to {@code gitember2.json}. Mutators publish through
 * {@link Context#fire(Object, String, Object, Object)} with {@code this} as the event source, on
 * the single JVM-wide bus also used by {@link Context}.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class Project implements Serializable, Comparable<Project>  {

    @JsonIgnore
    private static final transient Logger log = Logger.getLogger(Project.class.getName());

    private String projectHomeFolder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;
    private String userName;

    protected String empId;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String userPwd;
    private String userKey;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String keyPass;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String accessToken;
    private String userCommitName;   // author name
    private String userCommitEmail;  // author email
    private String committerName;
    private String committerEmail;
    private boolean indexed;
    private boolean showAllPullRequests = false; // when true, merged/closed PRs are listed too

    // ── runtime state: not persisted ────────────────────────────────────────────────────────

    @JsonIgnore
    private transient volatile GitRepoService gitRepoService;
    @JsonIgnore
    private final transient Object serviceLock = new Object();

    @JsonIgnore
    private transient volatile ScmBranch workingBranch;

    @JsonIgnore
    private final transient List<ScmBranch> localBranchesRaw = new ArrayList<>();
    @JsonIgnore
    private final transient List<ScmBranch> remoteBranchesRaw = new ArrayList<>();
    @JsonIgnore
    private final transient List<ScmBranch> tagsRaw = new ArrayList<>();

    @JsonIgnore
    private transient volatile List<ScmBranch> localBranches = Collections.emptyList();
    @JsonIgnore
    private transient volatile List<ScmBranch> remoteBranches = Collections.emptyList();
    @JsonIgnore
    private transient volatile List<ScmBranch> tags = Collections.emptyList();
    @JsonIgnore
    private transient volatile List<ScmRevisionInformation> stash = Collections.emptyList();

    @JsonIgnore
    private transient volatile List<ScmItem> statusList = new ArrayList<>();
    @JsonIgnore
    private transient volatile List<PlotCommit> plotCommitList = new ArrayList<>();
    @JsonIgnore
    private transient volatile List<PullRequest> pullRequests = Collections.emptyList();
    @JsonIgnore
    private transient volatile List<Submodule> submodules = Collections.emptyList();
    @JsonIgnore
    private transient volatile boolean lfsRepo;

    @JsonIgnore
    private final transient Map<String, ScmRevisionInformation> scmRevisionInformationCache = new ConcurrentHashMap<>();

    @JsonIgnore
    private final transient Object branchLock = new Object();
    @JsonIgnore
    private final transient Object tagLock = new Object();
    @JsonIgnore
    private final transient Object statusLock = new Object();

    @JsonIgnore
    private transient ProjectWatcher projectWatcher;
    @JsonIgnore
    private transient Thread projectWatcherThread;
    @JsonIgnore
    private transient Thread cacheWarmThread;

    public Project() {
    }

    public Project(String projectHomeFolder, Date openTime) {
        this.projectHomeFolder = normalizeHome(projectHomeFolder);
        this.openTime = openTime;
    }

    /**
     * Canonical work-tree path: absolute, no trailing separator, no trailing ".git" SEGMENT.
     * A directory literally named "foo.git" (bare-clone convention) is NOT stripped — only
     * a "&lt;separator&gt;.git" tail is.
     */
    public static String normalizeHome(String path) {
        if (path == null) {
            return null;
        }
        String p = path.trim().replaceAll("[/\\\\]+$", "");
        if (p.endsWith(File.separator + Const.GIT_FOLDER)
                || p.endsWith("/" + Const.GIT_FOLDER)
                || p.endsWith("\\" + Const.GIT_FOLDER)) {
            p = p.substring(0, p.length() - (Const.GIT_FOLDER.length() + 1))
                    .replaceAll("[/\\\\]+$", "");
        }
        return p;
    }

    /** Identity key: {@link #normalizeHome} lower-cased with {@link Locale#ROOT} (never the default locale). */
    public static String canonicalKey(String path) {
        String n = normalizeHome(path);
        return n == null ? "" : n.toLowerCase(Locale.ROOT);
    }

    /**
     * The git dir for this project. Mirrors the rule historically applied by
     * {@code Context.initRepoOnly}, including the bare-repo case where the home
     * folder itself already ends with ".git".
     */
    @JsonIgnore
    public String getGitDir() {
        String home = projectHomeFolder;
        if (home == null) {
            return null;
        }
        return home.endsWith(Const.GIT_FOLDER) ? home : home + File.separator + Const.GIT_FOLDER;
    }

    /**
     * Legacy-compatible work-tree path — KEEPS a trailing separator, because
     * {@code SearchService} md5-hashes this string to locate the Lucene index
     * directory; changing it would silently orphan every existing history index.
     */
    @JsonIgnore
    public String getProjectFolder() {
        String g = getGitDir();
        if (g == null) {
            return "";
        }
        return g.endsWith(Const.GIT_FOLDER) ? g.substring(0, g.length() - Const.GIT_FOLDER.length()) : g;
    }

    /**
     * Fills blanks on {@code this} from {@code other}. Used when two deserialized copies
     * of the same repo (one from the flat recent list, one from a workspace) collapse into
     * a single interned instance.
     */
    public void mergeMissingFrom(Project other) {
        if (other == null || other == this) {
            return;
        }
        if (StringUtils.isBlank(userName))        userName        = other.userName;
        if (StringUtils.isBlank(userPwd))         userPwd         = other.userPwd;
        if (StringUtils.isBlank(userKey))         userKey         = other.userKey;
        if (StringUtils.isBlank(keyPass))         keyPass         = other.keyPass;
        if (StringUtils.isBlank(accessToken))     accessToken     = other.accessToken;
        if (StringUtils.isBlank(userCommitName))  userCommitName  = other.userCommitName;
        if (StringUtils.isBlank(userCommitEmail)) userCommitEmail = other.userCommitEmail;
        if (StringUtils.isBlank(committerName))   committerName   = other.committerName;
        if (StringUtils.isBlank(committerEmail))  committerEmail  = other.committerEmail;
        if (StringUtils.isBlank(empId))           empId           = other.empId;
        indexed             |= other.indexed;
        showAllPullRequests |= other.showAllPullRequests;
        if (openTime == null || (other.openTime != null && other.openTime.after(openTime))) {
            openTime = other.openTime;
        }
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isShowAllPullRequests() {
        return showAllPullRequests;
    }

    public void setShowAllPullRequests(boolean showAllPullRequests) {
        this.showAllPullRequests = showAllPullRequests;
    }

    public String getUserCommitName() {
        return userCommitName;
    }

    public void setUserCommitName(String userCommitName) {
        this.userCommitName = userCommitName;
    }

    public String getUserCommitEmail() {
        return userCommitEmail;
    }

    public void setUserCommitEmail(String userCommitEmail) {
        this.userCommitEmail = userCommitEmail;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public String getProjectHomeFolder() {
        return projectHomeFolder;
    }

    /**
     * Prefer {@code Settings.getOrCreateProject(...)} to obtain a {@link Project} instance
     * in the first place. Mutating the home folder of an instance already interned into
     * {@code Settings.projects} / a {@code Workspace}'s {@code TreeSet} corrupts that set's
     * ordering invariant, since both are sorted by this value.
     */
    public void setProjectHomeFolder(String projectHomeFolder) {
        this.projectHomeFolder = normalizeHome(projectHomeFolder);
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public int compareTo(Project o) {
        return canonicalKey(projectHomeFolder).compareTo(canonicalKey(o == null ? null : o.projectHomeFolder));
    }

    @Override
    public int hashCode() {
        return canonicalKey(projectHomeFolder).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Project)) {
            return false;
        }
        return canonicalKey(projectHomeFolder).equals(canonicalKey(((Project) obj).projectHomeFolder));
    }

    @Override
    public String toString() {
        return "Git " + projectHomeFolder;
    }

    // ── repo-service lifecycle ──────────────────────────────────────────────────────────────

    /**
     * The cached {@link GitRepoService} for this project, opening it lazily on first use.
     * Callers must NOT close it -- it is owned and cached by this {@link Project}; release it
     * via {@link #closeRepoService()} instead.
     */
    public GitRepoService getGitRepoService() {
        GitRepoService s = gitRepoService;
        if (s == null) {
            synchronized (serviceLock) {
                if ((s = gitRepoService) == null) {
                    String gitDir = getGitDir();
                    try {
                        s = gitRepoService = new GitRepoService(gitDir, this);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Cannot open " + gitDir, e);
                    }
                }
            }
        }
        return s;
    }

    /** Eager open, mirroring the historic {@code Context.initRepoOnly}: throws at open time,
     *  not on first use. Shuts down any previously open service for this project first. */
    public void openRepoService() throws Exception {
        String gitDir = getGitDir();
        if (gitDir == null || !new File(gitDir).exists()) {
            throw new RuntimeException("Git folder " + gitDir + " not found");
        }
        synchronized (serviceLock) {
            if (gitRepoService != null) {
                gitRepoService.shutdown();
            }
            gitRepoService = new GitRepoService(gitDir, this);
        }
    }

    /** Idempotent: stops the watcher, interrupts the cache-warm thread, shuts down the
     *  {@link GitRepoService} and clears the commit-detail cache. */
    public void closeRepoService() {
        stopWatcher();
        Thread w = cacheWarmThread;
        if (w != null) {
            w.interrupt();
        }
        GitRepoService s;
        synchronized (serviceLock) {
            s = gitRepoService;
            gitRepoService = null;
        }
        if (s != null) {
            s.shutdown();
        }
        scmRevisionInformationCache.clear();
    }

    @JsonIgnore
    public boolean isRepoServiceOpen() {
        return gitRepoService != null;
    }

    public Map<String, ScmRevisionInformation> getScmRevisionInformationCache() {
        return scmRevisionInformationCache;
    }

    // ── init / refresh (moved from Context) ─────────────────────────────────────────────────

    /** Loads all repo-scoped state after {@link #openRepoService()} has succeeded. */
    public void initAfterOpen() throws Exception {
        scmRevisionInformationCache.clear();
        setStash(getGitRepoService().getStashList());
        setLfsRepo(getGitRepoService().isLfsRepo());

        updateBranches();
        updateTags();
        updateWorkingBranch();
        Context.refreshWorkingCopy();

        warmCommitCache();
        startWatcher();
        updatePullRequests();
        updateSubmodules();
    }

    private void warmCommitCache() {
        final GitRepoService svc = getGitRepoService();
        Thread t = new Thread(() -> {
            try {
                svc.getCommitsByTree(null, true, 100, null).forEach(c -> svc.adapt(c, null));
            } catch (RuntimeException ignored) {
                // repo closed / switched under us -- nothing to warm any more
            }
        });
        t.setDaemon(true);
        cacheWarmThread = t;
        t.start();
    }

    /** Starts (restarting if already running) the file watcher that keeps the working-copy
     *  status list fresh when files change outside the app. */
    /**
     * Starts (restarting if already running) the file watcher that keeps this project's
     * working-copy status list fresh when files change outside the app. The callback refreshes
     * THIS project specifically -- not whichever project happens to be active in {@link Context}
     * at the moment the file-system event arrives, which could be a different one if the user
     * has switched repositories since this watcher was started.
     */
    public void startWatcher() throws Exception {
        String gitDir = getGitDir();
        if (gitDir == null) {
            return;
        }
        String projFolder = gitDir
                .replace("/.git", "")
                .replace("\\.git", "");
        if (projectWatcherThread != null) {
            projectWatcherThread.interrupt();
        }
        projectWatcher = new ProjectWatcher(projFolder, (kind, fileName) -> {
            SwingUtilities.invokeLater(() -> this.updateStatus(null, true));
        });
        projectWatcherThread = new Thread(projectWatcher);
        projectWatcherThread.setDaemon(true);
        projectWatcherThread.start();
    }

    public void stopWatcher() {
        if (projectWatcherThread != null) {
            projectWatcherThread.interrupt();
            projectWatcherThread = null;
        }
        projectWatcher = null;
    }

    public void updateAll() {
        updateStatus(null);
        try {
            updateBranches();
            updateTags();
            updateStash();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot make updates  ", e);
        }
    }

    public void updateStatus(ProgressMonitor progressMonitor) {
        updateStatus(progressMonitor, false);
    }

    public void updateStatus(ProgressMonitor progressMonitor, boolean workingCopyOnly) {
        synchronized (statusLock) {
            if (getGitDir() != null) {
                List<ScmItem> statuses = getGitRepoService().getStatuses(progressMonitor);
                List<ScmItem> old = statusList;
                if (!GitemberUtil.areEqualIgnoreOrder(statuses,old)) {
                    statusList = new ArrayList<>(statuses);
                    Context.fire(this, Context.PROP_STATUS_LIST, old, statusList);
                }


                if (!workingCopyOnly) {
                    List<PlotCommit> oldPlot = plotCommitList;
                    plotCommitList = new ArrayList<>();
                    Context.fire(this, Context.PROP_PLOT_COMMIT_LIST, oldPlot, plotCommitList);
                }

            }
        }
    }

    public void updateWorkingBranch() {
        ScmBranch head = localBranches.stream().filter(ScmBranch::isHead).findFirst().orElse(null);
        if (head == null && getGitDir() != null) {
            // Unborn HEAD (brand-new repo, no commits yet): no refs/heads/* exist, so
            // localBranches is empty, but the symbolic HEAD still names the future branch.
            head = getGitRepoService().getCurrentScmBranch();
        }
        setWorkingBranch(head);
    }

    public void updateBranches() {
        synchronized (branchLock) {
            localBranchesRaw.clear();
            remoteBranchesRaw.clear();
            try {
                localBranchesRaw.addAll(getGitRepoService().getBranches());
                remoteBranchesRaw.addAll(getGitRepoService().getRemoteBranches());

                List<ScmBranch> oldLocal = localBranches;
                localBranches = new ArrayList<>(localBranchesRaw);
                Context.fire(this, Context.PROP_LOCAL_BRANCHES, oldLocal, localBranches);

                List<ScmBranch> oldRemote = remoteBranches;
                remoteBranches = new ArrayList<>(remoteBranchesRaw);
                Context.fire(this, Context.PROP_REMOTE_BRANCHES, oldRemote, remoteBranches);
            } catch (Exception e) {
                e.printStackTrace();
                log.log(Level.SEVERE, "Cannot update branch information");
            }
        }
    }

    public void updateTags() {
        synchronized (tagLock) {
            tagsRaw.clear();
            tagsRaw.addAll(getGitRepoService().getTags());

            List<ScmBranch> old = tags;
            tags = new ArrayList<>(tagsRaw);
            Context.fire(this, Context.PROP_TAGS, old, tags);
        }
    }

    public void updateStash() {
        setStash(getGitRepoService().getStashList());
    }

    private void setStash(List<ScmRevisionInformation> value) {
        List<ScmRevisionInformation> old = stash;
        stash = value;
        Context.fire(this, Context.PROP_STASH, old, value);
    }

    public void updatePlotCommitList(final String treeName,
                                      final boolean allHistory,
                                      final ProgressMonitor progressMonitor) {
        final PlotCommitList<PlotLane> plotCommits =
                getGitRepoService().getCommitsByTree(treeName, allHistory, -1, progressMonitor);

        List<PlotCommit> old = new ArrayList<>(plotCommitList);
        plotCommitList = new ArrayList<>(plotCommits);
        Context.fire(this, Context.PROP_PLOT_COMMIT_LIST, old, plotCommitList);
    }

    /** Refreshes the submodule list in a daemon thread; fires PROP_SUBMODULES on the EDT when done. */
    public void updateSubmodules() {
        final GitRepoService svc = getGitRepoService();
        Thread t = new Thread(() -> {
            try {
                List<Submodule> list = svc.getSubmodules();
                SwingUtilities.invokeLater(() -> setSubmodules(list));
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to load submodules", e);
                SwingUtilities.invokeLater(() -> setSubmodules(Collections.emptyList()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Fetches open PRs in a daemon thread; fires PROP_PULL_REQUESTS on the EDT when done. */
    public void updatePullRequests() {
        final GitRepoService svc = getGitRepoService();
        Thread t = new Thread(() -> {
            try {
                String remoteUrl = svc.getRepositoryRemoteUrl();
                List<PullRequest> prs = PullRequestService.fetch(remoteUrl, getAccessToken(), isShowAllPullRequests());
                SwingUtilities.invokeLater(() -> setPullRequests(prs));
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to update pull requests", e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── accessors for the moved runtime state ───────────────────────────────────────────────

    public ScmBranch getWorkingBranch() {
        return workingBranch;
    }

    public void setWorkingBranch(ScmBranch value) {
        ScmBranch old = workingBranch;
        workingBranch = value;
        Context.fire(this, Context.PROP_WORKING_BRANCH, old, value);
    }

    public boolean isLfsRepo() {
        return lfsRepo;
    }

    public void setLfsRepo(boolean value) {
        boolean old = lfsRepo;
        lfsRepo = value;
        Context.fire(this, Context.PROP_LFS_REPO, old, value);
    }

    public List<ScmBranch> getRemoteBranches() {
        return remoteBranches;
    }

    public List<ScmBranch> getLocalBranches() {
        return localBranches;
    }

    public List<ScmBranch> getTags() {
        return tags;
    }

    public List<ScmRevisionInformation> getStash() {
        return stash;
    }

    public List<ScmItem> getStatusList() {
        return statusList;
    }

    public List<PlotCommit> getPlotCommitList() {
        return plotCommitList;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    private void setPullRequests(List<PullRequest> value) {
        List<PullRequest> old = pullRequests;
        pullRequests = value;
        Context.fire(this, Context.PROP_PULL_REQUESTS, old, value);
    }

    public List<Submodule> getSubmodules() {
        return submodules;
    }

    private void setSubmodules(List<Submodule> value) {
        List<Submodule> old = submodules;
        submodules = value;
        Context.fire(this, Context.PROP_SUBMODULES, old, value);
    }
}
