package com.az.gitember.ui;

import com.az.gitember.data.CommitSignatureDetails;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.data.SignatureStatus;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Non-modal dialog that verifies a commit signature on demand and shows the outcome.
 */
public class CommitSignatureDialog extends JDialog {

    private final ScmRevisionInformation revision;
    private final Runnable onUpdated;

    private final JLabel statusLabel = new JLabel();
    private final JLabel formatValue = valueLabel();
    private final JLabel signerValue = valueLabel();
    private final JTextArea keyValue = new JTextArea(2, 28);
    private final JLabel trustValue = valueLabel();
    private final JLabel allowedSignersValue = valueLabel();
    private final JLabel messageLabel = new JLabel();
    private final JButton verifyAgainBtn = new JButton("Verify Again");

    public CommitSignatureDialog(Window owner, ScmRevisionInformation revision, Runnable onUpdated) {
        super(owner, "Commit Signature", ModalityType.MODELESS);
        this.revision = revision;
        this.onUpdated = onUpdated;
        setIconImages(Util.appIcons());
        setResizable(false);

        keyValue.setEditable(false);
        keyValue.setLineWrap(true);
        keyValue.setWrapStyleWord(true);
        keyValue.setOpaque(false);
        keyValue.setBorder(BorderFactory.createEmptyBorder());
        keyValue.setFont(UIManager.getFont("Label.font"));

        messageLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, messageLabel.getFont().getSize() - 1f));

        JPanel body = new JPanel(new GridBagLayout());
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, statusLabel.getFont().getSize() + 2f));
        body.add(statusLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(12, 0, 6, 8);
        body.add(new JSeparator(), gbc);
        gbc.insets = new Insets(2, 0, 2, 8);
        gbc.gridwidth = 1;

        addRow(body, gbc, 2, "Format:", formatValue);
        addRow(body, gbc, 3, "Signer:", signerValue);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 2, 8);
        body.add(new JLabel("Key:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        body.add(keyValue, gbc);

        gbc.insets = new Insets(2, 0, 2, 8);
        addRow(body, gbc, 5, "Trust:", trustValue);
        addRow(body, gbc, 6, "Allowed signers:", allowedSignersValue);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 2, 0);
        body.add(messageLabel, gbc);

        verifyAgainBtn.addActionListener(e -> verifyAsync());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 18, 12, 18));
        buttons.add(verifyAgainBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(body, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setSize(Math.max(getWidth(), 380), getHeight());
        setLocationRelativeTo(owner);
        Util.bindEscapeToDispose(this);

        applyDetails(emptyPendingDetails());
        verifyAsync();
    }

    public static void open(Window owner, ScmRevisionInformation revision, Runnable onUpdated) {
        if (revision != null && revision.isSigned()) {
            CommitSignatureDialog dialog = new CommitSignatureDialog(owner, revision, onUpdated);
            dialog.setVisible(true);
        }
    }

    private void verifyAsync() {
        verifyAgainBtn.setEnabled(false);
        statusLabel.setText("Verifying…");
        statusLabel.setIcon(null);
        new SwingWorker<CommitSignatureDetails, Void>() {
            @Override
            protected CommitSignatureDetails doInBackground() {
                CommitSignatureDetails details;
                GitRepoService service = Context.getGitRepoService();
                if (service == null) {
                    details = new CommitSignatureDetails();
                    details.setStatus(SignatureStatus.UNKNOWN);
                    details.setMessage("No repository is open.");
                } else {
                    details = service.verifyCommitSignature(revision);
                }
                return details;
            }

            @Override
            protected void done() {
                CommitSignatureDetails details;
                try {
                    details = get();
                } catch (Exception ex) {
                    details = new CommitSignatureDetails();
                    details.setStatus(SignatureStatus.UNKNOWN);
                    details.setMessage(ex.getMessage());
                }
                applyDetails(details);
                if (onUpdated != null) {
                    onUpdated.run();
                }
                boolean verifyEnabled = Context.getSettings() != null
                        && Context.getSettings().isVerifyCommitSignatures();
                verifyAgainBtn.setEnabled(verifyEnabled);
                pack();
            }
        }.execute();
    }

    private void applyDetails(CommitSignatureDetails details) {
        SignatureStatus status = details.getStatus() != null ? details.getStatus() : SignatureStatus.UNSIGNED;
        statusLabel.setIcon(statusIcon(status, 18));
        statusLabel.setText(statusHeadline(status));
        statusLabel.setForeground(statusColor(status));
        formatValue.setText(blankToDash(details.getFormat()));
        signerValue.setText(blankToDash(details.getSigner()));
        keyValue.setText(blankToDash(details.getKeyFingerprint()));
        trustValue.setText(blankToDash(details.getTrust()));
        allowedSignersValue.setText(blankToDash(details.getAllowedSignersPath()));
        String message = details.getMessage();
        messageLabel.setText(message != null && !message.isBlank() ? message : " ");
    }

    static Icon statusIcon(SignatureStatus status, int size) {
        FontAwesomeSolid ikon = FontAwesomeSolid.KEY;
        Color color = UIManager.getColor("Label.foreground");
        if (status == SignatureStatus.VERIFIED) {
            ikon = FontAwesomeSolid.CHECK_CIRCLE;
            color = new Color(0x2E7D32);
        } else if (status == SignatureStatus.INVALID) {
            ikon = FontAwesomeSolid.TIMES_CIRCLE;
            color = new Color(0xC62828);
        } else if (status == SignatureStatus.UNKNOWN) {
            ikon = FontAwesomeSolid.QUESTION_CIRCLE;
            color = UIManager.getColor("Label.disabledForeground");
        } else if (status == SignatureStatus.SIGNED) {
            ikon = FontAwesomeSolid.KEY;
        }
        return FontIcon.of(ikon, size, color != null ? color : Color.GRAY);
    }

    static Color statusColor(SignatureStatus status) {
        Color color = UIManager.getColor("Label.foreground");
        if (status == SignatureStatus.VERIFIED) {
            color = new Color(0x2E7D32);
        } else if (status == SignatureStatus.INVALID) {
            color = new Color(0xC62828);
        } else if (status == SignatureStatus.UNKNOWN) {
            color = UIManager.getColor("Label.disabledForeground");
        }
        return color;
    }

    static String statusHeadline(SignatureStatus status) {
        String headline = "Signed";
        if (status == SignatureStatus.VERIFIED) {
            headline = "Verified";
        } else if (status == SignatureStatus.INVALID) {
            headline = "Invalid";
        } else if (status == SignatureStatus.UNKNOWN) {
            headline = "Unknown";
        } else if (status == SignatureStatus.SIGNED) {
            headline = "Signed";
        }
        return headline;
    }

    private static CommitSignatureDetails emptyPendingDetails() {
        CommitSignatureDetails details = new CommitSignatureDetails();
        details.setStatus(SignatureStatus.SIGNED);
        return details;
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("—");
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        return label;
    }

    private static String blankToDash(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value;
    }

    private static void addRow(JPanel body, GridBagConstraints gbc, int row, String caption, JComponent value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(4, 0, 4, 12);
        body.add(new JLabel(caption), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        body.add(value, gbc);
    }
}
