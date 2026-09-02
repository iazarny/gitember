package com.az.gitember.ui;

import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.ExtensionInfo;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitRepoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Builds and shows a working-copy context menu for a list of {@link ScmItem}s.
 *
 * <p>All git operations are dispatched through the {@link ServiceRunner} supplied
 * at construction time, so the same menu logic works for both the single-project
 * working-copy panel (uses the global {@code Context.getGitRepoService()}) and the
 * workspace tree (creates a per-project auto-closed {@link GitRepoService}).
 */
public class WorkingCopyContextMenu {

    private static final Logger log = Logger.getLogger(WorkingCopyContextMenu.class.getName());

    /**
     * Executes a git operation against the correct service for the call site.
     * Implementations are responsible for service lifecycle (open/close).
     */
    @FunctionalInterface
    public interface ServiceRunner {
        void run(ServiceAction action) throws Exception;
    }

    /** A git action that receives an open service and may throw. */
    @FunctionalInterface
    public interface ServiceAction {
        void execute(GitRepoService svc) throws Exception;
    }

    private final JComponent parent;
    private final StatusBar statusBar;
    private final ServiceRunner serviceRunner;
    private final Supplier<String> folderProvider;
    private final Runnable onComplete;

    /**
     * @param parent         parent component used for confirmation dialogs
     * @param statusBar      status bar for progress / status messages
     * @param serviceRunner  provides (and owns the lifecycle of) the git service
     * @param folderProvider returns the project root folder (with or without trailing separator)
     * @param onComplete     called after every mutating action to trigger a view refresh
     */
    public WorkingCopyContextMenu(JComponent parent,
                                   StatusBar statusBar,
                                   ServiceRunner serviceRunner,
                                   Supplier<String> folderProvider,
                                   Runnable onComplete) {
        this.parent = parent;
        this.statusBar = statusBar;
        this.serviceRunner = serviceRunner;
        this.folderProvider = folderProvider;
        this.onComplete = onComplete;
    }

    /** Shows the context menu at (x, y) relative to {@code invoker}. */
    public void show(List<ScmItem> items, Component invoker, int x, int y) {
        if (CollectionUtils.isNotEmpty(items)) {
            JPopupMenu menu = new JPopupMenu();
            if (items.size() == 1) {
                buildSingleItemMenu(menu, items.get(0));
            }
            else {
                buildMultiItemMenu(menu, items);
            }
            menu.show(invoker, x, y);
        }
    }

    // ── Menu builders ─────────────────────────────────────────────────────────

