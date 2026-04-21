package com.az.gitember.ui;

import com.az.gitember.data.MergeConflictBlock;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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
 * Three-pane conflict resolution window.
 *
 * Shows OURS (left), editable RESULT (centre), and THEIRS (right) side by side.
 * Bezier connector panels in between draw coloured trapezoids that visually link
 * each conflict region across the three panes, and host small arrow buttons so
 * the user can accept one side with a single click.
 *
 * <pre>
 *  ┌──────────────┐  ┌──────────┐  ┌─────────────┐  ┌──────────┐  ┌────────────┐
 *  │  Ours        │  │  LEFT    │  │   Result    │  │  RIGHT   │  │   Theirs   │
 *  │  (stage 2)   │  │connector │  │  (editable) │  │connector │  │  (stage 3) │
 *  │  green tint  │  │  [→] btn │  │             │  │  [←] btn │  │  red tint  │
 *  └──────────────┘  └──────────┘  └─────────────┘  └──────────┘  └────────────┘
 * </pre>
 *
 * OURS and THEIRS are loaded from the git object store (stages 2 and 3) so the
 * user sees the real pre-merge content, not just what was extracted from the
 * conflict markers. If the staged blobs are unavailable for any reason we fall
 * back to text derived from the markers, which is usually good enough.
 *
 * Scroll position is synchronised across all three panes. The connector panels
 * repaint on every scroll event so the trapezoids always line up correctly.
 */
public class ThreeWayMergeWindow extends JFrame {

    private static final Logger log = Logger.getLogger(ThreeWayMergeWindow.class.getName());

    // How wide the connector columns between the panes should be.
    // Needs to be wide enough to fit the arrow button plus some breathing room
    // for the bezier curves to look reasonable.
    private static final int CONNECTOR_WIDTH = 54;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Absolute path to the file being resolved. */
    private final String filePath;

    /**
     * All conflict blocks found in the file, in order.
     * Each block knows its line ranges in the staged OURS and THEIRS blobs,
     * which lets us highlight the corresponding lines in those panes.
     */
    private final List<MergeConflictBlock> blocks = new ArrayList<>();

    /**
     * The subset of block indices that are still unresolved in the result pane.
     * We keep this in sync with the accept buttons so that the connector panel
     * knows which trapezoids to draw. When the user edits the result pane
     * manually we reconcile this list against the actual marker count.
     *
     * Note: indices here refer to positions in {@link #blocks}, not sequential
     * counters — after accepting block 1 the list might contain [0, 2, 3].
     */
    private final List<Integer> unresolvedIndices = new ArrayList<>();

    /**
     * Full text of the OURS version (git stage 2).
     * Populated first from the markers as a fallback, then replaced with the
     * real staged blob if that is available.
     */
    private String oursFullText;

    /** Full text of the THEIRS version (git stage 3). Same strategy as oursFullText. */
    private String theirsFullText;

    // -------------------------------------------------------------------------
    // UI components
    // -------------------------------------------------------------------------

    private RSyntaxTextArea oursPane;
    private RSyntaxTextArea resultPane;
    private RSyntaxTextArea theirsPane;

    private RTextScrollPane oursScroll;
    private RTextScrollPane resultScroll;
    private RTextScrollPane theirsScroll;

    /** Draws the bezier connection between oursPane and resultPane. */
    private MergeConnectorPanel leftConnector;

    /** Draws the bezier connection between resultPane and theirsPane. */
    private MergeConnectorPanel rightConnector;

    // toolbar stuff
    private JLabel  conflictCountLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton acceptOursBtn;
    private JButton acceptTheirsBtn;
    private JButton acceptAllOursBtn;
    private JButton acceptAllTheirsBtn;
    private JButton saveBtn;

    /**
     * Index of the conflict block the user is currently looking at.
     * This is a position among the *remaining* conflicts, not the original block index.
     */
    private int currentBlock = 0;

    /**
     * Guard flag used during scroll synchronisation.
     * Without it, each scroll event would trigger the listener on all three panes
     * and they'd fight each other in an infinite loop.
     */
    private boolean scrollSyncing = false;

    // =========================================================================
    // Constructor
    // =========================================================================

