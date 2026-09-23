package com.az.gitember.dialog;

import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Collects the remote URL and repository-relative path for {@code git submodule add}.
 */
public class AddSubmoduleDialog extends JDialog {

    private final JTextField urlField;
    private final JTextField pathField;
    private boolean confirmed;

    public AddSubmoduleDialog(Frame parent) {
        super(parent, "Add Submodule", true);
        setSize(520, 190);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("URL:"), gbc);
        urlField = new JTextField(32);
        urlField.putClientProperty("JTextField.placeholderText", "https://github.com/org/lib.git");
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(urlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Path:"), gbc);
        pathField = new JTextField(32);
        pathField.putClientProperty("JTextField.placeholderText", "libs/lib");
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(pathField, gbc);

        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        addBtn.addActionListener(e -> onConfirm());
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        buttons.add(addBtn);
        buttons.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(addBtn);
        Util.bindEscapeToDispose(this);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public String getPath() {
        return pathField.getText().trim();
    }

    private void onConfirm() {
        if (StringUtils.isBlank(getUrl()) || StringUtils.isBlank(getPath())) {
            JOptionPane.showMessageDialog(this,
                    "URL and path are required.",
                    "Add Submodule", JOptionPane.WARNING_MESSAGE);
        } else {
            confirmed = true;
            dispose();
        }
    }
}
