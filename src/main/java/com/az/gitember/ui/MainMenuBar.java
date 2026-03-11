package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.service.GitemberUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Set;
import java.util.function.Consumer;

public class MainMenuBar extends JMenuBar {

    // File menu items
    private final JMenuItem openItem;
    private final JMenuItem cloneItem;
    private final JMenuItem initItem;
    private final JMenu     openRecentMenu;
    private final JMenuItem settingsItem;
    private final JMenuItem exitItem;

    // Repository menu (enabled only when a repo is open)
    private final JMenu     repoMenu;
    private final JMenuItem indexHistoryItem;
    private final JMenuItem statisticsItem;
    private final JMenuItem openTerminalItem;
    private final JMenuItem credentialsItem;

    // LFS submenu (inside Repository menu)
    private final JMenu     lfsMenu;
    private final JMenuItem manageLfsItem;
    private final JMenuItem fetchLfsItem;

    // Submodules submenu (inside Repository menu)
    private final JMenu     submodulesMenu;
    private final JMenuItem updateSubmodulesItem;
    private final JMenuItem syncSubmodulesItem;

    // Branch menu (enabled only when a repo is open)
    private final JMenu     branchMenu;
    private final JMenuItem pullItem;
    private final JMenuItem pushItem;
    private final JMenuItem fetchItem;
    private final JMenuItem commitItem;

    // Working copy menu (enabled only when a repo is open)
    private final JMenu     workingCopyMenu;
    private final JMenuItem refreshItem;
    private final JMenuItem stashItem;
    private final JMenuItem createDiffItem;
    private final JMenuItem applyDiffItem;

    // Tools menu items (always available – no repo required)
    private final JMenuItem compareFilesItem;
    private final JMenuItem compareFoldersItem;

    // Help menu items
    private final JMenuItem aboutItem;

    private Consumer<Project> recentProjectHandler;

