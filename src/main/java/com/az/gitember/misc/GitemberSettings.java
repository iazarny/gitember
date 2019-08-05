package com.az.gitember.misc;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Created by Igor_Azarny on 07 -Jan -2017.
 */
public class GitemberSettings implements Serializable {


    private String lastLoginName;
    private String lastProject;
    private TreeSet<String> commitMessages = new TreeSet<>();
    private TreeSet<GitemberProjectSettings> projects = new TreeSet<>();

    public String getLastLoginName() {
        return lastLoginName;
    }

    public void setLastLoginName(String lastLoginName) {
        this.lastLoginName = lastLoginName;
    }

    public String getLastProject() {
        return lastProject;
    }

    public void setLastProject(String lastProject) {
        this.lastProject = lastProject;
    }

    public TreeSet<String> getCommitMessages() {
        return commitMessages;
    }

    public void setCommitMessages(TreeSet<String> commitMessages) {
        this.commitMessages = commitMessages;
    }

    public TreeSet<GitemberProjectSettings> getProjects() {
        return projects;
    }

    public void setProjects(TreeSet<GitemberProjectSettings> projects) {
        this.projects = projects;
    }
}
