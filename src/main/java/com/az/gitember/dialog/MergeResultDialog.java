package com.az.gitember.dialog;

import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.misc.Util;
import org.eclipse.jgit.api.MergeResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Shows the outcome of a workspace-wide merge: one row per repository with its merge status,
 * plus a details pane listing conflicted files and per-repository errors.
 * <p>
 * Unlike the single-repository flow, conflicts are only reported here — resolving them across
 * several repositories at once is deliberately not offered; the user opens the affected
 * repository and resolves it there.
 */
public class MergeResultDialog extends JDialog {

    public MergeResultDialog(Component parent, List<ProjectOperationResult<MergeResult>> results) {
        super(SwingUtilities.getWindowAncestor(parent), "Merge Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(parent);

        long ok = results.stream().filter(MergeResultDialog::isClean).count();
        long conflicting = results.stream().filter(MergeResultDialog::isConflicting).count();

        // ---- header ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        String summary = "Merged " + ok + " of " + results.size() + " repositories"
                + (conflicting > 0 ? " — " + conflicting + " with conflicts" : "");
        JLabel titleLabel = new JLabel(summary);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // ---- per-repository summary table ----
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Repository", "Status", "Conflicts"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (ProjectOperationResult<MergeResult> r : results) {
            if (r.isSuccess()) {
                MergeResult res = r.getResult();
                model.addRow(new Object[]{
                        r.getProjectName(),
                        res != null ? res.getMergeStatus().toString() : "",
                        conflictCount(res)
                });
            } else {
                model.addRow(new Object[]{r.getProjectName(), "Failed", "—"});
            }
        }
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 1; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(center);
        }
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        JScrollPane tableScroll = new JScrollPane(table);

        // ---- details: conflicted files / errors ----
        Font monoFont = SyntaxStyleUtil.monoFont();
        JEditorPane msgArea = new JEditorPane("text/html",
                PullResultDialog.toHtml(buildReport(results), monoFont.getSize()));
        msgArea.setEditable(false);
        msgArea.setOpaque(true);
        msgArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        msgArea.setFont(monoFont);
        msgArea.setCaretPosition(0);
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(BorderFactory.createTitledBorder("Details"));
        msgScroll.setPreferredSize(new Dimension(0, 120));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, msgScroll);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        JButton closeBtn = new JButton("Close");
        closeBtn.setName("closeButton");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }

    private static boolean isClean(ProjectOperationResult<MergeResult> r) {
        return r.isSuccess() && r.getResult() != null && r.getResult().getMergeStatus().isSuccessful();
    }

    private static boolean isConflicting(ProjectOperationResult<MergeResult> r) {
        return r.isSuccess() && r.getResult() != null
                && r.getResult().getMergeStatus() == MergeResult.MergeStatus.CONFLICTING;
    }

    private static Object conflictCount(MergeResult res) {
        Map<String, int[][]> conflicts = res != null ? res.getConflicts() : null;
        return conflicts != null ? conflicts.size() : 0;
    }

    private static String buildReport(List<ProjectOperationResult<MergeResult>> results) {
        StringBuilder sb = new StringBuilder();
        for (ProjectOperationResult<MergeResult> r : results) {
            sb.append("=== ").append(r.getProjectName()).append(" ===\n");
            if (r.isSuccess()) {
                MergeResult res = r.getResult();
                sb.append(res != null ? res.getMergeStatus().toString() : "").append('\n');
                Map<String, int[][]> conflicts = res != null ? res.getConflicts() : null;
                if (conflicts != null && !conflicts.isEmpty()) {
                    sb.append("Conflicted files:\n");
                    conflicts.keySet().forEach(f -> sb.append("  ").append(f).append('\n'));
                }
            } else {
                Exception e = r.getError();
                sb.append("FAILED: ").append(e != null ? e.getMessage() : "unknown error").append('\n');
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }
}
