package com.az.gitember.ui;

import com.az.gitember.data.MergeDialogResult;
import org.eclipse.jgit.api.MergeCommand;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for merge options — Swing equivalent of the JavaFX MergeDialog.
 * Lets the user pick commit message, squash flag, and fast-forward mode.
 */
public class MergeDialog extends JDialog {

    // Remember last choices across invocations (same as JavaFX version)
    private static boolean lastSquash = false;
    private static MergeCommand.FastForwardMode lastFFmode = MergeCommand.FastForwardMode.FF;

    private MergeDialogResult result;

    public MergeDialog(Frame owner, String branchShortName, String workingBranchName) {
        super(owner, "Merge Branch", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        String defaultMessage = "Merge " + branchShortName + " into " + workingBranchName;

        // ── Controls ────────────────────────────────────────────────────────
        JTextArea messageArea = new JTextArea(defaultMessage, 4, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);

        JCheckBox squashCheck = new JCheckBox("Squash commits", lastSquash);

        JComboBox<MergeCommand.FastForwardMode> ffCombo = new JComboBox<>(new MergeCommand.FastForwardMode[]{
                MergeCommand.FastForwardMode.FF,
                MergeCommand.FastForwardMode.NO_FF,
                MergeCommand.FastForwardMode.FF_ONLY
        });
        ffCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(ffModeLabel((MergeCommand.FastForwardMode) value));
                return this;
            }
        });
        ffCombo.setSelectedItem(lastFFmode);

        // ── Layout ──────────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        int row = 0;
        content.add(new JLabel("Merging:  " + branchShortName + "  →  " + workingBranchName),
                gbc(0, row++, 2, 0));

        content.add(new JLabel("Commit message:"), gbc(0, row, 1, 0));
        row++;
        GridBagConstraints scrollGbc = gbc(0, row++, 2, 1.0);
        scrollGbc.fill = GridBagConstraints.BOTH;
        scrollGbc.weighty = 1.0;
        content.add(messageScroll, scrollGbc);

        content.add(squashCheck, gbc(0, row++, 2, 0));

        content.add(new JLabel("Fast-forward mode:"), gbc(0, row, 1, 0));
        GridBagConstraints ffGbc = gbc(1, row++, 1, 1.0);
        ffGbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(ffCombo, ffGbc);

        // ── Buttons ─────────────────────────────────────────────────────────
        JButton okBtn     = new JButton("Merge");
        JButton cancelBtn = new JButton("Cancel");
        getRootPane().setDefaultButton(okBtn);

        okBtn.addActionListener(e -> {
            lastSquash = squashCheck.isSelected();
            lastFFmode = (MergeCommand.FastForwardMode) ffCombo.getSelectedItem();
            String msg = messageArea.getText().trim();
            if (msg.isBlank()) msg = defaultMessage;
            result = new MergeDialogResult(branchShortName, msg, lastSquash, lastFFmode);
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        getContentPane().setLayout(new BorderLayout(0, 6));
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(btnRow, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);
    }

    /** Returns the user's choices, or {@code null} if the dialog was cancelled. */
    public MergeDialogResult getResult() {
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String ffModeLabel(MergeCommand.FastForwardMode mode) {
        if (mode == null) return "";
        switch (mode) {
            case FF:      return "Fast Forward";
            case NO_FF:   return "No Fast Forward";
            case FF_ONLY: return "Fast Forward Only";
            default:      return mode.toString();
        }
    }

    private static GridBagConstraints gbc(int gridx, int gridy, int gridwidth, double weightx) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx      = gridx;
        c.gridy      = gridy;
        c.gridwidth  = gridwidth;
        c.weightx    = weightx;
        c.fill       = weightx > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor     = GridBagConstraints.WEST;
        c.insets     = new Insets(4, 4, 4, 4);
        return c;
    }
}
