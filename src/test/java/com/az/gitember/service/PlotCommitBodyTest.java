package com.az.gitember.service;

import com.az.gitember.data.Project;
import com.az.gitember.data.ScmPlotCommit;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.ui.CommitGraphRenderer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Commits from {@link ScmPlotWalk} keep a summary instead of their raw body (see
 * {@link ScmPlotCommit}). These tests pin down what still has to work on such a commit: the two
 * fields the history table paints, the full information {@code adapt} produces, and painting the
 * commit graph -- JGit's own {@code AbstractPlotRenderer.paintCommit} reads
 * {@code getShortMessage()}, which no summary can serve because the method is {@code final}.
 */
class PlotCommitBodyTest {

    private Path repoDir;
    private Repository repository;
    private Project project;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-plot-body-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();

        try (Git git = new Git(repository)) {
            for (int i = 1; i <= 3; i++) {
                Files.writeString(repoDir.resolve("file" + i + ".txt"), "content " + i);
                git.add().addFilepattern(".").call();
                git.commit().setMessage("Commit number " + i + "\n\nA body paragraph for " + i).call();
            }
            git.tag().setName("v1").call();
        }
        repository.close();

        project = new Project(repoDir.toString(), new Date());
        project.openRepoService();
    }

    @AfterEach
    void tearDown() throws Exception {
        project.closeRepoService();
        Context.reset();
        Files.walk(repoDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void walkedCommitsDropTheirBodyButKeepWhatTheTableShows() {
        List<PlotCommit<PlotLane>> commits = loadHistory();

        assertEquals(3, commits.size());
        for (PlotCommit<PlotLane> commit : commits) {
            assertNull(commit.getRawBuffer(), "the raw body is what makes a long history expensive");
            assertNotNull(ScmPlotCommit.shortMessageOf(commit));
            assertEquals("Test User", ScmPlotCommit.authorNameOf(commit));
        }
        assertEquals("Commit number 3", ScmPlotCommit.shortMessageOf(commits.get(0)));
    }

    @Test
    void adaptStillReturnsTheFullCommitInformation() {
        PlotCommit<PlotLane> head = loadHistory().get(0);

        ScmRevisionInformation info = project.getGitRepoService().adapt(head, null);

        assertEquals("Commit number 3", info.getShortMessage());
        assertTrue(info.getFullMessage().contains("A body paragraph for 3"),
                "the full message has to be read back from the object database");
        assertEquals("Test User", info.getAuthorName());
        assertEquals("test@example.com", info.getAuthorEmail());
        assertEquals(1, info.getParents().size());
        assertFalse(info.getAffectedItems().isEmpty());
    }

    @Test
    void getFullMessageReadsTheBodyBack() {
        PlotCommit<PlotLane> head = loadHistory().get(0);

        String fullMessage = project.getGitRepoService().getFullMessage(head);

        assertNotNull(fullMessage);
        assertTrue(fullMessage.startsWith("Commit number 3"));
        assertTrue(fullMessage.contains("A body paragraph for 3"));
    }

    @Test
    void searchMatchesOnMessageAuthorAndFileName() {
        List<PlotCommit<PlotLane>> commits = loadHistory();
        GitRepoService service = project.getGitRepoService();

        assertEquals(1, service.search(List.copyOf(commits), "number 2", false).size(), "short message");
        assertEquals(3, service.search(List.copyOf(commits), "test@example.com", false).size(), "author email");
        assertEquals(1, service.search(List.copyOf(commits), "file1.txt", false).size(), "affected file");
        assertEquals(0, service.search(List.copyOf(commits), "nothing-matches-this", false).size());
    }

    /**
     * The regression this class exists for: rendering a body-less commit used to fail with
     * "Cannot read the array length because b is null" inside JGit's plot renderer.
     */
    @Test
    void commitGraphRendersEveryRowIncludingTaggedAndRootCommits() throws Exception {
        Context.initRepoOnly(project);              // the renderer asks Context for the service
        List<PlotCommit<PlotLane>> commits = loadHistory();

        CommitGraphRenderer renderer = new CommitGraphRenderer();
        BufferedImage image = new BufferedImage(600, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            for (PlotCommit<PlotLane> commit : commits) {
                int graphWidth = renderer.render(g2, commit, 24);
                assertTrue(graphWidth > 0,
                        "graph width comes from the last step of paintCommit, so it proves the "
                                + "whole render ran for " + commit.getName());
            }
        } finally {
            g2.dispose();
        }
    }

    /** Newest first, exactly as {@code HistoryPanel} loads it. */
    private List<PlotCommit<PlotLane>> loadHistory() {
        return new ArrayList<>(project.getGitRepoService().getCommitsByTree(null, true, -1, null));
    }
}
