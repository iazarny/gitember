package com.az.gitember.ui;

import com.az.gitember.data.MergeConflictBlock;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Three-pane conflict resolution window with interactive connector panels.
 *
 * <pre>
 *  ┌──────────────┐  ┌──────────┐  ┌─────────────┐  ┌──────────┐  ┌────────────┐
 *  │ Mine (OURS)  │  │  LEFT    │  │   Result    │  │  RIGHT   │  │   Theirs   │
 *  │  stage 2     │  │connector │  │  editable   │  │connector │  │  stage 3   │
 *  │  (green)     │  │  [→] btn │  │             │  │  [←] btn │  │   (red)    │
 *  └──────────────┘  └──────────┘  └─────────────┘  └──────────┘  └────────────┘
 * </pre>
 *
 * OURS (left) and THEIRS (right) are loaded from git stages 2 and 3 respectively —
 * the real pre-merge versions, not text extracted from the conflict markers.
 * Bezier connector panels draw colour-coded trapezoids between each conflict
 * region in the static panes and the corresponding section in the editable result.
 * Clicking a [→] / [←] arrow button inside a connector accepts that side.
 */
public class ThreeWayMergeWindow extends JFrame {

    private static final Logger log = Logger.getLogger(ThreeWayMergeWindow.class.getName());

    private static final int CONNECTOR_WIDTH = 50;

    // ---- state ---------------------------------------------------------------
    private final String filePath;
    private final List<MergeConflictBlock> blocks = new ArrayList<>();

    /**
     * Indices into {@link #blocks} that are still unresolved in the result pane.
     * Maintained in sync when the user uses the accept buttons; trimmed on free edits.
     */
    private final List<Integer> unresolvedIndices = new ArrayList<>();

    /** Full text for the OURS (left) pane — loaded from git stage 2. */
    private String oursFullText;
    /** Full text for the THEIRS (right) pane — loaded from git stage 3. */
    private String theirsFullText;

    // ---- RSyntaxTextArea panes -----------------------------------------------
    private RSyntaxTextArea oursPane;
    private RSyntaxTextArea resultPane;
    private RSyntaxTextArea theirsPane;

    private RTextScrollPane oursScroll;
    private RTextScrollPane resultScroll;
    private RTextScrollPane theirsScroll;

    // ---- connector panels ----------------------------------------------------
    private MergeConnectorPanel leftConnector;   // OURS ↔ RESULT
    private MergeConnectorPanel rightConnector;  // RESULT ↔ THEIRS

    // ---- toolbar controls ----------------------------------------------------
    private JLabel  conflictLabel;
    private JButton prevBtn, nextBtn;
    private JButton acceptOursBtn, acceptTheirsBtn;
    private JButton acceptAllOursBtn, acceptAllTheirsBtn;
    private JButton saveBtn;

    private int     currentBlock  = 0;
    private boolean scrollSyncing = false;

    // ==========================================================================
    //  Construction
    // ==========================================================================

