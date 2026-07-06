package com.az.gitember.data;

import java.util.Date;

/**
 * Per-repository figures computed off the EDT. {@code error} marks a repository that could
 * not be read (missing / not a git dir); its values are ignored in the aggregate summaries.
 */
public record RepoStats(String branch, int modified, int conflicts,
                         int ahead, int behind, Date lastFetch, boolean error) {
    public static RepoStats failed() {
        return new RepoStats(" ", 0, 0, 0, 0, null, true);
    }
}