package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.ui.StatusBar;
import dev.langchain4j.model.ollama.OllamaChatModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates an AI-written Markdown summary of the differences between two branches
 * using a locally running Ollama model via LangChain4j.
 *
 * <p>Before running, presents a configuration dialog for the Ollama URL and model name.
 * On completion, displays the result in a viewer window and offers to save it as a
 * {@code .md} file.</p>
 */
public class AiBranchCompareHandler extends AbstractAsyncHandler<String> {

    private static final Logger log = Logger.getLogger(AiBranchCompareHandler.class.getName());

    /** Default Ollama server URL. */
    public static final String DEFAULT_URL   = "http://localhost:11434";
    /** Default model name (adjust to whatever you have pulled). */
    public static final String DEFAULT_MODEL = "llama3.2";

    /** Max characters of unified diff to feed to the model (~8 k tokens). */
    private static final int MAX_DIFF_CHARS = 32_000;
    /** Max commit messages to include in the prompt. */
    private static final int MAX_COMMITS = 50;

    private final String branchARef;
    private final String branchBRef;
    private final String branchALabel;
    private final String branchBLabel;

    private String ollamaUrl;
    private String modelName;

    public AiBranchCompareHandler(Component parent, StatusBar statusBar,
                                   String branchARef, String branchALabel,
                                   String branchBRef, String branchBLabel) {
        super(parent, statusBar);
        this.branchARef   = branchARef;
        this.branchBRef   = branchBRef;
        this.branchALabel = branchALabel;
        this.branchBLabel = branchBLabel;
    }

    @Override
    protected String getOperationName() {
        return "AI compare " + branchALabel + " ↔ " + branchBLabel;
    }

    /**
     * Shows the Ollama configuration dialog; if confirmed, kicks off the background task.
     */
    public void showAndExecute() {
        OllamaConfigDialog dlg = new OllamaConfigDialog(
                parent instanceof Frame f ? f : (Frame) SwingUtilities.getWindowAncestor(parent));
        dlg.setVisible(true);
        if (!dlg.confirmed) return;
        this.ollamaUrl  = dlg.getUrl();
        this.modelName  = dlg.getModel();
        execute();
    }

    // ==========================================================================
    //  Background work
    // ==========================================================================

    @Override
    protected String doInBackground() throws Exception {
        var svc = Context.getGitRepoService();

        // 1. Commit log (headRef = branchB is "newer")
        List<String> commits = svc.getCommitLogBetween(branchARef, branchBRef, MAX_COMMITS);

        // 2. Unified diff text
        String diffText = svc.getBranchDiffText(branchARef, branchBRef, MAX_DIFF_CHARS);

        // 3. Build prompt
        String prompt = buildPrompt(branchALabel, branchBLabel, commits, diffText);

        // 4. Call Ollama
        statusBar.setStatus("Waiting for Ollama (" + modelName + ")…");
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(5))
                .build();