    private void buildSingleItemMenu(JPopupMenu menu, ScmItem item) {
        String status    = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
        boolean isConflict = status.startsWith("Conflict");
        boolean isModified = ScmItem.Status.MODIFIED.equals(status);
        boolean isChanged  = ScmItem.Status.CHANGED.equals(status);
        boolean isMissed   = ScmItem.Status.MISSED.equals(status);
        boolean isRemoved  = ScmItem.Status.REMOVED.equals(status);

        // Group 1: Stage / Unstage
        if (ScmItem.isStaged(status)) {
            JMenuItem unstage = new JMenuItem("Unstage");
            unstage.addActionListener(e -> doUnstage(item));
            menu.add(unstage);
        } else {
            JMenuItem stage = new JMenuItem("Stage");
            stage.addActionListener(e -> doStage(item));
            menu.add(stage);
        }

        // Group 2: Diff / Revert / Resolve conflict
        boolean hasDiff   = isModified || isChanged;
        boolean hasRevert = isModified || isMissed;
        if (hasDiff || hasRevert || isConflict) {
            menu.addSeparator();
            if (hasDiff) {
                JMenuItem diff = new JMenuItem("Diff with repository");
                diff.addActionListener(e -> showDiffWithRepo(item));
                menu.add(diff);
            }
            if (hasRevert) {
                JMenuItem revert = new JMenuItem("Revert...");
                revert.addActionListener(e -> revertItem(item));
                menu.add(revert);
            }
            if (isConflict) {
                JMenu resolveMenu = new JMenu("Resolve conflict");

                JMenuItem markResolved = new JMenuItem("Mark resolved");
                markResolved.addActionListener(e -> resolveConflict(item, null));
                resolveMenu.add(markResolved);

                JMenuItem useOurs = new JMenuItem("Using mine (OURS)");
                useOurs.addActionListener(e -> resolveConflict(item,
                        org.eclipse.jgit.api.CheckoutCommand.Stage.OURS));
                resolveMenu.add(useOurs);

                JMenuItem useTheirs = new JMenuItem("Using theirs (THEIRS)");
                useTheirs.addActionListener(e -> resolveConflict(item,
                        org.eclipse.jgit.api.CheckoutCommand.Stage.THEIRS));
                resolveMenu.add(useTheirs);

                resolveMenu.addSeparator();
                JMenuItem mergeTool = new JMenuItem("Open in Merge Tool...");
                mergeTool.addActionListener(e -> openMergeTool(item));
                resolveMenu.add(mergeTool);

                menu.add(resolveMenu);
            }
        }

        // Group 3: History / Open
        boolean hasHistory = isModified || isMissed || isChanged;
        boolean hasOpen    = !isMissed && !isRemoved;
        if (hasHistory || hasOpen) {
            menu.addSeparator();
            if (hasHistory) {
                JMenuItem history = new JMenuItem("History");
                history.addActionListener(e -> showHistory(item));
                menu.add(history);
            }
            if (hasOpen) {
                JMenuItem open = new JMenuItem("Open");
                open.addActionListener(e -> openFile(item));
                menu.add(open);
            }
        }

        // Group 4: Ignore / Physical delete
        boolean canIgnore = canAddToGitIgnore(item);
        if (canIgnore || (!isMissed && !isRemoved)) {
            menu.addSeparator();
            if (canIgnore) {

                JMenu ignoreMenu = new JMenu("Add  to .gitignore");
                menu.add(ignoreMenu);

                JMenuItem ignore = new JMenuItem(FilenameUtils.getName(item.getShortName()) );
                ignore.addActionListener(e -> addToGitIgnore(List.of(item)));
                ignoreMenu.add(ignore);

                String ext = FilenameUtils.getExtension(item.getShortName());;
                if (StringUtils.isNotBlank(ext)) {
                    final String asterixExt = "*." + ext;
                    JMenuItem ignoreExt = new JMenuItem("Extention " + asterixExt );
                    ignoreExt.addActionListener(e -> addToGitIgnorePaths(List.of(asterixExt)));
                    ignoreMenu.add(ignoreExt);
                }

                Path path = Paths.get(item.getShortName());
                String firstFolder = path.getName(0).toString() + File.separator;
                JMenuItem ignoreFolder = new JMenuItem("Folder " + firstFolder );
                ignoreFolder.addActionListener(e -> addToGitIgnorePaths(List.of(firstFolder)));
                ignoreMenu.add(ignoreFolder);


            }
            if (!isMissed && !isRemoved) {
                JMenuItem delete = new JMenuItem("Physical delete...");
                delete.addActionListener(e -> physicalDelete(item));
                menu.add(delete);
            }
        }
    }

