package com.az.gitember.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Araxis-Merge style folder comparison window.
 *
 * <ul>
 *   <li>Recursively compares two directory trees.</li>
 *   <li>Table shows: Left path | Status | Right path.</li>
 *   <li>Colour-coded rows: blue = different, red/pink = left-only, green = right-only.</li>
 *   <li>Double-click on DIFFERENT opens {@link DiffViewerWindow}.</li>
 *   <li>"Differences only" filter toggle and Prev/Next navigation.</li>
 *   <li>Both path fields accept drag-and-drop of a folder.</li>
 * </ul>
 */
public class FolderCompareWindow extends JFrame {

    private static final Logger log = Logger.getLogger(FolderCompareWindow.class.getName());

    // ── Data ─────────────────────────────────────────────────────────────────

    enum EntryStatus { IDENTICAL, DIFFERENT, LEFT_ONLY, RIGHT_ONLY }

    record FolderEntry(String relativePath,
                       String leftAbsPath,
                       String rightAbsPath,
                       EntryStatus status) {}

    // ── UI components ─────────────────────────────────────────────────────────

    private final JTextField leftPathField  = new JTextField();
    private final JTextField rightPathField = new JTextField();
    private final JButton    compareBtn     = new JButton("Compare");

    private final JToggleButton diffsOnlyBtn = new JToggleButton("Differences only");
    private final JButton prevBtn  = new JButton("◄ Prev");
    private final JButton nextBtn  = new JButton("Next ►");
    private final JLabel  statsLbl = new JLabel(" ");

    private final FolderTableModel tableModel = new FolderTableModel();
    private final JTable table;

    // ── State ────────────────────────────────────────────────────────────────

    private List<FolderEntry> allEntries  = Collections.emptyList();
    private int currentDiffIdx = -1;

    // ── Construction ──────────────────────────────────────────────────────────

    public FolderCompareWindow() {
        super("Compare Folders");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // ---- Path selector bar ----
        JPanel pathBar = new JPanel(new GridBagLayout());
        pathBar.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

        JButton browseLeft  = new JButton("Browse…");
        JButton browseRight = new JButton("Browse…");
        browseLeft .addActionListener(e -> browse(leftPathField));
        browseRight.addActionListener(e -> browse(rightPathField));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 3, 2, 3);
        c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        pathBar.add(browseLeft, c);
        c.gridx = 1; c.weightx = 0.45; c.fill = GridBagConstraints.HORIZONTAL;
        pathBar.add(leftPathField, c);

        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        pathBar.add(compareBtn, c);

        c.gridx = 3; c.weightx = 0.45; c.fill = GridBagConstraints.HORIZONTAL;
        pathBar.add(rightPathField, c);
        c.gridx = 4; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        pathBar.add(browseRight, c);

        // ---- Filter / navigation toolbar ----
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        diffsOnlyBtn.addActionListener(e -> applyFilter());
        prevBtn.addActionListener(e -> navigateDiff(-1));
        nextBtn.addActionListener(e -> navigateDiff(+1));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterBar.add(diffsOnlyBtn);
        filterBar.add(new JSeparator(SwingConstants.VERTICAL));
        filterBar.add(prevBtn);
        filterBar.add(nextBtn);
        filterBar.add(Box.createHorizontalStrut(12));
        filterBar.add(statsLbl);

        // ---- Top area ----
        JPanel top = new JPanel(new BorderLayout());
        top.add(pathBar,   BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);

