package com.az.gitember.controller;

import com.az.gitember.data.SquarePos;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.*;
import javafx.scene.text.TextFlow;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class DiffViewer implements Initializable {

    public TextField newLabel;
    public TextField oldLabel;
    public TextFlow oldTextFlow;
    public TextFlow newTextFlow;
    public Pane diffDrawPanel;
    public Pane mainPanel;
    public ScrollPane oldScrollPane;
    public ScrollPane newScrollPane;

    private String oldText = null;
    private String newText = null;
    private EditList diffList = new EditList();

    private double fontSize;

    public void setData(String  oldFileName, String newFileName) throws IOException {

        this.oldText = Files.readString(Paths.get(oldFileName));
        this.newText = Files.readString(Paths.get(newFileName));

        RawText oldRawTet = new RawText(new File(oldFileName)); //TODO use this constructor RawText(byte[] input)
        RawText newRawTet = new RawText(new File(newFileName));

        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        RawTextComparator comparator = RawTextComparator.DEFAULT;

        diffList.addAll(diffAlgorithm.diff(comparator, oldRawTet, newRawTet));

        setText(oldTextFlow,  oldText, oldFileName, true);
        setText(newTextFlow,  newText, newFileName, false);
        createPathElements();

    }


    public void setOldLabel(String text) {
        oldLabel.setText(text);
        oldLabel.setEditable(false);
        oldLabel.getStyleClass().add("copy-label");
    }

    public void setNewLabel(String text) {
        newLabel.setText(text);
        newLabel.setEditable(false);
        newLabel.getStyleClass().add("copy-label");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        //scene.getStylesheets().add(this.getClass().getResource(Const.DEFAULT_CSS).toExternalForm());
        if (Context.isWindows() ) {
            fontSize = TextBrowserContentAdapter.FONT_SIZE + 4.0125; // windows
        } else {
            fontSize = TextBrowserContentAdapter.FONT_SIZE + 4.88; // linux
        }

        oldScrollPane.vvalueProperty().addListener((ObservableValue<? extends Number> ov,
                                                    Number old_val, Number new_val) -> {
            newScrollPane.setVvalue(new_val.doubleValue());
            updatePathElements();
        });

        newScrollPane.vvalueProperty().addListener((ObservableValue<? extends Number> ov,
                                                    Number old_val, Number new_val) -> {
            oldScrollPane.setVvalue(new_val.doubleValue());
            updatePathElements();
        });

        newScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater( () -> updatePathElements() );
        });

    }

    private SquarePos getDiffPos(Edit delta) {



        final int origPos = delta.getBeginA();
        final int origLines = delta.getLengthA();
        final int revPos = delta.getBeginB();
        final int revLines = delta.getLengthB();

        int origBottomPos = origPos + origLines;
        int revBottomPos = revPos + revLines;

        double deltaY1 = (oldTextFlow.getBoundsInParent().getMaxY()  - oldScrollPane.getViewportBounds().getHeight())
                * oldScrollPane.getVvalue();
        double deltaY2 = (newTextFlow.getBoundsInParent().getMaxY()  - newScrollPane.getViewportBounds().getHeight())
                * newScrollPane.getVvalue();

        int x1 = -1;
        int y1 = (int) (origPos * fontSize - deltaY1);

        int x2 = (int) diffDrawPanel.getWidth() + 1;
        int y2 = (int) (revPos * fontSize - deltaY2);

        int x3 = x2;
        int y3 = (int) (revBottomPos * fontSize - deltaY2);

        int x4 = x1;
        int y4 = (int) (origBottomPos * fontSize - deltaY1);

        return new SquarePos(x1,y1,x2,y2,x3,y3,x4,y4);

    }

    private void updatePathElements() {
        for (int i = 0; i < diffDrawPanel.getChildren().size(); i++) {
            final Path path = (Path) diffDrawPanel.getChildren().get(i);

            final Edit delta = this.diffList.get(i);

            SquarePos squarePos = getDiffPos(delta);

            MoveTo moveTo = (MoveTo) path.getElements().get(0);
            CubicCurveTo curve0 = (CubicCurveTo) path.getElements().get(1);
            LineTo lineTo0 = (LineTo) path.getElements().get(2);
            CubicCurveTo curve1 = (CubicCurveTo) path.getElements().get(3);
            LineTo lineTo1 = (LineTo) path.getElements().get(4);

            moveTo.setX(squarePos.getX1());
            moveTo.setY(squarePos.getY1());

            CubicCurveTo ccTo =  getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY2());
            curve0.setX(squarePos.getX2());
            curve0.setY(squarePos.getY2());
            curve0.setControlX1(ccTo.getControlX1());
            curve0.setControlX2(ccTo.getControlX2());
            curve0.setControlY1(ccTo.getControlY1());
            curve0.setControlY2(ccTo.getControlY2());


            lineTo0.setX(squarePos.getX3());
            lineTo0.setY(squarePos.getY3());

            ccTo =  getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
            curve1.setX(squarePos.getX4());
            curve1.setY(squarePos.getY4());
            curve1.setControlX1(ccTo.getControlX1());
            curve1.setControlX2(ccTo.getControlX2());
            curve1.setControlY1(ccTo.getControlY1());
            curve1.setControlY2(ccTo.getControlY2());

            lineTo1.setX(squarePos.getX1());
            lineTo1.setY(squarePos.getY1());

        }
    }


    private void createPathElements() {

        for (Edit delta : this.diffList) {
            SquarePos squarePos = getDiffPos(delta);


            MoveTo moveTo = new MoveTo(squarePos.getX1(), squarePos.getY1());
            CubicCurveTo curve0 = getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY4());
            LineTo lineTo0 = new LineTo(squarePos.getX3(), squarePos.getY3());
            CubicCurveTo curve1 = getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
            LineTo lineTo1 = new LineTo(squarePos.getX1(), squarePos.getY1());

            Path path = new Path();
            path.getElements().addAll(moveTo, curve0, lineTo0, curve1, lineTo1);
            path.setStroke(LookAndFeelSet.DIFF_STROKE_COLOR);
            path.setStrokeWidth(1);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setFill(LookAndFeelSet.DIFF_FILL_COLOR);
            diffDrawPanel.getChildren().add(path);
        }
    }

    private CubicCurveTo getCubicCurveTo(int x1, int y1, int x2, int y2) {
        int controlPointDeltaX = 15;
        return new CubicCurveTo(
                x2 > x1 ? diffDrawPanel.getWidth() - controlPointDeltaX : controlPointDeltaX, y1,
                x2 > x1 ? controlPointDeltaX : diffDrawPanel.getWidth() - controlPointDeltaX, y2,
                x2, y2);
    }




    private void setText(final TextFlow textFlow,
                         final String text, final String fileName, final boolean leftSide) {

        textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        textFlow.setMinWidth(Region.USE_PREF_SIZE);
        textFlow.setLineSpacing(-2.2);

        TextBrowserContentAdapter adapter = new TextBrowserContentAdapter(
                FilenameUtils.getExtension(fileName),
                this.diffList,
                leftSide,
                true, LookAndFeelSet.DIFF_FILL_COLOR);

        textFlow.getChildren().addAll(
                adapter.getText(text)
        );

    }


}
