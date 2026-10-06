package com.az.gitember.data;

/**
 * A workflow transition (or current status) for an issue in an issue tracker.
 */
public class IssueStatus {

    private String id;
    private String name;

    public IssueStatus() {
    }

    public IssueStatus(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