        // ---- Table ----
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(4, 0));
        table.getTableHeader().setReorderingAllowed(false);

        // Column widths: left path stretches, status is narrow, right path stretches
        table.getColumnModel().getColumn(0).setPreferredWidth(440);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(440);

        table.getColumnModel().getColumn(0).setCellRenderer(new PathRenderer(true));
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new PathRenderer(false));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) openEntry(tableModel.getEntryAt(row));
                }
            }
        });

        setupContextMenu();

        // ---- Legend bar ----
        JPanel legendBar = buildLegend();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(top,                        BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table),     BorderLayout.CENTER);
        getContentPane().add(legendBar,                  BorderLayout.SOUTH);

        // ---- DnD ----
        CompareFilesDialog.attachFileDrop(leftPathField,  true);
        CompareFilesDialog.attachFileDrop(rightPathField, true);

        compareBtn.addActionListener(e -> startScan());
    }

    // ── Scanning ─────────────────────────────────────────────────────────────

    private void startScan() {
        String leftStr  = leftPathField.getText().trim();
        String rightStr = rightPathField.getText().trim();

        if (leftStr.isEmpty() || rightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both folders.", "Compare Folders",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path leftRoot  = Path.of(leftStr);
        Path rightRoot = Path.of(rightStr);

        if (!Files.isDirectory(leftRoot) || !Files.isDirectory(rightRoot)) {
            JOptionPane.showMessageDialog(this, "Both paths must be directories.", "Compare Folders",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        compareBtn.setEnabled(false);
        statsLbl.setText("Scanning…");
        tableModel.setEntries(Collections.emptyList());
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        SwingWorker<List<FolderEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FolderEntry> doInBackground() throws Exception {
                return scan(leftRoot, rightRoot);
            }

            @Override
            protected void done() {
                compareBtn.setEnabled(true);
                try {
                    allEntries = get();
                    currentDiffIdx = -1;
                    applyFilter();
                    updateStats();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Folder scan failed", ex);
                    statsLbl.setText("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private List<FolderEntry> scan(Path leftRoot, Path rightRoot) throws Exception {
        Set<String> leftRel  = collectRelativePaths(leftRoot);
        Set<String> rightRel = collectRelativePaths(rightRoot);

        Set<String> all = new TreeSet<>();
        all.addAll(leftRel);
        all.addAll(rightRel);

        List<FolderEntry> entries = new ArrayList<>(all.size());
        for (String rel : all) {
            boolean inLeft  = leftRel.contains(rel);
            boolean inRight = rightRel.contains(rel);
            String  leftAbs  = inLeft  ? leftRoot.resolve(rel).toString()  : null;
            String  rightAbs = inRight ? rightRoot.resolve(rel).toString() : null;

            EntryStatus status;
            if (inLeft && inRight) {
                long mismatch = Files.mismatch(Path.of(leftAbs), Path.of(rightAbs));
                status = (mismatch == -1L) ? EntryStatus.IDENTICAL : EntryStatus.DIFFERENT;
            } else if (inLeft) {
                status = EntryStatus.LEFT_ONLY;
            } else {
                status = EntryStatus.RIGHT_ONLY;
            }
            entries.add(new FolderEntry(rel, leftAbs, rightAbs, status));
        }
        return entries;
    }

    private Set<String> collectRelativePaths(Path root) throws Exception {
        if (!Files.exists(root)) return Collections.emptySet();
        Set<String> paths = new TreeSet<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String rel = root.relativize(file).toString().replace('\\', '/');
                paths.add(rel);
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }

    // ── Filtering & navigation ────────────────────────────────────────────────

    private void applyFilter() {
        boolean diffsOnly = diffsOnlyBtn.isSelected();
        List<FolderEntry> visible = diffsOnly
                ? allEntries.stream()
                    .filter(e -> e.status() != EntryStatus.IDENTICAL)
                    .toList()
                : allEntries;
        tableModel.setEntries(visible);
        currentDiffIdx = -1;
        updateNavButtons();
    }

    private void navigateDiff(int direction) {
        List<FolderEntry> entries = tableModel.getEntries();
        List<Integer> diffRows = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).status() != EntryStatus.IDENTICAL) diffRows.add(i);
        }
        if (diffRows.isEmpty()) return;

        if (direction > 0) {
            currentDiffIdx = (currentDiffIdx < diffRows.size() - 1) ? currentDiffIdx + 1 : 0;
        } else {
            currentDiffIdx = (currentDiffIdx > 0) ? currentDiffIdx - 1 : diffRows.size() - 1;
        }

        int row = diffRows.get(currentDiffIdx);
        table.setRowSelectionInterval(row, row);
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
        updateNavButtons();
    }

    private void updateNavButtons() {
        long diffs = tableModel.getEntries().stream()
                .filter(e -> e.status() != EntryStatus.IDENTICAL).count();
        prevBtn.setEnabled(diffs > 0);
        nextBtn.setEnabled(diffs > 0);
    }

    private void updateStats() {
        long total     = allEntries.size();
        long different = allEntries.stream().filter(e -> e.status() == EntryStatus.DIFFERENT).count();
        long leftOnly  = allEntries.stream().filter(e -> e.status() == EntryStatus.LEFT_ONLY).count();
        long rightOnly = allEntries.stream().filter(e -> e.status() == EntryStatus.RIGHT_ONLY).count();
        long identical = allEntries.stream().filter(e -> e.status() == EntryStatus.IDENTICAL).count();
        statsLbl.setText(total + " files  |  " + identical + " identical  |  "
                + different + " different  |  " + leftOnly + " left only  |  " + rightOnly + " right only");
    }

    // ── Opening entries ───────────────────────────────────────────────────────

    private void openEntry(FolderEntry entry) {
        if (entry == null) return;
        switch (entry.status()) {
            case DIFFERENT  -> openDiff(entry);
            case LEFT_ONLY  -> openSingle(entry.leftAbsPath(),  entry.relativePath());
            case RIGHT_ONLY -> openSingle(entry.rightAbsPath(), entry.relativePath());
            case IDENTICAL  -> openDiff(entry);   // open to confirm they're equal
        }
    }

    private void openDiff(FolderEntry entry) {
        if (entry.leftAbsPath() == null || entry.rightAbsPath() == null) return;

        File lf = new File(entry.leftAbsPath());
        File rf = new File(entry.rightAbsPath());

        if (CompareFilesDialog.looksLikeBinary(lf) || CompareFilesDialog.looksLikeBinary(rf)) {
            JOptionPane.showMessageDialog(this,
                    entry.relativePath() + " appears to be binary.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                return new String[]{
                    Files.readString(lf.toPath()),
                    Files.readString(rf.toPath())
                };
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    DiffViewerWindow w = new DiffViewerWindow(
                            entry.relativePath(),
                            leftPathField.getText().trim(),  texts[0],
                            rightPathField.getText().trim(), texts[1]);
                    w.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open diff", ex);
                    JOptionPane.showMessageDialog(FolderCompareWindow.this,
                            "Cannot open diff: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openSingle(String absPath, String relPath) {
        if (absPath == null) return;
        File f = new File(absPath);
        if (CompareFilesDialog.looksLikeBinary(f)) {
            JOptionPane.showMessageDialog(this, relPath + " is a binary file.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return Files.readString(f.toPath());
            }
            @Override protected void done() {
                try {
                    FileViewerWindow w = new FileViewerWindow(relPath, get(), relPath);
                    w.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to open file", ex);
                }
            }
        };
        worker.execute();
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
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            target.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    // ── Context menu ──────────────────────────────────────────────────────────

    private void setupContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem openDiffItem  = new JMenuItem("Open diff");
        JMenuItem openLeftItem  = new JMenuItem("Open left file");
        JMenuItem openRightItem = new JMenuItem("Open right file");

        openDiffItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) openDiff(tableModel.getEntryAt(row));
        });
        openLeftItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                FolderEntry entry = tableModel.getEntryAt(row);
                openSingle(entry.leftAbsPath(), entry.relativePath());
            }
        });
        openRightItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                FolderEntry entry = tableModel.getEntryAt(row);
                openSingle(entry.rightAbsPath(), entry.relativePath());
            }
        });

        menu.add(openDiffItem);
        menu.addSeparator();
        menu.add(openLeftItem);
        menu.add(openRightItem);

        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                int row = table.getSelectedRow();
                FolderEntry entry = (row >= 0) ? tableModel.getEntryAt(row) : null;
                openDiffItem.setEnabled(entry != null
                        && entry.leftAbsPath() != null && entry.rightAbsPath() != null);
                openLeftItem.setEnabled(entry != null && entry.leftAbsPath() != null);
                openRightItem.setEnabled(entry != null && entry.rightAbsPath() != null);
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        table.setComponentPopupMenu(menu);
    }

    // ── Legend ────────────────────────────────────────────────────────────────

    private static JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        p.add(legendChip(diffColor(), "Different"));
        p.add(legendChip(leftOnlyColor(), "Left only"));
        p.add(legendChip(rightOnlyColor(), "Right only"));
        p.add(legendChip(null, "Identical (default)"));
        return p;
    }

    private static JLabel legendChip(Color bg, String text) {
        JLabel l = new JLabel(" " + text + " ");
        l.setOpaque(bg != null);
        if (bg != null) l.setBackground(bg);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 11f));
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return l;
    }

    // ── Colours ───────────────────────────────────────────────────────────────

    private static Color diffColor()      { return new Color(0xD6E4FF); }   // blue tint
    private static Color leftOnlyColor()  { return new Color(0xFFD6D6); }   // red tint
    private static Color rightOnlyColor() { return new Color(0xD6FFD6); }   // green tint

    // ── Table model ───────────────────────────────────────────────────────────

    private static class FolderTableModel extends AbstractTableModel {

        private static final String[] COLS = {"Left", "Status", "Right"};
        private List<FolderEntry> entries = new ArrayList<>();

        void setEntries(List<FolderEntry> data) {
            this.entries = new ArrayList<>(data);
            fireTableDataChanged();
        }

        List<FolderEntry> getEntries() { return entries; }

        FolderEntry getEntryAt(int row) {
            return (row >= 0 && row < entries.size()) ? entries.get(row) : null;
        }

        @Override public int getRowCount()    { return entries.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            FolderEntry e = entries.get(row);
            return switch (col) {
                case 0 -> e.leftAbsPath()  != null ? e.relativePath() : "";
                case 1 -> statusSymbol(e.status());
                case 2 -> e.rightAbsPath() != null ? e.relativePath() : "";
                default -> "";
            };
        }

        private static String statusSymbol(EntryStatus s) {
            return switch (s) {
                case IDENTICAL  -> "=";
                case DIFFERENT  -> "≠";
                case LEFT_ONLY  -> "◄";
                case RIGHT_ONLY -> "►";
            };
        }
    }

    // ── Cell renderers ────────────────────────────────────────────────────────

    /** Colours left/right path cells based on the row's entry status. */
    private class PathRenderer extends DefaultTableCellRenderer {
        private final boolean isLeft;
        PathRenderer(boolean isLeft) { this.isLeft = isLeft; }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            FolderEntry entry = tableModel.getEntryAt(row);
            if (!sel && entry != null) {
                Color bg = switch (entry.status()) {
                    case DIFFERENT  -> diffColor();
                    case LEFT_ONLY  -> isLeft ? leftOnlyColor()  : new Color(0xF0F0F0);
                    case RIGHT_ONLY -> isLeft ? new Color(0xF0F0F0) : rightOnlyColor();
                    case IDENTICAL  -> t.getBackground();
                };
                setBackground(bg);
                Color fg = entry.status() == EntryStatus.IDENTICAL
                        ? t.getForeground().darker()
                        : t.getForeground();
                setForeground(fg);
            }
            return this;
        }
    }

    /** Status column renderer — bold, centered. */
    private class StatusRenderer extends DefaultTableCellRenderer {
        StatusRenderer() {
            setHorizontalAlignment(CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            FolderEntry entry = tableModel.getEntryAt(row);
            if (!sel && entry != null) {
                Color bg = switch (entry.status()) {
                    case DIFFERENT  -> diffColor();
                    case LEFT_ONLY  -> leftOnlyColor();
                    case RIGHT_ONLY -> rightOnlyColor();
                    case IDENTICAL  -> t.getBackground();
                };
                setBackground(bg);
            }
            return this;
        }
    }
}
