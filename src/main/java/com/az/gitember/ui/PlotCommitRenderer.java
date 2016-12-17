package com.az.gitember.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class PlotCommitRenderer extends AbstractPlotRenderer {

    private static final Color[] colors = new Color[]{
            Color.rgb(255, 0, 0), Color.rgb(0, 255, 0), Color.rgb(229, 229, 0), Color.rgb(177, 178, 255), Color.rgb(255, 0, 255), Color.rgb(0, 255, 255),
            Color.rgb(206, 0, 0), Color.rgb(0, 187, 0), Color.rgb(0, 187, 187), Color.rgb(133, 133, 255), Color.rgb(217, 0, 190), Color.rgb(0, 197, 197),
            Color.rgb(159, 0, 0), Color.rgb(0, 159, 0), Color.rgb(136, 136, 0), Color.rgb(71, 71, 255), Color.rgb(173, 0, 151), Color.rgb(0, 155, 155),
            Color.rgb(99, 0, 0), Color.rgb(0, 99, 0), Color.rgb(83, 83, 0), Color.rgb(0, 0, 255), Color.rgb(129, 0, 113), Color.rgb(0, 114, 114),
    };

    private HashMap<PlotLane, Color> plotLaneColorHashMap = new HashMap<>();

    private GraphicsContext graphicsContext;

    private PlotCommit plotCommit;

    @Override
    protected int drawLabel(int x, int y, Ref ref) {
        return 0;
    }

    @Override
    protected Object laneColor(PlotLane myLane) {
        if (plotLaneColorHashMap.get(myLane) == null) {
            plotLaneColorHashMap.put(
                    myLane,
                    colors[ (1 + plotLaneColorHashMap.size()) % colors.length   ]
                    );
        }
        return plotLaneColorHashMap.get(myLane);
    }

    @Override
    protected void drawLine(Object o, int x1, int y1, int x2, int y2, int width) {

        graphicsContext.setStroke((Color) o);

        graphicsContext.setLineWidth(width * 2);

        if (x1 == x2 || y1 == y2) {
            graphicsContext.strokeLine(x1, y1, x2, y2);
            return;
        }


        graphicsContext.beginPath();
        graphicsContext.moveTo(x1, y1);
        if (x1 > x2) {
            if (y1 > y2) {
                graphicsContext.bezierCurveTo(x1 - 15, y1, x2, y2, x2, y2);
            } else {
                graphicsContext.bezierCurveTo(x1 - 10, y1, x2, y2, x2, y2);
            }
        } else if (x1 < x2) {
            if (y1 < y2) {
                graphicsContext.bezierCurveTo(x1 + 8, y1, x2, y2, x2, y2);
            } else {
                graphicsContext.bezierCurveTo(x1 + 10, y1, x2, y2, x2, y2);
            }
        }
        graphicsContext.stroke();
        graphicsContext.closePath();

    }

    @Override
    protected void drawCommitDot(int x, int y, int w, int h) {
        graphicsContext.setFill((Paint) laneColor(this.plotCommit.getLane()));
        graphicsContext.fillOval(x, y, w, h);
        graphicsContext.strokeOval(x, y, w + 1, h + 1);
    }

    @Override
    protected void drawBoundaryDot(int x, int y, int w, int h) {
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillOval(x, y, w, h);
        graphicsContext.strokeOval(x, y, w + 1, h + 1);
    }

    @Override
    protected void drawText(String msg, int x, int y) {

    }

    public void render(GraphicsContext graphicsContext, PlotCommit plotCommit, int height) {
        this.graphicsContext = graphicsContext;
        this.plotCommit = plotCommit;
        this.paintCommit(plotCommit, height);
    }


}
