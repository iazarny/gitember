package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;

/**
 * Prefixes a commit message with the issue key when the user opted in.
 */
public final class IssueCommitMessage {

    private IssueCommitMessage() {
    }

    public static String applyIssueKey(String message, Issue issue, boolean includeKey) {
        String result = message != null ? message : "";
        if (includeKey && issue != null && issue.isPresent()) {
            if (!IssueKeyDetector.contains(result, issue.getKey())) {
                String body = result.trim();
                if (body.isEmpty() && issue.getTitle() != null && !issue.getTitle().isBlank()) {
                    result = issue.getKey() + " " + issue.getTitle();
                } else if (body.isEmpty()) {
                    result = issue.getKey();
                } else {
                    result = issue.getKey() + " " + body;
                }
            }
        }
        return result;
    }
}