    public ThreeWayMergeWindow(String filePath) {
        this.filePath = filePath;
        String fileName = Paths.get(filePath).getFileName().toString();
        setTitle("Merge conflict: " + fileName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. Read the conflicted working-tree file
        String conflictedContent;
        try {
            conflictedContent = new String(
                    Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").replace("\r", "\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot read: " + filePath, e);
            JOptionPane.showMessageDialog(null,
                    "Cannot read file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Parse conflict blocks; also build derived ours/theirs as fallback
        parseConflicts(conflictedContent);

        // 3. Replace ours/theirs with real git-staged content (stage 2 / stage 3)
        loadStagedContent();

        String syntaxStyle = SyntaxStyleUtil.getSyntaxStyle(fileName);
        buildUI(conflictedContent, syntaxStyle);

        if (!blocks.isEmpty()) {
            currentBlock = 0;
            SwingUtilities.invokeLater(() -> scrollToDiff(0));
        }
        updateConflictLabel();
        updateButtonStates();

        setSize(1400, 800);
        setLocationRelativeTo(null);
        Util.bindEscapeToDispose(this);
        setVisible(true);
    }

    // ==========================================================================
    //  Parsing conflict markers
    // ==========================================================================

    /**
     * Parses the conflicted file to populate {@link #blocks}.
     *
     * <p>For each block the method computes line-number ranges in the staged files
     * (stage 2 / stage 3) using context-line counting:
     * <ul>
     *   <li>context lines before block {@code i} are the same in every stage</li>
     *   <li>block {@code i} in stage 2 starts at: (context lines before i) + (sum of ours lines in blocks 0..i-1)</li>
     *   <li>block {@code i} in stage 3 starts at: (context lines before i) + (sum of theirs lines in blocks 0..i-1)</li>
     * </ul>
     *
     * Also builds fallback {@link #oursFullText} / {@link #theirsFullText} from the
     * parsed content (used when the staged blob cannot be loaded).
     */
    private void parseConflicts(String content) {
        String[] lines = content.split("\n", -1);

        // Fallback derived texts
        StringBuilder oursSb   = new StringBuilder();
        StringBuilder theirsSb = new StringBuilder();

        int state = 0;
        String oursMarker = null, theirsMarker;
        List<String> curOurs   = new ArrayList<>();
        List<String> curTheirs = new ArrayList<>();

        // Running line counters in the staged files
        int oursLine   = 0;  // next line index to be written in stage-2 file
        int theirsLine = 0;  // next line index to be written in stage-3 file

        for (String line : lines) {
            switch (state) {
                case 0 -> {
                    if (line.startsWith("<<<<<<<")) {
                        oursMarker = line;
                        curOurs.clear();
                        curTheirs.clear();
                        state = 1;
                    } else {
                        // Context line: present in all stages
                        oursSb.append(line).append('\n');
                        theirsSb.append(line).append('\n');
                        oursLine++;
                        theirsLine++;
                    }
                }
                case 1 -> {
                    if (line.startsWith("=======")) state = 2;
                    else curOurs.add(line);
                }
                case 2 -> {
                    if (line.startsWith(">>>>>>>")) {
                        theirsMarker = line;
                        MergeConflictBlock block = new MergeConflictBlock(
                                blocks.size(), oursMarker, theirsMarker,
                                new ArrayList<>(curOurs), new ArrayList<>(curTheirs));

                        // Line ranges in staged files
                        block.oursStartLine  = oursLine;
                        block.oursEndLine    = oursLine + curOurs.size();
                        block.theirsStartLine = theirsLine;
                        block.theirsEndLine   = theirsLine + curTheirs.size();

                        // Advance line counters
                        oursLine   += curOurs.size();
                        theirsLine += curTheirs.size();

                        // Build fallback derived texts
                        for (String l : curOurs)   oursSb.append(l).append('\n');
                        for (String l : curTheirs) theirsSb.append(l).append('\n');

                        blocks.add(block);
                        state = 0;
                    } else {
                        curTheirs.add(line);
                    }
                }
            }
        }

        oursFullText   = oursSb.toString();
        theirsFullText = theirsSb.toString();
        for (int i = 0; i < blocks.size(); i++) unresolvedIndices.add(i);
    }

    // ==========================================================================
    //  Load real staged content (stage 2 = OURS, stage 3 = THEIRS)
    // ==========================================================================

    private void loadStagedContent() {
        try {
            var workTree = Context.getGitRepoService().getRepository().getWorkTree().toPath();
            String relative = workTree.relativize(Paths.get(filePath)).toString()
                    .replace('\\', '/');

            String staged2 = Context.getGitRepoService().getStagedFileContent(relative, 2);
            if (staged2 != null && !staged2.isBlank()) {
                oursFullText = staged2.replace("\r\n", "\n").replace("\r", "\n");
            }

            String staged3 = Context.getGitRepoService().getStagedFileContent(relative, 3);
            if (staged3 != null && !staged3.isBlank()) {
                theirsFullText = staged3.replace("\r\n", "\n").replace("\r", "\n");
            }
        } catch (Exception e) {
            log.log(Level.WARNING,
                    "Cannot load staged blobs, using derived text from markers", e);
        }
    }

    // ==========================================================================
    //  UI layout
    // ==========================================================================

    private void buildUI(String initialResult, String syntaxStyle) {
        oursPane   = createEditor(syntaxStyle, false);
        resultPane = createEditor(syntaxStyle, true);
        theirsPane = createEditor(syntaxStyle, false);

        oursPane.setText(oursFullText);
        resultPane.setText(initialResult);
        resultPane.setCaretPosition(0);
        theirsPane.setText(theirsFullText);

        oursScroll   = new RTextScrollPane(oursPane);
        resultScroll = new RTextScrollPane(resultPane);
        theirsScroll = new RTextScrollPane(theirsPane);
        for (RTextScrollPane sp : List.of(oursScroll, resultScroll, theirsScroll))
            sp.setLineNumbersEnabled(true);

        applyOursHighlights();
        applyTheirsHighlights();
        applyResultHighlights();

        leftConnector  = new MergeConnectorPanel(true);
        rightConnector = new MergeConnectorPanel(false);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.weightx = 0.33;
        center.add(titledPanel("Mine (OURS)",       oursScroll),   gbc);
        gbc.gridx = 1; gbc.weightx = 0.0;
        center.add(leftConnector,                                  gbc);
        gbc.gridx = 2; gbc.weightx = 0.34;
        center.add(titledPanel("Result (editable)", resultScroll), gbc);
        gbc.gridx = 3; gbc.weightx = 0.0;
        center.add(rightConnector,                                 gbc);
        gbc.gridx = 4; gbc.weightx = 0.33;
        center.add(titledPanel("Theirs",            theirsScroll), gbc);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildToolbar(), BorderLayout.NORTH);
        getContentPane().add(center,         BorderLayout.CENTER);

        setupScrollSync();
        setupResultDocumentListener();
    }

    private static RSyntaxTextArea createEditor(String syntaxStyle, boolean editable) {
        RSyntaxTextArea area = new RSyntaxTextArea();
        area.setSyntaxEditingStyle(syntaxStyle);
        area.setEditable(editable);
        area.setCodeFoldingEnabled(false);
        area.setAntiAliasingEnabled(true);
        area.setLineWrap(false);
        area.setHighlightCurrentLine(editable);
        SyntaxStyleUtil.applyTheme(area);
        area.setFont(SyntaxStyleUtil.monoFont());
        return area;
    }

    private static JPanel titledPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(" " + title);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        p.add(l,       BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildToolbar() {
        prevBtn  = new JButton("◀ Prev");
        nextBtn  = new JButton("Next ▶");
        conflictLabel      = new JLabel("—");
        acceptOursBtn      = new JButton("← Accept Mine");
        acceptTheirsBtn    = new JButton("Accept Theirs →");
        acceptAllOursBtn   = new JButton("Accept All Mine");
        acceptAllTheirsBtn = new JButton("Accept All Theirs");
        saveBtn = new JButton("Save & Mark Resolved");

        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn.addActionListener(e -> navigate(+1));
        acceptOursBtn.addActionListener(e -> acceptCurrent(true));
        acceptTheirsBtn.addActionListener(e -> acceptCurrent(false));
        acceptAllOursBtn.addActionListener(e -> acceptAll(true));
        acceptAllTheirsBtn.addActionListener(e -> acceptAll(false));
        saveBtn.addActionListener(e -> saveAndMarkResolved());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        p.add(prevBtn); p.add(conflictLabel); p.add(nextBtn);
        p.add(Box.createHorizontalStrut(16));
        p.add(acceptOursBtn); p.add(acceptTheirsBtn);
        p.add(Box.createHorizontalStrut(8));
        p.add(acceptAllOursBtn); p.add(acceptAllTheirsBtn);
        p.add(Box.createHorizontalStrut(16));
        p.add(saveBtn);
        return p;
    }

    // ==========================================================================
    //  Scroll synchronisation
    // ==========================================================================

    private void setupScrollSync() {
        AdjustmentListener vertSync = e -> {
            if (scrollSyncing) return;
            scrollSyncing = true;
            int v = e.getValue();
            oursScroll  .getVerticalScrollBar().setValue(v);
            resultScroll.getVerticalScrollBar().setValue(v);
            theirsScroll.getVerticalScrollBar().setValue(v);
            leftConnector.repaint();
            rightConnector.repaint();
            scrollSyncing = false;
        };
        oursScroll  .getVerticalScrollBar().addAdjustmentListener(vertSync);
        resultScroll.getVerticalScrollBar().addAdjustmentListener(vertSync);
        theirsScroll.getVerticalScrollBar().addAdjustmentListener(vertSync);
    }

    // ==========================================================================
    //  Result pane document listener
    // ==========================================================================

    private void setupResultDocumentListener() {
        resultPane.getDocument().addDocumentListener(new DocumentListener() {
            private boolean pending = false;
            private void schedule() {
                if (pending) return;
                pending = true;
                SwingUtilities.invokeLater(() -> {
                    pending = false;
                    applyResultHighlights();
                    reconcileUnresolved();
                    leftConnector.repaint();
                    rightConnector.repaint();
                    updateConflictLabel();
                    updateButtonStates();
                });
            }
            public void insertUpdate(DocumentEvent e)  { schedule(); }
            public void removeUpdate(DocumentEvent e)  { schedule(); }
            public void changedUpdate(DocumentEvent e) { /* style */ }
        });
    }

    /** Trim unresolvedIndices when the user manually removes conflict markers. */
    private void reconcileUnresolved() {
        int actual = countRemainingConflicts();
        while (unresolvedIndices.size() > actual)
            unresolvedIndices.remove(unresolvedIndices.size() - 1);
    }

    // ==========================================================================
    //  Highlighting
    // ==========================================================================

    private void applyOursHighlights() {
        oursPane.removeAllLineHighlights();
        for (MergeConflictBlock b : blocks) {
            for (int i = b.oursStartLine; i < b.oursEndLine; i++) {
                try { oursPane.addLineHighlight(i, SyntaxStyleUtil.addedBg()); }
                catch (Exception ex) { log.log(Level.FINE, "ours hl", ex); }
            }
        }
    }

    private void applyTheirsHighlights() {
        theirsPane.removeAllLineHighlights();
        for (MergeConflictBlock b : blocks) {
            for (int i = b.theirsStartLine; i < b.theirsEndLine; i++) {
                try { theirsPane.addLineHighlight(i, SyntaxStyleUtil.deletedBg()); }
                catch (Exception ex) { log.log(Level.FINE, "theirs hl", ex); }
            }
        }
    }

    void applyResultHighlights() {
        resultPane.removeAllLineHighlights();
        String[] lines = resultPane.getText().split("\n", -1);
        int state = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            try {
                if      (state == 0 && line.startsWith("<<<<<<<")) { resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());  state = 1; }
                else if (state == 1 && line.startsWith("=======")) { resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());  state = 2; }
                else if (state == 1)                                { resultPane.addLineHighlight(i, SyntaxStyleUtil.addedBg());             }
                else if (state == 2 && line.startsWith(">>>>>>>")) { resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());  state = 0; }
                else if (state == 2)                                { resultPane.addLineHighlight(i, SyntaxStyleUtil.deletedBg());           }
            } catch (Exception ex) { log.log(Level.FINE, "result hl", ex); }
        }
    }

