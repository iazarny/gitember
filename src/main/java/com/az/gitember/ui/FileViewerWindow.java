package com.az.gitember.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * Simple window to display text file content from a git commit.
 */
public class FileViewerWindow extends JFrame {

    private final RSyntaxTextArea textArea;

    public FileViewerWindow(String title, String content) {
        this(title, content, title);
    }

    public FileViewerWindow(String title, String content, String fileName) {
        setTitle(title);
        setSize(900, 600);
        setLocationRelativeTo(null);

        textArea = new RSyntaxTextArea(content);
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxStyleUtil.getSyntaxStyle(fileName));
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setFoldIndicatorEnabled(true);

        getContentPane().add(sp, BorderLayout.CENTER);
    }
}
