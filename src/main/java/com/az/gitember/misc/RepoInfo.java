package com.az.gitember.misc;

/**
 * Created by igor on 09.03.2019.
 */
public class RepoInfo {

    private String url;
    private String login;
    private String pwd;
    private String key;

    public RepoInfo(String url, String login, String pwd, String key) {
        this.url = url;
        this.login = login;
        this.pwd = pwd;
        this.key = key;
    }

    public RepoInfo() {
    }

    public static RepoInfo of(String url, String login, String pwd, String key) {
        return new RepoInfo(url, login, pwd, key);
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
}
