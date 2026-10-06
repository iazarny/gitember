package com.az.gitember.dialog;

import com.az.gitember.data.Issue;
import com.az.gitember.service.tracker.IssueBranchNamer;
import com.az.gitember.service.tracker.IssueTrackerService;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Create-branch dialog with an optional issue picker. Selecting an issue fills the
 * branch name from the configured pattern ({@code feature/PAY-123-fix-payment-timeout}).
 */
public class CreateBranchDialog extends JDialog {

    private final JTextField nameField;
    private final IssuePickerPanel issuePicker;
    private boolean confirmed;
    private boolean nameEditedByUser;
    private boolean applyingSuggestedName;

    public CreateBranchDialog(Window owner, String title, String prompt, String namePreset) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setResizable(true);

        issuePicker = new IssuePickerPanel(false);
        nameField = new JTextField(namePreset != null ? namePreset : "", 36);
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markUserEdit(); }
            @Override public void removeUpdate(DocumentEvent e) { markUserEdit(); }
            @Override public void changedUpdate(DocumentEvent e) { markUserEdit(); }
        });
        issuePicker.addIssueChangeListener(issue -> applyIssueName(issue));
        if (namePreset != null && !namePreset.isBlank()) {
            nameEditedByUser = true;
        }

        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        form.add(issuePicker, BorderLayout.NORTH);

        JPanel nameRow = new JPanel(new BorderLayout(6, 0));
        nameRow.add(new JLabel(prompt), BorderLayout.NORTH);
        nameRow.add(nameField, BorderLayout.CENTER);
        String pattern = IssueTrackerService.config() != null
                ? IssueTrackerService.config().getBranchNamePattern()
                : "";
        JLabel hint = new JLabel("Pattern: " + pattern);
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, hint.getFont().getSize() - 1f));
        nameRow.add(hint, BorderLayout.SOUTH);
        form.add(nameRow, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> {
            confirmed = nameField.getText() != null && !nameField.getText().isBlank();
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        Util.bindEscapeToDispose(this);

        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(owner);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getBranchName() {
        return nameField.getText();
    }

    public Issue getSelectedIssue() {
        return issuePicker.getSelectedIssue();
    }

    private void markUserEdit() {
        if (!applyingSuggestedName) {
            nameEditedByUser = true;
        }
    }

    private void applyIssueName(Issue issue) {
        if (!nameEditedByUser && issue != null) {
            String suggested = IssueBranchNamer.branchName(issue, IssueTrackerService.config());
            if (suggested != null && !suggested.isBlank()) {
                applyingSuggestedName = true;
                nameField.setText(suggested);
                applyingSuggestedName = false;
            }
        }
    }
}
