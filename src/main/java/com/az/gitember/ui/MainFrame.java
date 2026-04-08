package com.az.gitember.ui;

import com.az.gitember.data.*;
import com.az.gitember.dialog.CloneDialog;
import com.az.gitember.dialog.CommitDialog;
import com.az.gitember.dialog.CredentialsDialog;
import com.az.gitember.dialog.InitDialog;
import com.az.gitember.dialog.InteractiveContinueAbortDialog;
import com.az.gitember.dialog.LfsManageDialog;
import com.az.gitember.dialog.SettingsDialog;
import com.az.gitember.dialog.StatDialog;
import com.az.gitember.handler.*;
import com.az.gitember.handler.LfsFetchHandler;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainTreeCellRenderer.TreeNodeData;
import com.az.gitember.ui.misc.MainToolBar;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame {

    private static final Logger log = Logger.getLogger(MainFrame.class.getName());

    private final MainMenuBar menuBar;
    private final MainToolBar toolBar;
    private final MainTreePanel treePanel;
    private final ContentPanel contentPanel;
    private final StatusBar statusBar;
    private final WelcomePanel welcomePanel;

    // Main content area - switches between welcome and repo views
    private final CardLayout mainCardLayout;
    private final JPanel mainCardPanel;
    private final JSplitPane splitPane;

    private static final String CARD_WELCOME = "welcome";
    private static final String CARD_REPO = "repo";

    // Working copy
    private WorkingCopyPanel workingCopyPanel;
    private HistoryPanel historyPanel;
    private CommitDetailPanel stashDetailPanel;
    private PullRequestPanel pullRequestPanel;
    private SubmodulePanel submodulePanel;

    public MainFrame() {
        Context.setMainFrame(this);
        setTitle("Gitember");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set window icons — provide multiple sizes so each OS picks the best fit.
        // Windows uses 16/32/48/256; Linux uses 16/32/48/128; macOS uses the largest.
        setIconImages(loadIcons(
                "/icon/gitember-16.png",
                "/icon/gitember-20.png",
                "/icon/gitember-24.png",
                "/icon/gitember-32.png",
                "/icon/gitember-34.png",
                "/icon/gitember-36.png",
                "/icon/gitember-40.png",
                "/icon/gitember-48.png",
                "/icon/gitember-60.png",
                "/icon/gitember-64.png",
                "/icon/gitember-72.png",
                "/icon/gitember-80.png",
                "/icon/gitember-96.png",
                "/icon/gitember-256.png",
                "/icon/gitember-512.png"));

        // macOS Dock icon: use the largest available image (Java 9+ Taskbar API)
        if (java.awt.Taskbar.isTaskbarSupported()) {
            try {
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                    java.net.URL dockIconUrl = getClass().getResource("/icon/gitember-512.png");
                    if (dockIconUrl != null) {
                        taskbar.setIconImage(new ImageIcon(dockIconUrl).getImage());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Create components
        menuBar = new MainMenuBar();
        toolBar = new MainToolBar();
        treePanel = new MainTreePanel();
        contentPanel = new ContentPanel();
        statusBar = new StatusBar();

        // Welcome panel
        welcomePanel = new WelcomePanel();
        welcomePanel.setOnProjectSelected(this::openProject);
        welcomePanel.setOnProjectRemoved(project -> {
            Settings settings = Context.getSettings();
            if (settings != null) {
                settings.getProjects().remove(project);
                Context.saveSettings();
                refreshProjectLists();
            }
        });

        // Layout
        setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, contentPanel);
        splitPane.setDividerLocation(250);

        // Card layout to switch between welcome and repo views
        mainCardLayout = new CardLayout();
        mainCardPanel = new JPanel(mainCardLayout);
        mainCardPanel.add(welcomePanel, CARD_WELCOME);
        mainCardPanel.add(splitPane, CARD_REPO);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(mainCardPanel, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Wire up actions
        wireActions();

        // Listen to context changes
        Context.addPropertyChangeListener(Context.PROP_WORKING_BRANCH, this::onWorkingBranchChanged);
        Context.addPropertyChangeListener(Context.PROP_REPOSITORY_PATH, this::onRepoPathChanged);
        Context.addPropertyChangeListener(Context.PROP_STATUS_LIST, this::onStatusListChanged);
        Context.addPropertyChangeListener(Context.PROP_WORKING_COPY_REFRESH, e ->
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() {
                        Context.updateStatus(null, true);
                        return null;
                    }
                }.execute());

        // Tree selection listener
        treePanel.getTree().addTreeSelectionListener(this::onTreeSelection);

        // Reload working copy when the window regains focus (like an IDE)
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (Context.getRepositoryPath() != null) {
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() {
                            Context.updateStatus(null, true);
                            return null;
                        }
                    }.execute();
                }
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) {}
        });

        workingCopyPanel = new WorkingCopyPanel(statusBar);
        historyPanel = new HistoryPanel(statusBar);
        stashDetailPanel = new CommitDetailPanel();
        pullRequestPanel = new PullRequestPanel();
        submodulePanel = new SubmodulePanel(statusBar);

        Context.addPropertyChangeListener(Context.PROP_SUBMODULES,
                e -> submodulePanel.setSubmodules(Context.getSubmodules()));

        // Set up branch context menus
        BranchContextMenuFactory contextMenuFactory = new BranchContextMenuFactory(this, statusBar);
        treePanel.setContextMenuFactory(contextMenuFactory);

        // Start with welcome screen
        refreshProjectLists();
        setRepoActionsEnabled(false);
        toolBar.setVisible(false);
        mainCardLayout.show(mainCardPanel, CARD_WELCOME);
    }

    private void wireActions() {
        // Open
        menuBar.addOpenListener(e -> new OpenRepoHandler(this, statusBar).execute());
        toolBar.addOpenListener(e -> new OpenRepoHandler(this, statusBar).execute());

        // Clone
        menuBar.addCloneListener(e -> showCloneDialog());
        toolBar.addCloneListener(e -> showCloneDialog());

        // Init
        menuBar.addInitListener(e -> showInitDialog());

        // Welcome panel buttons
        welcomePanel.setOnOpenRepo(() -> new OpenRepoHandler(this, statusBar).execute());
        welcomePanel.setOnCloneRepo(this::showCloneDialog);
        welcomePanel.setOnInitRepo(this::showInitDialog);

        // Pull
        menuBar.addPullListener(e -> new PullHandler(this, statusBar).execute());
        toolBar.addPullListener(e -> new PullHandler(this, statusBar).execute());

        // Push
        menuBar.addPushListener(e -> new PushHandler(this, statusBar).execute());
        toolBar.addPushListener(e -> new PushHandler(this, statusBar).execute());

        // Fetch
        menuBar.addFetchListener(e -> new FetchHandler(this, statusBar).execute());
        toolBar.addFetchListener(e -> new FetchHandler(this, statusBar).execute());

        // Commit
        menuBar.addCommitListener(e -> showCommitDialog());
        toolBar.addCommitListener(e -> showCommitDialog());

        // Interactive Rebase (from menu: prompts for a base commit SHA)
        menuBar.addInteractiveRebaseListener(e -> {
            String sha = JOptionPane.showInputDialog(this,
                    "Enter the SHA of the base commit\n"
                    + "(all commits from HEAD down to, but not including, this commit will be rebased):",
                    "Interactive Rebase", JOptionPane.PLAIN_MESSAGE);
            if (sha != null && !sha.isBlank()) {
                String trimmed = sha.trim();
                com.az.gitember.handler.InteractiveRebaseHandler.showAndExecute(
                        this, statusBar, trimmed,
                        trimmed.substring(0, Math.min(7, trimmed.length())),
                        () -> historyPanel.loadHistory(null, true));
            }
        });

        // Working copy menu
        menuBar.addRefreshListener(e -> refreshWorkingCopy());
        menuBar.addStashListener(e -> showStashDialog());
        menuBar.addWorktreesListener(e -> showWorktreesDialog());
        menuBar.addCreateDiffListener(e -> createDiff());
        menuBar.addApplyDiffListener(e -> applyDiff());

        // Tools
        menuBar.addCompareFilesListener(e ->
                new DiffViewerWindow().setVisible(true));
        menuBar.addCompareFoldersListener(e ->
                new FolderCompareWindow().setVisible(true));
        menuBar.addIndexHistoryListener(e -> {
            com.az.gitember.dialog.IndexHistoryDialog dlg =
                    new com.az.gitember.dialog.IndexHistoryDialog(this);
            dlg.setOnComplete(historyPanel::refreshLuceneState);
            dlg.setVisible(true);
        });
        menuBar.addStatisticsListener(e -> new StatDialog(this, statusBar).setVisible(true));
        menuBar.addOpenTerminalListener(e -> openTerminalInRepo());
        menuBar.addManageLfsListener(e -> new LfsManageDialog(this).setVisible(true));
        menuBar.addFetchLfsListener(e -> new LfsFetchHandler(this, statusBar).execute());
        menuBar.addCompressDatabaseListener(e -> new com.az.gitember.handler.CompressDatabaseHandler(this, statusBar).execute());
        menuBar.addUpdateSubmodulesListener(e -> new com.az.gitember.handler.UpdateSubmodulesHandler(this, statusBar).execute());
        menuBar.addSyncSubmodulesListener(e -> submodulePanel.syncSubmoduleUrls());

        // Credentials
        menuBar.addCredentialsListener(e -> showCredentialsDialog());

        // Project settings (author / committer identity)
        menuBar.addProjectSettingsListener(e ->
                new com.az.gitember.dialog.ProjectSettingsDialog(this).setVisible(true));

        // Settings
        menuBar.addSettingsListener(e -> new SettingsDialog(this).setVisible(true));

        // Recent project handlers
        menuBar.setRecentProjectHandler(this::openProject);
        toolBar.setProjectSelectionHandler(this::openProject);
    }

    private void openProject(Project project) {
        String folder = project.getProjectHomeFolder();
        statusBar.setStatus("Opening " + folder + "...");
        statusBar.showProgress(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.init(folder);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    addCurrentProjectToSettings();
                    refreshProjectLists();
                    statusBar.clearProgress();
                    statusBar.setStatus("Repository opened");
                    InteractiveContinueAbortDialog.showIfRebaseInProgress(
                            MainFrame.this, statusBar,
                            () -> historyPanel.loadHistory(null, true));
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    log.log(Level.WARNING, "Failed to open project", cause);
                    statusBar.clearProgress();
                    statusBar.setStatus("Failed to open: " + cause.getMessage());

                    // Remove invalid project from list
                    Settings settings = Context.getSettings();
                    if (settings != null) {
                        settings.getProjects().remove(project);
                        Context.saveSettings();
                        refreshProjectLists();
                    }

                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Cannot open repository: " + folder
                                    + "\nIt will be removed from the list of recent projects.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void addCurrentProjectToSettings() {
        Settings settings = Context.getSettings();
        if (settings == null) return;

        String repoPath = Context.getRepositoryPath();
        if (repoPath == null) return;

        String folder = repoPath.replace(File.separator + ".git", "");

        // Preserve the existing project entry (and its credentials) — only update the timestamp.
        Project existing = settings.getProjects().stream()
                .filter(p -> p.getProjectHomeFolder().equalsIgnoreCase(folder))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setOpenTime(new Date());
        } else {
            settings.getProjects().add(new Project(folder, new Date()));
        }
        Context.saveSettings();
    }

    private void refreshProjectLists() {
        Settings settings = Context.getSettings();
        if (settings == null) return;

        menuBar.refreshRecentProjects(settings.getProjects());

        Project current = Context.getCurrentProject().orElse(null);
        toolBar.refreshProjects(settings.getProjects(), current);

        // Update welcome panel
        welcomePanel.setProjects(settings.getProjects());
    }

    private void showCloneDialog() {
        CloneDialog dialog = new CloneDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new CloneHandler(this, statusBar, dialog.getParameters()).execute();
        }
    }

    private void showCredentialsDialog() {
        Context.getCurrentProject().ifPresentOrElse(project -> {
            CredentialsDialog dialog = new CredentialsDialog(this, project);
            dialog.setVisible(true);
            dialog.setAccessToken(project.getAccessToken());
            dialog.setUserName(project.getUserName());
            dialog.setPassword(project.getUserPwd());
            if (dialog.isConfirmed()) {
                project.setAccessToken(dialog.getAccessToken());
                project.setUserName(dialog.getUserName());
                project.setUserPwd(dialog.getPassword());
                Context.saveSettings();
                statusBar.setStatus("Credentials saved");
            }
        }, () -> statusBar.setStatus("No repository open"));
    }

    private void showInitDialog() {
        InitDialog dialog = new InitDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new InitHandler(this, statusBar, dialog.getParameters()).execute();
        }
    }

    private void showCommitDialog() {
        CommitDialog dialog = new CommitDialog(this);
        dialog.setVisible(true);
    }

    private void refreshWorkingCopy() {
        statusBar.setStatus("Refreshing...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Context.updateAll();
                Context.updateWorkingBranch();
                return null;
            }

            @Override
            protected void done() {
                statusBar.setStatus("Refreshed");
            }
        };
        worker.execute();
    }

    private void loadStashDetail(ScmRevisionInformation stash) {
        // adapt() is called for every stash entry when the stash list is built,
        // so affected items are already populated at this point.
        stashDetailPanel.showRevision(stash);
        statusBar.setStatus("Stash: " + (stash.getShortMessage() != null ? stash.getShortMessage() : ""));
    }

    private void showStashDialog() {
        String message = JOptionPane.showInputDialog(this, "Stash message:", "Stash", JOptionPane.PLAIN_MESSAGE);
        if (message == null) return; // cancelled

        statusBar.setStatus("Stashing...");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().stash(message);
                Context.updateStash();
                Context.updateStatus(null, true);
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Stash created");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Stash failed", ex);
                    statusBar.setStatus("Stash failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Cannot stash: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showWorktreesDialog() {
        com.az.gitember.dialog.WorktreesDialog dlg =
                new com.az.gitember.dialog.WorktreesDialog(this);
        dlg.setVisible(true);

        // Refresh tree so add/remove is reflected immediately
        treePanel.refreshWorktrees();

        com.az.gitember.data.WorktreeInfo toOpen = dlg.getSelectedToOpen();
        if (toOpen != null) {
            openWorktree(toOpen.getPath());
        }
    }

    private void openWorktree(String path) {
        statusBar.setStatus("Opening worktree: " + path + "…");
        statusBar.showProgress(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                Context.init(path);
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    addCurrentProjectToSettings();
                    refreshProjectLists();
                    statusBar.setStatus("Worktree opened: " + path);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, "Failed to open worktree", cause);
                    statusBar.setStatus("Failed to open worktree: " + cause.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Cannot open worktree:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void createDiff() {
        statusBar.setStatus("Creating diff...");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return Context.getGitRepoService().createDiff();
            }

            @Override
            protected void done() {
                try {
                    String diff = get();
                    if (diff == null || diff.isBlank()) {
                        statusBar.setStatus("No differences found");
                        return;
                    }
                    statusBar.setStatus("Diff created");
                    FileViewerWindow viewer = new FileViewerWindow("Working Copy Diff", diff, "diff.patch");
                    viewer.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Create diff failed", ex);
                    statusBar.setStatus("Create diff failed");
                }
            }
        };
        worker.execute();
    }

    private void applyDiff() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select patch file to apply");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Patch / Diff files (*.patch, *.diff)", "patch", "diff"));
        chooser.setAcceptAllFileFilterUsed(true);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File patchFile = chooser.getSelectedFile();
        statusBar.setStatus("Applying patch: " + patchFile.getName() + "...");
        statusBar.showProgress(true);

        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                try (FileInputStream fis = new FileInputStream(patchFile)) {
                    return Context.getGitRepoService().applyDiff(fis);
                }
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    List<String> applied = get();
                    Context.updateStatus(null, true);
                    statusBar.setStatus("Patch applied: " + applied.size() + " file(s) modified");

                    // Build result message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Patch applied successfully.\n\n");
                    if (applied.isEmpty()) {
                        sb.append("No files were modified.");
                    } else {
                        sb.append("Modified files (").append(applied.size()).append("):\n");
                        for (String path : applied) {
                            sb.append("  ").append(path).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(MainFrame.this,
                            sb.toString(), "Apply Patch", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, "Apply diff failed", cause);
                    statusBar.setStatus("Apply diff failed: " + cause.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Cannot apply patch:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void setRepoActionsEnabled(boolean enabled) {
        menuBar.setRepoActionsEnabled(enabled);
        toolBar.setRepoActionsEnabled(enabled);
    }

    // Event handlers
    private void onWorkingBranchChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            ScmBranch branch = (ScmBranch) evt.getNewValue();
            String name = branch != null ? branch.getShortName() : "";
            toolBar.setBranchName(name);
            toolBar.updateSyncCounts(branch);
            updateTitle();
        });
    }

    private void onRepoPathChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            boolean hasRepo = evt.getNewValue() != null;
            setRepoActionsEnabled(hasRepo);
            toolBar.setVisible(hasRepo);
            if (hasRepo) {
                addCurrentProjectToSettings();
                refreshProjectLists();
                // Switch from welcome to repo view
                mainCardLayout.show(mainCardPanel, CARD_REPO);
            } else {
                // No repo - show welcome
                mainCardLayout.show(mainCardPanel, CARD_WELCOME);
            }
            updateTitle();
        });
    }

    private void onStatusListChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            workingCopyPanel.setItems(Context.getStatusList());
            toolBar.setCommitEnabled(workingCopyPanel.hasStagedItems());
        });
    }

    private void updateTitle() {
        String path = Context.getRepositoryPath();
        ScmBranch branch = Context.getWorkingBranch();
        if (path != null) {
            String folder = Context.getProjectFolder();
            String branchName = branch != null ? " [" + branch.getShortName() + "]" : "";
            setTitle("Gitember - " + folder + branchName);
        } else {
            setTitle("Gitember");
        }
    }

    private void onTreeSelection(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
        if (node == null) return;

        Object userObject = node.getUserObject();
        if (userObject instanceof TreeNodeData data) {
            boolean isWorkingCopy = data.type() == MainTreeCellRenderer.NodeType.WORKING_COPY;
            boolean isAllHistory  = data.type() == MainTreeCellRenderer.NodeType.HISTORY;
            boolean isPullRequest = data.type() == MainTreeCellRenderer.NodeType.PULL_REQUEST;

            // Merge/unmerge working copy toolbar
            if (isWorkingCopy) {
                toolBar.mergeWorkingCopyToolbar(workingCopyPanel);
            } else {
                toolBar.unmergeWorkingCopyToolbar();
            }

            // Merge/unmerge history search toolbar
            if (isAllHistory) {
                toolBar.mergeHistoryToolbar(historyPanel);
            } else {
                toolBar.unmergeHistoryToolbar();
            }

            // Merge/unmerge pull-request file filter
            if (isPullRequest) {
                toolBar.mergePullRequestToolbar(pullRequestPanel);
            } else {
                toolBar.unmergePullRequestToolbar();
            }

            switch (data.type()) {
                case WORKING_COPY -> {
                    Context.setActiveView(Context.ActiveView.WORKING_COPY);
                    contentPanel.setContent(workingCopyPanel);
                    workingCopyPanel.setItems(Context.getStatusList()); // show cached immediately
                    toolBar.setCommitEnabled(workingCopyPanel.hasStagedItems());
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() {
                            Context.updateStatus(null, true);
                            return null;
                        }
                    }.execute();
                }
                case HISTORY -> {
                    Context.setActiveView(Context.ActiveView.HISTORY);
                    contentPanel.setContent(historyPanel);
                    historyPanel.loadHistory(null, true);
                }
                case BRANCH -> {
                    Context.setActiveView(Context.ActiveView.HISTORY);
                    if (data.data() instanceof ScmBranch branch) {
                        contentPanel.setContent(historyPanel);
                        historyPanel.loadHistory(branch.getFullName(), false);
                    }
                }
                case TAG -> {
                    Context.setActiveView(Context.ActiveView.HISTORY);
                    if (data.data() instanceof ScmBranch tag) {
                        contentPanel.setContent(historyPanel);
                        historyPanel.loadHistory(tag.getFullName(), false);
                    }
                }
                case STASH -> {
                    if (data.data() instanceof ScmRevisionInformation stash) {
                        contentPanel.setContent(stashDetailPanel);
                        loadStashDetail(stash);
                    }
                }
                case PULL_REQUEST -> {
                    if (data.data() instanceof PullRequest pr) {
                        contentPanel.setContent(pullRequestPanel);
                        pullRequestPanel.showPullRequest(pr);
                    }
                }
                case SUBMODULES, SUBMODULE -> {
                    contentPanel.setContent(submodulePanel);
                    submodulePanel.setSubmodules(Context.getSubmodules());
                }
                case WORKTREE -> {
                    if (data.data() instanceof com.az.gitember.data.WorktreeInfo wt) {
                        openWorktree(wt.getPath());
                    }
                }
                default -> contentPanel.setContent(null);
            }
        }
    }

    /** Loads icon images from classpath resources, silently skipping any that are missing. */
    private java.util.List<Image> loadIcons(String... paths) {
        java.util.List<Image> icons = new java.util.ArrayList<>();
        for (String path : paths) {
            try {
                java.net.URL url = getClass().getResource(path);
                if (url != null) icons.add(new ImageIcon(url).getImage());
            } catch (Exception ignored) {
            }
        }
        // Fallback to the original single icon if none were found
        if (icons.isEmpty()) {
            try {
                java.net.URL url = getClass().getResource("/icon/gitember.png");
                if (url != null) icons.add(new ImageIcon(url).getImage());
            } catch (Exception ignored) {
            }
        }
        return icons;
    }

    /**
     * Switches the main content area to the history view and selects the commit
     * whose full SHA starts with {@code sha} (7-char short SHA is sufficient).
     * History is (re-)loaded from scratch so that the commit is always found.
     */
    public void showCommitInHistory(String sha) {
        contentPanel.setContent(historyPanel);
        toolBar.mergeHistoryToolbar(historyPanel);
        historyPanel.loadHistoryAndSelect(sha);
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public MainTreePanel getTreePanel() {
        return treePanel;
    }

    // ── Terminal launcher ─────────────────────────────────────────────────────

    private void openTerminalInRepo() {
        String path = Context.getProjectFolder();
        if (path == null || path.isEmpty()) {
            statusBar.setStatus("No repository open.");
            return;
        }
        File dir = new File(path);
        try {
            if (Context.isWindows()) {
                openTerminalWindows(dir);
            } else if (Context.isMac()) {
                // open -a Terminal <path>  works for Terminal.app
                new ProcessBuilder("open", "-a", "Terminal", dir.getAbsolutePath()).start();
            } else {
                openTerminalLinux(dir);
            }
            statusBar.setStatus("Terminal opened in " + path);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot open terminal", ex);
            statusBar.setStatus("Cannot open terminal: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Could not open a terminal:\n" + ex.getMessage(),
                    "Open Terminal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTerminalWindows(File dir) throws java.io.IOException {
        // Try Windows Terminal (wt.exe) first, fall back to cmd.exe
        try {
            new ProcessBuilder("wt", "-d", dir.getAbsolutePath()).start();
        } catch (java.io.IOException ignored) {
            new ProcessBuilder("cmd", "/c", "start", "cmd.exe")
                    .directory(dir).start();
        }
    }

    private void openTerminalLinux(File dir) throws java.io.IOException {
        // Try common terminal emulators in preference order
        String[][] candidates = {
            { "x-terminal-emulator" },
            { "gnome-terminal", "--working-directory=" + dir.getAbsolutePath() },
            { "xfce4-terminal", "--working-directory=" + dir.getAbsolutePath() },
            { "konsole", "--workdir", dir.getAbsolutePath() },
            { "xterm" }
        };
        for (String[] cmd : candidates) {
            try {
                new ProcessBuilder(cmd).directory(dir).start();
                return;
            } catch (java.io.IOException ignored) {
            }
        }
        throw new java.io.IOException(
                "No terminal emulator found. Install x-terminal-emulator, gnome-terminal, konsole, or xterm.");
    }
}
