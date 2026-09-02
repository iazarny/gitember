package com.az.gitember.ui;

import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import com.az.gitember.ui.misc.Util;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Side-by-side image comparison window. Mirrors {@link DiffViewerWindowTxt}
 * constructors, but shows two scaled images instead of a text diff.
 */
public class DiffViewerWindowImg extends JFrame {

    private static final Logger log = Logger.getLogger(DiffViewerWindowImg.class.getName());

    private static final String[] IMAGE_EXTENSIONS = {
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "tiff", "tif"
    };

    private final String fileName;
    private final JComboBox<ScmRevisionInformation> oldCombo;
    private final JComboBox<ScmRevisionInformation> newCombo;

    private FitImagePanel leftPane;
    private FitImagePanel rightPane;
    private JScrollPane leftScroll;
    private JScrollPane rightScroll;
    private JLabel diffInfoLabel;
    private JButton fitBtn;
    private JButton actualBtn;
    private JButton zoomInBtn;
    private JButton zoomOutBtn;

    private boolean fit = true;
    private double zoom = 1.0;
    private boolean syncingScroll = false;

    // ---- Constructors ----

    public DiffViewerWindowImg(String fileName, List<ScmRevisionInformation> fileRevisions,
                               String oldSha, String newSha) {
        this.fileName = fileName;
        setTitle("Diff: " + fileName);
        initCommon();

        oldCombo = new JComboBox<>();
        newCombo = new JComboBox<>();
        applyRevisionRenderer(oldCombo);
        applyRevisionRenderer(newCombo);
        if (fileRevisions != null) {
            for (ScmRevisionInformation rev : fileRevisions) {
                oldCombo.addItem(rev);
                newCombo.addItem(rev);
            }
        }
        selectRevision(oldCombo, oldSha);
        selectRevision(newCombo, newSha);
        oldCombo.addActionListener(e -> loadFromRevisions());
        newCombo.addActionListener(e -> loadFromRevisions());

        JPanel combosRow = new JPanel(new GridBagLayout());
        combosRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 3, 0, 3);

        g.gridx = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        combosRow.add(new JLabel("Old:"), g);
        g.gridx = 1;
        g.weightx = 0.45;
        g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(oldCombo, g);
        g.gridx = 2;
        g.weightx = 0.10;
        g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(new JLabel("New:"), g);
        g.gridx = 3;
        g.weightx = 0.45;
        g.fill = GridBagConstraints.HORIZONTAL;
        combosRow.add(newCombo, g);

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.add(combosRow);
        toolbar.add(buildZoomPanel());

        setupContentPane(toolbar,
                headerPanel(" Old revision", leftScroll),
                headerPanel(" New revision", rightScroll));

