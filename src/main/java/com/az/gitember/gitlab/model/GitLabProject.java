package com.az.gitember.gitlab.model;

import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;

import java.util.ArrayList;
import java.util.List;

public class GitLabProject {


    private final Object projectIdOrPath;

    private ProjectApi projectApi = null;
    private DiscussionsApi discussionsApi = null;
    private IssuesApi issuesApi = null;
    private LabelsApi labelsApi = null;
    private MilestonesApi milestonesApi = null;
    private UserApi userApi = null;
    private Project project= null;

    private List<Label> allProjectLabelsCache  = null;
    private List<Milestone> allProjectMilestone  = null;
    private List<Member> allProjectUsers  = null;

    public GitLabProject(Object projectIdOrPath) {
        this.projectIdOrPath = projectIdOrPath;
    }


    public Issue setEstimatedTime(Integer issueIid, String spendTime) throws GitLabApiException {
        getIssuesApi().estimateTime(projectIdOrPath, issueIid, spendTime);
        return getIssuesApi().getIssue(projectIdOrPath, issueIid);
    }


    public Issue addSpendTime(Integer issueIid, String spendTime) throws GitLabApiException {
        getIssuesApi().addSpentTime(projectIdOrPath, issueIid, spendTime);
        return getIssuesApi().getIssue(projectIdOrPath, issueIid);
    }

    public List<Member> getActiveUsers() {

        if (allProjectUsers == null) {
            try {
                allProjectUsers = getProjectApi().getAllMembers(projectIdOrPath);
            } catch (GitLabApiException e) {
                allProjectUsers = new ArrayList<>();
            }
        }
        return allProjectUsers;
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

    public List<Milestone> getMilestones() {
        if (allProjectMilestone == null) {
            try {
                allProjectMilestone = getMilestonesApi().getMilestones(projectIdOrPath);
            } catch (GitLabApiException e) {
                allProjectMilestone =  new ArrayList<>();
            }
        }

        return allProjectMilestone;
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

    public MilestonesApi getMilestonesApi() {
        if (milestonesApi == null) {
            GitLabApi glApi =getGitLabApi();
            milestonesApi = new MilestonesApi(glApi);
        }
        return milestonesApi;
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

    public UserApi getUserApi() {
        if (userApi == null) {
            userApi = getGitLabApi().getUserApi();
        }
        return  userApi;
    }

    public DiscussionsApi getDiscussionsApi() {
        if (discussionsApi == null) {
            discussionsApi = getGitLabApi().getDiscussionsApi();
        }
        return discussionsApi;
    }
}
