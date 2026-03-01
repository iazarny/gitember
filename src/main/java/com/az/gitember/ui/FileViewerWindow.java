package com.az.gitember.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.awt.event.KeyEvent;

/**
 * Simple window to display text file content from a git commit.
 */
public class FileViewerWindow extends JFrame {

    private final RSyntaxTextArea textArea;
    private final String suggestedFileName;
    private final SearchBar searchBar;

    public FileViewerWindow(String title, String content) {
        this(title, content, title);
    }

    public FileViewerWindow(String title, String content, String fileName) {
        setTitle(title);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.suggestedFileName = fileName;

        textArea = new RSyntaxTextArea(content);
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxStyleUtil.getSyntaxStyle(fileName));
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(SyntaxStyleUtil.monoFont());
        textArea.setCaretPosition(0);
        SyntaxStyleUtil.applyTheme(textArea);

        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setFoldIndicatorEnabled(true);

        searchBar = new SearchBar(textArea);

        JButton saveBtn = new JButton("Save...");
        saveBtn.addActionListener(e -> saveContent());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.add(saveBtn);
        btnPanel.add(closeBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchBar, BorderLayout.NORTH);
        getContentPane().add(sp,        BorderLayout.CENTER);
        getContentPane().add(btnPanel,  BorderLayout.SOUTH);

        getRootPane().setDefaultButton(closeBtn);

        // Ctrl/Cmd+F → open search bar
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().registerKeyboardAction(
                e -> searchBar.activate(), ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void saveContent() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save as");
        chooser.setSelectedFile(new File(suggestedFileName));
        chooser.setFileFilter(new FileNameExtensionFilter("Patch / Diff files (*.patch, *.diff)", "patch", "diff"));
        chooser.setAcceptAllFileFilterUsed(true);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        try {
            Files.writeString(target.toPath(), textArea.getText(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
