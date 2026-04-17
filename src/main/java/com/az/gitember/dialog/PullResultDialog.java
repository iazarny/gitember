package com.az.gitember.dialog;

import com.az.gitember.data.PullOperationResult;
import com.az.gitember.ui.SyntaxStyleUtil;
import com.az.gitember.ui.misc.Util;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

        Font monoFont = SyntaxStyleUtil.monoFont();
        JEditorPane msgArea = new JEditorPane("text/html", toHtml(displayMsgs, monoFont.getSize()));
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

    private static void addRows(DefaultTableModel model, List<String> files, String status) {
        for (String f : files) model.addRow(new Object[]{f, status});
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    static String toHtml(String text, int fontSize) {
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        Matcher m = URL_PATTERN.matcher(escaped);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String url = m.group();
            m.appendReplacement(sb, "<a href='" + url + "'>" + url + "</a>");
        }
        m.appendTail(sb);

        String body = sb.toString().replace("\n", "<br>");
        return "<html><body style='font-family:monospaced;font-size:" + (fontSize-2) + "px'>" + body + "</body></html>";
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
