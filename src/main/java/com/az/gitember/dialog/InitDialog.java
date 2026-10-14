package com.az.gitember.dialog;

import com.az.gitember.service.I18n;
import com.az.gitember.data.InitRepoParameters;

import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;

public class InitDialog extends JDialog {

    private final JTextField folderField;
    private final JCheckBox readmeCheck;
    private final JCheckBox gitignoreCheck;
    private final JCheckBox lfsCheck;
    private final JComboBox<String> defaultBranchCombo;
    private boolean confirmed = false;

    public InitDialog(Frame parent) {
        super(parent, I18n.t("initRepository"), true);
        setSize(480, 280);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Folder row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel(I18n.t("folder")), gbc);
        JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
        folderField = new JTextField(25);
        JButton browseBtn = new JButton("...");
        browseBtn.addActionListener(e -> browseFolder());
        folderPanel.add(folderField, BorderLayout.CENTER);
        folderPanel.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(folderPanel, gbc);

        //Default branch
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel(I18n.t("defaultBranch")), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        defaultBranchCombo = new JComboBox<>(
                new String[] {"main", "master"}
        );
        formPanel.add(defaultBranchCombo, gbc);

        // Checkboxes
        readmeCheck = new JCheckBox(I18n.t("createReadmeMd"));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(readmeCheck, gbc);

        gitignoreCheck = new JCheckBox(I18n.t("createGitignore"));
        gbc.gridy = 3;
        formPanel.add(gitignoreCheck, gbc);

        lfsCheck = new JCheckBox(I18n.t("enableGitLfs"));
        gbc.gridy = 4;
        formPanel.add(lfsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        formPanel.add(new JLabel(), gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton initBtn = new JButton(I18n.t("init"));
        initBtn.addActionListener(e -> onInit());
        JButton cancelBtn = new JButton(I18n.t("cancel"));
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(initBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(initBtn);
        Util.bindEscapeToDispose(this);
    }

    private void browseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(I18n.t("selectFolder"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onInit() {
        if (folderField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("folderIsRequired"),
                    I18n.t("validation"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public InitRepoParameters getParameters() {
        InitRepoParameters params = new InitRepoParameters();
        params.setDestinationFolder(folderField.getText().trim());
        params.setInitWithReame(readmeCheck.isSelected());
        params.setInitWithIgnore(gitignoreCheck.isSelected());
        params.setInitWithLfs(lfsCheck.isSelected());
        params.setDefaultBranch(defaultBranchCombo.getSelectedItem().toString());
        return params;
    }
}