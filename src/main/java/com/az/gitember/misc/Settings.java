package com.az.gitember.misc;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Igor_Azarny on 07 -Jan -2017.
 */
public class Settings implements Serializable {

    private boolean overwriteAuthorWithCommiter;

    private boolean rememberPasswords;
    private boolean useProxy;
    private String proxyServer;
    private String proxyPort;
    private boolean useProxyAuth;
    private String proxyUserName;
    private String proxyPassword;
    private String repoUserName;
    private String repoUserEmail;

    private String lastLoginName;
    private String lastProject;
    private ArrayList<String> commitMessages = new ArrayList<>();
    private ArrayList<String> projects = new ArrayList<>();
    private ArrayList<String> giturls = new ArrayList<>();
    private ArrayList<RepoInfo> loginPassword = new ArrayList<>();

    public ArrayList<String> getGiturls() {
        return giturls;
    }

    public void setGiturls(ArrayList<String> giturls) {
        this.giturls = giturls;
    }

    public ArrayList<String> getCommitMessages() {
        return commitMessages;
    }

    public void setCommitMessages(ArrayList<String> commitMessages) {
        this.commitMessages = commitMessages;
    }

    public ArrayList<String> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<String> projects) {
        this.projects = projects;
    }

    public String getLastProject() {
        return lastProject;
    }

    public void setLastProject(String lastProject) {
        this.lastProject = lastProject;
    }

    public String getLastLoginName() {
        return lastLoginName;
    }

    public void setLastLoginName(String lastLoginName) {
        this.lastLoginName = lastLoginName;
    }

    public boolean isOverwriteAuthorWithCommiter() {
        return overwriteAuthorWithCommiter;
    }

    public void setOverwriteAuthorWithCommiter(boolean overwriteAuthorWithCommiter) {
        this.overwriteAuthorWithCommiter = overwriteAuthorWithCommiter;
    }

    public boolean isRememberPasswords() {
        return rememberPasswords;
    }

    public void setRememberPasswords(boolean rememberPasswords) {
        this.rememberPasswords = rememberPasswords;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isUseProxyAuth() {
        return useProxyAuth;
    }

    public void setUseProxyAuth(boolean useProxyAuth) {
        this.useProxyAuth = useProxyAuth;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public ArrayList<RepoInfo> getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(ArrayList<RepoInfo> loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getRepoUserName() {
        return repoUserName;
    }

    public void setRepoUserName(String repoUserName) {
        this.repoUserName = repoUserName;
    }

    public String getRepoUserEmail() {
        return repoUserEmail;
    }

    public void setRepoUserEmail(String repoUserEmail) {
        this.repoUserEmail = repoUserEmail;
    }
}
