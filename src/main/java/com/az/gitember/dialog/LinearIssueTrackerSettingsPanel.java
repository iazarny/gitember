package com.az.gitember.dialog;

import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.TrackerProject;
import com.az.gitember.service.tracker.LinearIssueTracker;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Linear credentials (personal API key) and team selection.
 */
public class LinearIssueTrackerSettingsPanel extends JPanel {

    private final JPasswordField tokenField;
    private final JTextField workspaceField;
    private final TrackerProjectCheckList teamsList;

    public LinearIssueTrackerSettingsPanel(IssueTrackerConfig initial) {
        super(new GridBagLayout());
        IssueTrackerConfig cfg = initial != null ? initial : new IssueTrackerConfig();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel hint = new JLabel("<html>Create a personal API key in Linear → Settings → Security &amp; access.</html>");
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(hint, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("API Key:"), gbc);
        String token = IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(cfg.getType()) ? cfg.getApiToken() : "";
        tokenField = new JPasswordField(token != null ? token : "", 28);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(tokenField, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Workspace:"), gbc);
        String workspace = IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(cfg.getType())
                ? (cfg.getServer() != null ? cfg.getServer() : "") : "";
        workspaceField = new JTextField(workspace, 28);
        workspaceField.putClientProperty("JTextField.placeholderText", "https://linear.app/your-workspace");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(workspaceField, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        JButton testBtn = new JButton("Test Connection");
        testBtn.addActionListener(e -> testConnection());
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(testBtn, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Teams:"), gbc);
        teamsList = new TrackerProjectCheckList("Test connection to load teams.");
        List<String> keys = IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(cfg.getType())
                ? cfg.getProjectKeys() : List.of();
        teamsList.populate(keys, List.of());
        JScrollPane teamScroll = new JScrollPane(teamsList);
        teamScroll.setPreferredSize(new Dimension(280, 90));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(teamScroll, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
    }

    public IssueTrackerConfig getConfig() {
        IssueTrackerConfig cfg = new IssueTrackerConfig();
        cfg.setType(IssueTrackerConfig.TYPE_LINEAR);
        cfg.setAuthType(IssueTrackerConfig.AUTH_API_TOKEN);
        cfg.setApiToken(new String(tokenField.getPassword()));
        cfg.setServer(workspaceField.getText().trim());
        cfg.setEmail("");
        cfg.setProjectKeys(teamsList.selectedKeys());
        return cfg;
    }

    private void testConnection() {
        IssueTrackerConfig cfg = getConfig();
        cfg.setEnabled(true);
        Window owner = SwingUtilities.getWindowAncestor(this);
        boolean ready = cfg.isReady();
        if (!ready) {
            JOptionPane.showMessageDialog(owner != null ? owner : this,
                    "Enter a Linear API key first.",
                    "Linear", JOptionPane.WARNING_MESSAGE);
        }
        if (ready) {
            new SwingWorker<String, Void>() {
                List<TrackerProject> teams = List.of();
                String workspaceUrl = "";

                @Override
                protected String doInBackground() throws Exception {
                    LinearIssueTracker tracker = new LinearIssueTracker(cfg);
                    String who = tracker.testConnection();
                    teams = tracker.listProjects();
                    workspaceUrl = tracker.getOrganizationUrl();
                    return who;
                }

                @Override
                protected void done() {
                    try {
                        String who = get();
                        if ((workspaceField.getText() == null || workspaceField.getText().isBlank())
                                && workspaceUrl != null && !workspaceUrl.isBlank()) {
                            workspaceField.setText(workspaceUrl);
                        }
                        List<String> previously = teamsList.selectedKeys();
                        if (previously.isEmpty()) {
                            previously.addAll(cfg.getProjectKeys());
                        }
                        teamsList.populate(previously, teams);
                        JOptionPane.showMessageDialog(owner,
                                "Connected as " + who + ".\n" + teams.size() + " team(s) loaded.",
                                "Linear", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(owner,
                                "Cannot connect:\n" + cause.getMessage(),
                                "Linear", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}
