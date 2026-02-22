package com.az.gitember.ui;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.handler.*;
import com.az.gitember.service.Context;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates context menus for branch/tag nodes in the tree,
 * mirroring the original gitember MainTreeContextMenuFactory.
 */
public class BranchContextMenuFactory {

    private static final Logger log = Logger.getLogger(BranchContextMenuFactory.class.getName());

    private final Component parent;
    private final StatusBar statusBar;

    public BranchContextMenuFactory(Component parent, StatusBar statusBar) {
        this.parent = parent;
        this.statusBar = statusBar;
    }

    public JPopupMenu createBranchContextMenu(ScmBranch branch) {
        JPopupMenu menu = new JPopupMenu();

        String name = branch.getShortName();
        String fullName = name;
        boolean disablePull = branch.getRemoteMergeName() == null
                && ScmBranch.BranchType.LOCAL.equals(branch.getBranchType());
        if (branch.getRemoteMergeName() != null) {
            fullName = name + " (" + branch.getRemoteMergeName() + ")";
        }

        // Checkout
        JMenuItem checkoutItem = new JMenuItem("Checkout");
        checkoutItem.addActionListener(e ->
                new CheckoutBranchHandler(parent, statusBar, branch).execute());
        menu.add(checkoutItem);

        // Create branch
        JMenuItem createBranchItem = new JMenuItem("Create branch ...");
        createBranchItem.addActionListener(e ->
                CreateBranchHandler.showAndExecute(parent, statusBar, branch.getFullName()));
        menu.add(createBranchItem);

        // Merge and Rebase - only if not current branch and not a tag
        ScmBranch workingBranch = Context.getWorkingBranch();
        boolean isCurrentBranch = workingBranch != null
                && branch.getShortName().equals(workingBranch.getShortName());
        boolean isTag = ScmBranch.BranchType.TAG == branch.getBranchType();

        if (!isCurrentBranch && !isTag) {
            String workingName = workingBranch != null ? workingBranch.getShortName() : "current";

            JMenuItem mergeItem = new JMenuItem("Merge " + name + " -> " + workingName + "...");
            mergeItem.addActionListener(e ->
                    MergeBranchHandler.showAndExecute(parent, statusBar, branch.getFullName(), name));
            menu.add(mergeItem);

            JMenuItem rebaseItem = new JMenuItem("Rebase " + name + " -> " + workingName + "...");
            rebaseItem.addActionListener(e ->
                    RebaseBranchHandler.showAndExecute(parent, statusBar, branch.getFullName(), name));
            menu.add(rebaseItem);
        }

        // Pull / Push - only for local branches
        if (branch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            menu.addSeparator();

            JMenuItem pullItem = new JMenuItem("Pull " + (disablePull ? "" : fullName));
            pullItem.setEnabled(!disablePull);
            pullItem.addActionListener(e ->
                    new BranchPullHandler(parent, statusBar, branch).execute());
            menu.add(pullItem);

            String pushLabel = name.equals(fullName) ? "Push " + name + "..." : "Push " + fullName;
            JMenuItem pushItem = new JMenuItem(pushLabel);
            pushItem.addActionListener(e ->
                    new BranchPushHandler(parent, statusBar, branch).execute());
            menu.add(pushItem);
        }

        // Delete
        menu.addSeparator();
        JMenuItem deleteItem = new JMenuItem("Delete " + name + "...");
        deleteItem.addActionListener(e ->
                DeleteBranchHandler.showAndExecute(parent, statusBar, branch));
        menu.add(deleteItem);

        // Diff with submenu
        List<ScmBranch> localBranches = Context.getLocalBranches();
        List<ScmBranch> remoteBranches = Context.getRemoteBranches();
        int totalBranches = (localBranches != null ? localBranches.size() : 0)
                + (remoteBranches != null ? remoteBranches.size() : 0);

        if (totalBranches > 1) {
            menu.addSeparator();
            JMenu diffMenu = new JMenu("Diff with");
            fillDiffSubmenu(diffMenu, localBranches, branch.getFullName());
            fillDiffSubmenu(diffMenu, remoteBranches, branch.getFullName());
            menu.add(diffMenu);
        }

        return menu;
    }

    private void fillDiffSubmenu(JMenu diffMenu, List<ScmBranch> branches, String currentBranchFullName) {
        if (branches == null) return;
        for (ScmBranch br : branches) {
            if (br.getFullName().equals(currentBranchFullName)) continue;
            JMenuItem item = new JMenuItem(br.getFullName());
            item.addActionListener(e -> {
                statusBar.setStatus("Computing diff: " + currentBranchFullName + " ↔ " + br.getFullName() + "…");
                BranchDiffWindow window = new BranchDiffWindow(parent, currentBranchFullName, br.getFullName());
                window.setVisible(true);
            });
            diffMenu.add(item);
        }
    }

    /**
     * Context menu for an individual stash entry.
     */
    public JPopupMenu createStashContextMenu(ScmRevisionInformation stash) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem applyItem = new JMenuItem("Apply Stash");
        applyItem.addActionListener(e -> doApplyStash(stash));
        menu.add(applyItem);

        JMenuItem dropItem = new JMenuItem("Drop Stash");
        dropItem.addActionListener(e -> doDropStash(stash));
        menu.add(dropItem);

        return menu;
    }

    private void doApplyStash(ScmRevisionInformation stash) {
        statusBar.setStatus("Applying stash...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().applyStash(stash.getRevisionFullName());
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Stash applied");
                    Context.updateStatus(null, true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Apply stash failed", ex);
                    statusBar.setStatus("Apply stash failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Cannot apply stash:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doDropStash(ScmRevisionInformation stash) {
        String label = stash.getShortMessage() != null ? stash.getShortMessage() : "stash@{" + stash.getStashIndex() + "}";
        int choice = JOptionPane.showConfirmDialog(parent,
                "Drop stash \"" + label + "\"?\nThis cannot be undone.",
                "Drop Stash", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        statusBar.setStatus("Dropping stash...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().deleteStash(stash.getStashIndex());
                return null;
            }

            @Override
            protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Stash dropped");
                    Context.updateStash();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Drop stash failed", ex);
                    statusBar.setStatus("Drop stash failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Cannot drop stash:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Context menu for the "Tags" category header node.
     */
    public JPopupMenu createTagsCategoryMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem createTagItem = new JMenuItem("Create tag ...");
        createTagItem.addActionListener(e -> {
            String tagName = JOptionPane.showInputDialog(parent,
                    "Tag name:", "Create Tag", JOptionPane.PLAIN_MESSAGE);
            if (tagName != null && !tagName.isBlank()) {
                statusBar.setStatus("Create tag: not yet implemented");
            }
        });
        menu.add(createTagItem);

        return menu;
    }
}
