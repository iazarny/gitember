package com.az.gitember.dialog;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Rebase dialog.
 *
 * <p>Displays a table of commits that will be rebased, allowing the user to:
 * <ul>
 *   <li>Change the action for each commit (pick / reword / squash / fixup / drop)</li>
 *   <li>Reorder commits via drag-and-drop or the Up / Down buttons</li>
 *   <li>Edit the commit message inline (used when the action is "reword")</li>
 * </ul>
 *
 * <p>Commits are displayed <em>newest-first</em> (HEAD on top), matching the
 * history panel's natural order.  The caller reverses the list before handing
 * it to JGit, which applies commits oldest-first.
 */
public class InteractiveRebaseDialog extends JDialog {

    // ── Action enum ───────────────────────────────────────────────────────────

    /**
     * The rebase action for a single commit.  Using a project-local enum
     * avoids a hard dependency on JGit's {@code RebaseTodoLine.Action} in the
     * dialog layer, and allows us to define DROP (not available in all JGit
     * versions).
     */
    public enum RebaseAction {
        PICK, REWORD, SQUASH, FIXUP, DROP;

        /** Human-readable label shown in the combo-box and quick-action buttons. */
        public String label() { return name().toLowerCase(); }
    }

    // ── Action display metadata ───────────────────────────────────────────────

    private static final RebaseAction[] ORDERED_ACTIONS = {
            RebaseAction.PICK,
            RebaseAction.REWORD,
            RebaseAction.SQUASH,
            RebaseAction.FIXUP,
            RebaseAction.DROP
    };

    private static final String[] ACTION_LABELS =
            java.util.Arrays.stream(ORDERED_ACTIONS)
                    .map(RebaseAction::label)
                    .toArray(String[]::new);

    /** Background colours that visually distinguish each action row. */
    static final Color[] ACTION_COLORS = {
            new Color(220, 240, 220),  // pick   – soft green
            new Color(210, 230, 255),  // reword – soft blue
            new Color(255, 248, 210),  // squash – soft yellow
            new Color(255, 235, 205),  // fixup  – soft orange
            new Color(255, 215, 215)   // drop   – soft red
    };

    // ── Data model ───────────────────────────────────────────────────────────

    /**
     * A single entry in the interactive rebase plan.
     */
    public static class RebaseStep {

        private RebaseAction action;
        /** Full 40-char SHA used for matching against JGit's plan entries. */
        private final String fullSha;
        /** Commit message; editable when action == REWORD. */
        private String message;

        public RebaseStep(RebaseAction action, String fullSha, String message) {
            this.action  = action;
            this.fullSha = fullSha;
            this.message = message;
        }

        public RebaseAction getAction()  { return action;  }
        public String       getFullSha() { return fullSha; }
        public String       getMessage() { return message; }

        public void setAction(RebaseAction a)  { this.action  = a; }
        public void setMessage(String m)       { this.message = m; }

        /** Returns the first 7 characters of the SHA for display purposes. */
        public String getDisplaySha() {
            return fullSha.length() >= 7 ? fullSha.substring(0, 7) : fullSha;
        }
    }

    // ── Instance state ────────────────────────────────────────────────────────

    private final List<RebaseStep> steps;
    private boolean confirmed = false;

    private final JTable          table;
    private final RebasePlanModel model;

