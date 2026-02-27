package com.az.gitember.component;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
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

        // JScrollPane.setLayout() requires a ScrollPaneLayout subclass.
        // Override layoutContainer to reposition components after the normal
        // pass so the order becomes:  [VScrollbar | Gutter | Text viewport]
        setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);          // standard placement
                if (vsb == null || !vsb.isVisible()) return;

                Rectangle vsbR = vsb.getBounds();
                int vsbW = vsbR.width;

                // Shift the row header (gutter) right to make room for the scrollbar
                if (rowHead != null) {
                    Rectangle rh = rowHead.getBounds();
                    rowHead.setBounds(vsbW, rh.y, rh.width, rh.height);
                }

                // Shift the viewport right by the same amount
                if (viewport != null) {
                    Rectangle vp = viewport.getBounds();
                    viewport.setBounds(vp.x + vsbW, vp.y, vp.width, vp.height);
                }

                // Move the scrollbar to the far left
                vsb.setBounds(0, vsbR.y, vsbW, vsbR.height);
            }
        });
    }
}
