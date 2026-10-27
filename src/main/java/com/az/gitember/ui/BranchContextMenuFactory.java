package com.az.gitember.ui;

import com.az.gitember.service.I18n;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.data.Submodule;
import com.az.gitember.data.WorktreeInfo;
import com.az.gitember.dialog.WorktreeCreateDialog;
import com.az.gitember.dialog.WorktreesDialog;
import com.az.gitember.handler.*;
import com.az.gitember.service.Context;
import com.az.gitember.ui.mainframe.PullHandler;
import com.az.gitember.ui.mainframe.PushHandler;
import com.az.gitember.ui.maintree.MainTreePanel;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates context menus for branch/tag nodes in the tree,
 * mirroring the original gitember MainTreeContextMenuFactory.
 */
public class BranchContextMenuFactory {

    private static final Logger log = Logger.getLogger(BranchContextMenuFactory.class.getName());

    
    private final Frame parent;
    private final StatusBar statusBar;

    /** Called with the worktree path when the user chooses "Open". */
    private Consumer<String> worktreeOpenAction;
    /** Called after a worktree add/remove to refresh the tree. */
    private Runnable worktreeRefreshAction;

    public BranchContextMenuFactory(Frame parent) {
        this.parent = parent;
        this.statusBar = MainFrame.getInstance().getStatusBar();
    }

    public void setWorktreeOpenAction(Consumer<String> action)  { this.worktreeOpenAction   = action; }
    public void setWorktreeRefreshAction(Runnable action)       { this.worktreeRefreshAction = action; }

