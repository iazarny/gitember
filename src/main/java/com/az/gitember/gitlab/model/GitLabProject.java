package com.az.gitember.gitlab.model;

import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Label;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.List;

public class GitLabProject {


    private final Object projectIdOrPath;

    private ProjectApi projectApi = null;
    private IssuesApi issuesApi = null;
    private LabelsApi labelsApi = null;
    private Project project= null;

    private List<Label> allProjectLabelsCache  = null;

    public GitLabProject(Object projectIdOrPath) {
        this.projectIdOrPath = projectIdOrPath;
    }

    public List<Label> getProjectLabels()  {
        if (allProjectLabelsCache == null) {
            try {
                allProjectLabelsCache  = getLabelsApi().getProjectLabels(projectIdOrPath);
            } catch (GitLabApiException e) {
                allProjectLabelsCache = new ArrayList<>();
            }
        }
        return allProjectLabelsCache;
    }

    private GitLabApi gitLabApi = null;


    public GitLabApi getGitLabApi() {
        if (gitLabApi == null) {
            String accessToken = Context.settingsProperty.get().getGitlabSettings().getAccessToken();
            String url = GitemberUtil.getServer(Context.getGitRepoService().getRepositoryRemoteUrl());
            gitLabApi = new GitLabApi(url, accessToken);
        }
        return gitLabApi;
    }


    public LabelsApi getLabelsApi() {
        if (labelsApi == null) {
            GitLabApi glApi = getGitLabApi();
            labelsApi = new LabelsApi(glApi);
        }
        return labelsApi;
    }

    public Project getProject() throws GitLabApiException {
        if (project == null) {
            project = getProjectApi().getProject(projectIdOrPath);
        }
        return  project;
    }

    public IssuesApi getIssuesApi() {
        if (issuesApi == null) {
            GitLabApi glApi =getGitLabApi();
            issuesApi = new IssuesApi(glApi);
        }
        return issuesApi;
    }

    public ProjectApi getProjectApi() {
        if (projectApi == null) {
            GitLabApi glApi = getGitLabApi();
            projectApi  = new ProjectApi(glApi);
        }
        return projectApi;
    }

}
