package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
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

    /** All workspaces. Always contains at least one workspace once accessed (see {@link #getActiveWorkspace()}). */
    private List<Workspace> workspaces = new ArrayList<>();

    /** Name of the currently selected workspace. */
    private String currentWorkspace;

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

    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces != null ? workspaces : new ArrayList<>();
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    /**
     * Returns the currently selected workspace, creating a default one if none exist
     * (e.g. on first run or for settings migrated from the pre-workspace format).
     * Never returns {@code null}.
     */
    @JsonIgnore
    public Workspace getActiveWorkspace() {
        if (workspaces == null) {
            workspaces = new ArrayList<>();
        }
        if (workspaces.isEmpty()) {
            Workspace def = new Workspace(Workspace.DEFAULT_NAME);
            workspaces.add(def);
        }
        Workspace active = null;
        if (currentWorkspace != null) {
            active = workspaces.stream()
                    .filter(w -> currentWorkspace.equals(w.getName()))
                    .findFirst()
                    .orElse(null);
        }
        if (active == null) {
            active = workspaces.get(0);
            currentWorkspace = active.getName();
        }
        return active;
    }

    /**
     * Backward-compatible view of the active workspace's projects. Existing callers
     * (UI panels, {@code Context}) continue to operate on the selected workspace.
     */
    @JsonIgnore
    public TreeSet<Project> getProjects() {
        return getActiveWorkspace().getProjects();
    }

    @JsonIgnore
    public void setProjects(TreeSet<Project> projects) {
        getActiveWorkspace().setProjects(projects);
    }

    /**
     * Legacy migration hook: settings written before workspaces existed stored a flat
     * {@code projects} list. When such a file is read, fold those projects into the
     * default workspace. Deserialize-only — never written back (see {@link #getProjects()}).
     */
    @JsonSetter("projects")
    public void setLegacyProjects(TreeSet<Project> legacyProjects) {
        if (legacyProjects == null || legacyProjects.isEmpty()) {
            return;
        }
        if (workspaces == null) {
            workspaces = new ArrayList<>();
        }
        Workspace target = workspaces.stream()
                .filter(w -> Workspace.DEFAULT_NAME.equals(w.getName()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            target = new Workspace(Workspace.DEFAULT_NAME);
            workspaces.add(target);
        }
        target.getProjects().addAll(legacyProjects);
        if (currentWorkspace == null) {
            currentWorkspace = target.getName();
        }
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
}
