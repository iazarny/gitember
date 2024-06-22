package com.az.gitember.controller.history.renderer;

import com.az.gitember.controller.LookAndFeelSet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import java.util.HashMap;

public class HistoryPlotCommitRenderer extends AbstractPlotRenderer<PlotLane, Color>  {

    private final HashMap<PlotLane, Color> plotLaneColorHashMap = new HashMap<>();
    private GraphicsContext graphicsContext;
    private PlotCommit<PlotLane> plotCommit;

    private int actuallWidth = 100;


    @Override
    protected int drawLabel(int x, int y, Ref ref) {
        /*var hbox = new HBox();
        hbox.setStyle(LookAndFeelSet.HISTORY_LABEL_BOX_CSS);

        var txtNum = new Text(ref.getName());
        txtNum.setStyle(LookAndFeelSet.HISTORY_LABEL_BOX_TXT_CSS);
        hbox.getChildren().add(txtNum);

        // Little hack - Node.snapshot evaluate CSS only if attached to Scene
        new Scene(hbox);

        var parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        var img = hbox.snapshot(parameters, null);
        graphicsContext.drawImage(img, x, y - (hbox.getBoundsInLocal().getHeight() / 2));

        return (int) hbox.getBoundsInLocal().getWidth() + 10;*/
        return 1 ; //ref.getName().length() * 10 + 10;
    }

    @Override
    protected void drawText(String msg, int x, int y) {
        actuallWidth = x;
        // So x is actual width
        /*var hbox = new HBox();
        hbox.setStyle(LookAndFeelSet.HISTORY_BOX_CSS);
        hbox.setPadding(new Insets(0, 8, 0, 8));

        var txtNum = new Text(msg);
        txtNum.setStyle(LookAndFeelSet.HISTORY_BOX_TXT_CSS);
        hbox.getChildren().add(txtNum);

        // Little hack - Node.snapshot evaluate CSS only if attached to Scene
        new Scene(hbox);

        var parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        var img = hbox.snapshot(parameters, null);
        graphicsContext.drawImage(img, x, 0 + (hbox.getBoundsInLocal().getHeight() / 2));*/
    }

    @Override
    protected void drawLine(Color color, int x1, int y1, int x2, int y2, int width) {
        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(width);

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
        var color =  laneColor(this.plotCommit.getLane());

        if (this.plotCommit.getChildCount() == 0) {
            graphicsContext.setFill(LookAndFeelSet.BRANCH_NAME_COLOR);
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
        plotLaneColorHashMap.computeIfAbsent(
                myLane,
                k -> LookAndFeelSet.historyPlotCommitRenderedColors[(1 + plotLaneColorHashMap.size()) % LookAndFeelSet.historyPlotCommitRenderedColors.length]
        );
        return plotLaneColorHashMap.get(myLane);
    }


    public int  render(GraphicsContext graphicsContext, PlotCommit<PlotLane> plotCommit, int height) {
        this.graphicsContext = graphicsContext;
        this.plotCommit = plotCommit;
        this.paintCommit(plotCommit, height);
        return this.actuallWidth;
    }
}
