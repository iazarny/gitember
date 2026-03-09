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
    private String[] leftLines  = null;
    private String[] rightLines = null;

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

    void setData(EditList editList, String[] leftLines, String[] rightLines) {
        this.editList   = editList;
        this.leftLines  = leftLines;
        this.rightLines = rightLines;
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

        // Single pass per column: each line is drawn with its correct colour
        paintTextLines(g2, 0,    half, h, leftLines,  true);
        paintTextLines(g2, half, w,    h, rightLines, false);

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

    /**
     * Single-pass minimap renderer for one column.
     * <p>
     * Builds a per-line colour array from the {@code editList} first, then
     * iterates every source line once: blank lines are skipped, changed lines
     * are drawn with their highlight colour, unchanged lines with a dim grey.
     * Each line is rendered as a short horizontal segment whose x-start reflects
     * the leading indent and whose x-end reflects the trimmed text width.
     *
     * @param isLeft {@code true} for the left (old) column, {@code false} for right (new)
     */
    private void paintTextLines(Graphics2D g2, int x1, int x2, int panelH,
                                 String[] lines, boolean isLeft) {
        if (lines == null || lines.length == 0) return;
        int colW = x2 - x1 - 2;
        if (colW <= 0) return;

        int n = lines.length;

        // ── Build per-line colour lookup ──────────────────────────────────────
        Color dimColor = SyntaxStyleUtil.isDarkTheme()
                ? new Color(200, 200, 200, 70)
                : new Color(50,  50,  50,  70);
        Color[] lineColor = new Color[n];
        java.util.Arrays.fill(lineColor, dimColor);

        if (editList != null) {
            for (Edit edit : editList) {
                Color c = switch (edit.getType()) {
                    case DELETE  -> isLeft  ? deletedColor() : null;
                    case INSERT  -> !isLeft ? addedColor()   : null;
                    case REPLACE -> changedColor();
                    default      -> null;
                };
                if (c == null) continue;
                int begin = isLeft ? edit.getBeginA() : edit.getBeginB();
                int end   = isLeft ? edit.getEndA()   : edit.getEndB();
                for (int i = begin; i < end && i < n; i++) lineColor[i] = c;
            }
        }

        // ── Scale reference: max line length capped at 120 chars ──────────────
        int maxLen = 1;
        for (String line : lines) maxLen = Math.max(maxLen, line.length());
        maxLen = Math.min(maxLen, 120);

        // ── Draw one line per source line ─────────────────────────────────────
        for (int i = 0; i < n; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;

            int indent = 0;
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                if      (ch == ' ')  indent++;
                else if (ch == '\t') indent += 4;
                else break;
            }

            int textEnd = line.stripTrailing().length();
            if (textEnd <= indent) continue;

            int xStart = x1 + 1 + indent                      * colW / maxLen;
            int xEnd   = x1 + 1 + Math.min(textEnd, maxLen)   * colW / maxLen;
            if (xEnd <= xStart) xEnd = xStart + 1;
            xEnd = Math.min(xEnd, x2 - 1);

            g2.setColor(lineColor[i]);
            g2.drawLine(xStart, lineToY(i, n, panelH), xEnd, lineToY(i, n, panelH));
        }
    }

    /** Paints the semi-transparent viewport indicator. */
    private void paintViewportRect(Graphics2D g2, int w, int h) {
        int rh = Math.max(4, (int) (viewportSize * h));
        int y  = Math.min((int) (viewportOffset * h), h - rh);   // top cannot push rect below panel
        y      = Math.max(0, y);                                   // top cannot be above panel
        rh     = Math.min(rh, h - y);                             // bottom cannot exceed panel
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
