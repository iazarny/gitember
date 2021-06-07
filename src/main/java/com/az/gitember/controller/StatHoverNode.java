package com.az.gitember.controller;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class StatHoverNode extends StackPane {

    private final Label label;

    public StatHoverNode(String key, int value) {
        this(key,value, 50, 15);
    }

    public StatHoverNode(String key, int value, int prefW, int prefH) {

        setPrefSize(prefW, prefH);

        String val = value == 0 ? "" : String.valueOf(value);

        label = new Label(key + " " + val);

        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getChildren().setAll(label);
                toFront();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getChildren().clear();
            }
        });
    }


}
