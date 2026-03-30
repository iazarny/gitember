package com.az.gitember.data;

import com.az.gitember.service.Context;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RemoteRepoParameters {

    private String userName = "";
    private String userPwd = "";
    private String accessToken = "";
    private String url = "";
    private String destinationFolder = "";
    private String keyPassPhrase = "";
    /** Shallow clone depth: 0 means full clone; positive N = last N commits only. */
    private int depth = 0;

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

    /**
     * Creates parameters for the current repository.
     * Loads credentials from the stored project; if none are configured,
     * falls back to extracting them from the remote URL (embedded user-info
     * or base64-encoded credentials).
     */
    public static RemoteRepoParameters forCurrentRepo() {
        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String remoteUrl = gitConfig.getString(
                ConfigConstants.CONFIG_REMOTE_SECTION, "origin", ConfigConstants.CONFIG_KEY_URL);

        RemoteRepoParameters params = new RemoteRepoParameters();
        params.url = StringUtils.defaultString(remoteUrl);

        Context.getCurrentProject().ifPresent(p -> {
            params.userName     = StringUtils.defaultString(p.getUserName());
            params.userPwd      = StringUtils.defaultString(p.getUserPwd());
            params.accessToken  = StringUtils.defaultString(p.getAccessToken());
            params.keyPassPhrase = StringUtils.defaultString(p.getKeyPass());
        });

        // If the project had no credentials, try to extract them from the URL
        if (StringUtils.isBlank(params.accessToken) && StringUtils.isBlank(params.userName)) {
            params.extractCredentialsFromUrl(params.url);
        }

        return params;
    }

    /**
     * Parses user-info embedded in a remote URL and populates accessToken
     * or userName/userPwd accordingly.
     *
     * <p>Supported forms:
     * <ul>
     *   <li>{@code https://token@host/repo.git} — single value, detected as token by prefix/length</li>
     *   <li>{@code https://user:password@host/repo.git} — explicit user + password</li>
     *   <li>{@code https://user:token@host/repo.git} — password field is a token</li>
     *   <li>{@code https://BASE64@host/repo.git} — base64("user:pass") as the sole user-info field</li>
     * </ul>
     */
    private void extractCredentialsFromUrl(String rawUrl) {
        if (StringUtils.isBlank(rawUrl)) return;
        try {
            URI uri = new URI(rawUrl);
            String userInfo = uri.getUserInfo();
            if (StringUtils.isBlank(userInfo)) return;

            String user = null;
            String pass = null;

            if (userInfo.contains(":")) {
                int colon = userInfo.indexOf(':');
                user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
                pass = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
            } else {
                // Single value — try base64 decode first ("user:pass" encoded)
                String decoded = tryBase64Decode(userInfo);
                if (decoded != null && decoded.contains(":")) {
                    int colon = decoded.indexOf(':');
                    user = decoded.substring(0, colon);
                    pass = decoded.substring(colon + 1);
                } else {
                    user = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
                }
            }

            // The password field (or sole field) may itself be an access token
            String tokenCandidate = StringUtils.isNotBlank(pass) ? pass : user;
            if (looksLikeAccessToken(tokenCandidate)) {
                this.accessToken = tokenCandidate;
            } else {
                this.userName = StringUtils.defaultString(user);
                this.userPwd  = StringUtils.defaultString(pass);
            }
        } catch (URISyntaxException ignored) {
            // Non-parseable URL — skip silently
        }
    }

    /**
     * Returns true when {@code s} looks like a personal access token rather than
     * a regular password, based on known vendor prefixes and length heuristics:
     * <ul>
     *   <li>GitHub classic PAT: {@code ghp_…}</li>
     *   <li>GitHub fine-grained PAT: {@code github_pat_…}</li>
     *   <li>GitLab PAT: {@code glpat-…}</li>
     *   <li>Bitbucket app-password: ~20 alphanumeric chars</li>
     *   <li>Azure DevOps PAT: ~52 alphanumeric chars</li>
     * </ul>
     */
    private static boolean looksLikeAccessToken(String s) {
        if (StringUtils.isBlank(s)) return false;
        if (s.startsWith("ghp_"))        return true;   // GitHub classic PAT
        if (s.startsWith("github_pat_")) return true;   // GitHub fine-grained PAT
        if (s.startsWith("glpat-"))      return true;   // GitLab PAT
        // Bitbucket (~20 chars) / Azure DevOps (~52 chars): long alphanumeric-safe strings
        return s.length() >= 20 && s.matches("[A-Za-z0-9_~+/=-]+");
    }

    private static String tryBase64Decode(String s) {
        try {
            return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public RemoteRepoParameters(Project project) {
        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String remoteUrl = gitConfig.getString(ConfigConstants.CONFIG_REMOTE_SECTION,
                "origin", ConfigConstants.CONFIG_KEY_URL);

        this.userName     = StringUtils.defaultString(project.getUserName());
        this.userPwd      = StringUtils.defaultString(project.getUserPwd());
        this.accessToken  = StringUtils.defaultString(project.getAccessToken());
        this.keyPassPhrase = StringUtils.defaultString(project.getKeyPass());
        this.url = StringUtils.defaultString(remoteUrl);

        // Fallback: extract from URL if the project has no credentials stored
        if (StringUtils.isBlank(accessToken) && StringUtils.isBlank(userName)) {
            extractCredentialsFromUrl(this.url);
        }
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = Math.max(0, depth);
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
