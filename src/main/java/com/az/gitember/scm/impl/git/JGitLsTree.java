package com.az.gitember.scm.impl.git;


import static java.util.function.Predicate.isEqual;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.eclipse.jgit.lib.FileMode.GITLINK;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.eclipse.jgit.lib.FileMode.SYMLINK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.QuotedString;

/**
 * Created by igor on 23.12.2018.
 */
public class JGitLsTree {


    public static void main (String args[]) throws Exception {

         List<String> paths = new ArrayList<>();

        Repository db = GitUtil.openRepository("dev/gitember/.git");


        try (RevWalk rw = new RevWalk(db);
             TreeWalk tw = new TreeWalk(db)) {
            final ObjectId head = db.resolve("596dd96c86068f9325f6a457bc57242f7d5779a7");
            if (head == null) {
                return;
            }
            RevCommit c = rw.parseCommit(head);
            CanonicalTreeParser p = new CanonicalTreeParser();
            p.reset(rw.getObjectReader(), c.getTree());
            tw.reset(); // drop the first empty tree, which we do not need here
            if (paths.size() > 0) {
                tw.setFilter(PathFilterGroup.createFromStrings(paths));
            }
            tw.addTree(p);
            tw.addTree(new DirCacheIterator(db.readDirCache()));
            tw.setRecursive(true);
            while (tw.next()) {
                // (filterFileMode(tw, EXECUTABLE_FILE, GITLINK, REGULAR_FILE,  SYMLINK)) {
                    System.out.println(
                            QuotedString.GIT_PATH.quote(tw.getPathString()));
               // }
            }
        }

    }

}