    private void buildMultiItemMenu(JPopupMenu menu, List<ScmItem> items) {
        boolean hasUnstaged   = items.stream().anyMatch(i -> !i.isStaged());
        boolean hasStaged     = items.stream().anyMatch(i -> i.isStaged());
        boolean hasRevertable = items.stream().anyMatch(i -> {
            String s = i.getAttribute().getStatus();
            return ScmItem.Status.MODIFIED.equals(s) || ScmItem.Status.MISSED.equals(s);
        });
        boolean hasDeletable = items.stream().anyMatch(i -> {
            String s = i.getAttribute().getStatus();
            return !ScmItem.Status.MISSED.equals(s) && !ScmItem.Status.REMOVED.equals(s);
        });
        List<ScmItem> ignorable = items.stream().filter(this::canAddToGitIgnore).toList();

        if (hasUnstaged) {
            long count = items.stream().filter(i -> !i.isStaged()).count();
            JMenuItem stage = new JMenuItem("Stage selected (" + count + ")");
            stage.addActionListener(e -> doStageMultiple(
                    items.stream().filter(i -> !i.isStaged()).toList()));
            menu.add(stage);
        }
        if (hasStaged) {
            long count = items.stream().filter(i -> i.isStaged()).count();
            JMenuItem unstage = new JMenuItem("Unstage selected (" + count + ")");
            unstage.addActionListener(e -> doUnstageMultiple(
                    items.stream().filter(i -> i.isStaged()).toList()));
            menu.add(unstage);
        }
        if (hasRevertable) {
            menu.addSeparator();
            JMenuItem revert = new JMenuItem("Revert selected...");
            revert.addActionListener(e -> {
                int c = JOptionPane.showConfirmDialog(parent,
                        "Revert " + items.size() + " selected files?",
                        "Revert", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    doRevertMultiple(items.stream().filter(i -> {
                        String s = i.getAttribute().getStatus();
                        return ScmItem.Status.MODIFIED.equals(s) || ScmItem.Status.MISSED.equals(s);
                    }).toList());
                }
            });
            menu.add(revert);
        }
        if (!ignorable.isEmpty() || hasDeletable) {
            menu.addSeparator();
            if (!ignorable.isEmpty()) {
                JMenuItem ignore = new JMenuItem("Add selected to .gitignore (" + ignorable.size() + ")");
                ignore.addActionListener(e -> addToGitIgnore(ignorable));
                menu.add(ignore);
            }
            if (hasDeletable) {
                JMenuItem delete = new JMenuItem("Physical delete selected...");
                delete.addActionListener(e -> {
                    int c = JOptionPane.showConfirmDialog(parent,
                            "Physically delete " + items.size() + " selected files?",
                            "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (c == JOptionPane.YES_OPTION) {
                        for (ScmItem item : items) {
                            String s = item.getAttribute().getStatus();
                            if (!ScmItem.Status.MISSED.equals(s) && !ScmItem.Status.REMOVED.equals(s)) {
                                deleteFileFromDisk(item.getShortName());
                            }
                        }
                        onComplete.run();
                    }
                });
                menu.add(delete);
            }
        }
    }

    // ── Action methods ────────────────────────────────────────────────────────