        return model.generate(prompt);
    }

    // ==========================================================================
    //  Success handler — show result window
    // ==========================================================================

    @Override
    protected void onSuccess(String markdown) {
        String title = "AI comparison: " + branchALabel + " ↔ " + branchBLabel;
        new AiResultWindow(
                parent instanceof Frame f ? f : (Frame) SwingUtilities.getWindowAncestor(parent),
                title, markdown, branchALabel, branchBLabel)
                .setVisible(true);
    }

    // ==========================================================================
    //  Prompt builder
    // ==========================================================================

    private static String buildPrompt(String branchA, String branchB,
                                      List<String> commits, String diffText) {
        StringBuilder sb = new StringBuilder(1024 + diffText.length());
        sb.append("You are an expert software engineer and code reviewer.\n");
        sb.append("Analyse the following git diff between branch **").append(branchA)
          .append("** (old) and branch **").append(branchB).append("** (new).\n\n");

        if (!commits.isEmpty()) {
            sb.append("## Commits unique to ").append(branchB).append("\n\n");
            for (String msg : commits) sb.append("- ").append(msg).append('\n');
            sb.append('\n');
        }

        sb.append("## Unified diff\n\n```diff\n");
        sb.append(diffText);
        sb.append("\n```\n\n");

        sb.append("""
                ## Your task

                Produce a concise Markdown report with the following sections:

                ### Overview
                One short paragraph summarising the overall purpose / theme of the changes.

                ### Files changed
                A table with columns: File | Change type | Short description.

                ### Key changes
                Bullet list of the most significant code-level changes.

                ### Potential impact
                Any risks, breaking changes, or areas that deserve close review.

                Write the report now. Use only Markdown. Do not add any preamble.
                """);
        return sb.toString();
    }

    // ==========================================================================
    //  Inner: Ollama setup + configuration dialog
    // ==========================================================================

    private static class OllamaConfigDialog extends JDialog {

        boolean confirmed = false;
        private final JTextField modelField;

        // Status row
        private JLabel  statusLabel;
        private JButton downloadBtn;
        private JButton startBtn;
        private JProgressBar downloadProgress;
        private JLabel  downloadStatusLabel;

        // Model row
        private JLabel  modelStatusLabel;
        private JButton pullBtn;
        private JTextArea pullOutputArea;
        private JProgressBar pullProgress;

        // Bottom
        private JButton compareBtn;

        OllamaConfigDialog(Frame owner) {
            super(owner, "AI compare – Ollama setup", true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            modelField = new JTextField(DEFAULT_MODEL, 22);

            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
            content.add(buildOllamaPanel(), BorderLayout.NORTH);
            content.add(buildModelPanel(),  BorderLayout.CENTER);

            JButton cancelBtn = new JButton("Cancel");
            compareBtn = new JButton("Compare");
            cancelBtn.addActionListener(e -> dispose());
            compareBtn.addActionListener(e -> { confirmed = true; dispose(); });
            getRootPane().setDefaultButton(compareBtn);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            buttons.add(cancelBtn);
            buttons.add(compareBtn);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);
            getContentPane().add(buttons, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(520, getHeight()));
            setLocationRelativeTo(owner);

            refreshStatus();
        }

        // ------------------------------------------------------------------
        //  Panel builders
        // ------------------------------------------------------------------

        private JPanel buildOllamaPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createTitledBorder("Ollama server"));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(3, 4, 3, 4);
            g.anchor = GridBagConstraints.WEST;

            // Row 0: status label + action buttons
            statusLabel = new JLabel("Checking…");
            g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            p.add(statusLabel, g);

            downloadBtn = new JButton("Download & Install");
            downloadBtn.setVisible(false);
            downloadBtn.addActionListener(e -> doDownload());
            g.gridx = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            p.add(downloadBtn, g);

            startBtn = new JButton("Start Ollama");
            startBtn.setVisible(false);
            startBtn.addActionListener(e -> doStart());
            g.gridx = 2;
            p.add(startBtn, g);

            // Row 1: download progress (hidden until download starts)
            downloadProgress = new JProgressBar(0, 100);
            downloadProgress.setVisible(false);
            downloadProgress.setStringPainted(true);
            downloadStatusLabel = new JLabel();
            downloadStatusLabel.setVisible(false);

            g.gridx = 0; g.gridy = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(downloadProgress, g);

            g.gridy = 2;
            p.add(downloadStatusLabel, g);

            return p;
        }

        private JPanel buildModelPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createTitledBorder("Model"));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(3, 4, 3, 4);
            g.anchor = GridBagConstraints.WEST;

            g.gridx = 0; g.gridy = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            p.add(new JLabel("Model name:"), g);

            g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(modelField, g);

            pullBtn = new JButton("Pull model");
            pullBtn.addActionListener(e -> doPullModel());
            g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            p.add(pullBtn, g);

            modelStatusLabel = new JLabel();
            g.gridx = 0; g.gridy = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(modelStatusLabel, g);

            pullProgress = new JProgressBar();
            pullProgress.setIndeterminate(true);
            pullProgress.setVisible(false);
            g.gridy = 2;
            p.add(pullProgress, g);

            pullOutputArea = new JTextArea(4, 40);
            pullOutputArea.setEditable(false);
            pullOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            JScrollPane sp = new JScrollPane(pullOutputArea);
            sp.setVisible(false);
            pullOutputArea.putClientProperty("scrollPane", sp);
            g.gridy = 3; g.fill = GridBagConstraints.BOTH; g.weighty = 1;
            p.add(sp, g);

            return p;
        }

        // ------------------------------------------------------------------
        //  Status refresh
        // ------------------------------------------------------------------

        private void refreshStatus() {
            SwingWorker<OllamaManager.Status, Void> worker = new SwingWorker<>() {
                @Override protected OllamaManager.Status doInBackground() {
                    return OllamaManager.getStatus();
                }
                @Override protected void done() {
                    try { applyStatus(get()); } catch (Exception ex) { applyStatus(OllamaManager.Status.NOT_INSTALLED); }
                }
            };
            worker.execute();
        }

        private void applyStatus(OllamaManager.Status status) {
            switch (status) {
                case RUNNING -> {
                    statusLabel.setText("<html><font color='green'>&#9679; Ollama is running</font></html>");
                    downloadBtn.setVisible(false);
                    startBtn.setVisible(false);
                    refreshModelStatus();
                }
                case STOPPED -> {
                    statusLabel.setText("<html><font color='orange'>&#9679; Ollama is installed but not running</font></html>");
                    downloadBtn.setVisible(false);
                    startBtn.setVisible(true);
                    setCompareEnabled(false);
                }
                case NOT_INSTALLED -> {
                    statusLabel.setText("<html><font color='red'>&#9679; Ollama is not installed</font></html>");
                    downloadBtn.setVisible(true);
                    startBtn.setVisible(false);
                    setCompareEnabled(false);
                }
            }
            pack();
        }

        private void refreshModelStatus() {
            String model = modelField.getText().trim();
            if (model.isEmpty()) { setCompareEnabled(false); return; }
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return OllamaManager.isModelAvailable(model); }
                @Override protected void done() {
                    try {
                        boolean avail = get();
                        modelStatusLabel.setText(avail
                                ? "<html><font color='green'>Model \"" + model + "\" is available locally.</font></html>"
                                : "<html><font color='orange'>Model \"" + model + "\" is not pulled yet. Click \"Pull model\".</font></html>");
                        setCompareEnabled(avail);
                    } catch (Exception ex) { setCompareEnabled(false); }
                }
            }.execute();
        }

        private void setCompareEnabled(boolean enabled) {
            compareBtn.setEnabled(enabled);
        }

        // ------------------------------------------------------------------
        //  Actions: download, start, pull
        // ------------------------------------------------------------------

        private void doDownload() {
            downloadBtn.setEnabled(false);
            downloadProgress.setVisible(true);
            downloadStatusLabel.setVisible(true);
            downloadStatusLabel.setText("Starting download…");
            pack();

            new SwingWorker<Void, String>() {
                @Override protected Void doInBackground() throws Exception {
                    final long[] totalRef = {-1};
                    OllamaManager.download(
                            downloaded -> SwingUtilities.invokeLater(() -> {
                                long mb = downloaded / (1024 * 1024);
                                downloadStatusLabel.setText("Downloaded " + mb + " MB…");
                                if (totalRef[0] > 0) {
                                    downloadProgress.setValue((int)(downloaded / 1024));
                                }
                            }),
                            total -> {
                                totalRef[0] = total;
                                SwingUtilities.invokeLater(() -> {
                                    if (total > 0) {
                                        downloadProgress.setMaximum((int)(total / 1024));
                                        downloadProgress.setIndeterminate(false);
                                    } else {
                                        downloadProgress.setIndeterminate(true);
                                    }
                                });
                            }
                    );
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        downloadStatusLabel.setText("Download complete. Starting Ollama…");
                        doStart();
                    } catch (Exception ex) {
                        downloadStatusLabel.setText("Download failed: " + ex.getMessage());
                        downloadBtn.setEnabled(true);
                    }
                }
            }.execute();
        }

        private void doStart() {
            startBtn.setEnabled(false);
            statusLabel.setText("<html><font color='orange'>&#9679; Starting Ollama…</font></html>");

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    OllamaManager.startServerAndWait(30_000);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        applyStatus(OllamaManager.Status.RUNNING);
                    } catch (Exception ex) {
                        statusLabel.setText("<html><font color='red'>&#9679; Failed to start Ollama: " + ex.getMessage() + "</font></html>");
                        startBtn.setEnabled(true);
                    }
                }
            }.execute();
        }

        private void doPullModel() {
            String model = modelField.getText().trim();
            if (model.isEmpty()) return;

            pullBtn.setEnabled(false);
            modelField.setEnabled(false);
            pullProgress.setVisible(true);
            JScrollPane sp = (JScrollPane) pullOutputArea.getClientProperty("scrollPane");
            sp.setVisible(true);
            pullOutputArea.setText("");
            modelStatusLabel.setText("Pulling model \"" + model + "\"…");
            pack();

            new SwingWorker<Void, String>() {
                @Override protected Void doInBackground() throws Exception {
                    Process proc = OllamaManager.startModelPull(model);
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(proc.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            publish(line);
                        }
                    }
                    proc.waitFor();
                    return null;
                }
                @Override protected void process(java.util.List<String> chunks) {
                    for (String line : chunks) pullOutputArea.append(line + "\n");
                    pullOutputArea.setCaretPosition(pullOutputArea.getDocument().getLength());
                }
                @Override protected void done() {
                    pullProgress.setVisible(false);
                    pullBtn.setEnabled(true);
                    modelField.setEnabled(true);
                    try {
                        get();
                        refreshModelStatus();
                    } catch (Exception ex) {
                        modelStatusLabel.setText("<html><font color='red'>Pull failed: " + ex.getMessage() + "</font></html>");
                    }
                }
            }.execute();
        }

        String getUrl()   { return DEFAULT_URL; }
        String getModel() { return modelField.getText().trim(); }
    }

    // ==========================================================================
    //  Inner: result viewer window
    // ==========================================================================

    private static class AiResultWindow extends JDialog {

        AiResultWindow(Frame owner, String title, String markdown,
                       String branchA, String branchB) {
            super(owner, title, false);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JTextArea textArea = new JTextArea(markdown);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setCaretPosition(0);
            JScrollPane scroll = new JScrollPane(textArea);

            JButton saveBtn  = new JButton("Save as .md…");
            JButton closeBtn = new JButton("Close");

            saveBtn.addActionListener(e -> saveMarkdown(this, markdown, branchA, branchB));
            closeBtn.addActionListener(e -> dispose());

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(saveBtn);
            buttons.add(closeBtn);

            getContentPane().setLayout(new BorderLayout(0, 4));
            getContentPane().add(scroll,  BorderLayout.CENTER);
            getContentPane().add(buttons, BorderLayout.SOUTH);

            setSize(820, 600);
            setLocationRelativeTo(owner);
        }

        private static void saveMarkdown(Component parent, String markdown,
                                         String branchA, String branchB) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save AI comparison report");
            String ts  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
            String safe = (branchA + "_vs_" + branchB)
                    .replaceAll("[^a-zA-Z0-9._-]", "_")
                    .replaceAll("_+", "_");
            fc.setSelectedFile(new File("ai-compare-" + safe + "-" + ts + ".md"));
            if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".md")) f = new File(f.getAbsolutePath() + ".md");
            try {
                Files.writeString(f.toPath(), markdown, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(parent,
                        "Saved: " + f.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot save report", ex);
                JOptionPane.showMessageDialog(parent,
                        "Cannot save:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
