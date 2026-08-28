package com.az.gitember.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RepositoryScanService}: the scan roots are resolved against {@code user.home},
 * so each test points that property at a temp directory laid out like a real home folder.
 */
class RepositoryScanServiceTest {

    private Path home;
    private String originalUserHome;
    private RepositoryScanService scanService;

    @BeforeEach
    void setUp() throws Exception {
        home = Files.createTempDirectory("gitember-scan-home-");
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        scanService = new RepositoryScanService();
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setProperty("user.home", originalUserHome);
        Files.walk(home)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void findsRepositoriesInKnownFoldersAndReportsThemAsFound() throws Exception {
        Path direct = repo(home.resolve("Projects/gitember"));
        Path nested = repo(home.resolve("dev/work/team/api"));
        repo(home.resolve("git/tools"));
        Files.createDirectories(home.resolve("Projects/not-a-repo/src"));
        // Not one of the scanned folders.
        repo(home.resolve("Documents/hidden-away"));

        List<String> reported = new ArrayList<>();
        List<String> found = scanService.scan(reported::add);

        assertEquals(3, found.size(), "expected exactly the three repos under scanned folders: " + found);
        assertTrue(found.contains(direct.toString()));
        assertTrue(found.contains(nested.toString()));
        assertEquals(found, reported, "callback must report every repository, in discovery order");
    }

    @Test
    void doesNotDescendIntoARepositoryOrIntoSkippedFolders() throws Exception {
        Path outer = repo(home.resolve("Projects/outer"));
        repo(outer.resolve("submodule"));                       // inside a repo
        repo(home.resolve("Projects/app/node_modules/dep"));    // dependency folder
        repo(home.resolve("Projects/.cache/clone"));            // hidden folder

        List<String> found = scanService.scan(null);

        assertEquals(List.of(outer.toString()), found);
    }

    @Test
    void findsRepositoryPointedToByAGitFileAsInALinkedWorktree() throws Exception {
        Path worktree = home.resolve("code/feature-branch");
        Files.createDirectories(worktree);
        Files.writeString(worktree.resolve(".git"), "gitdir: /somewhere/.git/worktrees/feature-branch");

        List<String> found = scanService.scan(null);

        assertEquals(List.of(worktree.toString()), found);
    }

    @Test
    void stopsBelowTheDepthLimit() throws Exception {
        // Five levels below the root — one deeper than the scan goes.
        repo(home.resolve("Projects/a/b/c/d/e"));

        assertTrue(scanService.scan(null).isEmpty());
    }

    @Test
    void resolveRootsSkipsMissingFoldersAndDeduplicatesByRealPath() throws Exception {
        Files.createDirectories(home.resolve("Projects"));
        Files.createDirectories(home.resolve("git"));

        List<Path> roots = scanService.resolveRoots();

        assertEquals(2, roots.size(), "only existing folders are scanned: " + roots);
        // On a case-insensitive file system "Projects" and "projects" are the same folder and
        // must be listed once; on a case-sensitive one only "Projects" exists at all.
        assertEquals(1, roots.stream().filter(p -> p.getFileName().toString().equalsIgnoreCase("projects")).count());
    }

    /** Creates {@code dir} with an empty ".git" folder inside, which is all the scan looks for. */
    private Path repo(Path dir) throws Exception {
        Files.createDirectories(dir.resolve(".git"));
        return dir;
    }
}
