package com.az.gitember.ui;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;

/**
 * Created by Igor_Azarny on 25 - Dec -2016.
 */
public class LoginDialog extends Dialog<Pair<String, String>> {

    public LoginDialog(String title, String header, String login, String pwd) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        TextField username = new TextField();
        username.setPromptText("Username");
        if (login != null) {
            username.setText(login);
        }
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        if (pwd!=null) {
            password.setText(pwd);
        }

        grid.add(new Label("Username : "), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password : "), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = this.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(username.textProperty().isEmpty().get());

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });


        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> username.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

    }

    public LoginDialog(String title, String header) {
        this(title, header, null, null);
    }
}
