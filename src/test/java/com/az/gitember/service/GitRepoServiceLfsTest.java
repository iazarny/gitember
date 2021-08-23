package com.az.gitember.service;

import com.az.gitember.data.RemoteRepoParameters;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lfs.CleanFilter;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.SmudgeFilter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.util.SystemReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitRepoServiceLfsTest {

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

    private static final String README_FILE_BODY = "readme ";
    private static final String README_FILE2_BODY = "readme too";
    private static final String README_FILE3_BODY = "HAL9000";
    private static final String README_FILE4_BODY = "Bender";
    private static final String COMMIT_MSG = "Some commit msg";


    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {

        FilterCommandRegistry.register(GitRepoService.SMUDGE_NAME, SmudgeFilter.FACTORY);
        FilterCommandRegistry.register(GitRepoService.CLEAN_NAME, CleanFilter.FACTORY);


    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        FilterCommandRegistry.unregister(GitRepoService.SMUDGE_NAME);
        FilterCommandRegistry.unregister(GitRepoService.CLEAN_NAME);

    }

    @Test
    public void createRepositorywithLfs() throws Exception {

        String newRepo = Files.createTempDirectory("tmp-gtmbr-lfs").toString();
        Repository repository = GitRepoService.createRepository(newRepo, true, true, true);
        GitRepoService gitRepoService = new GitRepoService(
                repository
        );

        assertEquals(3, gitRepoService.getAllFiles().size());

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            stringBuilder.append("\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n");
        }
        Files.write(Paths.get(newRepo, "loremipsum.bmp"),
                stringBuilder.toString().getBytes(), StandardOpenOption.CREATE);

        DirCache dc = gitRepoService.addFileToCommitStage("loremipsum.bmp");
        assertEquals(4, dc.getEntryCount());


        gitRepoService.commit("Add loremipsum.bmp large file", null, null);


        String clonedRepoPath = Files.createTempDirectory("gitember-cloned").toString();

        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setUrl(newRepo);
        params.setDestinationFolder(clonedRepoPath);

        GitRepoService clonedRepositoryService = new GitRepoService(Paths.get(clonedRepoPath, ".git").toString());
        clonedRepositoryService.cloneRepository(
                params, new TextProgressMonitor(new PrintWriter(System.out))
        );

        final Path path = Path.of(clonedRepoPath, "loremipsum.bmp");
        byte[] fileBody =  Files.readAllBytes(path);
        assertTrue( fileBody.length <= LfsPointer.SIZE_THRESHOLD, "Must be file pointer instead of large file" );


        try {
            gitRepoService.shutdown();
            clonedRepositoryService.shutdown();

            //FileUtils.deleteDirectory(new File(newRepo));

           // FileUtils.deleteDirectory(new File(clonedRepoPath));

        } catch (Exception e) {
           // e.printStackTrace();

        }

    }


}
