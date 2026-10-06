package com.az.gitember.service.tracker;

import com.az.gitember.data.Issue;
import com.az.gitember.data.IssueTrackerConfig;
import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;
import com.az.gitember.service.Context;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves the configured {@link IssueTracker} for the current repository
 * (Jira or Linear today; YouTrack / GitHub / GitLab later).
 */
public final class IssueTrackerService {

    private static final Logger log = Logger.getLogger(IssueTrackerService.class.getName());

    private IssueTrackerService() {
    }

    public static boolean isConfigured() {
        IssueTrackerConfig config = config();
        return config != null && config.isReady();
    }

    public static IssueTrackerConfig config() {
        return config(Context.getActiveProject());
    }

    public static IssueTrackerConfig config(Project project) {
        IssueTrackerConfig config = null;
        if (project != null) {
            config = project.getIssueTracker();
        } else {
            Workspace workspace = Context.getWorkspace();
            if (workspace != null) {
                for (Project p : workspace.getProjects()) {
                    if (config == null && p.getIssueTracker() != null && p.getIssueTracker().isReady()) {
                        config = p.getIssueTracker();
                    }
                }
            }
        }
        return config;
    }

    public static IssueTracker current() {
        return create(config());
    }

    public static IssueTracker create(IssueTrackerConfig config) {
        IssueTracker tracker = null;
        if (config != null && config.isReady()) {
            String type = config.getType();
            if (IssueTrackerConfig.TYPE_JIRA.equalsIgnoreCase(type)) {
                tracker = new JiraIssueTracker(config);
            } else if (IssueTrackerConfig.TYPE_LINEAR.equalsIgnoreCase(type)) {
                tracker = new LinearIssueTracker(config);
            }
        }
        return tracker;
    }

    public static String issueUrl(String key) {
        String url = "";
        IssueTracker tracker = current();
        if (tracker != null && key != null && !key.isBlank()) {
            url = tracker.getIssueUrl(key);
        }
        return url;
    }

    /**
     * Posts a comment after a successful Git commit. Failures are logged only —
     * they must never undo the local commit.
     */
    public static void commentOnCommitQuietly(Project project, Issue issue, String revision, String branch, String message) {
        IssueTrackerConfig cfg = config(project);
        IssueTracker tracker = create(cfg);
        if (tracker != null && cfg != null && cfg.isCommentOnCommit()
                && issue != null && issue.isPresent()) {
            Thread worker = new Thread(() -> {
                try {
                    StringBuilder body = new StringBuilder("Gitember commit");
                    if (revision != null && !revision.isBlank()) {
                        int len = Math.min(8, revision.length());
                        body.append(' ').append(revision.substring(0, len));
                    }
                    if (branch != null && !branch.isBlank()) {
                        body.append(" on ").append(branch);
                    }
                    body.append("\n\n").append(message != null ? message : "");
                    tracker.addComment(issue.getKey(), body.toString());
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Cannot comment on " + issue.getKey(), ex);
                }
            }, "issue-tracker-comment");
            worker.setDaemon(true);
            worker.start();
        }
    }
}
