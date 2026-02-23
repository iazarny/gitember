package com.az.gitember.ui;

import com.az.gitember.ui.misc.RotatedIcon;
import org.kordamp.ikonli.Ikon;
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
            Icon icon = themeAwareIcon(ikon, 16);
            btn.setIcon(rotation == 0 ? icon : new RotatedIcon(icon, rotation));
        }

        btn.setText(text);
        if (tooltip != null) {
            btn.setToolTipText(tooltip);
        }

        btn.setFocusable(false);
        return btn;
    }

    /**
     * Returns an Icon that derives its colour from the painting component's
     * foreground at paint time, so it automatically adapts to light/dark themes.
     */
    public static Icon themeAwareIcon(Ikon ikon, int size) {
        FontIcon fi = FontIcon.of(ikon, size);
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color fg = c != null ? c.getForeground()
                        : UIManager.getColor("Button.foreground");
                fi.setIconColor(fg != null ? fg : Color.BLACK);
                fi.paintIcon(c, g, x, y);
            }
            @Override public int getIconWidth()  { return fi.getIconWidth(); }
            @Override public int getIconHeight() { return fi.getIconHeight(); }
        };
    }

}
