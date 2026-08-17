package com.az.gitember.dialog;

import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dialog that shows the result of a push operation:
 * remote URL and per-ref update statuses.
 */
public class PushResultDialog extends JDialog {

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    public PushResultDialog(Component parent, String remoteUrl, String messages) {
        super(SwingUtilities.getWindowAncestor(parent), "Push Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 320);
        setLocationRelativeTo(parent);

        // ---- header ----
        JLabel titleLabel = new JLabel("Push completed successfully");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));

        // ---- remote URL row ----
        JPanel remoteRow = new JPanel(new BorderLayout(6, 0));
        remoteRow.add(new JLabel("Remote:"), BorderLayout.WEST);
        JTextField urlField = new JTextField(remoteUrl != null ? remoteUrl : "—");
        urlField.setEditable(false);
        urlField.setOpaque(false);
        urlField.setBorder(BorderFactory.createEmptyBorder());
        remoteRow.add(urlField, BorderLayout.CENTER);

        // ---- header panel ----
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        remoteRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(remoteRow);

        // ---- messages area ----
        String text = (messages != null ? messages : "").trim();
        String displayText = text.isEmpty() ? "(no server messages)" : text;

        Font monoFont = SyntaxStyleUtil.monoFont();
        JEditorPane msgArea = new JEditorPane("text/html",
                PullResultDialog.toHtml(displayText, monoFont.getSize()));
        msgArea.setEditable(false);
        msgArea.setOpaque(true);
        msgArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        msgArea.setFont(monoFont);
        msgArea.addHyperlinkListener(ev -> {
            if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(ev.getURL().toExternalForm()));
                } catch (Exception ex) {
                    // ignore
                }
            }
        });

        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Details"));

        // ---- buttons ----
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        // ---- layout ----
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scroll,      BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }

    /**
     * Workspace variant: shows one section per pushed repository with its remote URL and
     * the server messages (or the error, for repositories that failed).
     */
    public PushResultDialog(Component parent, List<ProjectOperationResult<String>> results) {
        super(SwingUtilities.getWindowAncestor(parent), "Push Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 460);
        setLocationRelativeTo(parent);

        long ok = results.stream().filter(ProjectOperationResult::isSuccess).count();

        JLabel titleLabel = new JLabel("Push completed for " + ok + " of "
                + results.size() + " repositories");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        String displayText = results.isEmpty()
                ? "No repositories with unpushed changes."
                : buildWorkspaceReport(results);

        Font monoFont = SyntaxStyleUtil.monoFont();
        JEditorPane msgArea = new JEditorPane("text/html",
                PullResultDialog.toHtml(displayText, monoFont.getSize()));
        msgArea.setEditable(false);
        msgArea.setOpaque(true);
        msgArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        msgArea.setFont(monoFont);
        msgArea.setCaretPosition(0);
        msgArea.addHyperlinkListener(ev -> {
            if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(ev.getURL().toExternalForm()));
                } catch (Exception ex) {
                    // ignore
                }
            }
        });

        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Details"));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scroll,      BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }

    private static String buildWorkspaceReport(List<ProjectOperationResult<String>> results) {
        StringBuilder sb = new StringBuilder();
        for (ProjectOperationResult<String> r : results) {
            sb.append("=== ").append(r.getProjectName()).append(" ===\n");
            if (r.getRemoteUrl() != null && !r.getRemoteUrl().isEmpty()) {
                sb.append(r.getRemoteUrl()).append('\n');
            }
            if (r.isSuccess()) {
                String msg = r.getResult() != null ? r.getResult().trim() : "";
                sb.append(msg.isEmpty() ? "(no server messages)" : msg);
            } else {
                Exception e = r.getError();
                sb.append("FAILED: ").append(e != null ? e.getMessage() : "unknown error");
            }
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }
}