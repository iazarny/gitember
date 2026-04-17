package com.az.gitember.data;

/**
 * Represents a Git worktree entry as reported by {@code git worktree list --porcelain}.
 */
public class WorktreeInfo {

    private final String path;
    private final String head;
    private final String branch;   // null when detached
    private final boolean main;
    private final boolean locked;
    private final boolean prunable;

    public WorktreeInfo(String path, String head, String branch,
                        boolean main, boolean locked, boolean prunable) {
        this.path     = path;
        this.head     = head;
        this.branch   = branch;
        this.main     = main;
        this.locked   = locked;
        this.prunable = prunable;
    }

    public String getPath()   { return path; }
    public String getHead()   { return head; }

    /** Short branch name, e.g. {@code feature/xyz}. {@code null} if HEAD is detached. */
    public String getBranch() {
        if (branch == null) return null;
        // refs/heads/feature → feature
        return branch.startsWith("refs/heads/") ? branch.substring("refs/heads/".length()) : branch;
    }

    public boolean isMain()     { return main; }
    public boolean isLocked()   { return locked; }
    public boolean isPrunable() { return prunable; }

    public String getShortHead() {
        return (head != null && head.length() >= 7) ? head.substring(0, 7) : (head != null ? head : "");
    }

    @Override
    public String toString() { return path; }
}
