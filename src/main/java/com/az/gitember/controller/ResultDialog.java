package com.az.gitember.controller;

import com.az.gitember.App;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultDialog extends Dialog {

    private final static Logger log = Logger.getLogger(Main.class.getName());


    public ResultDialog(String title, String header, String text, Alert.AlertType alertType, Scene sceneOwner) {
        super();

        final DialogPane dialogPane = getDialogPane();

        setTitle(title);
        setHeaderText(header);
        setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);

        TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);

        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);

        dialogPane.setMinHeight(250);
        dialogPane.setContent(gridPane);
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        if (Alert.AlertType.WARNING == alertType || Alert.AlertType.ERROR == alertType) {
            log.log(Level.WARNING, text);
        }

        initOwner(sceneOwner.getWindow());
    }


    public ResultDialog(String title, String text, Alert.AlertType alertType) {
        this(title,"",  text, alertType, App.getScene());
    }
}
