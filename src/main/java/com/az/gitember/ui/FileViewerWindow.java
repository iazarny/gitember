package com.az.gitember.ui;

import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple window to display text file content from a git commit.
 * Supports optional git-blame annotation via the "Annotate" toggle button.
 */
public class FileViewerWindow extends JFrame {

    private static final Logger log = Logger.getLogger(FileViewerWindow.class.getName());

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final String suggestedFileName;
    private final SearchBar searchBar;

    // Blame components
    private final JToggleButton annotateBtn = new JToggleButton("Annotate");
    private final JTextArea blameTextArea   = new JTextArea();
    private final JScrollPane blameScrollPane;
    private final JPanel centerPanel        = new JPanel(new BorderLayout());

    private String  blameCommitSha  = null;
    private String  blameFilePath   = null;
    private boolean blameLoaded     = false;
    private String  lastBlameClickSha = null;

    public FileViewerWindow(String title, String content) {
        this(title, content, title);
    }

    public FileViewerWindow(String title, String content, String fileName) {
        setTitle(title);
        setSize(900, 600);
        setLocationRelativeTo(Context.getMainFrame());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(Util.appIcons());

        this.suggestedFileName = fileName;

        // ── Main text area ────────────────────────────────────────────────
        textArea = new RSyntaxTextArea(content);
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxStyleUtil.getSyntaxStyle(fileName));
        textArea.setCodeFoldingEnabled(false);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCaretPosition(0);
        textArea.setBracketMatchingEnabled(false);
        textArea.setAnimateBracketMatching(false);
        SyntaxStyleUtil.applyTheme(textArea);
        textArea.setFont(SyntaxStyleUtil.monoFont());  // after theme so settings font size is applied

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(false);

