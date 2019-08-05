package com.az.gitember.ui;

import com.az.gitember.misc.GitemberProjectSettings;
import com.az.gitember.misc.GitemberSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Igor_Azarny on 06 -Jan - 2017.
 */
public class SettingsModel {

    private BooleanProperty rememberMe = new SimpleBooleanProperty();
    private StringProperty projectName = new SimpleStringProperty();
    private StringProperty projectHameFolder = new SimpleStringProperty();
    private StringProperty userName = new SimpleStringProperty();
    private StringProperty userEmail = new SimpleStringProperty();
    private StringProperty projectRemoteUrl = new SimpleStringProperty();
    private StringProperty projectKeyPath = new SimpleStringProperty();
    private StringProperty projectPwd = new SimpleStringProperty();


    /////////////////// proxy
    private BooleanProperty useProxy = new SimpleBooleanProperty();
    private StringProperty proxyServer = new SimpleStringProperty();
    private StringProperty proxyPort = new SimpleStringProperty();
    private StringProperty proxyUserName = new SimpleStringProperty();
    private StringProperty proxyPassword = new SimpleStringProperty();

    public boolean isRememberMe() {
        return rememberMe.get();
    }

    public BooleanProperty rememberMeProperty() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe.set(rememberMe);
    }

    public String getProjectName() {
        return projectName.get();
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public String getProjectHameFolder() {
        return projectHameFolder.get();
    }

    public StringProperty projectHameFolderProperty() {
        return projectHameFolder;
    }

    public void setProjectHameFolder(String projectHameFolder) {
        this.projectHameFolder.set(projectHameFolder);
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

    public String getUserEmail() {
        return userEmail.get();
    }

    public StringProperty userEmailProperty() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail.set(userEmail);
    }

    public String getProjectRemoteUrl() {
        return projectRemoteUrl.get();
    }

    public StringProperty projectRemoteUrlProperty() {
        return projectRemoteUrl;
    }

    public void setProjectRemoteUrl(String projectRemoteUrl) {
        this.projectRemoteUrl.set(projectRemoteUrl);
    }

    public String getProjectKeyPath() {
        return projectKeyPath.get();
    }

    public StringProperty projectKeyPathProperty() {
        return projectKeyPath;
    }

    public void setProjectKeyPath(String projectKeyPath) {
        this.projectKeyPath.set(projectKeyPath);
    }

    public String getProjectPwd() {
        return projectPwd.get();
    }

    public StringProperty projectPwdProperty() {
        return projectPwd;
    }

    public void setProjectPwd(String projectPwd) {
        this.projectPwd.set(projectPwd);
    }

    public boolean isUseProxy() {
        return useProxy.get();
    }

    public boolean getUseProxy() {
        return useProxy.get();
    }

    public BooleanProperty useProxyProperty() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy.set(useProxy);
    }

    public String getProxyServer() {
        return proxyServer.get();
    }

    public StringProperty proxyServerProperty() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer.set(proxyServer);
    }

    public String getProxyPort() {
        return proxyPort.get();
    }

    public StringProperty proxyPortProperty() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort.set(proxyPort);
    }

    public String getProxyUserName() {
        return proxyUserName.get();
    }

    public StringProperty proxyUserNameProperty() {
        return proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName.set(proxyUserName);
    }

    public String getProxyPassword() {
        return proxyPassword.get();
    }

    public StringProperty proxyPasswordProperty() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword.set(proxyPassword);
    }
    public SettingsModel() {
        super();
    }


    public SettingsModel(GitemberProjectSettings gitemberSettings) {
        super();

        this.rememberMe.setValue(gitemberSettings.isRememberMe());
        this.projectName.setValue(gitemberSettings.getProjectName());
        this.projectHameFolder.setValue(gitemberSettings.getProjectHameFolder());
        this.userName.setValue(gitemberSettings.getUserName());
        this.userEmail.setValue(gitemberSettings.getUserEmail());
        this.projectRemoteUrl.setValue(gitemberSettings.getProjectRemoteUrl());
        this.projectKeyPath.setValue(gitemberSettings.getProjectKeyPath());
        this.projectPwd.setValue(gitemberSettings.getProjectPwd());


        /////////////////// proxy
        this.useProxy.setValue(gitemberSettings.isUseProxy());
        this.proxyServer.setValue(gitemberSettings.getProxyServer());
        this.proxyPort.setValue(gitemberSettings.getProxyPort());
        this.proxyUserName.setValue(gitemberSettings.getProxyUserName());
        this.proxyPassword.setValue(gitemberSettings.getProxyPassword());
    }

    public GitemberProjectSettings toGitemberProjectSettings() {
        final GitemberProjectSettings gitemberSettings = new GitemberProjectSettings();

        gitemberSettings.setRememberMe(this.rememberMe.getValue());
        gitemberSettings.setProjectName(this.projectName.getValueSafe());
        gitemberSettings.setProjectHameFolder(this.projectHameFolder.getValueSafe());
        gitemberSettings.setUserName(this.userName.getValueSafe());
        gitemberSettings.setUserEmail(this.userEmail.getValueSafe());
        gitemberSettings.setProjectRemoteUrl(this.projectRemoteUrl.getValueSafe());
        gitemberSettings.setProjectKeyPath(this.projectKeyPath.getValueSafe());
        gitemberSettings.setProjectPwd(this.projectPwd.getValueSafe());

        // proxy
        gitemberSettings.setUseProxy(this.useProxy.getValue());
        gitemberSettings.setProxyServer(this.proxyServer.getValueSafe());
        gitemberSettings.setProxyPort(this.proxyPort.getValueSafe());
        gitemberSettings.setProxyUserName(this.proxyUserName.getValueSafe());
        gitemberSettings.setProxyPassword(this.proxyPassword.getValueSafe());


        return gitemberSettings;
    }
}
