package com.az.gitember.data;

/**
 * Represents a pull request (GitHub) or merge request (GitLab).
 */
public record PullRequest(
        int number,
        String title,
        String author,
        String sourceBranch,
        String targetBranch,
        String webUrl,
        String state) {

    /**
     * True when this PR is still open. Normalises the per-host "open" wording:
     * GitHub/Gitea {@code open}, GitLab {@code opened}, Bitbucket {@code OPEN},
     * Azure DevOps {@code active}.
     */
    public boolean isOpen() {
        if (state == null) return true;
        String s = state.toLowerCase();
        return s.equals("open") || s.equals("opened") || s.equals("active");
    }

    @Override
    public String toString() {
        return "#" + number + " " + title;
    }
}
