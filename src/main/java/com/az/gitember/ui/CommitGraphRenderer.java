package com.az.gitember.ui;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;

public class CommitGraphRenderer extends AbstractPlotRenderer<PlotLane, Color> {

    public static final Color[] LANE_COLORS = {
            new Color(255, 0, 0), new Color(0, 200, 0), new Color(229, 200, 0),
            new Color(177, 178, 255), new Color(255, 0, 255), new Color(0, 200, 200),
            new Color(206, 0, 0), new Color(0, 160, 0), new Color(0, 187, 187),
            new Color(133, 133, 255), new Color(217, 0, 190), new Color(0, 170, 170),
            new Color(159, 0, 0), new Color(0, 130, 0), new Color(136, 136, 0),
            new Color(71, 71, 255), new Color(173, 0, 151), new Color(0, 140, 140),
            new Color(99, 0, 0), new Color(0, 99, 0), new Color(83, 83, 0),
            new Color(0, 0, 255), new Color(129, 0, 113), new Color(0, 114, 114),
    };

    private final HashMap<PlotLane, Color> plotLaneColorMap = new HashMap<>();
    private Graphics2D g2;
    private PlotCommit<PlotLane> currentCommit;
    private int graphWidth;

    @Override
    protected Color laneColor(PlotLane myLane) {
        return plotLaneColorMap.computeIfAbsent(
                myLane,
                k -> LANE_COLORS[(1 + plotLaneColorMap.size()) % LANE_COLORS.length]
        );
    }

    @Override
    protected void drawLine(Color color, int x1, int y1, int x2, int y2, int width) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (x1 == x2 || y1 == y2) {
            g2.drawLine(x1, y1, x2, y2);
            return;
        }

        // Bezier curve for diagonal lines (branch/merge)
        CubicCurve2D curve = new CubicCurve2D.Double();
        if (x1 > x2) {
            curve.setCurve(x1, y1, x1 - 10, y1, x2, y2, x2, y2);
        } else {
            curve.setCurve(x1, y1, x1 + 10, y1, x2 - 1, y2, x2, y2);
        }
        g2.draw(curve);
    }

    @Override
    protected void drawCommitDot(int x, int y, int w, int h) {
        Color color = laneColor(currentCommit.getLane());
        if (currentCommit.getChildCount() == 0) {
            g2.setColor(color.darker());
        } else {
            g2.setColor(color);
        }
        g2.fill(new Ellipse2D.Double(x, y, w + 1, h + 1));
        g2.setColor(color.darker());
        g2.draw(new Ellipse2D.Double(x, y, w + 2, h + 2));
    }

    @Override
    protected void drawBoundaryDot(int x, int y, int w, int h) {
        g2.setColor(UIManager.getColor("Panel.background") != null
                ? UIManager.getColor("Panel.background").brighter() : Color.LIGHT_GRAY);
        g2.fill(new Ellipse2D.Double(x, y, w + 1, h + 1));
        g2.setColor(UIManager.getColor("Panel.foreground") != null
                ? UIManager.getColor("Panel.foreground") : Color.GRAY);
        g2.draw(new Ellipse2D.Double(x, y, w + 2, h + 2));
    }

    @Override
    protected void drawText(String msg, int x, int y) {
        graphWidth = x;
    }

    @Override
    protected int drawLabel(int x, int y, Ref ref) {
        if (ref == null || ref.getName() == null) return 0;

        String name = ref.getName();
        // Shorten common prefixes
        if (name.startsWith("refs/heads/")) name = name.substring(11);
        else if (name.startsWith("refs/remotes/")) name = name.substring(13);
        else if (name.startsWith("refs/tags/")) name = name.substring(10);

        Font origFont = g2.getFont();
        Font labelFont = origFont.deriveFont(Font.BOLD, 10f);
        g2.setFont(labelFont);
        FontMetrics fm = g2.getFontMetrics();

        int padding = 4;
        int textWidth = fm.stringWidth(name);
        int boxWidth = textWidth + padding * 2;
        int boxHeight = fm.getHeight();

        // Draw label box - use theme-aware colors
        Color tableBg = UIManager.getColor("Table.background");
        boolean isDark = tableBg != null && tableBg.getRed() < 128;
        if (isDark) {
            g2.setColor(new Color(50, 60, 80));
            g2.fillRoundRect(x, y - boxHeight / 2, boxWidth, boxHeight, 6, 6);
            g2.setColor(new Color(100, 130, 180));
            g2.drawRoundRect(x, y - boxHeight / 2, boxWidth, boxHeight, 6, 6);
            g2.setColor(new Color(170, 190, 230));
        } else {
            g2.setColor(new Color(220, 230, 250));
            g2.fillRoundRect(x, y - boxHeight / 2, boxWidth, boxHeight, 6, 6);
            g2.setColor(new Color(100, 130, 180));
            g2.drawRoundRect(x, y - boxHeight / 2, boxWidth, boxHeight, 6, 6);
            g2.setColor(new Color(50, 70, 120));
        }
        g2.drawString(name, x + padding, y + fm.getAscent() - boxHeight / 2);

        g2.setFont(origFont);
        return boxWidth + 6;
    }

    public int render(Graphics2D g2, PlotCommit<PlotLane> commit, int rowHeight) {
        this.g2 = g2;
        this.currentCommit = commit;
        this.graphWidth = 0;
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCommit(commit, rowHeight);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldHint != null ? oldHint : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        return graphWidth;
    }
}
