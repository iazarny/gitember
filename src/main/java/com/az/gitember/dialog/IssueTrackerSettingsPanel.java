package com.az.gitember.dialog;

import com.az.gitember.data.IssueTrackerConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Per-repository issue-tracker form. Tracker type (Jira / Linear) selects the
 * credentials panel; branch and commit options stay shared.
 */
public class IssueTrackerSettingsPanel extends JPanel {

    public static final String LABEL_JIRA = "Jira";
    public static final String LABEL_LINEAR = "Linear";

    private final JComboBox<String> trackerCombo;
    private final JCheckBox enabledCheck;
    private final JPanel cards;
    private final CardLayout cardLayout;
    private final JiraIssueTrackerSettingsPanel jiraPanel;
    private final LinearIssueTrackerSettingsPanel linearPanel;
    private final JCheckBox includeIssueKeyCheck;
    private final JCheckBox commentOnCommitCheck;
    private final JTextField branchPatternField;
    private final JTextField branchTypeField;

    public IssueTrackerSettingsPanel(IssueTrackerConfig initial) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        IssueTrackerConfig cfg = initial != null ? initial : new IssueTrackerConfig();

        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel privacy = new JLabel("<html>Credentials stay on this machine (OS keychain / encrypted settings).<br>"
                + "Requests go only to the tracker you configure.</html>");
        privacy.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        north.add(privacy, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        north.add(new JLabel("Tracker:"), gbc);
        trackerCombo = new JComboBox<>(new String[]{LABEL_JIRA, LABEL_LINEAR});
        trackerCombo.setSelectedItem(typeToLabel(cfg.getType()));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        north.add(trackerCombo, gbc);

        enabledCheck = new JCheckBox("Enable", cfg.isEnabled());
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        north.add(enabledCheck, gbc);

        jiraPanel = new JiraIssueTrackerSettingsPanel(cfg);
        linearPanel = new LinearIssueTrackerSettingsPanel(cfg);
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(jiraPanel, IssueTrackerConfig.TYPE_JIRA);
        cards.add(linearPanel, IssueTrackerConfig.TYPE_LINEAR);
        trackerCombo.addActionListener(e -> showSelectedTracker());
        showSelectedTracker();

        JPanel south = new JPanel(new GridBagLayout());
        GridBagConstraints sg = new GridBagConstraints();
        sg.insets = new Insets(4, 8, 4, 8);
        sg.anchor = GridBagConstraints.WEST;

        sg.gridx = 0;
        sg.gridy = 0;
        south.add(new JLabel("Branch naming:"), sg);
        branchPatternField = new JTextField(cfg.getBranchNamePattern(), 22);
        branchPatternField.setToolTipText("Placeholders: {type} {key} {slug}");
        sg.gridx = 1;
        sg.gridwidth = 2;
        sg.fill = GridBagConstraints.HORIZONTAL;
        sg.weightx = 1;
        south.add(branchPatternField, sg);
        sg.gridwidth = 1;
        sg.fill = GridBagConstraints.NONE;
        sg.weightx = 0;

        sg.gridx = 0;
        sg.gridy = 1;
        south.add(new JLabel("Branch type:"), sg);
        branchTypeField = new JTextField(cfg.getBranchType(), 12);
        sg.gridx = 1;
        south.add(branchTypeField, sg);

        includeIssueKeyCheck = new JCheckBox("Include issue key in commit message",
                cfg.isIncludeIssueKeyInCommit());
        sg.gridx = 1;
        sg.gridy = 2;
        sg.gridwidth = 2;
        south.add(includeIssueKeyCheck, sg);

        commentOnCommitCheck = new JCheckBox("Add an issue comment when committing",
                cfg.isCommentOnCommit());
        sg.gridy = 3;
        south.add(commentOnCommitCheck, sg);

        add(north, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    public IssueTrackerConfig getConfig() {
        IssueTrackerConfig cfg;
        if (LABEL_LINEAR.equals(trackerCombo.getSelectedItem())) {
            cfg = linearPanel.getConfig();
        } else {
            cfg = jiraPanel.getConfig();
        }
        cfg.setEnabled(enabledCheck.isSelected());
        cfg.setIncludeIssueKeyInCommit(includeIssueKeyCheck.isSelected());
        cfg.setCommentOnCommit(commentOnCommitCheck.isSelected());
        cfg.setBranchNamePattern(branchPatternField.getText().trim());
        cfg.setBranchType(branchTypeField.getText().trim());
        return cfg;
    }

    private void showSelectedTracker() {
        String type = LABEL_LINEAR.equals(trackerCombo.getSelectedItem())
                ? IssueTrackerConfig.TYPE_LINEAR : IssueTrackerConfig.TYPE_JIRA;
        cardLayout.show(cards, type);
    }

    private static String typeToLabel(String type) {
        String label = LABEL_JIRA;
        if (IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(type)) {
            label = LABEL_LINEAR;
        }
        return label;
    }
}
