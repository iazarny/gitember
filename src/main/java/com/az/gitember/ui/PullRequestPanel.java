package com.az.gitember.ui;

import com.az.gitember.data.Project;
import com.az.gitember.data.PullRequest;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.avatar.AvatarService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel shown when a Pull Request node is selected in the tree.
 * Displays PR metadata (with author avatar) and the list of files changed
 * in the PR, colour-coded by status (added / deleted / changed),
 * with a live file-name filter.
 */
public class PullRequestPanel extends JPanel {

    private static final Logger log = Logger.getLogger(PullRequestPanel.class.getName());
    private static final int AVATAR_SIZE = 48;
    private static final Map<String, ImageIcon> avatarIconCache = new HashMap<>();

    // Header fields
    private final JTextField titleField  = readOnlyField();
    private final JTextField authorField = readOnlyField();
    private final JTextField stateField  = readOnlyField();
    private final JTextField branchField = readOnlyField();
    private final JButton    openUrlBtn  = new JButton("Open in Browser");
    private final JLabel     avatarLabel = new JLabel();

    // Files area
    private final FilesTableModel filesModel  = new FilesTableModel();
    private final JTable          filesTable;
    private final JPanel          badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JLabel          statusLabel = new JLabel();
    private final JTextField      searchField = new JTextField(15);

    private PullRequest currentPr;

    public PullRequestPanel() {
        filesTable = buildFilesTable();

        searchField.setPreferredSize(new Dimension(180, 25));
        searchField.setMinimumSize(new Dimension(100, 25));
        searchField.setMaximumSize(new Dimension(220, 25));
        searchField.putClientProperty("JTextField.placeholderText", "Filter files…");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        setLayout(new BorderLayout());
        add(buildHeader(),     BorderLayout.NORTH);
        add(buildFilesPanel(), BorderLayout.CENTER);
    }

    public JTextField getSearchField() { return searchField; }

    // ---- table construction ----

