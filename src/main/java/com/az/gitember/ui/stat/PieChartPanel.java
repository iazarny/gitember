package com.az.gitember.ui.stat;

import com.az.gitember.ui.SyntaxStyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.List;

/**
 * Java2D pie chart — scales with its component bounds.
 * Call {@link #setData(Map, Color[])} to update content.
 */
public class PieChartPanel extends JPanel {

    private static final int LEGEND_GAP = 16;
    private static final int LEGEND_BOX = 12;
    private static final int PADDING    = 12;

    private List<String>  authors = Collections.emptyList();
    private List<Integer> values  = Collections.emptyList();
    private Color[]       colors  = new Color[0];
    private long          total   = 0;

    // Rebuilt on every paint; used for hover hit-testing
    private final List<Arc2D.Double> arcs = new ArrayList<>();
    private int hoveredIndex = -1;

    public PieChartPanel() {
        setToolTipText("");  // enable tooltip machinery
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int hit = -1;
                for (int i = 0; i < arcs.size(); i++) {
                    if (arcs.get(i).contains(e.getX(), e.getY())) { hit = i; break; }
                }
                if (hit != hoveredIndex) { hoveredIndex = hit; repaint(); }
            }
        });
    }

    /**
     * @param lines  author → line-count map (sorted descending at paint time)
     * @param colors colour array aligned to the sorted author order
     */
    public void setData(Map<String, Integer> lines, Color[] colors) {
        if (lines == null || lines.isEmpty()) {
            authors = Collections.emptyList();
            values  = Collections.emptyList();
            this.colors = new Color[0];
            total = 0;
            repaint();
            return;
        }
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
            int    lines = values.get(hoveredIndex);
            double pct   = total > 0 ? 100.0 * lines / total : 0;
            return String.format("%s: %,d lines (%.1f%%)", authors.get(hoveredIndex), lines, pct);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (authors.isEmpty() || total == 0) { paintEmpty(g); return; }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ── Dynamic pie diameter ─────────────────────────────────────────────
        // Left half minus padding for the circle; right half for the legend.
        int pieDiam = Math.max(40, Math.min(w / 2 - PADDING * 2, h - PADDING * 2));
        int pieX    = PADDING;
        int pieY    = (h - pieDiam) / 2;

        // ── Slices ───────────────────────────────────────────────────────────
        arcs.clear();
        double startAngle = 90.0;
        for (int i = 0; i < values.size(); i++) {
            double extent = 360.0 * values.get(i) / total;
            Arc2D.Double arc = new Arc2D.Double(
                    pieX, pieY, pieDiam, pieDiam, startAngle, -extent, Arc2D.PIE);
            arcs.add(arc);

            if (i == hoveredIndex) {
                double mid = Math.toRadians(startAngle - extent / 2.0);
                g2.translate(6 * Math.cos(mid), -6 * Math.sin(mid));
            }
            g2.setColor(colors[i % colors.length]);
            g2.fill(arc);
            g2.setColor(getBackground().darker());
            g2.draw(arc);
            if (i == hoveredIndex) {
                double mid = Math.toRadians(startAngle - extent / 2.0);
                g2.translate(-6 * Math.cos(mid), 6 * Math.sin(mid));
            }
            startAngle -= extent;
        }

        // ── Legend ───────────────────────────────────────────────────────────
        int legendX  = pieX + pieDiam + LEGEND_GAP;
        int legendY  = PADDING;
        FontMetrics fm   = g2.getFontMetrics();
        int lineH        = Math.max(LEGEND_BOX + 4, fm.getHeight() + 2);
        int maxLabelChars = Math.max(6, (w - legendX - LEGEND_BOX - 50) / Math.max(1, fm.charWidth('m')));

        for (int i = 0; i < authors.size(); i++) {
            int y = legendY + i * lineH;
            if (y + lineH > h - PADDING) break;

            g2.setColor(colors[i % colors.length]);
            g2.fillRect(legendX, y + (lineH - LEGEND_BOX) / 2, LEGEND_BOX, LEGEND_BOX);
            g2.setColor(getForeground());
            g2.drawRect(legendX, y + (lineH - LEGEND_BOX) / 2, LEGEND_BOX, LEGEND_BOX);

            double pct   = 100.0 * values.get(i) / total;
            String label = String.format("%s  %.1f%%", truncate(authors.get(i), maxLabelChars), pct);
            g2.drawString(label, legendX + LEGEND_BOX + 6,
                    y + (lineH + fm.getAscent()) / 2 - 1);
        }

        g2.dispose();
    }

    private void paintEmpty(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getForeground());
        String msg = "No data";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, (getHeight() + fm.getAscent()) / 2);
        g2.dispose();
    }

    private static String truncate(String s, int max) {
        if (max <= 1) return "…";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /** HSB palette shared between pie chart and bar chart. */
    public static Color[] buildColors(int n) {
        if (n == 0) return new Color[0];
        boolean dark = SyntaxStyleUtil.isDarkTheme();
        float brightness = dark ? 0.85f : 0.75f;
        Color[] c = new Color[n];
        for (int i = 0; i < n; i++) c[i] = Color.getHSBColor(i / (float) n, 0.65f, brightness);
        return c;
    }
}
