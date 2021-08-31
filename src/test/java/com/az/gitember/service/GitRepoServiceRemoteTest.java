package com.az.gitember.service;

import com.az.gitember.data.RemoteRepoParameters;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class GitRepoServiceRemoteTest {

    private static final String MORE_FILE0 = "file0.txt";
    private static final String MORE_FILE1 = "file1.txt";
    private static final String README_FILE = "README.md";
    private static final String IGNORE_FILE = ".gitignore";

    private static final String FN_MASTER = "refs/heads/master";
    private static final String FN_BRANCH = "refs/heads/hal9000";

    private String remoteGitProject = null;
    private GitRepoService remoteRepositoryService;

    private SimpleHttpServer simpleHttpServer;

    private String clonedRepoPath;
    private GitRepoService clonedRepositoryService;

    private String fromRepo;
    private String fromRepoHttps;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        Path remoteGitProjectPath = Files.createTempDirectory("gitember-remote");
        remoteGitProject = remoteGitProjectPath.toAbsolutePath().toString();
        GitRepoService.createRepository(remoteGitProject, true, true);
        remoteRepositoryService = new GitRepoService(
                Paths.get(remoteGitProject, ".git").toString());
        Files.write(Paths.get(remoteGitProjectPath.toString(), README_FILE),
                new byte[]{}, StandardOpenOption.CREATE);
        Files.write(Paths.get(remoteGitProjectPath.toString(), IGNORE_FILE),
                new byte[]{}, StandardOpenOption.CREATE);
        remoteRepositoryService.addFileToCommitStage(README_FILE);
        remoteRepositoryService.addFileToCommitStage(IGNORE_FILE);
        remoteRepositoryService.commit("Added initial test files", null, null);


        simpleHttpServer = new SimpleHttpServer(remoteRepositoryService.getRepository(), true);
        simpleHttpServer.start();

        clonedRepoPath = Files.createTempDirectory("gitember-cloned").toString();
        clonedRepositoryService = new GitRepoService(Paths.get(clonedRepoPath, ".git").toString());
        fromRepo = simpleHttpServer.getUri().toString();
        fromRepoHttps = simpleHttpServer.getSecureUri().toString();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        simpleHttpServer.stop();
        remoteRepositoryService.shutdown();
        clonedRepositoryService.shutdown();
        try {
            FileUtils.deleteDirectory(new File(remoteGitProject));
        } catch (Exception e) {

        }

        try {
            FileUtils.deleteDirectory(new File(clonedRepoPath));
        } catch (Exception e) {

        }


    }

    @Test
    public void testCloneHttp() throws Exception {

        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(fromRepo);
        params.setDestinationFolder(clonedRepoPath);
        params.setUserName("agitter");
        params.setUserPwd("letmein");

        clonedRepositoryService
                .cloneRepository(
                        params,
                         null
                );
        assertTrue(Files.exists(Paths.get(clonedRepoPath, README_FILE)));
        assertTrue(Files.exists(Paths.get(clonedRepoPath, IGNORE_FILE)));
    }

    @Test
    public void testCloneHttps() throws Exception {
        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(fromRepo);
        params.setDestinationFolder(clonedRepoPath);
        params.setUserName("agitter");
        params.setUserPwd("letmein");

        clonedRepositoryService
                .cloneRepository(
                        params, null
                );
        assertTrue(Files.exists(Paths.get(clonedRepoPath, README_FILE)));
        assertTrue(Files.exists(Paths.get(clonedRepoPath, IGNORE_FILE)));
    }


    @Test
    public void testCloneNoCredentialsProvider() {

        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(fromRepo);
        params.setDestinationFolder(clonedRepoPath);
        try {
            clonedRepositoryService
                    .cloneRepository(
                            params, null
                    );
            fail();
        } catch (TransportException e) {
            assertTrue(e.getMessage().contains("not authorized"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCloneWrongCredentials() {

        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(fromRepo);
        params.setDestinationFolder(clonedRepoPath);
        params.setUserName("any");
        params.setUserPwd("wrong");

        try {
            clonedRepositoryService
                    .cloneRepository(
                            params, null
                    );
            fail();
        } catch (TransportException te) {
            assertTrue(te.getMessage().contains("not authorized"), te.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals("Not, expected", e.getMessage());
        }
    }

    @Test
    public void testRemoteRepositoryPush() throws Exception {

        testCloneHttps();

        Files.write(Paths.get(clonedRepoPath, MORE_FILE0),
                "test file 0 to master".getBytes(), StandardOpenOption.CREATE);
        clonedRepositoryService.addFileToCommitStage(Paths.get(MORE_FILE0).toString());
        clonedRepositoryService.commit("File 0 added", null, null);
        //local master to remote master
        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");

        RefSpec refSpec = new RefSpec(FN_MASTER + ":" + FN_MASTER);
        clonedRepositoryService.remoteRepositoryPush(cloneParameters, refSpec, null);
        assertEquals(3, remoteRepositoryService.getAllFiles(FN_MASTER).size());
    }



    @Test
    public void testRemoteRepositoryCreateBranch() throws Exception {

        testCloneHttps();


        Ref newBranch = clonedRepositoryService.createBranch(FN_MASTER, "hal9000");
        clonedRepositoryService.checkoutBranch(FN_BRANCH, null);
        Files.write(Paths.get(clonedRepoPath, MORE_FILE0),
                "test file 0 to hal9000 branch".getBytes(), StandardOpenOption.CREATE);
        clonedRepositoryService.addFileToCommitStage(Paths.get(MORE_FILE0).toString());
        clonedRepositoryService.commit("File 0 added", null, null);


        //local master to remote master
        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");

        RefSpec refSpec = new RefSpec(FN_BRANCH + ":" + FN_BRANCH);
        clonedRepositoryService.remoteRepositoryPush(cloneParameters, refSpec, null);

        assertEquals(2, remoteRepositoryService.getAllFiles(FN_MASTER).size());

        assertEquals(3, remoteRepositoryService.getAllFiles(FN_BRANCH).size());
    }


    @Test
    public void testRemoteRepositoryDeleteBranch() throws Exception {
        testRemoteRepositoryCreateBranch();
        RefSpec refSpec = new RefSpec().setSource(null).setDestination(FN_BRANCH);

        assertEquals(2, remoteRepositoryService.getBranches().size());

        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");
        clonedRepositoryService.remoteRepositoryPush(cloneParameters, refSpec, null);

        assertEquals(2, remoteRepositoryService.getAllFiles(FN_MASTER).size());

        assertEquals(1, remoteRepositoryService.getBranches().size());

    }






    @Test
    public void remoteRepositoryPull() throws Exception {
        testCloneHttps();
        externalUpdateOfRemoteRepo();
        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");

        String str = clonedRepositoryService.remoteRepositoryPull(
                cloneParameters,
                FN_MASTER,
                null
        );
    }




    @Test
    public void testRemoteRepositoryFetch() throws Exception {

        testCloneHttps();
        externalUpdateOfRemoteRepo();
        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");
        String str  = clonedRepositoryService.remoteRepositoryFetch(
                cloneParameters,
                null,
                null
        );
        assertTrue(str.contains("refs/heads/rbr1"));
        assertTrue(str.contains("refs/heads/master"));
    }


    @Test
    public void testRemoteCheckout() throws Exception  {

        testCloneHttps();

        externalUpdateOfRemoteRepo();

        RemoteRepoParameters cloneParameters = new RemoteRepoParameters();
        cloneParameters.setUserName("agitter");
        cloneParameters.setUserPwd("letmein");

        clonedRepositoryService.remoteRepositoryFetch(cloneParameters, null, null);

        try {
            clonedRepositoryService.checkoutBranch("refs/remotes/origin/rbr1", "trackremotebr1", null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(4, clonedRepositoryService.getAllFiles("refs/heads/trackremotebr1").size());

    }


    private void externalUpdateOfRemoteRepo() throws IOException, GitAPIException {
        Files.write(Paths.get(remoteGitProject, MORE_FILE0),
                "Added to remote master".getBytes(), StandardOpenOption.CREATE);
        remoteRepositoryService.addFileToCommitStage(Paths.get(MORE_FILE0).toString());
        remoteRepositoryService.commit("File " + MORE_FILE0 + " added to remote repo", null, null);

        remoteRepositoryService.checkoutBranch(FN_MASTER, "rbr1", null);

        Files.write(Paths.get(remoteGitProject, MORE_FILE1),
                "Added to remote master".getBytes(), StandardOpenOption.CREATE);
        remoteRepositoryService.addFileToCommitStage(Paths.get(MORE_FILE1).toString());
        remoteRepositoryService.commit("File " + MORE_FILE1 + " added to remote repo", null, null);
        remoteRepositoryService.checkoutBranch(FN_MASTER, null);
    }


}
