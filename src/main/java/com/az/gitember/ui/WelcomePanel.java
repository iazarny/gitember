package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.ui.welcome.ProjectCellRenderer;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Welcome screen shown on startup with a list of recent projects and workspaces.
 * List elements are either {@link Project} or {@link Workspace}; the renderer and
 * mouse handlers route by type.
 */
public class WelcomePanel extends JPanel {

    private final DefaultListModel<Object> listModel;
    private final JList<Object> projectList;
    private final ProjectCellRenderer projectCellRenderer;
    /** Workspaces the user has expanded; absent means collapsed (the default). */
    private final Set<Workspace> expandedWorkspaces = new HashSet<>();
    private Collection<Project> lastProjects;
    private List<Workspace> lastWorkspaces;
    private Consumer<Project> onProjectSelected;
    private Consumer<Project> onProjectRemoved;
    private Consumer<Workspace> onWorkspaceSelected;
    private Consumer<Workspace> onWorkspaceRemoved;
    private Runnable onOpenRepo;
    private Runnable onCloneRepo;
    private Runnable onInitRepo;
    private Runnable onInitWorkspace;
    private final JPopupMenu contextMenu;
    private final JMenuItem openMenuItem;
    private final JMenuItem removeMenuItem;

    public WelcomePanel() {
        setLayout(new BorderLayout());

        contextMenu = new JPopupMenu();
        openMenuItem = new JMenuItem("Open");
        removeMenuItem = new JMenuItem("Remove from list");
        contextMenu.add(openMenuItem);
        contextMenu.addSeparator();
        contextMenu.add(removeMenuItem);

        // Header
        JLabel header = new JLabel("Gitember", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 28f));
        header.setBorder(BorderFactory.createEmptyBorder(40, 0, 10, 0));



        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));

        JButton openRepoBtn  = createWellcomeButton("Open Repository", FontAwesomeSolid.FOLDER_OPEN);
        JButton cloneRepoBtn = createWellcomeButton("Clone repository", FontAwesomeSolid.FOLDER);
        JButton initRepoBtn  = createWellcomeButton("Init repository", FontAwesomeSolid.FOLDER_PLUS);
        JButton initWorkpaceBtn  = createWellcomeButton("Init workspace", FontAwesomeSolid.LAYER_GROUP);


        openRepoBtn.addActionListener(e -> { if (onOpenRepo != null) onOpenRepo.run(); });
        cloneRepoBtn.addActionListener(e -> { if (onCloneRepo != null) onCloneRepo.run(); });
        initRepoBtn.addActionListener(e -> { if (onInitRepo != null) onInitRepo.run(); });
        initWorkpaceBtn.addActionListener(e -> {
            if (onInitWorkspace != null) onInitWorkspace.run(); });

        commandPanel.add(Box.createHorizontalGlue());
        commandPanel.add(openRepoBtn);
        commandPanel.add(Box.createHorizontalStrut(20));
        commandPanel.add(cloneRepoBtn);
        commandPanel.add(Box.createHorizontalStrut(20));
        commandPanel.add(initRepoBtn);
        commandPanel.add(Box.createHorizontalStrut(20));
        commandPanel.add(initWorkpaceBtn);
        commandPanel.add(Box.createHorizontalGlue());



        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.NORTH);


        // Project list
        listModel = new DefaultListModel<>();
        projectList = new JList<>(listModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectCellRenderer = new ProjectCellRenderer();
        projectList.setCellRenderer(projectCellRenderer);
        projectList.setFixedCellHeight(60);

        projectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && !SwingUtilities.isRightMouseButton(e)) {
                    int index = projectList.locationToIndex(e.getPoint());
                    Rectangle bounds = index >= 0 ? projectList.getCellBounds(index, index) : null;
                    if (bounds == null || !bounds.contains(e.getPoint())) return;

                    Object item = listModel.getElementAt(index);
                    if (item instanceof Workspace workspace
                            && e.getX() - bounds.x < ProjectCellRenderer.CHEVRON_AREA_WIDTH + 16) {
                        toggleWorkspace(workspace);
                        return;
                    }
                    selectItem(item);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowContextMenu(e);
            }

            private void maybeShowContextMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int index = projectList.locationToIndex(e.getPoint());
                if (index < 0 || !projectList.getCellBounds(index, index).contains(e.getPoint())) return;
                projectList.setSelectedIndex(index);
                Object item = listModel.getElementAt(index);
                // Replace listeners each time to avoid accumulation
                for (java.awt.event.ActionListener al : openMenuItem.getActionListeners())
                    openMenuItem.removeActionListener(al);
                for (java.awt.event.ActionListener al : removeMenuItem.getActionListeners())
                    removeMenuItem.removeActionListener(al);
                openMenuItem.addActionListener(ev -> selectItem(item));
                removeMenuItem.addActionListener(ev -> removeItem(item));
                contextMenu.show(projectList, e.getX(), e.getY());
            }
        });

        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Inner panel: buttons + list, sized to match buttons row width
        JPanel innerPanel = new JPanel(new BorderLayout(0, 10));
        innerPanel.setOpaque(false);
        innerPanel.add(commandPanel, BorderLayout.NORTH);
        innerPanel.add(scrollPane, BorderLayout.CENTER);

        // Center horizontally using BoxLayout wrapper
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        // Wrapper that centers innerPanel horizontally at the buttons' preferred width
        JPanel alignWrapper = new JPanel(new GridBagLayout());
        alignWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        // Set preferred width to match the 3 buttons + gaps: 3*220 + 2*20 = 700
        innerPanel.setPreferredSize(new Dimension(700, 0));
        innerPanel.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
        alignWrapper.add(innerPanel, gbc);

        centerPanel.add(alignWrapper);

        // Hint at bottom
        JLabel hint = new JLabel("Click a project to open, or use File menu to open or clone a repository",
                SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 12f));
        hint.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        //add(centerPanel, BorderLayout.SOUTH);
        add(hint, BorderLayout.SOUTH);
    }

    private JButton createWellcomeButton(String text, FontAwesomeSolid  icon) {
        JButton btn = new JButton();

        btn.setIcon(Util.themeAwareIcon(icon, 64));

        btn.setText(text);

        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);

        btn.setIconTextGap(8);
        btn.setFocusable(false);

        // 👇 limit max width
        Dimension size = new Dimension(220, 140);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);

        return btn;
    }

    /** Routes a single click / "Open" on a list element to the type-specific handler. */
    private void selectItem(Object item) {
        if (item instanceof Project project) {
            if (onProjectSelected != null) onProjectSelected.accept(project);
        } else if (item instanceof Workspace workspace) {
            if (onWorkspaceSelected != null) onWorkspaceSelected.accept(workspace);
        }
    }

    /** Routes "Remove from list" on a list element to the type-specific handler. */
    private void removeItem(Object item) {
        if (item instanceof Project project) {
            if (onProjectRemoved != null) onProjectRemoved.accept(project);
        } else if (item instanceof Workspace workspace) {
            if (onWorkspaceRemoved != null) onWorkspaceRemoved.accept(workspace);
        }
    }

    public void setOnProjectSelected(Consumer<Project> handler) {
        this.onProjectSelected = handler;
    }

    public void setOnProjectRemoved(Consumer<Project> handler) {
        this.onProjectRemoved = handler;
    }

    public void setOnWorkspaceSelected(Consumer<Workspace> handler) {
        this.onWorkspaceSelected = handler;
    }

    public void setOnWorkspaceRemoved(Consumer<Workspace> handler) {
        this.onWorkspaceRemoved = handler;
    }

    public void setOnOpenRepo(Runnable handler) {
        this.onOpenRepo = handler;
    }

    public void setOnCloneRepo(Runnable handler) {
        this.onCloneRepo = handler;
    }

    public void setOnInitRepo(Runnable handler) {
        this.onInitRepo = handler;
    }


    public void setOnInitWorkspace(Runnable handler) {
        this.onInitWorkspace = handler;
    }

    public void setProjects(Collection<Project> projects) {
        setItems(projects, null);
    }

    /**
     * Populates the list with git projects and workspaces intermixed, most recently opened
     * first. Workspaces have no path, so they render without one. Collapsed by default; a
     * workspace's projects only appear once the user expands it via its chevron.
     */
    public void setItems(Collection<Project> projects, List<Workspace> workspaces) {
        this.lastProjects = projects;
        this.lastWorkspaces = workspaces;
        rebuildListModel();
    }

    /** Toggles a workspace between expanded/collapsed and re-renders the list. */
    private void toggleWorkspace(Workspace workspace) {
        if (!expandedWorkspaces.remove(workspace)) {
            expandedWorkspaces.add(workspace);
        }
        rebuildListModel();
    }

    private void rebuildListModel() {
        listModel.clear();
        ArrayList<Object> items = new ArrayList<>();
        if (lastProjects != null) {
            items.addAll(lastProjects);
        }
        if (lastWorkspaces != null) {
            items.addAll(lastWorkspaces);
        }

        items.sort((a, b) -> {
            Date ta = openTimeOf(a);
            Date tb = openTimeOf(b);
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        List<Project> allWsProjects = lastWorkspaces == null ? List.of() : lastWorkspaces.stream()
                .flatMap(workspace -> workspace.getProjects().stream())
                .toList();

        items.removeAll(allWsProjects);
        projectCellRenderer.setNestedProjects(allWsProjects);
        projectCellRenderer.setExpandedWorkspaces(expandedWorkspaces);

        List<Object> items2 = new ArrayList<>();

        for (Object source : items) {
            items2.add(source);
            if (source instanceof Workspace ws && expandedWorkspaces.contains(ws)) {
                items2.addAll(ws.getProjects());
            }
        }
        items2.forEach(listModel::addElement);
    }

    private static Date openTimeOf(Object item) {
        if (item instanceof Project project) return project.getOpenTime();
        if (item instanceof Workspace workspace) return workspace.getOpenTime();
        return null;
    }


}
