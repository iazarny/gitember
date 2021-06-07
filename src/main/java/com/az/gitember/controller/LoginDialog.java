package com.az.gitember.controller;


import com.az.gitember.App;
import com.az.gitember.data.RemoteRepoParameters;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by Igor_Azarny on 25 - Dec -2016.
 */
public class LoginDialog extends Dialog<RemoteRepoParameters> {

    public LoginDialog(String title, String header,
                       RemoteRepoParameters repoInfo) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo

        ButtonType okButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        TextField username = new TextField();
        username.setPromptText("Username");
        HBox.setHgrow(username, Priority.ALWAYS);
        if (repoInfo.getUserName() != null) {
            username.setText(repoInfo.getUserName());
        }
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        if (repoInfo.getUserPwd() != null) {
            password.setText(repoInfo.getUserPwd());
        }

        grid.add(new Label("Username : "), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password : "), 0, 1);
        grid.add(password, 1, 1);


        Node loginButton = this.getDialogPane().lookupButton(okButtonType);
        loginButton.setDisable(username.textProperty().isEmpty().get());

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });


        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> username.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                repoInfo.setUserName(username.getText());
                repoInfo.setUserPwd(password.getText());
                return repoInfo;
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }


}
