package com.az.gitember.dialog;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.logging.Level;

public class ResultDialog extends Dialog {

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




        String style = "";
        if (Alert.AlertType.WARNING == alertType) {
            style = LookAndFeelSet.RESULT_WARNING;
        } else if (Alert.AlertType.ERROR == alertType) {
            style = LookAndFeelSet.RESULT_ERROR;
        }

        gridPane.setStyle(style);
        textArea.setStyle(style);
        dialogPane.setStyle(style);



        initOwner(sceneOwner.getWindow());
    }


    public ResultDialog(String title, String text, Alert.AlertType alertType) {
        this(title,"",  text, alertType, App.getScene());
    }
}
