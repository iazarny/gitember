package com.az.gitember.ui.workspace;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmBranch;
import org.apache.commons.lang3.ObjectUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable "repository → branch" row: a workspace project's name, a combo box of branches to
 * choose from, and the repository's current branch — the branch an operation would act against.
 * Dialogs needing per-repository branch input (workspace merge, and any future workspace-wide
 * branch operation) stack one of these per project.
 * <p>
 * The caller supplies the branch list rather than the component reading it, because
 * {@link Project#getLocalBranches()} is only kept up to date for the active project; workspace
 * callers must load each repository's branches themselves (off the EDT) and pass them in.
 * <p>
 * The combo box always carries a leading {@code null} entry rendered as {@value #SKIP_LABEL} so a
 * repository can be left out; {@link #getSelectedBranch()} then returns {@code null}. Rows align
 * with each other when the owner calls {@link #setNameColumnWidth(int)} with the maximum
 * {@link #getPreferredNameWidth()} across all rows.
 */
public class ProjectBranchSelector extends JPanel {

    private static final String SKIP_LABEL = "— skip —";

    private final Project project;
    private final ScmBranch targetBranch;
    private final JLabel nameLabel;
    private final JComboBox<ScmBranch> branchCombo;

    /**
     * @param project   workspace project this row represents
     * @param branches  that repository's branches, as loaded by the caller; the entry flagged
     *                  {@link ScmBranch#isHead()} is taken as the target and excluded from the
     *                  choices
     * @param preselect short name of the branch to preselect (e.g. the branch the user
     *                  right-clicked); repositories without such a branch default to "skip".
     *                  {@code null} preselects nothing.
     */
    public ProjectBranchSelector(Project project, List<ScmBranch> branches, String preselect) {
        super(new BorderLayout(8, 0));
        this.project = project;
        this.targetBranch = branches.stream().filter(ScmBranch::isHead).findFirst().orElse(null);

        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        nameLabel = new JLabel(projectName(project));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));

        branchCombo = new JComboBox<>();
        branchCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof ScmBranch b ? b.getShortName() : SKIP_LABEL);
                return this;
            }
        });

        branchCombo.addItem(null); // "skip this repository"
        List<ScmBranch> candidates = branches.stream()
                .filter(b -> !b.isHead())
                .sorted(Comparator.comparing(ScmBranch::getShortName))
                .collect(Collectors.toList());
        candidates.forEach(branchCombo::addItem);

        if (candidates.isEmpty()) {
            branchCombo.setEnabled(false);
        } else if (preselect != null) {
            candidates.stream()
                    .filter(b -> preselect.equals(b.getShortName()))
                    .findFirst()
                    .ifPresent(branchCombo::setSelectedItem);
        }

        JLabel targetLabel = new JLabel(targetBranch != null
                ? "→  " + targetBranch.getShortName()
                : "→  (no branch)");
        targetLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        add(nameLabel, BorderLayout.WEST);
        add(branchCombo, BorderLayout.CENTER);
        add(targetLabel, BorderLayout.EAST);

        setToolTipText(project.getProjectHomeFolder());
    }

    /** Short repository name, matching how workspace result dialogs label projects. */
    private static String projectName(Project project) {
        return new File(ObjectUtils.getIfNull(project.getProjectHomeFolder(), "")).getName();
    }

    public Project getProject() {
        return project;
    }

    /** The branch the user picked, or {@code null} to skip this repository. */
    public ScmBranch getSelectedBranch() {
        return (ScmBranch) branchCombo.getSelectedItem();
    }

    /** The repository's current branch — the merge target. {@code null} on an unborn HEAD. */
    public ScmBranch getTargetBranch() {
        return targetBranch;
    }

    public int getPreferredNameWidth() {
        return nameLabel.getPreferredSize().width;
    }

    /** Pins the name column to {@code width} so stacked rows line up. */
    public void setNameColumnWidth(int width) {
        Dimension d = nameLabel.getPreferredSize();
        nameLabel.setPreferredSize(new Dimension(width, d.height));
    }
}
