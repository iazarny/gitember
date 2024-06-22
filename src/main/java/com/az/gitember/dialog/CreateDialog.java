package com.az.gitember.dialog;


import com.az.gitember.App;
import com.az.gitember.data.InitRepoParameters;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;

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
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        final ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        final TextField folder = new TextField();
        folder.setPromptText("Folder");
        HBox.setHgrow(folder, Priority.ALWAYS);
        final CheckBox initWithReadme = new CheckBox();
        final CheckBox initWithIgnore = new CheckBox();
        final CheckBox initWithLfs = new CheckBox();

        final Button selectFolder = new Button("...");
        final HBox folderHBox = new HBox(folder, selectFolder);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        grid.add(new Label("Folder : "), 0, 1);
        grid.add(folderHBox, 1, 1);

        grid.add(new Label("Init with README.md : "), 0, 2);
        grid.add(initWithReadme, 1, 2);
        grid.add(new Label("Init with .gitignore : "), 0, 3);
        grid.add(initWithIgnore, 1, 3);
        grid.add(new Label("Init with LFS : "), 0, 4);
        grid.add(initWithLfs, 1, 4);

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
        Bindings.bindBidirectional(initWithReadme.selectedProperty(), cloneParameters.initWithReameProperty());
        Bindings.bindBidirectional(initWithIgnore.selectedProperty(), cloneParameters.initWithIgnoreProperty());
        Bindings.bindBidirectional(initWithLfs.selectedProperty(), cloneParameters.initWithLfsProperty());

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
