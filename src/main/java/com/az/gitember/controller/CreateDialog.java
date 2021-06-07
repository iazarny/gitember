package com.az.gitember.controller;


import com.az.gitember.App;
import com.az.gitember.data.InitRepoParameters;
import com.az.gitember.data.RemoteRepoParameters;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Collection;

/**
 * Created by Igor_Azarny on 29 - Dec - 2016.
 */
public class CreateDialog extends Dialog<InitRepoParameters> {


    private InitRepoParameters cloneParameters = new InitRepoParameters();

    public CreateDialog(final String title,
                        final String header) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);

        final ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        final TextField folder = new TextField();
        folder.setPromptText("Folder");
        HBox.setHgrow(folder, Priority.ALWAYS);
        final CheckBox initWithFiles = new CheckBox();

        final Button selectFolder = new Button("...");
        final HBox folderHBox = new HBox(folder, selectFolder);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        grid.add(new Label("Folder : "), 0, 1);
        grid.add(folderHBox, 1, 1);

        grid.add(new Label("Init with README.md : "), 0, 2);
        grid.add(initWithFiles, 1, 2);

        final Node coneNodeButton = this.getDialogPane().lookupButton(createBtn);
        coneNodeButton.setDisable(true);

        selectFolder.setOnAction(
                event -> {
                    final DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    final File selectedDirectory =
                            directoryChooser.showDialog(App.getStage());
                    if (selectedDirectory != null) {
                        folder.setText(selectedDirectory.getAbsolutePath());
                        coneNodeButton.setDisable(false);
                    }
                }
        );

        folder.textProperty().addListener((observable, oldValue, newValue) -> {
                    String nw = newValue.trim();
                    coneNodeButton.setDisable(nw.isEmpty());
                });

        grid.requestLayout();

        Bindings.bindBidirectional(folder.textProperty(), cloneParameters.destinationFolderProperty());
        Bindings.bindBidirectional(initWithFiles.selectedProperty(), cloneParameters.initWithFilesProperty());

        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> folder.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == createBtn) {
                return cloneParameters;
            }
            return null;
        });


        this.initOwner(App.getScene().getWindow());

    }
}
