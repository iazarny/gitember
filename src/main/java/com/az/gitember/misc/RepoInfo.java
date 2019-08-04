package com.az.gitember.misc;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by igor on 09.03.2019.
 */
public class RepoInfo {

    private String url;
    private String login;
    private String pwd;
    private String key;
    private boolean rememberMe;

    public RepoInfo(String url, String login, String pwd, String key, boolean rememberMe) {
        this.url = url;
        this.login = login;
        this.pwd = pwd;
        this.key = key;
        this.rememberMe = rememberMe;
    }

    public RepoInfo() {
    }

    /*public static RepoInfo of(RepoInfo ri) {
        return new RepoInfo(
                ri.url,
                ri.login,
                ri.pwd,
                ri.key,
                ri.rememberMe);
    }*/

    public static RepoInfo of(String url, String login, String pwd, String key, boolean rememberMe) {
        return new RepoInfo(url, login, pwd, key, rememberMe);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "RepoInfo{" +
                "url='" + url + '\'' +
                ", login='" + login + '\'' +
                ", pwd='" + (StringUtils.isNotBlank(pwd) ? " ####### " : " no ") + '\'' +
                ", key='" + (StringUtils.isNotBlank(key) ? " ####### " : " no ")  + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
