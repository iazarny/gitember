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

        // JScrollPane.setLayout() requires a ScrollPaneLayout subclass.
        // Override layoutContainer to reposition components after the normal
        // pass so the order becomes:  [Text viewport | Gutter | VScrollbar]
        setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);          // standard placement
                if (vsb == null || !vsb.isVisible()) return;

                // After super: rowHead(gutter) is at x=0, viewport starts after it,
                // vsb is on the right. We want: viewport | gutter | vsb.
                if (rowHead != null) {
                    Rectangle rh  = rowHead.getBounds();
                    int rhW = rh.width;

                    // Shift viewport left to fill the space the gutter vacated
                    if (viewport != null) {
                        Rectangle vp = viewport.getBounds();
                        viewport.setBounds(0, vp.y, vp.width, vp.height);
                    }

                    // Move gutter to sit just left of the scrollbar
                    Rectangle vsbR = vsb.getBounds();
                    rowHead.setBounds(vsbR.x - rhW, rh.y, rhW, rh.height);
                }
            }
        });
    }
}
