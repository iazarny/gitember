package com.az.gitember.data;

import com.az.gitember.service.Context;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;

import java.io.File;

public class RemoteRepoParameters {

    private StringProperty userName = new SimpleStringProperty("");
    private StringProperty userPwd = new SimpleStringProperty("");
    private StringProperty url = new SimpleStringProperty("");
    private StringProperty destinationFolder = new SimpleStringProperty("");

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

        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String url = gitConfig.getString(ConfigConstants.CONFIG_REMOTE_SECTION,
                "origin",
                ConfigConstants.CONFIG_KEY_URL);

        this.userName.setValue(project.getUserName());
        this.userPwd.setValue(project.getUserPwd());
        this.keyPassPhrase.setValue(project.getKeyPass());
        this.url.setValue(url);

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
                ", keyPassPhrase=" + keyPassPhrase +
                '}';
    }
}