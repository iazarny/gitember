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

    @Override
    public String toString() {
        return "#" + number + " " + title;
    }
}
