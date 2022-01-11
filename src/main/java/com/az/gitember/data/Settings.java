package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.TreeSet;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)

public class Settings {

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<String> commitMsg = new TreeSet<>();

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<String> searchTerms = new TreeSet<>();

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<Project> projects = new TreeSet<>();

    private String theme;

    private GitlabSettings gitlabSettings = new GitlabSettings();

    public TreeSet<Project> getProjects() {
        return projects;
    }

    public void setProjects(TreeSet<Project> projects) {
        this.projects = projects;
    }

    public TreeSet<String> getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(TreeSet<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    public TreeSet<String> getCommitMsg() {
        return commitMsg;
    }

    public void setCommitMsg(TreeSet<String> commitMsg) {
        this.commitMsg = commitMsg;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public GitlabSettings getGitlabSettings() {
        if (gitlabSettings == null) {
            gitlabSettings = new GitlabSettings();
        }
        return gitlabSettings;
    }

    public void setGitlabSettings(GitlabSettings gitlabSettings) {
        this.gitlabSettings = gitlabSettings;
    }
}
