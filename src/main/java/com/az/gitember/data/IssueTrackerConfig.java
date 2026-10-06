package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-repository issue-tracker integration (credentials encrypted via {@link MaskStringValueSerializer}).
 * Stored on {@link Project} in {@code ~/.gitember/gitember2.json}.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class IssueTrackerConfig {

    public static final String TYPE_JIRA = "jira";
    public static final String TYPE_LINEAR = "linear";
    public static final String AUTH_API_TOKEN = "API_TOKEN";
    public static final String AUTH_OAUTH = "OAUTH";
    public static final String DEFAULT_BRANCH_PATTERN = "{type}/{key}-{slug}";
    public static final String DEFAULT_BRANCH_TYPE = "feature";

    private boolean enabled;
    private String type = TYPE_JIRA;
    private String server;
    private String authType = AUTH_API_TOKEN;
    private String email;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String apiToken;
    private List<String> projectKeys = new ArrayList<>();
    private boolean includeIssueKeyInCommit = true;
    private boolean commentOnCommit;
    private String branchNamePattern = DEFAULT_BRANCH_PATTERN;
    private String branchType = DEFAULT_BRANCH_TYPE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type != null && !type.isBlank() ? type : TYPE_JIRA;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAuthType() {
        return authType != null && !authType.isBlank() ? authType : AUTH_API_TOKEN;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public List<String> getProjectKeys() {
        if (projectKeys == null) {
            projectKeys = new ArrayList<>();
        }
        return projectKeys;
    }

    public void setProjectKeys(List<String> projectKeys) {
        this.projectKeys = projectKeys != null ? projectKeys : new ArrayList<>();
    }

    public boolean isIncludeIssueKeyInCommit() {
        return includeIssueKeyInCommit;
    }

    public void setIncludeIssueKeyInCommit(boolean includeIssueKeyInCommit) {
        this.includeIssueKeyInCommit = includeIssueKeyInCommit;
    }

    public boolean isCommentOnCommit() {
        return commentOnCommit;
    }

    public void setCommentOnCommit(boolean commentOnCommit) {
        this.commentOnCommit = commentOnCommit;
    }

    public String getBranchNamePattern() {
        return branchNamePattern != null && !branchNamePattern.isBlank()
                ? branchNamePattern : DEFAULT_BRANCH_PATTERN;
    }

    public void setBranchNamePattern(String branchNamePattern) {
        this.branchNamePattern = branchNamePattern;
    }

    public String getBranchType() {
        return branchType != null && !branchType.isBlank() ? branchType : DEFAULT_BRANCH_TYPE;
    }

    public void setBranchType(String branchType) {
        this.branchType = branchType;
    }

    public boolean isReady() {
        boolean ready = enabled && apiToken != null && !apiToken.isBlank();
        if (ready && !isLinear()) {
            ready = server != null && !server.isBlank();
            if (ready && AUTH_API_TOKEN.equals(getAuthType())) {
                ready = email != null && !email.isBlank();
            }
        }
        return ready;
    }

    public boolean isLinear() {
        return TYPE_LINEAR.equalsIgnoreCase(getType());
    }

    public boolean isJira() {
        return !isLinear();
    }
}
