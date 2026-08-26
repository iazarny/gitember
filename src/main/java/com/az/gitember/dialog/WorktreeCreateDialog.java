package com.az.gitember.dialog;

import com.az.gitember.data.WorktreeInfo;
import com.az.gitember.service.Context;
import com.az.gitember.ui.misc.Util;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WorktreeCreateDialog extends JDialog {

    private  JTextField pathField;
    private  JTextField branchField;
    private  JCheckBox  newBranchCb = new JCheckBox("Create new branch", true);
    private boolean confirmed;
    private String existingBranchName = null;

    public WorktreeCreateDialog(Frame parent, java.util.List<WorktreeInfo> existingWorktrees, String name) {
        super(parent, "Add Worktree", true);
        existingBranchName = name;
        init(existingWorktrees);
    }

    public WorktreeCreateDialog(Dialog parent, java.util.List<WorktreeInfo> existingWorktrees) {
        super(parent, "Add Worktree", true);
        init(existingWorktrees);

    }

    private void init(java.util.List<WorktreeInfo> existingWorktrees) {
        setSize(540, 210);
        setLocationRelativeTo(this.getParent());
        setResizable(false);

        int    nextN   = nextFeatureNumber(existingWorktrees);
        String paddedN = existingBranchName == null ? String.format("%03d", nextN) : existingBranchName;

        pathField   = new JTextField(suggestedPath(paddedN), 34);
        branchField = new JTextField(
                existingBranchName == null ?
                        "feature/" + paddedN : existingBranchName, 20);
        if (existingBranchName != null) {
            newBranchCb.setSelected(false);
            newBranchCb.setEnabled(false);
            branchField.setEnabled(false);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 4, 4, 4);
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Path:"), gbc);
        JPanel pathRow = new JPanel(new BorderLayout(4, 0));
        pathRow.add(pathField, BorderLayout.CENTER);
        JButton browseBtn = new JButton("…");
        browseBtn.addActionListener(e -> browseFolder());
        pathRow.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(pathRow, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Branch:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(branchField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        form.add(newBranchCb, gbc);

        JButton addBtn    = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        addBtn.addActionListener(e -> onConfirm());
        cancelBtn.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(addBtn);
        btns.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(addBtn);
        Util.bindEscapeToDispose(this);
    }

    /**
     * Scans existing worktree branches for {@code feature/NNN} or {@code feature_NNN},
     * returns max N + 1. Returns 1 if none found.
     */
    private static int nextFeatureNumber(List<WorktreeInfo> worktrees) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("feature[/_](\\d+)");
        int max = 0;
        for (WorktreeInfo wt : worktrees) {
            String branch = wt.getBranch();
            if (branch == null) continue;
            java.util.regex.Matcher m = p.matcher(branch);
            if (m.find()) {
                try { max = Math.max(max, Integer.parseInt(m.group(1))); }
                catch (NumberFormatException ignored) {}
            }
        }
        return max + 1;
    }

    /**
     * Suggested path: sibling of the repo folder, named {@code <repoName>_feature_<nnn>}.
     * Example: repo at {@code /home/user/myproject} → {@code /home/user/myproject_feature_001}.
     */
    private static String suggestedPath(String paddedN) {
        String repoPath = Context.getProjectFolder();
        if (repoPath == null || repoPath.isBlank()) {
            return "";
        } else {
            Path repoDir = Paths.get(repoPath);
            Path parent  = repoDir.getParent();
            if (parent == null)  {
                return "";
            } else {
                String repoName = repoDir.getFileName().toString();
                return parent.resolve(repoName + "_feature_" + paddedN).toString();
            }
        }
    }

    private void browseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Worktree Folder");
        String current = pathField.getText().trim();
        if (StringUtils.isNotBlank(current)) {
            java.io.File f = new java.io.File(current).getParentFile();
            if (f != null && f.exists()) {
                chooser.setCurrentDirectory(f);
            }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onConfirm() {
        if (pathField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Path is required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (branchField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Branch name is required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed()  { return confirmed; }
    public String  getPath()      { return pathField.getText().trim(); }
    public String  getBranch()    { return branchField.getText().trim(); }
    public boolean isNewBranch()  { return newBranchCb.isSelected(); }
}