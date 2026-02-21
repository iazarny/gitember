package com.az.gitember.dialog;

import com.az.gitember.App;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;

import java.util.regex.Pattern;

public class IntegerlDialog extends Dialog<Integer> {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d*");
    private static final Pattern NON_DIGITS = Pattern.compile("[^\\d]");

    private TextField integerField;

    public IntegerlDialog(final String title,
                          final String header,
                          final String fieldName,
                          final int currentValue
    ) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.integerField = new TextField();
        this.integerField.setText("" + currentValue);
        this.integerField.setEditable(true);
        this.integerField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!DIGITS_ONLY.matcher(newValue).matches()) {
                    integerField.setText(NON_DIGITS.matcher(newValue).replaceAll(""));
                }
            }
        });

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(new Label(fieldName), 0, 0);
        grid.add(integerField, 1, 0);

        integerField.setPrefWidth(100);

        this.getDialogPane().setContent(grid);
        grid.requestLayout();

        Platform.runLater(() -> integerField.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return Integer.valueOf("0" + integerField.getText());
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }
}