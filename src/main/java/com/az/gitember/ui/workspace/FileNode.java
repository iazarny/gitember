package com.az.gitember.ui.workspace;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmItem;

public class FileNode {

    private final Project project;
    private final ScmItem item;
    private final String leafName;

    public FileNode(Project project, ScmItem item, String leafName) {
        this.project = project;
        this.item = item;
        this.leafName = leafName;
    }

    public Project getProject() {
        return project;
    }

    public ScmItem getItem() {
        return item;
    }

    public String getLeafName() {
        return leafName;
    }

    String status() {
        return item.getAttribute() != null ? item.getAttribute().getStatus() : "";
    }

}
