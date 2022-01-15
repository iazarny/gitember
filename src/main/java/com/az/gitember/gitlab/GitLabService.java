package com.az.gitember.gitlab;

import org.gitlab4j.api.GitLabApi;

public class GitLabService {

    private final String gitlabUrl;
    private final String gitlabTolen;
    private final GitLabApi gitLabApi;

    public GitLabService(String gitlabUrl, String gitlabToken) {
        this.gitlabUrl = gitlabUrl;
        this.gitlabTolen = gitlabToken;
        this.gitLabApi = new GitLabApi(gitlabUrl, gitlabToken);
    }

    //GitLabApi

}
