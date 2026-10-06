package com.az.gitember.dialog;

import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.az.gitember.service.tracker.JiraIssueTracker;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Jira Cloud / Data Center credentials and project selection.
 */
public class JiraIssueTrackerSettingsPanel extends JPanel {

    private final JTextField serverField;
    private final JTextField emailField;
    private final JPasswordField tokenField;
    private final JRadioButton tokenRadio;
    private final JRadioButton oauthRadio;
    private final TrackerProjectCheckList projectsList;

    public JiraIssueTrackerSettingsPanel(IssueTrackerConfig initial) {
        super(new GridBagLayout());
        IssueTrackerConfig cfg = initial != null ? initial : new IssueTrackerConfig();
        boolean jiraType = !IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(cfg.getType());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Server:"), gbc);
        serverField = new JTextField(jiraType && cfg.getServer() != null ? cfg.getServer() : "", 28);
        serverField.putClientProperty("JTextField.placeholderText", "https://company.atlassian.net");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(serverField, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Authentication:"), gbc);
        tokenRadio = new JRadioButton("API Token",
                jiraType && !IssueTrackerConfig.AUTH_OAUTH.equals(cfg.getAuthType()));
        oauthRadio = new JRadioButton("OAuth (coming soon)");
        oauthRadio.setEnabled(false);
        ButtonGroup authGroup = new ButtonGroup();
        authGroup.add(tokenRadio);
        authGroup.add(oauthRadio);
        JPanel authRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        authRow.add(tokenRadio);
        authRow.add(oauthRadio);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(authRow, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Email:"), gbc);
        emailField = new JTextField(jiraType && cfg.getEmail() != null ? cfg.getEmail() : "", 28);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(emailField, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("API Token:"), gbc);
        tokenField = new JPasswordField(jiraType && cfg.getApiToken() != null ? cfg.getApiToken() : "", 28);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(tokenField, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        JButton testBtn = new JButton("Test Connection");
        testBtn.addActionListener(e -> testConnection());
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(testBtn, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Projects:"), gbc);
        projectsList = new TrackerProjectCheckList("Test connection to load projects.");
        projectsList.populate(jiraType ? cfg.getProjectKeys() : List.of(), List.of());
        JScrollPane projectScroll = new JScrollPane(projectsList);
        projectScroll.setPreferredSize(new Dimension(280, 90));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(projectScroll, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
    }

    public IssueTrackerConfig getConfig() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setType(IssueTrackerConfig.TYPE_JIRA);
        cfg.setServer(serverField.getText().trim());
        cfg.setAuthType(IssueTrackerConfig.AUTH_API_TOKEN);
        cfg.setEmail(emailField.getText().trim());
        cfg.setApiToken(new String(tokenField.getPassword()));
        cfg.setProjectKeys(projectsList.selectedKeys());
        return cfg;
    }

    private void testConnection() {
        IssueTrackerConfig cfg = getConfig();
        cfg.setEnabled(true);
        Window owner = SwingUtilities.getWindowAncestor(this);
        boolean ready = cfg.isReady();
        if (!ready) {
            JOptionPane.showMessageDialog(owner != null ? owner : this,
                    "Enter server, email, and API token first.",
                    "Jira", JOptionPane.WARNING_MESSAGE);
        }
        if (ready) {
            new SwingWorker<String, Void>() {
                List<TrackerProject> projects = List.of();

                @Override
                protected String doInBackground() throws Exception {
                    JiraIssueTracker tracker = new JiraIssueTracker(cfg);
                    String who = tracker.testConnection();
                    projects = tracker.listProjects();
                    return who;
                }

                @Override
                protected void done() {
                    try {
                        String who = get();
                        List<String> previously = projectsList.selectedKeys();
                        if (previously.isEmpty()) {
                            previously.addAll(cfg.getProjectKeys());
                        }
                        projectsList.populate(previously, projects);
                        JOptionPane.showMessageDialog(owner,
                                "Connected as " + who + ".\n" + projects.size() + " project(s) loaded.",
                                "Jira", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(owner,
                                "Cannot connect:\n" + cause.getMessage(),
                                "Jira", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}
