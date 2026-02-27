package com.az.gitember.dialog;

import com.az.gitember.data.Project;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for configuring per-project remote credentials.
 * Supports both classic username/password and access-token (GitHub PAT, GitLab token, etc.).
 * The access token takes priority over username/password in transport commands.
 */
public class CredentialsDialog extends JDialog {

    private final JPasswordField tokenField;
    private final JTextField userField;
    private final JPasswordField pwdField;
    private boolean confirmed = false;

    public CredentialsDialog(Frame parent, Project project) {
        super(parent, "Repository Credentials", true);
        setSize(460, 260);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Info label
        JLabel info = new JLabel(
                "<html>Access token takes priority over username/password.<br>"
                + "Leave token blank to use username &amp; password instead.</html>");
        info.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        form.add(info, gbc);
        gbc.gridwidth = 1;

        // Access token
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Access Token:"), gbc);
        tokenField = new JPasswordField(25);
        if (project.getAccessToken() != null) {
            tokenField.setText(project.getAccessToken());
        }
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(tokenField, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Username:"), gbc);
        userField = new JTextField(25);
        if (project.getUserName() != null) {
            userField.setText(project.getUserName());
        }
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(userField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(new JLabel("Password:"), gbc);
        pwdField = new JPasswordField(25);
        if (project.getUserPwd() != null) {
            pwdField.setText(project.getUserPwd());
        }
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(pwdField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Save");
        okBtn.addActionListener(e -> { confirmed = true; dispose(); });
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okBtn);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getAccessToken() {
        return new String(tokenField.getPassword()).trim();
    }

    public String getUserName() {
        return userField.getText().trim();
    }

    public String getPassword() {
        return new String(pwdField.getPassword());
    }

    public void setAccessToken(String accessToken) {
        tokenField.setText(accessToken);
    }

    public void setUserName(String userName) {
        userField.setText(userName);
    }

    public void setPassword(String password) {
        pwdField.setText(password);
    }
}
