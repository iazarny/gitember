package com.az.gitember.controller;

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

public class StringDialog extends Dialog<String> {

    private TextField valueTextField;
    private StringProperty valueProperty = new SimpleStringProperty();

    public StringDialog(final String title,
                        final String header,
                        final String valueName,
                        final String valueToEdit
    ) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        this.valueProperty.setValue(valueToEdit);
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.valueTextField = new TextField();

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(new Label(valueName), 0, 0);
        grid.add(valueTextField, 1, 0);

        valueTextField.setPrefWidth(400);

        Bindings.bindBidirectional(valueTextField.textProperty(), valueProperty);

        this.getDialogPane().setContent(grid);
        grid.requestLayout();

        Platform.runLater(() -> valueTextField.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return valueProperty.getValueSafe();
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }
}