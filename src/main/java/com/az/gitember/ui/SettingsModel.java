package com.az.gitember.ui;

import com.az.gitember.misc.Settings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by Igor_Azarny on 06 -Jan - 2017.
 */
public class SettingsModel {

    private StringProperty authorName = new SimpleStringProperty();
    private StringProperty authorEmail = new SimpleStringProperty();
    private BooleanProperty overwriteAuthorWithCommiter = new SimpleBooleanProperty();

    private BooleanProperty rememberPasswords = new SimpleBooleanProperty();
    private BooleanProperty useProxy = new SimpleBooleanProperty();
    private StringProperty proxyServer = new SimpleStringProperty();
    private StringProperty proxyPort = new SimpleStringProperty();
    private BooleanProperty useProxyAuth = new SimpleBooleanProperty();
    private StringProperty proxyUserName = new SimpleStringProperty();
    private StringProperty proxyPassword = new SimpleStringProperty();
    private StringProperty repoUserName = new SimpleStringProperty();
    private StringProperty repoUserEmail = new SimpleStringProperty();

    public String getAuthorName() {
        return authorName.get();
    }

    public StringProperty authorNameProperty() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName.set(authorName);
    }

    public String getAuthorEmail() {
        return authorEmail.get();
    }

    public StringProperty authorEmailProperty() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail.set(authorEmail);
    }

    public boolean getOverwriteAuthorWithCommiter() {
        return overwriteAuthorWithCommiter.get();
    }

    public BooleanProperty overwriteAuthorWithCommiterProperty() {
        return overwriteAuthorWithCommiter;
    }

    public void setOverwriteAuthorWithCommiter(boolean overwriteAuthorWithCommiter) {
        this.overwriteAuthorWithCommiter.set(overwriteAuthorWithCommiter);
    }

    public boolean getRememberPasswords() {
        return rememberPasswords.get();
    }

    public BooleanProperty rememberPasswordsProperty() {
        return rememberPasswords;
    }

    public void setRememberPasswords(boolean rememberPasswords) {
        this.rememberPasswords.set(rememberPasswords);
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

    public boolean getUseProxyAuth() {
        return useProxyAuth.get();
    }

    public BooleanProperty useProxyAuthProperty() {
        return useProxyAuth;
    }

    public void setUseProxyAuth(boolean useProxyAuth) {
        this.useProxyAuth.set(useProxyAuth);
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

    public String getRepoUserName() {
        return repoUserName.get();
    }

    public StringProperty repoUserNameProperty() {
        return repoUserName;
    }

    public void setRepoUserName(String repoUserName) {
        this.repoUserName.set(repoUserName);
    }

    public String getRepoUserEmail() {
        return repoUserEmail.get();
    }

    public StringProperty repoUserEmailProperty() {
        return repoUserEmail;
    }

    public void setRepoUserEmail(String repoUserEmail) {
        this.repoUserEmail.set(repoUserEmail);
    }

    public SettingsModel() {
        super();
    }


    public SettingsModel(Settings settings) {
        super();
        this.setOverwriteAuthorWithCommiter(settings.isOverwriteAuthorWithCommiter());

        this.setRememberPasswords(settings.isRememberPasswords());
        this.setUseProxy(settings.isUseProxy());
        this.setProxyServer(settings.getProxyServer());
        this.setProxyPort(settings.getProxyPort());
        this.setUseProxyAuth(settings.isUseProxyAuth());
        this.setProxyUserName(settings.getProxyUserName());
        this.setProxyPassword(settings.getProxyPassword());
        this.setRepoUserName(settings.getRepoUserName());
        this.setRepoUserEmail(settings.getRepoUserEmail());
    }

    public Settings createSettings() {
        final Settings settings = new Settings();

        settings.setOverwriteAuthorWithCommiter(this.getOverwriteAuthorWithCommiter());

        settings.setRememberPasswords(this.getRememberPasswords());
        settings.setUseProxy(this.getUseProxy());
        settings.setProxyServer(this.getProxyServer());
        settings.setProxyPort(this.getProxyPort());
        settings.setUseProxyAuth(this.getUseProxyAuth());
        settings.setProxyUserName(this.getProxyUserName());
        settings.setProxyPassword(this.getProxyPassword());
        settings.setRepoUserName(this.getRepoUserName());
        settings.setRepoUserEmail(this.getRepoUserEmail());

        return settings;
    }
}
