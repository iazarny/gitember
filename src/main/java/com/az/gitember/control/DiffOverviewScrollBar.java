package com.az.gitember.control;


import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Pair;
import com.az.gitember.data.Side;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.ArrayList;
import java.util.Objects;

/**
 * By Igor Azarny iazarny@yahoo.com 14-Nov-2021
 */
public class DiffOverviewScrollBar extends GridPane {

    private ScrollBar scrollBar = new ScrollBar();

    public void setOrientation(Orientation o) {
        this.scrollBar.setOrientation(o);
    }

    public void setMin(double m) {
        this.scrollBar.setMin(m);
    }

    public DoubleProperty maxProperty() {
        return this.scrollBar.maxProperty();
    }

    public DoubleProperty blockIncrementProperty() {
        return this.scrollBar.blockIncrementProperty();
    }

    public DoubleProperty visibleAmountProperty() {
        return this.scrollBar.visibleAmountProperty();
    }

    public DoubleProperty valueProperty() {
        return this.scrollBar.valueProperty();
    }

    public void setVisibleAmount(double d) {
        this.scrollBar.setVisibleAmount(d);
    }

    public DoubleProperty unitIncrementProperty() {
        return this.scrollBar.unitIncrementProperty();
    }

    public double getMax() {
        return this.scrollBar.getMax();
    }

    public double getVisibleAmount() {
        return this.scrollBar.getVisibleAmount();
    }












    private Pane leftPane;
    private Pane rightPane;
    private Pane windowPane;
    private Rectangle rectangle;

    private String leftText;
    private String rightText;
    private EditList diffList;

    private ArrayList EMPTY_ARRAYLIST = new ArrayList();

    private Pair<ArrayList<String>, ArrayList<Edit.Type>> leftLines
            = new Pair(EMPTY_ARRAYLIST, EMPTY_ARRAYLIST);
    private Pair<ArrayList<String>, ArrayList<Edit.Type>> rightLines
            = new Pair(EMPTY_ARRAYLIST, EMPTY_ARRAYLIST);
    private int leftMaxLine = 0 ;
    private int rightMaxLine = 0 ;

    private double windowOffset = 0;
    private DoubleProperty windowOffsetPercent = new ReadOnlyDoubleWrapper(0) ;
    private double windowHeight = 0 ;
    private double mousePressedInWindowY = 0 ;


    public DiffOverviewScrollBar() {
        super();

        leftPane  = new Pane();
        rightPane  = new Pane();
        windowPane = new Pane();

        rectangle = new Rectangle(0,0, 220, 120);
        rectangle.setFill(Color.rgb(185,185,180,0.50));


        addColumn(0, leftPane);
        addColumn(1, rightPane);
        addColumn(2, windowPane);


        ColumnConstraints leftConstraints =  new ColumnConstraints();
        ColumnConstraints rightConstraints =  new ColumnConstraints();
        ColumnConstraints windowConstraints =  new ColumnConstraints();

        leftConstraints.setPercentWidth(49);
        rightConstraints.setPercentWidth(49);
        windowConstraints.setPercentWidth(2);

        leftConstraints.setFillWidth(true);
        rightConstraints.setFillWidth(true);


        getColumnConstraints().add(0, leftConstraints);
        getColumnConstraints().add(1, rightConstraints);
        getColumnConstraints().add(2, windowConstraints);

        windowPane.getChildren().add(rectangle);


    }

    public double getWindowOffsetPercent() {
        return windowOffsetPercent.get();
    }

    public DoubleProperty windowOffsetPercentProperty() {
        return windowOffsetPercent;
    }

    public void setData(String leftText, String rightText, EditList diffList) {
        this.leftText = leftText;
        this.rightText = rightText;
        this.diffList = diffList;

        leftLines = GitemberUtil.getLines(leftText, diffList, Side.A);
        rightLines = GitemberUtil.getLines(rightText, diffList, Side.B);

        leftMaxLine = getMaxLen(leftLines.getFirst());
        rightMaxLine = getMaxLen(rightLines.getFirst());

        fillLines(leftPane, leftLines);
        fillLines(rightPane, rightLines);


        setOnMousePressed(event -> {
            if (event.getTarget() != rectangle) {
                double y = snapSizeY(event.getY());
                windowOffset = adjustWindowOffesstValue( y - windowHeight/2);
                windowOffsetPercent.setValue(windowOffset / snapSizeY(getLayoutBounds().getHeight()));
                layoutWindow();
            }
        });

        rectangle.setOnMousePressed(event -> {
            this.mousePressedInWindowY = event .getY() - windowOffset;
        });

        rectangle.setOnMouseDragged(event -> {

            windowOffset = (adjustWindowOffesstValue(event.getY() - mousePressedInWindowY));
            windowOffsetPercent.setValue(windowOffset / snapSizeY(getLayoutBounds().getHeight()));
            layoutWindow();
        });

    }

