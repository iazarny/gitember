package com.az.gitember.ui;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Folder comparison window with two side-by-side synchronised trees.
 *
 * <ul>
 *   <li>Left tree shows the left folder; right tree shows the right folder.</li>
 *   <li>Both trees share the same {@link DefaultTreeModel} and stay in sync
 *       (scroll + expand/collapse).</li>
 *   <li>Folder nodes show open/closed folder icons; file nodes show leaf icons.</li>
 *   <li>Left tree greys out RIGHT_ONLY files; right tree greys out LEFT_ONLY files.</li>
 *   <li>Double-click a file node to open {@link DiffViewerWindow}.</li>
 *   <li>"Differences only" filter and Prev/Next diff navigation.</li>
 * </ul>
 */
public class FolderCompareWindow extends JFrame {

    private static final Logger log = Logger.getLogger(FolderCompareWindow.class.getName());

    // ── Data ──────────────────────────────────────────────────────────────────

    enum EntryStatus { IDENTICAL, DIFFERENT, LEFT_ONLY, RIGHT_ONLY }

    record FolderEntry(String relativePath,
                       String leftAbsPath,
                       String rightAbsPath,
                       EntryStatus status) {}

    // ── UI ────────────────────────────────────────────────────────────────────

    private final JTextField leftPathField  = new JTextField();
    private final JTextField rightPathField = new JTextField();
    private final JButton    compareBtn     = new JButton("Compare");

    private final JLabel leftHeader  = new JLabel(" ");
    private final JLabel rightHeader = new JLabel(" ");

    private final JToggleButton diffsOnlyBtn = new JToggleButton("Differences only");
    private final JButton prevBtn  = new JButton("◄ Prev");
    private final JButton nextBtn  = new JButton("Next ►");
    private final JLabel  statsLbl = new JLabel(" ");

    private final JTree          leftTree;
    private final JTree          rightTree;
    private       DefaultTreeModel treeModel;

    // ── State ─────────────────────────────────────────────────────────────────

    private List<FolderEntry>              allEntries     = Collections.emptyList();
    private List<DefaultMutableTreeNode>   diffNodes      = Collections.emptyList();
    private int                            currentDiffIdx = -1;
    private boolean                        syncingExpansion  = false;
    private boolean                        syncingScroll     = false;
    private boolean                        syncingSelection  = false;

    // ── Construction ──────────────────────────────────────────────────────────

    public FolderCompareWindow() {
        super("Compare Folders");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        leftTree  = makeTree(new LeftRenderer());
        rightTree = makeTree(new RightRenderer());

        wireExpansionSync(leftTree,  rightTree);
        wireExpansionSync(rightTree, leftTree);
        wireSelectionSync(leftTree,  rightTree);
        wireSelectionSync(rightTree, leftTree);

        setupContextMenu(leftTree);
        setupContextMenu(rightTree);

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        diffsOnlyBtn.addActionListener(e -> applyFilter());
        prevBtn.addActionListener(e -> navigateDiff(-1));
        nextBtn.addActionListener(e -> navigateDiff(+1));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildTopBar(),   BorderLayout.NORTH);
        getContentPane().add(buildSplit(),    BorderLayout.CENTER);
        getContentPane().add(buildLegend(),  BorderLayout.SOUTH);

