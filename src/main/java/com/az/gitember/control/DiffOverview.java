package com.az.gitember.control;

import com.az.gitember.data.Pair;
import com.az.gitember.data.Side;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Igor Azarny (iazarny@yahoo.com) on 30 - Oct - 2021
 */
public class DiffOverview extends GridPane {

    private Pane leftPane;
    private Pane rightPane;

    private String leftText;
    private String rightText;
    private EditList diffList;

    private ArrayList EMPTY_ARRAYLIST = new ArrayList();

    private Pair<ArrayList<String>, ArrayList<Edit.Type>> leftLines
            = new Pair(EMPTY_ARRAYLIST, EMPTY_ARRAYLIST);
    private Pair<ArrayList<String>, ArrayList<Edit.Type>> rightLines
            = new Pair(EMPTY_ARRAYLIST, EMPTY_ARRAYLIST);

    public DiffOverview() {
        super();

        leftPane  = new Pane();
        rightPane  = new Pane();

        //leftPane.setStyle("-fx-border-color: red; ");
        //rightPane.setStyle("-fx-border-color: blue; ");

        addColumn(0, leftPane);
        addColumn(1, rightPane);

        ColumnConstraints leftConstraints =  new ColumnConstraints();
        ColumnConstraints rightConstraints =  new ColumnConstraints();

        leftConstraints.setPercentWidth(50);
        rightConstraints.setPercentWidth(50);

        leftConstraints.setFillWidth(true);
        rightConstraints.setFillWidth(true);


        getColumnConstraints().add(0, leftConstraints);
        getColumnConstraints().add(1, rightConstraints);

        //setStyle("-fx-background-color: cyan");


    }



    public void setData(String leftText, String rightText, EditList diffList) {
        this.leftText = leftText;
        this.rightText = rightText;
        this.diffList = diffList;

        leftLines = GitemberUtil.getLines(leftText, diffList, Side.A);
        rightLines = GitemberUtil.getLines(rightText, diffList, Side.B);

        fillLines(leftPane, leftLines);
        fillLines(rightPane, rightLines);

    }

    /*



     */

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double layoutWidth = snapSizeX(getLayoutBounds().getWidth());
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());

        resize(layoutWidth, layoutHeight);
        leftPane.resize(layoutWidth/2, layoutHeight);
        rightPane.resize(layoutWidth/2, layoutHeight);

        layoutLines(leftPane, leftLines.getFirst(), layoutWidth/2);
        layoutLines(rightPane, rightLines.getFirst(), layoutWidth/2);

    }

    private void layoutLines(Pane pane, List<String> lines, double layoutWidth) {
        double strokeSize = getLineThick();
        int maxLineWidthInChar = lines.stream().mapToInt( String::length).max().orElseGet(() -> 0);
        double charWidth = 0;
        if (maxLineWidthInChar > 0) {
            charWidth = layoutWidth / maxLineWidthInChar;
        }
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Line line = (Line) pane.getChildren().get(i);
            String text =   lines.get(i);
            double y = i * strokeSize + strokeSize/2;
            line.setStartY(y);
            line.setEndY(y);
            line.setStartX(0);
            line.setEndX(charWidth * text.length());
            //line.setStrokeWidth(Math.max(1.0, strokeSize/4));
            line.setStrokeWidth(strokeSize);
        }
    }

    private void fillLines(Pane pane, Pair<ArrayList<String>, ArrayList<Edit.Type>> linesTypes) {
        ArrayList<String> lines = linesTypes.getFirst();
        ArrayList<Edit.Type> types = linesTypes.getSecond();
        for (int i = 0; i < lines.size(); i++) {
            Line l = new Line(0,0,0,0);
            //l.setStyle("-fx-stroke: green;");
            //l.setStyle("-fx-background-color: red");
            //l.getStyleClass().add("diff-row-new");
            l.setStroke(getColor(types.get(i)));
            pane.getChildren().add(l);
        }
    }

    private Color getColor(Edit.Type type) {
        if (type != null) {
            switch (type) {
                case DELETE:
                    return Color.RED;
                case INSERT:
                    return Color.GREEN;
                case REPLACE:
                    return Color.LIGHTGRAY;
                case EMPTY:
                    return Color.MAGENTA;

            }

        }
        return Color.DARKGREY;

    }

    private double getLineThick() {
        int lines = Math.max(Math.max(leftLines.getFirst().size(), rightLines.getFirst().size()), 1); // TODO not real
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());
        return layoutHeight / lines;
    }
}
