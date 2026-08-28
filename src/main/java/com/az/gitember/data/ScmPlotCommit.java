package com.az.gitember.data;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * A {@link PlotCommit} that keeps only what the history table paints — the short message and the
 * author name — instead of the whole raw commit body.
 *
 * <p>JGit retains the complete raw bytes of every commit it walks so that {@code getShortMessage()},
 * {@code getFullMessage()} and {@code getAuthorIdent()} stay available. For a full history that is
 * by far the largest allocation in the application: measured over a 26 000-commit repository the
 * plot list costs 1 222 B/commit with bodies and 223 B/commit without. The commit list is retained
 * for as long as the repository is open, so the bodies are paid for permanently while only two
 * short strings per row are ever displayed.
 *
 * <p>{@link com.az.gitember.service.ScmPlotWalk} captures those two strings and releases the body
 * as each commit leaves the walk. Everything that needs the full body afterwards (the commit
 * detail panel, search, Lucene indexing) goes through
 * {@code GitRepoService.adapt(...)}, which re-reads the single commit it was asked about.
 */
public class ScmPlotCommit extends PlotCommit<PlotLane> {

    private String shortMessage;
    private String authorName;

    public ScmPlotCommit(AnyObjectId id) {
        super(id);
    }

    /**
     * Stores the fields to keep once the raw body is released. Called by
     * {@link com.az.gitember.service.ScmPlotWalk} while the body is still loaded.
     */
    public void setSummary(String shortMessage, String authorName) {
        this.shortMessage = shortMessage;
        this.authorName = authorName;
    }

    /**
     * The short message of {@code commit}: the captured copy for a commit whose body has been
     * released, the body itself for any other {@link RevCommit} (file history, stashes, commits
     * from a plain {@code RevWalk}).
     */
    public static String shortMessageOf(RevCommit commit) {
        if (commit == null) {
            return null;
        }
        if (commit instanceof ScmPlotCommit scmCommit && commit.getRawBuffer() == null) {
            return scmCommit.shortMessage;
        }
        return commit.getShortMessage();
    }

    /** The author name of {@code commit}; see {@link #shortMessageOf(RevCommit)}. */
    public static String authorNameOf(RevCommit commit) {
        if (commit == null) {
            return null;
        }
        if (commit instanceof ScmPlotCommit scmCommit && commit.getRawBuffer() == null) {
            return scmCommit.authorName;
        }
        PersonIdent author = commit.getAuthorIdent();
        return author == null ? null : author.getName();
    }
}