        loadFromRevisions();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Two labeled images already decoded. No revision combos.
     */
    public DiffViewerWindowImg(String fileName,
                               String leftLabel, BufferedImage leftImage,
                               String rightLabel, BufferedImage rightImage) {
        this.fileName = fileName;
        setTitle("Diff: " + fileName + " (" + leftLabel + " / " + rightLabel + ")");
        initCommon();
        oldCombo = null;
        newCombo = null;

        setupContentPane(buildZoomPanel(),
                headerPanel(" " + leftLabel, leftScroll),
                headerPanel(" " + rightLabel, rightScroll));

        setImages(leftImage, rightImage);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Two labeled image files loaded from disk. No revision combos.
     */
    public DiffViewerWindowImg(String fileName,
                               String leftLabel, File leftFile,
                               String rightLabel, File rightFile) {
        this.fileName = fileName;
        setTitle("Diff: " + fileName + " (" + leftLabel + " / " + rightLabel + ")");
        initCommon();
        oldCombo = null;
        newCombo = null;

        setupContentPane(buildZoomPanel(),
                headerPanel(" " + leftLabel, leftScroll),
                headerPanel(" " + rightLabel, rightScroll));

        loadFromFiles(leftFile, rightFile);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Commit image versus working-copy file. No revision combos.
     */
    public DiffViewerWindowImg(String fileName, String commitSha,
                               File commitFile, File diskFile) {
        this.fileName = fileName;
        setTitle("Diff with disk: " + fileName);
        initCommon();
        oldCombo = null;
        newCombo = null;

        String shortSha = commitSha != null && commitSha.length() > 8
                ? commitSha.substring(0, 8) : (commitSha != null ? commitSha : "");
        setupContentPane(buildZoomPanel(),
                headerPanel(" Commit: " + shortSha, leftScroll),
                headerPanel(" Working directory", rightScroll));

        loadFromFiles(commitFile, diskFile);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Empty compare window: browse or drop an image onto each side.
     */
    public DiffViewerWindowImg() {
        this.fileName = "";
        setTitle("Compare images");
        initCommon();
        oldCombo = null;
        newCombo = null;

        JTextField leftPathField = pathField();
        JTextField rightPathField = pathField();

        setupDropOnField(leftPathField, leftPane, leftPathField);
        setupDropOnField(rightPathField, rightPane, rightPathField);
        setupDropOnPanel(leftPane, leftPathField);
        setupDropOnPanel(rightPane, rightPathField);

        JButton browseLeft = new JButton("Browse…");
        JButton browseRight = new JButton("Browse…");
        browseLeft.addActionListener(e -> browseAndLoad(leftPathField, leftPane));
        browseRight.addActionListener(e -> browseAndLoad(rightPathField, rightPane));

        setupContentPane(
                buildZoomPanel(),
                buildEditableHeader(leftPathField, browseLeft, leftScroll),
                buildEditableHeader(rightPathField, browseRight, rightScroll));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            toFront();
            requestFocus();
        }
    }

    private void initCommon() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width - 60, screenSize.height - 80);
        setLocationRelativeTo(MainFrame.getInstance());
        setIconImages(Util.appIcons());
        setName("imageDiffWindow");

        leftPane = new FitImagePanel();
        rightPane = new FitImagePanel();
        leftPane.setName("leftImagePane");
        rightPane.setName("rightImagePane");
        leftPane.setPlaceholder("No image");
        rightPane.setPlaceholder("No image");

        leftScroll = wrap(leftPane);
        rightScroll = wrap(rightPane);
        leftScroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (fit) {
                    leftPane.revalidate();
                    rightPane.revalidate();
                }
            }
        });
        syncScroll(leftScroll, rightScroll);

        fitBtn = Util.createButton("Fit", "Fit both images to the pane", FontAwesomeSolid.EXPAND);
        fitBtn.setName("fitImageButton");
        fitBtn.addActionListener(e -> applyFit());

        actualBtn = Util.createButton("100%", "Actual size", FontAwesomeSolid.SEARCH);
        actualBtn.setName("actualSizeButton");
        actualBtn.addActionListener(e -> applyActualSize());

        zoomInBtn = Util.createButton("Zoom in", "Zoom in", FontAwesomeSolid.SEARCH_PLUS);
        zoomInBtn.setName("zoomInButton");
        zoomInBtn.addActionListener(e -> applyZoomBy(1.25));

        zoomOutBtn = Util.createButton("Zoom out", "Zoom out", FontAwesomeSolid.SEARCH_MINUS);
        zoomOutBtn.setName("zoomOutButton");
        zoomOutBtn.addActionListener(e -> applyZoomBy(1.0 / 1.25));

        diffInfoLabel = new JLabel("");
        diffInfoLabel.setName("imageDiffInfoLabel");
    }

    private static JScrollPane wrap(FitImagePanel pane) {
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getViewport().setBackground(UIManager.getColor("Panel.background"));
        return scroll;
    }

    private JPanel buildZoomPanel() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        nav.add(fitBtn);
        nav.add(actualBtn);
        nav.add(zoomOutBtn);
        nav.add(zoomInBtn);
        nav.add(Box.createHorizontalStrut(10));
        nav.add(diffInfoLabel);
        return nav;
    }

    private static JPanel headerPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void setupContentPane(JPanel toolbar, JPanel leftPanel, JPanel rightPanel) {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(buildImagePanel(leftPanel, rightPanel), BorderLayout.CENTER);
        Util.bindEscapeToDispose(this);
    }

    private JPanel buildImagePanel(JPanel leftPanel, JPanel rightPanel) {
        Dimension zero = new Dimension(0, 0);
        leftPanel.setPreferredSize(zero);
        rightPanel.setPreferredSize(zero);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        panel.add(leftPanel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(rightPanel, gbc);
        return panel;
    }

    private static JTextField pathField() {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", "Drop an image here, or click Browse…");
        f.setEditable(false);
        return f;
    }

    private static JPanel buildEditableHeader(JTextField pathField, JButton browseBtn,
                                              JComponent scroll) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        row.add(browseBtn, BorderLayout.WEST);
        row.add(pathField, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(row, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void setupDropOnField(JTextField field, FitImagePanel target, JTextField pathField) {
        Border orig = field.getBorder();
        new DropTarget(field, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent d) {
                if (d.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    d.acceptDrag(DnDConstants.ACTION_COPY);
                    field.setBorder(BorderFactory.createLineBorder(new Color(0x4488FF), 2));
                } else {
                    d.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent d) {
                field.setBorder(orig);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent d) {
                field.setBorder(orig);
                d.acceptDrop(DnDConstants.ACTION_COPY);
                boolean complete = false;
                try {
                    List<File> files = (List<File>)
                            d.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && files.get(0).isFile()) {
                        loadFileIntoPane(files.get(0), pathField, target);
                        complete = true;
                    }
                } catch (Exception ignored) {
                }
                d.dropComplete(complete);
            }
        }, true);
    }

    private void setupDropOnPanel(FitImagePanel pane, JTextField pathField) {
        new DropTarget(pane, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent d) {
                if (d.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    d.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    d.rejectDrag();
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent d) {
                d.acceptDrop(DnDConstants.ACTION_COPY);
                boolean complete = false;
                try {
                    List<File> files = (List<File>)
                            d.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && files.get(0).isFile()) {
                        loadFileIntoPane(files.get(0), pathField, pane);
                        complete = true;
                    }
                } catch (Exception ignored) {
                }
                d.dropComplete(complete);
            }
        }, true);
    }

    private void browseAndLoad(JTextField pathField, FitImagePanel pane) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select image to compare");
        fc.setFileFilter(new FileNameExtensionFilter("Images", IMAGE_EXTENSIONS));
        if (!pathField.getText().isBlank()) {
            File cur = new File(pathField.getText().trim());
            fc.setCurrentDirectory(cur.isDirectory() ? cur : cur.getParentFile());
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFileIntoPane(fc.getSelectedFile(), pathField, pane);
        }
    }

    private void loadFileIntoPane(File f, JTextField pathField, FitImagePanel pane) {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return readImage(f);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    pathField.setText(f.getAbsolutePath());
                    pane.setPlaceholder("Cannot decode image");
                    pane.setImage(image);
                    applyCurrentZoom();
                    updateInfo();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load image for comparison", ex);
                    JOptionPane.showMessageDialog(DiffViewerWindowImg.this,
                            "Cannot read image: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void syncScroll(JScrollPane left, JScrollPane right) {
        left.getVerticalScrollBar().addAdjustmentListener(e -> copyScroll(left, right, true, e.getValueIsAdjusting()));
        right.getVerticalScrollBar().addAdjustmentListener(e -> copyScroll(right, left, true, e.getValueIsAdjusting()));
        left.getHorizontalScrollBar().addAdjustmentListener(e -> copyScroll(left, right, false, e.getValueIsAdjusting()));
        right.getHorizontalScrollBar().addAdjustmentListener(e -> copyScroll(right, left, false, e.getValueIsAdjusting()));
    }

    private void copyScroll(JScrollPane from, JScrollPane to, boolean vertical, boolean adjusting) {
        if (!adjusting && !syncingScroll) {
            syncingScroll = true;
            JScrollBar src = vertical ? from.getVerticalScrollBar() : from.getHorizontalScrollBar();
            JScrollBar dst = vertical ? to.getVerticalScrollBar() : to.getHorizontalScrollBar();
            dst.setValue(src.getValue());
            syncingScroll = false;
        }
    }

    private void applyRevisionRenderer(JComboBox<ScmRevisionInformation> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScmRevisionInformation rev) {
                    setText(formatRevision(rev));
                }
                return c;
            }
        });
    }

    private static String formatRevision(ScmRevisionInformation revision) {
        String sha = revision.getRevisionFullName();
        String shortSha = sha != null && sha.length() > 8 ? sha.substring(0, 8) : (sha != null ? sha : "");
        String msg = revision.getShortMessage();
        if (msg != null && msg.length() > 50) {
            msg = msg.substring(0, 50) + "...";
        }
        String date = revision.getDate() != null ? GitemberUtil.formatDate(revision.getDate()) : "";
        return shortSha + " " + date + " " + (msg != null ? msg : "");
    }

    private void selectRevision(JComboBox<ScmRevisionInformation> combo, String sha) {
        if (sha != null && combo != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                ScmRevisionInformation item = combo.getItemAt(i);
                if (item != null && sha.equals(item.getRevisionFullName())) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadFromRevisions() {
        if (oldCombo != null && newCombo != null) {
            ScmRevisionInformation oldRev = (ScmRevisionInformation) oldCombo.getSelectedItem();
            ScmRevisionInformation newRev = (ScmRevisionInformation) newCombo.getSelectedItem();
            if (oldRev != null && newRev != null) {
                String oldSha = oldRev.getRevisionFullName();
                String newSha = newRev.getRevisionFullName();
                SwingWorker<BufferedImage[], Void> worker = new SwingWorker<>() {
                    @Override
                    protected BufferedImage[] doInBackground() {
                        return new BufferedImage[]{
                                loadFromCommit(oldSha),
                                loadFromCommit(newSha)
                        };
                    }

                    @Override
                    protected void done() {
                        try {
                            BufferedImage[] images = get();
                            setImages(images[0], images[1]);
                        } catch (Exception ex) {
                            log.log(Level.WARNING, "Failed to load image revisions", ex);
                            JOptionPane.showMessageDialog(DiffViewerWindowImg.this,
                                    "Cannot load images: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }

    private void loadFromFiles(File leftFile, File rightFile) {
        SwingWorker<BufferedImage[], Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage[] doInBackground() {
                return new BufferedImage[]{
                        loadFromFile(leftFile),
                        loadFromFile(rightFile)
                };
            }

            @Override
            protected void done() {
                try {
                    BufferedImage[] images = get();
                    setImages(images[0], images[1]);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load images", ex);
                    JOptionPane.showMessageDialog(DiffViewerWindowImg.this,
                            "Cannot load images: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private BufferedImage loadFromCommit(String sha) {
        BufferedImage image = null;
        try {
            String path = Context.getGitRepoService().saveFile(sha, fileName);
            image = readImage(new File(path));
        } catch (Exception ex) {
            log.log(Level.FINE, "No image at " + sha + " for " + fileName, ex);
        }
        return image;
    }

    private static BufferedImage loadFromFile(File file) {
        BufferedImage image = null;
        if (file != null && file.isFile()) {
            try {
                image = readImage(file);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot decode " + file, ex);
            }
        }
        return image;
    }

    static BufferedImage readImage(File file) throws Exception {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new Exception("Unsupported or corrupt image: " + file.getName());
        }
        return image;
    }

    private void setImages(BufferedImage left, BufferedImage right) {
        leftPane.setPlaceholder(left == null ? "No image" : "Cannot decode image");
        rightPane.setPlaceholder(right == null ? "No image" : "Cannot decode image");
        leftPane.setImage(left);
        rightPane.setImage(right);
        applyCurrentZoom();
        updateInfo();
    }

    private void applyFit() {
        fit = true;
        applyCurrentZoom();
    }

    private void applyActualSize() {
        fit = false;
        zoom = 1.0;
        applyCurrentZoom();
    }

    private void applyZoomBy(double factor) {
        if (fit) {
            zoom = currentFitScale();
            fit = false;
        }
        zoom = Math.max(0.05, Math.min(16.0, zoom * factor));
        applyCurrentZoom();
    }

    private double currentFitScale() {
        Dimension leftExt = leftScroll.getViewport().getExtentSize();
        Dimension rightExt = rightScroll.getViewport().getExtentSize();
        double leftScale = leftPane.fitScaleFor(leftExt.width, leftExt.height);
        double rightScale = rightPane.fitScaleFor(rightExt.width, rightExt.height);
        return Math.min(leftScale, rightScale);
    }

    private void applyCurrentZoom() {
        leftPane.setFit(fit);
        rightPane.setFit(fit);
        if (!fit) {
            leftPane.setZoom(zoom);
            rightPane.setZoom(zoom);
        }
        int barPolicy = fit ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        int vPolicy = fit ? ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
                : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
        leftScroll.setHorizontalScrollBarPolicy(barPolicy);
        rightScroll.setHorizontalScrollBarPolicy(barPolicy);
        leftScroll.setVerticalScrollBarPolicy(vPolicy);
        rightScroll.setVerticalScrollBarPolicy(vPolicy);
        leftPane.revalidate();
        rightPane.revalidate();
        leftScroll.revalidate();
        rightScroll.revalidate();
        repaint();
    }

    private void updateInfo() {
        diffInfoLabel.setText(describeComparison(leftPane.getImage(), rightPane.getImage()));
    }

    static String describeComparison(BufferedImage left, BufferedImage right) {
        String verdict;
        if (left == null && right == null) {
            verdict = "No images";
        } else if (left == null || right == null) {
            verdict = "Missing on one side";
        } else if (left.getWidth() != right.getWidth() || left.getHeight() != right.getHeight()) {
            verdict = "Different size";
        } else if (pixelsEqual(left, right)) {
            verdict = "Identical";
        } else {
            verdict = "Different";
        }
        return "Left: " + describe(left) + "    Right: " + describe(right) + "    " + verdict;
    }

    private static String describe(BufferedImage image) {
        String text = "(none)";
        if (image != null) {
            text = image.getWidth() + "\u00d7" + image.getHeight();
        }
        return text;
    }

    static boolean pixelsEqual(BufferedImage a, BufferedImage b) {
        boolean equal = a != null && b != null
                && a.getWidth() == b.getWidth()
                && a.getHeight() == b.getHeight();
        if (equal) {
            int w = a.getWidth();
            int h = a.getHeight();
            for (int y = 0; y < h && equal; y++) {
                for (int x = 0; x < w && equal; x++) {
                    if (a.getRGB(x, y) != b.getRGB(x, y)) {
                        equal = false;
                    }
                }
            }
        }
        return equal;
    }
}
