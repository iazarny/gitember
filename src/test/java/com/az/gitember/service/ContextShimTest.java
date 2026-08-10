package com.az.gitember.service;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.data.Workspace;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the {@link Context} static shims that now delegate to the active {@link Project}: with
 * nothing active they must be null-safe (not NPE), opening a project makes it both "active" and
 * "current", a project reachable only through a {@link Workspace} is now findable (the headline
 * fix over the historic path-based, flat-list-only lookup), and {@link Context#fire} publishes
 * with the given {@link Project} as the event source.
 */
class ContextShimTest {

    private Path repoDir;
    private Project project;

    @BeforeEach
    void setUp() {
        Context.reset();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (project != null) {
            project.closeRepoService();
        }
        if (repoDir != null) {
            deleteDirectory(repoDir);
        }
        Context.reset();
    }

    @Test
    void noActiveProject_everyShimIsNullSafe() {
        assertNull(Context.getRepositoryPath());
        assertEquals("", Context.getProjectFolder());
        assertNotNull(Context.getGitRepoService());
        assertNull(Context.getGitRepoService().getRepository());
        assertTrue(Context.getLocalBranches().isEmpty());
        assertTrue(Context.getRemoteBranches().isEmpty());
        assertTrue(Context.getTags().isEmpty());
        assertTrue(Context.getStash().isEmpty());
        assertTrue(Context.getStatusList().isEmpty());
        assertTrue(Context.getPlotCommitList().isEmpty());
        assertTrue(Context.getPullRequests().isEmpty());
        assertTrue(Context.getSubmodules().isEmpty());
        assertFalse(Context.isLfsRepo());
        assertTrue(Context.getCurrentProject().isEmpty());

        // update*() must be no-ops, not NPEs, when nothing is active.
        assertDoesNotThrow(Context::updateAll);
        assertDoesNotThrow(() -> Context.updateStatus(null));
        assertDoesNotThrow(Context::updateBranches);
        assertDoesNotThrow(Context::updateTags);
        assertDoesNotThrow(Context::updateStash);
        assertDoesNotThrow(Context::updateWorkingBranch);
        assertDoesNotThrow(Context::updateSubmodules);
        assertDoesNotThrow(Context::updatePullRequests);
    }

    @Test
    void init_makesTheOpenedProjectBothActiveAndCurrent() throws Exception {
        repoDir = createTempRepo();
        Settings settings = new Settings();
        Context.setSettings(settings);
        project = settings.getOrCreateProject(repoDir.toString());

        Context.init(project);

        assertSame(project, Context.getActiveProject());
        assertSame(project, Context.getCurrentProject().orElse(null));
        assertNotNull(Context.getRepositoryPath());
        assertTrue(Context.getRepositoryPath().endsWith(".git"));
    }

    @Test
    void currentProject_findsAProjectThatOnlyLivesInAWorkspace() throws Exception {
        repoDir = createTempRepo();
        Settings settings = new Settings();
        Workspace ws = new Workspace("Work");
        ws.getProjects().add(new Project(repoDir.toString(), new Date()));
        settings.getWorkspaces().add(ws);
        settings.internAll();
        Context.setSettings(settings);

        project = ws.getProjects().first();
        // Sanity: NOT in the flat recent list -- this is exactly the case the legacy
        // path-based Context.getCurrentProject() (settings.getProjects() only) could never find.
        assertFalse(settings.getProjects().contains(project));

        Context.initRepoOnly(project);

        assertTrue(Context.getCurrentProject().isPresent());
        assertSame(project, Context.getCurrentProject().get());
    }

    @Test
    void fire_publishesOnTheBusWithTheGivenSourceAndListenersReceiveIt() throws Exception {
        repoDir = createTempRepo();
        project = new Project(repoDir.toString(), new Date());

        final Object[] receivedSource = new Object[1];
        Context.addPropertyChangeListener(Context.PROP_LOCAL_BRANCHES, evt -> receivedSource[0] = evt.getSource());

        Context.fire(project, Context.PROP_LOCAL_BRANCHES, List.of(), List.of("x"));

        assertSame(project, receivedSource[0]);
    }

    private static Path createTempRepo() throws Exception {
        Path dir = Files.createTempDirectory("gitember-context-test-");
        try (Git git = Git.init().setDirectory(dir.toFile()).call()) {
            git.getRepository().getConfig().setString("user", null, "name", "Test User");
            git.getRepository().getConfig().setString("user", null, "email", "test@example.com");
            git.getRepository().getConfig().save();
            Files.writeString(dir.resolve("README.md"), "hello");
            git.add().addFilepattern(".").call();
            git.commit().setMessage("initial").call();
        }
        return dir;
    }

    private static void deleteDirectory(Path dir) throws Exception {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
