package com.az.gitember.service;

import com.az.gitember.data.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link WorkspaceSearchService}: indexing a project's working copy,
 * searching its content, and incremental reindexing (changed + deleted files).
 *
 * <p>Uses a real on-disk git repository in a temp directory, matching the project's test style.
 */
class WorkspaceSearchServiceTest {

    private Path repoDir;
    private Repository repository;
    private GitRepoService gitService;
    private Project project;
    private WorkspaceSearchService searchService;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-ws-search-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        gitService = new GitRepoService(repository);
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();

        project = new Project(repoDir.toFile().getAbsolutePath(), new Date());
        searchService = new WorkspaceSearchService();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Drop the (temp-dir keyed) index so runs stay isolated.
        try (SearchService s = new SearchService(project.getProjectHomeFolder(),
                SearchService.WORKSPACE_INDEX_PREFIX)) {
            s.dropIndex();
        }
        repository.close();
        deleteDirectory(repoDir);
    }

    @Test
    void indexAndSearch_findsOnlyMatchingFiles() throws Exception {
        writeAndCommit("readme.txt", "the quick brown fox");
        writeAndCommit("notes.txt", "lorem ipsum dolor");

        searchService.indexProject(project, null);

        assertTrue(searchService.isIndexed(project), "Project should be indexed after indexProject");
        assertTrue(searchService.allIndexed(List.of(project)));

        Map<Project, Set<String>> res = searchService.search(List.of(project), "brown");
        assertTrue(res.containsKey(project), "The project with a match should be present");
        assertTrue(res.get(project).contains("readme.txt"));
        assertFalse(res.get(project).contains("notes.txt"));
    }

    @Test
    void reindex_picksUpChangesAndDeletions() throws Exception {
        writeAndCommit("a.txt", "alpha content");
        searchService.indexProject(project, null);
        assertTrue(searchService.search(List.of(project), "alpha").containsKey(project));

        // Modify content, then reindex: old term gone, new term found.
        Thread.sleep(1100); // allow the file's last-modified stamp to advance
        Files.writeString(repoDir.resolve("a.txt"), "beta content");
        searchService.indexProject(project, null);
        assertTrue(searchService.search(List.of(project), "beta").containsKey(project));
        assertFalse(searchService.search(List.of(project), "alpha").containsKey(project));

        // Delete the file from disk, then reindex: it must be pruned from the index.
        Files.delete(repoDir.resolve("a.txt"));
        searchService.indexProject(project, null);
        assertFalse(searchService.search(List.of(project), "beta").containsKey(project));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void writeAndCommit(String name, String content) throws Exception {
        Files.writeString(repoDir.resolve(name), content);
        gitService.addFileToCommitStage(name);
        gitService.commit("add " + name, "Test User", "test@example.com");
    }

    private static void deleteDirectory(Path dir) throws java.io.IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
