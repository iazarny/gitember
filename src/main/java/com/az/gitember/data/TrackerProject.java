package com.az.gitember.data;

/**
 * A project/space in an issue tracker (Jira project, Linear team, …).
 */
public class TrackerProject {

    private String key;
    private String name;

    public TrackerProject() {
    }

    public TrackerProject(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String label = key;
        if (name != null && !name.isBlank() && !name.equals(key)) {
            label = key + " — " + name;
        }
        return label;
    }
}
