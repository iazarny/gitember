package com.az.gitember.dialog;

import javax.swing.*;
import java.awt.*;

public class CommitMessageArea extends JPanel {

    private final JTextArea messageArea;

    public CommitMessageArea(String name, int messageRows) {

        messageArea = new JTextArea(messageRows, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        add(new JLabel(name), BorderLayout.NORTH);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

    }

    public JTextArea getMessageArea() {
        return messageArea;
    }

    public String getText() {
        return messageArea.getText();
    }

    public void setText(String text) {
        messageArea.setText(text);
        messageArea.setCaretPosition(text != null ? text.length() : 0);
    }
}