        CompareFilesDialog.attachFileDrop(leftPathField,  true);
        CompareFilesDialog.attachFileDrop(rightPathField, true);
        compareBtn.addActionListener(e -> startScan());
    }

    // ── Widget factories ──────────────────────────────────────────────────────

    private JTree makeTree(TreeCellRenderer renderer) {
        JTree t = new JTree(treeModel);
        t.setRootVisible(false);
        t.setShowsRootHandles(true);
        t.setRowHeight(22);
        t.setCellRenderer(renderer);
        t.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                TreePath path = t.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.isLeaf() && node.getUserObject() instanceof NodeData data && data.isFile())
                    openEntry(data.entry);
            }
        });
        return t;
    }

    private JPanel buildTopBar() {
        // path selector
        JPanel pathBar = new JPanel(new GridBagLayout());
        pathBar.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        JButton browseLeft  = new JButton("Browse…");
        JButton browseRight = new JButton("Browse…");
        browseLeft .addActionListener(e -> browse(leftPathField));
        browseRight.addActionListener(e -> browse(rightPathField));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 3, 2, 3); c.gridy = 0;
        c.gridx = 0; c.weightx = 0;    c.fill = GridBagConstraints.NONE;       pathBar.add(browseLeft, c);
        c.gridx = 1; c.weightx = 0.45; c.fill = GridBagConstraints.HORIZONTAL; pathBar.add(leftPathField, c);
        c.gridx = 2; c.weightx = 0;    c.fill = GridBagConstraints.NONE;       pathBar.add(compareBtn, c);
        c.gridx = 3; c.weightx = 0.45; c.fill = GridBagConstraints.HORIZONTAL; pathBar.add(rightPathField, c);
        c.gridx = 4; c.weightx = 0;    c.fill = GridBagConstraints.NONE;       pathBar.add(browseRight, c);

        // filter / navigation
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterBar.add(diffsOnlyBtn);
        filterBar.add(new JSeparator(SwingConstants.VERTICAL));
        filterBar.add(prevBtn);
        filterBar.add(nextBtn);
        filterBar.add(Box.createHorizontalStrut(12));
        filterBar.add(statsLbl);

        JPanel top = new JPanel(new BorderLayout());
        top.add(pathBar,   BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);
        return top;
    }

    private JSplitPane buildSplit() {
        styleHeader(leftHeader);
        styleHeader(rightHeader);

        JScrollPane leftScroll  = new JScrollPane(leftTree);
        JScrollPane rightScroll = new JScrollPane(rightTree);

        // Sync vertical scrolling
        leftScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!syncingScroll) { syncingScroll = true;
                rightScroll.getVerticalScrollBar().setValue(e.getValue()); syncingScroll = false; }
        });
        rightScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!syncingScroll) { syncingScroll = true;
                leftScroll.getVerticalScrollBar().setValue(e.getValue()); syncingScroll = false; }
        });

        JPanel leftPanel  = new JPanel(new BorderLayout());
        leftPanel.add(leftHeader,  BorderLayout.NORTH);
        leftPanel.add(leftScroll,  BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightHeader, BorderLayout.NORTH);
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.5);
        split.setDividerSize(5);
        return split;
    }

    private static void styleHeader(JLabel lbl) {
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0,
                        UIManager.getColor("Separator.foreground")),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        lbl.setOpaque(true);
    }

    private void wireExpansionSync(JTree source, JTree target) {
        source.addTreeExpansionListener(new TreeExpansionListener() {
            @Override public void treeExpanded(TreeExpansionEvent e) {
                if (!syncingExpansion) { syncingExpansion = true;
                    target.expandPath(e.getPath()); syncingExpansion = false; }
            }
            @Override public void treeCollapsed(TreeExpansionEvent e) {
                if (!syncingExpansion) { syncingExpansion = true;
                    target.collapsePath(e.getPath()); syncingExpansion = false; }
            }
        });
    }

    private void wireSelectionSync(JTree source, JTree target) {
        source.addTreeSelectionListener(e -> {
            if (!syncingSelection) {
                syncingSelection = true;
                target.setSelectionPath(e.getPath());
                if (e.getPath() != null) target.scrollPathToVisible(e.getPath());
                syncingSelection = false;
            }
        });
    }

    // ── Scanning ──────────────────────────────────────────────────────────────

    private void startScan() {
        String leftStr  = leftPathField.getText().trim();
        String rightStr = rightPathField.getText().trim();
        if (leftStr.isEmpty() || rightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both folders.",
                    "Compare Folders", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Path leftRoot  = Path.of(leftStr);
        Path rightRoot = Path.of(rightStr);
        if (!Files.isDirectory(leftRoot) || !Files.isDirectory(rightRoot)) {
            JOptionPane.showMessageDialog(this, "Both paths must be directories.",
                    "Compare Folders", JOptionPane.WARNING_MESSAGE);
            return;
        }

        compareBtn.setEnabled(false);
        statsLbl.setText("Scanning…");
        treeModel.setRoot(new DefaultMutableTreeNode("root"));
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        new SwingWorker<List<FolderEntry>, Void>() {
            @Override protected List<FolderEntry> doInBackground() throws Exception {
                return scan(leftRoot, rightRoot);
            }
            @Override protected void done() {
                compareBtn.setEnabled(true);
                try {
                    allEntries = get();
                    currentDiffIdx = -1;
                    leftHeader.setText(leftStr);
                    rightHeader.setText(rightStr);
                    applyFilter();
                    updateStats();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Folder scan failed", ex);
                    statsLbl.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private List<FolderEntry> scan(Path leftRoot, Path rightRoot) throws Exception {
        Set<String> leftRel  = collectRelativePaths(leftRoot);
        Set<String> rightRel = collectRelativePaths(rightRoot);
        Set<String> all = new TreeSet<>(leftRel);
        all.addAll(rightRel);

        List<FolderEntry> entries = new ArrayList<>(all.size());
        for (String rel : all) {
            boolean inLeft  = leftRel.contains(rel);
            boolean inRight = rightRel.contains(rel);
            String  leftAbs  = inLeft  ? leftRoot.resolve(rel).toString()  : null;
            String  rightAbs = inRight ? rightRoot.resolve(rel).toString() : null;
            EntryStatus status;
            if (inLeft && inRight) {
                status = Files.mismatch(Path.of(leftAbs), Path.of(rightAbs)) == -1L
                        ? EntryStatus.IDENTICAL : EntryStatus.DIFFERENT;
            } else {
                status = inLeft ? EntryStatus.LEFT_ONLY : EntryStatus.RIGHT_ONLY;
            }
            entries.add(new FolderEntry(rel, leftAbs, rightAbs, status));
        }
        return entries;
    }

    private Set<String> collectRelativePaths(Path root) throws Exception {
        if (!Files.exists(root)) return Collections.emptySet();
        Set<String> ignore = effectiveIgnoreExtensions();
        Set<String> paths  = new TreeSet<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!ignore.contains(extensionOf(file.getFileName().toString())))
                    paths.add(root.relativize(file).toString().replace('\\', '/'));
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }

    private static Set<String> effectiveIgnoreExtensions() {
        Settings s = Context.getSettings();
        return s != null ? s.getEffectiveIgnoreCompareFiles() : Settings.DEFAULT_IGNORE_COMPARE_FILES;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    // ── Tree model building ───────────────────────────────────────────────────

    private DefaultMutableTreeNode buildTreeModel(List<FolderEntry> entries) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        Map<String, DefaultMutableTreeNode> dirNodes = new LinkedHashMap<>();

        for (FolderEntry entry : entries) {
            String[] segments = entry.relativePath().split("/");
            DefaultMutableTreeNode parent = root;
            StringBuilder soFar = new StringBuilder();

            for (int i = 0; i < segments.length - 1; i++) {
                if (soFar.length() > 0) soFar.append('/');
                soFar.append(segments[i]);
                String key = soFar.toString();
                DefaultMutableTreeNode dir = dirNodes.get(key);
                if (dir == null) {
                    dir = new DefaultMutableTreeNode(new NodeData(segments[i], null));
                    dirNodes.put(key, dir);
                    parent.add(dir);
                }
                parent = dir;
            }
            parent.add(new DefaultMutableTreeNode(
                    new NodeData(segments[segments.length - 1], entry)));
        }

        propagateStatus(root);
        return root;
    }

    private EntryStatus propagateStatus(DefaultMutableTreeNode node) {
        NodeData data = (node.getUserObject() instanceof NodeData nd) ? nd : null;
        if (data != null && data.isFile()) return data.entry.status();

        EntryStatus worst = EntryStatus.IDENTICAL;
        for (int i = 0; i < node.getChildCount(); i++) {
            EntryStatus cs = propagateStatus((DefaultMutableTreeNode) node.getChildAt(i));
            if (statusPriority(cs) > statusPriority(worst)) worst = cs;
        }
        if (data != null) data.propagatedStatus = worst;
        return worst;
    }

    private static int statusPriority(EntryStatus s) {
        return switch (s) {
            case IDENTICAL  -> 0;
            case RIGHT_ONLY -> 1;
            case LEFT_ONLY  -> 2;
            case DIFFERENT  -> 3;
        };
    }

    // ── Filter & navigation ───────────────────────────────────────────────────

    private void applyFilter() {
        List<FolderEntry> visible = diffsOnlyBtn.isSelected()
                ? allEntries.stream().filter(e -> e.status() != EntryStatus.IDENTICAL).toList()
                : allEntries;

        DefaultMutableTreeNode newRoot = buildTreeModel(visible);
        treeModel.setRoot(newRoot);

        // Expand all in both trees
        for (int i = 0; i < leftTree.getRowCount();  i++) leftTree.expandRow(i);
        for (int i = 0; i < rightTree.getRowCount(); i++) rightTree.expandRow(i);

        // Collect diff leaf nodes for prev/next navigation
        diffNodes = new ArrayList<>();
        Enumeration<?> en = newRoot.preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            if (node.isLeaf() && node.getUserObject() instanceof NodeData data
                    && data.isFile() && data.entry.status() != EntryStatus.IDENTICAL)
                diffNodes.add(node);
        }
        currentDiffIdx = -1;
        prevBtn.setEnabled(!diffNodes.isEmpty());
        nextBtn.setEnabled(!diffNodes.isEmpty());
    }

    private void navigateDiff(int direction) {
        if (diffNodes.isEmpty()) return;
        currentDiffIdx = direction > 0
                ? (currentDiffIdx < diffNodes.size() - 1 ? currentDiffIdx + 1 : 0)
                : (currentDiffIdx > 0 ? currentDiffIdx - 1 : diffNodes.size() - 1);

        DefaultMutableTreeNode node = diffNodes.get(currentDiffIdx);
        TreePath path = new TreePath(node.getPath());
        leftTree.setSelectionPath(path);
        leftTree.scrollPathToVisible(path);
        rightTree.setSelectionPath(path);
        rightTree.scrollPathToVisible(path);
    }

    private void updateStats() {
        long total     = allEntries.size();
        long identical = allEntries.stream().filter(e -> e.status() == EntryStatus.IDENTICAL).count();
        long different = allEntries.stream().filter(e -> e.status() == EntryStatus.DIFFERENT).count();
        long leftOnly  = allEntries.stream().filter(e -> e.status() == EntryStatus.LEFT_ONLY).count();
        long rightOnly = allEntries.stream().filter(e -> e.status() == EntryStatus.RIGHT_ONLY).count();
        statsLbl.setText(total + " files  |  " + identical + " identical  |  "
                + different + " different  |  " + leftOnly + " left only  |  " + rightOnly + " right only");
    }

    // ── Open entries ──────────────────────────────────────────────────────────

    private FolderEntry entryFromTree(JTree tree) {
        TreePath p = tree.getSelectionPath();
        if (p == null) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
        return node.getUserObject() instanceof NodeData d && d.isFile() ? d.entry : null;
    }

    private void openEntry(FolderEntry entry) {
        if (entry == null) return;
        switch (entry.status()) {
            case DIFFERENT, IDENTICAL -> openDiff(entry);
            case LEFT_ONLY  -> openSingle(entry.leftAbsPath(),  entry.relativePath());
            case RIGHT_ONLY -> openSingle(entry.rightAbsPath(), entry.relativePath());
        }
    }

    private void openDiff(FolderEntry entry) {
        if (entry == null || entry.leftAbsPath() == null || entry.rightAbsPath() == null) return;
        File lf = new File(entry.leftAbsPath());
        File rf = new File(entry.rightAbsPath());
        if (CompareFilesDialog.looksLikeBinary(lf) || CompareFilesDialog.looksLikeBinary(rf)) {
            JOptionPane.showMessageDialog(this, entry.relativePath() + " appears to be binary.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() throws Exception {
                return new String[]{ Files.readString(lf.toPath()), Files.readString(rf.toPath()) };
            }
            @Override protected void done() {
                try {
                    String[] t = get();
                    new DiffViewerWindow(entry.relativePath(),
                            leftPathField.getText().trim(),  t[0],
                            rightPathField.getText().trim(), t[1]).setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open diff", ex);
                    JOptionPane.showMessageDialog(FolderCompareWindow.this,
                            "Cannot open diff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openSingle(String absPath, String relPath) {
        if (absPath == null) return;
        File f = new File(absPath);
        if (CompareFilesDialog.looksLikeBinary(f)) {
            JOptionPane.showMessageDialog(this, relPath + " is a binary file.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new SwingWorker<String, Void>() {
            @Override protected String  doInBackground() throws Exception { return Files.readString(f.toPath()); }
            @Override protected void done() {
                try { new FileViewerWindow(relPath, get(), relPath).setVisible(true); }
                catch (Exception ex) { log.log(Level.WARNING, "Failed to open file", ex); }
            }
        }.execute();
    }

    // ── Browse ────────────────────────────────────────────────────────────────

    private void browse(JTextField target) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select folder");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (!target.getText().isBlank()) {
            File cur = new File(target.getText().trim());
            if (cur.isDirectory()) fc.setCurrentDirectory(cur);
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            target.setText(fc.getSelectedFile().getAbsolutePath());
    }

    // ── Context menu ──────────────────────────────────────────────────────────

    private void setupContextMenu(JTree tree) {
        JPopupMenu menu      = new JPopupMenu();
        JMenuItem diffItem   = new JMenuItem("Open diff");
        JMenuItem leftItem   = new JMenuItem("Open left file");
        JMenuItem rightItem  = new JMenuItem("Open right file");

        diffItem .addActionListener(e -> openDiff(entryFromTree(tree)));
        leftItem .addActionListener(e -> { FolderEntry en = entryFromTree(tree); if (en != null) openSingle(en.leftAbsPath(),  en.relativePath()); });
        rightItem.addActionListener(e -> { FolderEntry en = entryFromTree(tree); if (en != null) openSingle(en.rightAbsPath(), en.relativePath()); });

        menu.add(diffItem); menu.addSeparator(); menu.add(leftItem); menu.add(rightItem);

        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                FolderEntry en = entryFromTree(tree);
                diffItem .setEnabled(en != null && en.leftAbsPath()  != null && en.rightAbsPath() != null);
                leftItem .setEnabled(en != null && en.leftAbsPath()  != null);
                rightItem.setEnabled(en != null && en.rightAbsPath() != null);
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        tree.addMouseListener(new MouseAdapter() {
            private void show(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                TreePath p = tree.getPathForLocation(e.getX(), e.getY());
                if (p != null) tree.setSelectionPath(p);
                menu.show(tree, e.getX(), e.getY());
            }
            @Override public void mousePressed(MouseEvent e)  { show(e); }
            @Override public void mouseReleased(MouseEvent e) { show(e); }
        });
    }

    // ── Legend ────────────────────────────────────────────────────────────────

    private static JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        p.add(chip(SyntaxStyleUtil.rowBgChanged(), "Different  ≠"));
        p.add(chip(SyntaxStyleUtil.rowBgDeleted(), "Left only  ◄"));
        p.add(chip(SyntaxStyleUtil.rowBgAdded(),   "Right only  ►"));
        p.add(chip(null,                           "Identical"));
        return p;
    }

    private static JLabel chip(Color bg, String text) {
        JLabel l = new JLabel(" " + text + " ");
        l.setOpaque(bg != null);
        if (bg != null) l.setBackground(bg);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 11f));
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return l;
    }

    // ── NodeData ──────────────────────────────────────────────────────────────

    static class NodeData {
        final String      name;
        final FolderEntry entry;            // null → directory
        EntryStatus       propagatedStatus; // computed by propagateStatus()

        NodeData(String name, FolderEntry entry) {
            this.name = name;
            this.entry = entry;
            this.propagatedStatus = entry != null ? entry.status() : EntryStatus.IDENTICAL;
        }

        boolean isFile() { return entry != null; }
        EntryStatus effectiveStatus() { return isFile() ? entry.status() : propagatedStatus; }
    }

    // ── Cell renderers ────────────────────────────────────────────────────────

    /**
     * Base renderer. Inherits folder-open / folder-closed / leaf icons from
     * {@link DefaultTreeCellRenderer} automatically via the {@code expanded} and
     * {@code leaf} parameters — no explicit icon override needed.
     */
    private static abstract class BaseRenderer extends DefaultTreeCellRenderer {

        private transient JTree  currentTree;
        private           boolean currentSel;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // super sets open/closed folder icon (non-leaf) or leaf icon based on expanded/leaf
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            currentTree = tree;
            currentSel  = sel;
            // Keep opaque=false so JLabel never fills just the label bounds with background;
            // full-row background is painted manually in paint() below.
            setOpaque(false);

            if (!(value instanceof DefaultMutableTreeNode node)) return this;
            if (!(node.getUserObject() instanceof NodeData data)) return this;

            EntryStatus status = data.effectiveStatus();
            setText(label(data, status));

            if (!sel) {
                Color bg = bg(status);
                setBackground(bg != null ? bg : tree.getBackground());
                setForeground(fg(status));
            }
            return this;
        }

        @Override
        public void paint(Graphics g) {
            // Paint background spanning the full tree row width (not just label bounds).
            // Skip for selected rows — the L&F (FlatLaf) already paints a full-width highlight.
            if (!currentSel && currentTree != null) {
                g.setColor(getBackground());
                g.fillRect(0, 0, currentTree.getWidth(), getHeight());
            }
            super.paint(g);
        }

        /** Text to display for this node. */
        protected abstract String label(NodeData data, EntryStatus status);
        /** Row background colour, or {@code null} for default. */
        protected abstract Color  bg(EntryStatus status);
        /** Row foreground colour. */
        protected abstract Color  fg(EntryStatus status);
    }

    /** Left-side tree: highlights LEFT_ONLY (red), greys out RIGHT_ONLY. */
    private static class LeftRenderer extends BaseRenderer {
        @Override protected String label(NodeData data, EntryStatus status) {
            String n = data.isFile() ? data.name : data.name + "/";
            return switch (status) {
                case DIFFERENT  -> n + "  ≠";
                case LEFT_ONLY  -> n + "  ◄";
                default         -> n;
            };
        }
        @Override protected Color bg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowBgChanged();
                case LEFT_ONLY  -> SyntaxStyleUtil.rowBgDeleted();
                default         -> null;
            };
        }
        @Override protected Color fg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowFgChanged();
                case LEFT_ONLY  -> SyntaxStyleUtil.rowFgDeleted();
                case RIGHT_ONLY -> UIManager.getColor("Label.disabledForeground");
                default         -> UIManager.getColor("Tree.foreground");
            };
        }
    }

    /** Right-side tree: highlights RIGHT_ONLY (green), greys out LEFT_ONLY. */
    private static class RightRenderer extends BaseRenderer {
        @Override protected String label(NodeData data, EntryStatus status) {
            String n = data.isFile() ? data.name : data.name + "/";
            return switch (status) {
                case DIFFERENT  -> n + "  ≠";
                case RIGHT_ONLY -> n + "  ►";
                default         -> n;
            };
        }
        @Override protected Color bg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowBgChanged();
                case RIGHT_ONLY -> SyntaxStyleUtil.rowBgAdded();
                default         -> null;
            };
        }
        @Override protected Color fg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowFgChanged();
                case RIGHT_ONLY -> SyntaxStyleUtil.rowFgAdded();
                case LEFT_ONLY  -> UIManager.getColor("Label.disabledForeground");
                default         -> UIManager.getColor("Tree.foreground");
            };
        }
    }
}
