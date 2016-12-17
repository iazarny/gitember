package com.az.gitember.ui;


import java.util.Collections;
import java.util.function.IntFunction;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;
import org.reactfx.value.Val;
/**
 * Thanks to Tomas Mikula.
 */
public class ArrowFactory implements IntFunction<Node> {
    private final ObservableValue<Integer> shownLine;
    private final StyledTextArea<?, ?> area;

    public ArrowFactory(StyledTextArea<?, ?> area) {
        this.area  =area;
        this.shownLine = area.currentParagraphProperty();
    }

    @Override
    public Node apply(int lineNumber) {
        Polygon triangle = new Polygon(0.0, 0.0, 40.0, 5.0, 0.0, 10.0);
        triangle.setFill(Color.GREEN);

        ObservableValue<Boolean> visible = Val.map(
                shownLine,
                sl -> sl == lineNumber);

        triangle.visibleProperty().bind(
                Val.flatMap(triangle.sceneProperty(), scene -> {
                    return scene != null ? visible : Val.constant(false);
                })
        );

        return triangle;
    }
}