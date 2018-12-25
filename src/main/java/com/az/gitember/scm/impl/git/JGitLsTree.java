package com.az.gitember.scm.impl.git;


import static java.util.function.Predicate.isEqual;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.eclipse.jgit.lib.FileMode.GITLINK;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.eclipse.jgit.lib.FileMode.SYMLINK;

import java.util.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.QuotedString;

/**
 * Created by igor on 23.12.2018.
 */
public class JGitLsTree {

    static Set<String> prevList = null;

    public static void main(String args[]) throws Exception {


        TreeMap<RevCommit, Set<String>> rawStatMap = new TreeMap<>(
                new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return Integer.valueOf(((RevCommit) o1).getCommitTime()).compareTo(
                                Integer.valueOf(((RevCommit) o2).getCommitTime())
                        );
                    }
                }
        );

        Map<RevCommit, Set<String>> deleted = new HashMap<>();


        Repository db = GitUtil.openRepository("c:\\dev\\gitember\\.git");
        //Repository db = GitUtil.openRepository("C:\\dev\\epam\\dhq-merge-and-normalize\\.git");
        try (Git git = new Git(db)) {
            final LogCommand cmd = git.log()
//                    .add(db.resolve("HEAD"))
                    .add(db.resolve("refs/heads/stat"))
                    .setRevFilter(RevFilter.ALL);
            //.addPath(fileName);
            Iterable<RevCommit> logs = cmd.call();
            int k = 0;
            for (RevCommit commit : logs) {
                String commitID = commit.getName();
                //System.out.println(">>>>>>>>>>>>>>>>>>>>> " + commitID);
                if (commitID != null && !commitID.isEmpty()) {
                    LogCommand logs2 = git.log().all();
                    Repository repository = logs2.getRepository();
                    TreeWalk tw = new TreeWalk(repository);
                    tw.setRecursive(true);

                    RevCommit commitToCheck = commit;
                    tw.addTree(commitToCheck.getTree());
                    for (RevCommit parent : commitToCheck.getParents()) {
                        tw.addTree(parent.getTree());
                    }

                    Set<String> fileList = new HashSet<>();
                    rawStatMap.put(commit, fileList);

                    Set<String> deletedList = new HashSet<>();
                    deleted.put(commit, deletedList);



                    while (tw.next()) {
                        int similarParents = 0;
                        for (int i = 1; i < tw.getTreeCount(); i++)
                            if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
                                similarParents++;
                        if (similarParents == 0) {
                            if (tw.getFileMode(0) != FileMode.MISSING) {
                                fileList.add(tw.getPathString());
                            } else {
                                deletedList.add(tw.getPathString());

                            }
                            //System.out.println("File names: " + tw.getPathString());
                        }
                    }


                }
            }



            rawStatMap.keySet().stream().forEach(
                    r -> {

                        if (prevList != null) {

                            if (deleted.containsKey(r)) {
                                prevList.removeAll(deleted.get(r));
                            }

                            rawStatMap.get(r).addAll(prevList);
                        }
                        prevList = rawStatMap.get(r);
                    }
            );

            rawStatMap.keySet().stream().forEach(
                    r -> {
                        System.out.println("\n--------------- " +r.getId() + "  " + r.getCommitTime() + " " + r.getFullMessage());
                        rawStatMap.get(r).stream().forEach(
                                f -> System.out.print(" " + f)

                        );
                    }
            );

        }




        /*try (RevWalk revWalk = new RevWalk(db);
             TreeWalk tw = new TreeWalk(db)) {
            final ObjectId head = db.resolve("709163ff258ec7e69b0ea91654a28eac069e8752");
            //final ObjectId head = db.resolve("refs/heads/stat");
            if (head == null) {
                return;
            }
            RevCommit c = revWalk.parseCommit(head);
            do {

                CanonicalTreeParser p = new CanonicalTreeParser();
                p.reset(revWalk.getObjectReader(), c.getTree());
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
                c = revWalk.next();
            } while(c != null);

        }*/

    }

}