    private double adjustWindowOffesstValue(double val) {
        double newVal = val;
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());
        if (newVal + windowHeight > layoutHeight ) {
            newVal = layoutHeight  - windowHeight;
        }
        if (newVal < 0) {
            newVal =  0;
        }
        return newVal;
    }


    /**
     * Set height offset.
     * @param offset 0..1
     * @param size 0..1 relative height of visible content
     */
    public void setHilightPosSize(double offset, double size) {
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());
        windowOffset = (layoutHeight * offset);
        windowHeight = layoutHeight * size;
        layoutWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double layoutWidth = snapSizeX(getLayoutBounds().getWidth());
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());

        resize(layoutWidth, layoutHeight);
        leftPane.resize(layoutWidth/2, layoutHeight);
        rightPane.resize(layoutWidth/2, layoutHeight);

        layoutLines(leftPane, leftLines, layoutWidth/2, leftMaxLine);
        layoutLines(rightPane, rightLines, layoutWidth/2, rightMaxLine);

        layoutWindow();

    }


    /**
     * Move highlight rectangle to the specified vertical position and set size.
     */
    private void layoutWindow() {

        double layoutWidth = snapSizeX(getLayoutBounds().getWidth());
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());

        rectangle.setX(-1 * layoutWidth);
        rectangle.setWidth(layoutWidth);

        rectangle.setY(windowOffset);
        rectangle.setHeight(windowHeight);
    }

    /**
     * Depict lines of text on correc position with correct size
     * @param pane
     * @param lineType
     * @param layoutWidth
     * @param maxWidth
     */
    private void layoutLines(Pane pane, Pair<ArrayList<String>, ArrayList<Edit.Type>> lineType, double layoutWidth, int maxWidth) {
        double strokeSize = getLineThick();

        double charWidth = 0;
        if (maxWidth > 0) {
            charWidth = layoutWidth / maxWidth;
        }
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Line line = (Line) pane.getChildren().get(i);

            String text =   lineType.getFirst().get(i);

            double y = i * strokeSize + strokeSize/2;
            line.setStartY(y);
            line.setEndY(y);
            line.setStartX(0);

            if (text == null) {
                line.setEndX(0);
            } else {
                line.setEndX( charWidth * text.length());
            }
            line.setStrokeWidth(strokeSize);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
        }
    }

    private void fillLines(Pane pane, Pair<ArrayList<String>, ArrayList<Edit.Type>> linesTypes) {
        ArrayList<String> lines = linesTypes.getFirst();
        ArrayList<Edit.Type> types = linesTypes.getSecond();
        for (int i = 0; i < lines.size(); i++) {
            Line l = new Line(0,0,0,0);
            l.setStroke(getColor(types.get(i), lines.get(i)));
            pane.getChildren().add(l);
        }
    }

    private Color getColor(Edit.Type type, String str) {
        if (str == null) {
            return Color.WHITE;
        } else if (type != null) {
            switch (type) {
                case DELETE:
                    return LookAndFeelSet.DIFF_COLOR_DELETE;
                case INSERT:
                    return LookAndFeelSet.DIFF_COLOR_INSERT;
                case REPLACE:
                    return LookAndFeelSet.DIFF_COLOR_REPLACE;
                case EMPTY:
                    return Color.MAGENTA;
            }
        }
        return LookAndFeelSet.DIFF_COLOR_TEXT;
    }

    private double getLineThick() {
        int lines = Math.max(Math.max(leftLines.getFirst().size(), rightLines.getFirst().size()), 1); // TODO not real
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());
        return layoutHeight / lines;
    }

    private int getMaxLen(ArrayList<String> lines) {
        return  lines.stream().filter(Objects::nonNull).mapToInt( String::length).max().orElseGet(() -> 0);
    }

}
