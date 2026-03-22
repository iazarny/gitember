package com.az.gitember.dialog;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.TreeSet;

public class SettingsDialog extends JDialog {

    private final JComboBox<String> themeCombo;
    private final JSpinner          fontSizeSpinner;
    private final JTextArea         ignoreExtArea;
    private final JCheckBox         leakDetectorCheck;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        setSize(460, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        Settings settings = Context.getSettings();
        String currentTheme = settings != null && "dark".equalsIgnoreCase(settings.getTheme()) ? "Dark" : "Light";
        int currentFontSize = settings != null ? settings.getFontSize() : 13;
        if (currentFontSize <= 0) currentFontSize = 13;
        boolean currentLeakDetector = settings == null || !Boolean.FALSE.equals(settings.getEnableLeakDetector());

        // Show the stored ignore list (defaults are seeded at startup, so this is always populated)
        String currentIgnore = settings != null
                ? String.join(", ", settings.getIgnoreCompareFiles())
                : String.join(", ", Settings.DEFAULT_IGNORE_COMPARE_FILES);
        ignoreExtArea = new JTextArea(currentIgnore, 3, 30);
        ignoreExtArea.setLineWrap(true);
        ignoreExtArea.setWrapStyleWord(true);
        ignoreExtArea.setFont(ignoreExtArea.getFont().deriveFont(Font.PLAIN, 11f));

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

        // Ignore extensions (folder compare)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Ignore extensions\n(folder compare):"), gbc);

        JScrollPane ignoreScroll = new JScrollPane(ignoreExtArea);
        ignoreScroll.setPreferredSize(new Dimension(0, 64));
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        form.add(ignoreScroll, gbc);
        gbc.weighty = 0;

        JButton resetBtn = new JButton("Reset to defaults");
        resetBtn.setFont(resetBtn.getFont().deriveFont(Font.PLAIN, 11f));
        resetBtn.addActionListener(e -> ignoreExtArea.setText(
                String.join(", ", Settings.DEFAULT_IGNORE_COMPARE_FILES)));
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        form.add(resetBtn, gbc);
        gbc.anchor = GridBagConstraints.WEST;

        // Leak detector
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Enable secret detector:"), gbc);

        leakDetectorCheck = new JCheckBox("", currentLeakDetector);
        leakDetectorCheck.setToolTipText("Scan staged files for secrets words before each commit");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(leakDetectorCheck, gbc);

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

        // Parse ignore-extensions textarea (comma-separated, strip dots and whitespace)
        TreeSet<String> ignore = new TreeSet<>();
        for (String tok : ignoreExtArea.getText().split("[,\\s]+")) {
            String ext = tok.trim().toLowerCase().replaceAll("^\\.", "");
            if (!ext.isEmpty()) ignore.add(ext);
        }
        // Store exactly what the user typed — no magic substitution
        settings.setIgnoreCompareFiles(ignore);

        settings.setEnableLeakDetector(leakDetectorCheck.isSelected());

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