    public MainMenuBar() {

        // ── File ─────────────────────────────────────────────────────────────
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        openItem = new JMenuItem("Open Repository...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cloneItem = new JMenuItem("Clone Repository...", KeyEvent.VK_C);
        initItem  = new JMenuItem("Init Repository...",  KeyEvent.VK_I);

        openRecentMenu = new JMenu("Open Recent");
        openRecentMenu.setMnemonic(KeyEvent.VK_R);
        openRecentMenu.setEnabled(false);

        settingsItem = new JMenuItem("Settings...", KeyEvent.VK_S);

        exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(cloneItem);
        fileMenu.add(initItem);
        fileMenu.addSeparator();
        fileMenu.add(openRecentMenu);
        fileMenu.addSeparator();
        fileMenu.add(settingsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ── Repository (repo-only) ────────────────────────────────────────────
        repoMenu = new JMenu("Repository");
        repoMenu.setMnemonic(KeyEvent.VK_R);

        indexHistoryItem = new JMenuItem("Index History…", KeyEvent.VK_I);
        indexHistoryItem.setToolTipText(
                "Build a Lucene index of commit file content for full-text search");

        statisticsItem = new JMenuItem("Statistics…", KeyEvent.VK_S);
        statisticsItem.setToolTipText(
                "Show per-developer commit / line statistics and monthly charts");

        credentialsItem = new JMenuItem("Credentials…", KeyEvent.VK_E);

        openTerminalItem = new JMenuItem("Open Terminal", KeyEvent.VK_T);
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

        repoMenu.add(indexHistoryItem);
        repoMenu.add(statisticsItem);
        repoMenu.addSeparator();
        repoMenu.add(lfsMenu);
        repoMenu.add(submodulesMenu);
        repoMenu.addSeparator();
        repoMenu.add(openTerminalItem);
        repoMenu.addSeparator();
        repoMenu.add(credentialsItem);

        // ── Branch (repo-only) ────────────────────────────────────────────────
        branchMenu = new JMenu("Branch");
        branchMenu.setMnemonic(KeyEvent.VK_B);

        pullItem   = new JMenuItem("Pull",       KeyEvent.VK_L);
        pushItem   = new JMenuItem("Push",       KeyEvent.VK_P);
        fetchItem  = new JMenuItem("Fetch",      KeyEvent.VK_F);
        commitItem = new JMenuItem("Commit...",  KeyEvent.VK_M);

        branchMenu.add(pullItem);
        branchMenu.add(pushItem);
        branchMenu.add(fetchItem);
        branchMenu.addSeparator();
        branchMenu.add(commitItem);

        // ── Working copy (repo-only) ──────────────────────────────────────────
        workingCopyMenu = new JMenu("Working copy");
        workingCopyMenu.setMnemonic(KeyEvent.VK_W);

        refreshItem    = new JMenuItem("Refresh",        KeyEvent.VK_R);
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        stashItem      = new JMenuItem("Stash...",       KeyEvent.VK_S);
        createDiffItem = new JMenuItem("Create diff",    KeyEvent.VK_D);
        applyDiffItem  = new JMenuItem("Apply diff...",  KeyEvent.VK_A);

        workingCopyMenu.add(refreshItem);
        workingCopyMenu.add(stashItem);
        workingCopyMenu.addSeparator();
        workingCopyMenu.add(createDiffItem);
        workingCopyMenu.add(applyDiffItem);

        // ── Tools (always available) ──────────────────────────────────────────
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);

        compareFilesItem   = new JMenuItem("Compare Files…",   KeyEvent.VK_F);
        compareFoldersItem = new JMenuItem("Compare Folders…", KeyEvent.VK_D);

        compareFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        compareFoldersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        toolsMenu.add(compareFilesItem);
        toolsMenu.add(compareFoldersItem);

        // ── Help ─────────────────────────────────────────────────────────────
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> {
            JEditorPane ep = new JEditorPane("text/html",
                    "<html><body style='font-family:sans-serif;font-size:12px'>" +
                    "<b>Gitember 3</b> — Git GUI Client<br><br>" +
                    "Web site: <a href='https://gitember.org/'>https://gitember.org/</a><br>" +
                    "Support: <a href='https://github.com/iazarny/gitember/issues'>https://github.com/iazarny/gitember/issues</a><br>" +
                    "</body></html>");
            ep.setEditable(false);
            ep.setOpaque(false);
            ep.addHyperlinkListener(ev -> {
                if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try { Desktop.getDesktop().browse(new URI("https://gitember.org/")); }
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

        // ── Menu bar order ────────────────────────────────────────────────────
        add(fileMenu);
        add(repoMenu);
        add(branchMenu);
        add(workingCopyMenu);
        add(toolsMenu);
        add(helpMenu);

        setRepoActionsEnabled(false);
    }

    // ── Recent projects ───────────────────────────────────────────────────────

    public void refreshRecentProjects(Set<Project> projects) {
        openRecentMenu.removeAll();
        if (projects == null || projects.isEmpty()) {
            openRecentMenu.setEnabled(false);
            return;
        }
        for (Project project : projects) {
            String label = GitemberUtil.getFolderName(project.getProjectHomeFolder());
            JMenuItem item = new JMenuItem(label);
            item.setToolTipText(project.getProjectHomeFolder());
            item.addActionListener(e -> {
                if (recentProjectHandler != null) recentProjectHandler.accept(project);
            });
            openRecentMenu.add(item);
        }
        openRecentMenu.setEnabled(true);
    }

    public void setRecentProjectHandler(Consumer<Project> handler) {
        this.recentProjectHandler = handler;
    }

    // ── Enable / disable all repo-dependent menus at once ─────────────────────

    public void setRepoActionsEnabled(boolean enabled) {
        // Entire menus go grey when no repo is open
        /*repoMenu.setEnabled(enabled);
        branchMenu.setEnabled(enabled);
        workingCopyMenu.setEnabled(enabled);*/

        repoMenu.setVisible(enabled);
        branchMenu.setVisible(enabled);
        workingCopyMenu.setVisible(enabled);
    }

    // ── Listener registration ─────────────────────────────────────────────────

    public void addOpenListener(ActionListener l)          { openItem.addActionListener(l); }
    public void addCloneListener(ActionListener l)         { cloneItem.addActionListener(l); }
    public void addInitListener(ActionListener l)          { initItem.addActionListener(l); }
    public void addPullListener(ActionListener l)          { pullItem.addActionListener(l); }
    public void addPushListener(ActionListener l)          { pushItem.addActionListener(l); }
    public void addFetchListener(ActionListener l)         { fetchItem.addActionListener(l); }
    public void addCommitListener(ActionListener l)        { commitItem.addActionListener(l); }
    public void addRefreshListener(ActionListener l)       { refreshItem.addActionListener(l); }
    public void addStashListener(ActionListener l)         { stashItem.addActionListener(l); }
    public void addCreateDiffListener(ActionListener l)    { createDiffItem.addActionListener(l); }
    public void addApplyDiffListener(ActionListener l)     { applyDiffItem.addActionListener(l); }
    public void addCredentialsListener(ActionListener l)   { credentialsItem.addActionListener(l); }
    public void addSettingsListener(ActionListener l)      { settingsItem.addActionListener(l); }
    public void addCompareFilesListener(ActionListener l)  { compareFilesItem.addActionListener(l); }
    public void addCompareFoldersListener(ActionListener l){ compareFoldersItem.addActionListener(l); }
    public void addIndexHistoryListener(ActionListener l)   { indexHistoryItem.addActionListener(l); }
    public void addStatisticsListener(ActionListener l)     { statisticsItem.addActionListener(l); }
    public void addOpenTerminalListener(ActionListener l)   { openTerminalItem.addActionListener(l); }
    public void addManageLfsListener(ActionListener l)          { manageLfsItem.addActionListener(l); }
    public void addFetchLfsListener(ActionListener l)           { fetchLfsItem.addActionListener(l); }
    public void addUpdateSubmodulesListener(ActionListener l)   { updateSubmodulesItem.addActionListener(l); }
    public void addSyncSubmodulesListener(ActionListener l)     { syncSubmodulesItem.addActionListener(l); }
}
