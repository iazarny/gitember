package com.az.gitember.misc;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Created by Igor_Azarny on 05 -Aug -2019.
 */
public class GitemberProjectSettings implements Serializable, Comparable<GitemberProjectSettings> {


    private boolean rememberMe;
    private String projectName;
    private String projectHameFolder;
    private String userName;
    private String userEmail;
    private String projectRemoteUrl;
    private String projectKeyPath;
    private String projectPwd;


    /////////////////// proxy
    private boolean useProxy;
    private String proxyServer;
    private String proxyPort;
    private String proxyUserName;
    private String proxyPassword;

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectHameFolder() {
        return projectHameFolder;
    }

    public void setProjectHameFolder(String projectHameFolder) {
        this.projectHameFolder = projectHameFolder;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getProjectRemoteUrl() {
        return projectRemoteUrl;
    }

    public void setProjectRemoteUrl(String projectRemoteUrl) {
        this.projectRemoteUrl = projectRemoteUrl;
    }

    public String getProjectKeyPath() {
        return projectKeyPath;
    }

    public void setProjectKeyPath(String projectKeyPath) {
        this.projectKeyPath = projectKeyPath;
    }

    public String getProjectPwd() {
        return projectPwd;
    }

    public void setProjectPwd(String projectPwd) {
        this.projectPwd = projectPwd;
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

    @Override
    public int compareTo(GitemberProjectSettings o) {
        if(o == null) {
            return 0;
        }
        return this.projectHameFolder.compareTo(o.projectHameFolder);
    }

    public RepoInfo toRepoInfo() {
        return RepoInfo.of(
                projectRemoteUrl, userEmail, projectPwd, projectKeyPath, rememberMe
        );
    }

    public void updateFrom(RepoInfo repoInfo){
        setProjectRemoteUrl(repoInfo.getUrl());
        setUserName(repoInfo.getLogin());
        setProjectPwd(repoInfo.getPwd());
        setProjectKeyPath(repoInfo.getKey());
        setRememberMe(repoInfo.isRememberMe());
    }
}
