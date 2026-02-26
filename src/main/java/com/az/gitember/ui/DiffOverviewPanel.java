package com.az.gitember.ui;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Compact diff-overview / minimap panel placed on the right edge of the diff window.
 *
 * <p>Diff blocks (background, column dividers, coloured regions) are rendered once
 * into a {@link BufferedImage} cache whenever the data or the component size changes.
 * Every subsequent repaint just blits that image and then paints only the lightweight
 * viewport rectangle on top — no redundant iteration over the edit list.</p>
 *
 * <p>Call {@link #setData} whenever the diff is recomputed and
 * {@link #setViewport} whenever the scroll position changes.</p>
 */
class DiffOverviewPanel extends JPanel {

    private static final int PREF_WIDTH = 80;

    private EditList editList;
    private int leftLineCount  = 1;
    private int rightLineCount = 1;

    private double viewportOffset = 0.0;   // 0.0–1.0
    private double viewportSize   = 1.0;   // 0.0–1.0

    private Consumer<Double> onJump;

    private boolean dragging   = false;
    private int     dragStartY = 0;

    /** Cached rendering of everything except the viewport rectangle. */
    private BufferedImage cachedImage = null;

    DiffOverviewPanel() {
        setPreferredSize(new Dimension(PREF_WIDTH, 0));
        setMinimumSize  (new Dimension(PREF_WIDTH, 0));
        setMaximumSize  (new Dimension(PREF_WIDTH, Integer.MAX_VALUE));
        setToolTipText("Diff overview — click or drag to navigate");

        // Invalidate cache when the panel is resized
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                cachedImage = null;
                repaint();
            }
        });

        MouseAdapter handler = new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) { handlePress(e);   }
            @Override public void mouseDragged (MouseEvent e) { handleDrag(e);    }
            @Override public void mouseReleased(MouseEvent e) { dragging = false; }
        };
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    void setData(EditList editList, int leftLineCount, int rightLineCount) {
        this.editList       = editList;
        this.leftLineCount  = Math.max(leftLineCount,  1);
        this.rightLineCount = Math.max(rightLineCount, 1);
        cachedImage = null;   // force cache rebuild
        repaint();
    }

    /** @param offsetPercent 0.0–1.0 top of visible area / total content height
     *  @param sizePercent   0.0–1.0 visible area height / total content height */
    void setViewport(double offsetPercent, double sizePercent) {
        this.viewportOffset = clamp(offsetPercent);
        this.viewportSize   = clamp(sizePercent);
        repaint();   // cheap: just blits cache + redraws the rect
    }

    void setOnJump(Consumer<Double> onJump) {
        this.onJump = onJump;
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Rebuild cache if needed (first paint, setData called, or resize)
        if (cachedImage == null || cachedImage.getWidth() != w || cachedImage.getHeight() != h) {
            cachedImage = buildCache(w, h);
        }

        // 1. Blit the pre-rendered diff blocks
        g.drawImage(cachedImage, 0, 0, null);

        // 2. Paint only the viewport rectangle on top
        Graphics2D g2 = (Graphics2D) g.create();
        paintViewportRect(g2, w, h);
        g2.dispose();
    }

    /** Renders background + dividers + all diff blocks into a fresh BufferedImage. */
    private BufferedImage buildCache(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(getBackground());
        g2.fillRect(0, 0, w, h);

        int half = w / 2;

        // Diff blocks
        if (editList != null) {
            for (Edit edit : editList) {
                switch (edit.getType()) {
                    case DELETE -> paintBlock(g2, 0, half, h,
                            edit.getBeginA(), edit.getEndA(), leftLineCount,  deletedColor());
                    case INSERT -> paintBlock(g2, half, w, h,
                            edit.getBeginB(), edit.getEndB(), rightLineCount, addedColor());
                    case REPLACE -> {
                        paintBlock(g2, 0,    half, h, edit.getBeginA(), edit.getEndA(), leftLineCount,  changedColor());
                        paintBlock(g2, half, w,    h, edit.getBeginB(), edit.getEndB(), rightLineCount, changedColor());
                    }
                    default -> { /* EMPTY — skip */ }
                }
            }
        }

        // Column divider and outer border (on top of blocks)
        Color sepColor = UIManager.getColor("Separator.foreground");
        if (sepColor == null) sepColor = Color.GRAY;
        g2.setColor(sepColor);
        g2.drawLine(half, 0, half, h - 1);
        g2.drawLine(0,    0, 0,    h - 1);
        g2.drawLine(w - 1, 0, w - 1, h - 1);

        g2.dispose();
        return img;
    }

    /** Paints a filled rectangle for a range of lines in one column. */
    private void paintBlock(Graphics2D g2, int x1, int x2, int panelH,
                             int beginLine, int endLine, int totalLines, Color color) {
        if (endLine <= beginLine) endLine = beginLine + 1;
        int y1 = lineToY(beginLine, totalLines, panelH);
        int y2 = lineToY(endLine,   totalLines, panelH);
        if (y2 <= y1) y2 = y1 + 2;
        g2.setColor(color);
        g2.fillRect(x1 + 1, y1, x2 - x1 - 1, y2 - y1);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> ");
    }

    /** Paints the semi-transparent viewport indicator. */
    private void paintViewportRect(Graphics2D g2, int w, int h) {
        int y  = (int) (viewportOffset * h);
        int rh = Math.max(4, (int) (viewportSize * h));
        g2.setColor(new Color(128, 128, 128, 55));
        g2.fillRect(0, y, w, rh);
        g2.setColor(new Color(100, 100, 100, 170));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(0, y, w - 1, rh - 1);
    }

    private int lineToY(int line, int totalLines, int panelH) {
        return (int) ((double) line / totalLines * panelH);
    }

    // ── Mouse interaction ─────────────────────────────────────────────────────

    private void handlePress(MouseEvent e) {
        int h     = getHeight();
        int rectY = (int) (viewportOffset * h);
        int rectH = Math.max(4, (int) (viewportSize * h));

        if (e.getY() >= rectY && e.getY() <= rectY + rectH) {
            dragging   = true;
            dragStartY = e.getY() - rectY;
        } else {
            double offset = clamp((double) e.getY() / h - viewportSize / 2);
            if (onJump != null) onJump.accept(offset);
        }
    }

    private void handleDrag(MouseEvent e) {
        if (!dragging) return;
        double offset = clamp((double) (e.getY() - dragStartY) / getHeight());
        if (onJump != null) onJump.accept(offset);
    }

    // ── Colours ───────────────────────────────────────────────────────────────

    private static Color addedColor() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(0, 110, 0)   : new Color(100, 200, 100);
    }
    private static Color deletedColor() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(130, 30, 30) : new Color(200, 100, 100);
    }
    private static Color changedColor() {
        return SyntaxStyleUtil.isDarkTheme() ? new Color(30, 60, 130) : new Color(100, 140, 210);
    }

    private static double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }
}
