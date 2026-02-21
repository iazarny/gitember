package com.az.gitember.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Simple window to display text file content from a git commit.
 */
public class FileViewerWindowOld extends JFrame {

    private final JTextArea textArea;

    public FileViewerWindowOld(String title, String content) {
        setTitle(title);
        setSize(900, 600);
        setLocationRelativeTo(null);

        textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
}
