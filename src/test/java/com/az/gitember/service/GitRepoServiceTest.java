package com.az.gitember.service;

import com.az.gitember.data.CommitInfo;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.data.ScmStat;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.EmptyProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitRepoServiceTest {

    private static final String README_FILE = "README.md";
    private static final String README_FILE2 = "README2.md";
    private static final String README_FILE3 = "README3.md";
    private static final String README_FILE4 = "README4.md";
    private static final String README_FILE_TOO = "README2.md";
    private static final String IGNORE_FILE = ".gitignore";
    private static final String FOLDER = "folder";
    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FN_BR1 = "refs/heads/br1";
    private static final String FN_MASTER = "refs/heads/master";
    
    private static final String README_FILE_BODY = "readme too";
    private static final String README_FILE2_BODY = "readme too";
    private static final String README_FILE3_BODY = "HAL9000";
    private static final String README_FILE4_BODY = "Bender";
    private static final String COMMIT_MSG = "Some commit msg";

    private String tmpGitProject = null;
    private Path tmpGitProjectPath = null;
    private GitRepoService gitRepoService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        tmpGitProjectPath = Files.createTempDirectory("tmp-gtmbr");
        tmpGitProject = tmpGitProjectPath.toAbsolutePath().toString();
        GitRepoService.createRepository(tmpGitProject);
        gitRepoService = new GitRepoService(
                Paths.get(tmpGitProject, ".git").toString()
        );
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        gitRepoService.shutdown();
        try {
            FileUtils.deleteDirectory(new File(tmpGitProject));
        } catch (Exception e) {

        }

    }


    @Test
    public void createRepository() throws Exception {
        String newRepo = Files.createTempDirectory("tmp-gtmbr").toString();
        GitRepoService.createRepository(newRepo, true, true);
        gitRepoService = new GitRepoService(
                Paths.get(newRepo, ".git").toString()
        );

        assertEquals(2, gitRepoService.getAllFiles().size());

        try {
            FileUtils.deleteDirectory(new File(newRepo));

        } catch (Exception e) {

        }

    }

    @Test
    public void addFileToCommitStage() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        DirCache dc = gitRepoService.addFileToCommitStage(README_FILE_TOO);
        assertEquals(1, dc.getEntryCount());

        Files.createDirectories(Paths.get(tmpGitProjectPath.toString(), FOLDER));
        Files.write(Paths.get(tmpGitProjectPath.toString(), FOLDER, FILE_1),
                "test 1".getBytes(), StandardOpenOption.CREATE);
        Files.write(Paths.get(tmpGitProjectPath.toString(), FOLDER, FILE_2),
                "test 2".getBytes(), StandardOpenOption.CREATE);
        dc = gitRepoService.addFileToCommitStage(
                Paths.get(FOLDER).toString() );

        gitRepoService.commit("Add file and two folders", null, null);

        assertEquals(3, dc.getEntryCount());
    }

    @Test
    public void addFileToCommitStageNegative() throws Exception {
        DirCache dc = gitRepoService.addFileToCommitStage("NotExistingFile.txt");
        assertEquals(0, dc.getEntryCount());
    }

    @Test
    public void removeFileFromCommitStage() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        DirCache dc = gitRepoService.addFileToCommitStage(README_FILE_TOO);
        assertEquals(1, dc.getEntryCount());

        Ref ref = gitRepoService.removeFileFromCommitStage(README_FILE_TOO);
        assertNotNull(ref);
    }

    @Test
    public void commit() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        DirCache dc = gitRepoService.addFileToCommitStage(README_FILE_TOO);
        assertEquals(1, dc.getEntryCount());

        RevCommit rc = gitRepoService.commit(COMMIT_MSG, null, null);
        assertEquals(COMMIT_MSG, rc.getShortMessage());
    }

    @Test
    public void getAllFiles() throws Exception {
        assertEquals(0, gitRepoService.getAllFiles().size());
        for (int i = 0; i < 100; i++) {
            Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO + i),
                    README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
            gitRepoService.addFileToCommitStage(README_FILE_TOO + i);
        }
        gitRepoService.commit(COMMIT_MSG, null, null);
        assertEquals(100, gitRepoService.getAllFiles().size());
    }


    @Test
    public void createBranch() throws Exception {
        commit();
        Ref newBranch = gitRepoService.createBranch(FN_MASTER, "br1");
        gitRepoService.checkoutBranch(FN_BR1, null);
        assertEquals(FN_BR1, newBranch.getName());
        assertEquals(1, gitRepoService.getAllFiles(FN_BR1).size());
        assertEquals(1, gitRepoService.getAllFiles(FN_MASTER).size());

        // adding new file
        Files.createDirectories(Paths.get(tmpGitProjectPath.toString(), FOLDER));
        Files.write(
                Paths.get(tmpGitProjectPath.toString(), FOLDER, FILE_1),
                "test".getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(
                Paths.get(FOLDER).toString()
        );
        RevCommit rc = gitRepoService.commit("Added new file to br1", null, null);
        assertEquals("Added new file to br1", rc.getShortMessage());

        gitRepoService.checkoutBranch(FN_MASTER, null);
        assertEquals(1, gitRepoService.getAllFiles(FN_MASTER).size());

        gitRepoService.checkoutBranch(FN_BR1, null);
        assertEquals(2, gitRepoService.getAllFiles(FN_BR1).size());
    }



    @Test
    public void deleteLocalBranchNegative() throws Exception {
        gitRepoService.createBranch(FN_MASTER, "br1");
        gitRepoService.checkoutBranch(FN_BR1, null);
        try {
            gitRepoService.deleteLocalBranch(FN_BR1);
            fail( "Branch refs/heads/br1 is checked out and cannot be deleted");

        } catch (IOException e) {
            assertTrue(true);

        }
    }

    @Test
    public void deleteLocalBranch() throws Exception {
        gitRepoService.createBranch(FN_MASTER, "br1");
        gitRepoService.checkoutBranch(FN_BR1, null);
        gitRepoService.checkoutBranch(FN_MASTER, null);
        try {
            gitRepoService.deleteLocalBranch(FN_BR1);
            assertTrue(true);

        } catch (IOException e) {
            fail();

        }
    }

    @Test
    public void mergeBranch() throws Exception {
        gitRepoService.checkoutBranch(FN_MASTER, "br1", null);

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE_TOO);
        gitRepoService.commit("To br1", null, null);

        gitRepoService.checkoutBranch(FN_MASTER, null);
        assertEquals(0, gitRepoService.getAllFiles(FN_MASTER).size());
        gitRepoService.mergeBranch(FN_BR1, "Merger from br1 to master", true, MergeCommand.FastForwardMode.FF);
        assertEquals(1, gitRepoService.getAllFiles().size());
    }

    @Test
    public void rebaseBranch() throws Exception {
        gitRepoService.checkoutBranch(FN_MASTER, "br1", null);
        gitRepoService.checkoutBranch(FN_MASTER, null);

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE_TOO);
        gitRepoService.commit("To master", null, null);

        gitRepoService.checkoutBranch(FN_BR1, null, null);
        Files.write(Paths.get(tmpGitProjectPath.toString(), FILE_1),
                "file 1".getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(FILE_1);
        gitRepoService.commit("To br1", null, null);

        assertEquals(1, gitRepoService.getAllFiles(FN_BR1).size());

        gitRepoService.rebaseBranch(FN_MASTER);
        assertEquals(2, gitRepoService.getAllFiles(FN_BR1).size());

    }

    @Test
    public void getBranches() throws Exception {
        gitRepoService.checkoutBranch(FN_MASTER, "br1", null);
        assertEquals(2, gitRepoService.getBranches().size());
        gitRepoService.checkoutBranch(FN_MASTER, null);
        gitRepoService.deleteLocalBranch(FN_BR1);
        assertEquals(1, gitRepoService.getBranches().size());
    }

    @Test
    public void getFileHistory() throws Exception {

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE_TOO),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        Files.write(Paths.get(tmpGitProjectPath.toString(), IGNORE_FILE),
                "".getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE_TOO);
        gitRepoService.addFileToCommitStage(IGNORE_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);

        Files.write(Paths.get(tmpGitProject, README_FILE_TOO),
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        gitRepoService.addFileToCommitStage(README_FILE_TOO);
        gitRepoService.commit("Changes in read me", null, null);
        Files.write(Paths.get(tmpGitProject, README_FILE_TOO),
                "\n Bender".getBytes(), StandardOpenOption.APPEND);
        Files.write(Paths.get(tmpGitProject, IGNORE_FILE),
                "\n Bender".getBytes(), StandardOpenOption.APPEND);

        gitRepoService.addFileToCommitStage(README_FILE_TOO);
        gitRepoService.addFileToCommitStage(IGNORE_FILE);
        gitRepoService.commit("Benders added in read me", null, null);

        List<ScmRevisionInformation> lst = gitRepoService.getFileHistory(README_FILE_TOO);
        assertEquals(3, lst.size());

        assertEquals("Benders added in read me", lst.get(0).getShortMessage());
        assertEquals("Changes in read me", lst.get(1).getShortMessage());
        assertEquals(COMMIT_MSG, lst.get(2).getShortMessage());

        lst = gitRepoService.getFileHistory(IGNORE_FILE);
        assertEquals(2, lst.size());
    }

    @Test
    public void createTag() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        gitRepoService.createTag("t0");
        gitRepoService.createTag("t00");
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit("Changes in read me", null, null);
        gitRepoService.createTag("t1");
        List<ScmBranch> lst = gitRepoService.getTags();
        assertEquals(3, lst.size());
        assertEquals("refs/tags/t0", lst.get(0).getShortName());
        assertEquals("refs/tags/t00", lst.get(1).getShortName());
        assertEquals("refs/tags/t1", lst.get(2).getShortName());
    }

    @Test
    public void getHead() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        CommitInfo ci = gitRepoService.getHead();
        assertNotNull(ci);
        assertEquals("refs/heads/master", ci.getName());
    }

    @Test
    public void stash() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);

        Path readmePath = Paths.get(tmpGitProject, README_FILE);
        List<ScmRevisionInformation> lst = gitRepoService.getStashList();
        assertEquals(0, lst.size());

        byte[] readmeBytesOriginal = Files.readAllBytes(readmePath);
        Files.write(readmePath,
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged0 = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged0.length);
        gitRepoService.stash();

        Files.write(readmePath,
                "\n readme changes 1".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged1 = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged1.length);
        gitRepoService.stash();

        lst = gitRepoService.getStashList();
        assertEquals(2, lst.size());


        gitRepoService.applyStash(lst.get(0).getRevisionFullName());
        byte[] readmeBytesApplied = Files.readAllBytes(readmePath);
        assertNotEquals(readmeBytesOriginal.length, readmeBytesApplied.length);

        gitRepoService.deleteStash(0);
        lst = gitRepoService.getStashList();
        assertEquals(1, lst.size());

    }

    @Test
    public void checkoutRevCommit() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        RevCommit rc0 = gitRepoService.commit(COMMIT_MSG, null, null);

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE2),
                README_FILE2_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE2);
        RevCommit rc1 = gitRepoService.commit(COMMIT_MSG, null, null);

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE3),
                README_FILE3_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE3);
        RevCommit rc2 = gitRepoService.commit(COMMIT_MSG, null, null);

        gitRepoService.checkoutBranch(FN_MASTER, "br1", null);
        assertEquals(2, gitRepoService.getBranches().size());

        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE4),
                README_FILE4_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE4);
        RevCommit rc3 = gitRepoService.commit(COMMIT_MSG, null, null);

        gitRepoService.checkoutRevCommit(rc0, null);
        assertEquals(1, gitRepoService.getAllFiles().size());
        ScmStat stat = gitRepoService.blame(gitRepoService.getAllFiles(), new EmptyProgressMonitor() {} );
        assertEquals(1, stat.getLogMap().size());


        gitRepoService.checkoutRevCommit(rc2, null);
        assertEquals(3, gitRepoService.getAllFiles().size());

        gitRepoService.checkoutRevCommit(rc3, null);
        assertEquals(4, gitRepoService.getAllFiles().size());
        stat = gitRepoService.blame(gitRepoService.getAllFiles(), new EmptyProgressMonitor() {} );
        assertEquals(1, stat.getLogMap().size());



    }

    @Test
    public void checkoutFile() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        Path readmePath = Paths.get(tmpGitProject, README_FILE);

        byte[] readmeBytesOriginal = Files.readAllBytes(readmePath);
        Files.write(readmePath,
                "\n readme changes 0".getBytes(), StandardOpenOption.APPEND);
        byte[] readmeBytesChanged = Files.readAllBytes(readmePath);

        assertNotEquals(readmeBytesOriginal.length, readmeBytesChanged.length);
        gitRepoService.checkoutFile(README_FILE, null);

        byte[] readmeBytesOriginalNew = Files.readAllBytes(readmePath);

        assertTrue(readmeBytesOriginalNew.length > 0);
    }

    @Test
    public void saveFile() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n HAL9000".getBytes(), StandardOpenOption.APPEND);
        gitRepoService.addFileToCommitStage(README_FILE);
        RevCommit rc = gitRepoService.commit("HAL9000", null, null);
        Files.write(Paths.get(tmpGitProject, README_FILE),
                "\n Bender".getBytes(), StandardOpenOption.APPEND);
        gitRepoService.addFileToCommitStage(README_FILE);
        RevCommit rcWithBender = gitRepoService.commit("Bender", null, null);

        String absPath = gitRepoService.saveFile(rc.name(), README_FILE);
        String str = new String(Files.readAllBytes(Paths.get(absPath)));
        assertTrue(str.contains("HAL9000"));
        assertFalse(str.contains("Bender"));

        absPath = gitRepoService.saveFile(rcWithBender.name(), README_FILE);
        str = new String(Files.readAllBytes(Paths.get(absPath)));
        assertTrue(str.contains("HAL9000"));
        assertTrue(str.contains("Bender"));
    }

    @Test
    public void removeFile() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        assertEquals(1, gitRepoService.getAllFiles(FN_MASTER).size());
        gitRepoService.removeFile(README_FILE);
        gitRepoService.commit("Removed md", null, null);
        assertEquals(0, gitRepoService.getAllFiles(FN_MASTER).size());

    }

    @Test
    public void compressDatabase() throws Exception {
        Files.write(Paths.get(tmpGitProjectPath.toString(), README_FILE),
                README_FILE_BODY.getBytes(), StandardOpenOption.CREATE);
        gitRepoService.addFileToCommitStage(README_FILE);
        gitRepoService.commit(COMMIT_MSG, null, null);
        gitRepoService.compressDatabase(null);
        assertTrue(true);

    }

}
