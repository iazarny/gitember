package com.az.gitember.ui.welcome;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.ActivityChartService;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.service.WorkspaceChartService;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ProjectCellRenderer extends JPanel implements ListCellRenderer<Object> {

    private final JLabel     nameLabel;
    private final JLabel     pathLabel;
    private final JLabel     dateLabel;
    private final ChartPanel chartPanel;

    public ProjectCellRenderer() {
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