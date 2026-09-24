package com.az.gitember.dialog;

import com.az.gitember.data.ProjectOperationResult;
import com.az.gitember.data.PullOperationResult;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dialog that shows the result of a pull operation:
 * added / deleted / changed files in a colour-coded table,
 * plus any message returned by the remote server.
 */
public class PullResultDialog extends JDialog {

    // Light-theme pastels
    private static final Color COLOR_ADDED_LIGHT   = new Color(210, 245, 210);
    private static final Color COLOR_DELETED_LIGHT = new Color(250, 210, 210);
    private static final Color COLOR_CHANGED_LIGHT = new Color(210, 225, 250);

    // Dark-theme muted variants
    private static final Color COLOR_ADDED_DARK   = new Color(30, 80, 30);
    private static final Color COLOR_DELETED_DARK = new Color(100, 30, 30);
    private static final Color COLOR_CHANGED_DARK = new Color(25, 50, 100);

    private static boolean isDarkTheme() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) return false;
        int lum = (bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000;
        return lum < 128;
    }

    public PullResultDialog(Component parent, PullOperationResult result) {
        super(SwingUtilities.getWindowAncestor(parent), "Pull Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 500);
        setLocationRelativeTo(parent);

        boolean dark = isDarkTheme();
        Color colorAdded   = dark ? COLOR_ADDED_DARK   : COLOR_ADDED_LIGHT;
        Color colorDeleted = dark ? COLOR_DELETED_DARK : COLOR_DELETED_LIGHT;
        Color colorChanged = dark ? COLOR_CHANGED_DARK : COLOR_CHANGED_LIGHT;
        Color fgAdded   = dark ? new Color(180, 240, 180) : Color.BLACK;
        Color fgDeleted = dark ? new Color(240, 160, 160) : Color.BLACK;
        Color fgChanged = dark ? new Color(160, 195, 255) : Color.BLACK;

        // ---- header ----
        JPanel headerPanel = new JPanel(new BorderLayout(6, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        JLabel titleLabel = new JLabel("Pull completed — " + result.getStatus());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        if (result.hasChanges()) {
            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            badges.add(makeBadge("+" + result.getAddedFiles().size()    + "  added",   colorAdded,   fgAdded));
            badges.add(makeBadge("-" + result.getDeletedFiles().size()  + "  deleted", colorDeleted, fgDeleted));
            badges.add(makeBadge("~" + result.getChangedFiles().size()  + "  changed", colorChanged, fgChanged));
            headerPanel.add(badges, BorderLayout.CENTER);
        } else {
            JLabel noChanges = new JLabel("No file changes.");
            noChanges.setForeground(UIManager.getColor("Label.disabledForeground"));
            headerPanel.add(noChanges, BorderLayout.CENTER);
        }

        // ---- file-change table ----
        DefaultTableModel model = new DefaultTableModel(new String[]{"File", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        addRows(model, result.getAddedFiles(),   "Added");
        addRows(model, result.getDeletedFiles(), "Deleted");
        addRows(model, result.getChangedFiles(), "Changed");

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    Color bg, fg;
                    switch ((String) getValueAt(row, 1)) {
                        case "Added"   -> { bg = colorAdded;   fg = fgAdded; }
                        case "Deleted" -> { bg = colorDeleted; fg = fgDeleted; }
                        default        -> { bg = colorChanged; fg = fgChanged; }
                    }
                    c.setBackground(bg);
                    c.setForeground(fg);
                }
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(72);
        table.getColumnModel().getColumn(1).setMaxWidth(90);

        JScrollPane tableScroll = new JScrollPane(table);

        // ---- server messages ----
        String msgs = result.getServerMessages();
        String displayMsgs = msgs.isEmpty() ? "(no server messages)" : msgs;

        JTextPane msgArea = createMessagePane(displayMsgs);

        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(BorderFactory.createTitledBorder("Server messages"));
        msgScroll.setPreferredSize(new Dimension(0, 100));

        // ---- centre split: table + server messages ----
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, msgScroll);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        // ---- buttons ----
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        // ---- layout ----
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane,   BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }

    /**
     * Workspace variant: one row per repository summarising its pull outcome
     * (status and added / deleted / changed counts), with a server-messages / errors
     * area below listing per-repository details.
     */
    public PullResultDialog(Component parent, List<ProjectOperationResult<PullOperationResult>> results) {
        super(SwingUtilities.getWindowAncestor(parent), "Pull Result",
                ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 500);
        setLocationRelativeTo(parent);

        long ok = results.stream().filter(ProjectOperationResult::isSuccess).count();

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        JLabel titleLabel = new JLabel("Pull completed for " + ok + " of "
                + results.size() + " repositories");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // ---- per-repository summary table ----
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Repository", "Status", "Added", "Deleted", "Changed"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (ProjectOperationResult<PullOperationResult> r : results) {
            if (r.isSuccess()) {
                PullOperationResult res = r.getResult();
                model.addRow(new Object[]{
                        r.getProjectName(),
                        res != null ? res.getStatus() : "",
                        res != null ? res.getAddedFiles().size()   : 0,
                        res != null ? res.getDeletedFiles().size() : 0,
                        res != null ? res.getChangedFiles().size() : 0
                });
            } else {
                model.addRow(new Object[]{r.getProjectName(), "Failed", "—", "—", "—"});
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
        JScrollPane tableScroll = new JScrollPane(table);

        // ---- per-repository server messages / errors ----
        String displayMsgs = results.isEmpty()
                ? "No repositories with a remote."
                : buildWorkspaceReport(results);
        JTextPane msgArea = createMessagePane(displayMsgs);
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(BorderFactory.createTitledBorder("Details"));
        msgScroll.setPreferredSize(new Dimension(0, 120));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, msgScroll);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(closeBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane,   BorderLayout.CENTER);
        mainPanel.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(closeBtn);
        Util.bindEscapeToDispose(this);
    }

    private static String buildWorkspaceReport(List<ProjectOperationResult<PullOperationResult>> results) {
        StringBuilder sb = new StringBuilder();
        for (ProjectOperationResult<PullOperationResult> r : results) {
            sb.append("=== ").append(r.getProjectName()).append(" ===\n");
            if (r.getRemoteUrl() != null && !r.getRemoteUrl().isEmpty()) {
                sb.append(r.getRemoteUrl()).append('\n');
            }
            if (r.isSuccess()) {
                PullOperationResult res = r.getResult();
                String msgs = res != null ? res.getServerMessages() : "";
                sb.append(res != null ? res.getStatus() : "").append('\n');
                if (msgs != null && !msgs.isEmpty()) {
                    sb.append(msgs);
                }
            } else {
                Exception e = r.getError();
                sb.append("FAILED: ").append(e != null ? e.getMessage() : "unknown error");
            }
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    private static void addRows(DefaultTableModel model, List<String> files, String status) {
        for (String f : files) model.addRow(new Object[]{f, status});
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    /**
     * Styled text pane for remote server messages. Avoids JEditorPane HTML,
     * which can paint an empty document (charset reload) or invisible text
     * on a dark theme. Uses the UI font so emoji render, and styles URLs
     * as clickable links.
     */
    static JTextPane createMessagePane(String text) {
        Font uiFont = UIManager.getFont("Label.font");
        if (uiFont == null) {
            uiFont = new Font(Font.DIALOG, Font.PLAIN, 13);
        }
        Color fg = UIManager.getColor("TextArea.foreground");
        Color bg = UIManager.getColor("TextArea.background");
        Color link = UIManager.getColor("Component.linkColor");
        if (fg == null) {
            fg = UIManager.getColor("Label.foreground");
        }
        if (bg == null) {
            bg = UIManager.getColor("Panel.background");
        }
        if (link == null) {
            link = new Color(0x5B9BD5);
        }

        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setOpaque(true);
        pane.setFont(uiFont);
        if (fg != null) {
            pane.setForeground(fg);
        }
        if (bg != null) {
            pane.setBackground(bg);
        }
        pane.setCaretColor(fg != null ? fg : pane.getForeground());
        pane.getCaret().setVisible(false);

        String display = sanitizeRemoteText(text != null ? text : "");
        fillStyledMessage(pane.getStyledDocument(), display, fg, link);
        pane.setCaretPosition(0);

        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLinkAt(pane, e.getPoint());
            }
        });
        pane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (urlAt(pane, e.getPoint()) != null) {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    pane.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        return pane;
    }

    private static void fillStyledMessage(StyledDocument doc, String display, Color fg, Color link) {
        SimpleAttributeSet plain = new SimpleAttributeSet();
        if (fg != null) {
            StyleConstants.setForeground(plain, fg);
        }
        SimpleAttributeSet linkAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(linkAttr, link);
        StyleConstants.setUnderline(linkAttr, true);
        Matcher m = URL_PATTERN.matcher(display);
        int last = 0;
        try {
            while (m.find()) {
                if (m.start() > last) {
                    doc.insertString(doc.getLength(), display.substring(last, m.start()), plain);
                }
                SimpleAttributeSet href = new SimpleAttributeSet(linkAttr);
                href.addAttribute("url", m.group());
                doc.insertString(doc.getLength(), m.group(), href);
                last = m.end();
            }
            if (last < display.length()) {
                doc.insertString(doc.getLength(), display.substring(last), plain);
            }
        } catch (BadLocationException ignored) {
        }
    }

    private static void openLinkAt(JTextPane pane, Point point) {
        String url = urlAt(pane, point);
        if (url != null) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) {
            }
        }
    }

    private static String urlAt(JTextPane pane, Point point) {
        String url = null;
        int pos = pane.viewToModel2D(point);
        if (pos >= 0) {
            AttributeSet as = pane.getStyledDocument().getCharacterElement(pos).getAttributes();
            Object value = as.getAttribute("url");
            if (value instanceof String s) {
                url = s;
            }
        }
        return url;
    }

    /**
     * Collapses git progress {@code \\r} overwrites and strips ANSI so leftover
     * control characters do not show up as empty boxes.
     */
    static String sanitizeRemoteText(String text) {
        String cleaned = "";
        if (text != null) {
            cleaned = text.replace("\r\n", "\n");
            cleaned = cleaned.replaceAll("\\u001B\\[[0-9;?]*[ -/]*[@-~]", "");
            cleaned = cleaned.replaceAll("\\u001B[@-Z\\\\-_]", "");
            StringBuilder out = new StringBuilder(cleaned.length());
            String[] lines = cleaned.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.indexOf('\r') >= 0) {
                    String[] parts = line.split("\r", -1);
                    String last = "";
                    for (String part : parts) {
                        if (!part.isEmpty()) {
                            last = part;
                        }
                    }
                    line = last;
                }
                if (i > 0) {
                    out.append('\n');
                }
                out.append(line);
            }
            cleaned = out.toString();
        }
        return cleaned;
    }

    static String toHtml(String text, int fontSize) {
        String escaped = encodeHtml(sanitizeRemoteText(text));

        Matcher m = URL_PATTERN.matcher(escaped);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String url = m.group();
            m.appendReplacement(sb, Matcher.quoteReplacement("<a href='" + url + "'>" + url + "</a>"));
        }
        m.appendTail(sb);

        int size = fontSize > 2 ? fontSize : 12;
        String body = sb.toString().replace("\n", "<br>");
        return "<html><body style='font-family:Dialog,SansSerif;font-size:" + size + "px'>"
                + body + "</body></html>";
    }

    /**
     * Escapes HTML and turns characters above ASCII into numeric entities so
     * JEditorPane's HTML parser (ISO-8859-1 by default) does not drop emoji.
     */
    static String encodeHtml(String text) {
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            if (cp == '&') {
                out.append("&amp;");
            } else if (cp == '<') {
                out.append("&lt;");
            } else if (cp == '>') {
                out.append("&gt;");
            } else if (cp == '\n' || cp == '\t') {
                out.appendCodePoint(cp);
            } else if (cp < 32) {
                // drop other control characters (CR already handled)
            } else if (cp > 127) {
                out.append("&#").append(cp).append(';');
            } else {
                out.append((char) cp);
            }
        });
        return out.toString();
    }

    private static JLabel makeBadge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        return lbl;
    }
}
