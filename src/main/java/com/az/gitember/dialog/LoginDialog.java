package com.az.gitember.dialog;


import com.az.gitember.App;
import com.az.gitember.data.RemoteRepoParameters;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * Created by Igor_Azarny on 25 - Dec -2016.
 */
public class LoginDialog extends Dialog<RemoteRepoParameters> {

    private static int  prefWidth = 300;

    public LoginDialog(String title, String header,
                       RemoteRepoParameters repoInfo) {

        super();
        String adjustedHeader = StringUtils.getIfBlank(header, () -> "").toLowerCase(Locale.ROOT);
        this.setTitle(title);
        this.setHeaderText(adjustedHeader);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        ButtonType okButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setPrefWidth(prefWidth);
        HBox.setHgrow(username, Priority.ALWAYS);
        if (repoInfo.getUserName() != null) {
            username.setText(repoInfo.getUserName());
        }
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setPrefWidth(prefWidth);
        if (repoInfo.getUserPwd() != null) {
            password.setText(repoInfo.getUserPwd());
        }

        grid.add(new Label("Username : "), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password : "), 0, 1);
        grid.add(password, 1, 1);

        if (adjustedHeader.contains("github") && adjustedHeader.contains("http") && adjustedHeader.contains("not authorized")) { //TODO ugly need better approach
            Node msg = new Text(
                    "Github specific\n" +
                    " remote: Support for password authentication was removed on August 13, 2021. \n Please use a personal access token instead.\n" +
                    " remote: Please see https://github.blog/2020-12-15-token-authentication-requirements-for-git-operations/ \n for more information. ");
            grid.add(msg, 0, 2, 2, 1);
        }


        Node loginButton = this.getDialogPane().lookupButton(okButtonType);
        loginButton.setDisable(username.textProperty().isEmpty().get());

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });


        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> {
            if (StringUtils.isBlank(username.getText())) {
                username.requestFocus();
            } else {
                password.requestFocus();
            }
        });

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