    private JTable buildFilesTable() {
        JTable table = new JTable(filesModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String cat = filesModel.categoryAt(row);
                    c.setBackground(colorFor(cat));
                    c.setForeground(fgFor(cat));
                }
                return c;
            }
        };

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status column: narrow + centred
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(72);
        table.getColumnModel().getColumn(1).setMaxWidth(90);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) openFileDiff(filesModel.getItemAt(row));
                }
            }
        });

        return table;
    }

    // ---- panel layout ----

    private JPanel buildFilesPanel() {
        JLabel heading = new JLabel("  Changed files");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        heading.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel headRow = new JPanel(new BorderLayout(4, 0));
        headRow.add(heading,     BorderLayout.WEST);
        headRow.add(statusLabel, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.add(headRow,     BorderLayout.NORTH);
        top.add(badgesPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top,                        BorderLayout.NORTH);
        panel.add(new JScrollPane(filesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHeader() {
        // ── Avatar (left) ─────────────────────────────────────────────────
        avatarLabel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMinimumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Separator.foreground"), 1));
        avatarLabel.setIcon(placeholderIcon("", AVATAR_SIZE));

        // ── Form fields ───────────────────────────────────────────────────
        JPanel fields = new JPanel(new GridBagLayout());

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(2, 2, 2, 6);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(2, 0, 2, 10);

        // Row 0: Title (spans all columns)
        lc.gridx = 0; lc.gridy = 0;
        fields.add(new JLabel("Title:"), lc);
        fc.gridx = 1; fc.gridy = 0; fc.gridwidth = 5;
        fields.add(titleField, fc);
        fc.gridwidth = 1;

        // Row 1: Author | State | Branches
        lc.gridx = 0; lc.gridy = 1; fields.add(new JLabel("Author:"),   lc);
        fc.gridx = 1; fc.gridy = 1; fields.add(authorField, fc);
        lc.gridx = 2; lc.gridy = 1; fields.add(new JLabel("State:"),    lc);
        fc.gridx = 3; fc.gridy = 1; fields.add(stateField,  fc);
        lc.gridx = 4; lc.gridy = 1; fields.add(new JLabel("Branches:"), lc);
        fc.gridx = 5; fc.gridy = 1; fields.add(branchField, fc);

        // Row 2: Open button
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 2; bc.gridwidth = 6;
        bc.anchor = GridBagConstraints.WEST;
        bc.insets = new Insets(4, 0, 2, 0);
        openUrlBtn.setEnabled(false);
        openUrlBtn.addActionListener(e -> openInBrowser());
        fields.add(openUrlBtn, bc);

        // ── Wrapper: avatar west, fields centre ───────────────────────────
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, getBackground().darker()),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panel.add(avatarLabel, BorderLayout.WEST);
        panel.add(fields,      BorderLayout.CENTER);
        return panel;
    }

    // ---- public API ----

    public void showPullRequest(PullRequest pr) {
        this.currentPr = pr;
        filesModel.clear();
        badgesPanel.removeAll();
        badgesPanel.revalidate();
        badgesPanel.repaint();
        searchField.setText("");

        if (pr == null) {
            titleField.setText("");
            authorField.setText("");
            stateField.setText("");
            branchField.setText("");
            statusLabel.setText("");
            openUrlBtn.setEnabled(false);
            avatarLabel.setIcon(placeholderIcon("", AVATAR_SIZE));
            return;
        }

        titleField.setText("#" + pr.number() + "  " + pr.title());
        authorField.setText(pr.author()      != null ? pr.author()      : "");
        stateField.setText(pr.state()        != null ? pr.state()       : "");
        branchField.setText(pr.sourceBranch() + "  →  " + pr.targetBranch());
        openUrlBtn.setEnabled(pr.webUrl() != null && !pr.webUrl().isBlank());
        statusLabel.setText("Loading…");

        // Show initials placeholder, then fetch real avatar
        avatarLabel.setIcon(placeholderIcon(pr.author() != null ? pr.author() : "", AVATAR_SIZE));
        fetchAvatarAsync(null, pr.author());

        loadChangedFiles(pr);
    }

    // ---- avatar ----

    private void fetchAvatarAsync(String email, String authorName) {
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                try {
                    String remoteUrl = Context.getGitRepoService() != null
                            ? Context.getGitRepoService().getRepositoryRemoteUrl() : null;
                    String token = Context.getCurrentProject()
                            .map(Project::getAccessToken).orElse(null);
                    return AvatarService.getAvatar(email, authorName, remoteUrl, token);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        Image scaled = img.getScaledInstance(AVATAR_SIZE, AVATAR_SIZE,
                                Image.SCALE_SMOOTH);
                        avatarLabel.setIcon(new ImageIcon(scaled));
                    }
                } catch (Exception ignored) {
                    avatarLabel.setIcon(placeholderIcon("", AVATAR_SIZE));
                }
            }
        }.execute();
    }

    private static ImageIcon placeholderIcon(String name, int size) {
        return avatarIconCache.computeIfAbsent(name, new Function<String, ImageIcon>() {
            @Override
            public ImageIcon apply(String n) {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float hue = n != null && !n.isBlank()
                        ? (Math.abs(n.hashCode()) % 360) / 360.0f : 0.55f;
                g2.setColor(Color.getHSBColor(hue, 0.45f, 0.65f));
                g2.fillOval(0, 0, size - 1, size - 1);
                String initials = initials(n);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 3));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth(initials)) / 2;
                int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, tx, ty);
                g2.dispose();
                return new ImageIcon(img);
            }
        });
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        return ("" + name.charAt(0)).toUpperCase();
    }

    // ---- file filter ----

    private void applyFilter() {
        String term = searchField.getText().trim().toLowerCase();
        filesModel.applyFilter(term);
    }

    // ---- background loading ----

    private void loadChangedFiles(PullRequest pr) {
        new SwingWorker<List<ScmItem>, Void>() {
            @Override
            protected List<ScmItem> doInBackground() throws Exception {
                return Context.getGitRepoService()
                        .getPrChangedFiles(pr.sourceBranch(), pr.targetBranch());
            }

            @Override
            protected void done() {
                try {
                    List<ScmItem> items = get();
                    filesModel.setData(items);
                    applyFilter();
                    updateBadges(items);
                    statusLabel.setText("");
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load PR files", ex);
                    statusLabel.setText("Could not load files: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateBadges(List<ScmItem> items) {
        int added = 0, deleted = 0, changed = 0;
        for (ScmItem item : items) {
            switch (filesModel.categoryOf(item)) {
                case "Added"   -> added++;
                case "Deleted" -> deleted++;
                default        -> changed++;
            }
        }

        badgesPanel.removeAll();
        if (added   > 0) badgesPanel.add(makeBadge("+" + added   + "  added",   SyntaxStyleUtil.rowBgAdded(),   SyntaxStyleUtil.rowFgAdded()));
        if (deleted > 0) badgesPanel.add(makeBadge("-" + deleted  + "  deleted", SyntaxStyleUtil.rowBgDeleted(), SyntaxStyleUtil.rowFgDeleted()));
        if (changed > 0) badgesPanel.add(makeBadge("~" + changed  + "  changed", SyntaxStyleUtil.rowBgChanged(), SyntaxStyleUtil.rowFgChanged()));
        if (added == 0 && deleted == 0 && changed == 0) {
            JLabel none = new JLabel("  No file changes.");
            none.setForeground(UIManager.getColor("Label.disabledForeground"));
            badgesPanel.add(none);
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
    }

    // ---- diff viewer ----

    private void openFileDiff(ScmItem item) {
        if (item == null || currentPr == null) return;
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String targetContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.targetBranch(), item.getShortName());
                String sourceContent = Context.getGitRepoService()
                        .getFileContentAtRef(currentPr.sourceBranch(), item.getShortName());
                return new String[]{targetContent, sourceContent};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    DiffViewerWindow w = new DiffViewerWindow(
                            item.getShortName(),
                            currentPr.targetBranch() + " → " + currentPr.sourceBranch(),
                            texts[0], texts[1]);
                    w.setVisible(true);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to show PR file diff", ex);
                    JOptionPane.showMessageDialog(PullRequestPanel.this,
                            "Cannot show diff: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ---- browser ----

    private void openInBrowser() {
        if (currentPr == null || currentPr.webUrl() == null) return;
        try {
            Desktop.getDesktop().browse(new URI(currentPr.webUrl()));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Cannot open browser", ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot open browser: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- helpers ----

    private static Color colorFor(String category) {
        return switch (category) {
            case "Added"   -> SyntaxStyleUtil.rowBgAdded();
            case "Deleted" -> SyntaxStyleUtil.rowBgDeleted();
            default        -> SyntaxStyleUtil.rowBgChanged();
        };
    }

    private static Color fgFor(String category) {
        return switch (category) {
            case "Added"   -> SyntaxStyleUtil.rowFgAdded();
            case "Deleted" -> SyntaxStyleUtil.rowFgDeleted();
            default        -> SyntaxStyleUtil.rowFgChanged();
        };
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

    private static JTextField readOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        return f;
    }

    // ---- table model ----

    private static class FilesTableModel extends AbstractTableModel {
        private static final String[] COLS = {"File", "Status"};
        private List<ScmItem> allItems = new ArrayList<>();
        private List<ScmItem> items    = new ArrayList<>();  // filtered view

        void setData(List<ScmItem> data) {
            allItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
            items    = new ArrayList<>(allItems);
            fireTableDataChanged();
        }

        void clear() {
            allItems = new ArrayList<>();
            items    = new ArrayList<>();
            fireTableDataChanged();
        }

        void applyFilter(String term) {
            if (term == null || term.isBlank()) {
                items = new ArrayList<>(allItems);
            } else {
                items = new ArrayList<>();
                for (ScmItem item : allItems) {
                    String name = item.getViewRepresentation();
                    if (name != null && name.toLowerCase().contains(term)) {
                        items.add(item);
                    }
                }
            }
            fireTableDataChanged();
        }

        ScmItem getItemAt(int row) {
            return row >= 0 && row < items.size() ? items.get(row) : null;
        }

        String categoryAt(int row) {
            return row >= 0 && row < items.size() ? categoryOf(items.get(row)) : "Changed";
        }

        String categoryOf(ScmItem item) {
            String status = item.getAttribute() != null ? item.getAttribute().getStatus() : "";
            if (ScmItem.Status.ADDED.equals(status))   return "Added";
            if (ScmItem.Status.REMOVED.equals(status)) return "Deleted";
            return "Changed";
        }

        @Override public int    getRowCount()              { return items.size(); }
        @Override public int    getColumnCount()           { return COLS.length; }
        @Override public String getColumnName(int col)     { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ScmItem item = items.get(row);
            return switch (col) {
                case 0 -> item.getViewRepresentation();
                case 1 -> categoryOf(item);
                default -> "";
            };
        }
    }
}
