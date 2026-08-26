package com.az.gitember.ui.support;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Temp-repo builders for the AssertJ-Swing UI suite. Mirrors the real on-disk-repo
 * pattern already used by {@code GitRepoServiceTest} in the service-layer tests.
 */
public final class GitFixtures {

    private GitFixtures() {
    }

    public static Path newInitializedRepo() throws Exception {
        return newInitializedRepo("gitember-ui-repo-", "master");
    }

    /**
     * Creates a temp repository whose folder name starts with {@code namePrefix} and whose
     * initial branch is {@code initialBranch}. The branch is pinned explicitly rather than left
     * to JGit's default so tests can name the branch they check out back to ("master" here)
     * regardless of any {@code init.defaultBranch} the host git config may set.
     */
    public static Path newInitializedRepo(String namePrefix, String initialBranch) throws Exception {
        Path dir = Files.createTempDirectory(namePrefix);
        try (Git git = Git.init()
                .setDirectory(dir.toFile())
                .setInitialBranch(initialBranch)
                .call()) {
            git.getRepository().getConfig().setString("user", null, "name", "UI Test");
            git.getRepository().getConfig().setString("user", null, "email", "ui-test@example.com");
            git.getRepository().getConfig().save();
        }
        return dir;
    }

    /** Writes {@code fileName} with {@code content} and stages it, leaving it uncommitted. */
    public static void writeAndStageFile(Path repoDir, String fileName, String content) throws Exception {
        Files.writeString(repoDir.resolve(fileName), content);
        try (Git git = Git.open(repoDir.toFile())) {
            git.add().addFilepattern(fileName).call();
        }
    }

    /** Paths tracked in the repository's HEAD commit, or an empty set when HEAD is unborn. */
    public static Set<String> filesInHead(Path repoDir) throws Exception {
        Set<String> files = new HashSet<>();
        try (Git git = Git.open(repoDir.toFile())) {
            Repository repository = git.getRepository();
            ObjectId head = repository.resolve(Constants.HEAD);
            if (head != null) {
                try (RevWalk revWalk = new RevWalk(repository);
                     TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(revWalk.parseCommit(head).getTree());
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        files.add(treeWalk.getPathString());
                    }
                }
            }
        }
        return files;
    }

    /**
     * The current branch, or {@code null} when the repository cannot be read. Suited to
     * {@code Pause}/{@code Condition} polling, where a checked exception can't escape.
     */
    public static String currentBranchOrNull(Path repoDir) {
        try {
            return getBranchName(repoDir);
        } catch (Exception e) {
            return null;
        }
    }

    /** Writes {@code fileName} with {@code content}, stages it, and commits it. */
    public static void commitFile(Path repoDir, String fileName, String content, String message) throws Exception {
        Files.writeString(repoDir.resolve(fileName), content);
        try (Git git = Git.open(repoDir.toFile())) {
            git.add().addFilepattern(fileName).call();
            git.commit().setMessage(message).call();
        }
    }

    public static void commitFile(Path repoDir, String fileName,  String message) throws Exception {
        try (Git git = Git.open(repoDir.toFile())) {
            git.add().addFilepattern(fileName).call();
            git.commit().setMessage(message).call();
        }
    }


    public static void createBranch(Path repoDir, String branchName) throws Exception {
        try (Git git = Git.open(repoDir.toFile())) {
            //git.checkout().setCreateBranch(true).setName("branchName").call();
            git.branchCreate().setName(branchName).call();
        }
    }

    public static void checkoutBranch(Path repoDir, String branch) throws Exception {
        try (Git git = Git.open(repoDir.toFile())) {
            git.checkout().setName(branch).call();
        }
    }

    public static String getBranchName(Path repoDir) throws Exception {
        try (Git git = Git.open(repoDir.toFile())) {
            Repository repository = git.getRepository();

            // Returns the short branch name (e.g., "main", "dev")
            return repository.getBranch();
        }
    }

    public static MergeResult mergeBranch(Path repoDir, String source, String destinantion) throws IOException, GitAPIException {
        try (Git git = Git.open(repoDir.toFile())) {
            Repository repository = git.getRepository();
            git.checkout().setName(destinantion).call();
            ObjectId sourceBranchHead = repository.resolve("refs/heads/" + source);
            MergeResult result = git.merge()
                    .include(source, sourceBranchHead)
                    .setMessage("Merge feature-branch into main")
                    .call();
            return result;
        }
    }

    public static void delete(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        }
    }



}
