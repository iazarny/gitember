package com.az.gitember.controller.history.factory;

import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.history.renderer.HistoryPlotCommitRenderer;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

public class HistoryCommitsTableLaneCellFactory implements ObservableValue<HBox> {

    private final PlotCommit<PlotLane> commit;
    private final HistoryPlotCommitRenderer historyPlotCommitRenderer;
    private final double canvasHeight;


    public HistoryCommitsTableLaneCellFactory(PlotCommit<PlotLane> commit,
                                              HistoryPlotCommitRenderer historyPlotCommitRenderer,
                                              double canvasHeight) {
        this.commit = commit;
        this.historyPlotCommitRenderer = historyPlotCommitRenderer;
        this.canvasHeight = canvasHeight;
    }

    @Override
    public HBox getValue() {
        var canvas = new Canvas(1000, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int actualWidth = historyPlotCommitRenderer.render(gc, commit, (int) canvasHeight);

        WritableImage croppedImage = new WritableImage(actualWidth, (int) canvasHeight);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        canvas.snapshot(sp, croppedImage);

        Canvas croppedCanvas = new Canvas(actualWidth, (int) canvasHeight);
        GraphicsContext croppedGc = croppedCanvas.getGraphicsContext2D();
        croppedGc.drawImage(croppedImage, 0, 0);

        Text text = new Text(commit.getShortMessage());
        HBox textWrapper = new HBox(text);
        textWrapper.setAlignment(Pos.CENTER);

        HBox labelHBox = new HBox();
        labelHBox.setStyle("-fx-border-color: transparent; -fx-border-width: 10px;");

        for (int i = 0; i < commit.getRefCount(); i++) {
            HBox labelWrapper = new HBox();
            labelWrapper.setStyle(LookAndFeelSet.HISTORY_LABEL_BOX_CSS);
            labelWrapper.setAlignment(Pos.CENTER_LEFT);
            Ref ref = commit.getRef(i);
            Text txtRef = new Text(ref.getName());
            txtRef.setStyle(LookAndFeelSet.HISTORY_LABEL_BOX_TXT_CSS);
            labelWrapper.getChildren().add(txtRef);
            labelHBox.getChildren().add(labelWrapper);
        }

        return new HBox(croppedCanvas, labelHBox, textWrapper);
    }

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }

    @Override
    public void addListener(ChangeListener<? super HBox> listener) {
    }

    @Override
    public void removeListener(ChangeListener<? super HBox> listener) {
    }
}
