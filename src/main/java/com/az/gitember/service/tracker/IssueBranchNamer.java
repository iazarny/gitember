package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;

/**
 * Builds a local branch name from an issue and the configured pattern,
 * e.g. {@code feature/PAY-123-fix-payment-timeout}.
 */
public final class IssueBranchNamer {

    private static final int SLUG_MAX = 48;

    private IssueBranchNamer() {
    }

    public static String branchName(Issue issue, IssueTrackerConfig config) {
        String name = "";
        if (issue != null && issue.isPresent()) {
            IssueTrackerConfig cfg = config != null ? config : new IssueTrackerConfig();
            String pattern = cfg.getBranchNamePattern();
            String type = cfg.getBranchType();
            String slug = slugify(issue.getTitle());
            name = pattern
                    .replace("{type}", type)
                    .replace("{key}", issue.getKey())
                    .replace("{slug}", slug);
            name = name.replaceAll("/+", "/").replaceAll("^/|/$", "");
            if (name.endsWith("-")) {
                name = name.substring(0, name.length() - 1);
            }
        }
        return name;
    }

    public static String slugify(String title) {
        String slug = "";
        if (title != null && !title.isBlank()) {
            slug = title.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "");
            if (slug.length() > SLUG_MAX) {
                slug = slug.substring(0, SLUG_MAX).replaceAll("-+$", "");
            }
        }
        return slug;
    }
}
