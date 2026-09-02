package com.az.gitember.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Paints a {@link BufferedImage} fitted to the viewport or at a fixed zoom,
 * with a checkerboard behind transparent pixels.
 */
class FitImagePanel extends JPanel {

    private static final int CHECKER_SIZE = 8;
    private static final Color CHECKER_A = new Color(0xC0, 0xC0, 0xC0);
    private static final Color CHECKER_B = new Color(0xE8, 0xE8, 0xE8);

    private BufferedImage image;
    private String placeholder = "No image";
    private boolean fit = true;
    private double zoom = 1.0;

    FitImagePanel() {
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
    }

    void setImage(BufferedImage image) {
        this.image = image;
        revalidate();
        repaint();
    }

    BufferedImage getImage() {
        return image;
    }

    void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    void setFit(boolean fit) {
        this.fit = fit;
        revalidate();
        repaint();
    }

    boolean isFit() {
        return fit;
    }

    void setZoom(double zoom) {
        this.fit = false;
        this.zoom = Math.max(0.05, Math.min(16.0, zoom));
        revalidate();
        repaint();
    }

    double getZoom() {
        return zoom;
    }

    /** Scale that would fit the current image into the given viewport. */
    double fitScaleFor(int viewWidth, int viewHeight) {
        double scale = 1.0;
        if (image != null && image.getWidth() > 0 && image.getHeight() > 0
                && viewWidth > 0 && viewHeight > 0) {
            scale = Math.min((double) viewWidth / image.getWidth(),
                    (double) viewHeight / image.getHeight());
        }
        return scale;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = new Dimension(200, 200);
        if (image != null) {
            if (fit) {
                Container parent = getParent();
                if (parent instanceof JViewport viewport) {
                    Dimension extent = viewport.getExtentSize();
                    if (extent.width > 0 && extent.height > 0) {
                        size = extent;
                    }
                }
            } else {
                size = new Dimension(
                        Math.max(1, (int) Math.round(image.getWidth() * zoom)),
                        Math.max(1, (int) Math.round(image.getHeight() * zoom)));
            }
        }
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        paintCheckerboard(g2, w, h);
        if (image != null) {
            double scale = zoom;
            if (fit) {
                scale = fitScaleFor(w, h);
            }
            int dw = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int dh = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int x = (w - dw) / 2;
            int y = (h - dh) / 2;
            g2.drawImage(image, x, y, dw, dh, null);
        } else if (placeholder != null && !placeholder.isBlank()) {
            g2.setColor(UIManager.getColor("Label.disabledForeground"));
            FontMetrics fm = g2.getFontMetrics();
            int tx = Math.max(0, (w - fm.stringWidth(placeholder)) / 2);
            int ty = Math.max(fm.getAscent(), (h + fm.getAscent()) / 2);
            g2.drawString(placeholder, tx, ty);
        }
        g2.dispose();
    }

    private void paintCheckerboard(Graphics2D g2, int w, int h) {
        for (int y = 0; y < h; y += CHECKER_SIZE) {
            for (int x = 0; x < w; x += CHECKER_SIZE) {
                boolean alt = ((x / CHECKER_SIZE) + (y / CHECKER_SIZE)) % 2 == 0;
                g2.setColor(alt ? CHECKER_A : CHECKER_B);
                g2.fillRect(x, y, CHECKER_SIZE, CHECKER_SIZE);
            }
        }
    }
}
