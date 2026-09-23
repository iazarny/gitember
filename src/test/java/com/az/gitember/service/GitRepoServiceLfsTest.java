package com.az.gitember.service;

import com.az.gitember.data.LfsException;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmItem;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * On-disk tests for LFS detection, clone, pointer diff, transfer errors, and lock errors.
 */
class GitRepoServiceLfsTest {

    private Path repoDir;
    private Repository repository;
    private GitRepoService service;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-lfs-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
        service = new GitRepoService(repository);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
        deleteDirectory(repoDir);
    }

    @Test
    void detectLfsRepository_plainRepoIsNotLfs() {
        assertFalse(service.isLfsRepo());
    }

    @Test
    void detectLfsRepository_enableLfsCreatesLfsDir() throws Exception {
        service.enableLfsOnExistingRepo();
        assertTrue(service.isLfsRepo());
        assertTrue(Files.exists(repoDir.resolve(".git").resolve("lfs").resolve("tmp")));
    }

    @Test
    void detectLfsRepository_trackPatternMarksRepo() throws Exception {
        service.lfsTrack("*.psd");
        assertTrue(service.isLfsRepo());
        assertTrue(service.getLfsTrackedPatterns().contains("*.psd"));

        service.lfsUntrack("*.psd");
        assertFalse(service.getLfsTrackedPatterns().contains("*.psd"));
    }

    @Test
    void cloneLfsRepository_clonedRepoIsDetectedAsLfs() throws Exception {
        service.enableLfsOnExistingRepo();
        service.lfsTrack("*.bin");
        writePointer("asset.bin", "a".repeat(64), 42);
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("lfs pointer")
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }

        Path cloneDir = Files.createTempDirectory("gitember-lfs-clone-");
        GitRepoService cloned = null;
        try {
            GitRepoService cloner = new GitRepoService();
            RemoteRepoParameters params = new RemoteRepoParameters();
            params.setUrl(repoDir.toUri().toString());
            params.setDestinationFolder(cloneDir.toString());
            cloner.cloneRepository(params, null);

            cloned = new GitRepoService(cloneDir.resolve(".git").toString());
            assertTrue(cloned.isLfsRepo(), "Cloned LFS repo must be detected");
            assertTrue(cloned.getLfsTrackedPatterns().contains("*.bin"));
        } finally {
            if (cloned != null) {
                cloned.shutdown();
            }
            deleteDirectory(cloneDir);
        }
    }

    @Test
    void downloadLfsFiles_withoutHttpRemote_throwsLfsException() throws Exception {
        service.enableLfsOnExistingRepo();
        service.lfsTrack("*.bin");
        writePointer("asset.bin", "b".repeat(64), 99);
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("pointer")
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }

        RemoteRepoParameters params = new RemoteRepoParameters();
        LfsException ex = assertThrows(LfsException.class, () -> service.fetchLfsObjects(params));
        assertEquals(LfsException.Kind.NO_REMOTE, ex.getKind());
    }

    @Test
    void uploadLfsFiles_withoutHttpRemote_throwsLfsException() throws Exception {
        service.enableLfsOnExistingRepo();
        String oid = "c".repeat(64);
        writePointer("asset.bin", oid, 10);
        LfsPointer pointer = GitRepoService.parseLfsPointerText(pointerText(oid, 10));
        Path media = new org.eclipse.jgit.lfs.Lfs(repository).getMediaFile(pointer.getOid());
        Files.createDirectories(media.getParent());
        Files.write(media, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        try (Git git = new Git(repository)) {
            git.add().addFilepattern("asset.bin").call();
            git.commit().setMessage("pointer")
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }

        RemoteRepoParameters params = new RemoteRepoParameters();
        LfsException ex = assertThrows(LfsException.class, () -> service.uploadLfsObjects(params));
        assertEquals(LfsException.Kind.NO_REMOTE, ex.getKind());
    }

    @Test
    void lockAndUnlock_withoutHttpRemote_throwLfsException() {
        RemoteRepoParameters params = new RemoteRepoParameters();
        LfsException lockEx = assertThrows(LfsException.class,
                () -> service.lockLfsFile("asset.bin", params));
        assertEquals(LfsException.Kind.NO_REMOTE, lockEx.getKind());

        LfsException unlockEx = assertThrows(LfsException.class,
                () -> service.unlockLfsFile("asset.bin", false, params));
        assertEquals(LfsException.Kind.NO_REMOTE, unlockEx.getKind());
    }

    @Test
    void lfsDiffHandling_comparesHeadAndWorkingPointers() throws Exception {
        service.lfsTrack("*.bin");
        writePointer("asset.bin", "d".repeat(64), 100);
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("old pointer")
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }

        writePointer("asset.bin", "e".repeat(64), 200);
        String diff = service.getLfsDiff("asset.bin");
        assertTrue(diff.startsWith("LFS: asset.bin"), diff);
        assertTrue(diff.contains("d".repeat(64)), diff);
        assertTrue(diff.contains("e".repeat(64)), diff);
        assertTrue(diff.contains("100"), diff);
        assertTrue(diff.contains("200"), diff);
    }

    @Test
    void lfsDiffHandling_formatWhenNotPointer() {
        String text = GitRepoService.formatLfsDiff("readme.txt", null, null, false, -1);
        assertTrue(text.contains("Not an LFS pointer"));
    }

    @Test
    void parseLfsPointerText_readsOidAndSize() throws Exception {
        String oid = "f".repeat(64);
        LfsPointer pointer = GitRepoService.parseLfsPointerText(pointerText(oid, 1234));
        assertNotNull(pointer);
        assertEquals(oid, pointer.getOid().getName());
        assertEquals(1234, pointer.getSize());
        assertNull(GitRepoService.parseLfsPointerText("just a regular file\n"));
    }

    @Test
    void getLfsFiles_listsCommittedPointer() throws Exception {
        service.lfsTrack("*.bin");
        writePointer("asset.bin", "1".repeat(64), 7);
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("lfs file")
                    .setAuthor("Test User", "test@example.com")
                    .setCommitter("Test User", "test@example.com")
                    .call();
        }

        List<ScmItem> files = service.getLfsFiles("HEAD");
        assertEquals(1, files.size());
        assertEquals("asset.bin", files.get(0).getShortName());
        assertEquals(ScmItem.Status.LFS, files.get(0).getAttribute().getStatus());
    }

    @Test
    void lfsErrorHandling_kindsAreDistinct() {
        LfsException auth = new LfsException(LfsException.Kind.AUTH, "401");
        LfsException locked = new LfsException(LfsException.Kind.LOCKED, "already locked");
        assertEquals(LfsException.Kind.AUTH, auth.getKind());
        assertEquals(LfsException.Kind.LOCKED, locked.getKind());
        assertNotEquals(auth.getKind(), locked.getKind());
    }

    private void writePointer(String name, String oid, long size) throws IOException {
        Files.writeString(repoDir.resolve(name), pointerText(oid, size), StandardCharsets.UTF_8);
    }

    private static String pointerText(String oid, long size) {
        return "version https://git-lfs.github.com/spec/v1\n"
                + "oid sha256:" + oid + "\n"
                + "size " + size + "\n";
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (dir != null && Files.exists(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
