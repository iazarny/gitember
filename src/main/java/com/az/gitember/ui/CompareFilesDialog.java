package com.az.gitember.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modal dialog for comparing any two files from the file system.
 * Each path field accepts drag-and-drop of a single file.
 * On OK a {@link DiffViewerWindow} is opened with the two files' contents.
 */
public class CompareFilesDialog extends JDialog {

    private static final Logger log = Logger.getLogger(CompareFilesDialog.class.getName());

    private final JTextField leftField  = new JTextField();
    private final JTextField rightField = new JTextField();
    private final JButton    okBtn      = new JButton("Compare");

    public CompareFilesDialog(Frame owner) {
        super(owner, "Compare Files", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        GridBagConstraints lc = gbc(0, 0, 0);
        GridBagConstraints fc = gbc(1, 0, 1.0);
        GridBagConstraints bc = gbc(2, 0, 0);

        // Left row
        lc.gridy = 0; fc.gridy = 0; bc.gridy = 0;
        content.add(new JLabel("Left file:"), lc);
        content.add(leftField,                fc);
        JButton browseLeft = new JButton("Browse…");
        browseLeft.addActionListener(e -> browse(leftField, false));
        content.add(browseLeft, bc);

        // Right row
        lc.gridy = 1; fc.gridy = 1; bc.gridy = 1;
        content.add(new JLabel("Right file:"), lc);
        content.add(rightField,                fc);
        JButton browseRight = new JButton("Browse…");
        browseRight.addActionListener(e -> browse(rightField, false));
        content.add(browseRight, bc);

        // Swap button (centered, between the two rows)
        JButton swapBtn = new JButton("⇅ Swap");
        swapBtn.addActionListener(e -> {
            String tmp = leftField.getText();
            leftField.setText(rightField.getText());
            rightField.setText(tmp);
            validateOk();
        });
        GridBagConstraints sc = gbc(1, 2, 0);
        sc.anchor = GridBagConstraints.CENTER;
        content.add(swapBtn, sc);

        // Button row
        okBtn.setEnabled(false);
        okBtn.addActionListener(e -> openDiff());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okBtn);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        getContentPane().setLayout(new BorderLayout(0, 6));
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(btnRow, BorderLayout.SOUTH);

        // Wire up DnD and change listeners
        attachFileDrop(leftField,  false);
        attachFileDrop(rightField, false);
        leftField.getDocument() .addDocumentListener(docListener());
        rightField.getDocument().addDocumentListener(docListener());

        leftField.setPreferredSize(new Dimension(420, leftField.getPreferredSize().height));
        pack();
        setLocationRelativeTo(owner);
    }

    // ── Actions ─────────────────────────────────────────────────────────────

    private void browse(JTextField target, boolean dirsOnly) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select file");
        fc.setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        if (!target.getText().isBlank()) {
            File cur = new File(target.getText().trim());
            fc.setCurrentDirectory(cur.isDirectory() ? cur : cur.getParentFile());
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            target.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void validateOk() {
        String l = leftField.getText().trim();
        String r = rightField.getText().trim();
        okBtn.setEnabled(!l.isEmpty() && !r.isEmpty()
                && new File(l).isFile() && new File(r).isFile());
    }

    private void openDiff() {
        File left  = new File(leftField.getText().trim());
        File right = new File(rightField.getText().trim());

        if (looksLikeBinary(left) || looksLikeBinary(right)) {
            JOptionPane.showMessageDialog(this,
                    "One or both files appear to be binary and cannot be compared as text.",
                    "Binary file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String l = Files.readString(left.toPath());
                String r = Files.readString(right.toPath());
                return new String[]{l, r};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    String name = left.getName();
                    DiffViewerWindow w = new DiffViewerWindow(
                            name,
                            left.getAbsolutePath(),  texts[0],
                            right.getAbsolutePath(), texts[1]);
                    w.setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to read files for comparison", ex);
                    JOptionPane.showMessageDialog(CompareFilesDialog.this,
                            "Cannot read file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private javax.swing.event.DocumentListener docListener() {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { validateOk(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { validateOk(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateOk(); }
        };
    }

    private static GridBagConstraints gbc(int gridx, int gridy, double weightx) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = gridx;
        c.gridy   = gridy;
        c.weightx = weightx;
        c.fill    = (weightx > 0) ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(4, 4, 4, 4);
        return c;
    }

    /** Attaches a file/folder drag-and-drop handler to the given text field. */
    static void attachFileDrop(JTextField field, boolean dirOnly) {
        Border defaultBorder = field.getBorder();
        new DropTarget(field, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    field.setBorder(BorderFactory.createLineBorder(new Color(0x4488FF), 2));
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                field.setBorder(defaultBorder);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                field.setBorder(defaultBorder);
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>)
                            dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File f = files.get(0);
                        if (!dirOnly || f.isDirectory()) {
                            field.setText(f.getAbsolutePath());
                            dtde.dropComplete(true);
                            return;
                        }
                    }
                } catch (Exception ignored) {}
                dtde.dropComplete(false);
            }
        }, true);
    }

    /** Heuristic: treat as binary if the first 8 KB contains a null byte. */
    static boolean looksLikeBinary(File f) {
        try {
            byte[] buf = new byte[8192];
            int read;
            try (var in = Files.newInputStream(f.toPath())) {
                read = in.read(buf);
            }
            for (int i = 0; i < read; i++) {
                if (buf[i] == 0) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
