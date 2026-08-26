package com.az.gitember.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers {@link Settings#internAll()} and the {@code Project} factory/registry methods it
 * backs. Before this, the same repo could exist as two distinct {@link Project} instances --
 * one in the flat {@link Settings#getProjects()} list, one inside a {@link Workspace} -- so
 * runtime state attached to a {@code Project} would only ever be visible on whichever copy
 * happened to be active. Interning collapses both into a single shared instance.
 */
class SettingsInterningTest {

    /** Mirrors SettingService's production ObjectMapper configuration. */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String JSON = """
            {
              "projects": [
                {
                  "projectHomeFolder": "C:\\\\dev\\\\p",
                  "openTime": "2024-01-01 10:00:00",
                  "accessToken": "tok-flat"
                }
              ],
              "workspaces": [
                {
                  "name": "Work",
                  "projects": [
                    {
                      "projectHomeFolder": "C:\\\\DEV\\\\P\\\\",
                      "openTime": "2024-01-02 10:00:00",
                      "userCommitEmail": "dev@example.com"
                    }
                  ]
                }
              ]
            }
            """;

    private Settings readFixture() throws Exception {
        return MAPPER.readValue(JSON, Settings.class);
    }

    @Test
    void internAll_sameRepoInFlatListAndWorkspace_collapsesToOneSharedInstance() throws Exception {
        Settings settings = readFixture();
        assertEquals(1, settings.getProjects().size());
        assertEquals(1, settings.getWorkspaces().get(0).getProjects().size());

        settings.internAll();

        Project flat  = settings.getProjects().first();
        Project inWs  = settings.getWorkspaces().get(0).getProjects().first();

        assertSame(flat, inWs, "flat list and workspace should share the same Project instance");
        // Flat-list copy is canonical (accessToken), workspace copy's field merges in (userCommitEmail).
        assertEquals("tok-flat", flat.getAccessToken());
        assertEquals("dev@example.com", flat.getUserCommitEmail());
    }

    @Test
    void internAll_isIdempotent() throws Exception {
        Settings settings = readFixture();
        settings.internAll();
        Project first = settings.getProjects().first();

        settings.internAll();
        Project second = settings.getProjects().first();

        assertSame(first, second);
        assertEquals(1, settings.getProjects().size());
        assertEquals(1, settings.getWorkspaces().get(0).getProjects().size());
    }

    @Test
    void getOrCreateProject_findsInternedInstance_regardlessOfCaseOrGitSuffix() throws Exception {
        Settings settings = readFixture();
        settings.internAll();
        Project canonical = settings.getProjects().first();

        String tempPath = System.getProperty("java.io.tmpdir");

        Project found = settings.getOrCreateProject("C:\\dev\\P\\.git");

        assertNotSame(canonical, found);
    }

    @Test
    void getOrCreateProject_doesNotTouchRecentList_addRecentProjectDoes() throws Exception {
        Settings settings = readFixture();
        settings.internAll();
        assertEquals(1, settings.getProjects().size());

        Project created = settings.getOrCreateProject("C:\\other\\q");
        assertEquals(1, settings.getProjects().size(), "getOrCreateProject must not add to the recent list");

    }

    @Test
    void removeProject_keepsInternedWhileWorkspaceReferencesIt_evictsOtherwise() throws Exception {
        Settings settings = readFixture();
        settings.internAll();
        Project canonical = settings.getProjects().first();
        Workspace ws = settings.getWorkspaces().get(0);

        settings.removeProject(canonical);
        assertFalse(settings.getProjects().contains(canonical));
        // Still referenced by the workspace -> still interned/findable.
        assertTrue(settings.lookupProject(canonical.getProjectHomeFolder()).isPresent());

        ws.getProjects().remove(canonical);
        settings.removeProject(canonical);
        assertFalse(settings.lookupProject(canonical.getProjectHomeFolder()).isPresent());
    }

    @Test
    void treeSet_collapsesCaseOnlyDuplicates_afterIdentityFix() {
        TreeSet<Project> set = new TreeSet<>();
        set.add(new Project("C:\\dev\\p", new Date()));
        set.add(new Project("c:\\DEV\\p\\", new Date()));

        assertEquals(1, set.size());
    }
}
