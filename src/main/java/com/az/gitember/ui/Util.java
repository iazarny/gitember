package com.az.gitember.ui;

import com.az.gitember.ui.misc.RotatedIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class Util {

    public static JButton createButton(String text) {
        return createButton(text, null, null);
    }

    public static JButton createButton(String text, String tooltip) {
        return createButton(text, tooltip, null);
    }

    public static JButton createButton(String text, String tooltip, org.kordamp.ikonli.Ikon ikon) {
        return createButton(text, tooltip, ikon, 0);
    }

    public static JButton createButton(String text, String tooltip, org.kordamp.ikonli.Ikon ikon, int rotation) {
        JButton btn = new JButton();

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setMargin(new Insets(10, 20, 10, 20));

        if (ikon != null) {
            if (rotation == 0) {
                btn.setIcon(FontIcon.of(ikon, 16));
            } else  {
                btn.setIcon(new RotatedIcon(  FontIcon.of(ikon, 16), rotation));
            }

        }

        btn.setText(text);
        if (tooltip != null) {
            btn.setToolTipText(tooltip);
        }

        btn.setFocusable(false);
        return btn;
    }

}
