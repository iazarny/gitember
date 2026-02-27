package com.az.gitember.ui.stat;

import com.az.gitember.data.ScmStat;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * Java2D stacked-bar chart showing per-developer line contributions per month.
 * Call {@link #setData(List, List, Color[])} to update content.
 */
public class MonthlyBarChartPanel extends JPanel {

    private static final int PAD_LEFT   = 60;
    private static final int PAD_RIGHT  = 16;
    private static final int PAD_TOP    = 16;
    private static final int PAD_BOTTOM = 64;  // space for rotated labels
    private static final int Y_TICKS    = 5;

    private List<ScmStat> monthly  = Collections.emptyList();
    private List<String>  authors  = Collections.emptyList();
    private Color[]       colors   = new Color[0];

    private static final SimpleDateFormat LABEL_FMT = new SimpleDateFormat("yyyy-MM");

    public MonthlyBarChartPanel() {
        setPreferredSize(new Dimension(800, 200));
    }

    /**
     * @param monthly  one ScmStat per month, chronological order
     * @param authors  sorted author list (same order as pie chart)
     * @param colors   colour array aligned to authors
     */
    public void setData(List<ScmStat> monthly, List<String> authors, Color[] colors) {
        this.monthly = monthly != null ? monthly : Collections.emptyList();
        this.authors = authors != null ? authors : Collections.emptyList();
        this.colors  = colors  != null ? colors  : new Color[0];
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (monthly.isEmpty() || authors.isEmpty()) {
            paintEmpty(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w        = getWidth();
        int h        = getHeight();
        int chartW   = w - PAD_LEFT - PAD_RIGHT;
        int chartH   = h - PAD_TOP  - PAD_BOTTOM;

        // Compute max total across months
        long maxTotal = 1;
        for (ScmStat s : monthly) {
            long t = s.getTotalLines().values().stream().mapToLong(Integer::longValue).sum();
            if (t > maxTotal) maxTotal = t;
        }

        // Draw Y axis ticks + gridlines
        g2.setFont(g2.getFont().deriveFont(10f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= Y_TICKS; i++) {
            long tickVal = maxTotal * i / Y_TICKS;
            int  yPx     = PAD_TOP + chartH - (int)(chartH * i / Y_TICKS);

            g2.setColor(new Color(128, 128, 128, 60));
            g2.drawLine(PAD_LEFT, yPx, PAD_LEFT + chartW, yPx);

            g2.setColor(getForeground());
            String label = formatCount(tickVal);
            g2.drawString(label, PAD_LEFT - fm.stringWidth(label) - 4,
                    yPx + fm.getAscent() / 2);
        }

        // Draw Y axis line
        g2.setColor(getForeground());
        g2.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + chartH);

        // Bar width with small gap
        int n       = monthly.size();
        int barW    = Math.max(4, (chartW - n * 4) / n);
        int gap     = chartW / n - barW;

        for (int mi = 0; mi < n; mi++) {
            ScmStat stat = monthly.get(mi);
            int barX = PAD_LEFT + mi * (barW + gap) + gap / 2;

            // Draw stacked segments bottom-up
            int yBottom = PAD_TOP + chartH;
            for (int ai = 0; ai < authors.size(); ai++) {
                String author  = authors.get(ai);
                Integer lines  = stat.getTotalLines().getOrDefault(author, 0);
                if (lines <= 0) continue;
                int segH = (int)(chartH * (long) lines / maxTotal);
                if (segH < 1) segH = 1;
                int segY = yBottom - segH;

                g2.setColor(colors[ai % colors.length]);
                g2.fillRect(barX, segY, barW, segH);

                yBottom = segY;
            }

            // Draw bar border
            g2.setColor(getBackground().darker());
            int drawnH = PAD_TOP + chartH - yBottom;
            if (drawnH > 0) g2.drawRect(barX, yBottom, barW, drawnH);

            // X-axis label (rotated 45°)
            String xLabel = stat.getDate() != null
                    ? LABEL_FMT.format(stat.getDate())
                    : ("M" + (mi + 1));
            drawRotatedLabel(g2, xLabel, barX + barW / 2, PAD_TOP + chartH + 6);
        }

        // X axis line
        g2.setColor(getForeground());
        g2.drawLine(PAD_LEFT, PAD_TOP + chartH, PAD_LEFT + chartW, PAD_TOP + chartH);

        g2.dispose();
    }

    private void drawRotatedLabel(Graphics2D g2, String text, int cx, int baseY) {
        FontMetrics fm = g2.getFontMetrics();
        AffineTransform old = g2.getTransform();
        g2.translate(cx, baseY);
        g2.rotate(Math.toRadians(45));
        g2.setColor(getForeground());
        g2.drawString(text, 2, fm.getAscent() / 2);
        g2.setTransform(old);
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

    private static String formatCount(long v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.0fk", v / 1_000.0);
        return String.valueOf(v);
    }
}
