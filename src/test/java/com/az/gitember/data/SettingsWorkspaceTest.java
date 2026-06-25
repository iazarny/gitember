package com.az.gitember.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the workspace concept on {@link Settings}: backward-compatible project access,
 * migration of legacy flat {@code projects} JSON, and round-trip serialization.
 */
class SettingsWorkspaceTest {

    private static ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    @Test
    void getProjectsCreatesDefaultWorkspace() {
        Settings settings = new Settings();
        settings.getProjects().add(new Project("/home/repo", new Date()));

        assertEquals(1, settings.getWorkspaces().size());
        Workspace active = settings.getActiveWorkspace();
        assertEquals(Workspace.DEFAULT_NAME, active.getName());
        assertEquals(1, active.getProjects().size());
        assertEquals(Workspace.DEFAULT_NAME, settings.getCurrentWorkspace());
    }

    @Test
    void legacyProjectsAreMigratedIntoDefaultWorkspace() throws Exception {
        String legacyJson = """
                {
                  "projects": [ { "projectHomeFolder": "/home/legacy", "openTime": "2024-01-01 10:00:00" } ],
                  "theme": "dark"
                }
                """;

        Settings settings = mapper().readValue(legacyJson, Settings.class);

        assertEquals("dark", settings.getTheme());
        assertEquals(1, settings.getWorkspaces().size());
        assertEquals(Workspace.DEFAULT_NAME, settings.getWorkspaces().get(0).getName());
        assertTrue(settings.getProjects().stream()
                .anyMatch(p -> "/home/legacy".equals(p.getProjectHomeFolder())));
    }

    @Test
    void roundTripPreservesWorkspacesAndDropsLegacyField() throws Exception {
        Settings settings = new Settings();
        settings.getWorkspaces().add(new Workspace("Work"));
        settings.getWorkspaces().add(new Workspace("Personal"));
        settings.setCurrentWorkspace("Work");
        settings.getActiveWorkspace().getProjects().add(new Project("/home/work-repo", new Date()));

        ObjectMapper m = mapper();
        String json = m.writeValueAsString(settings);

        // New format is written. Projects live only nested under a workspace, never as a
        // top-level field, so the only "projects" key is the one inside each workspace.
        assertTrue(json.contains("\"workspaces\""));
        assertTrue(json.contains("\"currentWorkspace\""));
        long projectsKeyCount = json.split("\"projects\"", -1).length - 1;
        assertEquals(2, projectsKeyCount, "expected one 'projects' key per workspace, none at top level");

        Settings reloaded = m.readValue(json, Settings.class);
        assertEquals(2, reloaded.getWorkspaces().size());
        assertEquals("Work", reloaded.getCurrentWorkspace());
        assertTrue(reloaded.getProjects().stream()
                .anyMatch(p -> "/home/work-repo".equals(p.getProjectHomeFolder())));
    }
}