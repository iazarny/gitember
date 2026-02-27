package com.az.gitember.component;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class RRightTextScrollPane extends RTextScrollPane {

    public RRightTextScrollPane(RSyntaxTextArea textArea) {
        super(textArea);
        configure();
    }

    private void configure() {

        setLineNumbersEnabled(true);

        Gutter gutter = getGutter();
        JScrollBar verticalBar = getVerticalScrollBar();

        setRowHeaderView(null);

        setLayout(new BorderLayout());

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(gutter, BorderLayout.CENTER);
        eastPanel.add(verticalBar, BorderLayout.EAST);

        add(getViewport(), BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }
}
