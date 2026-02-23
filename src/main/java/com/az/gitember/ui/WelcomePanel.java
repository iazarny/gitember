package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.service.GitemberUtil;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Welcome screen shown on startup with a list of recent projects.
 */
public class WelcomePanel extends JPanel {

    private final DefaultListModel<Project> listModel;
    private final JList<Project> projectList;
    private Consumer<Project> onProjectSelected;
    private Runnable onOpenRepo;
    private Runnable onCloneRepo;
    private Runnable onInitRepo;

    public WelcomePanel() {
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Gitember", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 28f));
        header.setBorder(BorderFactory.createEmptyBorder(40, 0, 10, 0));



        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));

        JButton openRepoBtn  = createWellcomeButton("Open Repository", FontAwesomeSolid.FOLDER_OPEN);
        JButton cloneRepoBtn = createWellcomeButton("Clone repository", FontAwesomeSolid.CLONE);
        JButton initRepoBtn  = createWellcomeButton("Init repository", FontAwesomeSolid.FOLDER_PLUS);

        openRepoBtn.addActionListener(e -> { if (onOpenRepo != null) onOpenRepo.run(); });
        cloneRepoBtn.addActionListener(e -> { if (onCloneRepo != null) onCloneRepo.run(); });
        initRepoBtn.addActionListener(e -> { if (onInitRepo != null) onInitRepo.run(); });

        commandPanel.add(Box.createHorizontalGlue());
        commandPanel.add(openRepoBtn);
        commandPanel.add(Box.createHorizontalStrut(20));
        commandPanel.add(cloneRepoBtn);
        commandPanel.add(Box.createHorizontalStrut(20));
        commandPanel.add(initRepoBtn);
        commandPanel.add(Box.createHorizontalGlue());



        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.NORTH);


        /*        JLabel subtitle = new JLabel("Recent Projects", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(Color.GRAY);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.add(subtitle, BorderLayout.SOUTH);*/

        // Project list
        listModel = new DefaultListModel<>();
        projectList = new JList<>(listModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setCellRenderer(new ProjectCellRenderer());
        projectList.setFixedCellHeight(60);

        projectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = projectList.locationToIndex(e.getPoint());
                    if (index >= 0 && projectList.getCellBounds(index, index).contains(e.getPoint())) {
                        Project project = listModel.getElementAt(index);
                        if (onProjectSelected != null) {
                            onProjectSelected.accept(project);
                        }
                    }
                }
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

    private JButton createWellcomeButton(String text, FontAwesomeSolid icon) {
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

    public void setOnProjectSelected(Consumer<Project> handler) {
        this.onProjectSelected = handler;
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

    public void setProjects(Collection<Project> projects) {
        listModel.clear();
        if (projects != null) {
            // Show most recently opened first
            projects.stream()
                    .sorted((a, b) -> {
                        if (a.getOpenTime() == null && b.getOpenTime() == null) return 0;
                        if (a.getOpenTime() == null) return 1;
                        if (b.getOpenTime() == null) return -1;
                        return b.getOpenTime().compareTo(a.getOpenTime());
                    })
                    .forEach(listModel::addElement);
        }
    }

    private static class ProjectCellRenderer extends JPanel implements ListCellRenderer<Project> {

        private final JLabel nameLabel;
        private final JLabel pathLabel;
        private final JLabel dateLabel;

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

            JPanel textPanel = new JPanel(new BorderLayout(0, 2));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel, BorderLayout.NORTH);
            textPanel.add(pathLabel, BorderLayout.SOUTH);

            add(textPanel, BorderLayout.CENTER);
            add(dateLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Project> list, Project project,
                                                       int index, boolean isSelected, boolean cellHasFocus) {
            String folder = project.getProjectHomeFolder();
            String name = new File(folder).getName();
            nameLabel.setText(name);
            pathLabel.setText(folder);

            if (project.getOpenTime() != null) {
                dateLabel.setText(GitemberUtil.formatDate(project.getOpenTime()));
            } else {
                dateLabel.setText("");
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
    }
}
