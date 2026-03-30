package com.az.gitember.ui;

import com.az.gitember.ui.misc.RotatedIcon;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
        Dimension size = new Dimension(110, 40);
        return createButton(text, tooltip, ikon, rotation, size);
    }

    public static JButton createButton(String text, String tooltip, org.kordamp.ikonli.Ikon ikon, int rotation, Dimension size) {
        JButton btn = new JButton();

        if (size != null) {
            btn.setPreferredSize(size);
            btn.setMinimumSize(size);
            btn.setMaximumSize(size);
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);

        if (ikon != null) {
            Icon icon = themeAwareIcon(ikon, 16);
            btn.setIcon(rotation == 0 ? icon : new RotatedIcon(icon, rotation));
        }

        btn.setText("<html>" + text + "</html>");
        if (tooltip != null) {
            btn.setToolTipText(tooltip);
        }

        btn.setFocusable(false);
        return btn;
    }

    private static List<Image> cachedAppIcons;

    /**
     * Returns the gitember icon set (multiple resolutions) for use with
     * {@link java.awt.Window#setIconImages(List)}.
     */
    public static List<Image> appIcons() {
        if (cachedAppIcons != null) return cachedAppIcons;
        int[] sizes = {16, 32, 48, 64, 128, 256};
        List<Image> icons = new ArrayList<>(sizes.length);
        for (int s : sizes) {
            String path = "/icon/gitember-" + s + ".png";
            try (var is = Util.class.getResourceAsStream(path)) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null) icons.add(img);
                }
            } catch (Exception ignored) {}
        }
        cachedAppIcons = icons;
        return icons;
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
