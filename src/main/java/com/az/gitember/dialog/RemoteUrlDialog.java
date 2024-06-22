package com.az.gitember.dialog;

import com.az.gitember.App;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class RemoteUrlDialog extends Dialog<String> {

    private TextField remoteUrl;
    private StringProperty remoteUrlProperty = new SimpleStringProperty();

    public RemoteUrlDialog(final String title,
                           final String header,
                           final String currentRemoteUrl
    ) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        this.remoteUrlProperty.setValue(currentRemoteUrl);
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.remoteUrl = new TextField();

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(new Label("Remote URL"), 0, 0);
        grid.add(remoteUrl, 1, 0);

        remoteUrl.setPrefWidth(400);

        Bindings.bindBidirectional(remoteUrl.textProperty(), remoteUrlProperty);

        this.getDialogPane().setContent(grid);
        grid.requestLayout();

        Platform.runLater(() -> remoteUrl.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return remoteUrlProperty.getValueSafe();
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }
}