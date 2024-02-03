package com.az.gitember.controller;


import com.az.gitember.App;
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
public class CloneDialog extends Dialog<RemoteRepoParameters> {

    private final Label httpLoginLabel;
    private final TextField httpLogin;

    private final Label httpPasswordLabel;
    private final PasswordField httpPpassword;

    private RemoteRepoParameters cloneParameters = new RemoteRepoParameters();

    public CloneDialog(final String title,
                       final String header,
                       final Collection<String> urlHistory) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);


        this.getDialogPane().getStyleClass().add("text-input-dialog");
        ButtonType cloneBtn = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(cloneBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 15, 15, 25));

        TextField repositoryURL = new TextField();
        repositoryURL.setPromptText("URL");
        repositoryURL.setMinWidth(400);
        //repositoryURL.getEntries().addAll(urlHistory);

        TextField folder = new TextField();
        folder.setPromptText("Folder");
        HBox.setHgrow(folder, Priority.ALWAYS);

        Button selectFolder = new Button("...");
        HBox folderHBox = new HBox(folder, selectFolder);



        httpLoginLabel = new Label("Login");
        httpLogin = new TextField();

        httpPasswordLabel = new Label("Password");
        httpPpassword = new PasswordField();

        grid.add(new Label("URL : "), 0, 0);
        grid.add(repositoryURL, 1, 0);

        grid.add(new Label("Folder : "), 0, 1);
        grid.add(folderHBox, 1, 1);



        Node coneNodeButton = this.getDialogPane().lookupButton(cloneBtn);
        coneNodeButton.setDisable(true);

        selectFolder.setOnAction(
                event -> {
                    final DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    final File selectedDirectory =
                            directoryChooser.showDialog(App.getStage());
                    if (selectedDirectory != null) {
                        folder.setText(selectedDirectory.getAbsolutePath());
                    }
                }
        );




        grid.add(httpLoginLabel, 0, 2);
        grid.add(httpLogin, 1, 2);

        grid.add(httpPasswordLabel, 0, 3);
        grid.add(httpPpassword, 1, 3);

        grid.requestLayout();


        httpLoginLabel.setVisible(false);
        httpLogin.setVisible(false);
        httpPasswordLabel.setVisible(false);
        httpPpassword.setVisible(false);


        repositoryURL.textProperty().addListener((observable, oldValue, newValue) -> {
            String nw = newValue.trim();
            coneNodeButton.setDisable(nw.isEmpty());
            boolean gitSectionVisible = true;
            boolean httpSectionVisible = true;
            if (nw.startsWith("git@") || nw.startsWith("ssh")) {
                gitSectionVisible = true;
                httpSectionVisible = false;
            } else if (nw.startsWith("https:") || nw.startsWith("http:")) {
                gitSectionVisible = false;
                httpSectionVisible = true;
            } else {
                gitSectionVisible = false;
                httpSectionVisible = false;
            }


            httpLoginLabel.setVisible(httpSectionVisible);
            httpLogin.setVisible(httpSectionVisible);
            httpPasswordLabel.setVisible(httpSectionVisible);
            httpPpassword.setVisible(httpSectionVisible);

            grid.requestLayout();

        });

        Bindings.bindBidirectional(repositoryURL.textProperty(), cloneParameters.urlProperty());
        Bindings.bindBidirectional(folder.textProperty(), cloneParameters.destinationFolderProperty());
        Bindings.bindBidirectional(httpLogin.textProperty(), cloneParameters.userNameProperty());
        Bindings.bindBidirectional(httpPpassword.textProperty(), cloneParameters.userPwdProperty());

        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> repositoryURL.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == cloneBtn) {
                return cloneParameters;
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }
}
