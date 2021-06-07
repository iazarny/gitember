package com.az.gitember.controller.handlers;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;

import java.util.function.Consumer;

public class LongTaskFinishHandler implements EventHandler<WorkerStateEvent> {

    private final Scene scene;
    private final ProgressBar progressBar;
    private final Label operationName;
    private final ToolBar toolBar;
    private final Consumer resuConsumer;

    public LongTaskFinishHandler(Scene scene, ProgressBar progressBar, Label operationName, ToolBar toolBar, Consumer resuConsumer) {
        this.scene = scene;
        this.progressBar = progressBar;
        this.operationName = operationName;
        this.toolBar = toolBar;
        this.resuConsumer = resuConsumer;
    }

    @Override
    public void handle(WorkerStateEvent event) {
        scene.setCursor(Cursor.DEFAULT);
        progressBar.progressProperty().unbind();
        operationName.textProperty().unbind();
        toolBar.setVisible(false);
        if (resuConsumer != null) {
            resuConsumer.accept(event);
        }
    }

}
