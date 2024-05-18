package com.az.gitember.module.history.factory;

import com.az.gitember.module.history.renderer.HistoryPlotCommitRenderer;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

public class HistoryCommitsTableLaneCellFactory implements ObservableValue<Canvas> {

    private final PlotCommit<PlotLane> commit;
    private final HistoryPlotCommitRenderer historyPlotCommitRenderer;
    private final double canvasWidth;
    private final double canvasHeight;


    public HistoryCommitsTableLaneCellFactory(PlotCommit<PlotLane> commit,
                                              HistoryPlotCommitRenderer historyPlotCommitRenderer,
                                              double canvasHeight) {
        this.commit = commit;
        this.historyPlotCommitRenderer = historyPlotCommitRenderer;
        this.canvasWidth = 100 * canvasHeight;
        this.canvasHeight = canvasHeight;
    }

    @Override
    public Canvas getValue() {
        var canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        historyPlotCommitRenderer.render(gc, commit, (int) canvasHeight);
        return canvas;
    }

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }

    @Override
    public void addListener(ChangeListener<? super Canvas> listener) {
    }

    @Override
    public void removeListener(ChangeListener<? super Canvas> listener) {
    }
}
