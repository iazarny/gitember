package com.az.gitember.ui;

import com.az.gitember.MainApp;
import com.az.gitember.misc.Pair;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.UUID;

/**
 * Created by Igor_Azarny on 29 - Dec - 2016.
 */
public class CloneDialog extends Dialog<Pair<String, String>> {

    public CloneDialog(String title, String header) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo


        ButtonType loginButtonType = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        TextField repositoryURL = new TextField();
        repositoryURL.setPromptText("URL");
        repositoryURL.setMinWidth(400);
        repositoryURL.setText("https://127.0.0.1/root/test.git");

        TextField folder = new TextField();
        folder.setPromptText("Folder");
        folder.setText("C:\\Users\\Igor_Azarny\\aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\\" + UUID.randomUUID().toString());
        HBox.setHgrow(folder, Priority.ALWAYS);

        Button selectFolder = new Button("...");
        HBox folderHBox = new HBox(folder, selectFolder);

        grid.add(new Label("URL : "), 0, 0);
        grid.add(repositoryURL, 1, 0);

        grid.add(new Label("Folder : "), 0, 1);
        grid.add(folderHBox, 1, 1);

        Node loginButton = this.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        selectFolder.setOnAction(
                event -> {
                    final DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(new File(MainApp.getSettingsService().getUserHomeFolder()));
                    final File selectedDirectory =
                            directoryChooser.showDialog(MainApp.getMainStage());
                    if (selectedDirectory != null) {
                        folder.setText(selectedDirectory.getAbsolutePath());
                    }
                }
        );

        repositoryURL.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() /*|| folder.getText().isEmpty()*/);
        });

        /*folder.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || repositoryURL.getText().isEmpty());
        });*/

        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> repositoryURL.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(repositoryURL.getText(), folder.getText());
            }
            return null;
        });
    }
}
