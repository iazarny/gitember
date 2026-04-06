package com.az.gitember.dialog;

import com.az.gitember.data.InitRepoParameters;

import com.az.gitember.ui.Util;

import javax.swing.*;
import java.awt.*;

public class InitDialog extends JDialog {

    private final JTextField folderField;
    private final JCheckBox readmeCheck;
    private final JCheckBox gitignoreCheck;
    private final JCheckBox lfsCheck;
    private boolean confirmed = false;

    public InitDialog(Frame parent) {
        super(parent, "Init Repository", true);
        setSize(450, 230);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Folder row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Folder:"), gbc);
        JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
        folderField = new JTextField(25);
        JButton browseBtn = new JButton("...");
        browseBtn.addActionListener(e -> browseFolder());
        folderPanel.add(folderField, BorderLayout.CENTER);
        folderPanel.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(folderPanel, gbc);

        // Checkboxes
        readmeCheck = new JCheckBox("Create README.md");
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(readmeCheck, gbc);

        gitignoreCheck = new JCheckBox("Create .gitignore");
        gbc.gridy = 2;
        formPanel.add(gitignoreCheck, gbc);

        lfsCheck = new JCheckBox("Enable Git LFS");
        gbc.gridy = 3;
        formPanel.add(lfsCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton initBtn = new JButton("Init");
        initBtn.addActionListener(e -> onInit());
        JButton cancelBtn = new JButton("Cancel");
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
        chooser.setDialogTitle("Select Folder");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onInit() {
        if (folderField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Folder is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
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
        return params;
    }
}