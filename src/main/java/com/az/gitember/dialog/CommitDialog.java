package com.az.gitember.dialog;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.service.LlmCommitMessageService;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.service.detector.DetectorService;
import com.az.gitember.service.detector.FileType;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import com.az.gitember.ui.FileViewerWindow;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.misc.Util;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.ResolveMerger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommitDialog extends JDialog {

    private static final Logger log = Logger.getLogger(CommitDialog.class.getName());

    private static final java.util.Set<String> STAGED_STATUSES = java.util.Set.of(
            ScmItem.Status.ADDED,
            ScmItem.Status.CHANGED,
            ScmItem.Status.REMOVED,
            ScmItem.Status.RENAMED
    );

    private final CommitMessagePanel commitMessagePanel;
    /** Snapshot of the workspace's projects, in the same order as {@link #commitMessagePanel}'s per-project tabs. Non-null only in workspace-active (dashboard) mode. */
    private final List<Project> workspaceProjects;
    private final JTable filesTable;
    private final DefaultTableModel tableModel;
    private final DefaultTableModel findingsModel;
    private final JPanel findingsPanel;
    private final List<Finding> findings = new ArrayList<>();

    // Scan progress UI
    private final JLabel       scanStatusLabel;
    private final JProgressBar scanProgress;
    private final JPanel       scanStatusPanel;




    public CommitDialog(Frame parent) {
        super(parent,  getDialogTitle(),
                java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        setMinimumSize(new Dimension(800, 400));
        setSize(800, 580);
        setLocationRelativeTo(parent);

        // Files table
        final String[] cols;
        if (Context.isWorkspaceMode()) {
            cols = new String[]{"Repo", "Status", "File"};
        } else {
            cols = new String[]{"Status", "File"};
        }
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        filesTable = new JTable(tableModel);
        if (Context.isWorkspaceMode()) {
            filesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            filesTable.getColumnModel().getColumn(0).setMaxWidth(300);
            filesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            filesTable.getColumnModel().getColumn(1).setMaxWidth(150);
        } else {
            filesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            filesTable.getColumnModel().getColumn(0).setMaxWidth(150);
        }


        populateFiles();

        JScrollPane tableScroll = new JScrollPane(filesTable);
        tableScroll.setPreferredSize(new Dimension(0, 160));

        // Message area: common message, plus one per project in workspace-active mode
        boolean workSpaceActive = MainFrame.getInstance().isWorkspaceActive();
        if (workSpaceActive) {
            workspaceProjects = new ArrayList<>(Context.getWorkspace().getProjects());
            String[] names = workspaceProjects.stream().map(CommitDialog::projectLabel).toArray(String[]::new);
            commitMessagePanel = new CommitMessagePanel(names);
        } else {
            workspaceProjects = null;
            commitMessagePanel = new CommitMessagePanel(null);
        }

        // Scan status panel (shown while LLM scan is in progress)
        scanStatusLabel = new JLabel("Scanning for secrets…");
        scanStatusLabel.setFont(scanStatusLabel.getFont().deriveFont(Font.ITALIC));
        scanProgress = new JProgressBar();
        scanProgress.setIndeterminate(true);
        scanProgress.setPreferredSize(new Dimension(0, 6));
        scanStatusPanel = new JPanel(new BorderLayout(4, 2));
        scanStatusPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        scanStatusPanel.add(scanProgress,    BorderLayout.NORTH);
        scanStatusPanel.add(scanStatusLabel, BorderLayout.CENTER);
        scanStatusPanel.setVisible(false);

        // Findings table (hidden until results arrive)
        findingsModel = new DefaultTableModel(new String[]{"File", "Line", "Type", "Confidence", "Details", ""}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
            @Override public Class<?> getColumnClass(int col) { return col == 5 ? JButton.class : Object.class; }
        };
        JTable findingsTable = new JTable(findingsModel);
        findingsTable.setRowHeight(findingsTable.getRowHeight() + 4);
        findingsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        findingsTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        findingsTable.getColumnModel().getColumn(1).setMaxWidth(60);
        findingsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        findingsTable.getColumnModel().getColumn(2).setMaxWidth(140);
        findingsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        findingsTable.getColumnModel().getColumn(3).setMaxWidth(90);
        findingsTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        findingsTable.getColumnModel().getColumn(5).setMaxWidth(70);
        findingsTable.setDefaultRenderer(Object.class, new FindingsCellRenderer());
        findingsTable.getColumnModel().getColumn(5).setCellRenderer(new OpenButtonRenderer());
        findingsTable.getColumnModel().getColumn(5).setCellEditor(new OpenButtonEditor(findingsTable));
        findingsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = findingsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) openFinding(row);
                }
            }
        });

        JScrollPane findingsScroll = new JScrollPane(findingsTable);
        findingsScroll.setPreferredSize(new Dimension(0, 120));

        JLabel findingsLabel = new JLabel("⚠ Potential secrets / sensitive data detected:");
        findingsLabel.setName("findingsLabel");
        findingsLabel.setForeground(SyntaxStyleUtil.statusColor("DELETE"));
        findingsLabel.setFont(findingsLabel.getFont().deriveFont(Font.BOLD));

        findingsPanel = new JPanel(new BorderLayout(3, 3));
        findingsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        findingsPanel.add(findingsLabel, BorderLayout.NORTH);
        findingsPanel.add(findingsScroll, BorderLayout.CENTER);
        findingsPanel.setVisible(false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton commitBtn = new JButton("Commit");
        commitBtn.setName("commitButton");
        commitBtn.addActionListener(e -> onCommit());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setName("cancelButton");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(commitBtn);
        buttonPanel.add(cancelBtn);

        // Layout
        JPanel southOfMessage = new JPanel(new BorderLayout());
        southOfMessage.add(scanStatusPanel, BorderLayout.NORTH);
        southOfMessage.add(findingsPanel,   BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(commitMessagePanel, BorderLayout.CENTER);
        messagePanel.add(southOfMessage,      BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        mainPanel.add(tableScroll,   BorderLayout.NORTH);
        mainPanel.add(messagePanel,  BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel,    BorderLayout.CENTER);
        getContentPane().add(buttonPanel,  BorderLayout.SOUTH);

        getRootPane().setDefaultButton(commitBtn);
        Util.bindEscapeToDispose(this);

        // Run features sequentially: commit message generation first, then leak detection.
        // In workspace mode both are aggregated across every project's staged changes (see
        // buildStagedDiffText() / collectFilesToScan()); the generated message is applied as the
        // single common message, used as the fallback for any project without its own message.
        SwingUtilities.invokeLater(() -> {
            if (isCommitMessageGenEnabled()) {
                startCommitMessageGeneration(isLeakDetectorEnabled());
            } else {
                startDetector();
            }
        });
    }

    // -------------------------------------------------------------------------

    private static String getDialogTitle() {
        final String titleTail;
        if (MainFrame.getInstance().isWorkspaceActive()) {
            titleTail = Context.getWorkspace().getName();
        } else {
            titleTail = Context.getWorkingBranch().getShortName();
        }
        return "Commit ["+titleTail+"]";
    }

    private void populateFiles() {
        tableModel.setRowCount(0);

        if (MainFrame.getInstance().isWorkspaceActive()) {
            populateWorkspaceFiles();
        } else {
            populateSingleRepoFiles(Context.getStatusList());
        }
    }

    /** Staged files of the currently open repository (non-workspace mode). */
    private void populateSingleRepoFiles(List<ScmItem> items) {
        for (ScmItem item : items) {
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            if (STAGED_STATUSES.contains(status)) {
                tableModel.addRow(new Object[]{ status, item.getShortName() });
            }
        }
    }

    /**
     * Staged files across every project in the current workspace. Each project's repository is
     * read via a throwaway {@link GitRepoService}; rows are {@code [repo, status, file]} matching
     * the workspace-mode column layout.
     */
    private void populateWorkspaceFiles() {
        Workspace workspace = Context.getWorkspace();
        if (workspace == null) return;

        for (Project project : workspace.getProjects()) {
            String home = project.getProjectHomeFolder();
            String repoName = new java.io.File(home).getName();

            try {
                GitRepoService svc = project.getGitRepoService();
                List<ScmItem> items = svc.getStatuses(null);
                for (ScmItem item : items) {
                    String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                    if (STAGED_STATUSES.contains(status)) {
                        tableModel.addRow(new Object[]{ repoName, status, item.getShortName() });
                    }
                }
            } catch (Exception ex) {
                log.warning("Cannot read staged files for " + home + ": " + ex.getMessage());
            }
        }
    }

    private boolean isLeakDetectorEnabled() {
        return Context.getSettings() != null
                && Boolean.TRUE.equals(Context.getSettings().getEnableLeakDetector());
    }

    private String llmModel() {
        return Context.getSettings() != null
                ? Context.getSettings().getLlmDetectorModel()
                : "qwen2.5-coder";
    }

    private boolean isCommitMessageGenEnabled() {
        return Context.getSettings() != null
                && Boolean.TRUE.equals(Context.getSettings().getEnableCommitMessageGeneration());
    }

    // -------------------------------------------------------------------------
    //  AI commit message generation
    // -------------------------------------------------------------------------

    private void startCommitMessageGeneration(boolean chainToDetector) {

        if (isCommitMessageGenEnabled())  {
            String model = llmModel();

            applyAiSuggestion("…");
            scanStatusLabel.setText("Generating commit message…");
            scanStatusPanel.setVisible(true);

            new SwingWorker<String, String>() {
                @Override
                protected String doInBackground() throws Exception {
                    OllamaManager.Status status = OllamaManager.getStatus();
                    log.info("AI commit msg: Ollama status = " + status);

                    if (status == OllamaManager.Status.STOPPED) {
                        publish("Starting Ollama…");
                        OllamaManager.startServerAndWait(20_000);
                        status = OllamaManager.Status.RUNNING;
                    }
                    if (status != OllamaManager.Status.RUNNING) {
                        throw new IllegalStateException("Ollama not available (status: " + status + ")");
                    }
                    if (!OllamaManager.isModelAvailable(model)) {
                        log.info("AI commit msg: pulling model " + model);
                        publish("Pulling model \"" + model + "\" (first run, please wait)…");
                        Process pull = OllamaManager.startModelPull(model);
                        pull.waitFor();
                    }
                    if (!OllamaManager.isRunning() || !OllamaManager.isModelAvailable(model)) {
                        throw new IllegalStateException("Model '" + model + "' not available after pull");
                    }
                    publish("Generating commit message…");
                    String diff = buildStagedDiffText();
                    log.info("AI commit msg: diff length = " + (diff != null ? diff.length() : 0));
                    return LlmCommitMessageService.generate(diff, null, OllamaManager.BASE_URL, model);
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        scanStatusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }

                @Override
                protected void done() {
                    try {
                        String suggestion = get();
                        if (suggestion != null && !suggestion.isBlank()) {
                            applyAiSuggestion(suggestion.trim());
                        } else {
                            clearAiSuggestion();
                        }
                    } catch (Exception ex) {
                        log.warning("AI commit message generation failed: " + ex.getMessage());
                        clearAiSuggestion();
                    }
                    if (chainToDetector) {
                        startDetector();
                    } else {
                        scanStatusPanel.setVisible(false);
                    }
                }
            }.execute();
        }


    }

    private void applyAiSuggestion(String suggestion) {
        commitMessagePanel.setMessage(suggestion);
    }

    private void clearAiSuggestion() {
        commitMessagePanel.setMessage("");
    }

    /**
     * Staged diff text to feed the LLM. In single-repository mode it's the current repo's diff;
     * in workspace mode it's every staged project's diff concatenated under a repo header, so a
     * single common commit message can be generated for the whole workspace.
     */
    private String buildStagedDiffText() throws Exception {
        if (workspaceProjects == null) {
            return Context.getGitRepoService() != null
                    ? Context.getGitRepoService().getStagedDiffText(LlmCommitMessageService.MAX_DIFF_CHARS)
                    : null;
        }
        StringBuilder sb = new StringBuilder();
        for (Project project : workspaceProjects) {
            try {
                GitRepoService svc = project.getGitRepoService();
                if (!svc.hasStaged()) continue;
                String diff = svc.getStagedDiffText(LlmCommitMessageService.MAX_DIFF_CHARS);
                if (diff == null || diff.isBlank()) continue;
                sb.append("=== ").append(projectLabel(project)).append(" ===\n").append(diff).append("\n\n");
            } catch (Exception ex) {
                log.warning("Cannot read staged diff for " + project.getProjectHomeFolder() + ": " + ex.getMessage());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    // -------------------------------------------------------------------------
    //  Async detector
    // -------------------------------------------------------------------------

    /** A file to scan for secrets, plus its owning repo's label in workspace mode ({@code null} otherwise). */
    private record ScanTarget(Path path, String repoLabel) {}

    /**
     * Staged files to scan. In single-repository mode these come from {@link Context#getStatusList()};
     * in workspace mode every project's staged files are collected via a throwaway
     * {@link GitRepoService}, tagged with their project's label so findings can show which repo
     * they belong to.
     */
    private List<ScanTarget> collectFilesToScan() {
        List<ScanTarget> result = new ArrayList<>();
        if (workspaceProjects != null) {
            for (Project project : workspaceProjects) {
                String repoLabel = projectLabel(project);
                try {
                    GitRepoService svc = project.getGitRepoService();
                    for (ScmItem item : svc.getStatuses(null)) {
                        String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                        if (!STAGED_STATUSES.contains(status) || ScmItem.Status.REMOVED.equals(status)) continue;
                        Path p = Paths.get(project.getProjectHomeFolder(), item.getShortName());
                        if (Files.exists(p) && Files.isRegularFile(p)) {
                            result.add(new ScanTarget(p, repoLabel));
                        }
                    }
                } catch (Exception ex) {
                    log.warning("Cannot read staged files for " + project.getProjectHomeFolder() + ": " + ex.getMessage());
                }
            }
        } else {
            List<ScmItem> items = Context.getStatusList();
            String repoPath = Context.getProjectFolder();
            if (items != null) {
                for (ScmItem item : items) {
                    String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                    if (!STAGED_STATUSES.contains(status) || ScmItem.Status.REMOVED.equals(status)) continue;
                    Path p = Paths.get(repoPath, item.getShortName());
                    if (Files.exists(p) && Files.isRegularFile(p)) {
                        result.add(new ScanTarget(p, null));
                    }
                }
            }
        }
        return result;
    }

    private void startDetector() {
        if (!isLeakDetectorEnabled()) return;

        List<ScanTarget> targets = collectFilesToScan();
        if (targets.isEmpty()) return;

        List<Path> toScan = new ArrayList<>();
        Map<Path, String> repoLabelByPath = new HashMap<>();
        for (ScanTarget target : targets) {
            toScan.add(target.path());
            if (target.repoLabel() != null) repoLabelByPath.put(target.path(), target.repoLabel());
        }

        scanStatusPanel.setVisible(true);

        String model = llmModel();

        new SwingWorker<List<Finding>, String>() {

            @Override
            protected List<Finding> doInBackground() throws Exception {
                // ---- Try to set up Ollama (best-effort) ----
                boolean llmReady = false;
                try {
                    OllamaManager.Status status = OllamaManager.getStatus();

                    if (status == OllamaManager.Status.STOPPED) {
                        publish("Starting Ollama…");
                        OllamaManager.startServerAndWait(20_000);
                        status = OllamaManager.Status.RUNNING;
                    }

                    if (status == OllamaManager.Status.RUNNING) {
                        if (!OllamaManager.isModelAvailable(model)) {
                            publish("Pulling model \"" + model + "\" (first run, please wait)...");
                            Process pull = OllamaManager.startModelPull(model);
                            try (java.io.BufferedReader br = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(pull.getInputStream()))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    String trimmed = line.trim();
                                    if (!trimmed.isEmpty()) publish("Pulling: " + trimmed);
                                }
                            }
                            pull.waitFor();
                        }
                        llmReady = OllamaManager.isRunning() && OllamaManager.isModelAvailable(model);
                    }
                } catch (Exception e) {
                    log.fine("Ollama setup failed, falling back to empirical scan: " + e.getMessage());
                }

                // ---- Scan files ----
                publish(llmReady ? "LLM secret scan in progress…" : "Scanning for secrets…");

                DetectorService service = llmReady
                        ? new DetectorService(OllamaManager.BASE_URL, model)
                        : new DetectorService();

                List<Finding> all = new ArrayList<>();
                for (Path filePath : toScan) {
                    try {
                        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                        ScanContext ctx = new ScanContext(filePath, lines, new FileType());
                        all.addAll(service.detect(ctx));
                    } catch (Exception ex) {
                        log.fine("Skipping " + filePath + ": " + ex.getMessage());
                    }
                }
                return all;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    scanStatusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                scanStatusPanel.setVisible(false);
                try {
                    List<Finding> all = get();
                    if (!all.isEmpty()) {
                        findings.clear();
                        findings.addAll(all);
                        findingsModel.setRowCount(0);
                        for (Finding f : all) {
                            String fileName = f.getFile() != null ? f.getFile().getFileName().toString() : "";
                            String repoLabel = f.getFile() != null ? repoLabelByPath.get(f.getFile()) : null;
                            String fileDisplay = repoLabel != null ? repoLabel + "/" + fileName : fileName;
                            findingsModel.addRow(new Object[]{
                                    fileDisplay,
                                    f.getLineNo(),
                                    f.getType(),
                                    f.getConfidence() != null ? f.getConfidence().name() : "",
                                    f.getMessage(),
                                    "Open"
                            });
                        }
                        findingsPanel.setVisible(true);
                        pack();
                        setLocationRelativeTo(getOwner());
                    }
                } catch (Exception ex) {
                    log.fine("Detector worker failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // -------------------------------------------------------------------------

    private void onCommit() {
        if (workspaceProjects != null) {
            List<String> missingMessage = new ArrayList<>();
            for (int i = 0; i < workspaceProjects.size(); i++) {
                Project project = workspaceProjects.get(i);
                if (hasStaged(project) && commitMessagePanel.getEffectiveMessage(i).isEmpty()) {
                    missingMessage.add(projectLabel(project));
                }
            }
            if (!missingMessage.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Commit message is required for: " + String.join(", ", missingMessage)
                                + "\n(set a per-project message, or a common message as fallback)",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (commitMessagePanel.getMessage().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Commit message is required",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            //This is initial implementation. Without any distributed transactions support.
            if (workspaceProjects != null) {
                //Check is any of the repo has unresolved conflicts
                Map<Project, List<ScmItem>> conflictedFiles = getConflictedFiles(Context.getWorkspace());
                if (!conflictedFiles.isEmpty()) {
                    new ConflictedFilesDialog(this, conflictedFiles).setVisible(true);
                    return;
                }

                for (int i = 0; i < workspaceProjects.size(); i++) {
                    commitSingleProject(workspaceProjects.get(i), commitMessagePanel.getEffectiveMessage(i));
                }

                // Commit succeeded for every project; now check (without merging) whether each
                // branch would merge cleanly into its upstream, and warn about the ones that won't.
                List<String> unmergeable = new ArrayList<>();
                for (Project project : workspaceProjects) {
                    try {
                        GitRepoService svc = project.getGitRepoService();
                        if (!svc.canMergeWithUpstream()) {
                            unmergeable.add(projectLabel(project));
                        }
                    } catch (Exception ex) {
                        log.warning("Cannot check mergeability for " + project.getProjectHomeFolder()
                                + ": " + ex.getMessage());
                    }
                }
                if (!unmergeable.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Commit succeeded for all projects in the workspace.\n"
                                    + "However, merging with the upstream branch may fail for: "
                                    + String.join(", ", unmergeable),
                            "Merge Warning", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                Project project = Context.getCurrentProject().orElse(null);
                commitSingleProject(project, commitMessagePanel.getMessage().trim());
                Context.updateStatus(null);
                Context.updateBranches();
                Context.updateWorkingBranch();
            }
            dispose();
        } catch (Exception e) {
            log.warning("Commit failed: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Commit failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasStaged(Project project) {
        try {
            return project.getGitRepoService().hasStaged();
        } catch (Exception e) {
            return false;
        }
    }

    private static String projectLabel(Project project) {
        String folder = project.getProjectHomeFolder();
        if (folder == null || folder.isBlank()) {
            return "(unknown)";
        }
        String name = new java.io.File(folder).getName();
        return name.isEmpty() ? folder : name;
    }

    /** Commits staged items for one project, using the configured author/committer identity. */
    private void commitSingleProject(Project project, String message) throws IOException, GitAPIException {
        GitRepoService svc = project.getGitRepoService();
        if (svc.hasStaged()) {
            String authorName     = StringUtils.trimToNull(project.getUserCommitName());
            String authorEmail    = StringUtils.trimToNull(project.getUserCommitEmail());
            String committerName  = StringUtils.trimToNull(project.getCommitterName());
            String committerEmail = StringUtils.trimToNull(project.getCommitterEmail());
            svc.commit(message, authorName, authorEmail, committerName, committerEmail);
        }
    }

    private Map<Project, List<ScmItem>> getConflictedFiles(Workspace workspace) throws IOException {
        Map<Project, List<ScmItem>> rez = new HashMap<>() ;
        for (Project proj : workspace.getProjects()) {
            List<ScmItem> files = getConflictedFiles(proj);
            if (!CollectionUtils.isEmpty(files)) {
                rez.put(
                        proj,
                        files
                );
            }
        }
        return rez;
    }

    private List<ScmItem> getConflictedFiles(Project project) throws IOException {
        GitRepoService svc = project.getGitRepoService();
        return svc.getStatuses(null).stream()
                .filter(i -> ScmItem.Status.CONFLICT.equals(i.getAttribute().getStatus()))
                .collect(Collectors.toUnmodifiableList());
    }

    private void openFinding(int row) {
        if (row < 0 || row >= findings.size()) return;
        Finding f = findings.get(row);
        if (f.getFile() == null) return;
        try {
            String content = Files.readString(f.getFile(), StandardCharsets.UTF_8);
            FileViewerWindow viewer = new FileViewerWindow(
                    f.getFile().getFileName().toString(), content,
                    f.getFile().getFileName().toString());
            viewer.setVisible(true);
            viewer.toFront();
            viewer.requestFocus();
            SwingUtilities.invokeLater(() -> viewer.scrollToAndHighlight(f.getLineNo()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot open file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    //  Cell renderers / editors
    // -------------------------------------------------------------------------

    private static class OpenButtonRenderer implements TableCellRenderer {
        private final JButton btn = new JButton("Open");
        { btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f)); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return btn;
        }
    }

    private class OpenButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton("Open");
        private int clickedRow = -1;

        OpenButtonEditor(JTable table) {
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f));
            btn.addActionListener(e -> {
                fireEditingStopped();
                openFinding(clickedRow);
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            clickedRow = row;
            return btn;
        }
        @Override public Object getCellEditorValue() { return "Open"; }
        @Override public boolean isCellEditable(EventObject e) { return true; }
    }

    /** Colours findings rows by confidence level. */
    private static class FindingsCellRenderer extends DefaultTableCellRenderer {

        private static final Color COLOR_CRITICAL = new Color(0xFF, 0xCC, 0xCC);
        private static final Color COLOR_HIGH     = new Color(0xFF, 0xE8, 0xCC);
        private static final Color COLOR_MEDIUM   = new Color(0xFF, 0xF8, 0xCC);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object confObj = table.getModel().getValueAt(row, 3);
                String conf = confObj != null ? confObj.toString() : "";
                c.setBackground(switch (conf) {
                    case "CRITICAL" -> COLOR_CRITICAL;
                    case "HIGH"     -> COLOR_HIGH;
                    case "MEDIUM"   -> COLOR_MEDIUM;
                    default         -> table.getBackground();
                });
            }
            return c;
        }
    }
}
