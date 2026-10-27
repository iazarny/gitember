package com.az.gitember.ui.workspace;

import com.az.gitember.data.Project;

/**
 * Leaf data for a search hit; rendered by its (file) leaf name.
 * */
public class SearchHit {

    private final Project project;
    private final String path;
    private final String leafName;

    public SearchHit(Project project, String path, String leafName) {
        this.project = project;
        this.path = path;
        this.leafName = leafName;
    }

    public Project getProject() {
        return project;
    }

    public String getPath() {
        return path;
    }

    public String getLeafName() {
        return leafName;
    }

    @Override
    public String toString() {
        return leafName;
    }
}
