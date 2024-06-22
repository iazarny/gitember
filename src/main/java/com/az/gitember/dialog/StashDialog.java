package com.az.gitember.dialog;

import com.az.gitember.App;
import com.az.gitember.data.StashDialogResult;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StashDialog extends Dialog<StashDialogResult> {

    public StashDialog() {
        super();
        setTitle("Stash");
        getDialogPane().getStyleClass().add("text-input-dialog");

        final VBox vbox = new VBox();
        vbox.setSpacing(10);

        final TextField messageText = new TextField();
        GridPane.setHgrow(messageText, Priority.ALWAYS);
        GridPane.setFillWidth(messageText, true);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        vbox.getChildren().add(new Label("Message"));
        vbox.getChildren().add(messageText);

        getDialogPane().setContent(vbox);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new StashDialogResult(
                        messageText.getText()
                );
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());
        Platform.runLater(() -> messageText.requestFocus());

    }

}
