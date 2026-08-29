package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.dialog.SettingsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MainMenuBar extends JMenuBar {

    // File menu items
    private final JMenuItem openItem;
    private final JMenuItem cloneItem;
    private final JMenuItem initRepository;
    private final JMenuItem initWorkspaceItem;
    private final JMenu     openRecentMenu;
    private  JMenuItem settingsItem;
    private final JMenuItem exitItem;

    // Repository menu (enabled only when a repo is open)
    private final JMenu     repoMenu;
    private final JMenuItem indexHistoryItem;
    private final JMenuItem statisticsItem;
    private final JMenuItem openTerminalItem;
    private final JMenuItem openExplorer;
    private final JMenuItem projectSettingsItem;

    // LFS submenu (inside Repository menu)
    private final JMenu     lfsMenu;
    private final JMenuItem manageLfsItem;
    private final JMenuItem fetchLfsItem;

    // Submodules submenu (inside Repository menu)
    private final JMenu     submodulesMenu;
    private final JMenuItem updateSubmodulesItem;
    private final JMenuItem syncSubmodulesItem;

    // Repository maintenance
    private final JMenuItem compressDatabaseItem;

    // Branch menu (enabled only when a repo is open)
    private final JMenu     branchMenu;
    private final JMenuItem pullItem;
    private final JMenuItem pushItem;
    private final JMenuItem fetchItem;
    private final JMenuItem commitItem;
    private final JMenuItem branchCreateItem;
    private final JMenuItem mergeItem;
    private final JMenuItem interactiveRebaseItem;

    private final JMenu     workspaceMenu;
    private final JMenuItem workspacePullItem;
    private final JMenuItem workspacePushItem;
    private final JMenuItem workspaceFetchItem;
    private final JMenuItem workspaceCommitItem;
    private final JMenuItem workspaceBranchCreateItem;
    private final JMenuItem workspaceMergeItem;

    // Working copy menu (enabled only when a repo is open)
    private final JMenu     workingCopyMenu;
    private final JMenuItem refreshItem;
    private final JMenuItem stashItem;
    private final JMenuItem worktreesItem;
    private final JMenuItem createDiffItem;
    private final JMenuItem applyDiffItem;

    // Tools menu items (always available – no repo required)
    private final JMenuItem compareFilesItem;
    private final JMenuItem compareFoldersItem;

    // Help menu items
    private final JMenuItem helpContentsItem;
    private final JMenuItem aboutItem;

    private Consumer<Project> recentProjectHandler;
    private Consumer<Workspace> recentWorkspaceHandler;

    public MainMenuBar() {

        // -- File -------------------------------------------------------------
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        openItem = new JMenuItem("Open Repository...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cloneItem = new JMenuItem("Clone Repository...", KeyEvent.VK_C);
        initRepository = new JMenuItem("Init Repository...",  KeyEvent.VK_I);
        initWorkspaceItem  = new JMenuItem("Init Workspace...");
        initWorkspaceItem.setName("initWorkspaceItem");

        openRecentMenu = new JMenu("Open Recent");
        openRecentMenu.setMnemonic(KeyEvent.VK_R);
        openRecentMenu.setEnabled(false);






        exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(cloneItem);
        fileMenu.add(initRepository);
        fileMenu.addSeparator();
        fileMenu.add(initWorkspaceItem);
        fileMenu.addSeparator();
        fileMenu.add(openRecentMenu);

        if(GitemberUtil.isMac()) {
            // Uses 'meta' for Command key
            //settingsItem.setAccelerator(KeyStroke.getKeyStroke("meta COMMA"));
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
                    // 2. Attach your handler directly to the native macOS "Preferences..." item
                    desktop.setPreferencesHandler(e -> {
                        new SettingsDialog(MainFrame.getInstance()).setVisible(true);
                    });
                }
            }

        } else {
            settingsItem = new JMenuItem("Settings...", KeyEvent.VK_S);
            fileMenu.addSeparator();
            fileMenu.add(settingsItem);
        }


        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // -- Repository (repo-only) --------------------------------------------
        repoMenu = new JMenu("Repository");
        repoMenu.setMnemonic(KeyEvent.VK_R);

        indexHistoryItem = new JMenuItem("Index History…", KeyEvent.VK_I);
        indexHistoryItem.setToolTipText(
                "Build a Lucene index of commit file content for full-text search");

        statisticsItem = new JMenuItem("Statistics…", KeyEvent.VK_S);
        statisticsItem.setToolTipText(
                "Show per-developer commit / line statistics and monthly charts");

        projectSettingsItem = new JMenuItem("Project Settings…", KeyEvent.VK_P);


        String expMessage = null;
        String expToolTip = null;
        if (GitemberUtil.isWindows()) {
            expMessage = "Explorer";
            expToolTip = "Open a explorer in the repository folder";
        } else if (GitemberUtil.isMac()) {
            expMessage = "Finder";
            expToolTip = "Open a finder in the repository folder";
        } else if (GitemberUtil.isLinux()) {
            expMessage = "File manager";
            expToolTip = "Open a file manage in the repository folder";
        }

        if (StringUtils.isNotBlank(expMessage)) {
            openExplorer = new JMenuItem(expMessage, KeyEvent.VK_E);
            openExplorer.setToolTipText(expToolTip);
        } else {
            openExplorer = null;
        }



        openTerminalItem = new JMenuItem("Terminal", KeyEvent.VK_T);
        openTerminalItem.setToolTipText("Open a terminal in the repository folder");

        // LFS submenu
        lfsMenu = new JMenu("Git LFS");
        lfsMenu.setMnemonic(KeyEvent.VK_L);

        manageLfsItem = new JMenuItem("Manage LFS…", KeyEvent.VK_M);
        manageLfsItem.setToolTipText("Enable LFS, track patterns, view LFS files, fetch objects");

        fetchLfsItem = new JMenuItem("Fetch LFS Objects", KeyEvent.VK_F);
        fetchLfsItem.setToolTipText("Download LFS file content from the remote LFS server");

        lfsMenu.add(manageLfsItem);
        lfsMenu.addSeparator();
        lfsMenu.add(fetchLfsItem);

        // Submodules submenu
        submodulesMenu = new JMenu("Submodules");
        submodulesMenu.setMnemonic(KeyEvent.VK_U);

        updateSubmodulesItem = new JMenuItem("Update Submodules", KeyEvent.VK_U);
        updateSubmodulesItem.setToolTipText("Run git submodule init + update for all submodules");

        syncSubmodulesItem = new JMenuItem("Sync Submodule URLs", KeyEvent.VK_Y);
        syncSubmodulesItem.setToolTipText("Update recorded remote URLs from .gitmodules");

        submodulesMenu.add(updateSubmodulesItem);
        submodulesMenu.add(syncSubmodulesItem);

        compressDatabaseItem = new JMenuItem("Compress Database", KeyEvent.VK_Z);
        compressDatabaseItem.setToolTipText("Run git gc — pack loose objects and prune unreachable data");

        repoMenu.add(indexHistoryItem);
        repoMenu.add(statisticsItem);
        repoMenu.addSeparator();
        repoMenu.add(lfsMenu);
        repoMenu.add(submodulesMenu);
        repoMenu.add(projectSettingsItem);
        repoMenu.addSeparator();
        repoMenu.add(compressDatabaseItem);
        repoMenu.addSeparator();
        repoMenu.add(openTerminalItem);
        if (openExplorer != null) {
            repoMenu.add(openExplorer);
        }


        // -- Branch (repo-only) ------------------------------------------------
        branchMenu = new JMenu("Branch");
        branchMenu.setMnemonic(KeyEvent.VK_B);

        pullItem             = Util.createMenuItem("Pull", null,  FontAwesomeSolid.REPLY, -45);
        pushItem             = Util.createMenuItem("Push", null,  FontAwesomeSolid.REPLY, 135);
        fetchItem            = Util.createMenuItem("Fetch", "Fetch changes from remote repository", FontAwesomeSolid.REPLY_ALL, -45);
        commitItem           = Util.createMenuItem("Commit ...", "Commit", FontAwesomeSolid.CHECK, 0);
        branchCreateItem     = Util.createMenuItem("Branch ...", "Create branch", FontAwesomeSolid.CODE_BRANCH, 0);
        mergeItem            = Util.createMenuItem("Merge ...", "Merge branch into working branch", FontAwesomeSolid.CODE_BRANCH, 180);

        interactiveRebaseItem = new JMenuItem("Interactive Rebase…",   KeyEvent.VK_I);
        interactiveRebaseItem.setToolTipText(
                "Interactively rebase commits – right-click a commit in the history for the full workflow");

        branchMenu.add(branchCreateItem);
        branchMenu.add(mergeItem);
        branchMenu.addSeparator();
        branchMenu.add(pullItem);
        branchMenu.add(pushItem);
        branchMenu.add(fetchItem);
        branchMenu.addSeparator();
        branchMenu.add(commitItem);
        branchMenu.addSeparator();
        branchMenu.add(interactiveRebaseItem);
        
        // -- Workspace ---------------------------------------------------------
        workspaceMenu = new JMenu("Workspace");
        workspacePullItem             = Util.createMenuItem("Pull", "Pull all repositories under workspace",  FontAwesomeSolid.REPLY, -45);
        workspacePushItem             = Util.createMenuItem("Push", "Push  all repositories under workspace",  FontAwesomeSolid.REPLY, 135);
        workspaceFetchItem            = Util.createMenuItem("Fetch", "Fetch changes all repository", FontAwesomeSolid.REPLY_ALL, -45);
        workspaceCommitItem           =  Util.createMenuItem("Commit ...", "Commit", FontAwesomeSolid.CHECK, 0);
        workspaceBranchCreateItem     = Util.createMenuItem("Branch ...", "Create branches in each repository", FontAwesomeSolid.CODE_BRANCH, 0);
        workspaceMergeItem            = Util.createMenuItem("Merge ...", "Merge branches ", FontAwesomeSolid.CODE_BRANCH, 180);

        workspaceMenu.setName("workspaceMenu");
        workspacePullItem.setName("workspacePullItem");
        workspacePushItem.setName("workspacePushItem");
        workspaceFetchItem.setName("workspaceFetchItem");
        workspaceCommitItem.setName("workspaceCommitItem");
        workspaceBranchCreateItem.setName("workspaceBranchCreateItem");
        workspaceMergeItem.setName("workspaceMergeItem");

        workspaceMenu.add(workspaceBranchCreateItem);
        workspaceMenu.add(workspaceMergeItem);

        workspaceMenu.addSeparator();

        workspaceMenu.add(workspacePullItem);
        workspaceMenu.add(workspacePushItem);
        workspaceMenu.add(workspaceFetchItem);
        workspaceMenu.addSeparator();
        workspaceMenu.add(workspaceCommitItem);



        // -- Working copy (repo-only) ------------------------------------------
        workingCopyMenu = new JMenu("Working copy");
        workingCopyMenu.setMnemonic(KeyEvent.VK_W);

        refreshItem    =  Util.createMenuItem("Refresh", null, FontAwesomeSolid.SYNC, 0);
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

        stashItem      = new JMenuItem("Stash...",       KeyEvent.VK_S);
        worktreesItem  = new JMenuItem("Worktrees...",   KeyEvent.VK_W);
        worktreesItem.setToolTipText("Manage linked working trees (git worktree)");
        createDiffItem = new JMenuItem("Create diff",    KeyEvent.VK_D);
        createDiffItem.setEnabled(false);
        applyDiffItem  = new JMenuItem("Apply diff...",  KeyEvent.VK_A);

        workingCopyMenu.add(refreshItem);
        workingCopyMenu.add(stashItem);
        workingCopyMenu.add(worktreesItem);
        workingCopyMenu.addSeparator();
        workingCopyMenu.add(createDiffItem);
        workingCopyMenu.add(applyDiffItem);

        // -- Tools (always available) ------------------------------------------
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);

        compareFilesItem   = new JMenuItem("Compare Files…",   KeyEvent.VK_F);
        compareFoldersItem = new JMenuItem("Compare Folders…", KeyEvent.VK_D);

        compareFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        compareFoldersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        toolsMenu.add(compareFilesItem);
        toolsMenu.add(compareFoldersItem);

        // -- Help -------------------------------------------------------------
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        helpContentsItem = new JMenuItem("Help Contents", KeyEvent.VK_H);
        helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpContentsItem.addActionListener(e -> {
            try { Desktop.getDesktop().browse(new URI("http://www.gitember.org/doc/docs/ge-intro/index.html")); }
            catch (Exception ex) { /* ignore */ }
        });
        helpMenu.add(helpContentsItem);
        helpMenu.addSeparator();

        aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> {
            JEditorPane ep = new JEditorPane("text/html",
                    "<html><body style='font-family:sans-serif;font-size:12px'>" +
                    "<b>Gitember 3.4.1 </b> — Git GUI Client<br><br>" +
                    "Web site: <a href='https://gitember.org/'>https://gitember.org/</a><br>" +
                    "Support: <a href='https://github.com/iazarny/gitember/issues'>https://github.com/iazarny/gitember/issues</a><br>" +
                    "</body></html>");
            ep.setEditable(false);
            ep.setOpaque(false);
            ep.addHyperlinkListener(ev -> {
                if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(ev.getURL().toURI());
                    }
                    catch (Exception ex) { /* ignore */ }
                }
            });
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    ep,
                    "About Gitember",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        // -- Menu bar order ----------------------------------------------------
        add(fileMenu);
        add(repoMenu);
        add(branchMenu);
        add(workspaceMenu);
        add(workingCopyMenu);
        add(toolsMenu);
        add(helpMenu);

        setRepoActionsEnabled(false);
        setWorkspaceActionEnabled(false);
    }

    // -- Recent projects & workspaces ------------------------------------------

    public void refreshRecentProjects(Set<Project> projects) {
        refreshRecent(projects, null);
    }

    /**
     * Rebuilds the "Open Recent" menu with recent git projects and, below a separator,
     * recent workspaces. The menu is disabled when both are empty.
     */
    public void refreshRecent(Set<Project> projects, List<Workspace> workspaces) {
        openRecentMenu.removeAll();

        boolean hasProjects   = projects != null && !projects.isEmpty();
        boolean hasWorkspaces = workspaces != null && !workspaces.isEmpty();

        if (!hasProjects && !hasWorkspaces) {
            openRecentMenu.setEnabled(false);
            return;
        }

        if (hasProjects) {
            for (Project project : projects) {
                String label = GitemberUtil.getFolderName(project.getProjectHomeFolder());
                JMenuItem item = new JMenuItem(label);
                item.setToolTipText(project.getProjectHomeFolder());
                item.addActionListener(e -> {
                    if (recentProjectHandler != null) recentProjectHandler.accept(project);
                });
                openRecentMenu.add(item);
            }
        }

        if (hasWorkspaces) {
            if (hasProjects) openRecentMenu.addSeparator();
            for (Workspace workspace : workspaces) {
                JMenuItem item = new JMenuItem(workspace.getName());
                item.addActionListener(e -> {
                    if (recentWorkspaceHandler != null) recentWorkspaceHandler.accept(workspace);
                });
                openRecentMenu.add(item);
            }
        }

        openRecentMenu.setEnabled(true);
    }

    public void setRecentProjectHandler(Consumer<Project> handler) {
        this.recentProjectHandler = handler;
    }

    public void setRecentWorkspaceHandler(Consumer<Workspace> handler) {
        this.recentWorkspaceHandler = handler;
    }

    // -- Enable / disable all repo-dependent menus at once ---------------------

    public void setRepoActionsEnabled(boolean enabled) {
        repoMenu.setVisible(enabled);
        branchMenu.setVisible(enabled);
        workingCopyMenu.setVisible(enabled);
    }

    public void setWorkspaceActionEnabled(boolean enabled) {
        workspaceMenu.setVisible(enabled);
    }

    public void setCommitEnabled(boolean commitEnabled) {
        commitItem.setEnabled(commitEnabled);
        workspaceCommitItem.setEnabled(commitEnabled);
    }

    public void setWorkspacePullEnabled(boolean enabled)  { workspacePullItem.setEnabled(enabled); }
    public void setWorkspacePushEnabled(boolean enabled)  { workspacePushItem.setEnabled(enabled); }
    public void setWorkspaceFetchEnabled(boolean enabled) { workspaceFetchItem.setEnabled(enabled); }

    // -- Listener registration -------------------------------------------------

    public void addOpenListener(ActionListener l)          { openItem.addActionListener(l); }
    public void addCloneListener(ActionListener l)         { cloneItem.addActionListener(l); }
    public void addInitListener(ActionListener l)          { initRepository.addActionListener(l); }
    public void addInitWorkspaceListener(ActionListener l)  { initWorkspaceItem.addActionListener(l); }
    public void addPullListener(ActionListener l)          { pullItem.addActionListener(l); }
    public void addPushListener(ActionListener l)          { pushItem.addActionListener(l); }
    public void addFetchListener(ActionListener l)         { fetchItem.addActionListener(l); }
    public void addCommitListener(ActionListener l)        { commitItem.addActionListener(l); }
    public void addMergeListener(ActionListener l)         { mergeItem.addActionListener(l); }
    public void addCreateBranchListener(ActionListener l)  { branchCreateItem.addActionListener(l); }
    public void addRefreshListener(ActionListener l)       { refreshItem.addActionListener(l); }
    public void addStashListener(ActionListener l)         { stashItem.addActionListener(l); }
    public void setCreateDiffEnabled(boolean enabled)      { createDiffItem.setEnabled(enabled); }
    public void addCreateDiffListener(ActionListener l)    { createDiffItem.addActionListener(l); }
    public void addApplyDiffListener(ActionListener l)     { applyDiffItem.addActionListener(l); }
    public void addWorktreesListener(ActionListener l)     { worktreesItem.addActionListener(l); }
    public void addProjectSettingsListener(ActionListener l)  { projectSettingsItem.addActionListener(l); }
    public void addSettingsListener(ActionListener l)      { settingsItem.addActionListener(l); }
    public void addCompareFilesListener(ActionListener l)  { compareFilesItem.addActionListener(l); }
    public void addCompareFoldersListener(ActionListener l){ compareFoldersItem.addActionListener(l); }
    public void addIndexHistoryListener(ActionListener l)   { indexHistoryItem.addActionListener(l); }
    public void addStatisticsListener(ActionListener l)     { statisticsItem.addActionListener(l); }
    public void addOpenTerminalListener(ActionListener l)   { openTerminalItem.addActionListener(l); }
    public void addOpenExplorerListener(ActionListener l)   {
        if (openExplorer != null) {
            openExplorer.addActionListener(l);
        }
    }
    public void addManageLfsListener(ActionListener l)          { manageLfsItem.addActionListener(l); }
    public void addFetchLfsListener(ActionListener l)           { fetchLfsItem.addActionListener(l); }
    public void addCompressDatabaseListener(ActionListener l)    { compressDatabaseItem.addActionListener(l); }
    public void addUpdateSubmodulesListener(ActionListener l)   { updateSubmodulesItem.addActionListener(l); }
    public void addSyncSubmodulesListener(ActionListener l)     { syncSubmodulesItem.addActionListener(l); }
    public void addInteractiveRebaseListener(ActionListener l)   { interactiveRebaseItem.addActionListener(l); }
    public void addHelpContentsListener(ActionListener l)       { helpContentsItem.addActionListener(l); }

    public void addWorskpacePullListener(ActionListener l)       { workspacePullItem.addActionListener(l); }
    public void addWorskpacePushListener(ActionListener l)       { workspacePushItem.addActionListener(l); }
    public void addWorskpaceFetchListener(ActionListener l)       { workspaceFetchItem.addActionListener(l); }
    public void addWorskpaceCommitListener(ActionListener l)       { workspaceCommitItem.addActionListener(l); }
    public void addWorskpaceCreateBranchListener(ActionListener l) { workspaceBranchCreateItem.addActionListener(l); }
    public void addWorskpaceMergeListener(ActionListener l)        { workspaceMergeItem.addActionListener(l); }


}
