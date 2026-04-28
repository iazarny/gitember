package com.az.gitember.dialog;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import java.awt.*;
import java.util.TreeSet;

public class SettingsDialog extends JDialog {

    private final JComboBox<String> themeCombo;
    private final JSpinner          fontSizeSpinner;
    private final JTextArea         ignoreExtArea;
    private final JCheckBox         leakDetectorCheck;
    private final JCheckBox         branchCompareDescCheck;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        setSize(460, 400);
        setLocationRelativeTo(owner);
        setResizable(false);

        Settings settings = Context.getSettings();
        String currentTheme = settings != null && "dark".equalsIgnoreCase(settings.getTheme()) ? "Dark" : "Light";
        int currentFontSize = settings != null ? settings.getFontSize() : 13;
        if (currentFontSize <= 0) currentFontSize = 13;
        boolean currentLeakDetector = settings == null || !Boolean.FALSE.equals(settings.getEnableLeakDetector());
        boolean currentBranchCompareDesc = settings != null && Boolean.TRUE.equals(settings.getEnableBranchCompareDescription());

        // Show the stored ignore list (defaults are seeded at startup, so this is always populated)
        String currentIgnore = settings != null
                ? String.join(", ", settings.getIgnoreCompareFiles())
                : String.join(", ", Settings.DEFAULT_IGNORE_COMPARE_FILES);
        ignoreExtArea = new JTextArea(currentIgnore, 3, 30);
        ignoreExtArea.setLineWrap(true);
        ignoreExtArea.setWrapStyleWord(true);
        ignoreExtArea.setFont(ignoreExtArea.getFont().deriveFont(Font.PLAIN, SyntaxStyleUtil.monoFont().getSize()-2));

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
        form.add(new JLabel("Enable secret detector (AI experimental):"), gbc);

        leakDetectorCheck = new JCheckBox("", currentLeakDetector);
        leakDetectorCheck.setToolTipText("Scan staged files for secrets words before each commit");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(leakDetectorCheck, gbc);

        // Branch compare description
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Branch compare description (AI experimental):"), gbc);

        branchCompareDescCheck = new JCheckBox("", currentBranchCompareDesc);
        branchCompareDescCheck.setToolTipText("Show AI descriptions when comparing branches");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(branchCompareDescCheck, gbc);

        // When either AI feature is enabled, verify Ollama is present
        leakDetectorCheck.addItemListener(e -> {
            if (leakDetectorCheck.isSelected() && !currentLeakDetector) {
                ensureOllamaOrRevert(leakDetectorCheck);
            }
        });
        branchCompareDescCheck.addItemListener(e -> {
            if (branchCompareDescCheck.isSelected() && !currentBranchCompareDesc) {
                ensureOllamaOrRevert(branchCompareDescCheck);
            }
        });

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
        Util.bindEscapeToDispose(this);
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
        settings.setEnableBranchCompareDescription(branchCompareDescCheck.isSelected());

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

    /**
     * If Ollama is not installed, shows a confirmation dialog. On confirmation,
     * downloads and installs Ollama (showing progress). If the user declines or
     * the download fails, reverts the checkbox to unchecked.
     */
    private void ensureOllamaOrRevert(JCheckBox checkbox) {
        if (OllamaManager.getStatus() != OllamaManager.Status.NOT_INSTALLED) return;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "This AI feature requires Ollama, which is not installed.\n\n" +
                "Gitember will download and install Ollama automatically.\n" +
                "This may take several minutes depending on your internet speed.\n\n" +
                "Download and install Ollama now?",
                "Ollama Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            checkbox.setSelected(false);
            return;
        }

        // Show a progress dialog while downloading
        JDialog progressDialog = new JDialog(this, "Installing Ollama", true);
        JLabel statusLabel  = new JLabel("Preparing download…");
        JProgressBar bar    = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setIndeterminate(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        p.add(statusLabel, BorderLayout.NORTH);
        p.add(bar,         BorderLayout.CENTER);
        progressDialog.getContentPane().add(p);
        progressDialog.pack();
        progressDialog.setMinimumSize(new Dimension(340, 100));
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        new SwingWorker<Void, String>() {
            private long total = -1;

            @Override
            protected Void doInBackground() throws Exception {
                publish("Downloading Ollama…");
                OllamaManager.download(
                        bytes -> {
                            if (total > 0) {
                                int pct = (int) (bytes * 100 / total);
                                SwingUtilities.invokeLater(() -> {
                                    bar.setIndeterminate(false);
                                    bar.setValue(pct);
                                    bar.setString(pct + "%");
                                });
                            }
                        },
                        t -> total = t
                );
                publish("Starting Ollama…");
                OllamaManager.startServerAndWait(30_000);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get(); // rethrow any exception
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SettingsDialog.this,
                            "Ollama installation failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    checkbox.setSelected(false);
                }
            }
        }.execute();

        progressDialog.setVisible(true); // blocks until done() calls dispose()
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
