package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.service.GitemberUtil;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.function.Consumer;

public class MainToolBar extends JToolBar {

    private final JButton openBtn;
    private final JButton cloneBtn;
    private final JButton pullBtn;
    private final JButton pushBtn;
    private final JButton fetchBtn;
    private final JButton commitBtn;
    private final JComboBox<Project> projectCombo;
    private final JLabel branchLabel;

    private Consumer<Project> projectSelectionHandler;
    private boolean suppressComboEvents = false;

    public MainToolBar() {
        setFloatable(false);

        openBtn = createButton("Open", "Open repository", FontAwesomeSolid.FOLDER_OPEN);
        cloneBtn = createButton("Clone", "Clone repository" , FontAwesomeSolid.CLONE);
        pullBtn = createButton("Pull", "Pull",  FontAwesomeSolid.ARROW_DOWN);
        pushBtn = createButton("Push", "Push", FontAwesomeSolid.ARROW_UP);
        fetchBtn = createButton("Fetch", "Fetch", FontAwesomeSolid.SYNC_ALT);
        commitBtn = createButton("Commit", "Fetch", FontAwesomeSolid.CHECK);

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
        add(projectCombo);
        addSeparator();

        add(pullBtn);
        add(pushBtn);
        add(fetchBtn);
        addSeparator();
        add(commitBtn);

        addSeparator();
        branchLabel = new JLabel("");
        branchLabel.setFont(branchLabel.getFont().deriveFont(Font.BOLD));
        add(branchLabel);

        setRepoActionsEnabled(false);
    }

    private JButton createButton(String text, String tooltip, org.kordamp.ikonli.Ikon ikon) {
        JButton btn = new JButton();
        btn.setIcon(FontIcon.of(ikon, 16));
        btn.setText(text);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        return btn;
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
        commitBtn.setEnabled(enabled);
    }

    public void setBranchName(String name) {
        branchLabel.setText(name != null ? name : "");
    }

    // Working copy toolbar components merged into main toolbar
    private java.util.List<Component> mergedComponents = new java.util.ArrayList<>();

    public void mergeWorkingCopyToolbar(WorkingCopyPanel wcp) {
        if (!mergedComponents.isEmpty()) return; // already merged

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

        JSeparator sep3 = new JToolBar.Separator();
        mergedComponents.add(sep3);
        add(sep3);

        JLabel filterLabel = new JLabel("Filter:");
        mergedComponents.add(filterLabel);
        add(filterLabel);

        mergedComponents.add(wcp.getSearchField());
        add(wcp.getSearchField());

        revalidate();
        repaint();
    }

    public void unmergeWorkingCopyToolbar() {
        if (mergedComponents.isEmpty()) return;
        for (Component c : mergedComponents) {
            remove(c);
        }
        mergedComponents.clear();
        revalidate();
        repaint();
    }

    public void addOpenListener(ActionListener l) { openBtn.addActionListener(l); }
    public void addCloneListener(ActionListener l) { cloneBtn.addActionListener(l); }
    public void addPullListener(ActionListener l) { pullBtn.addActionListener(l); }
    public void addPushListener(ActionListener l) { pushBtn.addActionListener(l); }
    public void addFetchListener(ActionListener l) { fetchBtn.addActionListener(l); }
    public void addCommitListener(ActionListener l) { commitBtn.addActionListener(l); }
}
