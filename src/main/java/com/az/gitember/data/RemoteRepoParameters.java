package com.az.gitember.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class RemoteRepoParameters {

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


    public RemoteRepoParameters(Project project) {
        this.userName.setValue(project.getUserName());
        this.userPwd.setValue(project.getUserPwd());
        this.pathToKey.setValue(project.getKeyPass());
        this.keyPassPhrase.setValue(project.getKeyPass());
        //this.url.setValue(project.);

    }



    public RemoteRepoParameters() {
    }

    @Override
    public String toString() {
        return "CloneParameters{" +
                "userName=" + userName +
                ", userPwd=" + (StringUtils.isBlank(userPwd.getValue()) ? "none" : "set") +
                ", url=" + url +
                ", destinationFolder=" + destinationFolder +
                ", pathToKey=" + pathToKey +
                ", keyPassPhrase=" + keyPassPhrase +
                '}';
    }
}