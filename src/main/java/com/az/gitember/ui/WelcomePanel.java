package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.ActivityChartService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.service.WorkspaceChartService;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Welcome screen shown on startup with a list of recent projects and workspaces.
 * List elements are either {@link Project} or {@link Workspace}; the renderer and
 * mouse handlers route by type.
 */
public class WelcomePanel extends JPanel {

    private final DefaultListModel<Object> listModel;
    private final JList<Object> projectList;
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
        JButton initWorkpaceBtn  = createWellcomeButton("Init workspace", FontAwesomeSolid.FOLDER_PLUS);


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
        projectList.setCellRenderer(new ProjectCellRenderer());
        projectList.setFixedCellHeight(60);

        projectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && !SwingUtilities.isRightMouseButton(e)) {
                    int index = projectList.locationToIndex(e.getPoint());
                    if (index >= 0 && projectList.getCellBounds(index, index).contains(e.getPoint())) {
                        selectItem(listModel.getElementAt(index));
                    }
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
     * first. Workspaces have no path, so they render without one.
     */
    public void setItems(Collection<Project> projects, List<Workspace> workspaces) {
        listModel.clear();
        List<Object> items = new ArrayList<>();
        if (projects != null) items.addAll(projects);
        if (workspaces != null) items.addAll(workspaces);

        items.sort((a, b) -> {
            Date ta = openTimeOf(a);
            Date tb = openTimeOf(b);
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        items.forEach(listModel::addElement);
    }

    private static Date openTimeOf(Object item) {
        if (item instanceof Project project) return project.getOpenTime();
        if (item instanceof Workspace workspace) return workspace.getOpenTime();
        return null;
    }

    private static class ProjectCellRenderer extends JPanel implements ListCellRenderer<Object> {

        private final JLabel     nameLabel;
        private final JLabel     pathLabel;
        private final JLabel     dateLabel;
        private final ChartPanel chartPanel;

        ProjectCellRenderer() {
            setLayout(new BorderLayout(8, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            nameLabel = new JLabel();
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

            pathLabel = new JLabel();
            pathLabel.setFont(pathLabel.getFont().deriveFont(Font.PLAIN, 11f));
            pathLabel.setForeground(Color.GRAY);

            dateLabel = new JLabel();
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 11f));
            dateLabel.setForeground(Color.GRAY);
            dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            chartPanel = new ChartPanel();

            JPanel textPanel = new JPanel(new BorderLayout(0, 2));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel, BorderLayout.NORTH);
            textPanel.add(pathLabel, BorderLayout.SOUTH);

            JPanel eastPanel = new JPanel(new BorderLayout(0, 2));
            eastPanel.setOpaque(false);
            eastPanel.add(dateLabel, BorderLayout.NORTH);
            eastPanel.add(chartPanel, BorderLayout.CENTER);

            add(textPanel, BorderLayout.CENTER);
            add(eastPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                       int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Workspace workspace) {
                // Workspaces have no path — show the name only.
                nameLabel.setIcon(Util.themeAwareIcon(FontAwesomeSolid.LAYER_GROUP, 16));
                nameLabel.setText(workspace.getName());
                StringBuilder pathLabelBuilder = new StringBuilder();
                workspace.getProjects().stream().forEach(
                        p -> {
                            pathLabelBuilder
                                    .append(new File(p.getProjectHomeFolder()).getName())
                                    .append(" ");
                        }
                );
                pathLabel.setText(pathLabelBuilder.toString());
                if (workspace.getOpenTime() != null) {
                    dateLabel.setText(GitemberUtil.formatDate(workspace.getOpenTime()));
                } else {
                    dateLabel.setText("");
                }
                BufferedImage chart = WorkspaceChartService.getOrSchedule(workspace, list::repaint);
                chartPanel.setChart(isSelected ? null : chart);
            } else if (value instanceof Project project) {
                String folder = project.getProjectHomeFolder();
                String name = new File(folder).getName();
                nameLabel.setIcon(Util.themeAwareIcon(FontAwesomeSolid.DATABASE, 16));
                nameLabel.setText(name);
                pathLabel.setText(folder);

                if (project.getOpenTime() != null) {
                    dateLabel.setText(GitemberUtil.formatDate(project.getOpenTime()));
                } else {
                    dateLabel.setText("");
                }

                BufferedImage chart = ActivityChartService.getOrSchedule(project, list::repaint);
                chartPanel.setChart(isSelected ? null : chart);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                nameLabel.setForeground(list.getSelectionForeground());
                pathLabel.setForeground(list.getSelectionForeground());
                dateLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                nameLabel.setForeground(list.getForeground());
                pathLabel.setForeground(Color.GRAY);
                dateLabel.setForeground(Color.GRAY);
            }

            return this;
        }

        private static class ChartPanel extends JPanel {
            private BufferedImage chart;

            ChartPanel() {
                setOpaque(false);
                setPreferredSize(new Dimension(120, 28));
            }

            void setChart(BufferedImage img) {
                this.chart = img;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (chart != null) {
                    g.drawImage(chart, 0, 0, getWidth(), getHeight(), null);
                }
            }
        }
    }
}