    public JPopupMenu createBranchContextMenu(ScmBranch sourceScmBranch) {
        JPopupMenu menu = new JPopupMenu();

        String name = sourceScmBranch.getShortName();
        String fullName = name;
        boolean disablePull = sourceScmBranch.getRemoteMergeName() == null
                && ScmBranch.BranchType.LOCAL.equals(sourceScmBranch.getBranchType());
        if (sourceScmBranch.getRemoteMergeName() != null) {
            fullName = name + " (" + sourceScmBranch.getRemoteMergeName() + ")";
        }

        // Checkout
        JMenuItem checkoutItem = new JMenuItem(I18n.t("branchContextMenuFactory.checkout"));
        checkoutItem.addActionListener(e ->
                new CheckoutBranchHandler(parent,  sourceScmBranch).execute());
        menu.add(checkoutItem);

        // Create branch
        boolean isRemote = ScmBranch.BranchType.REMOTE.equals(sourceScmBranch.getBranchType());
        JMenuItem createBranchItem = new JMenuItem(isRemote ? I18n.t("branchContextMenuFactory.createLocalBranch") : I18n.t("branchContextMenuFactory.createBranch"));
        createBranchItem.addActionListener(e ->
                CreateBranchHandler.showAndExecute(parent,  sourceScmBranch.getFullName(), isRemote));
        menu.add(createBranchItem);

        // Merge and Rebase - only if not current branch and not a tag
        ScmBranch workingBranch = Context.getWorkingBranch();
        boolean isCurrentBranch = workingBranch != null
                && sourceScmBranch.getShortName().equals(workingBranch.getShortName());
        boolean isTag = ScmBranch.BranchType.TAG == sourceScmBranch.getBranchType();

        if (!isCurrentBranch && !isTag) {
            String workingName = workingBranch != null ? workingBranch.getShortName() : "current";

            JMenuItem mergeItem = new JMenuItem(I18n.t("branchContextMenuFactory.merge", name, workingName));
            mergeItem.addActionListener(e ->
                    MergeBranchHandler.showAndExecute(parent,  sourceScmBranch));
            menu.add(mergeItem);

            JMenuItem rebaseItem = new JMenuItem(I18n.t("branchContextMenuFactory.rebase", name, workingName));
            rebaseItem.addActionListener(e ->
                    RebaseBranchHandler.showAndExecute(parent,  sourceScmBranch.getFullName(), name));
            menu.add(rebaseItem);
        }

        // Pull / Push - only for local branches
        if (sourceScmBranch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            menu.addSeparator();

            JMenuItem pullItem = new JMenuItem(I18n.t("pull", (disablePull ? "" : fullName)));
            pullItem.setEnabled(!disablePull);
            pullItem.addActionListener(e ->
                    new PullHandler(parent,  sourceScmBranch).execute());
            menu.add(pullItem);

            JMenu pushMenu = new JMenu(I18n.t("push"));

            String pushLabel = name.equals(fullName) ? I18n.t("branchContextMenuFactory.push", name) : I18n.t("branchContextMenuFactory.push2", fullName);
            JMenuItem pushItem = new JMenuItem(pushLabel);
            pushItem.addActionListener(e ->new PushHandler(parent,  sourceScmBranch).execute() );

            String pushForceLabel = name.equals(fullName) ? I18n.t("forcePush", name) : I18n.t("branchContextMenuFactory.forcePush", fullName);
            JMenuItem pushForceItem = new JMenuItem(pushForceLabel);
            pushForceItem.addActionListener(e ->new PushHandler(parent,  sourceScmBranch, true).execute() );

            pushMenu.add(pushItem);
            pushMenu.add(pushForceItem);

            menu.add(pushMenu);

        }

        // Push / Delete - tag-specific actions
        if (isTag) {
            menu.addSeparator();

            JMenuItem pushTagItem = new JMenuItem(I18n.t("pushTagToRemote"));
            pushTagItem.addActionListener(e ->
                    new PushTagHandler(parent,  sourceScmBranch).execute());
            menu.add(pushTagItem);
        }

        // Create work tree
        if (sourceScmBranch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            menu.addSeparator();
            JMenuItem createWorkTree = new JMenuItem(I18n.t("createWorktree"));
            menu.add(createWorkTree);

            if (isCurrentBranch) {
                createWorkTree.setEnabled(false);
            }

            createWorkTree.addActionListener(e -> {
                try {
                    List<WorktreeInfo> worktrees = Context.getGitRepoService().listWorktrees();
                    WorktreeCreateDialog dlg = new WorktreeCreateDialog(parent, worktrees, name);
                    dlg.setVisible(true);
                    if (dlg.isConfirmed()) {
                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                Context.getGitRepoService().addWorktree(dlg.getPath(), dlg.getBranch(), dlg.isNewBranch());
                                return null;
                            }
                            @Override
                            protected void done() {
                                try {
                                    get();
                                    MainFrame.getInstance().getTreePanel().refreshWorktrees();
                                } catch (Exception ex) {
                                    log.log(Level.WARNING, "Add worktree failed", ex);
                                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                                    JOptionPane.showMessageDialog(parent,
                                            I18n.t("cannotAddWorktree", cause.getMessage()),
                                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        worker.execute();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });


        }

        //Rename
        if (sourceScmBranch.getBranchType() == ScmBranch.BranchType.LOCAL) {
            JMenuItem renameBranchItem = new JMenuItem(MessageFormat.format(I18n.t("renameBranch"), name));
            renameBranchItem.addActionListener(e ->
                    RenameBranchHandler.showAndExecute(parent, name, false));
            menu.add(renameBranchItem);
        } else {
            JMenuItem renameBranchItem = new JMenuItem(
                    MessageFormat.format(I18n.t("renameRemoteBranch"),  sourceScmBranch.getFullName() )
            );
            renameBranchItem.addActionListener(e ->
                    RenameBranchHandler.showAndExecute(parent, sourceScmBranch.getFullName(), true));
            menu.add(renameBranchItem);
        }


        // Delete
        menu.addSeparator();
        if (isTag) {
            JMenuItem deleteTagItem = new JMenuItem(I18n.t("branchContextMenuFactory.deleteTag", name));
            deleteTagItem.addActionListener(e ->
                    DeleteTagHandler.showAndExecute(parent, statusBar, sourceScmBranch));
            menu.add(deleteTagItem);
        } else {
            JMenuItem deleteItem = new JMenuItem(I18n.t("branchContextMenuFactory.delete", name));
            deleteItem.addActionListener(e ->
                    DeleteBranchHandler.showAndExecute(parent, statusBar, sourceScmBranch));
            deleteItem.setEnabled(!sourceScmBranch.isHead());
            menu.add(deleteItem);
        }

        // Diff with submenu
        List<ScmBranch> localBranches = Context.getLocalBranches();
        List<ScmBranch> remoteBranches = Context.getRemoteBranches();
        int totalBranches = (localBranches != null ? localBranches.size() : 0)
                + (remoteBranches != null ? remoteBranches.size() : 0);

        if (totalBranches > 1) {
            menu.addSeparator();
            JMenu diffMenu = new JMenu(I18n.t("diffWith"));
            fillDiffSubmenu(diffMenu, localBranches, sourceScmBranch.getFullName());
            fillDiffSubmenu(diffMenu, remoteBranches, sourceScmBranch.getFullName());
            menu.add(diffMenu);
        }

        return menu;
    }

    private void fillDiffSubmenu(JMenu diffMenu, List<ScmBranch> branches, String currentBranchFullName) {
        if (branches != null) {
            for (ScmBranch br : branches) {
                if (br.getFullName().equals(currentBranchFullName)) continue;
                JMenuItem item = new JMenuItem(br.getFullName());
                item.addActionListener(e -> {
                    statusBar.setStatus(I18n.t("computingDiff", currentBranchFullName, br.getFullName()));
                    BranchDiffWindow window = new BranchDiffWindow(parent, currentBranchFullName, br.getFullName());
                    window.setVisible(true);
                });
                diffMenu.add(item);
            }
        }

    }

    /**
     * Context menu for an individual stash entry.
     */
    public JPopupMenu createStashContextMenu(ScmRevisionInformation stash) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem applyItem = new JMenuItem(I18n.t("applyStash"));
        applyItem.addActionListener(e -> doApplyStash(stash));
        menu.add(applyItem);

        JMenuItem editItem = new JMenuItem(I18n.t("branchContextMenuFactory.editMessage"));
        editItem.addActionListener(e -> doEditStashMessage(stash));
        menu.add(editItem);

        menu.addSeparator();

        JMenuItem dropItem = new JMenuItem(I18n.t("dropStash"));
        dropItem.addActionListener(e -> doDropStash(stash));
        menu.add(dropItem);

        return menu;
    }

    private void doEditStashMessage(ScmRevisionInformation stash) {
        String current = stash.getShortMessage() != null ? stash.getShortMessage() : "";
        String newMessage = (String) JOptionPane.showInputDialog(
                parent, I18n.t("stashMessage"), I18n.t("editStashMessage"),
                JOptionPane.PLAIN_MESSAGE, null, null, current);
        if (newMessage == null || newMessage.equals(current)) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Context.getGitRepoService().renameStash(stash.getStashIndex(), newMessage);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    stash.setShortMessage(newMessage);
                    Context.updateStash();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Edit stash message failed", ex);
                    JOptionPane.showMessageDialog(parent,
                            I18n.t("cannotEditStashMessage", ex.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doApplyStash(ScmRevisionInformation stash) {
        statusBar.setStatus(I18n.t("applyingStash"));
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
                    statusBar.setStatus(I18n.t("stashApplied"));
                    Context.updateStatus(null, true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Apply stash failed", ex);
                    statusBar.setStatus(I18n.t("applyStashFailed", ex.getMessage()));
                    JOptionPane.showMessageDialog(parent,
                            I18n.t("cannotApplyStash", ex.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doDropStash(ScmRevisionInformation stash) {
        String label = stash.getShortMessage() != null ? stash.getShortMessage() : "stash@{" + stash.getStashIndex() + "}";
        int choice = JOptionPane.showConfirmDialog(parent,
                I18n.t("dropStashCannotUndone", label),
                I18n.t("dropStash"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        statusBar.setStatus(I18n.t("droppingStash"));
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
                    statusBar.setStatus(I18n.t("stashDropped"));
                    Context.updateStash();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Drop stash failed", ex);
                    statusBar.setStatus(I18n.t("dropStashFailed", ex.getMessage()));
                    JOptionPane.showMessageDialog(parent,
                            I18n.t("cannotDropStash", ex.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Context menu for a worktree node.
     * The main worktree only has "Open"; linked worktrees also have Remove actions.
     */
    public JPopupMenu createWorktreeContextMenu(WorktreeInfo wt) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem openItem = new JMenuItem(I18n.t("open"));
        openItem.addActionListener(e -> {
            if (worktreeOpenAction != null) worktreeOpenAction.accept(wt.getPath());
        });
        menu.add(openItem);

        if (!wt.isMain()) {
            menu.addSeparator();

            JMenuItem removeItem = new JMenuItem(I18n.t("branchContextMenuFactory.remove"));
            removeItem.addActionListener(e -> doRemoveWorktree(wt, false));
            menu.add(removeItem);

            JMenuItem forceRemoveItem = new JMenuItem(I18n.t("branchContextMenuFactory.removeForce"));
            forceRemoveItem.addActionListener(e -> doRemoveWorktree(wt, true));
            menu.add(forceRemoveItem);
        }

        return menu;
    }

    private void doRemoveWorktree(WorktreeInfo wt, boolean force) {
        String pathLabel = wt.getPath();
        String msg = force
                ? I18n.t("forceRemoveWorktreeAtUncommitted", pathLabel)
                : I18n.t("removeWorktreeAt", pathLabel);
        int choice = JOptionPane.showConfirmDialog(parent, msg,
                I18n.t("removeWorktree"), JOptionPane.OK_CANCEL_OPTION,
                force ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        statusBar.setStatus(I18n.t("removingWorktree"));
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Context.getGitRepoService().removeWorktree(wt.getPath(), force);
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus(I18n.t("worktreeRemoved"));
                    if (worktreeRefreshAction != null) worktreeRefreshAction.run();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.log(Level.WARNING, "Remove worktree failed", cause);
                    statusBar.setStatus(I18n.t("removeWorktreeFailed", cause.getMessage()));
                    JOptionPane.showMessageDialog(parent,
                            I18n.t("cannotRemoveWorktree", cause.getMessage()),
                            I18n.t("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Context menu for the "Tags" category header node.
     */
    public JPopupMenu createTagsCategoryMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem createTagItem = new JMenuItem(I18n.t("branchContextMenuFactory.createTag"));
        createTagItem.addActionListener(e ->
                CreateTagHandler.showAndExecute(parent, statusBar));
        menu.add(createTagItem);

        return menu;
    }

    public JPopupMenu createSubmoduleContextMenu(Submodule submodule) {
        return SubmodulePanel.createSubmoduleMenu(parent, submodule);
    }

    public JPopupMenu createSubmodulesCategoryMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem addItem = new JMenuItem(I18n.t("branchContextMenuFactory.addSubmodule"));
        addItem.addActionListener(e -> AddSubmoduleHandler.showAndExecute(parent));
        menu.add(addItem);

        JMenuItem initItem = new JMenuItem(I18n.t("initializeSubmodules"));
        initItem.addActionListener(e -> new InitSubmodulesHandler(parent).execute());
        menu.add(initItem);

        JMenuItem updateItem = new JMenuItem(I18n.t("branchContextMenuFactory.updateSubmodules"));
        updateItem.addActionListener(e -> new UpdateSubmodulesHandler(parent).execute());
        menu.add(updateItem);

        JMenuItem recursiveItem = new JMenuItem(I18n.t("recursiveUpdate"));
        recursiveItem.addActionListener(e -> new UpdateSubmodulesHandler(parent, true).execute());
        menu.add(recursiveItem);

        return menu;
    }
}
