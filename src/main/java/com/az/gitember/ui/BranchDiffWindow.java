package com.az.gitember.ui;

import com.az.gitember.service.Context;
import com.az.gitember.service.LlmDiffDescriptionService;
import com.az.gitember.service.OllamaManager;
import com.az.gitember.ui.misc.Util;
import org.eclipse.jgit.diff.DiffEntry;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Branch diff window with two side-by-side synchronised trees, mirroring
 * {@link FolderCompareWindow} but backed by git branch data.
 *
 * <ul>
 *   <li>Added files (right branch only) → greyed out on left, highlighted on right.</li>
 *   <li>Deleted files (left branch only) → highlighted on left, greyed out on right.</li>
 *   <li>Modified / Renamed → highlighted in both panels.</li>
 *   <li>Double-click or context menu to open diff / single-file viewer.</li>
 * </ul>
 */
public class BranchDiffWindow extends JFrame {

    private static final Logger log = Logger.getLogger(BranchDiffWindow.class.getName());

    // ── Entry status (mirrors FolderCompareWindow.EntryStatus) ────────────────

    enum EntryStatus {
        DIFFERENT  ("≠"),
        LEFT_ONLY  ("<<"),   // Deleted: exists only in left/A branch
        RIGHT_ONLY (">>");   // Added:   exists only in right/B branch

        public final String label;
        EntryStatus(String label) { this.label = label; }
    }

    record BranchEntry(String path, EntryStatus status, DiffEntry diffEntry) {}

    // ── State ─────────────────────────────────────────────────────────────────

    private final String branchARef;
    private final String branchBRef;
    private final String branchALabel;
    private final String branchBLabel;

    private List<BranchEntry>            allEntries     = Collections.emptyList();
    private List<DefaultMutableTreeNode> diffNodes      = Collections.emptyList();
    private int                          currentDiffIdx = -1;
    private boolean syncingExpansion = false;
    private boolean syncingScroll    = false;
    private boolean syncingSelection = false;

    // ── UI ────────────────────────────────────────────────────────────────────

    private final JLabel leftHeader  = new JLabel(" ");
    private final JLabel rightHeader = new JLabel(" ");
    private final JLabel statsLbl    = new JLabel(" ");
    private final JButton prevBtn    = Util.createButton("Prev", "Previous difference", FontAwesomeSolid.ARROW_UP);
    private final JButton nextBtn    = Util.createButton("Next", "Next difference",  FontAwesomeSolid.ARROW_DOWN);
    private final JButton aiDescBtn  = Util.createButton("Describe", "Describe difference with AI ...");

    // AI description panel (shown / hidden on demand)
    private JPanel     aiPanel;
    private JTextArea  aiTextArea;
    private JSplitPane mainSplit;

    private final JTree          leftTree;
    private final JTree          rightTree;
    private       DefaultTreeModel treeModel;

    // ── Construction ──────────────────────────────────────────────────────────

    public BranchDiffWindow(Component parent, String branchARef, String branchBRef) {
        this.branchARef   = branchARef;
        this.branchBRef   = branchBRef;
        this.branchALabel = shortName(branchARef);
        this.branchBLabel = shortName(branchBRef);

        setTitle("Branch Diff: " + branchALabel + " ↔ " + branchBLabel);
        setSize(1100, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
        prevBtn.addActionListener(e -> navigateDiff(-1));
        nextBtn.addActionListener(e -> navigateDiff(+1));

        boolean aiEnabled = Boolean.TRUE.equals(
                Context.getSettings() != null && Context.getSettings().getEnableBranchCompareDescription());
        aiDescBtn.setVisible(aiEnabled);
        aiDescBtn.setToolTipText("Ask the local Ollama LLM to describe the changes between these revisions");
        aiDescBtn.addActionListener(e -> requestAiDescription());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildNavBar(),  BorderLayout.NORTH);
        getContentPane().add(buildCenter(),  BorderLayout.CENTER);
        getContentPane().add(buildLegend(), BorderLayout.SOUTH);

        loadDiff();

        Util.bindEscapeToDispose(this);
    }

