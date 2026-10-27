package com.az.gitember.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Table-driven coverage of the path/identity contract in {@link Project}: normalization,
 * the git-dir vs. work-tree distinction, and the canonical identity key.
 *
 * <p>{@link Project#getProjectFolder()} must keep its trailing separator, byte-compatible
 * with the legacy {@code Context.getProjectFolder()} expression
 * ({@code repositoryPath.replace(".git", "")}) — {@code SearchService} md5-hashes this
 * string to name the Lucene history index folder, so any drift here silently orphans a
 * user's existing index.
 */
class ProjectPathNormalizationTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "C:\\dev\\p",
            "C:\\dev\\p\\",
            "C:\\dev\\p\\.git",
            "C:\\dev\\p/.git",
    })
    void normalizeHome_variousSpellings_collapseToSameWorkTree(String input) {
        assertEquals("C:\\dev\\p", Project.normalizeHome(input));
    }

    @Test
    void normalizeHome_unixStyleWithTrailingSlash() {
        assertEquals("/home/u/p", Project.normalizeHome("/home/u/p/.git/"));
    }

    @Test
    void normalizeHome_bareRepoNamedDotGit_isNotStripped() {
        // A directory literally named "foo.git" (bare-clone convention) is not a work tree
        // with a ".git" subfolder -- it must be left alone.
        assertEquals("C:\\repos\\foo.git", Project.normalizeHome("C:\\repos\\foo.git"));
    }

    @Test
    void normalizeHome_null_returnsNull() {
        assertNull(Project.normalizeHome(null));
    }

    @Test
    void getGitDir_alwaysEndsWithDotGit_andIsIdempotent() {
        Project p = new Project("C:\\dev\\p", new Date());
        assertEquals("C:\\dev\\p" + java.io.File.separator + ".git", p.getGitDir());

        // Re-normalizing the already-normalized folder is a no-op.
        Project again = new Project(Project.normalizeHome(p.getProjectHomeFolder()), new Date());
        assertEquals(p.getGitDir(), again.getGitDir());
    }

    @Test
    void getGitDir_bareRepo_homeFolderAlreadyEndsWithDotGit() {
        Project p = new Project("C:\\repos\\foo.git", new Date());
        assertEquals("C:\\repos\\foo.git", p.getGitDir());
    }

    @Test
    void getProjectFolder_keepsTrailingSeparator_matchingLegacyExpression() {
        Project p = new Project("C:\\dev\\p", new Date());
        String gitDir = p.getGitDir();
        // The legacy Context.getProjectFolder() expression: repositoryPath.replace(".git", "")
        String legacy = gitDir.replace(".git", "");
        assertEquals(legacy, p.getProjectFolder());
        assertTrue(p.getProjectFolder().endsWith(java.io.File.separator));
    }

    @Test
    void getProjectFolder_pathWithInteriorDotGitSubstring_doesNotMangle() {
        // Regression: the legacy naive String.replace(".git", "") corrupts paths that contain
        // ".git" as an interior substring (e.g. a folder literally named "foo.github").
        Project p = new Project("C:\\dev\\foo.github\\p", new Date());
        String legacyBuggyResult = p.getGitDir().replace(".git", "");
        assertNotEquals(legacyBuggyResult, p.getProjectFolder(),
                "getProjectFolder() should not reproduce the legacy interior-substring bug");
        assertTrue(p.getProjectFolder().startsWith("C:\\dev\\foo.github\\p"));
    }

    @Test
    void canonicalKey_isCaseInsensitive_underRootLocale() {
        assertEquals(Project.canonicalKey("C:\\Dev\\P"), Project.canonicalKey("c:\\dev\\p"));
    }

    @Test
    void canonicalKey_null_isEmptyNotNull() {
        assertEquals("", Project.canonicalKey(null));
    }

    @Test
    void identityMethods_areMutuallyConsistent_forCaseAndSeparatorVariants() {
        Project a = new Project("C:\\dev\\p", new Date());
        Project b = new Project("c:\\DEV\\p\\", new Date());
        Project c = new Project("C:\\dev\\p\\.git", new Date());

        assertEquals(a, b);
        assertEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        assertEquals(0, a.compareTo(b));
        assertEquals(0, a.compareTo(c));
    }

    @Test
    void identityMethods_areNullSafe() {
        Project noFolder = new Project();
        assertDoesNotThrow(() -> noFolder.hashCode());
        assertDoesNotThrow(() -> noFolder.equals(new Project()));
        assertDoesNotThrow(() -> noFolder.compareTo(new Project()));
        assertDoesNotThrow(() -> noFolder.compareTo(null));
    }
}
