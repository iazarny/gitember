package com.az.gitember.ui.stat;

import com.az.gitember.ui.SyntaxStyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

/**
 * Java2D pie chart displaying lines-per-developer share.
 * Call {@link #setData(Map, Color[])} to update content.
 */
public class PieChartPanel extends JPanel {

    private static final int PIE_SIZE    = 200;
    private static final int LEGEND_GAP  = 16;
    private static final int LEGEND_BOX  = 12;
    private static final int PADDING     = 16;

    private List<String>  authors  = Collections.emptyList();
    private List<Integer> values   = Collections.emptyList();
    private Color[]       colors   = new Color[0];
    private long          total    = 0;

    // Arc shapes for hit-testing (tooltip)
    private final List<Arc2D.Double> arcs = new ArrayList<>();
    private int hoveredIndex = -1;

    public PieChartPanel() {
        setPreferredSize(new Dimension(420, 260));
        setToolTipText("");   // enable tooltip machinery

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int hit = -1;
                for (int i = 0; i < arcs.size(); i++) {
                    if (arcs.get(i).contains(e.getX(), e.getY())) {
                        hit = i;
                        break;
                    }
                }
                if (hit != hoveredIndex) {
                    hoveredIndex = hit;
                    repaint();
                }
            }
        });
    }

    /** @param lines  author → line-count map (any order; will be sorted descending by value)
     *  @param colors colour array aligned to sorted author order */
    public void setData(Map<String, Integer> lines, Color[] colors) {
        if (lines == null || lines.isEmpty()) {
            authors = Collections.emptyList();
            values  = Collections.emptyList();
            this.colors = new Color[0];
            total = 0;
            repaint();
            return;
        }
        // sort descending
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(lines.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        authors = new ArrayList<>();
        values  = new ArrayList<>();
        total   = 0;
        for (Map.Entry<String, Integer> e : sorted) {
            authors.add(e.getKey());
            values.add(e.getValue());
            total += e.getValue();
        }
        this.colors = colors != null ? colors : buildColors(authors.size());
        repaint();
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (hoveredIndex >= 0 && hoveredIndex < authors.size()) {
            String author = authors.get(hoveredIndex);
            int    lines  = values.get(hoveredIndex);
            double pct    = total > 0 ? 100.0 * lines / total : 0;
            return String.format("%s: %,d lines (%.1f%%)", author, lines, pct);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (authors.isEmpty() || total == 0) {
            paintEmpty(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Pie area on the left
        int pieX = PADDING;
        int pieY = (h - PIE_SIZE) / 2;

        arcs.clear();
        double startAngle = 90.0;
        for (int i = 0; i < values.size(); i++) {
            double extent = 360.0 * values.get(i) / total;
            Arc2D.Double arc = new Arc2D.Double(
                    pieX, pieY, PIE_SIZE, PIE_SIZE,
                    startAngle, -extent, Arc2D.PIE);
            arcs.add(arc);

            // Draw slice — slightly exploded if hovered
            if (i == hoveredIndex) {
                double mid = Math.toRadians(startAngle - extent / 2.0);
                double dx  = 6 * Math.cos(mid);
                double dy  = -6 * Math.sin(mid);
                g2.translate(dx, dy);
            }
            g2.setColor(colors[i % colors.length]);
            g2.fill(arc);
            g2.setColor(getBackground().darker());
            g2.draw(arc);
            if (i == hoveredIndex) {
                g2.translate(-6 * Math.cos(Math.toRadians(startAngle - extent / 2.0)),
                              6 * Math.sin(Math.toRadians(startAngle - extent / 2.0)));
            }
            startAngle -= extent;
        }

        // Legend on the right
        int legendX = pieX + PIE_SIZE + LEGEND_GAP;
        int legendY = PADDING;
        int lineH   = Math.max(LEGEND_BOX + 4, g2.getFontMetrics().getHeight() + 2);

        for (int i = 0; i < authors.size(); i++) {
            int y = legendY + i * lineH;
            if (y + lineH > h - PADDING) break; // clip if not enough room

            g2.setColor(colors[i % colors.length]);
            g2.fillRect(legendX, y + (lineH - LEGEND_BOX) / 2, LEGEND_BOX, LEGEND_BOX);
            g2.setColor(getForeground());
            g2.drawRect(legendX, y + (lineH - LEGEND_BOX) / 2, LEGEND_BOX, LEGEND_BOX);

            double pct = 100.0 * values.get(i) / total;
            String label = String.format("%s  %.1f%%", truncate(authors.get(i), 18), pct);
            g2.drawString(label, legendX + LEGEND_BOX + 6,
                    y + (lineH + g2.getFontMetrics().getAscent()) / 2 - 1);
        }

        g2.dispose();
    }

    private void paintEmpty(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getForeground());
        String msg = "No data";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2,
                (getHeight() + fm.getAscent()) / 2);
        g2.dispose();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /** Build a default HSB palette for {@code n} entries. */
    public static Color[] buildColors(int n) {
        if (n == 0) return new Color[0];
        boolean dark = SyntaxStyleUtil.isDarkTheme();
        float brightness = dark ? 0.85f : 0.75f;
        Color[] c = new Color[n];
        for (int i = 0; i < n; i++) {
            c[i] = Color.getHSBColor(i / (float) n, 0.65f, brightness);
        }
        return c;
    }
}
