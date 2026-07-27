package com.az.gitember.ui.workspace;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.StatusBar;
import com.az.gitember.ui.WorkingCopyContextMenu;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkingCopyMouseAdapter extends MouseAdapter {

    private static final Logger log = Logger.getLogger(WorkingCopyMouseAdapter.class.getName());
    /**
     * Pixel width of the leading checkbox, used to hit-test stage/unstage clicks.
     */
    private final int checkboxWidth = new JCheckBox().getPreferredSize().width;

    private final WorkspaceDashboardPanel workspaceDashboardPanel;
    private final JTree workingCopyTree;
    private final DefaultTreeModel workingCopyModel;
    private final DefaultMutableTreeNode workingCopyRoot;
    private final StatusBar statusBar;

    public WorkingCopyMouseAdapter(WorkspaceDashboardPanel workspaceDashboardPanel,  StatusBar statusBar) {
        this.workspaceDashboardPanel = workspaceDashboardPanel;
        this.workingCopyTree = workspaceDashboardPanel.getWorkingCopyTree();
        this.workingCopyModel = workspaceDashboardPanel.getWorkingCopyModel();
        this.workingCopyRoot = workspaceDashboardPanel.getWorkingCopyRoot();
        this.statusBar = statusBar;
    }

    private void handlePopup(MouseEvent e) {
        if (e.isPopupTrigger())  {
            TreePath path = workingCopyTree.getPathForLocation(e.getX(), e.getY());
            if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) return;
            if (!(node.getUserObject() instanceof FileNode fileNode)) return;

            com.az.gitember.data.Project project = fileNode.getProject();
            DefaultMutableTreeNode projectNode = projectNodeOf(node);
            try {
                Context.initRepoOnly(project.getProjectHomeFolder());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            WorkingCopyContextMenu ctxMenu = new WorkingCopyContextMenu(
                    workspaceDashboardPanel,
                    statusBar,
                    action -> {
                        try (com.az.gitember.service.GitRepoService svc =
                                     com.az.gitember.service.GitRepoService.of(project)) {
                            action.execute(svc);
                        }
                    },
                    project::getProjectHomeFolder,
                    () -> {
                        if (projectNode != null) {
                            workspaceDashboardPanel.loadProjectWorkingCopy(project, projectNode);
                        }
                        workspaceDashboardPanel.updateButtonStates();
                    });
            ctxMenu.show(java.util.List.of(fileNode.getItem()), workingCopyTree, e.getX(), e.getY());
        }

    }

    @Override
    public void mousePressed(MouseEvent e) { handlePopup(e); }

    @Override
    public void mouseReleased(MouseEvent e) { handlePopup(e); }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger() || !SwingUtilities.isLeftMouseButton(e)) return;
        TreePath path = workingCopyTree.getPathForLocation(e.getX(), e.getY());
        if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) return;
        if (!(node.getUserObject() instanceof FileNode fileNode)) return;

        Rectangle bounds = workingCopyTree.getPathBounds(path);
        if (bounds != null && e.getX() <= bounds.x + checkboxWidth) {
            toggleStage(node, fileNode);
        }
    }

    /**
     * Toggles the staged state of a single file using its <em>own</em> project's repository
     * (a throwaway {@link GitRepoService}), then reloads that project's subtree.
     */
    private void toggleStage(DefaultMutableTreeNode node, FileNode fileNode) {
        Project project = fileNode.getProject();
        ScmItem item = fileNode.getItem();
        boolean staged = ScmItem.isStaged(fileNode.status());
        DefaultMutableTreeNode projectNode = projectNodeOf(node);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (GitRepoService svc = GitRepoService.of(project)) {
                    if (staged) {
                        svc.unstageItem(item);
                    } else {
                        svc.stageItem(item);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Stage/unstage failed for " + item.getShortName(), ex);
                }
                if (projectNode != null) {
                    workspaceDashboardPanel.loadProjectWorkingCopy(project, projectNode);
                }
                workspaceDashboardPanel.updateButtonStates();
            }
        }.execute();
    }

    /**
     * Walks up from a file node to its owning project node (direct child of the hidden root).
     */
    private DefaultMutableTreeNode projectNodeOf(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode current = node;
        while (current.getParent() != null && current.getParent() != workingCopyRoot) {
            current = (DefaultMutableTreeNode) current.getParent();
        }
        return current.getParent() == workingCopyRoot ? current : null;
    }

}