    // ── Widget factories ──────────────────────────────────────────────────────

    private static final class FullRowTreeUI extends BasicTreeUI {
        private final BaseRenderer rowRenderer;
        FullRowTreeUI(BaseRenderer r) { this.rowRenderer = r; }

        @Override
        protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets,
                Rectangle bounds, TreePath path, int row,
                boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
            Color c = tree.isRowSelected(row)
                    ? UIManager.getColor("Tree.selectionBackground")
                    : rowBg(path);
            if (c != null) {
                g.setColor(c);
                g.fillRect(0, bounds.y, tree.getWidth(), bounds.height);
            }
            super.paintRow(g, clipBounds, insets, bounds, path, row,
                    isExpanded, hasBeenExpanded, isLeaf);
        }

        private Color rowBg(TreePath path) {
            Object last = path.getLastPathComponent();
            if (!(last instanceof DefaultMutableTreeNode node)) return null;
            if (!(node.getUserObject() instanceof NodeData data)) return null;
            Color c = rowRenderer.bg(data.effectiveStatus());
            return c != null ? c : tree.getBackground();
        }
    }

    private JTree makeTree(BaseRenderer renderer) {
        JTree t = new JTree(treeModel);
        t.setUI(new FullRowTreeUI(renderer));
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

    private JPanel buildNavBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        bar.add(prevBtn);
        bar.add(nextBtn);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(statsLbl);
        if (aiDescBtn.isVisible()) {
            bar.add(Box.createHorizontalStrut(12));
            bar.add(aiDescBtn);
        }
        return bar;
    }

    /** Builds the central area: tree split pane + optional AI description panel below. */
    private JComponent buildCenter() {
        aiTextArea = new JTextArea();
        aiTextArea.setEditable(false);
        aiTextArea.setLineWrap(true);
        aiTextArea.setWrapStyleWord(true);
        aiTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        aiTextArea.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane aiScroll = new JScrollPane(aiTextArea);
        aiScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIManager.getColor("Separator.foreground")));

        JLabel aiHeader = new JLabel(" AI Change Description");
        aiHeader.setFont(aiHeader.getFont().deriveFont(Font.BOLD, 12f));
        aiHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0,
                        UIManager.getColor("Separator.foreground")),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        aiHeader.setOpaque(true);

        aiPanel = new JPanel(new BorderLayout());
        aiPanel.add(aiHeader, BorderLayout.NORTH);
        aiPanel.add(aiScroll, BorderLayout.CENTER);
        aiPanel.setVisible(false);

        mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildSplit(), aiPanel);
        mainSplit.setResizeWeight(0.65);
        mainSplit.setDividerSize(5);
        mainSplit.setDividerLocation(Integer.MAX_VALUE);  // hide until shown
        return mainSplit;
    }

    private JSplitPane buildSplit() {
        styleHeader(leftHeader);
        styleHeader(rightHeader);

        JScrollPane leftScroll  = new JScrollPane(leftTree);
        JScrollPane rightScroll = new JScrollPane(rightTree);

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

    // ── Sync ──────────────────────────────────────────────────────────────────

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

    // ── Loading ───────────────────────────────────────────────────────────────

    private void loadDiff() {
        statsLbl.setText("Computing diff…");
        new SwingWorker<List<DiffEntry>, Void>() {
            @Override protected List<DiffEntry> doInBackground() throws Exception {
                return Context.getGitRepoService().getBranchDiff(branchARef, branchBRef);
            }
            @Override protected void done() {
                try {
                    List<DiffEntry> raw = get();
                    allEntries = toEntries(raw);
                    leftHeader.setText(branchALabel);
                    rightHeader.setText(branchBLabel);
                    rebuildTree();
                    updateStats();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Branch diff failed", ex);
                    statsLbl.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(BranchDiffWindow.this,
                            "Cannot compute branch diff:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static List<BranchEntry> toEntries(List<DiffEntry> raw) {
        List<BranchEntry> result = new ArrayList<>(raw.size());
        for (DiffEntry d : raw) {
            EntryStatus status = switch (d.getChangeType()) {
                case ADD    -> EntryStatus.RIGHT_ONLY;
                case DELETE -> EntryStatus.LEFT_ONLY;
                default     -> EntryStatus.DIFFERENT;  // MODIFY, RENAME, COPY
            };
            String path = d.getChangeType() == DiffEntry.ChangeType.DELETE
                    ? d.getOldPath() : d.getNewPath();
            result.add(new BranchEntry(path, status, d));
        }
        return result;
    }

    // ── Tree model ────────────────────────────────────────────────────────────

    private void rebuildTree() {
        DefaultMutableTreeNode root = buildTreeModel(allEntries);
        treeModel.setRoot(root);

        for (int i = 0; i < leftTree.getRowCount();  i++) leftTree.expandRow(i);
        for (int i = 0; i < rightTree.getRowCount(); i++) rightTree.expandRow(i);

        diffNodes = new ArrayList<>();
        Enumeration<?> en = root.preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            if (node.isLeaf() && node.getUserObject() instanceof NodeData data && data.isFile())
                diffNodes.add(node);
        }
        currentDiffIdx = -1;
        prevBtn.setEnabled(!diffNodes.isEmpty());
        nextBtn.setEnabled(!diffNodes.isEmpty());
    }

    private DefaultMutableTreeNode buildTreeModel(List<BranchEntry> entries) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        Map<String, DefaultMutableTreeNode> dirNodes = new LinkedHashMap<>();

        for (BranchEntry entry : entries) {
            String[] segments = entry.path().split("/");
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

        EntryStatus worst = EntryStatus.DIFFERENT;  // lowest priority for dirs
        boolean first = true;
        for (int i = 0; i < node.getChildCount(); i++) {
            EntryStatus cs = propagateStatus((DefaultMutableTreeNode) node.getChildAt(i));
            if (first || statusPriority(cs) > statusPriority(worst)) { worst = cs; first = false; }
        }
        if (data != null) data.propagatedStatus = first ? EntryStatus.DIFFERENT : worst;
        return first ? EntryStatus.DIFFERENT : worst;
    }

    private static int statusPriority(EntryStatus s) {
        return switch (s) {
            case LEFT_ONLY  -> 0;
            case RIGHT_ONLY -> 1;
            case DIFFERENT  -> 2;
        };
    }

    // ── Navigation ────────────────────────────────────────────────────────────

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
        long different = allEntries.stream().filter(e -> e.status() == EntryStatus.DIFFERENT).count();
        long leftOnly  = allEntries.stream().filter(e -> e.status() == EntryStatus.LEFT_ONLY).count();
        long rightOnly = allEntries.stream().filter(e -> e.status() == EntryStatus.RIGHT_ONLY).count();
        statsLbl.setText(total + " files  |  "
                + different + " modified  |  "
                + leftOnly  + " deleted  |  "
                + rightOnly + " added");
    }

    // ── AI description ────────────────────────────────────────────────────────

    private void requestAiDescription() {
        if (!OllamaManager.isRunning()) {
            JOptionPane.showMessageDialog(this,
                    "Ollama is not running.\n\n" +
                    "Start Ollama first, or enable it in Settings → AI Features.",
                    "Ollama Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        aiDescBtn.setEnabled(false);
        aiTextArea.setText("Generating description…  (this may take a minute)");
        aiPanel.setVisible(true);
        mainSplit.setDividerLocation(0.65);

        String ollamaUrl  = OllamaManager.BASE_URL;
        String modelName  = Context.getSettings() != null
                ? Context.getSettings().getLlmDetectorModel() : "llama3.2";

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String diffText = Context.getGitRepoService()
                        .getBranchDiffText(branchARef, branchBRef, LlmDiffDescriptionService.MAX_DIFF_CHARS);
                return LlmDiffDescriptionService.describe(
                        diffText, branchALabel, branchBLabel, ollamaUrl, modelName);
            }
            @Override
            protected void done() {
                aiDescBtn.setEnabled(true);
                try {
                    aiTextArea.setText(get());
                    aiTextArea.setCaretPosition(0);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "AI description failed", ex);
                    aiTextArea.setText("Failed to generate description:\n" + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Open actions ──────────────────────────────────────────────────────────

    private BranchEntry entryFromTree(JTree tree) {
        TreePath p = tree.getSelectionPath();
        if (p == null) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
        return node.getUserObject() instanceof NodeData d && d.isFile() ? d.entry : null;
    }

    private void openEntry(BranchEntry entry) {
        if (entry == null) return;
        switch (entry.status()) {
            case DIFFERENT -> openDiff(entry);
            case LEFT_ONLY -> openSingleFile(entry, true);
            case RIGHT_ONLY -> openSingleFile(entry, false);
        }
    }

    private void openDiff(BranchEntry entry) {
        if (entry == null) return;
        DiffEntry d = entry.diffEntry();
        String leftPath  = DiffEntry.DEV_NULL.equals(d.getOldPath()) ? d.getNewPath() : d.getOldPath();
        String rightPath = DiffEntry.DEV_NULL.equals(d.getNewPath()) ? d.getOldPath() : d.getNewPath();

        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() throws Exception {
                return new String[]{
                    Context.getGitRepoService().getFileContentAtRef(branchARef, leftPath),
                    Context.getGitRepoService().getFileContentAtRef(branchBRef, rightPath)
                };
            }
            @Override protected void done() {
                try {
                    String[] contents = get();
                    new DiffViewerWindow(entry.path(),
                            branchALabel, contents[0],
                            branchBLabel, contents[1]).setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open diff", ex);
                    JOptionPane.showMessageDialog(BranchDiffWindow.this,
                            "Cannot open diff:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openSingleFile(BranchEntry entry, boolean useLeft) {
        if (entry == null) return;
        DiffEntry d      = entry.diffEntry();
        String ref       = useLeft ? branchARef   : branchBRef;
        String branchLbl = useLeft ? branchALabel  : branchBLabel;
        String path      = useLeft
                ? (DiffEntry.DEV_NULL.equals(d.getOldPath()) ? d.getNewPath() : d.getOldPath())
                : (DiffEntry.DEV_NULL.equals(d.getNewPath()) ? d.getOldPath() : d.getNewPath());

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return Context.getGitRepoService().getFileContentAtRef(ref, path);
            }
            @Override protected void done() {
                try {
                    new FileViewerWindow(path + "  [" + branchLbl + "]", get(), path).setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open file", ex);
                    JOptionPane.showMessageDialog(BranchDiffWindow.this,
                            "Cannot open file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Context menu ──────────────────────────────────────────────────────────

    private void setupContextMenu(JTree tree) {
        JPopupMenu menu     = new JPopupMenu();
        JMenuItem diffItem  = new JMenuItem("Open diff");
        JMenuItem leftItem  = new JMenuItem("Open left file  (" + branchALabel + ")");
        JMenuItem rightItem = new JMenuItem("Open right file  (" + branchBLabel + ")");

        diffItem .addActionListener(e -> { BranchEntry en = entryFromTree(tree); if (en != null) openDiff(en); });
        leftItem .addActionListener(e -> { BranchEntry en = entryFromTree(tree); if (en != null) openSingleFile(en, true); });
        rightItem.addActionListener(e -> { BranchEntry en = entryFromTree(tree); if (en != null) openSingleFile(en, false); });

        menu.add(diffItem); menu.addSeparator(); menu.add(leftItem); menu.add(rightItem);

        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                BranchEntry en = entryFromTree(tree);
                diffItem .setEnabled(en != null && en.status() == EntryStatus.DIFFERENT);
                leftItem .setEnabled(en != null && en.status() != EntryStatus.RIGHT_ONLY);
                rightItem.setEnabled(en != null && en.status() != EntryStatus.LEFT_ONLY);
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
        p.add(chip(SyntaxStyleUtil.changedBg(),  "Modified  " + EntryStatus.DIFFERENT.label));
        p.add(chip(SyntaxStyleUtil.deletedBg(),  "Deleted (left only)  " + EntryStatus.LEFT_ONLY.label));
        p.add(chip(SyntaxStyleUtil.addedBg(),    "Added (right only)  " + EntryStatus.RIGHT_ONLY.label));
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String shortName(String ref) {
        if (ref == null) return "";
        if (ref.startsWith("refs/heads/"))  return ref.substring("refs/heads/".length());
        if (ref.startsWith("refs/remotes/")) return ref.substring("refs/remotes/".length());
        if (ref.startsWith("refs/tags/"))   return ref.substring("refs/tags/".length());
        return ref;
    }

    // ── NodeData ──────────────────────────────────────────────────────────────

    static class NodeData {
        final String      name;
        final BranchEntry entry;             // null → directory node
        EntryStatus       propagatedStatus;

        NodeData(String name, BranchEntry entry) {
            this.name = name;
            this.entry = entry;
            this.propagatedStatus = entry != null ? entry.status() : EntryStatus.DIFFERENT;
        }

        boolean isFile() { return entry != null; }
        EntryStatus effectiveStatus() { return isFile() ? entry.status() : propagatedStatus; }
    }

    // ── Cell renderers ────────────────────────────────────────────────────────

    private static abstract class BaseRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setOpaque(false);
            if (!(value instanceof DefaultMutableTreeNode node)) return this;
            if (!(node.getUserObject() instanceof NodeData data)) return this;

            EntryStatus status = data.effectiveStatus();
            setText(label(data, status));

            if (!sel) {
                Color b = bg(status);
                setBackground(b != null ? b : tree.getBackground());
                setForeground(fg(status));
            }
            return this;
        }

        protected abstract String label(NodeData data, EntryStatus status);
        protected abstract Color  bg(EntryStatus status);
        protected abstract Color  fg(EntryStatus status);
    }

    /** Left tree: shows LEFT_ONLY (deleted) highlighted; RIGHT_ONLY greyed out. */
    private static class LeftRenderer extends BaseRenderer {
        @Override protected String label(NodeData data, EntryStatus status) {
            String n = data.isFile() ? data.name : data.name + "/";
            return switch (status) {
                case DIFFERENT -> n + "  " + EntryStatus.DIFFERENT.label;
                case LEFT_ONLY -> n + "  " + EntryStatus.LEFT_ONLY.label;
                default        -> n;
            };
        }
        @Override protected Color bg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT -> SyntaxStyleUtil.changedBg();
                case LEFT_ONLY -> SyntaxStyleUtil.deletedBg();
                default        -> null;
            };
        }
        @Override protected Color fg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowFgChanged();
                case LEFT_ONLY  -> SyntaxStyleUtil.rowFgDeleted();
                case RIGHT_ONLY -> UIManager.getColor("Label.disabledForeground");
            };
        }
    }

    /** Right tree: shows RIGHT_ONLY (added) highlighted; LEFT_ONLY greyed out. */
    private static class RightRenderer extends BaseRenderer {
        @Override protected String label(NodeData data, EntryStatus status) {
            String n = data.isFile() ? data.name : data.name + "/";
            return switch (status) {
                case DIFFERENT  -> n + "  " + EntryStatus.DIFFERENT.label;
                case RIGHT_ONLY -> n + "  " + EntryStatus.RIGHT_ONLY.label;
                default         -> n;
            };
        }
        @Override protected Color bg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.changedBg();
                case RIGHT_ONLY -> SyntaxStyleUtil.addedBg();
                default         -> null;
            };
        }
        @Override protected Color fg(EntryStatus status) {
            return switch (status) {
                case DIFFERENT  -> SyntaxStyleUtil.rowFgChanged();
                case RIGHT_ONLY -> SyntaxStyleUtil.rowFgAdded();
                case LEFT_ONLY  -> UIManager.getColor("Label.disabledForeground");
            };
        }
    }
}
