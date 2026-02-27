package com.az.gitember.component;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class RLeftTextScrollPane extends RTextScrollPane {

    public RLeftTextScrollPane(RSyntaxTextArea textArea) {
        super(textArea);
        configure();
    }

    private void configure() {

        setLineNumbersEnabled(true);

        Gutter gutter = getGutter();
        JScrollBar verticalBar = getVerticalScrollBar();

        // Remove default layout components
        setRowHeaderView(null);

        setLayout(new BorderLayout());

        // Order: Scrollbar | LineNumbers | Text
        add(verticalBar, BorderLayout.WEST);
        add(gutter, BorderLayout.CENTER);
        add(getViewport(), BorderLayout.EAST);
    }
}
