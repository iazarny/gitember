package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * A named group of projects/repositories. {@link Settings} may hold several workspaces,
 * letting the user keep unrelated sets of repositories together (e.g. "Work", "Personal").
 * Workspaces are independent of the flat {@link Settings#getProjects()} recent-projects list.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class Workspace implements Serializable, Comparable<Workspace> {

    private String name;

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<Project> projects = new TreeSet<>();

    public Workspace() {
    }

    public Workspace(String name) {
        this.name = name;
    }

    public Workspace(String name, TreeSet<Project> projects) {
        this.name = name;
        this.projects = projects != null ? projects : new TreeSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSet<Project> getProjects() {
        if (projects == null) {
            projects = new TreeSet<>();
        }
        return projects;
    }

    public void setProjects(TreeSet<Project> projects) {
        this.projects = projects != null ? projects : new TreeSet<>();
    }

    @Override
    public int compareTo(Workspace o) {
        String a = name != null ? name : "";
        String b = o.name != null ? o.name : "";
        return a.toLowerCase().compareTo(b.toLowerCase());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Workspace)) {
            return false;
        }
        String a = name != null ? name : "";
        String b = ((Workspace) obj).name;
        return a.equals(b != null ? b : "");
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