    public ThreeWayMergeWindow(String filePath) {
        this.filePath = filePath;

        String fileName = Paths.get(filePath).getFileName().toString();
        setTitle("Merge conflict: " + fileName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Read the conflicted file from disk. If this fails there is nothing we
        // can do — show an error and bail out early rather than leaving an empty
        // window open.
        String conflictedContent;
        try {
            byte[] raw = Files.readAllBytes(Paths.get(filePath));
            conflictedContent = new String(raw, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")   // normalise Windows line endings
                    .replace("\r",   "\n");  // normalise old Mac line endings
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot read conflicted file: " + filePath, e);
            JOptionPane.showMessageDialog(null,
                    "Cannot open file for merge resolution:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse the <<<<<<<  =======  >>>>>>> markers.
        // This also builds fallback ours/theirs texts from the parsed content.
        parseConflicts(conflictedContent);

        // Try to replace the fallback texts with the real git-object-store content.
        // Doing this after parsing means we always have something to show even if
        // the staged blobs cannot be retrieved.
        loadStagedContent();

        String syntaxStyle = SyntaxStyleUtil.getSyntaxStyle(fileName);
        buildUI(conflictedContent, syntaxStyle);

        // Scroll to the first conflict on open. We use invokeLater here because
        // modelToView doesn't work until the component has been laid out.
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

    // =========================================================================
    // Parsing conflict markers
    // =========================================================================

    /**
     * Walks through the conflicted file line by line and collects all conflict
     * blocks into {@link #blocks}.
     *
     * As a side-effect it also builds {@link #oursFullText} and
     * {@link #theirsFullText} as fallback representations: context lines (lines
     * outside any conflict marker) go into both, while the ours / theirs sections
     * of each block go into the respective text. This gives us something sane to
     * show in the side panes even if the staged blobs are not available.
     *
     * The tricky part is computing the line ranges in the *staged* files so that
     * the line highlights in the ours/theirs panes actually line up with the right
     * content. Context lines count the same in every stage; only the conflict
     * sections differ — so we maintain separate line counters for each side.
     */
    private void parseConflicts(String content) {
        String[] lines = content.split("\n", -1);

        StringBuilder oursText   = new StringBuilder();
        StringBuilder theirsText = new StringBuilder();

        // Small state machine: 0 = normal, 1 = inside ours section, 2 = inside theirs section
        int state = 0;
        String oursMarkerLine = null;
        List<String> curOursLines   = new ArrayList<>();
        List<String> curTheirsLines = new ArrayList<>();

        // These track how many lines we've written to each staged-file representation
        // so far. We need them to set the correct line highlight ranges later.
        int oursLineCounter   = 0;
        int theirsLineCounter = 0;

        for (String line : lines) {
            switch (state) {
                case 0 -> {
                    if (line.startsWith("<<<<<<<")) {
                        // Start of a conflict block. Save the marker and clear our buffers.
                        oursMarkerLine = line;
                        curOursLines.clear();
                        curTheirsLines.clear();
                        state = 1;
                    } else {
                        // Ordinary context line — appears identically in both staged files.
                        oursText.append(line).append('\n');
                        theirsText.append(line).append('\n');
                        oursLineCounter++;
                        theirsLineCounter++;
                    }
                }
                case 1 -> {
                    if (line.startsWith("=======")) {
                        state = 2; // switch to collecting theirs lines
                    } else {
                        curOursLines.add(line);
                    }
                }
                case 2 -> {
                    if (line.startsWith(">>>>>>>")) {
                        // End of block — assemble the MergeConflictBlock.
                        String theirsMarkerLine = line;
                        MergeConflictBlock block = new MergeConflictBlock(
                                blocks.size(),
                                oursMarkerLine,
                                theirsMarkerLine,
                                new ArrayList<>(curOursLines),
                                new ArrayList<>(curTheirsLines));

                        // Line ranges in the staged file representations.
                        // These are used by applyOursHighlights / applyTheirsHighlights
                        // to colour the right lines in the side panes.
                        block.oursStartLine   = oursLineCounter;
                        block.oursEndLine     = oursLineCounter + curOursLines.size();
                        block.theirsStartLine = theirsLineCounter;
                        block.theirsEndLine   = theirsLineCounter + curTheirsLines.size();

                        oursLineCounter   += curOursLines.size();
                        theirsLineCounter += curTheirsLines.size();

                        // Also append to the fallback texts
                        for (String l : curOursLines)   oursText.append(l).append('\n');
                        for (String l : curTheirsLines) theirsText.append(l).append('\n');

                        blocks.add(block);
                        state = 0;
                    } else {
                        curTheirsLines.add(line);
                    }
                }
            }
        }

        oursFullText   = oursText.toString();
        theirsFullText = theirsText.toString();

        // All blocks start off unresolved
        for (int i = 0; i < blocks.size(); i++) {
            unresolvedIndices.add(i);
        }
    }

    // =========================================================================
    // Load real staged content from git object store
    // =========================================================================

    /**
     * Replaces the fallback ours/theirs texts with the actual staged blobs from
     * git stages 2 (ours) and 3 (theirs).
     *
     * Using the real staged content is important because it gives the user the
     * full context of each side, not just the lines between the conflict markers.
     * For example, a function signature might be outside the marked region but
     * still changed on one side — you wouldn't see that with just the markers.
     *
     * This method is deliberately lenient: if we cannot load the blobs for any
     * reason (new repo, detached HEAD, weird git state) we just log a warning and
     * leave the fallback texts in place. The merge resolution still works, just
     * with slightly less context.
     */
    private void loadStagedContent() {
        try {
            var workTree = Context.getGitRepoService().getRepository().getWorkTree().toPath();

            // Git always uses forward slashes in paths, even on Windows
            String relativePath = workTree.relativize(Paths.get(filePath))
                    .toString()
                    .replace('\\', '/');

            String staged2 = Context.getGitRepoService().getStagedFileContent(relativePath, 2);
            if (staged2 != null && !staged2.isBlank()) {
                oursFullText = staged2.replace("\r\n", "\n").replace("\r", "\n");
            }

            String staged3 = Context.getGitRepoService().getStagedFileContent(relativePath, 3);
            if (staged3 != null && !staged3.isBlank()) {
                theirsFullText = staged3.replace("\r\n", "\n").replace("\r", "\n");
            }

        } catch (Exception e) {
            // Not fatal — the fallback texts from parseConflicts() are still usable.
            log.log(Level.WARNING,
                    "Could not load staged blobs for " + filePath
                    + " — falling back to text derived from conflict markers", e);
        }
    }

    // =========================================================================
    // UI construction
    // =========================================================================

    private void buildUI(String initialResultText, String syntaxStyle) {
        // Create the three editor panes. Only the result pane is editable.
        oursPane   = createSyntaxEditor(syntaxStyle, false);
        resultPane = createSyntaxEditor(syntaxStyle, true);
        theirsPane = createSyntaxEditor(syntaxStyle, false);

        oursPane.setText(oursFullText);
        theirsPane.setText(theirsFullText);

        // The result pane starts with the raw conflicted content including markers.
        // The user resolves conflicts either by clicking accept buttons or by
        // editing directly.
        resultPane.setText(initialResultText);
        resultPane.setCaretPosition(0);

        oursScroll   = new RTextScrollPane(oursPane);
        resultScroll = new RTextScrollPane(resultPane);
        theirsScroll = new RTextScrollPane(theirsPane);

        oursScroll.setLineNumbersEnabled(true);
        resultScroll.setLineNumbersEnabled(true);
        theirsScroll.setLineNumbersEnabled(true);

        // Apply initial highlights before the connectors are added, so the pane
        // colours are already correct when the window first appears.
        applyOursHighlights();
        applyTheirsHighlights();
        applyResultHighlights();

        // isLeft=true  means this connector sits between oursPane and resultPane
        // isLeft=false means it sits between resultPane and theirsPane
        leftConnector  = new MergeConnectorPanel(true);
        rightConnector = new MergeConnectorPanel(false);

        // Five-column layout: ours | left-connector | result | right-connector | theirs
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy   = 0;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.33;
        center.add(titledPanel("Ours",               oursScroll),   gbc);

        gbc.gridx = 1; gbc.weightx = 0.0;
        center.add(leftConnector,                                    gbc);

        gbc.gridx = 2; gbc.weightx = 0.34;
        center.add(titledPanel("Result (editable)",  resultScroll), gbc);

        gbc.gridx = 3; gbc.weightx = 0.0;
        center.add(rightConnector,                                   gbc);

        gbc.gridx = 4; gbc.weightx = 0.33;
        center.add(titledPanel("Theirs",             theirsScroll), gbc);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildToolbar(), BorderLayout.NORTH);
        getContentPane().add(center,         BorderLayout.CENTER);

        setupScrollSync();
        setupResultDocumentListener();
    }

    /**
     * Creates a read-only or editable RSyntaxTextArea with our standard settings.
     *
     * We disable code folding because the panes are supposed to show the complete
     * file — folding sections away would confuse the line-number mapping that the
     * connector panel relies on.
     */
    private static RSyntaxTextArea createSyntaxEditor(String syntaxStyle, boolean editable) {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        editor.setSyntaxEditingStyle(syntaxStyle);
        editor.setEditable(editable);
        editor.setCodeFoldingEnabled(false);
        editor.setAntiAliasingEnabled(true);
        editor.setLineWrap(false);
        editor.setHighlightCurrentLine(editable);  // cursor line highlight only in editable pane
        SyntaxStyleUtil.applyTheme(editor);
        editor.setFont(SyntaxStyleUtil.monoFont());
        return editor;
    }

    /**
     * Wraps a scroll pane in a panel with a bold title bar at the top.
     * Small but makes it much easier to see which pane is which.
     */
    private static JPanel titledPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(" " + title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0,
                        UIManager.getColor("Separator.foreground")),
                BorderFactory.createEmptyBorder(3, 4, 3, 4)));
        panel.add(label,   BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildToolbar() {
        prevBtn  = Util.createButton("Prev",   "Previous conflict", FontAwesomeSolid.ARROW_UP);
        nextBtn  = Util.createButton("Next",   "Next conflict",     FontAwesomeSolid.ARROW_DOWN);

        conflictCountLabel = new JLabel("—");
        conflictCountLabel.setFont(conflictCountLabel.getFont().deriveFont(Font.BOLD));

        // The accept-current buttons apply the chosen side to whichever conflict
        // currentBlock points at. The accept-all buttons loop through everything.
        acceptOursBtn      = Util.createButton("Ours",       "Accept our version for this conflict",  FontAwesomeSolid.ARROW_LEFT);
        acceptTheirsBtn    = Util.createButton("Theirs",     "Accept their version for this conflict", FontAwesomeSolid.ARROW_RIGHT);
        acceptAllOursBtn   = Util.createButton("All Ours",   "Accept our version for all remaining conflicts");
        acceptAllTheirsBtn = Util.createButton("All Theirs", "Accept their version for all remaining conflicts");

        saveBtn = Util.createButton("Resolved", "Write file to disk and stage it", FontAwesomeSolid.SAVE);

        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn.addActionListener(e -> navigate(+1));
        acceptOursBtn.addActionListener(e -> acceptCurrent(true));
        acceptTheirsBtn.addActionListener(e -> acceptCurrent(false));
        acceptAllOursBtn.addActionListener(e -> acceptAll(true));
        acceptAllTheirsBtn.addActionListener(e -> acceptAll(false));
        saveBtn.addActionListener(e -> saveAndMarkResolved());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        toolbar.add(prevBtn);
        toolbar.add(conflictCountLabel);
        toolbar.add(nextBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(acceptOursBtn);
        toolbar.add(acceptTheirsBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(acceptAllOursBtn);
        toolbar.add(acceptAllTheirsBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(saveBtn);
        return toolbar;
    }

    // =========================================================================
    // Scroll synchronisation
    // =========================================================================

    /**
     * Keeps all three panes at the same vertical scroll position.
     *
     * We add the same listener to all three scroll bars. The scrollSyncing flag
     * prevents the recursive loop that would otherwise happen when one pane's
     * scroll event triggers the others.
     *
     * The connector panels also repaint on every scroll so the trapezoids always
     * connect to the correct lines — they use modelToView() internally which
     * accounts for the current scroll offset.
     */
    private void setupScrollSync() {
        AdjustmentListener syncListener = e -> {
            if (scrollSyncing) return;
            scrollSyncing = true;
            int scrollValue = e.getValue();
            oursScroll.getVerticalScrollBar().setValue(scrollValue);
            resultScroll.getVerticalScrollBar().setValue(scrollValue);
            theirsScroll.getVerticalScrollBar().setValue(scrollValue);
            leftConnector.repaint();
            rightConnector.repaint();
            scrollSyncing = false;
        };
        oursScroll.getVerticalScrollBar().addAdjustmentListener(syncListener);
        resultScroll.getVerticalScrollBar().addAdjustmentListener(syncListener);
        theirsScroll.getVerticalScrollBar().addAdjustmentListener(syncListener);
    }

    // =========================================================================
    // Document listener on the result pane
    // =========================================================================

    /**
     * Watches the result pane for any edits and keeps the highlight colours and
     * connector drawings in sync.
     *
     * The "pending" flag de-duplicates rapid-fire events — for example, calling
     * setText() fires many individual document events. We only want one refresh
     * pass per logical edit, so we schedule it via invokeLater and skip
     * subsequent events until that pass runs.
     */
    private void setupResultDocumentListener() {
        resultPane.getDocument().addDocumentListener(new DocumentListener() {

            private boolean pending = false;

            private void scheduleRefresh() {
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

            @Override public void insertUpdate(DocumentEvent e)  { scheduleRefresh(); }
            @Override public void removeUpdate(DocumentEvent e)  { scheduleRefresh(); }
            @Override public void changedUpdate(DocumentEvent e) { /* style change only — ignore */ }
        });
    }

    /**
     * Shrinks {@link #unresolvedIndices} to match the number of conflict markers
     * actually present in the result pane.
     *
     * This is needed when the user removes markers manually by typing rather than
     * using the accept buttons. We trim from the end because we don't know which
     * block was removed without a full re-parse.
     *
     * TODO: a smarter approach would be to re-parse the result text and match
     *       remaining markers back to their original blocks. For now this is
     *       good enough for typical usage.
     */
    private void reconcileUnresolved() {
        int actualRemaining = countRemainingConflicts();
        while (unresolvedIndices.size() > actualRemaining) {
            unresolvedIndices.remove(unresolvedIndices.size() - 1);
        }
    }

    // =========================================================================
    // Line highlighting
    // =========================================================================

    /**
     * Highlights the ours-side lines in the left pane for all conflict blocks.
     * We use the green "added" colour because these are lines that exist on our side.
     *
     * Line ranges come from the staged-file line numbers stored in each block —
     * see parseConflicts() for how those are computed.
     */
    private void applyOursHighlights() {
        oursPane.removeAllLineHighlights();
        for (MergeConflictBlock block : blocks) {
            for (int line = block.oursStartLine; line < block.oursEndLine; line++) {
                try {
                    oursPane.addLineHighlight(line, SyntaxStyleUtil.addedBg());
                } catch (Exception ex) {
                    log.log(Level.FINE, "Failed to add ours highlight at line " + line, ex);
                }
            }
        }
    }

    /**
     * Highlights theirs-side lines in the right pane.
     * Red "deleted" colour because these are lines that don't exist on our side.
     */
    private void applyTheirsHighlights() {
        theirsPane.removeAllLineHighlights();
        for (MergeConflictBlock block : blocks) {
            for (int line = block.theirsStartLine; line < block.theirsEndLine; line++) {
                try {
                    theirsPane.addLineHighlight(line, SyntaxStyleUtil.deletedBg());
                } catch (Exception ex) {
                    log.log(Level.FINE, "Failed to add theirs highlight at line " + line, ex);
                }
            }
        }
    }

    /**
     * Highlights conflict markers and their content in the result pane.
     *
     * We use three colours:
     *  - changedBg for the marker lines themselves (<<<, ===, >>>)
     *  - addedBg   for the ours  section
     *  - deletedBg for the theirs section
     *
     * This method is package-private because it is also called from the connector
     * panel's doAccept pathway, which lives in the inner class.
     */
    void applyResultHighlights() {
        resultPane.removeAllLineHighlights();

        String[] lines = resultPane.getText().split("\n", -1);
        int state = 0;  // 0=normal, 1=ours section, 2=theirs section

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            try {
                if (state == 0 && line.startsWith("<<<<<<<")) {
                    resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());
                    state = 1;
                } else if (state == 1 && line.startsWith("=======")) {
                    resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());
                    state = 2;
                } else if (state == 1) {
                    resultPane.addLineHighlight(i, SyntaxStyleUtil.addedBg());
                } else if (state == 2 && line.startsWith(">>>>>>>")) {
                    resultPane.addLineHighlight(i, SyntaxStyleUtil.changedBg());
                    state = 0;
                } else if (state == 2) {
                    resultPane.addLineHighlight(i, SyntaxStyleUtil.deletedBg());
                }
            } catch (Exception ex) {
                log.log(Level.FINE, "Failed to add result highlight at line " + i, ex);
            }
        }
    }

    // =========================================================================
    // Navigation between conflicts
    // =========================================================================

    /**
     * Moves {@link #currentBlock} by {@code delta} and scrolls all three panes to
     * show the corresponding conflict.
     *
     * @param delta +1 to go forward, -1 to go back
     */
    private void navigate(int delta) {
        int remaining = countRemainingConflicts();
        if (remaining == 0) return;
        currentBlock = Math.max(0, Math.min(currentBlock + delta, remaining - 1));
        scrollToDiff(currentBlock);
        updateConflictLabel();
        updateButtonStates();
    }

    /**
     * Scrolls all three panes so that the nth remaining conflict is visible.
     *
     * For the result pane we find the nth {@code <<<<<<<} line by scanning.
     * For ours/theirs we look up the original block via unresolvedIndices and use
     * the pre-computed staged-file line numbers.
     *
     * The two panes might end up at slightly different positions if the conflict
     * regions have different lengths, but the sync listener will keep them at
     * the same raw scroll value — so we just position the result pane and let
     * the sync pull the others along.
     */
    private void scrollToDiff(int nthConflict) {
        // Scan result text for the nth <<<<<<< marker
        String[] resultLines = resultPane.getText().split("\n", -1);
        int found = 0;
        for (int i = 0; i < resultLines.length; i++) {
            if (resultLines[i].startsWith("<<<<<<<")) {
                if (found == nthConflict) {
                    scrollToLine(resultPane, resultScroll, i);

                    // Also position the side panes if we have the original block info
                    if (nthConflict < unresolvedIndices.size()) {
                        int origIdx = unresolvedIndices.get(nthConflict);
                        MergeConflictBlock block = blocks.get(origIdx);
                        scrollToLine(oursPane,   oursScroll,   block.oursStartLine);
                        scrollToLine(theirsPane, theirsScroll, block.theirsStartLine);
                    }
                    return;
                }
                found++;
            }
        }
    }

    /**
     * Scrolls a single pane+scrollpane pair so that {@code lineNumber} is visible,
     * with some margin above so there's context.
     *
     * RSyntaxTextArea.modelToView() can return null if the component hasn't been
     * laid out yet, so we guard against that.
     */
    private void scrollToLine(RSyntaxTextArea pane, RTextScrollPane scroll, int lineNumber) {
        try {
            if (lineNumber < 0 || lineNumber >= pane.getLineCount()) return;

            int charOffset = pane.getLineStartOffset(lineNumber);
            pane.setCaretPosition(charOffset);

            Rectangle charRect = pane.modelToView(charOffset);
            if (charRect == null) return;  // component not yet laid out

            JScrollBar vertBar = scroll.getVerticalScrollBar();
            // Show the target line roughly a quarter of the way down the viewport
            int margin   = Math.max(40, scroll.getViewport().getHeight() / 4);
            int targetY  = Math.max(0, charRect.y - margin);
            int maxValue = vertBar.getMaximum() - vertBar.getModel().getExtent();
            vertBar.setValue(Math.min(targetY, maxValue));

        } catch (Exception ex) {
            log.log(Level.FINE, "scrollToLine failed at line " + lineNumber, ex);
        }
    }

    // =========================================================================
    // Accept operations
    // =========================================================================

    /** Accepts one side of the conflict that currentBlock points at. */
    private void acceptCurrent(boolean useOurs) {
        if (countRemainingConflicts() == 0) return;
        doAccept(currentBlock, useOurs);
    }

    /**
     * Accepts the same side for every remaining conflict block.
     * We always accept block 0 in a loop because each accepted block removes
     * itself from the text, shifting everything else down by one position.
     */
    private void acceptAll(boolean useOurs) {
        int remaining = countRemainingConflicts();
        for (int i = 0; i < remaining; i++) {
            doAccept(0, useOurs);
        }
    }

    /**
     * The core accept operation. Replaces the nth remaining conflict block in
     * the result pane with either the ours or theirs section, then updates
     * all display state.
     *
     * Package-private so the MergeConnectorPanel inner class can call it when
     * the user clicks an arrow button.
     *
     * @param nthBlock zero-based index into the *remaining* conflicts (not the
     *                 original block list)
     * @param useOurs  true to keep our version, false to keep theirs
     */
    void doAccept(int nthBlock, boolean useOurs) {
        // Keep unresolvedIndices in sync before touching the text
        if (nthBlock < unresolvedIndices.size()) {
            unresolvedIndices.remove(nthBlock);
        }

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

    /**
     * Finds the nth {@code <<<<<<<...>>>>>>>} block in the result pane text and
     * replaces it with either the ours or theirs lines.
     *
     * We rebuild the full text from the split lines array because RSyntaxTextArea
     * doesn't give us a nice way to replace a range of lines atomically. One
     * setText() call is cleaner than multiple insert/remove pairs.
     */
    private void replaceConflictBlock(int nthBlock, boolean useOurs) {
        String[] lines = resultPane.getText().split("\n", -1);

        int blocksSeen = -1;
        int state = 0;
        int blockStartLine = -1;
        List<String> oursSection   = new ArrayList<>();
        List<String> theirsSection = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (state == 0 && line.startsWith("<<<<<<<")) {
                blocksSeen++;
                if (blocksSeen == nthBlock) {
                    blockStartLine = i;
                    oursSection.clear();
                    theirsSection.clear();
                    state = 1;
                }
            } else if (state == 1) {
                if (line.startsWith("=======")) {
                    state = 2;
                } else {
                    oursSection.add(line);
                }
            } else if (state == 2) {
                if (line.startsWith(">>>>>>>")) {
                    // Found the end of the block we want to replace.
                    // Rebuild the full text, substituting the chosen side.
                    final int blockEndLine = i;
                    List<String> chosen = useOurs ? oursSection : theirsSection;

                    StringBuilder rebuilt = new StringBuilder();
                    for (int j = 0; j < lines.length; j++) {
                        if (j == blockStartLine) {
                            // Insert the chosen lines in place of the whole block
                            for (String chosen_line : chosen) {
                                rebuilt.append(chosen_line).append('\n');
                            }
                        } else if (j > blockStartLine && j <= blockEndLine) {
                            // These are the original block lines — skip them
                        } else {
                            rebuilt.append(lines[j]);
                            if (j < lines.length - 1) {
                                rebuilt.append('\n');
                            }
                        }
                    }
                    resultPane.setText(rebuilt.toString());
                    return;
                } else {
                    theirsSection.add(line);
                }
            }
        }
        // If we reach here the block wasn't found — shouldn't happen in practice
        log.log(Level.WARNING, "replaceConflictBlock: block " + nthBlock + " not found in result text");
    }

    // =========================================================================
    // Save and stage
    // =========================================================================

    /**
     * Writes the current result pane content to disk and stages the file
     * (equivalent to {@code git add <file>}).
     *
     * We warn the user if there are still conflict markers in the text because
     * committing those would almost certainly break the project. But we don't
     * prevent it — sometimes people intentionally keep markers as placeholders
     * while they figure out what to do.
     */
    private void saveAndMarkResolved() {
        if (countRemainingConflicts() > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "There are still unresolved conflict markers in the file.\n"
                    + "Saving with markers will likely break compilation or tests.\n\n"
                    + "Save and mark as resolved anyway?",
                    "Unresolved conflicts",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        try {
            Files.write(Paths.get(filePath),
                    resultPane.getText().getBytes(StandardCharsets.UTF_8));

            // Stage the file via JGit — this is the "git add" step that tells git
            // the conflict is resolved
            var workTree = Context.getGitRepoService().getRepository().getWorkTree().toPath();
            String relativePath = workTree.relativize(Paths.get(filePath))
                    .toString()
                    .replace('\\', '/');
            Context.getGitRepoService().addFileToCommitStage(relativePath);

            // Refresh the working copy panel so it picks up the new RESOLVED status
            Context.refreshWorkingCopy();

            JOptionPane.showMessageDialog(this,
                    "File saved and staged as resolved.",
                    "Resolved", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to save resolved file: " + filePath, ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to save the file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    /**
     * Counts how many {@code <<<<<<<} markers remain in the result pane.
     * This is the ground truth for whether there are still unresolved conflicts.
     */
    private int countRemainingConflicts() {
        String text = resultPane.getText();
        int count = 0;
        int searchFrom = 0;
        while (true) {
            int pos = text.indexOf("<<<<<<<", searchFrom);
            if (pos < 0) break;
            count++;
            searchFrom = pos + 7;
        }
        return count;
    }

    /**
     * Scans the result pane text and returns the line-number triples for every
     * remaining conflict block: {@code [oursMarkerLine, separatorLine, theirsMarkerLine]}.
     *
     * Used by the connector panel to determine where to draw the trapezoids.
     */
    private List<int[]> getResultConflictLineRanges() {
        List<int[]> ranges = new ArrayList<>();
        String[] lines = resultPane.getText().split("\n", -1);
        int state = 0;
        int oursMarkerLine = -1;
        int separatorLine  = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (state == 0 && line.startsWith("<<<<<<<")) {
                oursMarkerLine = i;
                state = 1;
            } else if (state == 1 && line.startsWith("=======")) {
                separatorLine = i;
                state = 2;
            } else if (state == 2 && line.startsWith(">>>>>>>")) {
                ranges.add(new int[]{oursMarkerLine, separatorLine, i});
                state = 0;
            }
        }
        return ranges;
    }

    private void updateConflictLabel() {
        int remaining = countRemainingConflicts();
        if (remaining == 0) {
            conflictCountLabel.setText("All conflicts resolved ✓");
        } else {
            conflictCountLabel.setText("Conflict " + (currentBlock + 1) + " of " + remaining);
        }
    }

    private void updateButtonStates() {
        int remaining = countRemainingConflicts();
        boolean hasConflicts = remaining > 0;
        prevBtn.setEnabled(hasConflicts && currentBlock > 0);
        nextBtn.setEnabled(hasConflicts && currentBlock < remaining - 1);
        acceptOursBtn.setEnabled(hasConflicts);
        acceptTheirsBtn.setEnabled(hasConflicts);
        acceptAllOursBtn.setEnabled(hasConflicts);
        acceptAllTheirsBtn.setEnabled(hasConflicts);
    }

    // =========================================================================
    // Inner class: MergeConnectorPanel
    // =========================================================================

    /**
     * A narrow panel placed between two editor panes that draws bezier-curve
     * trapezoids connecting each conflict region on the left to the corresponding
     * region on the right.
     *
     * The trapezoids are colour-coded: green on the left connector (ours side),
     * red on the right (theirs side). A small arrow button sits at the midpoint
     * of each trapezoid; clicking it calls doAccept() on the outer class to
     * accept that side of the conflict.
     *
     * Coordinates are computed fresh on every paintComponent() call using
     * RSyntaxTextArea.modelToView() so they stay in sync with the scroll position.
     * This is intentionally simple — no caching, because the number of conflict
     * blocks is usually small (< 20) and the calculation is cheap.
     *
     * The isLeft flag controls which pair of panes this connector sits between:
     *   isLeft == true  →  ours pane ↔ result pane   (shows [→] buttons)
     *   isLeft == false →  result pane ↔ theirs pane  (shows [←] buttons)
     */
    private class MergeConnectorPanel extends JPanel {

        private static final int BUTTON_WIDTH  = 22;
        private static final int BUTTON_HEIGHT = 18;

        private final boolean isLeft;

        /** Hit-test bounds for the arrow buttons, rebuilt on every paint. */
        private final List<Rectangle> buttonBounds    = new ArrayList<>();
        private final List<Integer>   buttonBlockIndex = new ArrayList<>();

        MergeConnectorPanel(boolean isLeft) {
            this.isLeft = isLeft;
            setPreferredSize(new Dimension(CONNECTOR_WIDTH, 0));
            setMinimumSize(new Dimension(CONNECTOR_WIDTH, 0));
            setMaximumSize(new Dimension(CONNECTOR_WIDTH, Integer.MAX_VALUE));
            setOpaque(true);
            setToolTipText(isLeft
                    ? "Click → to accept our version of this conflict"
                    : "Click ← to accept their version of this conflict");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleButtonClick(e);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Clear the hit-test lists — they'll be rebuilt as we paint
            buttonBounds.clear();
            buttonBlockIndex.clear();

            List<int[]> conflictRanges = getResultConflictLineRanges();

            // We only draw connectors for blocks that are still unresolved
            int drawCount = Math.min(unresolvedIndices.size(), conflictRanges.size());
            if (drawCount == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth  = getWidth();
            int panelHeight = getHeight();

            for (int i = 0; i < drawCount; i++) {
                int origBlockIdx = unresolvedIndices.get(i);
                MergeConflictBlock block = blocks.get(origBlockIdx);
                int[] range = conflictRanges.get(i);

                // Get Y coordinates in the static pane (ours or theirs)
                int staticTop, staticBottom;
                if (isLeft) {
                    staticTop    = lineYInPanel(oursPane, oursScroll, block.oursStartLine);
                    staticBottom = lineYInPanel(oursPane, oursScroll, block.oursEndLine);
                } else {
                    staticTop    = lineYInPanel(theirsPane, theirsScroll, block.theirsStartLine);
                    staticBottom = lineYInPanel(theirsPane, theirsScroll, block.theirsEndLine);
                }

                // Get Y coordinates in the result pane.
                // Left connector covers <<<<<<< to =======  (the ours section).
                // Right connector covers ======= to >>>>>>>  (the theirs section).
                int resultTop, resultBottom;
                if (isLeft) {
                    resultTop    = lineYInPanel(resultPane, resultScroll, range[0]);       // <<< line
                    resultBottom = lineYInPanel(resultPane, resultScroll, range[1] + 1);   // === line + 1
                } else {
                    resultTop    = lineYInPanel(resultPane, resultScroll, range[1]);       // === line
                    resultBottom = lineYInPanel(resultPane, resultScroll, range[2] + 1);   // >>> line + 1
                }

                // Skip if both regions are entirely outside the visible area
                if (staticBottom < 0 && resultBottom < 0) continue;
                if (staticTop > panelHeight && resultTop > panelHeight) continue;

                // Avoid zero-height regions — give them at least 2px so there's something to draw
                if (staticTop == staticBottom) staticBottom = staticTop + 2;
                if (resultTop == resultBottom) resultBottom = resultTop + 2;

                // Arrange left/right edges depending on which side we're on.
                // Left connector: static pane is on the left, result on the right.
                // Right connector: result pane is on the left, static on the right.
                int leftTop, leftBottom, rightTop, rightBottom;
                if (isLeft) {
                    leftTop    = staticTop;    leftBottom = staticBottom;
                    rightTop   = resultTop;    rightBottom = resultBottom;
                } else {
                    leftTop    = resultTop;    leftBottom = resultBottom;
                    rightTop   = staticTop;    rightBottom = staticBottom;
                }

                boolean isCurrentBlock = (i == currentBlock && currentBlock < drawCount);
                Color fillColor = isCurrentBlock
                        ? SyntaxStyleUtil.changedBg()
                        : (isLeft ? SyntaxStyleUtil.addedBg() : SyntaxStyleUtil.deletedBg());

                drawBezierTrapezoid(g2, panelWidth, leftTop, leftBottom, rightTop, rightBottom,
                        new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 200));

                // Draw the accept button at the vertical midpoint of the visible connector region
                int midY = (Math.max(leftTop, rightTop) + Math.min(leftBottom, rightBottom)) / 2;
                int btnX = (panelWidth - BUTTON_WIDTH) / 2;
                int btnY = midY - BUTTON_HEIGHT / 2;
                Rectangle btnRect = new Rectangle(btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT);
                drawArrowButton(g2, btnRect,
                        isLeft ? FontAwesomeSolid.ARROW_RIGHT : FontAwesomeSolid.ARROW_LEFT);
                buttonBounds.add(btnRect);
                buttonBlockIndex.add(i);
            }

            g2.dispose();
        }

        /**
         * Draws the filled bezier trapezoid that connects two line ranges across
         * the connector panel.
         *
         * The shape uses cubic bezier curves for the top and bottom edges to give
         * a smooth S-curve appearance. The control points are placed at 35% and
         * 65% of the panel width, which looks good for most connector widths.
         *
         * @param g2     graphics context (caller owns it, we don't dispose it here)
         * @param w      panel width
         * @param lTop   top Y of the left edge
         * @param lBot   bottom Y of the left edge
         * @param rTop   top Y of the right edge
         * @param rBot   bottom Y of the right edge
         * @param color  fill colour including desired alpha
         */
        private void drawBezierTrapezoid(Graphics2D g2, int w,
                                         int lTop, int lBot,
                                         int rTop, int rBot,
                                         Color color) {
            float cp1x = w * 0.35f;  // first control point x
            float cp2x = w * 0.65f;  // second control point x

            GeneralPath shape = new GeneralPath();
            shape.moveTo(0, lTop);
            shape.curveTo(cp1x, lTop, cp2x, rTop, w, rTop);   // top bezier edge
            shape.lineTo(w, rBot);
            shape.curveTo(cp2x, rBot, cp1x, lBot, 0, lBot);   // bottom bezier edge
            shape.closePath();

            // Fill
            g2.setColor(color);
            g2.fill(shape);

            // Outline the curved edges with a slightly darker stroke
            Color outline = new Color(
                    Math.max(0, color.getRed()   - 30),
                    Math.max(0, color.getGreen() - 30),
                    Math.max(0, color.getBlue()  - 30),
                    color.getAlpha());
            g2.setColor(outline);
            g2.setStroke(new BasicStroke(0.8f));
            g2.draw(new java.awt.geom.CubicCurve2D.Float(0, lTop, cp1x, lTop, cp2x, rTop, w, rTop));
            g2.draw(new java.awt.geom.CubicCurve2D.Float(0, lBot, cp1x, lBot, cp2x, rBot, w, rBot));
        }

        /**
         * Draws a small rounded-rectangle button with an arrow icon inside.
         * The button look is deliberately minimal to match standard FlatLaf buttons.
         */
        private void drawArrowButton(Graphics2D g2, Rectangle bounds,
                                     org.kordamp.ikonli.Ikon iconType) {
            // Background
            Color bg = UIManager.getColor("Button.background");
            if (bg == null) bg = new Color(220, 220, 220);
            g2.setColor(bg);
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 6, 6);

            // Border
            Color fg = UIManager.getColor("Button.foreground");
            g2.setColor(fg != null ? fg : Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 6, 6);

            // Icon — centred inside the button
            Icon icon = Util.themeAwareIcon(iconType, 12);
            int iconX = bounds.x + (bounds.width  - icon.getIconWidth())  / 2;
            int iconY = bounds.y + (bounds.height - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, iconX, iconY);
        }

        /** Handles a click on the connector panel. Checks all button hit rectangles. */
        private void handleButtonClick(MouseEvent e) {
            for (int i = 0; i < buttonBounds.size(); i++) {
                if (buttonBounds.get(i).contains(e.getPoint())) {
                    doAccept(buttonBlockIndex.get(i), isLeft);
                    return;
                }
            }
        }

        /**
         * Returns the Y coordinate of {@code lineNum} in this panel's coordinate
         * space, accounting for the current scroll position.
         *
         * The conversion goes: line number → character offset → pixel rect inside
         * the text area → adjust for scroll offset → convert to panel coordinates.
         *
         * Returns 0 if anything goes wrong (e.g. line number out of range, or the
         * component hasn't been laid out yet).
         */
        private int lineYInPanel(RSyntaxTextArea textArea, RTextScrollPane scrollPane, int lineNum) {
            try {
                int clamped = Math.max(0, Math.min(lineNum, textArea.getLineCount() - 1));
                int charOffset = textArea.getLineStartOffset(clamped);
                Rectangle charRect = textArea.modelToView(charOffset);
                if (charRect == null) return 0;

                // Subtract the scroll offset and convert from viewport coordinates
                // to our panel's coordinate system
                int scrollOffset = scrollPane.getViewport().getViewPosition().y;
                Point viewportInPanel = SwingUtilities.convertPoint(
                        scrollPane.getViewport(), 0, 0, this);
                return charRect.y - scrollOffset + viewportInPanel.y;

            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