    private void doStage(ScmItem item) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> svc.stageItem(item));
                return null;
            }
            @Override protected void done() { handleDone("Staged: " + item.getShortName()); }
        }.execute();
    }

    private void doUnstage(ScmItem item) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> svc.unstageItem(item));
                return null;
            }
            @Override protected void done() { handleDone("Unstaged: " + item.getShortName()); }
        }.execute();
    }

    private void doStageMultiple(List<ScmItem> items) {
        statusBar.setStatus("Staging " + items.size() + " files...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> { for (ScmItem i : items) svc.stageItem(i); });
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                handleDone("Staged " + items.size() + " files");
            }
        }.execute();
    }

    private void doUnstageMultiple(List<ScmItem> items) {
        statusBar.setStatus("Unstaging " + items.size() + " files...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> { for (ScmItem i : items) svc.unstageItem(i); });
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                handleDone("Unstaged " + items.size() + " files");
            }
        }.execute();
    }

    private void revertItem(ScmItem item) {
        int c = JOptionPane.showConfirmDialog(parent,
                "Revert '" + item.getShortName() + "' to last committed version?\nAll local changes will be lost.",
                "Revert", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> svc.checkoutFile(item.getShortName(), null));
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    statusBar.setStatus("Reverted: " + item.getShortName());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Revert failed", ex);
                    statusBar.setStatus("Revert failed: " + ex.getMessage());
                }
                onComplete.run();
            }
        }.execute();
    }

    private void doRevertMultiple(List<ScmItem> items) {
        statusBar.setStatus("Reverting " + items.size() + " files...");
        statusBar.showProgress(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> {
                    for (ScmItem i : items) svc.checkoutFile(i.getShortName(), null);
                });
                return null;
            }
            @Override protected void done() {
                statusBar.clearProgress();
                try {
                    get();
                    statusBar.setStatus("Reverted " + items.size() + " files");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Revert failed", ex);
                    statusBar.setStatus("Revert failed: " + ex.getMessage());
                }
                onComplete.run();
            }
        }.execute();
    }

    private void resolveConflict(ScmItem item, org.eclipse.jgit.api.CheckoutCommand.Stage stage) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> {
                    if (stage != null) svc.checkoutFile(item.getShortName(), stage);
                    svc.addFileToCommitStage(item.getShortName());
                });
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    statusBar.setStatus("Conflict resolved: " + item.getShortName());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Resolve failed", ex);
                    statusBar.setStatus("Resolve failed: " + ex.getMessage());
                }
                onComplete.run();
            }
        }.execute();
    }

    private void openMergeTool(ScmItem item) {
        String absPath = normalizedFolder() + item.getShortName().replace('/', File.separatorChar);
        new ThreeWayMergeWindow(absPath);
    }

    void showDiffWithRepo(ScmItem item) {
        String fileName = item.getShortName();
        boolean text = ExtensionInfo.ExtType.TEXT == ExtensionMap.getExtensionType(fileName);
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String[] result = new String[2];
                serviceRunner.run(svc -> {
                    String tempPath = svc.saveFile("HEAD", fileName);
                    result[0] = Files.readString(Paths.get(tempPath));
                });
                result[1] = Files.readString(Paths.get(normalizedFolder() + fileName));
                return result;
            }
            @Override
            protected void done() {
                try {
                    String[] contents = get();
                    new DiffViewerWindowTxt(fileName, "HEAD", contents[0], contents[1]).setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to show diff", ex);
                    JOptionPane.showMessageDialog(parent,
                            "Cannot show diff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showHistory(ScmItem item) {
        JFrame frame = new JFrame("History: " + item.getShortName());
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(parent);
        HistoryPanel hp = new HistoryPanel(statusBar);
        frame.getContentPane().add(hp);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        hp.loadFileHistory(item.getShortName());
    }

    void openFile(ScmItem item) {
        String filePath = normalizedFolder() + item.getShortName();
        if (ExtensionInfo.ExtType.TEXT == ExtensionMap.getExtensionType(item.getShortName())) {
            try {
                String content = Files.readString(Paths.get(filePath));
                FileViewerWindow viewer = new FileViewerWindow(item.getShortName(), content, item.getShortName());
                String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
                if (ScmItem.Status.MODIFIED.equals(status) || ScmItem.Status.CHANGED.equals(status)) {
                    viewer.enableBlame(null, item.getShortName());
                }
                viewer.setVisible(true);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file", ex);
                JOptionPane.showMessageDialog(parent, "Cannot open: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                Desktop.getDesktop().open(new File(filePath));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot open file with system", ex);
            }
        }
    }

    private boolean canAddToGitIgnore(ScmItem item) {
        return item != null && !Const.GIT_IGNORE_NAME.equals(item.getShortName());
    }

    private void addToGitIgnore(List<ScmItem> items) {
        if (items == null) return;
        List<String> paths = items.stream()
                .map(ScmItem::getShortName)
                .collect(Collectors.toList());
        addToGitIgnorePaths(paths);
    }

    // New method accepting List<String>
    private void addToGitIgnorePaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) return;

        statusBar.setStatus("Adding " + paths.size() + " file(s) to .gitignore...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                serviceRunner.run(svc -> {
                    for (String path : paths) {
                        svc.addToGitIgnore(path);
                    }
                });
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusBar.setStatus("Added to .gitignore");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Add to .gitignore failed", ex);
                    statusBar.setStatus("Add to .gitignore failed: " + ex.getMessage());
                }
                onComplete.run();
            }
        }.execute();
    }

    public void physicalDelete(ScmItem item) {
        int c = JOptionPane.showConfirmDialog(parent,
                "Physically delete '" + item.getShortName() + "'?\nThis cannot be undone.",
                "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        deleteFileFromDisk(item.getShortName());
        onComplete.run();
    }

    public void deleteFileFromDisk(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(normalizedFolder() + fileName));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot delete file: " + fileName, ex);
        }
    }

    private void handleDone(String message) {
        try {
            statusBar.setStatus(message);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Operation failed", ex);
            statusBar.setStatus("Error: " + ex.getMessage());
        }
        onComplete.run();
    }

    /** Returns the project folder with a guaranteed trailing path separator. */
    private String normalizedFolder() {
        String folder = folderProvider.get();
        if (folder == null) return "";
        return (folder.endsWith("/") || folder.endsWith(File.separator))
                ? folder
                : folder + File.separator;
    }
}
