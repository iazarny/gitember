package com.az.gitember.data;

/**
 * Represents a Git submodule entry with its current status relative to the parent repository.
 */
public class Submodule {

    public enum Status {
        /** Not yet initialized — `git submodule init` has not been run. */
        UNINITIALIZED,
        /** Checked out at exactly the commit recorded in the parent index. */
        UP_TO_DATE,
        /** Checked out at a different commit than recorded in the parent index. */
        MODIFIED,
        /** Submodule directory exists but HEAD cannot be resolved. */
        MISSING
    }

    private final String name;
    private final String path;
    private final String url;
    private final Status status;
    /** Abbreviated HEAD SHA of the submodule repository, empty if uninitialized. */
    private final String headSha;
    /** Abbreviated SHA recorded in the parent's index. */
    private final String indexSha;

    public Submodule(String name, String path, String url,
                     Status status, String headSha, String indexSha) {
        this.name     = name;
        this.path     = path;
        this.url      = url;
        this.status   = status;
        this.headSha  = headSha;
        this.indexSha = indexSha;
    }

    public String getName()     { return name; }
    public String getPath()     { return path; }
    public String getUrl()      { return url; }
    public Status getStatus()   { return status; }
    public String getHeadSha()  { return headSha; }
    public String getIndexSha() { return indexSha; }

    @Override
    public String toString() { return name; }
}
