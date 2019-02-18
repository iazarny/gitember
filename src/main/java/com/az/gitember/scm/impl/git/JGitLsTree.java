package com.az.gitember.scm.impl.git;


import static java.util.function.Predicate.isEqual;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.eclipse.jgit.lib.FileMode.GITLINK;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.eclipse.jgit.lib.FileMode.SYMLINK;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.Triplet;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.QuotedString;
import sun.misc.IOUtils;

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
            for (RevCommit commit : logs) {
                String commitID = commit.getName();
                if (!commitID.isEmpty()) {


                    //LogCommand logs2 = git.log().all();
                    //Repository repository = logs2.getRepository();
                    //TreeWalk tw = new TreeWalk(repository);
                    TreeWalk tw = new TreeWalk(db);
                    tw.setRecursive(true);

                    tw.addTree(commit.getTree());
                    for (RevCommit parent : commit.getParents()) {
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


            TreeMap<Integer, Pair<RevCommit, Set<String>>> normalizedStatMap = new TreeMap<>(
                    Integer::compareTo
            );

            Period period = Period.ofWeeks(1);
            for (int startTime = rawStatMap.firstKey().getCommitTime(); startTime < rawStatMap.lastKey().getCommitTime(); startTime += period.get(ChronoUnit.DAYS) * 24 * 60 * 60) {
                RevCommit nrc = floorRevCommit(rawStatMap, startTime);
                if (nrc != null) {
                    System.out.println(GitemberUtil.intToDate(rawStatMap.floorKey(nrc).getCommitTime()));
                    normalizedStatMap.put(
                            startTime,
                            new Pair<>(nrc, rawStatMap.get(nrc))
                    );
                }

            }


            Map<RevCommit, Map<String, Map<String, Integer>> > blamedRevisions = new TreeMap<RevCommit, Map<String, Map<String, Integer>>>(
                    (o1, o2) -> {
                        return Integer.valueOf(o1.getCommitTime()).compareTo(
                                o2.getCommitTime()
                        );
                    }
            );


            normalizedStatMap.keySet().stream().forEach(
                    revTime -> {

                        RevCommit toBlame = normalizedStatMap.get(revTime).getFirst();

                        System.out.println("toBlame: " + toBlame);

                        blamedRevisions.computeIfAbsent(
                                toBlame,
                                revCommit -> {
                                    Map<String, Map<String, Integer>> fileAuthCnt = new HashMap<String, Map<String, Integer>>();

                                    AtomicInteger ai = new AtomicInteger(0);

                                    normalizedStatMap.get(revTime).getSecond().stream().forEach(
                                            filePath -> {
                                                ai.incrementAndGet();

                                                BlameCommand blamer = new BlameCommand(db);
                                                blamer.setStartCommit(toBlame);
                                                blamer.setFilePath(filePath);
                                                try {
                                                    BlameResult blame = blamer.call();
                                                    Map<String, Integer> authCnt = new HashMap<String, Integer>();
                                                    if (blame != null && blame.getResultContents() != null) {
                                                        int lines = blame.getResultContents().size();
                                                        for (int i = 0; i < lines; i++) {
                                                            RevCommit ccc = blame.getSourceCommit(i);
                                                            String email = ccc.getAuthorIdent().getEmailAddress();
                                                            Integer cnt = authCnt.getOrDefault(email , 0) + 1;
                                                            authCnt.put(email, cnt);
                                                        }
                                                    }
                                                    fileAuthCnt.put(filePath, authCnt);
                                                } catch (GitAPIException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                    );
                                    return fileAuthCnt;
                                }
                        );
                    }
            );

        }


    }





    /*
    * |------|------|------|------|------|------|------|------|------|------|
    * |----|||||--|---|||--|----------------|||||--|---|---|-|--||----|--|
    * |------|----|-----|--|--------------------|------|-----|---|----|--|
    *
    * */

    private static RevCommit floorRevCommit(TreeMap<RevCommit, Set<String>> rawStatMap, int startTime) {
        RevCommit candidate = null;
        for (RevCommit r : rawStatMap.keySet()) {
            if (r.getCommitTime() <= startTime) {
                candidate = r;
            } else {
                break;
            }
        }
        System.out.println("########### " + candidate);
        return candidate;
    }


    private static int countLinesOfFileInCommit(Repository repository, ObjectId commitID, String name) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitID);
            RevTree tree = commit.getTree();

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(name));
                if (!treeWalk.next()) {
                    // throw new IllegalStateException("Did not find expected file 'README.md'");
                    return -1;
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                //TODO skip veery large files loader.getSize()

                // load the content of the file into a stream
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                loader.copyTo(stream);

                revWalk.dispose();

                byte[] arr = stream.toByteArray();
                if (RawText.isBinary(arr)) {
                    return 0;
                }

                ByteArrayInputStream bias = new ByteArrayInputStream(arr);

                LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(bias));
                lineNumberReader.skip(Long.MAX_VALUE);
                return lineNumberReader.getLineNumber();
            }
        } catch (IOException e) {
            return 0;
        }
    }

}
