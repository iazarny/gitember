package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public enum SignOption {
        // 1. Define the constants and pass custom values to the constructor
        NONE("None"),
        SSH("Ssh"),
        PGP("Pgp");

        // 2. Define a private field to store the value
        private final String option;

        // 3. Create an enum constructor (must be private or package-private)
        private SignOption(String option) {
            this.option = option;
        }

        // 4. Create a public getter method to access the value
        public String getOption() {
            return this.option;
        }
    }

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

    /** Ollama model used by every AI feature when the user has not chosen another one. */
    public static final String DEFAULT_LLM_DETECTOR_MODEL = "qwen2.5-coder";

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
    private String  llmDetectorModel   = DEFAULT_LLM_DETECTOR_MODEL;
    private String   signOption   = SignOption.NONE.getOption();
    private String   signKey;
    private Boolean  signCommit;
    private Boolean  signTag;
    private String  dateFormat;
    private String  timeFormat;



    /** canonicalKey -> the single canonical Project instance. Rebuilt by {@link #internAll()}. */
    @JsonIgnore
    private final transient Map<String, Project> byKey = new HashMap<>();

    /**
     * Canonicalizes so exactly ONE {@link Project} instance exists per normalized home folder,
     * shared by {@link #projects} and every {@link Workspace#getProjects()}. Idempotent.
     * The flat list is processed FIRST so its instance wins as canonical (it is historically
     * the one edited by the project-settings dialog); workspace copies only fill in blanks.
     */
    public void internAll() {
        byKey.clear();
        projects = rebuild(projects);
        for (Workspace ws : getWorkspaces()) {
            ws.setProjects(rebuild(ws.getProjects()));
        }
    }

    private TreeSet<Project> rebuild(TreeSet<Project> src) {
        TreeSet<Project> out = new TreeSet<>();
        if (src != null) {
            for (Project p : src) {
                Project c = intern(p);
                if (c != null) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    /** Interns {@code candidate}: returns the existing canonical instance (merging blanks from
     *  {@code candidate}), or registers {@code candidate} itself as canonical if none exists yet. */
    public Project intern(Project candidate) {
        if (candidate == null || candidate.getProjectHomeFolder() == null) {
            return null;
        }
        // Jackson bypasses the setter (setterVisibility = NONE), so normalize the field here too.
        candidate.setProjectHomeFolder(Project.normalizeHome(candidate.getProjectHomeFolder()));
        String key = Project.canonicalKey(candidate.getProjectHomeFolder());
        Project existing = byKey.get(key);
        if (existing == null) {
            byKey.put(key, candidate);
            return candidate;
        }
        existing.mergeMissingFrom(candidate);
        return existing;
    }

    /** Lookup without mutation or creation. */
    public Optional<Project> lookupProject(String homeFolder) {
        return Optional.ofNullable(byKey.get(Project.canonicalKey(homeFolder)));
    }

    /**
     * The single factory for obtaining the canonical {@link Project} for a home folder,
     * creating and interning one if it doesn't exist yet. Deliberately does NOT touch the
     * flat recent-projects list — use {@link #addRecentProject(Project)} for that.
     */
    public Project getOrCreateProject(String homeFolder) {
        String key = Project.canonicalKey(homeFolder);
        Project p = byKey.get(key);
        if (p == null) {
            p = new Project(Project.normalizeHome(homeFolder), new Date());
            byKey.put(key, p);
        }
        return p;
        //return new Project(Project.normalizeHome(homeFolder), new Date());
    }

    /** Adds (or, if already present, bumps the open time of) a project in the flat recent list. */
    public void addRecentProject(Project p) {
        if (p == null) {
            return;
        }
        Project c = intern(p);
        if (!projects.contains(c)) {
            projects.add(c);
        } else {
            c.setOpenTime(new Date());
        }
    }

    /** Removes a project from the flat recent list; un-interns it only if no workspace still references it. */
    public void removeProject(Project p) {
        if (p == null) {
            return;
        }
        projects.remove(p);
        boolean stillUsed = getWorkspaces().stream().anyMatch(ws -> ws.getProjects().contains(p));
        if (!stillUsed) {
            byKey.remove(Project.canonicalKey(p.getProjectHomeFolder()));
        }
    }

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

    public String getSignOption() {
        return signOption;
    }

    public void setSignOption(String signOption) {
        this.signOption = signOption;
    }

    public String getSignKey() {
        return signKey;
    }


    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public Boolean getSignCommit() {
        return signCommit;
    }

    public void setSignCommit(Boolean signCommit) {
        this.signCommit = signCommit;
    }

    public Boolean getSignTag() {
        return signTag;
    }

    public void setSignTag(Boolean signTag) {
        this.signTag = signTag;
    }

    /** Never blank: an unset or cleared model falls back to {@link #DEFAULT_LLM_DETECTOR_MODEL}. */
    public String getLlmDetectorModel() {
        return llmDetectorModel != null && !llmDetectorModel.isBlank()
                ? llmDetectorModel : DEFAULT_LLM_DETECTOR_MODEL;
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