    /** Row grabbed at the start of a drag operation. */
    private int dragSourceRow = -1;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param owner          owning frame
     * @param baseDisplaySha abbreviated SHA shown in the header (for context)
     * @param initialSteps   commits to rebase, <em>newest-first</em>
     */
    public InteractiveRebaseDialog(Frame owner, String baseDisplaySha,
                                   List<RebaseStep> initialSteps) {
        super(owner, "Interactive Rebase", Dialog.ModalityType.DOCUMENT_MODAL);
        this.steps = new ArrayList<>(initialSteps);
        setSize(860, 540);
        setLocationRelativeTo(owner);
        setResizable(true);
        setMinimumSize(new Dimension(600, 400));

        // ── Warning / info header ─────────────────────────────────────────────
        JLabel warningLabel = new JLabel(
                "<html><b>Warning:</b> Interactive rebase rewrites history. "
                + "Only use on <b>local, unpushed</b> commits.</html>");
        warningLabel.setForeground(new Color(160, 80, 0));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 2, 12));

        JLabel baseLabel = new JLabel(
                "<html>Rebasing commits onto: <b>" + baseDisplaySha + "</b>"
                + " &nbsp;—&nbsp; drag rows or use ▲/▼ to reorder; double-click a message to edit</html>");
        baseLabel.setBorder(BorderFactory.createEmptyBorder(2, 12, 8, 12));

        // ── Table ─────────────────────────────────────────────────────────────
        model = new RebasePlanModel();
        table = new JTable(model);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Action column – combo-box editor
        JComboBox<String> actionCombo = new JComboBox<>(ACTION_LABELS);
        actionCombo.setFont(table.getFont());
        TableColumn actionCol = table.getColumnModel().getColumn(0);
        actionCol.setPreferredWidth(80);
        actionCol.setMaxWidth(100);
        actionCol.setCellEditor(new DefaultCellEditor(actionCombo));
        actionCol.setCellRenderer(new ActionCellRenderer());

        // SHA column – read-only, mono font
        TableColumn shaCol = table.getColumnModel().getColumn(1);
        shaCol.setPreferredWidth(70);
        shaCol.setMaxWidth(80);

        // Message column
        table.getColumnModel().getColumn(2).setPreferredWidth(600);

        // Row-colour renderer for SHA and message columns
        table.setDefaultRenderer(Object.class, new RowColorRenderer());

        // Drag-and-drop for reordering
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(buildTransferHandler());

        // ── Tool-bar ──────────────────────────────────────────────────────────
        JButton upBtn   = new JButton("▲ Up");
        JButton downBtn = new JButton("▼ Down");
        upBtn  .addActionListener(e -> moveSelectedRow(-1));
        downBtn.addActionListener(e -> moveSelectedRow(+1));
        upBtn  .setToolTipText("Move selected commit up (applied earlier)");
        downBtn.setToolTipText("Move selected commit down (applied later)");

        JPanel movePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        movePanel.add(upBtn);
        movePanel.add(downBtn);
        movePanel.add(Box.createHorizontalStrut(12));
        movePanel.add(new JLabel("Set: "));
        for (int i = 0; i < ORDERED_ACTIONS.length; i++) {
            movePanel.add(quickActionButton(ORDERED_ACTIONS[i], ACTION_COLORS[i]));
        }

        // ── OK / Cancel ───────────────────────────────────────────────────────
        JButton startBtn  = new JButton("Start Rebase");
        JButton cancelBtn = new JButton("Cancel");
        startBtn .setToolTipText("Begin the interactive rebase with the current plan");
        cancelBtn.setToolTipText("Abort – no changes will be made");
        startBtn .addActionListener(e -> { confirmed = true; dispose(); });
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(startBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnPanel.add(startBtn);
        btnPanel.add(cancelBtn);

        // ── Legend ────────────────────────────────────────────────────────────
        JPanel legendPanel = buildLegendPanel();

        // ── Assembly ──────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(warningLabel, BorderLayout.NORTH);
        headerPanel.add(baseLabel,    BorderLayout.CENTER);
        headerPanel.add(legendPanel,  BorderLayout.SOUTH);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(movePanel, BorderLayout.NORTH);
        southPanel.add(btnPanel,  BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout(0, 4));
        getContentPane().add(headerPanel,            BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table),  BorderLayout.CENTER);
        getContentPane().add(southPanel,              BorderLayout.SOUTH);
    }

    // ── Public accessors ─────────────────────────────────────────────────────

    /** Returns {@code true} if the user pressed "Start Rebase". */
    public boolean isConfirmed() { return confirmed; }

    /**
     * Returns the (possibly reordered / edited) plan in <em>newest-first</em>
     * display order.
     */
    public List<RebaseStep> getSteps() { return new ArrayList<>(steps); }

    // ── Private helpers ───────────────────────────────────────────────────────

    private JButton quickActionButton(RebaseAction action, Color bg) {
        JButton btn = new JButton(action.label());
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setToolTipText(actionTooltip(action));
        btn.addActionListener(e -> setSelectedAction(action));
        return btn;
    }

    private static String actionTooltip(RebaseAction a) {
        if (a == RebaseAction.PICK)   return "Keep this commit as-is";
        if (a == RebaseAction.REWORD) return "Keep commit but edit its message";
        if (a == RebaseAction.SQUASH) return "Combine with previous commit (merge messages)";
        if (a == RebaseAction.FIXUP)  return "Combine with previous commit (discard this message)";
        if (a == RebaseAction.DROP)   return "Delete this commit entirely";
        return a.label();
    }

    private JPanel buildLegendPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        p.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        String[][] entries = {
                {"pick",   "keep commit"},
                {"reword", "edit message"},
                {"squash", "combine + merge msgs"},
                {"fixup",  "combine, discard msg"},
                {"drop",   "delete commit"}
        };
        for (int i = 0; i < entries.length; i++) {
            JLabel chip = new JLabel("  ");
            chip.setOpaque(true);
            chip.setBackground(ACTION_COLORS[i]);
            chip.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            p.add(chip);
            p.add(new JLabel("<html><b>" + entries[i][0] + "</b> – " + entries[i][1] + "</html>"));
            if (i < entries.length - 1) p.add(Box.createHorizontalStrut(10));
        }
        return p;
    }

    private void moveSelectedRow(int delta) {
        stopEditing();
        int row = table.getSelectedRow();
        if (row < 0) return;
        int newRow = row + delta;
        if (newRow < 0 || newRow >= steps.size()) return;
        RebaseStep moved = steps.remove(row);
        steps.add(newRow, moved);
        model.fireTableDataChanged();
        table.setRowSelectionInterval(newRow, newRow);
        table.scrollRectToVisible(table.getCellRect(newRow, 0, true));
    }

    private void setSelectedAction(RebaseAction action) {
        stopEditing();
        int row = table.getSelectedRow();
        if (row < 0) return;
        steps.get(row).setAction(action);
        model.fireTableRowsUpdated(row, row);
    }

    private void stopEditing() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    private static int actionIndex(RebaseAction action) {
        for (int i = 0; i < ORDERED_ACTIONS.length; i++) {
            if (ORDERED_ACTIONS[i] == action) return i;
        }
        return 0;
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private class RebasePlanModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Action", "SHA", "Commit Message"};

        @Override public int getRowCount()    { return steps.size(); }
        @Override public int getColumnCount() { return 3; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 1; // SHA is read-only
        }

        @Override
        public Object getValueAt(int row, int col) {
            RebaseStep s = steps.get(row);
            return switch (col) {
                case 0  -> s.getAction().label();
                case 1  -> s.getDisplaySha();
                case 2  -> s.getMessage();
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row < 0 || row >= steps.size()) return;
            RebaseStep s = steps.get(row);
            if (col == 0 && value instanceof String label) {
                for (RebaseAction a : ORDERED_ACTIONS) {
                    if (a.label().equals(label)) { s.setAction(a); break; }
                }
            } else if (col == 2 && value instanceof String msg) {
                s.setMessage(msg);
            }
            fireTableCellUpdated(row, col);
        }
    }

    // ── Cell renderers ────────────────────────────────────────────────────────

    /** Bold action label with the row's background colour. */
    private class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
            if (!isSelected && row < steps.size()) {
                c.setBackground(ACTION_COLORS[actionIndex(steps.get(row).getAction())]);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
            return c;
        }
    }

    /** Colours other columns by the row's current action. */
    private class RowColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
            if (!isSelected && row < steps.size()) {
                c.setBackground(ACTION_COLORS[actionIndex(steps.get(row).getAction())]);
            }
            if (col == 1) {
                c.setFont(new Font(Font.MONOSPACED, Font.PLAIN, t.getFont().getSize()));
            }
            return c;
        }
    }

    // ── Drag-and-drop transfer handler ────────────────────────────────────────

    /**
     * Reorders rows by drag-and-drop within the table.
     * The source row index is stored in {@link #dragSourceRow}; a plain
     * string transferable is used to satisfy the DnD protocol without
     * needing a custom {@code DataFlavor}.
     */
    private TransferHandler buildTransferHandler() {
        return new TransferHandler() {

            @Override public int getSourceActions(JComponent c) { return MOVE; }

            @Override
            protected Transferable createTransferable(JComponent c) {
                stopEditing();
                dragSourceRow = table.getSelectedRow();
                return new StringSelection(String.valueOf(dragSourceRow));
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDrop()
                        && support.isDataFlavorSupported(DataFlavor.stringFlavor)
                        && dragSourceRow >= 0;
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
                int to   = dl.getRow();
                int from = dragSourceRow;
                dragSourceRow = -1;
                if (to > from) to--;
                if (from == to || from < 0) return false;
                RebaseStep moved = steps.remove(from);
                steps.add(to, moved);
                model.fireTableDataChanged();
                table.setRowSelectionInterval(to, to);
                return true;
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                dragSourceRow = -1;
            }
        };
    }
}
