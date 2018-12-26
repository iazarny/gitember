package com.az.gitember.scm.impl.git;


import static java.util.function.Predicate.isEqual;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.eclipse.jgit.lib.FileMode.GITLINK;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.eclipse.jgit.lib.FileMode.SYMLINK;

import java.util.*;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.blame.BlameResult;
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
            Iterable<RevCommit> logs = cmd.call();
            int k = 0;
            for (RevCommit commit : logs) {
                String commitID = commit.getName();
                if (commitID != null && !commitID.isEmpty()) {

                    BlameCommand blamer = new BlameCommand(db);
                    blamer.setStartCommit(commit.getId());

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
                                String filePath = tw.getPathString();
                                fileList.add(filePath);

                                blamer.setFilePath(filePath);
                                BlameResult blame = blamer.call();

//https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/ShowBlame.java

                            } else {
                                deletedList.add(tw.getPathString());

                            }
                        }
                    }
                }
            }



            rawStatMap.keySet().stream().forEach(
                    r -> {
                        if (prevList != null) {
                            rawStatMap.get(r).addAll(prevList);
                            rawStatMap.get(r).removeAll(deleted.get(r));
                        }
                        prevList = rawStatMap.get(r);
                    }
            );

            rawStatMap.keySet().stream().forEach(
                    r -> {
                        System.out.println("\n\n--------- " + r.getId() + " " +r.getCommitTime() + " " + r.getShortMessage());
                        rawStatMap.get(r).stream().filter(f -> f.equals("aaa.txt")).forEach(
                                f -> System.out.print(" " + f)

                        );
                    }
            );

        }


    }

}
