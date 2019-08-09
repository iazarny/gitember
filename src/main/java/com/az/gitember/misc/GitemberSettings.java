package com.az.gitember.misc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Created by Igor_Azarny on 07 -Jan -2017.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class GitemberSettings implements Serializable {


    private String lastLoginName;
    private String lastGitFolder;

    @JsonDeserialize(as=TreeSet.class)
    private TreeSet<String> commitMessages = new TreeSet<>();

    @JsonDeserialize(as=TreeSet.class)
    private TreeSet<GitemberProjectSettings> projects = new TreeSet<>();

    public String getLastLoginName() {
        return lastLoginName;
    }

    public void setLastLoginName(String lastLoginName) {
        this.lastLoginName = lastLoginName;
    }

    public String getLastGitFolder() {
        return lastGitFolder;
    }

    public void setLastGitFolder(String lastGitFolder) {
        this.lastGitFolder = lastGitFolder;
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

    public GitemberProjectSettings getLastProjectSettings() {
        Optional<GitemberProjectSettings> gpsOpt = projects.stream().filter(
                gps -> {  return gps.getProjectHameFolder().equalsIgnoreCase(lastGitFolder); }
        ).findFirst();
        if (gpsOpt.isPresent()) {
            return gpsOpt.get();
        }
        return null;
    }

    public void addGitemberProjectSettings(GitemberProjectSettings ps) {
        projects.add(ps);
    }
}
