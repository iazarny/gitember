package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.service.GitemberUtil;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.function.Consumer;

public class MainMenuBar extends JMenuBar {

    // File menu items
    private final JMenuItem openItem;
    private final JMenuItem cloneItem;
    private final JMenuItem initItem;
    private final JMenu openRecentMenu;
    private final JMenuItem exitItem;

    // Branch menu items
    private final JMenuItem pullItem;
    private final JMenuItem pushItem;
    private final JMenuItem fetchItem;
    private final JMenuItem commitItem;

    // Working copy menu items
    private final JMenuItem refreshItem;
    private final JMenuItem stashItem;
    private final JMenuItem createDiffItem;
    private final JMenuItem applyDiffItem;

    // Repository credentials
    private final JMenuItem credentialsItem;

    // Settings
    private final JMenuItem settingsItem;

    // Help menu items
    private final JMenuItem aboutItem;

    private Consumer<Project> recentProjectHandler;

    public MainMenuBar() {
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        openItem = new JMenuItem("Open Repository...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        cloneItem = new JMenuItem("Clone Repository...", KeyEvent.VK_C);
        initItem = new JMenuItem("Init Repository...", KeyEvent.VK_I);

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

        // Branch menu
        JMenu branchMenu = new JMenu("Branch");
        branchMenu.setMnemonic(KeyEvent.VK_B);

        pullItem = new JMenuItem("Pull", KeyEvent.VK_L);
        pushItem = new JMenuItem("Push", KeyEvent.VK_P);
        fetchItem = new JMenuItem("Fetch", KeyEvent.VK_F);
        commitItem = new JMenuItem("Commit...", KeyEvent.VK_M);
        credentialsItem = new JMenuItem("Credentials...", KeyEvent.VK_E);

        branchMenu.add(pullItem);
        branchMenu.add(pushItem);
        branchMenu.add(fetchItem);
        branchMenu.addSeparator();
        branchMenu.add(commitItem);
        branchMenu.addSeparator();
        branchMenu.add(credentialsItem);

        // Working copy menu
        JMenu workingCopyMenu = new JMenu("Working copy");
        workingCopyMenu.setMnemonic(KeyEvent.VK_W);

        refreshItem = new JMenuItem("Refresh", KeyEvent.VK_R);
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        stashItem = new JMenuItem("Stash...", KeyEvent.VK_S);
        createDiffItem = new JMenuItem("Create diff", KeyEvent.VK_D);
        applyDiffItem = new JMenuItem("Apply diff...", KeyEvent.VK_A);

        workingCopyMenu.add(refreshItem);
        workingCopyMenu.add(stashItem);
        workingCopyMenu.addSeparator();
        workingCopyMenu.add(createDiffItem);
        workingCopyMenu.add(applyDiffItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Gitember 3 - Git GUI Client",
                "About Gitember",
                JOptionPane.INFORMATION_MESSAGE));

        helpMenu.add(aboutItem);

        add(fileMenu);
        add(branchMenu);
        add(workingCopyMenu);
        add(helpMenu);

        setRepoActionsEnabled(false);
    }

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
                if (recentProjectHandler != null) {
                    recentProjectHandler.accept(project);
                }
            });
            openRecentMenu.add(item);
        }
        openRecentMenu.setEnabled(true);
    }

    public void setRecentProjectHandler(Consumer<Project> handler) {
        this.recentProjectHandler = handler;
    }

    public void setRepoActionsEnabled(boolean enabled) {
        pullItem.setEnabled(enabled);
        pushItem.setEnabled(enabled);
        fetchItem.setEnabled(enabled);
        commitItem.setEnabled(enabled);
        credentialsItem.setEnabled(enabled);
        refreshItem.setEnabled(enabled);
        stashItem.setEnabled(enabled);
        createDiffItem.setEnabled(enabled);
        applyDiffItem.setEnabled(enabled);
    }

    public void addOpenListener(ActionListener l) { openItem.addActionListener(l); }
    public void addCloneListener(ActionListener l) { cloneItem.addActionListener(l); }
    public void addInitListener(ActionListener l) { initItem.addActionListener(l); }
    public void addPullListener(ActionListener l) { pullItem.addActionListener(l); }
    public void addPushListener(ActionListener l) { pushItem.addActionListener(l); }
    public void addFetchListener(ActionListener l) { fetchItem.addActionListener(l); }
    public void addCommitListener(ActionListener l) { commitItem.addActionListener(l); }
    public void addRefreshListener(ActionListener l) { refreshItem.addActionListener(l); }
    public void addStashListener(ActionListener l) { stashItem.addActionListener(l); }
    public void addCreateDiffListener(ActionListener l) { createDiffItem.addActionListener(l); }
    public void addApplyDiffListener(ActionListener l)  { applyDiffItem.addActionListener(l); }
    public void addCredentialsListener(ActionListener l) { credentialsItem.addActionListener(l); }
    public void addSettingsListener(ActionListener l) { settingsItem.addActionListener(l); }
}
