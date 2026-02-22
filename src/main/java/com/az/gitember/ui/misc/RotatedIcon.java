package com.az.gitember.ui.misc;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class RotatedIcon implements Icon {

    private final Icon icon;
    private final double angle; // degrees

    public RotatedIcon(Icon icon, double angle) {
        this.icon = icon;
        this.angle = Math.toRadians(angle);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        g2.rotate(angle, x + w / 2.0, y + h / 2.0);
        icon.paintIcon(c, g2, x, y);

        g2.dispose();
    }
}