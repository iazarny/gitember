package com.az.gitember.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocGenServiceTest {

    private Path repoDir;
    private Repository repository;
    private Git git;
    private final DocGenService docGen = new DocGenService();

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-docgen-");
        git = Git.init().setDirectory(repoDir.toFile()).call();
        repository = git.getRepository();
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
        Files.writeString(repoDir.resolve("a.txt"), "hello\n");
        git.add().addFilepattern("a.txt").call();
        git.commit().setMessage("initial").call();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (git != null) {
            git.close();
        }
        deleteDirectory(repoDir);
    }

    @Test
    void commandLine_nullCommand_returnsEmptyList() {
        assertEquals(List.of(), docGen.commandLine(null));
    }

    @Test
    void commandLine_addFile_isGitAdd() {
        var command = git.add().addFilepattern("a.txt");
        assertEquals(List.of("git add -- a.txt"), docGen.commandLine(command));
    }

    @Test
    void commandLine_rmCached_isGitRmCached() {
        var command = git.rm().setCached(true).addFilepattern("a.txt");
        assertEquals(List.of("git rm --cached -- a.txt"), docGen.commandLine(command));
    }

    @Test
    void commandLine_resetPath_isGitResetPath() {
        var command = git.reset().addPath("a.txt");
        assertEquals(List.of("git reset -- a.txt"), docGen.commandLine(command));
    }

    @Test
    void commandLine_resetHardHead_isGitResetHard() {
        var command = git.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setRef(Constants.HEAD);
        assertEquals(List.of("git reset --hard HEAD"), docGen.commandLine(command));
    }

    @Test
    void commandLine_commitAmendSigned_includesFlagsAndMessage() {
        var command = git.commit()
                .setAmend(true)
                .setSign(true)
                .setAuthor("Ann", "ann@example.com")
                .setMessage("fix it");
        assertEquals(
                List.of("git commit --author='Ann <ann@example.com>' --amend -S -m 'fix it'"),
                docGen.commandLine(command));
    }

    @Test
    void commandLine_checkoutExistingBranch() {
        var command = git.checkout().setCreateBranch(false).setName("master");
        assertEquals(List.of("git checkout master"), docGen.commandLine(command));
    }

    @Test
    void commandLine_checkoutNewBranchFromStartPoint() {
        var command = git.checkout()
                .setCreateBranch(true)
                .setName("feature")
                .setStartPoint("master");
        assertEquals(List.of("git checkout -b feature master"), docGen.commandLine(command));
    }

    @Test
    void commandLine_createAndRenameAndDeleteBranch() {
        var create = git.branchCreate().setName("topic").setStartPoint("master");
        assertEquals(List.of("git branch topic master"), docGen.commandLine(create));

        var rename = git.branchRename().setOldName("topic").setNewName("renamed");
        assertEquals(List.of("git branch -m topic renamed"), docGen.commandLine(rename));

        var delete = git.branchDelete().setBranchNames("renamed").setForce(true);
        assertEquals(List.of("git branch -D -- renamed"), docGen.commandLine(delete));
    }

    @Test
    void commandLine_mergeSquashNoFf() throws Exception {
        var command = git.merge()
                .include(repository.exactRef("refs/heads/master"))
                .setMessage("merge master")
                .setSquash(true)
                .setFastForward(MergeCommand.FastForwardMode.NO_FF);
        assertEquals(
                List.of("git merge --squash --no-ff -m 'merge master' master"),
                docGen.commandLine(command));
    }

    @Test
    void commandLine_rebaseOperations() throws Exception {
        var cont = git.rebase().setOperation(RebaseCommand.Operation.CONTINUE);
        assertEquals(List.of("git rebase --continue"), docGen.commandLine(cont));

        var abort = git.rebase().setOperation(RebaseCommand.Operation.ABORT);
        assertEquals(List.of("git rebase --abort"), docGen.commandLine(abort));

        var skip = git.rebase().setOperation(RebaseCommand.Operation.SKIP);
        assertEquals(List.of("git rebase --skip"), docGen.commandLine(skip));

        var onto = git.rebase().setUpstream("master").setPreserveMerges(true);
        assertEquals(List.of("git rebase --rebase-merges master"), docGen.commandLine(onto));
    }

    @Test
    void commandLine_logAndDiffAndGc() throws Exception {
        ObjectId head = repository.resolve(Constants.HEAD);
        var log = git.log().add(head).setMaxCount(10);
        assertEquals(List.of("git log -n 10"), docGen.commandLine(log));

        var diff = git.diff().setCached(true);
        assertEquals(List.of("git diff --cached"), docGen.commandLine(diff));

        var gc = git.gc();
        assertEquals(List.of("git gc"), docGen.commandLine(gc));
    }

    @Test
    void commandLine_stashAndNotesAndTag() throws Exception {
        var stashList = git.stashList();
        assertEquals(List.of("git stash list"), docGen.commandLine(stashList));

        var stashDrop = git.stashDrop().setStashRef(2);
        assertEquals(List.of("git stash drop stash@{2}"), docGen.commandLine(stashDrop));

        var stashApply = git.stashApply().setStashRef("stash@{0}");
        assertEquals(List.of("git stash apply stash@{0}"), docGen.commandLine(stashApply));

        var stashCreate = git.stashCreate().setWorkingDirectoryMessage("wip");
        assertEquals(List.of("git stash push -m wip"), docGen.commandLine(stashCreate));

        RevCommit head = git.log().setMaxCount(1).call().iterator().next();
        var notesAdd = git.notesAdd().setObjectId(head).setMessage("reviewed");
        List<String> notes = docGen.commandLine(notesAdd);
        assertEquals(1, notes.size());
        assertTrue(notes.get(0).startsWith("git notes add --force -m reviewed "));
        assertTrue(notes.get(0).endsWith(head.getName()));

        var tag = git.tag().setName("v1").setAnnotated(true).setForceUpdate(true).setSigned(false);
        assertEquals(List.of("git tag --force -a v1"), docGen.commandLine(tag));

        var tagDelete = git.tagDelete().setTags("v1");
        assertEquals(List.of("git tag -d -- v1"), docGen.commandLine(tagDelete));
    }

    @Test
    void commandLine_listBranchesAndSubmodules() {
        var all = git.branchList().setListMode(ListBranchCommand.ListMode.ALL);
        assertEquals(List.of("git branch -a"), docGen.commandLine(all));

        var remotes = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE);
        assertEquals(List.of("git branch -r"), docGen.commandLine(remotes));

        var init = git.submoduleInit().addPath("vendor/lib");
        assertEquals(List.of("git submodule init -- vendor/lib"), docGen.commandLine(init));

        var update = git.submoduleUpdate().addPath("vendor/lib");
        assertEquals(List.of("git submodule update -- vendor/lib"), docGen.commandLine(update));

        var add = git.submoduleAdd().setURI("https://example.com/lib.git").setPath("vendor/lib");
        assertEquals(
                List.of("git submodule add https://example.com/lib.git vendor/lib"),
                docGen.commandLine(add));

        var deinit = git.submoduleDeinit().addPath("vendor/lib").setForce(true);
        assertEquals(List.of("git submodule deinit --force -- vendor/lib"), docGen.commandLine(deinit));
    }

    private static void deleteDirectory(Path dir) throws Exception {
        if (dir != null && Files.exists(dir)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws java.io.IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, java.io.IOException exc) throws java.io.IOException {
                    Files.deleteIfExists(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
