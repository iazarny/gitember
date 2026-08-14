package com.az.gitember.dialog;

import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.data.Project;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.ui.misc.Util;
import com.az.gitember.ui.workspace.ProjectBranchSelector;
import org.eclipse.jgit.api.MergeCommand;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workspace counterpart of {@link com.az.gitember.ui.MergeDialog}: lets the user pick, per
 * repository, which local branch to merge into that repository's current branch, then applies
 * one shared commit message / squash / fast-forward setting to all of them.
 * <p>
 * Repositories left on "skip" are excluded from {@link #getResult()}.
 */
public class WorkspaceMergeDialog extends JDialog {

    // Remember last choices across invocations, as MergeDialog does
    private static boolean lastSquash = false;
    private static MergeCommand.FastForwardMode lastFFmode =
            MergeCommand.FastForwardMode.FF;

    private final List<ProjectBranchSelector> selectors = new ArrayList<>();
    private final JTextArea messageArea;

    private Map<Project, MergeDialogResult> result;

    /**
     * @param sourceBranch      branch the user acted on, used to preselect the matching branch in
     *                          every repository that has one; may be {@code null}
     * @param branchesByProject each workspace project's branches, loaded by the caller off the EDT
     *                          (iteration order determines row order)
     */
    public WorkspaceMergeDialog(Frame owner, ScmBranch sourceBranch,
                                Map<Project, List<ScmBranch>> branchesByProject) {
        super(owner, "Merge Branch in Workspace", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String preselect = sourceBranch != null ? sourceBranch.getShortName() : null;

        // ── per-repository branch rows ──────────────────────────────────────
        JPanel rows = new JPanel(new GridBagLayout());
        rows.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        GridBagConstraints rc = new GridBagConstraints();
        rc.gridx = 0;
        rc.weightx = 1.0;
        rc.fill = GridBagConstraints.HORIZONTAL;
        rc.anchor = GridBagConstraints.WEST;
        rc.insets = new Insets(1, 0, 1, 0);

        int row = 0;
        for (Map.Entry<Project, List<ScmBranch>> entry : branchesByProject.entrySet()) {
            ProjectBranchSelector selector =
                    new ProjectBranchSelector(entry.getKey(), entry.getValue(), preselect);
            selectors.add(selector);
            rc.gridy = row++;
            rows.add(selector, rc);
        }
        // Align the repository-name column across all rows
        int nameWidth = selectors.stream().mapToInt(ProjectBranchSelector::getPreferredNameWidth).max().orElse(0);
        selectors.forEach(s -> s.setNameColumnWidth(nameWidth));

        // Soak up leftover vertical space so rows stay top-aligned
        rc.gridy = row;
        rc.weighty = 1.0;
        rc.fill = GridBagConstraints.BOTH;
        rows.add(Box.createVerticalGlue(), rc);

        JScrollPane rowsScroll = new JScrollPane(rows);
        rowsScroll.setBorder(BorderFactory.createTitledBorder("Branch to merge per repository"));
        rowsScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ── shared merge options ────────────────────────────────────────────
        messageArea = new JTextArea(preselect != null ? "Merge " + preselect : "", 5, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createTitledBorder("Commit message"));

        JCheckBox squashCheck = new JCheckBox("Squash commits", lastSquash);

        JComboBox<MergeCommand.FastForwardMode> ffCombo =
                new JComboBox<>(new MergeCommand.FastForwardMode[]{
                        MergeCommand.FastForwardMode.FF,
                        MergeCommand.FastForwardMode.NO_FF,
                        MergeCommand.FastForwardMode.FF_ONLY
                });
        ffCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(ffModeLabel((MergeCommand.FastForwardMode) value));
                return this;
            }
        });
        ffCombo.setSelectedItem(lastFFmode);

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        options.add(squashCheck);
        options.add(Box.createHorizontalStrut(12));
        options.add(new JLabel("Fast-forward mode:"));
        options.add(ffCombo);

        JPanel south = new JPanel(new BorderLayout());
        south.add(messageScroll, BorderLayout.CENTER);
        south.add(options, BorderLayout.SOUTH);
        south.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // ── buttons ─────────────────────────────────────────────────────────
        JButton okBtn = new JButton("Merge");
        JButton cancelBtn = new JButton("Cancel");
        getRootPane().setDefaultButton(okBtn);

        okBtn.addActionListener(e -> {
            lastSquash = squashCheck.isSelected();
            lastFFmode = (MergeCommand.FastForwardMode) ffCombo.getSelectedItem();
            String sharedMessage = messageArea.getText().trim();

            Map<Project, MergeDialogResult> selected = new LinkedHashMap<>();
            for (ProjectBranchSelector selector : selectors) {
                ScmBranch branch = selector.getSelectedBranch();
                if (branch == null) {
                    continue; // skipped
                }
                ScmBranch target = selector.getTargetBranch();
                String message = !sharedMessage.isEmpty()
                        ? sharedMessage
                        : "Merge " + branch.getShortName()
                          + (target != null ? " into " + target.getShortName() : "");
                selected.put(selector.getProject(), new MergeDialogResult(
                        branch.getFullName(), message, lastSquash, lastFFmode));
            }

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No repository selected — pick a branch for at least one repository.",
                        "Merge", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = selected;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        // ── layout ──────────────────────────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(0, 6));
        main.add(rowsScroll, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(btnRow, BorderLayout.SOUTH);

        setSize(620, 520);
        setMinimumSize(new Dimension(520, 400));
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);
    }

    /**
     * Per-project merge parameters keyed by project, or {@code null} if the dialog was
     * cancelled. Only repositories the user actually selected a branch for are present.
     */
    public Map<Project, MergeDialogResult> getResult() {
        return result;
    }

    private static String ffModeLabel(MergeCommand.FastForwardMode mode) {
        if (mode == null) return "";
        return switch (mode) {
            case FF -> "Fast Forward";
            case NO_FF -> "No Fast Forward";
            case FF_ONLY -> "Fast Forward Only";
        };
    }
}
