package com.az.gitember.ui;

import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.diff.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Side-by-side diff viewer using JGit's HISTOGRAM diff algorithm
 * with RSyntaxTextArea for syntax highlighting and no line wrapping.
 */
public class DiffViewerWindow extends JFrame {

    private static final Logger log = Logger.getLogger(DiffViewerWindow.class.getName());

    private RSyntaxTextArea oldPane;
    private RSyntaxTextArea newPane;
    private RTextScrollPane leftScroll;
    private RTextScrollPane rightScroll;
    private final JComboBox<RevisionItem> oldCombo;
    private final JComboBox<RevisionItem> newCombo;
    private JLabel diffInfoLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private final String fileName;
    private DiffConnectorPanel centerPanel;

    private EditList editList;
    private int currentDiff = -1;
    private String oldText;
    private String newText;

    // Highlight colors — two palettes selected at paint time based on active theme
    private static Color addedBg() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(0, 70, 0) : new Color(200, 255, 200);
    }
    private static Color deletedBg() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(90, 20, 20) : new Color(255, 200, 200);
    }
    private static Color changedBg() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(20, 50, 100) : new Color(200, 230, 255);
    }

    // ---- Constructors ----

    public DiffViewerWindow(String fileName, List<ScmRevisionInformation> fileRevisions,
                            String oldSha, String newSha) {
        this.fileName = fileName;
        setTitle("Diff: " + fileName);
        initCommon(SyntaxStyleUtil.getSyntaxStyle(fileName));

        // Revision combo boxes
        oldCombo = new JComboBox<>();
        newCombo = new JComboBox<>();
        if (fileRevisions != null) {
            for (ScmRevisionInformation rev : fileRevisions) {
                RevisionItem item = new RevisionItem(rev);
                oldCombo.addItem(item);
                newCombo.addItem(item);
            }
        }
        selectRevision(oldCombo, oldSha);
        selectRevision(newCombo, newSha);
        oldCombo.addActionListener(e -> loadAndDiff());
        newCombo.addActionListener(e -> loadAndDiff());

        // Row 1: combo boxes stretch to fill their respective editor panes (45%/10%/45%)
        JPanel combosRow = new JPanel(new GridBagLayout());
        combosRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 3, 0, 3);

        g.gridx = 0; g.weightx = 0;    g.fill = GridBagConstraints.NONE;
        combosRow.add(new JLabel("Old:"), g);
        g.gridx = 1; g.weightx = 0.45; g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(oldCombo, g);
        // spacer that matches the connector-panel column
        g.gridx = 2; g.weightx = 0.10; g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(new JLabel("New:"), g);
        g.gridx = 3; g.weightx = 0.45; g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(newCombo, g);

        // Row 2: navigation centered
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.add(combosRow);
        toolbar.add(buildNavPanel());

        setupContentPane(toolbar,
                headerPanel(" Old revision", leftScroll),
                headerPanel(" New revision", rightScroll));

        loadAndDiff();
    }

    /**
     * Constructor for branch-to-branch file diff.
     * Both content strings are provided directly — no revision combos shown.
     */
    public DiffViewerWindow(String fileName,
                            String leftLabel,  String leftContent,
                            String rightLabel, String rightContent) {
        this.fileName = fileName;
        setTitle("Diff: " + fileName + " (" + leftLabel + " / " + rightLabel + ")");
        initCommon(SyntaxStyleUtil.getSyntaxStyle(fileName));
        oldCombo = null;
        newCombo = null;

        setupContentPane(buildNavPanel(),
                headerPanel(" " + leftLabel, leftScroll),
                headerPanel(" " + rightLabel, rightScroll));

        this.oldText = leftContent;
        this.newText = rightContent;
        computeAndDisplayDiff();
    }

    /**
     * Constructor for diff with disk (no revision combos).
     */
    public DiffViewerWindow(String fileName, String commitSha,
                            String commitContent, String diskContent) {
        this.fileName = fileName;
        setTitle("Diff with disk: " + fileName);
        initCommon(SyntaxStyleUtil.getSyntaxStyle(fileName));
        oldCombo = null;
        newCombo = null;

        String shortSha = commitSha.length() > 8 ? commitSha.substring(0, 8) : commitSha;
        setupContentPane(buildNavPanel(),
                headerPanel(" Commit: " + shortSha, leftScroll),
                headerPanel(" Working directory", rightScroll));

        this.oldText = commitContent;
        this.newText = diskContent;
        computeAndDisplayDiff();
    }

    // ---- Private init helpers ----

    /** Initializes all shared components. Must be called first in every constructor. */
    private void initCommon(String syntaxStyle) {
        setSize(1200, 700);
        setLocationRelativeTo(null);

        oldPane = createEditor(syntaxStyle);
        newPane = createEditor(syntaxStyle);

        leftScroll = new RTextScrollPane(oldPane);
        leftScroll.setFoldIndicatorEnabled(false);
        rightScroll = new RTextScrollPane(newPane);
        rightScroll.setFoldIndicatorEnabled(false);
        centerPanel = new DiffConnectorPanel();
        syncScroll(leftScroll, rightScroll);

        prevBtn = new JButton("<< Prev");
        prevBtn.setEnabled(false);
        prevBtn.addActionListener(e -> navigateDiff(-1));

        nextBtn = new JButton("Next >>");
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> navigateDiff(1));

        diffInfoLabel = new JLabel("");
    }

    /** Centered navigation row: Prev / Next / diff-info label. */
    private JPanel buildNavPanel() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        nav.add(prevBtn);
        nav.add(nextBtn);
        nav.add(Box.createHorizontalStrut(10));
        nav.add(diffInfoLabel);
        return nav;
    }

    /** Wraps a scroll pane in a titled panel. */
    private static JPanel headerPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /** Assembles toolbar + diff panel into the content pane. */
    private void setupContentPane(JPanel toolbar, JPanel leftPanel, JPanel rightPanel) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(buildDiffPanel(leftPanel, rightPanel), BorderLayout.CENTER);
    }

    // ---- Layout ----

    private JPanel buildDiffPanel(JPanel leftPanel, JPanel rightPanel) {
        centerPanel.setPreferredSize(new Dimension(40, 0));
        centerPanel.setMinimumSize(new Dimension(40, 0));

        // Repaint center panel on scroll so connectors track line positions
        leftScroll.getVerticalScrollBar().addAdjustmentListener(e -> centerPanel.repaint());
        rightScroll.getVerticalScrollBar().addAdjustmentListener(e -> centerPanel.repaint());

        // Use GridBagLayout for 45% / center / 45% distribution
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        gbc.gridx = 0; gbc.weightx = 0.45; gbc.weighty = 1.0;
        panel.add(leftPanel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.10;
        panel.add(centerPanel, gbc);
        gbc.gridx = 2; gbc.weightx = 0.45;
        panel.add(rightPanel, gbc);

        return panel;
    }

    private RSyntaxTextArea createEditor(String syntaxStyle) {
        RSyntaxTextArea area = new RSyntaxTextArea();
        area.setSyntaxEditingStyle(syntaxStyle);
        area.setEditable(false);
        area.setCodeFoldingEnabled(false);
        area.setAntiAliasingEnabled(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setLineWrap(false);
        area.setHighlightCurrentLine(false);
        SyntaxStyleUtil.applyTheme(area);
        return area;
    }

    private void syncScroll(JScrollPane left, JScrollPane right) {
        left.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                right.getVerticalScrollBar().setValue(e.getValue());
            }
        });
        right.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                left.getVerticalScrollBar().setValue(e.getValue());
            }
        });
        left.getHorizontalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                right.getHorizontalScrollBar().setValue(e.getValue());
            }
        });
        right.getHorizontalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                left.getHorizontalScrollBar().setValue(e.getValue());
            }
        });
    }

    private void selectRevision(JComboBox<RevisionItem> combo, String sha) {
        if (sha == null || combo == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (sha.equals(combo.getItemAt(i).revision.getRevisionFullName())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadAndDiff() {
        if (oldCombo == null || newCombo == null) return;
        RevisionItem oldItem = (RevisionItem) oldCombo.getSelectedItem();
        RevisionItem newItem = (RevisionItem) newCombo.getSelectedItem();
        if (oldItem == null || newItem == null) return;

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String oldContent = readFileFromCommit(oldItem.revision.getRevisionFullName());
                String newContent = readFileFromCommit(newItem.revision.getRevisionFullName());
                return new String[]{oldContent, newContent};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    oldText = texts[0];
                    newText = texts[1];
                    computeAndDisplayDiff();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load files for diff", e);
                    oldPane.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String readFileFromCommit(String sha) {
        try {
            String tempPath = Context.getGitRepoService().saveFile(sha, fileName);
            return Files.readString(Paths.get(tempPath));
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to read file from commit " + sha, e);
            return "";
        }
    }

    private void computeAndDisplayDiff() {
        if (oldText == null || newText == null) return;

        // Compute diff using JGit HISTOGRAM algorithm
        RawText oldRaw = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        RawText newRaw = new RawText(newText.getBytes(StandardCharsets.UTF_8));
        DiffAlgorithm algorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        editList = algorithm.diff(RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

        // Set text content (RSyntaxTextArea handles syntax highlighting)
        oldPane.setText(oldText);
        oldPane.setCaretPosition(0);
        newPane.setText(newText);
        newPane.setCaretPosition(0);

        // Apply diff line highlights
        applyLineHighlights(oldPane, editList, true);
        applyLineHighlights(newPane, editList, false);

        // Update navigation
        currentDiff = -1;
        updateNavState();
        diffInfoLabel.setText(editList.size() + " difference" + (editList.size() != 1 ? "s" : ""));

        // Repaint center connector panel
        centerPanel.repaint();

        // Auto-scroll to first diff
        if (!editList.isEmpty()) {
            currentDiff = 0;
            scrollToCurrentDiff();
            updateNavState();
        }
    }

    private void applyLineHighlights(RSyntaxTextArea pane, EditList edits, boolean isOld) {
        pane.removeAllLineHighlights();

        for (Edit edit : edits) {
            int beginLine, endLine;
            Color color;

            if (isOld) {
                beginLine = edit.getBeginA();
                endLine = edit.getEndA();
                color = switch (edit.getType()) {
                    case DELETE -> deletedBg();
                    case REPLACE -> changedBg();
                    default -> null;
                };
            } else {
                beginLine = edit.getBeginB();
                endLine = edit.getEndB();
                color = switch (edit.getType()) {
                    case INSERT -> addedBg();
                    case REPLACE -> changedBg();
                    default -> null;
                };
            }

            if (color != null) {
                for (int line = beginLine; line < endLine; line++) {
                    try {
                        pane.addLineHighlight(line, color);
                    } catch (Exception e) {
                        log.log(Level.FINE, "Failed to highlight line " + line, e);
                    }
                }
            }
        }
    }

    private void navigateDiff(int direction) {
        if (editList == null || editList.isEmpty()) return;
        currentDiff += direction;
        currentDiff = Math.max(0, Math.min(currentDiff, editList.size() - 1));
        scrollToCurrentDiff();
        updateNavState();
    }

    private void scrollToCurrentDiff() {
        if (editList == null || currentDiff < 0 || currentDiff >= editList.size()) return;

        Edit edit = editList.get(currentDiff);
        scrollToLine(oldPane, edit.getBeginA());
        scrollToLine(newPane, edit.getBeginB());
    }

    private void scrollToLine(RSyntaxTextArea pane, int lineNumber) {
        try {
            if (lineNumber >= pane.getLineCount()) return;
            int offset = pane.getLineStartOffset(lineNumber);
            pane.setCaretPosition(offset);

            Rectangle rect = pane.modelToView(offset);
            if (rect != null) {
                rect.y = Math.max(0, rect.y - 60);
                rect.height = pane.getVisibleRect().height;
                pane.scrollRectToVisible(rect);
            }
        } catch (Exception e) {
            log.log(Level.FINE, "Failed to scroll to line", e);
        }
    }

    private void updateNavState() {
        if (editList == null || editList.isEmpty()) {
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            diffInfoLabel.setText("No differences");
        } else {
            prevBtn.setEnabled(currentDiff > 0);
            nextBtn.setEnabled(currentDiff < editList.size() - 1);
            diffInfoLabel.setText("Diff " + (currentDiff + 1) + " / " + editList.size());
        }
    }

    /**
     * Custom panel that draws colored trapezoid connectors between
     * corresponding diff regions in the left and right editors.
     */
    private class DiffConnectorPanel extends JPanel {

        DiffConnectorPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (editList == null || editList.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();

            // Compute Y offset of each viewport relative to this panel
            int leftOffsetY = getViewportOffsetY(leftScroll);
            int rightOffsetY = getViewportOffsetY(rightScroll);
            int leftScrollPos = leftScroll.getViewport().getViewPosition().y;
            int rightScrollPos = rightScroll.getViewport().getViewPosition().y;

            int panelHeight = getHeight();

            for (Edit edit : editList) {
                Color color = getConnectorColor(edit);
                if (color == null) continue;

                int leftTop    = getLineYInEditor(oldPane, edit.getBeginA()) - leftScrollPos  + leftOffsetY;
                int leftBottom = getLineYInEditor(oldPane, edit.getEndA())   - leftScrollPos  + leftOffsetY;
                int rightTop   = getLineYInEditor(newPane, edit.getBeginB()) - rightScrollPos + rightOffsetY;
                int rightBottom= getLineYInEditor(newPane, edit.getEndB())   - rightScrollPos + rightOffsetY;

                if (leftBottom < 0 && rightBottom < 0) continue;
                if (leftTop > panelHeight && rightTop > panelHeight) continue;

                if (leftTop  == leftBottom)  leftBottom  = leftTop  + 2;
                if (rightTop == rightBottom) rightBottom = rightTop + 2;

                float cx1 = w * 0.4f;
                float cx2 = w * 0.6f;

                java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
                path.moveTo(0, leftTop);
                path.curveTo(cx1, leftTop,    cx2, rightTop,    w, rightTop);
                path.lineTo(w, rightBottom);
                path.curveTo(cx2, rightBottom, cx1, leftBottom, 0, leftBottom);
                path.closePath();

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2.fill(path);

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new java.awt.geom.CubicCurve2D.Float(
                        0, leftTop,    cx1, leftTop,    cx2, rightTop,    w, rightTop));
                g2.draw(new java.awt.geom.CubicCurve2D.Float(
                        0, leftBottom, cx1, leftBottom, cx2, rightBottom, w, rightBottom));
            }

            g2.dispose();
        }

        private Color getConnectorColor(Edit edit) {
            return switch (edit.getType()) {
                case DELETE  -> deletedBg();
                case INSERT  -> addedBg();
                case REPLACE -> changedBg();
                default      -> null;
            };
        }

        private int getLineYInEditor(RSyntaxTextArea pane, int lineNumber) {
            try {
                int lineCount = pane.getLineCount();
                if (lineNumber >= lineCount) lineNumber = lineCount - 1;
                if (lineNumber < 0) lineNumber = 0;
                int offset = pane.getLineStartOffset(lineNumber);
                Rectangle rect = pane.modelToView(offset);
                if (rect != null) return rect.y;
            } catch (Exception ignored) {
            }
            return 0;
        }

        private int getViewportOffsetY(RTextScrollPane scrollPane) {
            try {
                Point p = SwingUtilities.convertPoint(scrollPane.getViewport(), 0, 0, this);
                return p.y;
            } catch (Exception e) {
                return 0;
            }
        }
    }

    // Revision combo item
    private static class RevisionItem {
        final ScmRevisionInformation revision;

        RevisionItem(ScmRevisionInformation revision) {
            this.revision = revision;
        }

        @Override
        public String toString() {
            String sha = revision.getRevisionFullName();
            String shortSha = sha != null && sha.length() > 8 ? sha.substring(0, 8) : (sha != null ? sha : "");
            String msg = revision.getShortMessage();
            if (msg != null && msg.length() > 50) msg = msg.substring(0, 50) + "...";
            String date = revision.getDate() != null ? GitemberUtil.formatDate(revision.getDate()) : "";
            return shortSha + " " + date + " " + (msg != null ? msg : "");
        }
    }
}
