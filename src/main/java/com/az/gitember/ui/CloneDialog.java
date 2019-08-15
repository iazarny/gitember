package com.az.gitember.ui;

import com.az.gitember.GitemberApp;
import com.az.gitember.misc.GitemberProjectSettings;
import com.az.gitember.misc.Pair;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
public class CloneDialog extends Dialog<GitemberProjectSettings> {

    public static class CloneParameters {

        private StringProperty userName = new SimpleStringProperty("");
        private StringProperty userPwd = new SimpleStringProperty("");
        private StringProperty url = new SimpleStringProperty("");
        private StringProperty destinationFolder = new SimpleStringProperty("");
        private StringProperty pathToKey = new SimpleStringProperty(System.getProperty("user.home")
                + File.separator
                + ".ssh"
                + File.separator
                + "id_rsa");
        private StringProperty keyPassPhrase = new SimpleStringProperty("");

        public String getUrl() {
            return url.get();
        }

        public StringProperty urlProperty() {
            return url;
        }

        public void setUrl(String url) {
            this.url.set(url);
        }

        public String getDestinationFolder() {
            return destinationFolder.get();
        }

        public StringProperty destinationFolderProperty() {
            return destinationFolder;
        }

        public void setDestinationFolder(String destinationFolder) {
            this.destinationFolder.set(destinationFolder);
        }

        public String getPathToKey() {
            return pathToKey.get();
        }

        public StringProperty pathToKeyProperty() {
            return pathToKey;
        }

        public void setPathToKey(String pathToKey) {
            this.pathToKey.set(pathToKey);
        }

        public String getKeyPassPhrase() {
            return keyPassPhrase.get();
        }

        public StringProperty keyPassPhraseProperty() {
            return keyPassPhrase;
        }

        public void setKeyPassPhrase(String keyPassPhrase) {
            this.keyPassPhrase.set(keyPassPhrase);
        }

        public String getUserName() {
            return userName.get();
        }

        public StringProperty userNameProperty() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName.set(userName);
        }

        public String getUserPwd() {
            return userPwd.get();
        }

        public StringProperty userPwdProperty() {
            return userPwd;
        }

        public void setUserPwd(String userPwd) {
            this.userPwd.set(userPwd);
        }

        public GitemberProjectSettings toGitemberProjectSettings() {
            GitemberProjectSettings projectSettings = new GitemberProjectSettings();
            projectSettings.setRememberMe(false);
            projectSettings.setProjectName(destinationFolder.getValueSafe());
            projectSettings.setProjectHameFolder(destinationFolder.getValueSafe());
            projectSettings.setUserName(userName.getValueSafe());
            projectSettings.setProjectPwd(userPwd.getValueSafe());
            projectSettings.setProjectRemoteUrl(url.getValueSafe());
            projectSettings.setProjectKeyPath(pathToKey.getValueSafe());



            return projectSettings;
        }
    }

    private Label pathToKeyLabel;
    private TextField pathToKey;
    private Button selectPathToKeyBtn;

    private Label passphrazeLabel;
    private PasswordField passphraze;

    private CloneParameters cloneParameters = new CloneParameters();

    public CloneDialog(final String title,
                       final String header,
                       final Collection<String> urlHistory) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);


        ButtonType loginButtonType = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        AutoCompleteTextField repositoryURL = new AutoCompleteTextField();
        repositoryURL.setPromptText("URL");
        repositoryURL.setMinWidth(400);
        repositoryURL.getEntries().addAll(urlHistory);

        TextField folder = new TextField();
        folder.setPromptText("Folder");
        HBox.setHgrow(folder, Priority.ALWAYS);

        Button selectFolder = new Button("...");
        HBox folderHBox = new HBox(folder, selectFolder);

        pathToKeyLabel = new Label("Key path : ");
        selectPathToKeyBtn = new Button("...");
        pathToKey = new TextField();
        HBox.setHgrow(pathToKey, Priority.ALWAYS);
        HBox keyHBox = new HBox(pathToKey, selectPathToKeyBtn);

        passphrazeLabel = new Label("Key passphraze");
        passphraze = new PasswordField();

        grid.add(new Label("URL : "), 0, 0);
        grid.add(repositoryURL, 1, 0);

        grid.add(new Label("Folder : "), 0, 1);
        grid.add(folderHBox, 1, 1);

        grid.add(pathToKeyLabel, 0, 2);
        grid.add(keyHBox, 1, 2);

        grid.add(passphrazeLabel, 0, 3);
        grid.add(passphraze, 1, 3);

        pathToKeyLabel.setDisable(true);
        pathToKey.setDisable(true);
        passphrazeLabel.setDisable(true);
        passphraze.setDisable(true);
        selectPathToKeyBtn.setDisable(true);

        Node loginButton = this.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        selectFolder.setOnAction(
                event -> {
                    final DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getUserHomeFolder()));
                    final File selectedDirectory =
                            directoryChooser.showDialog(GitemberApp.getMainStage());
                    if (selectedDirectory != null) {
                        folder.setText(selectedDirectory.getAbsolutePath());
                    }
                }
        );

        selectPathToKeyBtn.setOnAction(
                event -> {
                    final FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getUserHomeFolder()));
                    final File selectedFile =
                            fileChooser.showOpenDialog(GitemberApp.getMainStage());
                    if (selectedFile != null) {
                        pathToKey.setText(selectedFile.getAbsolutePath());
                    }
                }
        );

        repositoryURL.textProperty().addListener((observable, oldValue, newValue) -> {
            String nw = newValue.trim();
            loginButton.setDisable(nw.isEmpty());
            boolean keyyDisable = true;
            if (nw.startsWith("git@")){
                keyyDisable = false;
            } else if (nw.startsWith("https:") || nw.startsWith("http:")){
                keyyDisable = true;
            }
            pathToKeyLabel.setDisable(keyyDisable);
            pathToKey.setDisable(keyyDisable);
            passphrazeLabel.setDisable(keyyDisable);
            passphraze.setDisable(keyyDisable);
            selectPathToKeyBtn.setDisable(keyyDisable);

        });

        Bindings.bindBidirectional(repositoryURL.textProperty(), cloneParameters.urlProperty());
        Bindings.bindBidirectional(folder.textProperty(), cloneParameters.destinationFolderProperty());
        Bindings.bindBidirectional(pathToKey.textProperty(), cloneParameters.pathToKeyProperty());
        Bindings.bindBidirectional(passphraze.textProperty(), cloneParameters.keyPassPhraseProperty());

        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> repositoryURL.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return cloneParameters.toGitemberProjectSettings();
            }
            return null;
        });
    }
}