        // ── Blame sidebar ─────────────────────────────────────────────────
        blameTextArea.setEditable(false);
        blameTextArea.setFont(SyntaxStyleUtil.monoFont());
        blameTextArea.setFocusable(false);
        blameTextArea.setLineWrap(false);
        blameTextArea.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));

        boolean dark = SyntaxStyleUtil.isDarkTheme();
        blameTextArea.setBackground(dark ? new Color(35, 35, 40)  : new Color(240, 240, 245));
        blameTextArea.setForeground(dark ? new Color(160, 170, 180) : new Color(80,  80,  90));

        blameScrollPane = new JScrollPane(blameTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        blameScrollPane.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1,
                dark ? new Color(70, 70, 80) : new Color(200, 200, 210)));
        blameScrollPane.setVisible(false);

        // ── Blame context menu & mouse interactions ───────────────────────
        JPopupMenu blameContextMenu = new JPopupMenu();
        JMenuItem openInHistoryItem = new JMenuItem("Open commit in History");
        openInHistoryItem.addActionListener(e -> {
            if (lastBlameClickSha != null && !lastBlameClickSha.isBlank()) {
                navigateToHistoryCommit(lastBlameClickSha);
            }
        });
        blameContextMenu.add(openInHistoryItem);

        blameTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    lastBlameClickSha = extractShaFromBlamePoint(e.getPoint());
                    if (lastBlameClickSha != null && !lastBlameClickSha.isBlank()) {
                        navigateToHistoryCommit(lastBlameClickSha);
                    }
                }
            }

            @Override public void mousePressed(MouseEvent e)  { maybeShowBlameMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowBlameMenu(e); }

            private void maybeShowBlameMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                lastBlameClickSha = extractShaFromBlamePoint(e.getPoint());
                openInHistoryItem.setEnabled(lastBlameClickSha != null && !lastBlameClickSha.isBlank());
                blameContextMenu.show(blameTextArea, e.getX(), e.getY());
            }
        });

        // Sync blame viewport to follow the main viewport (one-way, no model conflicts)
        scrollPane.getViewport().addChangeListener(e -> {
            int y = scrollPane.getViewport().getViewPosition().y;
            Point cur = blameScrollPane.getViewport().getViewPosition();
            if (cur.y != y) {
                blameScrollPane.getViewport().setViewPosition(new Point(0, y));
            }
        });

        // ── Search bar ────────────────────────────────────────────────────
        searchBar = new SearchBar(textArea);

        // ── Buttons ───────────────────────────────────────────────────────
        JButton saveBtn  = new JButton("Save...");
        saveBtn.addActionListener(e -> saveContent());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        annotateBtn.setVisible(false);   // shown only when enableBlame() is called
        annotateBtn.addActionListener(e -> toggleAnnotation());

        JPanel leftBtns  = new JPanel(new FlowLayout(FlowLayout.LEFT,  8, 6));
        leftBtns.add(annotateBtn);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightBtns.add(saveBtn);
        rightBtns.add(closeBtn);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(leftBtns,  BorderLayout.WEST);
        btnPanel.add(rightBtns, BorderLayout.EAST);

        // ── Layout ────────────────────────────────────────────────────────
        centerPanel.add(blameScrollPane, BorderLayout.WEST);
        centerPanel.add(scrollPane,      BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchBar,   BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel,    BorderLayout.SOUTH);

        getRootPane().setDefaultButton(closeBtn);

        // Ctrl/Cmd+F → open search bar
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().registerKeyboardAction(
                e -> searchBar.activate(), ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);

        Util.bindEscapeToDispose(this);

        this.addPropertyChangeListener("graphicsConfiguration", evt -> {
            SwingUtilities.invokeLater(() -> {
                textArea.revalidate();
                if (textArea.getParserCount() > 0) {
                    textArea.forceReparsing(0);
                }
                textArea.repaint();
            });
        });
    }

    /**
     * Call this before {@code setVisible(true)} to enable the "Annotate" button.
     *
     * @param commitSha SHA of the commit whose tree the file was read from
     * @param filePath  repo-relative file path
     */
    public void enableBlame(String commitSha, String filePath) {
        this.blameCommitSha = commitSha;
        this.blameFilePath  = filePath;
        annotateBtn.setVisible(true);
    }

    /**
     * Scrolls to {@code lineNo} (1-based) and highlights that line in amber.
     * Call after {@code setVisible(true)} or from {@code SwingUtilities.invokeLater}.
     */
    public void scrollToAndHighlight(int lineNo) {
        if (lineNo <= 0) return;
        try {
            int zeroLine = lineNo - 1;
            textArea.addLineHighlight(zeroLine, new Color(0xFF, 0xD0, 0x00, 120));
            int offset = textArea.getLineStartOffset(zeroLine);
            textArea.setCaretPosition(offset);
            textArea.scrollRectToVisible(textArea.modelToView2D(offset).getBounds());
        } catch (Exception ex) {
            log.fine("scrollToAndHighlight failed at line " + lineNo + ": " + ex.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void toggleAnnotation() {
        if (!annotateBtn.isSelected()) {
            blameScrollPane.setVisible(false);
            centerPanel.revalidate();
            return;
        }

        if (blameLoaded) {
            blameScrollPane.setVisible(true);
            centerPanel.revalidate();
            return;
        }

        annotateBtn.setEnabled(false);
        annotateBtn.setText("Loading…");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return Context.getGitRepoService()
                        .getBlameAnnotations(blameCommitSha, blameFilePath);
            }

            @Override
            protected void done() {
                try {
                    List<String> annotations = get();
                    blameTextArea.setText(String.join("\n", annotations));
                    blameTextArea.setCaretPosition(0);

                    // Fit blame panel to its content width
                    int charWidth = blameTextArea.getFontMetrics(blameTextArea.getFont())
                            .charWidth('m');
                    int maxLen = annotations.stream()
                            .mapToInt(String::length).max().orElse(36);
                    int panelW = charWidth * (maxLen + 2) + 12;
                    blameScrollPane.setPreferredSize(
                            new Dimension(panelW, blameScrollPane.getHeight()));

                    blameLoaded = true;
                    blameScrollPane.setVisible(true);
                    centerPanel.revalidate();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Blame load failed", ex);
                    annotateBtn.setSelected(false);
                    JOptionPane.showMessageDialog(FileViewerWindow.this,
                            "Blame unavailable: " + ex.getMessage(),
                            "Annotate", JOptionPane.WARNING_MESSAGE);
                } finally {
                    annotateBtn.setEnabled(true);
                    annotateBtn.setText("Annotate");
                }
            }
        }.execute();
    }

    /** Extracts the 7-char commit SHA from the annotation line under the given point. */
    private String extractShaFromBlamePoint(Point p) {
        try {
            int offset = blameTextArea.viewToModel2D(p);
            int line   = blameTextArea.getLineOfOffset(offset);
            int start  = blameTextArea.getLineStartOffset(line);
            int end    = blameTextArea.getLineEndOffset(line);
            String lineText = blameTextArea.getText(start, end - start);
            if (lineText == null || lineText.length() < 7) return null;
            String sha = lineText.substring(0, 7).trim();
            return sha.isEmpty() ? null : sha;
        } catch (Exception ex) {
            log.fine("Failed to extract blame SHA: " + ex.getMessage());
            return null;
        }
    }

    /** Brings the main window to front and navigates the history view to the given short SHA. */
    private void navigateToHistoryCommit(String sha) {
        javax.swing.JFrame mf = Context.getMainFrame();
        if (mf instanceof MainFrame mainFrame) {
            mainFrame.toFront();
            mainFrame.showCommitInHistory(sha);
        }
    }

    private void saveContent() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save as");
        chooser.setSelectedFile(new File(suggestedFileName));
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Patch / Diff files (*.patch, *.diff)", "patch", "diff"));
        chooser.setAcceptAllFileFilterUsed(true);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            Files.writeString(chooser.getSelectedFile().toPath(),
                    textArea.getText(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
