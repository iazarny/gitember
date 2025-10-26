package com.az.gitember.dialog;

import com.az.gitember.App;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileCompareDialog extends Dialog<List<File>> {

    private TextField leftFileField;
    private TextField rightFileField;
    private Button leftFileButton;
    private Button rightFileButton;
    private Button swapButton;

    public FileCompareDialog(String title, String header) {
        super();
        this.setTitle(title);
        this.setHeaderText(header);
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Left file
        Label leftFileLabel = new Label("Left File:");
        grid.add(leftFileLabel, 0, 0);

        leftFileField = new TextField();
        leftFileField.setEditable(false);
        leftFileField.setPromptText("Drop or select left file...");
        HBox.setHgrow(leftFileField, Priority.ALWAYS);

        leftFileButton = new Button("Browse");
        leftFileButton.setOnAction(e -> selectFile(leftFileField));

        HBox leftFileHBox = new HBox(5, leftFileField, leftFileButton);
        grid.add(leftFileHBox, 1, 0);
        GridPane.setHgrow(leftFileHBox, Priority.ALWAYS);

        // Right file
        Label rightFileLabel = new Label("Right File:");
        grid.add(rightFileLabel, 0, 1);

        rightFileField = new TextField();
        rightFileField.setEditable(false);
        rightFileField.setPromptText("Drop or select right file...");
        HBox.setHgrow(rightFileField, Priority.ALWAYS);

        rightFileButton = new Button("Browse");
        rightFileButton.setOnAction(e -> selectFile(rightFileField));

        HBox rightFileHBox = new HBox(5, rightFileField, rightFileButton);
        grid.add(rightFileHBox, 1, 1);
        GridPane.setHgrow(rightFileHBox, Priority.ALWAYS);

        // Swap button
        swapButton = new Button("Swap");
        swapButton.setTooltip(new Tooltip("Swap left and right files"));
        swapButton.setOnAction(e -> swapFiles());
        swapButton.setGraphic(
                new StackedFontIcon() {
                    {
                        getChildren().add(new FontIcon("fa-exchange"));
                    }
                }
        );

        // Drop zone info


        grid.add(swapButton, 1, 2);

        setupDragAndDrop(leftFileField);
        setupDragAndDrop(rightFileField);

        this.getDialogPane().setContent(grid);

        // Enable/disable OK button based on whether both files are selected
        Node okButton = this.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        leftFileField.textProperty().addListener((obs, oldVal, newVal) -> updateOkButton(okButton));
        rightFileField.textProperty().addListener((obs, oldVal, newVal) -> updateOkButton(okButton));

        Platform.runLater(() -> leftFileField.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                List<File> files = new ArrayList<>();
                files.add(new File(leftFileField.getText()));
                files.add(new File(rightFileField.getText()));
                return files;
            }
            return null;
        });
    }

    private void setupDragAndDrop(TextField textField) {
        textField.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        textField.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (files.size() == 1) {
                    File file = files.get(0);
                    if (file.isFile()) {
                        textField.setText(file.getAbsolutePath());
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void selectFile(TextField targetField) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select File to Compare");
        File selectedFile = fileChooser.showOpenDialog(App.getStage());
        if (selectedFile != null) {
            targetField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void swapFiles() {
        String temp = leftFileField.getText();
        leftFileField.setText(rightFileField.getText());
        rightFileField.setText(temp);
    }

    private void updateOkButton(Node okButton) {
        boolean leftSet = leftFileField.getText() != null && !leftFileField.getText().trim().isEmpty();
        boolean rightSet = rightFileField.getText() != null && !rightFileField.getText().trim().isEmpty();
        okButton.setDisable(!leftSet || !rightSet);
    }
}

