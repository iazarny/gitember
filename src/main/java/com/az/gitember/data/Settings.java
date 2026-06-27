package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    /**
     * Named groups of projects/repositories. Independent of the flat {@link #projects}
     * recent-projects list above — a project may appear in both, in several workspaces, or
     * in none.
     */
    private List<Workspace> workspaces = new ArrayList<>();

    private String theme;
    private int fontSize = 13;
    private Boolean enableLeakDetector = false; //EXPERIMENTAL FEATURE
    private Boolean enableBranchCompareDescription = false; //EXPERIMENTAL FEATURE
    private Boolean enableCommitMessageGeneration = false; //EXPERIMENTAL FEATURE
    private String  llmDetectorModel   = "qwen2.5-coder";

    public Boolean getEnableBranchCompareDescription() {
        return enableBranchCompareDescription;
    }

    public void setEnableBranchCompareDescription(Boolean enableBranchCompareDescription) {
        this.enableBranchCompareDescription = enableBranchCompareDescription;
    }

    public Boolean getEnableCommitMessageGeneration() {
        return enableCommitMessageGeneration;
    }

    public void setEnableCommitMessageGeneration(Boolean enableCommitMessageGeneration) {
        this.enableCommitMessageGeneration = enableCommitMessageGeneration;
    }

    public Boolean getEnableLeakDetector() {
        return enableLeakDetector;
    }

    public void setEnableLeakDetector(Boolean enableLeakDetector) {
        this.enableLeakDetector = enableLeakDetector;
    }

    public String getLlmDetectorModel() {
        return llmDetectorModel != null ? llmDetectorModel : "qwen2.5-coder";
    }

    public void setLlmDetectorModel(String llmDetectorModel) {
        this.llmDetectorModel = llmDetectorModel;
    }

    public TreeSet<Project> getProjects() {
        return projects;
    }

    public void setProjects(TreeSet<Project> projects) {
        this.projects = projects;
    }

    public List<Workspace> getWorkspaces() {
        if (workspaces == null) {
            workspaces = new ArrayList<>();
        }
        return workspaces;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces != null ? workspaces : new ArrayList<>();
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
     * Returns the configured ignore-extensions set.
     * Defaults are seeded into this set at startup (see {@code Context.readSettings()}),
     * so what is stored here is exactly what gets used — no hidden fallback.
     */
    public Set<String> getEffectiveIgnoreCompareFiles() {
        return ignoreCompareFiles != null ? ignoreCompareFiles : DEFAULT_IGNORE_COMPARE_FILES;
    }

    /**
     * Returns a workspace name not already in use, e.g. "New workspace",
     * then "New workspace 2", "New workspace 3", and so on.
     */
    public String createNewWorkspaceName() {
        final String base = "New workspace";

        Set<String> existingNames = workspaces.stream()
                .map(Workspace::getName)
                .collect(Collectors.toSet());

        if (!existingNames.contains(base)) {
            return base;
        }
        for (int i = 1; ; i++) {
            String candidate = base + " " + i;
            if (!existingNames.contains(candidate)) {
                return candidate;
            }
        }
    }
}
