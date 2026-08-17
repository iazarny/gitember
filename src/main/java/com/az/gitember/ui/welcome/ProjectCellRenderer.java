package com.az.gitember.ui.welcome;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.ActivityChartService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.service.WorkspaceChartService;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ProjectCellRenderer extends JPanel implements ListCellRenderer<Object> {

    /** Extra left indent for a project rendered as a workspace's child. */
    private static final int NESTED_INDENT = 40;

    /** Width of the clickable chevron area that toggles a workspace's expanded state. */
    public static final int CHEVRON_AREA_WIDTH = 16;

    private final JLabel     chevronLabel;
    private final Icon       chevronRightIcon;
    private final Icon       chevronDownIcon;
    private final JLabel     nameLabel;
    private final JLabel     pathLabel;
    private final JLabel     dateLabel;
    private final ChartPanel chartPanel;
    private final Border     topLevelBorder;
    private final Border     nestedBorder;

    private Set<Project> nestedProjects = Set.of();
    private Set<Workspace> expandedWorkspaces = Set.of();

    public ProjectCellRenderer() {
        setLayout(new BorderLayout(8, 2));
        topLevelBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        );
        nestedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 16 + NESTED_INDENT, 8, 16)
        );
        setBorder(topLevelBorder);

        chevronRightIcon = Util.themeAwareIcon(FontAwesomeSolid.CHEVRON_RIGHT, 12);
        chevronDownIcon = Util.themeAwareIcon(FontAwesomeSolid.CHEVRON_DOWN, 12);
        chevronLabel = new JLabel();
        chevronLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chevronLabel.setPreferredSize(new Dimension(CHEVRON_AREA_WIDTH, CHEVRON_AREA_WIDTH));
        chevronLabel.setMinimumSize(chevronLabel.getPreferredSize());

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

        JPanel nameRow = new JPanel(new BorderLayout(4, 0));
        nameRow.setOpaque(false);
        nameRow.add(chevronLabel, BorderLayout.WEST);
        nameRow.add(nameLabel, BorderLayout.CENTER);

        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setOpaque(false);
        textPanel.add(nameRow, BorderLayout.NORTH);
        textPanel.add(pathLabel, BorderLayout.SOUTH);

        JPanel eastPanel = new JPanel(new BorderLayout(0, 2));
        eastPanel.setOpaque(false);
        eastPanel.add(dateLabel, BorderLayout.NORTH);
        eastPanel.add(chartPanel, BorderLayout.CENTER);

        add(textPanel, BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }

    /** Marks which projects belong under a workspace, so they render indented. */
    public void setNestedProjects(Collection<Project> nested) {
        this.nestedProjects = new HashSet<>(nested);
    }

    /** Marks which workspaces are expanded, so their chevron points down and children are visible. */
    public void setExpandedWorkspaces(Collection<Workspace> expanded) {
        this.expandedWorkspaces = new HashSet<>(expanded);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        setBorder(value instanceof Project project && nestedProjects.contains(project) ? nestedBorder : topLevelBorder);

        if (value instanceof Workspace workspace) {
            // Workspaces have no path — show the name only.
            chevronLabel.setIcon(expandedWorkspaces.contains(workspace) ? chevronDownIcon : chevronRightIcon);
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
            chevronLabel.setIcon(null);
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
            chevronLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            nameLabel.setForeground(list.getForeground());
            pathLabel.setForeground(Color.GRAY);
            dateLabel.setForeground(Color.GRAY);
            chevronLabel.setForeground(list.getForeground());
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