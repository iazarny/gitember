package com.az.gitember.dialog;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.ui.misc.Util;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class SettingsDialog extends JDialog {

    private  JTextField   userName ;
    private  JTextField   userEmail ;

    private  JComboBox<String> signCombo;
    private  JLabel keyOrPAth;
    private  JButton browseBtn;
    private  JTextField   signKey ;
    private  JLabel singCommitLabel;
    private  JCheckBox  signCommit;
    private  JLabel singTagLabel;

    private  JCheckBox  signTag;

    private  JComboBox<String> themeCombo;
    private  JSpinner fontSizeSpinner;
    private  JTextArea ignoreExtArea;
    private  JCheckBox leakDetectorCheck;
    private  JCheckBox branchCompareDescCheck;
    private  JCheckBox commitMsgGenCheck;
    private  JComboBox<String> llmModelCombo;

    /**
     * Offered in the model combo, which stays editable — any tag pulled in Ollama works.
     * The first entry is {@link Settings#DEFAULT_LLM_DETECTOR_MODEL}.
     */
    private static final String[] SUGGESTED_LLM_MODELS = {
            Settings.DEFAULT_LLM_DETECTOR_MODEL,
            "qwen2.5-coder:14b",
            "deepseek-coder-v2",
            "codellama",
            "llama3.2",
            "mistral"
    };

    private final Settings settings;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        setSize(640, 480);
        setLocationRelativeTo(owner);
        setResizable(true);

        settings = Context.getSettings();


        // Show the stored ignore list (defaults are seeded at startup, so this is always populated)
        String currentIgnore = settings != null
                ? String.join(", ", settings.getIgnoreCompareFiles())
                : String.join(", ", Settings.DEFAULT_IGNORE_COMPARE_FILES);
        ignoreExtArea = new JTextArea(currentIgnore, 3, 30);
        ignoreExtArea.setLineWrap(true);
        ignoreExtArea.setWrapStyleWord(true);
        //ignoreExtArea.setFont(ignoreExtArea.getFont().deriveFont(Font.PLAIN, SyntaxStyleUtil.monoFont().getSize() - 2));

        JTabbedPane tabbedPane = new JTabbedPane();


        tabbedPane.addTab("Common", createCommonPanel());
        tabbedPane.addTab("UI", createUIPanel());
        tabbedPane.addTab("Commit Singing", createComminSignPanel());
        tabbedPane.addTab("AI", createAIPanel());
        tabbedPane.addTab("Other", createComparePanel());

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        //form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));


        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> applyAndClose());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okBtn);
        Util.bindEscapeToDispose(this);
    }

    private JPanel createComparePanel() {

        GridBagConstraints gbc = createGridBagConstraints();

        JPanel comparePanel = new JPanel(new GridBagLayout());

        // 1. Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span both columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        comparePanel.add(new JLabel("Ignore extensions (folder compare):"), gbc);

        // 2. Scrollable Edit Area (stretches horizontally and vertically)
        JScrollPane ignoreScroll = new JScrollPane(ignoreExtArea);
        ignoreScroll.setPreferredSize(new Dimension(200, 64)); // Preferred height: 64px, flexible width

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Span across column 0 and column 1
        gbc.fill = GridBagConstraints.BOTH; // Expand in both directions
        gbc.weightx = 1.0; // Fill horizontal space
        gbc.weighty = 0.3; // Give it a fixed proportion of vertical space
        comparePanel.add(ignoreScroll, gbc);

        // 3. Reset Button (aligned to bottom-right)
        JButton resetBtn = new JButton("Reset to defaults");
        resetBtn.addActionListener(e -> ignoreExtArea.setText(
                String.join(", ", Settings.DEFAULT_IGNORE_COMPARE_FILES)));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        comparePanel.add(resetBtn, gbc);

        // 4. Spacer (pushes everything to the top)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        comparePanel.add(new JLabel(), gbc);

        return comparePanel;
    }

    private JPanel createAIPanel() {

        boolean currentLeakDetector = settings == null || !Boolean.FALSE.equals(settings.getEnableLeakDetector());
        boolean currentBranchCompareDesc = settings != null && Boolean.TRUE.equals(settings.getEnableBranchCompareDescription());
        boolean currentCommitMsgGen = settings != null && Boolean.TRUE.equals(settings.getEnableCommitMessageGeneration());
        String currentLlmModel = settings != null
                ? settings.getLlmDetectorModel() : Settings.DEFAULT_LLM_DETECTOR_MODEL;


        //////////////////////////////  AI
        JPanel aiPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        // Leak detector
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        aiPanel.add(new JLabel("Enable secret detector (AI experimental):"), gbc);

        leakDetectorCheck = new JCheckBox("", currentLeakDetector);
        leakDetectorCheck.setToolTipText("Scan staged files for secrets words before each commit");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        aiPanel.add(leakDetectorCheck, gbc);

        // Branch compare description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        aiPanel.add(new JLabel("Branch compare description (AI experimental):"), gbc);

        branchCompareDescCheck = new JCheckBox("", currentBranchCompareDesc);
        branchCompareDescCheck.setToolTipText("Show AI descriptions when comparing branches");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        aiPanel.add(branchCompareDescCheck, gbc);

        // Commit message generation
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        aiPanel.add(new JLabel("AI commit message generation (AI experimental):"), gbc);

        commitMsgGenCheck = new JCheckBox("", currentCommitMsgGen);
        commitMsgGenCheck.setToolTipText("Generate commit message suggestions in the commit dialog");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        aiPanel.add(commitMsgGenCheck, gbc);

        // Model shared by all three features above
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        aiPanel.add(new JLabel("Ollama model:"), gbc);

        llmModelCombo = new JComboBox<>(SUGGESTED_LLM_MODELS);
        llmModelCombo.setEditable(true);
        llmModelCombo.setSelectedItem(currentLlmModel);
        llmModelCombo.setToolTipText("Model the features above ask Ollama for. Any pulled model "
                + "can be typed in; leave it empty to use " + Settings.DEFAULT_LLM_DETECTOR_MODEL + ".");
        llmModelCombo.setPreferredSize(new Dimension(220, 25));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        aiPanel.add(llmModelCombo, gbc);

        // When any AI feature is enabled, verify Ollama is present
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
        commitMsgGenCheck.addItemListener(e -> {
            if (commitMsgGenCheck.isSelected() && !currentCommitMsgGen) {
                ensureOllamaOrRevert(commitMsgGenCheck);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        aiPanel.add(new JLabel(), gbc);
        return aiPanel;
    }



    private JPanel createComminSignPanel() {

        JPanel comminSiggPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();



        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        comminSiggPanel.add(new JLabel("Sign option:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        signCombo = new JComboBox<>(new String[]{
                Settings.SignOption.NONE.getOption() ,
                Settings.SignOption.PGP.getOption() ,
                Settings.SignOption.SSH.getOption()});
        signCombo.setSelectedItem(settings.getSignOption());

        signCombo.setPreferredSize(new java.awt.Dimension(120, 25));
        signCombo.addItemListener(
                e -> {
                    String item = e.getItem().toString();

                    setVisibility(true);

                    if (Settings.SignOption.NONE.getOption().equalsIgnoreCase(item)) {
                        setVisibility(false);

                        signCommit.setSelected(false);
                        signTag.setSelected(false);

                    } else if (Settings.SignOption.PGP.getOption().equalsIgnoreCase(item)) {
                        keyOrPAth.setText("Signing key:");
                        browseBtn.setVisible(false);
                    } else if (Settings.SignOption.SSH.getOption().equalsIgnoreCase(item)) {
                        keyOrPAth.setText("Path to pub key:");
                        browseBtn.setVisible(true);
                    }
                }
        );



        comminSiggPanel.add(signCombo, gbc);


        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        keyOrPAth = new JLabel("Sign key:");
        comminSiggPanel.add(keyOrPAth, gbc);


        gbc.gridy = 1;
        gbc.gridx = 1;
        JPanel destPanel = new JPanel(new BorderLayout(5, 0));
        signKey = new JTextField(25);
        signKey.setText(settings.getSignKey());
        browseBtn = new JButton("...");
        browseBtn.addActionListener(e -> browseKey());
        destPanel.add(signKey, BorderLayout.CENTER);
        destPanel.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1;
        comminSiggPanel.add(destPanel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        singCommitLabel = new JLabel("Sign commits:");
        comminSiggPanel.add(singCommitLabel, gbc);


        gbc.gridy = 2;
        gbc.gridx = 1;
        signCommit = new JCheckBox("", settings.getSignCommit());
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        comminSiggPanel.add(signCommit, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        singTagLabel = new JLabel("Sign tag:");
        comminSiggPanel.add(singTagLabel, gbc);


        gbc.gridy = 3;
        gbc.gridx = 1;
        signTag = new JCheckBox("", settings.getSignTag());
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        comminSiggPanel.add(signTag, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        comminSiggPanel.add(new JLabel(), gbc);


        if (Settings.SignOption.NONE.getOption().equalsIgnoreCase(settings.getSignOption())) {
            setVisibility(false);

        }
        if (Settings.SignOption.PGP.getOption().equalsIgnoreCase(settings.getSignOption())) {
            keyOrPAth.setText("Signing key:");
            browseBtn.setVisible(false);
        } else if (Settings.SignOption.SSH.getOption().equalsIgnoreCase(settings.getSignOption())) {
            keyOrPAth.setText("Path to pub key:");
            browseBtn.setVisible(true);
        }


        return comminSiggPanel;

    }


    private void setVisibility(boolean visible) {
        singCommitLabel.setVisible(visible);
        singTagLabel.setVisible(visible);
        keyOrPAth.setVisible(visible);
        browseBtn.setVisible(visible);
        signCommit.setVisible(visible);
        signTag.setVisible(visible);
        signKey.setVisible(visible);
    }
    private void browseKey() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle("Select Key");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            signKey.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }


    /*// Access the global Git configuration (~/.gitconfig)
            StoredConfig globalConfig = SystemReader.getInstance().openUserConfig(null, SystemReader.getInstance().getFS());

            // Load existing settings
            globalConfig.load();

            // Set user.name and user.email under the "user" section
            globalConfig.setString("user", null, "name", "Your Name");
            globalConfig.setString("user", null, "email", "your.email@example.com");

            // Save the changes back to the global file
            globalConfig.save();

            System.out.println("Global Git user.name and user.email updated successfully.");*/

    private JPanel createCommonPanel() {
        JPanel commonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        // Row 0: Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Fixed width for labels column
        commonPanel.add(new JLabel("User name:"), gbc);

        // Row 0: Text Field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Pushes content to the left by taking all horizontal extra space
        userName = new JTextField();
        userName.setPreferredSize(new java.awt.Dimension(250, 25));
        commonPanel.add(userName, gbc);

        // Row 1: Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        commonPanel.add(new JLabel("User email:"), gbc);

        // Row 1: Text Field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0; // Consistently push to left on subsequent rows
        userEmail = new JTextField();
        userEmail.setPreferredSize(new java.awt.Dimension(250, 25));
        commonPanel.add(userEmail, gbc);

        // Row 2: Vertical spacer pushing components to the top
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 1.0; // Absorbs vertical extra space
        commonPanel.add(new JLabel(), gbc);


        StoredConfig globalConfig = SystemReader.getInstance().openUserConfig(null, FS.detect());

        try {
            globalConfig.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        userName.setText(globalConfig.getString("user", null, "name"));
        userEmail.setText(globalConfig.getString("user", null, "email"));

        return commonPanel;
    }


    private JPanel createUIPanel() {

        String currentTheme = settings != null && "dark".equalsIgnoreCase(settings.getTheme()) ? "Dark" : "Light";
        int currentFontSize = settings != null ? settings.getFontSize() : 13;
        if (currentFontSize <= 0) currentFontSize = 13;

        JPanel uiPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = createGridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        uiPanel.add(new JLabel("Theme:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setSelectedItem(currentTheme);
        themeCombo.setPreferredSize(new java.awt.Dimension(120, 25));
        uiPanel.add(themeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        uiPanel.add(new JLabel("Font size:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(currentFontSize, 8, 36, 1));
        fontSizeSpinner.setPreferredSize(new java.awt.Dimension(120, 25));
        uiPanel.add(fontSizeSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        uiPanel.add(new JLabel(), gbc);

        return uiPanel;
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
        settings.setEnableCommitMessageGeneration(commitMsgGenCheck.isSelected());

        // An empty entry means "use the default", which is what the getter falls back to.
        Object selectedModel = llmModelCombo.getSelectedItem();
        String llmModel = selectedModel != null ? selectedModel.toString().trim() : "";
        settings.setLlmDetectorModel(llmModel.isEmpty() ? null : llmModel);

        settings.setSignOption(signCombo.getSelectedItem().toString());
        settings.setSignKey(signKey.getText());
        settings.setSignCommit(signCommit.isSelected());
        settings.setSignTag(signTag.isSelected());

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


        //-------------------------
        StoredConfig globalConfig = SystemReader.getInstance().openUserConfig(null, FS.detect());

        try {
            globalConfig.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set user.name and user.email under the "user" section
        globalConfig.setString("user", null, "name", userName.getText());
        globalConfig.setString("user", null, "email", userEmail.getText());

        // Save the changes back to the global file
        try {
            globalConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        JLabel statusLabel = new JLabel("Preparing download…");
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setIndeterminate(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        p.add(statusLabel, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
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
                //publish("Starting Ollama…");
                //OllamaManager.startServerAndWait(30_000);
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
                    get();
                    publish("Starting Ollama…");
                    OllamaManager.startServerAndWait(30_000);
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof OllamaManager.ChecksumMismatchException) {
                        JOptionPane.showMessageDialog(SettingsDialog.this,
                                "Ollama checksum verification failed.\n" +
                                        "The downloaded file may be corrupt or tampered with.\n\n" +
                                        "All AI features have been disabled for security.",
                                "Checksum Verification Failed", JOptionPane.ERROR_MESSAGE);
                        disableAllAiFeatures();
                    } else {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(SettingsDialog.this,
                                "Ollama installation failed:\n" + msg,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        checkbox.setSelected(false);
                    }
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

    private void disableAllAiFeatures() {
        leakDetectorCheck.setSelected(false);
        branchCompareDescCheck.setSelected(false);
        commitMsgGenCheck.setSelected(false);
        Settings settings = Context.getSettings();
        if (settings != null) {
            settings.setEnableLeakDetector(false);
            settings.setEnableBranchCompareDescription(false);
            settings.setEnableCommitMessageGeneration(false);
            Context.saveSettings();
        }
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

    private GridBagConstraints  createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        // Anchor all components to the top-left of their cells
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new java.awt.Insets(10,10,10,10); // Optional: add clean padding between controls
        return gbc;
    }
}
