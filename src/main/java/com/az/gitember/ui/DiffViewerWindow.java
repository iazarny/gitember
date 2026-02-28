package com.az.gitember.ui;

import com.az.gitember.component.RLeftTextScrollPane;
import com.az.gitember.component.RRightTextScrollPane;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.diff.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
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
    private RLeftTextScrollPane leftScroll;
    private RRightTextScrollPane rightScroll;
    private final JComboBox<RevisionItem> oldCombo;
    private final JComboBox<RevisionItem> newCombo;
    private JLabel diffInfoLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private final String fileName;
    private DiffConnectorPanel centerPanel;

    private DiffOverviewPanel overviewPanel;
    private SearchBar searchBar;

    private EditList editList;
    private int currentDiff = -1;
    private String oldText;
    private String newText;

    // Editable / arbitrary-compare mode
    private boolean editableMode = false;
    private boolean loadingFile  = false;
    private Timer   debounce;



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

    /**
     * Opens an empty, fully editable compare window (F7 / Compare Files).
     * <ul>
     *   <li>Both panes are editable – type or paste any text.</li>
     *   <li>Drop a file onto a pane or its path field to load it.</li>
     *   <li>Click Browse to pick a file from disk.</li>
     *   <li>Diff is recomputed automatically (400 ms debounce).</li>
     * </ul>
     */
    public DiffViewerWindow() {
        this.fileName = "";
        setTitle("Compare");
        initCommon(RSyntaxTextArea.SYNTAX_STYLE_NONE);
        oldCombo = null;
        newCombo = null;
        editableMode = true;

        oldPane.setEditable(true);
        newPane.setEditable(true);
        oldPane.setHighlightCurrentLine(true);
        newPane.setHighlightCurrentLine(true);

        debounce = new Timer(400, e -> computeAndDisplayDiff());
        debounce.setRepeats(false);

        javax.swing.event.DocumentListener autoRecompute = new javax.swing.event.DocumentListener() {
            private void trigger() { if (!loadingFile) debounce.restart(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { trigger(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { trigger(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
        };
        oldPane.getDocument().addDocumentListener(autoRecompute);
        newPane.getDocument().addDocumentListener(autoRecompute);

        JTextField leftPathField  = pathField();
        JTextField rightPathField = pathField();

        setupDropOnField(leftPathField,  oldPane);
        setupDropOnField(rightPathField, newPane);
        setupDropOnEditor(oldPane, leftPathField);
        setupDropOnEditor(newPane, rightPathField);

        JButton browseLeft  = new JButton("Browse…");
        JButton browseRight = new JButton("Browse…");
        browseLeft .addActionListener(e -> browseAndLoad(leftPathField,  oldPane));
        browseRight.addActionListener(e -> browseAndLoad(rightPathField, newPane));

        setupContentPane(
                buildNavPanel(),
                buildEditableHeader(leftPathField,  browseLeft,  leftScroll),
                buildEditableHeader(rightPathField, browseRight, rightScroll));
    }

    // ---- Private init helpers ----

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            toFront();
            requestFocus();
        }
    }

    /** Initializes all shared components. Must be called first in every constructor. */
    private void initCommon(String syntaxStyle) {
        setSize(1200, 700);
        setLocationRelativeTo(null);

        oldPane = createEditor(syntaxStyle);
        newPane = createEditor(syntaxStyle);

        leftScroll = new RLeftTextScrollPane(oldPane);
        leftScroll.setFoldIndicatorEnabled(false);
        rightScroll = new RRightTextScrollPane(newPane);
        rightScroll.setFoldIndicatorEnabled(false);
        centerPanel  = new DiffConnectorPanel();
        overviewPanel = new DiffOverviewPanel();
        overviewPanel.setOnJump(offset -> {
            JScrollBar bar = leftScroll.getVerticalScrollBar();
            int range = bar.getMaximum() - bar.getModel().getExtent();
            bar.setValue((int) (offset * range));
            // rightScroll syncs automatically via syncScroll()
        });
        leftScroll.getVerticalScrollBar().addAdjustmentListener(e -> updateOverviewViewport());
        leftScroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                updateOverviewViewport();
            }
        });
        syncScroll(leftScroll, rightScroll);

        prevBtn = new JButton("<< Prev");
        prevBtn.setEnabled(false);
        prevBtn.addActionListener(e -> navigateDiff(-1));

        nextBtn = new JButton("Next >>");
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> navigateDiff(1));

        diffInfoLabel = new JLabel("");

        searchBar = new SearchBar(oldPane, newPane);
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
        getContentPane().add(toolbar,                             BorderLayout.NORTH);
        getContentPane().add(buildDiffPanel(leftPanel, rightPanel), BorderLayout.CENTER);
        getContentPane().add(searchBar,                           BorderLayout.SOUTH);

        // Ctrl/Cmd+F → open search bar
        KeyStroke ctrlF = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().registerKeyboardAction(
                e -> searchBar.activate(), ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ---- Layout ----

    private JPanel buildDiffPanel(JPanel leftPanel, JPanel rightPanel) {
        centerPanel.setPreferredSize(new Dimension(40, 0));
        centerPanel.setMinimumSize(new Dimension(40, 0));

        // Repaint center panel on scroll so connectors track line positions
        leftScroll.getVerticalScrollBar() .addAdjustmentListener(e -> centerPanel.repaint());
        rightScroll.getVerticalScrollBar().addAdjustmentListener(e -> centerPanel.repaint());

        // Layout: left(43%) | connectors(9%) | right(43%) | overview(fixed 80px)
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill  = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.43;
        panel.add(leftPanel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.09;
        panel.add(centerPanel, gbc);
        gbc.gridx = 2; gbc.weightx = 0.43;
        panel.add(rightPanel, gbc);
        gbc.gridx = 3; gbc.weightx = 0.0;   // fixed preferred width
        panel.add(overviewWithHeader(overviewPanel), gbc);
        return panel;
    }

    private JPanel overviewWithHeader(DiffOverviewPanel overviewPanel) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.add(new JLabel("    "),  BorderLayout.NORTH);
        row.add(overviewPanel,  BorderLayout.CENTER);
        return row;
    }

    private void updateOverviewViewport() {
        JScrollBar bar = leftScroll.getVerticalScrollBar();
        int value  = bar.getValue();
        int extent = bar.getModel().getExtent();
        int max    = bar.getMaximum();
        int range  = max - extent;
        double offset = range > 0 ? (double) value / range : 0.0;
        double size   = max   > 0 ? (double) extent / max  : 1.0;
        overviewPanel.setViewport(offset, size);
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

    // ---- Editable-mode helpers -----------------------------------------------

    private static JTextField pathField() {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", "Drop a file here, or click Browse…");
        f.setEditable(false);   // path is set programmatically / via DnD / browse
        return f;
    }

    private static JPanel buildEditableHeader(JTextField pathField, JButton browseBtn,
                                              JComponent scroll) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        row.add(browseBtn,  BorderLayout.WEST);
        row.add(pathField,  BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(row,   BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** File-drop on a path field: sets field text and loads file into the paired editor. */
    private void setupDropOnField(JTextField field, RSyntaxTextArea targetEditor) {
        Border orig = field.getBorder();
        new DropTarget(field, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent d) {
                if (d.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    d.acceptDrag(DnDConstants.ACTION_COPY);
                    field.setBorder(BorderFactory.createLineBorder(new Color(0x4488FF), 2));
                } else { d.rejectDrag(); }
            }
            @Override public void dragExit(DropTargetEvent d) { field.setBorder(orig); }
            @Override @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent d) {
                field.setBorder(orig);
                d.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>)
                            d.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && files.get(0).isFile()) {
                        loadFileIntoEditor(files.get(0), field, targetEditor);
                        d.dropComplete(true); return;
                    }
                } catch (Exception ignored) {}
                d.dropComplete(false);
            }
        }, true);
    }

    /** File-drop directly on an editor pane (updates the paired path field too). */
    private void setupDropOnEditor(RSyntaxTextArea editor, JTextField pathField) {
        new DropTarget(editor, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent d) {
                if (d.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                    d.acceptDrag(DnDConstants.ACTION_COPY);
                else d.rejectDrag();
            }
            @Override @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent d) {
                d.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>)
                            d.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && files.get(0).isFile()) {
                        loadFileIntoEditor(files.get(0), pathField, editor);
                        d.dropComplete(true); return;
                    }
                } catch (Exception ignored) {}
                d.dropComplete(false);
            }
        }, true);
    }

    private void browseAndLoad(JTextField pathField, RSyntaxTextArea editor) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select file to compare");
        if (!pathField.getText().isBlank()) {
            File cur = new File(pathField.getText().trim());
            fc.setCurrentDirectory(cur.isDirectory() ? cur : cur.getParentFile());
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            loadFileIntoEditor(fc.getSelectedFile(), pathField, editor);
    }

    private void loadFileIntoEditor(File f, JTextField pathField, RSyntaxTextArea editor) {
        if (CompareFilesDialog.looksLikeBinary(f)) {
            JOptionPane.showMessageDialog(this, f.getName() + " appears to be binary.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return Files.readString(f.toPath());
            }
            @Override protected void done() {
                try {
                    String content = get();
                    pathField.setText(f.getAbsolutePath());
                    loadingFile = true;
                    editor.setSyntaxEditingStyle(SyntaxStyleUtil.getSyntaxStyle(f.getName()));
                    editor.setText(content);
                    editor.setCaretPosition(0);
                    loadingFile = false;
                    if (debounce != null) debounce.stop();
                    computeAndDisplayDiff();
                } catch (Exception ex) {
                    loadingFile = false;
                    log.log(Level.WARNING, "Failed to load file for comparison", ex);
                    JOptionPane.showMessageDialog(DiffViewerWindow.this,
                            "Cannot read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ---- End editable-mode helpers -------------------------------------------

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
        if (editableMode) {
            // In editable mode the panes ARE the source of truth
            oldText = oldPane.getText();
            newText = newPane.getText();
        }
        if (oldText == null || newText == null) return;

        // Compute diff using JGit HISTOGRAM algorithm
        RawText oldRaw = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        RawText newRaw = new RawText(newText.getBytes(StandardCharsets.UTF_8));
        DiffAlgorithm algorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        editList = algorithm.diff(RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

        // Feed the overview minimap
        overviewPanel.setData(editList, oldText.split("\n", -1), newText.split("\n", -1));
        SwingUtilities.invokeLater(this::updateOverviewViewport);

        if (!editableMode) {
            // Non-editable: push the loaded text into the panes
            oldPane.setText(oldText);
            oldPane.setCaretPosition(0);
            newPane.setText(newText);
            newPane.setCaretPosition(0);
        }

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
                    case DELETE -> SyntaxStyleUtil.deletedBg();
                    case REPLACE -> SyntaxStyleUtil.changedBg();
                    default -> null;
                };
            } else {
                beginLine = edit.getBeginB();
                endLine = edit.getEndB();
                color = switch (edit.getType()) {
                    case INSERT -> SyntaxStyleUtil.addedBg();
                    case REPLACE -> SyntaxStyleUtil.changedBg();
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
                case DELETE  -> SyntaxStyleUtil.deletedBg();
                case INSERT  -> SyntaxStyleUtil.addedBg();
                case REPLACE -> SyntaxStyleUtil.changedBg();
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
