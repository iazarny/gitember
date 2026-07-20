package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.misc.Util;
import com.az.gitember.ui.workspace.WorkspaceDashboardPanel;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.function.Consumer;

public class MainToolBar extends JToolBar {

    private final JButton openBtn;
    private final JButton cloneBtn;
    private final JButton createBranchBtn;
    private final JButton pullBtn;
    private final JButton pushBtn;
    private final JButton fetchBtn;
    private final JButton commitBtn;
    private final JComboBox<Project> projectCombo;
    private final JLabel branchLabel;

    private Consumer<Project> projectSelectionHandler;
    private boolean suppressComboEvents = false;

    private final java.util.List<Component> mergedWorkspaceComponents = new java.util.ArrayList<>();
    private final java.util.List<Component> mergedHistoryComponents = new java.util.ArrayList<>();
    private final java.util.List<Component> mergedComponents = new java.util.ArrayList<>();

    public MainToolBar() {
        setFloatable(false);

        createBranchBtn = Util.createButton("Branch", "Create branch", FontAwesomeSolid.CODE_BRANCH);

        openBtn = Util.createButton("Open", "Open repository", FontAwesomeSolid.FOLDER_OPEN);
        cloneBtn = Util.createButton("Clone", "Clone repository" , FontAwesomeSolid.CLONE);
        pullBtn = Util.createButton("Pull", "Pull",  FontAwesomeSolid.REPLY, -45);
        pushBtn = Util.createButton("Push", "Push", FontAwesomeSolid.REPLY, 135);

        fetchBtn = Util.createButton("Fetch", "Fetch changes from remote repository", FontAwesomeSolid.REPLY_ALL, -45);
        commitBtn = Util.createButton("Commit", "Commit", FontAwesomeSolid.CHECK);

        projectCombo = new JComboBox<>();
        projectCombo.setVisible(false);

        projectCombo.setMaximumSize(new Dimension(250, 28));
        projectCombo.setPreferredSize(new Dimension(200, 28));
        projectCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Project p) {
                    setText(GitemberUtil.getFolderName(p.getProjectHomeFolder()));
                    setToolTipText(p.getProjectHomeFolder());
                }
                return this;
            }
        });
        projectCombo.addActionListener(e -> {
            if (!suppressComboEvents && projectSelectionHandler != null) {
                Project selected = (Project) projectCombo.getSelectedItem();
                if (selected != null) {
                    projectSelectionHandler.accept(selected);
                }
            }
        });

        add(openBtn);
        add(cloneBtn);
        addSeparator();
        //add(projectCombo);
        //addSeparator();

        add(createBranchBtn);
        add(pullBtn);
        add(pushBtn);
        add(fetchBtn);
        addSeparator();
        add(commitBtn);

        //addSeparator();
        branchLabel = new JLabel("");
        branchLabel.setFont(branchLabel.getFont().deriveFont(Font.BOLD));
        //add(branchLabel);

        setRepoActionsEnabled(false);
    }



    public void refreshProjects(Set<Project> projects, Project selected) {
        suppressComboEvents = true;
        projectCombo.removeAllItems();
        if (projects != null) {
            for (Project p : projects) {
                projectCombo.addItem(p);
            }
        }
        if (selected != null) {
            projectCombo.setSelectedItem(selected);
        }
        //projectCombo.setVisible(projects != null && projects.size() > 1);
        suppressComboEvents = false;
    }

    public void setProjectSelectionHandler(Consumer<Project> handler) {
        this.projectSelectionHandler = handler;
    }

    public void setRepoActionsEnabled(boolean enabled) {
        pullBtn.setEnabled(enabled);
        pushBtn.setEnabled(enabled);
        fetchBtn.setEnabled(enabled);
        if (!enabled) {
            commitBtn.setEnabled(false);
        }
    }

    public void setBranchEnabled(boolean enabled) {
        createBranchBtn.setEnabled(enabled);
    }

    public void setCommitEnabled(boolean enabled) {
        commitBtn.setEnabled(enabled);
    }

    public void setPushEnabled(boolean enabled) {
        pushBtn.setEnabled(enabled);
    }

    public void setPullEnabled(boolean enabled) {
        pullBtn.setEnabled(enabled);
    }

    public void setFetchEnabled(boolean enabled) {
        fetchBtn.setEnabled(enabled);
    }

    public void setBranchName(String name) {
        branchLabel.setText(name != null ? name : "");
    }


    public void mergeWorkSpaceToolbar(WorkspaceDashboardPanel wdp) {
        if (mergedWorkspaceComponents.isEmpty()) { //do not merge twice
            JSeparator sep = new JToolBar.Separator();
            mergedWorkspaceComponents.add(sep);
            add(sep);

            mergedWorkspaceComponents.add(wdp.getStageAllBtn());
            add(wdp.getStageAllBtn());

            mergedWorkspaceComponents.add(wdp.getUnstageAllBtn());
            add(wdp.getUnstageAllBtn());

            JSeparator sep2 = new JToolBar.Separator();
            mergedWorkspaceComponents.add(sep2);
            add(sep2);

            mergedWorkspaceComponents.add(wdp.getRefreshBtn());
            add(wdp.getRefreshBtn());

            Component glue = Box.createHorizontalGlue();
            mergedWorkspaceComponents.add(glue);
            add(glue);


            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setBorder(new EmptyBorder(0, 0, 0, 4));
            mergedWorkspaceComponents.add(searchLabel);
            add(searchLabel);

            mergedWorkspaceComponents.add(wdp.getSearchField());
            add(wdp.getSearchField());

            revalidate();
            repaint();
        }
    }



    public void mergeWorkingCopyToolbar(WorkingCopyPanel wcp) {
        if (mergedComponents.isEmpty()) { // do not merge twice
            JSeparator sep = new JToolBar.Separator();
            mergedComponents.add(sep);
            add(sep);

            mergedComponents.add(wcp.getStageAllBtn());
            add(wcp.getStageAllBtn());

            mergedComponents.add(wcp.getUnstageAllBtn());
            add(wcp.getUnstageAllBtn());

            JSeparator sep2 = new JToolBar.Separator();
            mergedComponents.add(sep2);
            add(sep2);

            mergedComponents.add(wcp.getRefreshBtn());
            add(wcp.getRefreshBtn());

            Component glue = Box.createHorizontalGlue();
            mergedComponents.add(glue);
            add(glue);

            JLabel filterLabel = new JLabel("Search:");
            filterLabel.setBorder(new EmptyBorder(0, 0, 0, 4));
            mergedComponents.add(filterLabel);
            add(filterLabel);

            mergedComponents.add(wcp.getSearchField());
            add(wcp.getSearchField());

            revalidate();
            repaint();
        }
    }




    public void mergeHistoryToolbar(HistoryPanel hp) {
        if (mergedHistoryComponents.isEmpty()) { //do not merge twice
            Component glue = Box.createHorizontalGlue();
            mergedHistoryComponents.add(glue);
            add(glue);

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setBorder(new EmptyBorder(0, 0, 0, 4));
            mergedHistoryComponents.add(searchLabel);
            add(searchLabel);

            mergedHistoryComponents.add(hp.getSearchField());
            add(hp.getSearchField());

            revalidate();
            repaint();
        }
    }

    public void unmergeWorkSpaceToolbar() {
        if (!mergedWorkspaceComponents.isEmpty())  {
            for (Component c : mergedWorkspaceComponents) {
                remove(c);
            }
            mergedWorkspaceComponents.clear();
            revalidate();
            repaint();
        }
    }

    public void unmergeWorkingCopyToolbar() {
        if (!mergedComponents.isEmpty())  {
            for (Component c : mergedComponents) {
                remove(c);
            }
            mergedComponents.clear();
            revalidate();
            repaint();
        }
    }

    public void unmergeHistoryToolbar() {
        if (!mergedHistoryComponents.isEmpty()) {
            for (Component c : mergedHistoryComponents) {
                remove(c);
            }
            mergedHistoryComponents.clear();
            revalidate();
            repaint();
        }
    }



    // Pull-request file filter component merged into main toolbar
    private final java.util.List<Component> mergedPrComponents = new java.util.ArrayList<>();

    public void mergePullRequestToolbar(PullRequestPanel prp) {
        if (!mergedPrComponents.isEmpty()) return;

        Component glue = Box.createHorizontalGlue();
        mergedPrComponents.add(glue);
        add(glue);

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setBorder(new EmptyBorder(0, 0, 0, 4));
        mergedPrComponents.add(filterLabel);
        add(filterLabel);

        mergedPrComponents.add(prp.getSearchField());
        add(prp.getSearchField());

        revalidate();
        repaint();
    }

    public void unmergePullRequestToolbar() {
        if (mergedPrComponents.isEmpty()) return;
        for (Component c : mergedPrComponents) {
            remove(c);
        }
        mergedPrComponents.clear();
        revalidate();
        repaint();
    }

    /**
     * Updates Pull/Push button text and tooltips to reflect how many commits
     * the current branch is behind / ahead of its remote tracking branch.
     * Call this whenever the working branch changes.
     */
    public void updateSyncCounts(ScmBranch branch) {
        if (branch == null) {
            resetSyncButtons();
            return;
        }

        int behind = branch.getBehindCount();
        int ahead  = branch.getAheadCount();

        // Pull button: shows "behind" count — N commits exist on remote that we don't have
        if (behind > 0) {
            pullBtn.setText("<html>Pull <small>(" + behind + ")</small></html>");
            pullBtn.setToolTipText(branch.getScmBranchPullTooltip().orElse("Pull"));
        } else {
            pullBtn.setText("Pull");
            pullBtn.setToolTipText("Pull");
        }

        // Push button: shows "ahead" count — N local commits not yet on remote
        if (ahead > 0) {
            pushBtn.setText("<html>Push <small>(" + ahead + ")</small></html>");
            pushBtn.setToolTipText(branch.getScmBranchPushTooltip().orElse("Push"));
        } else {
            pushBtn.setText("Push");
            pushBtn.setToolTipText("Push");
        }
    }

    private void resetSyncButtons() {
        pullBtn.setText("Pull");
        pullBtn.setToolTipText("Pull");
        pushBtn.setText("Push");
        pushBtn.setToolTipText("Push");
    }

    public void addBranchListener(ActionListener l) { createBranchBtn.addActionListener(l); }
    public void addOpenListener(ActionListener l) { openBtn.addActionListener(l); }
    public void addCloneListener(ActionListener l) { cloneBtn.addActionListener(l); }
    public void addPullListener(ActionListener l) { pullBtn.addActionListener(l); }
    public void addPushListener(ActionListener l) { pushBtn.addActionListener(l); }
    public void addFetchListener(ActionListener l) { fetchBtn.addActionListener(l); }
    public void addCommitListener(ActionListener l) {
        commitBtn.addActionListener(l);
    }


}
