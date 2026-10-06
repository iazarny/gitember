package com.az.gitember.data;

/**
 * One work item from an {@link com.az.gitember.service.tracker.IssueTracker}
 * (Jira, Linear, GitHub Issues, …). Gitember uses this as the current work context
 * for branch names, commit messages, and links.
 */
public class Issue {

    private String key;
    private String title;
    private String description;
    private String status;
    private String assignee;
    private String url;

    public Issue() {
    }

    public Issue(String key, String title, String description, String status, String assignee, String url) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignee = assignee;
        this.url = url;
    }

    public static Issue none() {
        return new Issue("", "(none)", null, null, null, null);
    }

    public boolean isPresent() {
        return key != null && !key.isBlank();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        String label;
        if (!isPresent()) {
            label = "(none)";
        } else if (title == null || title.isBlank()) {
            label = key;
        } else {
            label = key + " — " + title;
        }
        return label;
    }
}
