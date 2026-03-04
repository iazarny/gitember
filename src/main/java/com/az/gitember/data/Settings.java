package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Set;
import java.util.TreeSet;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)

public class Settings {

    /** Default file extensions ignored in folder comparison (compile/build artefacts). */
    public static final Set<String> DEFAULT_IGNORE_COMPARE_FILES = new TreeSet<>(Set.of(
            // JVM
            "class", "jar", "war", "ear", "aar",
            // C / C++
            "obj", "o", "a", "lib", "pch",
            // native binaries
            "dll", "so", "dylib", "exe", "out",
            // Python
            "pyc", "pyo", "pyd",
            // .NET / debug
            "pdb", "idb", "ilk",
            // Rust
            "rlib", "rmeta"
    ));

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<String> commitMsg = new TreeSet<>();

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<String> searchTerms = new TreeSet<>();

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<Project> projects = new TreeSet<>();

    private String theme;
    private int fontSize = 13;

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

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    @JsonDeserialize(as = TreeSet.class)
    private TreeSet<String> ignoreCompareFiles = new TreeSet<>();

    public TreeSet<String> getIgnoreCompareFiles() {
        return ignoreCompareFiles;
    }

    public void setIgnoreCompareFiles(TreeSet<String> ignoreCompareFiles) {
        this.ignoreCompareFiles = ignoreCompareFiles;
    }

    /**
     * Returns the configured ignore-extensions set, or the built-in default when the
     * stored set is absent or empty.
     */
    public Set<String> getEffectiveIgnoreCompareFiles() {
        return (ignoreCompareFiles == null || ignoreCompareFiles.isEmpty())
                ? DEFAULT_IGNORE_COMPARE_FILES
                : ignoreCompareFiles;
    }
}
