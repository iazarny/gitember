package com.az.gitember.controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import java.util.HashMap;

public class HistoryPlotCommitRenderer extends AbstractPlotRenderer {



    private HashMap<PlotLane, Color> plotLaneColorHashMap = new HashMap<>();

    private GraphicsContext graphicsContext;

    private PlotCommit plotCommit;

    @Override
    protected int drawLabel(int x, int y, Ref ref) {
        return 0;
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
                graphicsContext.bezierCurveTo(x1 - 10, y1, x2, y2, x2, y2);
            } else {
                graphicsContext.bezierCurveTo(x1 - 10, y1, x2, y2, x2, y2);
            }
        } else if (x1 < x2) {
            if (y1 < y2) {
                graphicsContext.bezierCurveTo(x1 + 10, y1, x2 -1, y2, x2, y2);
            } else {
                graphicsContext.bezierCurveTo(x1 + 10, y1 , x2 -1, y2, x2, y2);
            }
        }
        graphicsContext.stroke();
        graphicsContext.closePath();

    }

    @Override
    protected void drawCommitDot(int x, int y, int w, int h) {

        Color color =  laneColor(this.plotCommit.getLane());
        if (this.plotCommit.getChildCount() == 0) {

            graphicsContext.setFill(LookAndFeelSet.BRANCH_NAME_COLOR);
            graphicsContext.fillText( getText(plotCommit) , x + w , y);

            graphicsContext.setFill(color.darker());
        } else {
            graphicsContext.setFill(color);
        }
        graphicsContext.fillOval(x, y, w+1, h+1);
        graphicsContext.strokeOval(x, y, w + 2, h + 2);
    }


    @Override
    protected void drawBoundaryDot(int x, int y, int w, int h) {
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillOval(x, y, w+1, h+1);
        graphicsContext.strokeOval(x, y, w + 2, h + 2);
    }

    @Override
    protected Color laneColor(PlotLane myLane) {
        if (plotLaneColorHashMap.get(myLane) == null) {
            plotLaneColorHashMap.put(
                    myLane,
                    LookAndFeelSet.historyPlotCommitRenderedColors[ (1 + plotLaneColorHashMap.size()) % LookAndFeelSet.historyPlotCommitRenderedColors.length   ]
            );
        }
        return plotLaneColorHashMap.get(myLane);
    }

    private String getText(PlotCommit plotCommit) {
        String rez = plotCommit.getRef(0).getName();
        for (int i = 0; i < plotCommit.getRefCount(); i++) {
            if (plotCommit.getRef(i).getName().startsWith("refs/heads")) {
                rez = plotCommit.getRef(i).getName();
                break;
            }
        }
        return rez;
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
