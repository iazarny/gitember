package com.az.gitember.ui;

import com.az.gitember.data.*;
import com.az.gitember.dialog.*;
import com.az.gitember.handler.*;
import com.az.gitember.handler.CreateBranchHandler;
import com.az.gitember.handler.LfsFetchHandler;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.mainframe.*;
import com.az.gitember.ui.maintree.CellRenderer;
import com.az.gitember.ui.maintree.MainTreePanel;
import com.az.gitember.ui.workspace.WorkspaceDashboardPanel;
import org.eclipse.jgit.lib.RepositoryState;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame {

    private static final Logger log = Logger.getLogger(MainFrame.class.getName());
    private static MainFrame instance;

    private  MainMenuBar menuBar;
    private  MainToolBar toolBar;
    private  MainTreePanel treePanel;
    private  ContentPanel contentPanel;
    private  StatusBar statusBar;
    private  WelcomePanel welcomePanel;

    // Main content area - switches between welcome and repo views
    private  CardLayout mainCardLayout;
    private  JPanel mainCardPanel;
    private  JSplitPane splitPane;

    public static final String CARD_WELCOME = "welcome";
    public static final String CARD_REPO = "repo";


    private  WorkingCopyPanel workingCopyPanel; // Working copy
    private  HistoryPanel historyPanel;
    private  CommitDetailPanel stashDetailPanel;
    private  PullRequestPanel pullRequestPanel;
    private  SubmodulePanel submodulePanel;
    private  WorkspaceDashboardPanel workspaceDashboardPanel; // Workspace dashboard (shown when the workspace root node is selected)

    private  ActiveView activeView = ActiveView.HISTORY;

    public static synchronized MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    private MainFrame() {
        setTitle("Gitember");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize(screenSize.width - 40, screenSize.height - 60);
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


    }

    public void init() {
        // Create components
        menuBar = new MainMenuBar();
        toolBar = new MainToolBar();
        toolBar.setVisible(false);
        treePanel = new MainTreePanel();
        contentPanel = new ContentPanel();
        statusBar = new StatusBar();

        // Welcome panel
        welcomePanel = new WelcomePanel();
        welcomePanel.setOnProjectSelected(new OpenRecentProjectHandler(this));
        welcomePanel.setOnProjectRemoved(project -> {
            Settings settings = Context.getSettings();
            if (settings != null) {
                settings.removeProject(project);
                Context.saveSettings();
                refreshProjectLists();
            }
        });
        welcomePanel.setOnWorkspaceSelected(new OpenRecentWorkspaceHanlder(this));
        welcomePanel.setOnWorkspaceRemoved(workspace -> {
            Settings settings = Context.getSettings();
            if (settings != null) {
                settings.getWorkspaces().remove(workspace);
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
        Context.addPropertyChangeListener(Context.PROP_WORKING_BRANCH, new WorkingBranchChangedChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_REPOSITORY_PATH, new RepoPathChangedChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_STATUS_LIST, new WorkingItemsChangedChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_WORKING_COPY_REFRESH, new WorkingItemsRefreshChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_NAVIGATE_TO_HISTORY, new NavigateToHistoryChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_NAVIGATE_TO_WORKING_COPY, new NavigateToWorkingCopyChangeListener(this));
        Context.addPropertyChangeListener(Context.PROP_SUBMODULES, new SubmodulesChangedChangeListener(this));

        // Tree selection listener
        treePanel.getTree().addTreeSelectionListener(this::onTreeSelection);

        // Reload working copy when the window regains focus (like an IDE)
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (Context.isWorkspaceMode()) {

                    if (workspaceDashboardPanel.isShowing()) { //Context.getActiveView() == Context.ActiveView.WORKSPACE
                        workspaceDashboardPanel.reloadActiveTab();
                    } else {
                        new SwingWorker<Void, Void>() {
                            @Override protected Void doInBackground() {
                                Context.updateStatus(null, true);
                                return null;
                            }
                        }.execute();
                    }
                } else {
                    System.out.println(">>>>>>>> 1");
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
        stashDetailPanel = new CommitDetailPanel(statusBar);
        pullRequestPanel = new PullRequestPanel();
        submodulePanel = new SubmodulePanel(statusBar);
        workspaceDashboardPanel = new WorkspaceDashboardPanel(statusBar);
        // Keep the Commit button in sync with dashboard stage/unstage actions in workspace mode:
        // committing is possible whenever any project in the workspace has a staged item.
        workspaceDashboardPanel.setOnCommitStateChanged(hasStaged -> {
            toolBar.setCommitEnabled(hasStaged);
            menuBar.setCommitEnabled(hasStaged);
        });



        // Set up branch context menus
        BranchContextMenuFactory contextMenuFactory = new BranchContextMenuFactory(this);
        contextMenuFactory.setWorktreeOpenAction(this::openWorktree);
        contextMenuFactory.setWorktreeRefreshAction(treePanel::refreshWorktrees);
        treePanel.setContextMenuFactory(contextMenuFactory);

        // Start with welcome screen
        refreshProjectLists();
        //setRepoActionsEnabled(false);
        //toolBar.setVisible(false);
        mainCardLayout.show(mainCardPanel, CARD_WELCOME);
    }



    private void wireActions() {
        // Open
        menuBar.addOpenListener(e -> new OpenRepoHandler(this).execute());
        toolBar.addOpenListener(e -> new OpenRepoHandler(this).execute());

        // Clone
        menuBar.addCloneListener(e -> showCloneDialog());
        toolBar.addCloneListener(e -> showCloneDialog());

        // Init
        menuBar.addInitListener(e -> showInitDialog());
        menuBar.addInitWorkspaceListener(e -> showWorkspaceDialog());

        // Welcome panel buttons
        welcomePanel.setOnOpenRepo(() -> new OpenRepoHandler(this).execute());
        welcomePanel.setOnCloneRepo(this::showCloneDialog);
        welcomePanel.setOnInitRepo(this::showInitDialog);
        welcomePanel.setOnInitWorkspace(this::showWorkspaceDialog);

        // Pull
        menuBar.addPullListener(e -> new PullHandler(this, null).execute());
        toolBar.addPullListener(e -> new PullHandler(this, null).execute());

        // Push
        menuBar.addPushListener(e -> new PushHandler(this, null).execute());
        toolBar.addPushListener(e -> new PushHandler(this, null).execute());

        // Fetch
        menuBar.addFetchListener(e -> new FetchHandler(this).execute());
        toolBar.addFetchListener(e -> new FetchHandler(this).execute());

        // Commit
        menuBar.addCommitListener(e -> showCommitDialog());
        toolBar.addCommitListener(e -> showCommitDialog());


        // Create branch
        menuBar.addCreateBranchListener(e -> CreateBranchHandler.showAndExecuteFromCurrent(this));
        toolBar.addBranchListener(e -> CreateBranchHandler.showAndExecuteFromCurrent(this));

        // Interactive Rebase (from menu: prompts for a base commit SHA)
        menuBar.addInteractiveRebaseListener(e -> {
            String sha = JOptionPane.showInputDialog(this,
                    "Enter the SHA of the base commit\n"
                    + "(all commits from HEAD down to, but not including, this commit will be rebased):",
                    "Interactive Rebase", JOptionPane.PLAIN_MESSAGE);
            if (sha != null && !sha.isBlank()) {
                String trimmed = sha.trim();
                com.az.gitember.handler.InteractiveRebaseHandler.showAndExecute(
                        this, trimmed,
                        trimmed.substring(0, Math.min(7, trimmed.length())),
                        () -> historyPanel.loadHistory(null, true));
            }
        });

        //Workspace menu

        menuBar.addWorskpacePullListener(e -> new PullHandler(this, null).execute());
        menuBar.addWorskpacePushListener(e -> new PushHandler(this, null).execute());
        menuBar.addWorskpaceFetchListener(e -> new FetchHandler(this).execute());
        menuBar.addWorskpaceCommitListener(e -> showCommitDialog());
        menuBar.addWorskpaceCreateBranchListener(e -> CreateBranchHandler.showAndExecuteFromCurrent(this));

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
        menuBar.addStatisticsListener(e -> new StatDialog(this).setVisible(true));
        menuBar.addOpenTerminalListener(e -> openTerminalInRepo());
        menuBar.addOpenExplorerListener(e -> openExplorerInRepo());
        menuBar.addManageLfsListener(e -> new LfsManageDialog(this).setVisible(true));
        menuBar.addFetchLfsListener(e -> new LfsFetchHandler(this).execute());
        menuBar.addCompressDatabaseListener(e -> new com.az.gitember.handler.CompressDatabaseHandler(this).execute());
        menuBar.addUpdateSubmodulesListener(e -> new com.az.gitember.handler.UpdateSubmodulesHandler(this).execute());
        menuBar.addSyncSubmodulesListener(e -> submodulePanel.syncSubmoduleUrls());

        // Project settings (author / committer identity + credentials)
        menuBar.addProjectSettingsListener(e ->
                new com.az.gitember.dialog.ProjectSettingsDialog(this).setVisible(true));

        // Settings
        menuBar.addSettingsListener(e -> new SettingsDialog(this).setVisible(true));

        // Recent project handlers
        menuBar.setRecentProjectHandler(new OpenRecentProjectHandler(this));
        menuBar.setRecentWorkspaceHandler(new OpenRecentWorkspaceHanlder(this));
        toolBar.setProjectSelectionHandler(new OpenRecentProjectHandler(this));
    }

    public void setActiveView(ActiveView view) {
        activeView = view;
    }
    public ActiveView getActiveView()          {
        return activeView;
    }

    /**
     * True when a workspace is open <em>and</em> the aggregated workspace view is the active one
     * (as opposed to having drilled into a single repository of the workspace). Remote operations
     * (push / pull / fetch) act on the whole workspace only in this state.
     */
    public  boolean isWorkspaceActive() {
        return Context.isWorkspaceMode() && getActiveView() == ActiveView.WORKSPACE;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public MainTreePanel getTreePanel() {
        return treePanel;
    }

    public MainMenuBar getMainMenuBar() {
        return menuBar;
    }

    public CardLayout getMainCardLayout() {
        return mainCardLayout;
    }

    public JPanel getMainCardPanel() {
        return mainCardPanel;
    }

    public MainToolBar getToolBar() {
        return toolBar;
    }

    public WorkingCopyPanel getWorkingCopyPanel() {
        return workingCopyPanel;
    }

    public SubmodulePanel getSubmodulePanel() {
        return submodulePanel;
    }

    public HistoryPanel getHistoryPanel() {
        return historyPanel;
    }

    public WorkspaceDashboardPanel getWorkspaceDashboardPanel() {
        return workspaceDashboardPanel;
    }

    public ContentPanel getContentPanel() {
        return contentPanel;
    }

    public void addCurrentProjectToSettings() {
        Settings settings = Context.getSettings();
        Project project = Context.getActiveProject();
        if (settings == null || project == null) return;

        // Projects are interned (Settings#internAll), so this is the same instance already
        // referenced by any workspace it belongs to -- no more separate "similar" copies to sync.
        settings.addRecentProject(project);
        Context.saveSettings();
    }

    public void refreshProjectLists() {
        Settings settings = Context.getSettings();
        if (settings == null) return;

        menuBar.refreshRecent(settings.getProjects(), settings.getWorkspaces());

        Project current = Context.getCurrentProject().orElse(null);
        toolBar.refreshProjects(settings.getProjects(), current);

        // Update welcome panel (git projects + workspaces)
        welcomePanel.setItems(settings.getProjects(), settings.getWorkspaces());
    }

    private void showCloneDialog() {
        CloneDialog dialog = new CloneDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new CloneHandler(this, dialog.getParameters()).execute();
        }
    }

    private void showWorkspaceDialog() {
        WorkspaceDialog workspaceDialog =  new
                WorkspaceDialog(this,
                new OpenRecentWorkspaceHanlder(this));

        workspaceDialog.nameField.setText(
                Context.getSettings().createNewWorkspaceName());

        workspaceDialog.setVisible(true);

    }



    private void showInitDialog() {
        InitDialog dialog = new InitDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            new InitHandler(this,  dialog.getParameters()).execute();
        }
    }

    private void showCommitDialog() {
        CommitDialog dialog = new CommitDialog(this);
        dialog.setVisible(true);
    }

    private boolean isResolvableRepoState() {
        if (Context.getGitRepoService() == null) return false;
        RepositoryState s = Context.getGitRepoService().getRepositoryState();
        return s == RepositoryState.MERGING_RESOLVED
                || s == RepositoryState.CHERRY_PICKING_RESOLVED
                || s == RepositoryState.REVERTING_RESOLVED;
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

    public void setRepoActionsEnabled(boolean enabled) {
        menuBar.setRepoActionsEnabled(enabled);
        toolBar.setRepoActionsEnabled(enabled);
    }

    public void setWorkspaceActionsEnabled(boolean enabled) {
        menuBar.setWorkspaceActionEnabled(enabled);
        //toolBar.setRepoActionsEnabled(enabled);
    }


    public boolean isCommitEnabled() {
        boolean commitEnabled = false;
        if (Context.isWorkspaceMode()) {
            if (getActiveView() == ActiveView.WORKSPACE) {
                for (Project project : Context.getWorkspace().getProjects()) { // at least one has staged items
                    if (project.getGitRepoService().hasStaged()) {
                        commitEnabled = true;
                        break;
                    }
                }
            } else { // workspace , but selected particular project
                commitEnabled =  workingCopyPanel.hasStagedItems() || isResolvableRepoState();
            }

        } else {
            commitEnabled =  workingCopyPanel.hasStagedItems() || isResolvableRepoState();
        }
        return commitEnabled;
    }

    /**
     * Recomputes the enabled state of the Pull / Push / Fetch buttons for workspace mode.
     * Pull and Fetch are enabled when at least one project has a remote URL; Push additionally
     * requires at least one project with a remote to have an unpushed HEAD commit. The per-project
     * git reads run off the EDT; the buttons are updated on the EDT once the scan completes.
     */
    private void updateWorkspaceRemoteActions() {
        if (isWorkspaceActive()) {
            java.util.List<Project> projects = new java.util.ArrayList<>(
                    Context.getWorkspace() == null ? List.of() : Context.getWorkspace().getProjects());
            toolBar.setPullEnabled(false);
            toolBar.setPushEnabled(false);
            toolBar.setFetchEnabled(false);
            menuBar.setWorkspacePullEnabled(false);
            menuBar.setWorkspacePushEnabled(false);
            menuBar.setWorkspaceFetchEnabled(false);
            if (projects.isEmpty()) {
                return;
            }
            new SwingWorker<boolean[], Void>() {
                @Override
                protected boolean[] doInBackground() {
                    boolean hasRemote = false;
                    boolean hasUnpushed = false;
                    for (Project project : projects) {
                        try {
                            GitRepoService svc = project.getGitRepoService();
                            if (!svc.isRepositoryHasRemoteUrl()) {
                                continue;
                            }
                            hasRemote = true;
                            CommitInfo head = svc.getHead();
                            if (head.getSha() != null && svc.isCommitUnpushed(head.getSha())) {
                                hasUnpushed = true;
                            }
                        } catch (Exception ex) {
                            log.log(Level.FINE, "Cannot read remote state for " + project, ex);
                        }
                        if (hasRemote && hasUnpushed) break; // nothing left to learn
                    }
                    return new boolean[]{hasRemote, hasUnpushed};
                }

                @Override
                protected void done() {
                    try {
                        boolean[] state = get();
                        toolBar.setPullEnabled(state[0]);
                        toolBar.setFetchEnabled(state[0]);
                        toolBar.setPushEnabled(state[1]);
                        menuBar.setWorkspacePullEnabled(state[0]);
                        menuBar.setWorkspaceFetchEnabled(state[0]);
                        menuBar.setWorkspacePushEnabled(state[1]);
                    } catch (Exception ex) {
                        log.log(Level.FINE, "Cannot update workspace remote actions", ex);
                    }
                }
            }.execute();
        }

    }

    /**
     * Refreshes the workspace dashboard and the remote-action buttons after a workspace-wide
     * push / pull / fetch. Called by the corresponding handlers once their operation completes.
     */
    public void refreshWorkspaceView() {
        if (isWorkspaceActive()) {
            workspaceDashboardPanel.reloadActiveTab();
            updateWorkspaceRemoteActions();
        }

    }

    /**
     * Reloads the sidebar branches/tags/stashes subtree for every project a workspace-wide
     * operation touched successfully. Shared by handlers whose per-project operation can change
     * refs (create branch, fetch, pull, …) so the tree doesn't go stale until the next full rebuild.
     */
    public void refreshWorkspaceProjectBranches(List<? extends ProjectOperationResult<?>> results) {
        for (ProjectOperationResult<?> result : results) {
            if (result.isSuccess()) {
                treePanel.refreshProjectBranches(result.getProject());
            }
        }
    }



    public void updateTitle() {
        String path = Context.getRepositoryPath();
        ScmBranch branch = Context.getWorkingBranch();
        if (getActiveView() == ActiveView.WORKSPACE) {
            setTitle("Gitember - " + Context.getWorkspace().getName());
        } else if (path != null) {
            String folder = Context.getProjectFolder();
            String branchName = branch != null ? " [" + branch.getShortName() + "]" : "";
            setTitle("Gitember - " + folder + branchName);
        } else {
            setTitle("Gitember");
        }
    }

    private Optional<Project> getProject(DefaultMutableTreeNode node) {
        for (TreeNode current = node; current != null; current = current.getParent()) {
            if (current instanceof DefaultMutableTreeNode treeNode) {
                Object userObject = treeNode.getUserObject();

                if (userObject instanceof TreeNodeData treeNodeData) {
                    Object data = treeNodeData.data();

                    // A repository node carries its Project directly.
                    if (data instanceof Project project) {
                        return Optional.of(project);
                    }

                    // Category nodes carry a Supplier<Optional<Project>>.
                    if (data instanceof Supplier<?> supplier
                            && supplier.get() instanceof Optional<?> optional) {
                        return optional
                                .filter(Project.class::isInstance)
                                .map(Project.class::cast);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /** Projects are interned (Settings#internAll), so identity is a valid, O(1) test. */
    private boolean isCurrentProject(Project p) {
        return p == Context.getActiveProject();
    }

    private void onTreeSelection(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
        if (node == null)  return;
        getProject(node).ifPresent(
                p-> {
                    // Re-initializing an already-open project reloads branches/tags and rebuilds
                    // the tree nodes, which clears the selection just made. Only switch context
                    // when the click actually targets a different project (workspace mode).
                    if (!isCurrentProject(p)) {
                        try {
                            Context.init(p);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

        );

        Object userObject = node.getUserObject();
        if (userObject instanceof TreeNodeData data) {


            boolean isWorkSpace = data.type() == CellRenderer.NodeType.WORKSPACE;
            boolean isWorkingCopy = data.type() == CellRenderer.NodeType.WORKING_COPY;
            boolean isAllHistory  = data.type() == CellRenderer.NodeType.HISTORY;
            boolean isPullRequest = data.type() == CellRenderer.NodeType.PULL_REQUEST;

            // Merge/unmerge working copy toolbar

            if (isWorkSpace) {
                toolBar.mergeWorkSpaceToolbar(workspaceDashboardPanel);
            } else {
                toolBar.unmergeWorkSpaceToolbar();
            }

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
                case WORKSPACE -> {
                    Context.setRepositoryPath(null);
                    setActiveView(ActiveView.WORKSPACE);
                    contentPanel.setContent(workspaceDashboardPanel);
                    workspaceDashboardPanel.refreshButtonStates();
                    workspaceDashboardPanel.refresh();
                    updateWorkspaceRemoteActions();
                    updateTitle();
                }
                case REPOSITORY -> {
                    // Clicking a workspace repository opens that project's working copy.
                    activateProjectWorkingCopy();
                }
                case WORKING_COPY -> {
                    activateProjectWorkingCopy();
                }
                case HISTORY -> {
                    setActiveView(ActiveView.HISTORY);
                    contentPanel.setContent(historyPanel);
                    historyPanel.loadHistory(null, true);
                }
                case BRANCH -> {
                    setActiveView( ActiveView.HISTORY);
                    if (data.data() instanceof ScmBranch branch) {
                        contentPanel.setContent(historyPanel);
                        historyPanel.loadHistory(branch.getFullName(), false);
                    }
                }
                case TAG -> {
                     setActiveView( ActiveView.HISTORY);
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
                case WORKTREE -> { /* handled via context menu */ }
                default -> contentPanel.setContent(null);
            }
        }
    }

    public void activateProjectWorkingCopy() {
         setActiveView( ActiveView.WORKING_COPY);
        contentPanel.setContent(workingCopyPanel);
        List<ScmItem> cachedStatus = Context.getStatusList();
        workingCopyPanel.setItems(cachedStatus); // show cached immediately
        boolean commitEnabled = isCommitEnabled();
        toolBar.setCommitEnabled(commitEnabled);
        menuBar.setCommitEnabled(commitEnabled);
        menuBar.setCreateDiffEnabled(cachedStatus != null && !cachedStatus.isEmpty());
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                Context.updateStatus(null, true);
                return null;
            }
        }.execute();
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
        setActiveView(ActiveView.HISTORY);
        contentPanel.setContent(historyPanel);
        toolBar.mergeHistoryToolbar(historyPanel);
        historyPanel.loadHistoryAndSelect(sha);
    }

    /**
     * Switches the main content area to the working copy view.
     * Used after a pull that produced conflicts so the user immediately sees
     * which files need resolution.
     */
    public void showWorkingCopy() {
        setActiveView(ActiveView.WORKING_COPY);
        contentPanel.setContent(workingCopyPanel);
        toolBar.mergeWorkingCopyToolbar(workingCopyPanel);
        Context.updateStatus(null, true);
    }


    // ── Terminal launcher ─────────────────────────────────────────────────────

    private void openExplorerInRepo() {
        String path = Context.getProjectFolder();
        if (path == null || path.isEmpty()) {
            statusBar.setStatus("No repository open.");
            return;
        }
        File dir = new File(path);
        try {
            if (GitemberUtil.isWindows()) {
                new ProcessBuilder("explorer.exe", dir.getAbsolutePath()).start();
            } else if (GitemberUtil.isMac()) {
                new ProcessBuilder("open",  dir.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", dir.getAbsolutePath()).start();
            }
            statusBar.setStatus("File manager opened in " + path);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot open file manager", ex);
            statusBar.setStatus("Cannot open file manager: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Could not open a file manager:\n" + ex.getMessage(),
                    "Open File Manager", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTerminalInRepo() {
        String path = Context.getProjectFolder();
        if (path == null || path.isEmpty()) {
            statusBar.setStatus("No repository open.");
            return;
        }
        File dir = new File(path);
        try {
            if (GitemberUtil.isWindows()) {
                openTerminalWindows(dir);
            } else if (GitemberUtil.isMac()) {
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
