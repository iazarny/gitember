package com.az.gitember.service;

import com.az.gitember.data.ScmPlotCommit;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link PlotWalk} that produces {@link ScmPlotCommit}s and releases each commit's raw body as
 * soon as the commit leaves the walk, keeping only the short message and author name the history
 * table paints.
 *
 * <p>The summary is taken in {@link #next()} rather than by walking the finished list, because
 * the body has to be read before it is dropped and re-reading it later would mean a second pass
 * over the object database.
 */
public class ScmPlotWalk extends PlotWalk {

    private final Map<String, String> authorNamePool = new HashMap<>();

    public ScmPlotWalk(Repository repo) {
        super(repo);
    }

    @Override
    protected RevCommit createCommit(AnyObjectId id) {
        return new ScmPlotCommit(id);
    }

    @Override
    public RevCommit next() throws MissingObjectException, IncorrectObjectTypeException, IOException {
        RevCommit commit = super.next();
        if (commit instanceof ScmPlotCommit scmCommit && commit.getRawBuffer() != null) {
            PersonIdent author = commit.getAuthorIdent();
            byte[] signature = commit.getRawGpgSignature();
            scmCommit.setSummary(commit.getShortMessage(),
                    author == null ? null : pooled(author.getName()),
                    signature != null);
            scmCommit.disposeBody();
        }
        return commit;
    }

    /**
     * One shared instance per distinct author name. A repository has orders of magnitude fewer
     * authors than commits, and {@code PersonIdent} builds a fresh string for each commit, so
     * pooling here removes most of what the summaries cost. The pool itself is garbage once the
     * walk is closed -- only the strings the commits point at survive.
     */
    private String pooled(String authorName) {
        if (authorName == null) {
            return null;
        }
        String existing = authorNamePool.putIfAbsent(authorName, authorName);
        return existing != null ? existing : authorName;
    }
}
