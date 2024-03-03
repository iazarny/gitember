package com.az.gitember.control;


import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Pair;
import com.az.gitember.data.Side;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
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

    private DoubleProperty max;
    private DoubleProperty blockIncrement;
    private DoubleProperty visibleAmount;
    private DoubleProperty value;
    private DoubleProperty unitIncrement;
    private DoubleProperty min;

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


    public void setMin(double m) {
        this.minProperty().set(m);
    }

    public double getMax() {
        return this.max == null ? 100.0D : this.max.get();
    }

    public double getVisibleAmount() {
        return this.visibleAmount == null ? 15.0D : this.visibleAmount.get();
    }


    public void setVisibleAmount(double d) {
        this.visibleAmountProperty().set(d);
    }


    public double getMin() {
        return min.get();
    }

    public DoubleProperty minProperty() {
        if (this.min == null) {
            this.min = new SimpleDoubleProperty(this, "min");
        }
        return min;
    }

    public DoubleProperty maxProperty() {

        if (this.max == null) {
            this.max = new SimpleDoubleProperty(this, "max", 100.0D);
        }

        return max;
    }

    public double getBlockIncrement() {
        return this.blockIncrement == null ? 10.0D : this.blockIncrement.get();
    }

    public DoubleProperty blockIncrementProperty() {
        if (this.blockIncrement == null) {
            this.blockIncrement = new SimpleDoubleProperty(10.0D);
        }

        return this.blockIncrement;
    }

    public DoubleProperty visibleAmountProperty() {
        if (this.visibleAmount == null) {
            this.visibleAmount = new SimpleDoubleProperty(this, "visibleAmount");
        }
        return visibleAmount;
    }

    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {

        if (this.value == null) {
            this.value = new SimpleDoubleProperty(this, "value");
        }

        return value;
    }

    public double getUnitIncrement() {
        return unitIncrement.get();
    }

    public DoubleProperty unitIncrementProperty() {
        if (this.unitIncrement == null) {
            unitIncrement = new SimpleDoubleProperty(1.0D);
        }
        return unitIncrement;
    }

    public DiffOverviewScrollBar() {
        super();

        setMinWidth(20);
        setMaxWidth(60);
        setPrefWidth(60);

        leftPane  = new Pane();
        rightPane  = new Pane();
        windowPane = new Pane();

        leftPane.getStyleClass().add("overview-border");
        rightPane.getStyleClass().add("overview-border");

        rectangle = new Rectangle(0,0, 220, 120);
        rectangle.setFill(Color.rgb(185,185,180,0.50));

        addColumn(1, leftPane);
        addColumn(2, rightPane);
        addColumn(3, windowPane);

        ColumnConstraints borderConstraints =  new ColumnConstraints();
        ColumnConstraints leftConstraints =  new ColumnConstraints();
        ColumnConstraints rightConstraints =  new ColumnConstraints();
        ColumnConstraints windowConstraints =  new ColumnConstraints();

        borderConstraints.setPercentWidth(2);
        leftConstraints.setPercentWidth(49);
        rightConstraints.setPercentWidth(49);
        windowConstraints.setPercentWidth(0);

        leftConstraints.setFillWidth(true);
        rightConstraints.setFillWidth(true);

        getColumnConstraints().add(0, borderConstraints);
        getColumnConstraints().add(1, leftConstraints);
        getColumnConstraints().add(2, rightConstraints);
        getColumnConstraints().add(3, windowConstraints);

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
        double layoutWidthHalf = layoutWidth/2;
        double layoutHeight = snapSizeY(getLayoutBounds().getHeight());

        resize(layoutWidth, layoutHeight);
        leftPane.resize(layoutWidthHalf, layoutHeight);
        rightPane.resize(layoutWidthHalf, layoutHeight);

        layoutLines(leftPane, leftLines,  leftMaxLine > 0 ? layoutWidthHalf / leftMaxLine : 0);
        layoutLines(rightPane, rightLines,  leftMaxLine > 0 ? layoutWidthHalf / rightMaxLine : 0);

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
     * Depict lines of text on correct position with correct size
     * @param pane
     * @param lineType
     * @param charWidth with if sinlge char in px
     */
    private void layoutLines(Pane pane, Pair<ArrayList<String>, ArrayList<Edit.Type>> lineType, double charWidth) {
        final double strokeSize = getLineThick();
        for (int i = 0; i < pane.getChildren().size(); i++) {
            final String text =   lineType.getFirst().get(i);
            if (text != null && text.length() > 0) {
                final Line line = (Line) pane.getChildren().get(i);
                final double y = i * strokeSize + strokeSize/2;
                line.setStartY(y);
                line.setEndY(y);
                line.setStartX(1);
                line.setEndX( charWidth * text.length());
                line.setStrokeWidth(strokeSize);
            }

        }
    }

    private void fillLines(Pane pane, Pair<ArrayList<String>, ArrayList<Edit.Type>> linesTypes) {
        ArrayList<String> lines = linesTypes.getFirst();
        ArrayList<Edit.Type> types = linesTypes.getSecond();
        for (int i = 0; i < lines.size(); i++) {
            Line l = new Line(-10000,0,-10000,0);
            l.setStroke(getColor(types.get(i), lines.get(i)));
            l.setStrokeLineCap(StrokeLineCap.BUTT);
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
