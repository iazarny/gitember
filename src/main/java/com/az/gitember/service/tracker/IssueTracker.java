package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueStatus;
import com.az.gitember.data.TrackerProject;

import java.util.List;
import java.util.Optional;

/**
 * Generic issue tracker: Jira, Linear, YouTrack, GitHub Issues, GitLab Issues, …
 * Gitember uses this for issue context around Git work (branches, commits, links).
 */
public interface IssueTracker {

    String getName();

    /**
     * Search issues. An empty query returns assigned / recent open issues in the
     * configured projects.
     */
    List<Issue> search(String query) throws Exception;

    Optional<Issue> getIssue(String key) throws Exception;

    void createIssue(Issue issue) throws Exception;

    void addComment(String issueKey, String comment) throws Exception;

    List<IssueStatus> getStatuses(String issueKey) throws Exception;

    void transition(String issueKey, String transitionId) throws Exception;

    String getIssueUrl(String issueKey);

    List<TrackerProject> listProjects() throws Exception;

    /** Lightweight auth check; returns a display name or email on success. */
    String testConnection() throws Exception;
}
