package com.az.gitember.dialog;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private final JComboBox<String> themeCombo;
    private final JSpinner fontSizeSpinner;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        setSize(350, 180);
        setLocationRelativeTo(owner);
        setResizable(false);

        Settings settings = Context.getSettings();
        String currentTheme = settings != null && "dark".equalsIgnoreCase(settings.getTheme()) ? "Dark" : "Light";
        int currentFontSize = settings != null ? settings.getFontSize() : 13;
        if (currentFontSize <= 0) currentFontSize = 13;

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Theme
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Theme:"), gbc);

        themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setSelectedItem(currentTheme);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(themeCombo, gbc);

        // Font size
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Font size:"), gbc);

        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(currentFontSize, 8, 36, 1));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(fontSizeSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> applyAndClose());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okBtn);
    }

    private void applyAndClose() {
        Settings settings = Context.getSettings();
        if (settings == null) {
            dispose();
            return;
        }

        String selectedTheme = (String) themeCombo.getSelectedItem();
        int selectedFontSize = (int) fontSizeSpinner.getValue();

        boolean themeChanged = !selectedTheme.equalsIgnoreCase(
                "dark".equalsIgnoreCase(settings.getTheme()) ? "Dark" : "Light");
        boolean fontChanged = selectedFontSize != settings.getFontSize();

        settings.setTheme("Dark".equals(selectedTheme) ? "dark" : "light");
        settings.setFontSize(selectedFontSize);
        Context.saveSettings();

        if (themeChanged) {
            try {
                if ("Dark".equals(selectedTheme)) {
                    FlatDarkLaf.setup();
                } else {
                    FlatLightLaf.setup();
                }
            } catch (Exception ignored) {
            }
        }

        if (fontChanged) {
            Font defaultFont = UIManager.getFont("defaultFont");
            if (defaultFont == null) {
                defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, selectedFontSize);
            } else {
                defaultFont = defaultFont.deriveFont((float) selectedFontSize);
            }
            UIManager.put("defaultFont", defaultFont);
        }

        if (themeChanged || fontChanged) {
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
        }

        dispose();
    }

    public static void applyFontSize(int size) {
        Font defaultFont = UIManager.getFont("defaultFont");
        if (defaultFont == null) {
            defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, size);
        } else {
            defaultFont = defaultFont.deriveFont((float) size);
        }
        UIManager.put("defaultFont", defaultFont);
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
        }
    }
}
