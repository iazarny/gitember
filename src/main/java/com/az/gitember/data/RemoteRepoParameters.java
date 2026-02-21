package com.az.gitember.data;

import com.az.gitember.service.Context;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;

public class RemoteRepoParameters {

    private String userName = "";
    private String userPwd = "";
    private String accessToken = "";
    private String url = "";
    private String destinationFolder = "";
    private String keyPassPhrase = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public String getKeyPassPhrase() {
        return keyPassPhrase;
    }

    public void setKeyPassPhrase(String keyPassPhrase) {
        this.keyPassPhrase = keyPassPhrase;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public RemoteRepoParameters(Project project) {
        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String url = gitConfig.getString(ConfigConstants.CONFIG_REMOTE_SECTION,
                "origin",
                ConfigConstants.CONFIG_KEY_URL);

        this.userName = project.getUserName();
        this.userPwd = project.getUserPwd();
        this.accessToken = project.getAccessToken();
        this.keyPassPhrase = project.getKeyPass();
        this.url = url;
    }

    public RemoteRepoParameters() {
    }

    @Override
    public String toString() {
        return "CloneParameters{" +
                "userName=" + userName +
                ", userPwd=" + (StringUtils.isBlank(userPwd) ? "none" : "set") +
                ", accessToken=" + (StringUtils.isBlank(accessToken) ? "none" : "set") +
                ", url=" + url +
                ", destinationFolder=" + destinationFolder +
                ", keyPassPhrase=" + keyPassPhrase +
                '}';
    }
}
