package com.az.gitember.dialog;

import com.az.gitember.data.Project;
import com.az.gitember.service.Context;

import com.az.gitember.ui.Util;

import javax.swing.*;
import java.awt.*;

/**
 * Per-project identity settings: author and committer name / e-mail.
 * Blank fields mean "use the global git config value".
 */
public class ProjectSettingsDialog extends JDialog {

    private final JTextField authorNameField;
    private final JTextField authorEmailField;
    private final JTextField committerNameField;
    private final JTextField committerEmailField;

    public ProjectSettingsDialog(Frame owner) {
        super(owner, "Project Settings", java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(480, 320);
        setLocationRelativeTo(owner);
        setResizable(false);

        Project project = Context.getCurrentProject().orElse(null);

        authorNameField    = new JTextField(project != null ? nvl(project.getUserCommitName())  : "", 28);
        authorEmailField   = new JTextField(project != null ? nvl(project.getUserCommitEmail()) : "", 28);
        committerNameField = new JTextField(project != null ? nvl(project.getCommitterName())   : "", 28);
        committerEmailField= new JTextField(project != null ? nvl(project.getCommitterEmail())  : "", 28);

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

    private void applyAndClose() {
        Project project = Context.getCurrentProject().orElse(null);
        if (project == null) {
            dispose();
            return;
        }
        project.setUserCommitName (authorNameField.getText().trim());
        project.setUserCommitEmail(authorEmailField.getText().trim());
        project.setCommitterName  (committerNameField.getText().trim());
        project.setCommitterEmail (committerEmailField.getText().trim());
        Context.saveSettings();
        dispose();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String nvl(String s) { return s != null ? s : ""; }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
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
