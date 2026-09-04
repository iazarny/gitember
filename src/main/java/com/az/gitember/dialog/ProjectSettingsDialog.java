package com.az.gitember.dialog;

import com.az.gitember.data.Const;
import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.az.gitember.ui.FileViewerWindow;

import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.ObjectUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Per-project settings: author/committer identity and remote repository credentials.
 * Blank identity fields mean "use the global git config value".
 */
public class ProjectSettingsDialog extends JDialog {

    private final JTextField authorNameField;
    private final JTextField authorEmailField;
    private final JTextField committerNameField;
    private final JTextField committerEmailField;

    private final JPasswordField tokenField;
    private final JTextField     userField;
    private final JPasswordField pwdField;

    private final JCheckBox showAllPullRequestsCheck;

    public ProjectSettingsDialog(Frame owner) {
        super(owner, "Project Settings", java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(480, 720);
        setLocationRelativeTo(owner);
        setResizable(false);

        Project project = Context.getCurrentProject().orElse(null);

        authorNameField    = new JTextField(project != null ? ObjectUtils.getIfNull(project.getUserCommitName(),"")  : "", 28);
        authorEmailField   = new JTextField(project != null ? ObjectUtils.getIfNull(project.getUserCommitEmail(),"") : "", 28);
        committerNameField = new JTextField(project != null ? ObjectUtils.getIfNull(project.getCommitterName(),"")   : "", 28);
        committerEmailField= new JTextField(project != null ? ObjectUtils.getIfNull(project.getCommitterEmail(),"")  : "", 28);

        tokenField = new JPasswordField(25);
        userField  = new JTextField(25);
        pwdField   = new JPasswordField(25);
        showAllPullRequestsCheck = new JCheckBox("Show all pull requests (including merged / closed)");
        showAllPullRequestsCheck.setToolTipText(
                "When off, only open pull/merge requests are listed; when on, merged and closed ones are shown too");
        if (project != null) {
            if (project.getAccessToken() != null) tokenField.setText(project.getAccessToken());
            if (project.getUserName()    != null) userField.setText(project.getUserName());
            if (project.getUserPwd()     != null) pwdField.setText(project.getUserPwd());
            showAllPullRequestsCheck.setSelected(project.isShowAllPullRequests());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.anchor  = GridBagConstraints.WEST;

        addSeparatorLabel(form, gbc, 0, "Author  (who wrote the change)");
        addRow(form, gbc, 1, "Name:",  authorNameField);
        addRow(form, gbc, 2, "Email:", authorEmailField);
        addSeparatorLabel(form, gbc, 3, "Committer  (who applied the change — leave blank to use author)");
        addRow(form, gbc, 4, "Name:",  committerNameField);
        addRow(form, gbc, 5, "Email:", committerEmailField);



        addSeparatorLabel(form, gbc, 6, "Repository Credentials");

        JLabel info = new JLabel(
                "<html>Access token takes priority over username/password.<br>"
                + "Leave token blank to use username &amp; password instead.</html>");
        info.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(info, gbc);
        gbc.gridwidth = 1;

        addRow(form, gbc, 8, "Access Token:", tokenField);
        addRow(form, gbc, 9, "Username:",     userField);
        addRow(form, gbc, 10, "Password:",     pwdField);

        addSeparatorLabel(form, gbc, 11, "Remote URL");

        String originUrl = null;
        if (Context.getGitRepoService() != null) {
            originUrl = Context.getGitRepoService().getOriginUrl();
        }
        JTextField remoteUrlField = new JTextField(ObjectUtils.getIfNull(originUrl, ""), 28);
        remoteUrlField.setEditable(false);
        remoteUrlField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        addRow(form, gbc, 12, "Origin:", remoteUrlField);

        addSeparatorLabel(form, gbc, 13, "Pull Requests");
        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(showAllPullRequestsCheck, gbc);
        gbc.gridwidth = 1;

        addSeparatorLabel(form, gbc, 15, "Ignore rules");
        JButton editGitIgnoreBtn = new JButton("Edit .gitignore...");
        editGitIgnoreBtn.setName("editGitIgnoreButton");
        editGitIgnoreBtn.setToolTipText("Open the repository .gitignore in a separate editor window");
        editGitIgnoreBtn.setEnabled(project != null);
        editGitIgnoreBtn.addActionListener(e -> openGitIgnoreEditor());
        gbc.gridx = 0; gbc.gridy = 16; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(editGitIgnoreBtn, gbc);
        gbc.gridwidth = 1;

        int nextRow = 17;
        Settings settings = Context.getSettings();
        if (settings != null && settings.isVerifyCommitSignatures() && settings.isRepositoryAllowedSigners()) {
            addSeparatorLabel(form, gbc, nextRow, "Commit signatures");
            nextRow++;
            JButton editAllowedSignersBtn = new JButton("Edit .git/allowed_signers...");
            editAllowedSignersBtn.setToolTipText("Open this repository's allowed_signers file");
            editAllowedSignersBtn.setEnabled(project != null);
            editAllowedSignersBtn.addActionListener(e -> openRepoAllowedSignersEditor());
            gbc.gridx = 0; gbc.gridy = nextRow; gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            form.add(editAllowedSignersBtn, gbc);
            gbc.gridwidth = 1;
            setSize(480, 780);
        }

        JButton okBtn     = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(e -> applyAndClose());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form,     BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
        Util.bindEscapeToDispose(this);
    }

    private void openGitIgnoreEditor() {
        String folder = Context.getProjectFolder();
        if (folder != null && !folder.isBlank()) {
            FileViewerWindow.openForEdit("Edit .gitignore", Path.of(folder, Const.GIT_IGNORE_NAME));
        }
    }

    private void openRepoAllowedSignersEditor() {
        if (Context.getGitRepoService() != null && Context.getGitRepoService().getRepository() != null) {
            Path path = Context.getGitRepoService().getRepository().getDirectory().toPath()
                    .resolve(Const.ALLOWED_SIGNERS_NAME);
            FileViewerWindow.openForEdit("Edit allowed_signers", path);
        }
    }

    private void applyAndClose() {
        // Projects are interned (Settings#internAll): the active project is the single shared
        // instance also referenced by any workspace it belongs to, so there is no separate
        // "similar" copy left to keep in sync.
        Context.getCurrentProject().ifPresent(project -> {
            project.setUserCommitName (authorNameField.getText().trim());
            project.setUserCommitEmail(authorEmailField.getText().trim());
            project.setCommitterName  (committerNameField.getText().trim());
            project.setCommitterEmail (committerEmailField.getText().trim());
            project.setAccessToken(new String(tokenField.getPassword()).trim());
            project.setUserName   (userField.getText().trim());
            project.setUserPwd    (new String(pwdField.getPassword()));
        });
        Context.saveSettings();
        dispose();
    }

    // ── helpers ──────────────────────────────────────────────────────────────


    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(field, gbc);
    }

    private void addSeparatorLabel(JPanel form, GridBagConstraints gbc, int row, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, lbl.getFont().getSize() - 1f));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(lbl, gbc);
        gbc.gridwidth = 1;
    }
}
