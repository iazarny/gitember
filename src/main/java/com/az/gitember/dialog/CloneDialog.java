package com.az.gitember.dialog;

import com.az.gitember.data.RemoteRepoParameters;
import org.apache.commons.lang3.StringUtils;

import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class CloneDialog extends JDialog {

    private final JTextField urlField;
    private final JTextField destField;
    private final JTextField userField;
    private final JPasswordField pwdField;
    private final JPasswordField tokenField;
    private final JButton cloneBtn;
    private final JButton cancelBtn;
    private final JLabel userLbl;
    private final JLabel pwdLbl;
    private final JLabel tokenLbl;
    private final JRadioButton pwdAuthBtn;
    private final JRadioButton tokenAuthBtn;
    private final JPanel authTypePanel;
    private final JCheckBox shallowCheck;
    private final JSpinner  depthSpinner;
    private boolean confirmed = false;

    public CloneDialog(Frame parent) {
        super(parent, "Clone Repository", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // URL
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Repository URL:"), gbc);
        urlField = new JTextField(30);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(urlField, gbc);

        // Destination
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Destination:"), gbc);
        JPanel destPanel = new JPanel(new BorderLayout(5, 0));
        destField = new JTextField(25);
        JButton browseBtn = new JButton("...");
        browseBtn.addActionListener(e -> browseDestination());
        destPanel.add(destField, BorderLayout.CENTER);
        destPanel.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(destPanel, gbc);

        // Auth type selector — shown for HTTPS, hidden for SSH/git@
        pwdAuthBtn = new JRadioButton("Username & Password", true);
        tokenAuthBtn = new JRadioButton("Access Token");
        ButtonGroup authGroup = new ButtonGroup();
        authGroup.add(pwdAuthBtn);
        authGroup.add(tokenAuthBtn);
        authTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        authTypePanel.setOpaque(false);
        authTypePanel.add(pwdAuthBtn);
        authTypePanel.add(Box.createHorizontalStrut(16));
        authTypePanel.add(tokenAuthBtn);
        authTypePanel.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        formPanel.add(sizePreservingWrapper(authTypePanel), gbc);
        gbc.gridwidth = 1;

        // Username row — shown when HTTPS + Password auth
        userLbl = new JLabel("Username:");
        userLbl.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(sizePreservingWrapper(userLbl), gbc);
        userField = new JTextField(20);
        userField.setVisible(false);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(sizePreservingWrapper(userField), gbc);

        // Password row — shown when HTTPS + Password auth
        pwdLbl = new JLabel("Password:");
        pwdLbl.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(sizePreservingWrapper(pwdLbl), gbc);
        pwdField = new JPasswordField(20);
        pwdField.setVisible(false);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(sizePreservingWrapper(pwdField), gbc);

        // Token row — shown when HTTPS + Access Token auth
        tokenLbl = new JLabel("Token:");
        tokenLbl.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(sizePreservingWrapper(tokenLbl), gbc);
        tokenField = new JPasswordField(20);
        tokenField.setVisible(false);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(sizePreservingWrapper(tokenField), gbc);

        // Shallow clone row
        shallowCheck = new JCheckBox("Clone last commits only (shallow clone)");
        depthSpinner  = new JSpinner(new SpinnerNumberModel(100, 1, 999999, 1));
        depthSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) depthSpinner.getEditor()).getTextField().setColumns(6);
        shallowCheck.addActionListener(e -> depthSpinner.setEnabled(shallowCheck.isSelected()));
        JPanel shallowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        shallowPanel.setOpaque(false);
        shallowPanel.add(shallowCheck);
        shallowPanel.add(Box.createHorizontalStrut(8));
        shallowPanel.add(depthSpinner);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.weightx = 1;
        formPanel.add(shallowPanel, gbc);
        gbc.gridwidth = 1;

        // Wire the auth type radio buttons to switch visible credential rows
        pwdAuthBtn.addActionListener(e -> applyAuthMode());
        tokenAuthBtn.addActionListener(e -> applyAuthMode());

        // Wire URL change listener
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onUrlChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onUrlChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onUrlChanged(); }
        });

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cloneBtn = new JButton("Clone");
        cloneBtn.setEnabled(false);
        cloneBtn.addActionListener(e -> onClone());
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cloneBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(cloneBtn);
        Util.bindEscapeToDispose(this);
    }

    // -------------------------------------------------------------------------

    private void onUrlChanged() {
        String url = urlField.getText().trim();
        cloneBtn.setEnabled(StringUtils.isNotBlank(url));

        boolean isHttp = url.startsWith("https:") || url.startsWith("http:");
        authTypePanel.setVisible(isHttp);
        if (isHttp) {
            applyAuthMode();
        } else {
            // SSH / git@ / empty — hide all credential rows
            userLbl.setVisible(false);
            userField.setVisible(false);
            pwdLbl.setVisible(false);
            pwdField.setVisible(false);
            tokenLbl.setVisible(false);
            tokenField.setVisible(false);
        }
    }

    private void applyAuthMode() {
        boolean usePwd = pwdAuthBtn.isSelected();
        userLbl.setVisible(usePwd);
        userField.setVisible(usePwd);
        pwdLbl.setVisible(usePwd);
        pwdField.setVisible(usePwd);
        tokenLbl.setVisible(!usePwd);
        tokenField.setVisible(!usePwd);
    }

    /**
     * Wraps a component so that the grid row always reserves the component's
     * preferred height, even when the component is hidden via setVisible(false).
     * GridBagLayout normally collapses rows around invisible components; the
     * wrapper stays visible and reports a constant preferred size, preventing
     * neighbouring rows from jumping.
     */
    private static JPanel sizePreservingWrapper(JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return content.getPreferredSize();
            }
            @Override
            public Dimension getMinimumSize() {
                return content.getPreferredSize();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private void browseDestination() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Destination Folder");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onClone() {
        if (urlField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Repository URL is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (destField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Destination folder is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public RemoteRepoParameters getParameters() {
        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(urlField.getText().trim());
        params.setDestinationFolder(destField.getText().trim());
        if (tokenAuthBtn.isSelected()) {
            params.setAccessToken(new String(tokenField.getPassword()));
        } else {
            params.setUserName(userField.getText().trim());
            params.setUserPwd(new String(pwdField.getPassword()));
        }
        if (shallowCheck.isSelected()) {
            params.setDepth((Integer) depthSpinner.getValue());
        }
        return params;
    }
}