    // ==========================================================================
    //  Navigation
    // ==========================================================================

    private void navigate(int delta) {
        int remaining = countRemainingConflicts();
        if (remaining == 0) return;
        currentBlock = Math.max(0, Math.min(currentBlock + delta, remaining - 1));
        scrollToDiff(currentBlock);
        updateConflictLabel();
        updateButtonStates();
    }

    private void scrollToDiff(int nthBlock) {
        // Scroll result to nth <<<<<<< line
        String[] lines = resultPane.getText().split("\n", -1);
        int count = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("<<<<<<<")) {
                if (count == nthBlock) {
                    scrollToLine(resultPane, resultScroll, i);
                    if (nthBlock < unresolvedIndices.size()) {
                        int origIdx = unresolvedIndices.get(nthBlock);
                        MergeConflictBlock b = blocks.get(origIdx);
                        scrollToLine(oursPane,   oursScroll,   b.oursStartLine);
                        scrollToLine(theirsPane, theirsScroll, b.theirsStartLine);
                    }
                    return;
                }
                count++;
            }
        }
    }

    private void scrollToLine(RSyntaxTextArea pane, RTextScrollPane scroll, int lineNumber) {
        try {
            if (lineNumber < 0 || lineNumber >= pane.getLineCount()) return;
            int offset = pane.getLineStartOffset(lineNumber);
            pane.setCaretPosition(offset);
            Rectangle rect = pane.modelToView(offset);
            if (rect == null) return;
            JScrollBar bar = scroll.getVerticalScrollBar();
            int margin = Math.max(40, scroll.getViewport().getHeight() / 4);
            int target = Math.max(0, rect.y - margin);
            bar.setValue(Math.min(target, bar.getMaximum() - bar.getModel().getExtent()));
        } catch (Exception ex) { log.log(Level.FINE, "scroll", ex); }
    }

    // ==========================================================================
    //  Accept operations
    // ==========================================================================

    private void acceptCurrent(boolean useOurs) {
        if (countRemainingConflicts() == 0) return;
        doAccept(currentBlock, useOurs);
    }

    private void acceptAll(boolean useOurs) {
        int remaining = countRemainingConflicts();
        for (int i = 0; i < remaining; i++) doAccept(0, useOurs);
    }

    void doAccept(int nthBlock, boolean useOurs) {
        if (nthBlock < unresolvedIndices.size())
            unresolvedIndices.remove(nthBlock);

        replaceConflictBlock(nthBlock, useOurs);
        applyResultHighlights();

        int remaining = countRemainingConflicts();
        if (remaining > 0) {
            currentBlock = Math.min(currentBlock, remaining - 1);
            SwingUtilities.invokeLater(() -> scrollToDiff(currentBlock));
        } else {
            currentBlock = 0;
        }
        leftConnector.repaint();
        rightConnector.repaint();
        updateConflictLabel();
        updateButtonStates();
    }

    private void replaceConflictBlock(int nthBlock, boolean useOurs) {
        String[] raw = resultPane.getText().split("\n", -1);
        int blockCount = -1, state = 0, blockStart = -1;
        List<String> oursSec = new ArrayList<>(), theirsSec = new ArrayList<>();

        for (int i = 0; i < raw.length; i++) {
            String line = raw[i];
            if (state == 0 && line.startsWith("<<<<<<<")) {
                blockCount++;
                if (blockCount == nthBlock) {
                    blockStart = i; oursSec.clear(); theirsSec.clear(); state = 1;
                }
            } else if (state == 1) {
                if (line.startsWith("=======")) state = 2; else oursSec.add(line);
            } else if (state == 2) {
                if (line.startsWith(">>>>>>>")) {
                    final int blockEnd = i;
                    List<String> chosen = useOurs ? oursSec : theirsSec;
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < raw.length; j++) {
                        if (j == blockStart) {
                            for (String cl : chosen) sb.append(cl).append('\n');
                        } else if (j > blockStart && j <= blockEnd) {
                            // replaced
                        } else {
                            sb.append(raw[j]);
                            if (j < raw.length - 1) sb.append('\n');
                        }
                    }
                    resultPane.setText(sb.toString());
                    return;
                } else theirsSec.add(line);
            }
        }
    }

    // ==========================================================================
    //  Save & stage
    // ==========================================================================

    private void saveAndMarkResolved() {
        if (countRemainingConflicts() > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "There are still unresolved conflict markers.\nSave and mark as resolved anyway?",
                    "Unresolved conflicts", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        try {
            Files.write(Paths.get(filePath),
                    resultPane.getText().getBytes(StandardCharsets.UTF_8));
            var workTree = Context.getGitRepoService().getRepository().getWorkTree().toPath();
            String relative = workTree.relativize(Paths.get(filePath)).toString().replace('\\', '/');
            Context.getGitRepoService().addFileToCommitStage(relative);
            Context.refreshWorkingCopy();
            JOptionPane.showMessageDialog(this, "File saved and staged.",
                    "Resolved", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Cannot save: " + filePath, ex);
            JOptionPane.showMessageDialog(this,
                    "Error saving:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================================================
    //  Helpers
    // ==========================================================================

    private int countRemainingConflicts() {
        String text = resultPane.getText();
        int count = 0, idx = 0;
        while (idx < text.length()) {
            int pos = text.indexOf("<<<<<<<", idx);
            if (pos < 0) break;
            count++; idx = pos + 7;
        }
        return count;
    }

    /**
     * Returns, for each remaining conflict block in the result pane, a triple:
     * {@code [oursMarkerLine, sepLine, theirsMarkerLine]}.
     */
    private List<int[]> getResultConflictLineRanges() {
        List<int[]> result = new ArrayList<>();
        String[] lines = resultPane.getText().split("\n", -1);
        int state = 0, oursLine = -1, sepLine = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if      (state == 0 && line.startsWith("<<<<<<<")) { oursLine = i; state = 1; }
            else if (state == 1 && line.startsWith("=======")) { sepLine  = i; state = 2; }
            else if (state == 2 && line.startsWith(">>>>>>>")) { result.add(new int[]{oursLine, sepLine, i}); state = 0; }
        }
        return result;
    }

    private void updateConflictLabel() {
        int remaining = countRemainingConflicts();
        conflictLabel.setText(remaining == 0
                ? "All conflicts resolved ✓"
                : "Conflict " + (currentBlock + 1) + " of " + remaining);
    }

    private void updateButtonStates() {
        int remaining = countRemainingConflicts();
        boolean has = remaining > 0;
        prevBtn.setEnabled(has && currentBlock > 0);
        nextBtn.setEnabled(has && currentBlock < remaining - 1);
        acceptOursBtn.setEnabled(has);
        acceptTheirsBtn.setEnabled(has);
        acceptAllOursBtn.setEnabled(has);
        acceptAllTheirsBtn.setEnabled(has);
    }

    // ==========================================================================
    //  Inner class: MergeConnectorPanel
    // ==========================================================================

    /**
     * Draws bezier trapezoids between a static OURS/THEIRS pane and the editable
     * RESULT pane.  A small arrow button in the centre of each connector accepts
     * the corresponding block when clicked.
     *
     * <p>{@code isLeft == true}  → OURS ↔ RESULT  (click [→] to accept mine)</p>
     * <p>{@code isLeft == false} → RESULT ↔ THEIRS (click [←] to accept theirs)</p>
     */
    private class MergeConnectorPanel extends JPanel {

        private static final int BTN_W = 22;
        private static final int BTN_H = 18;

        private final boolean isLeft;
        private final List<Rectangle> btnRects    = new ArrayList<>();
        private final List<Integer>   btnBlockIdx = new ArrayList<>();

        MergeConnectorPanel(boolean isLeft) {
            this.isLeft = isLeft;
            setPreferredSize(new Dimension(CONNECTOR_WIDTH, 0));
            setMinimumSize  (new Dimension(CONNECTOR_WIDTH, 0));
            setMaximumSize  (new Dimension(CONNECTOR_WIDTH, Integer.MAX_VALUE));
            setOpaque(true);
            setToolTipText(isLeft ? "Click → to accept Mine" : "Click ← to accept Theirs");

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { handleClick(e); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            btnRects.clear();
            btnBlockIdx.clear();

            List<int[]> resultRanges = getResultConflictLineRanges();
            int n = Math.min(unresolvedIndices.size(), resultRanges.size());
            if (n == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), panelH = getHeight();

            for (int i = 0; i < n; i++) {
                int origIdx = unresolvedIndices.get(i);
                MergeConflictBlock block  = blocks.get(origIdx);
                int[]              range  = resultRanges.get(i);

                // Y coords in the static pane (OURS or THEIRS)
                int staticTop, staticBot;
                if (isLeft) {
                    staticTop = lineY(oursPane,   oursScroll,   block.oursStartLine);
                    staticBot = lineY(oursPane,   oursScroll,   block.oursEndLine);
                } else {
                    staticTop = lineY(theirsPane, theirsScroll, block.theirsStartLine);
                    staticBot = lineY(theirsPane, theirsScroll, block.theirsEndLine);
                }

                // Y coords in the result pane
                // Left connector → ours section: <<<<<<< to =======
                // Right connector → theirs section: ======= to >>>>>>>
                int resultTop, resultBot;
                if (isLeft) {
                    resultTop = lineY(resultPane, resultScroll, range[0]);
                    resultBot = lineY(resultPane, resultScroll, range[1] + 1);
                } else {
                    resultTop = lineY(resultPane, resultScroll, range[1]);
                    resultBot = lineY(resultPane, resultScroll, range[2] + 1);
                }

                if (staticBot < 0 && resultBot < 0) continue;
                if (staticTop > panelH && resultTop > panelH) continue;
                if (staticTop == staticBot) staticBot = staticTop + 2;
                if (resultTop == resultBot) resultBot = resultTop + 2;

                // Left connector: left=static, right=result
                // Right connector: left=result, right=static
                int lTop = isLeft ? staticTop : resultTop;
                int lBot = isLeft ? staticBot : resultBot;
                int rTop = isLeft ? resultTop  : staticTop;
                int rBot = isLeft ? resultBot  : staticBot;

                boolean isCurrent = (i == currentBlock && currentBlock < n);
                Color fill = isCurrent
                        ? SyntaxStyleUtil.changedBg()
                        : (isLeft ? SyntaxStyleUtil.addedBg() : SyntaxStyleUtil.deletedBg());

                drawTrapezoid(g2, w, lTop, lBot, rTop, rBot,
                        new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 210));

                // Arrow button at the midpoint of the connector
                int midY = (Math.max(lTop, rTop) + Math.min(lBot, rBot)) / 2;
                int btnX = (w - BTN_W) / 2;
                int btnY = midY - BTN_H / 2;
                Rectangle btn = new Rectangle(btnX, btnY, BTN_W, BTN_H);
                drawArrowButton(g2, btn, isLeft ? "→" : "←");
                btnRects.add(btn);
                btnBlockIdx.add(i);
            }
            g2.dispose();
        }

        private void drawTrapezoid(Graphics2D g2, int w,
                                   int lTop, int lBot, int rTop, int rBot, Color color) {
            float cx1 = w * 0.35f, cx2 = w * 0.65f;
            GeneralPath path = new GeneralPath();
            path.moveTo(0, lTop);
            path.curveTo(cx1, lTop, cx2, rTop, w, rTop);
            path.lineTo(w, rBot);
            path.curveTo(cx2, rBot, cx1, lBot, 0, lBot);
            path.closePath();
            g2.setColor(color);
            g2.fill(path);
            g2.setColor(color.darker());
            g2.setStroke(new java.awt.BasicStroke(0.8f));
            g2.draw(new java.awt.geom.CubicCurve2D.Float(0, lTop, cx1, lTop, cx2, rTop, w, rTop));
            g2.draw(new java.awt.geom.CubicCurve2D.Float(0, lBot, cx1, lBot, cx2, rBot, w, rBot));
        }

        private void drawArrowButton(Graphics2D g2, Rectangle r, String arrow) {
            Color bg = UIManager.getColor("Button.background");
            if (bg == null) bg = new Color(220, 220, 220);
            g2.setColor(bg);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
            Color fg = UIManager.getColor("Button.foreground");
            g2.setColor(fg != null ? fg : Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(1f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 6, 6);
            Font f = g2.getFont().deriveFont(Font.BOLD, 11f);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width  - fm.stringWidth(arrow)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(arrow, tx, ty);
        }

        private void handleClick(MouseEvent e) {
            for (int i = 0; i < btnRects.size(); i++) {
                if (btnRects.get(i).contains(e.getPoint())) {
                    doAccept(btnBlockIdx.get(i), isLeft);
                    return;
                }
            }
        }

        /** Y position (in this panel's coordinate space) of the given line in pane/scroll. */
        private int lineY(RSyntaxTextArea pane, RTextScrollPane scroll, int lineNum) {
            try {
                int lc = pane.getLineCount();
                lineNum = Math.max(0, Math.min(lineNum, lc - 1));
                int offset = pane.getLineStartOffset(lineNum);
                Rectangle rect = pane.modelToView(offset);
                if (rect == null) return 0;
                int scrollY = scroll.getViewport().getViewPosition().y;
                Point vpInPanel = SwingUtilities.convertPoint(scroll.getViewport(), 0, 0, this);
                return rect.y - scrollY + vpInPanel.y;
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
