package com.az.gitember.service;

import com.az.gitember.data.ScmRevisionInformation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class GitRepoServiceNotesTest {

    private Path repoDir;
    private Repository repository;
    private GitRepoService service;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-notes-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        service = new GitRepoService(repository);
        repository.getConfig().setString("user", null, "name", "Test User");
        repository.getConfig().setString("user", null, "email", "test@example.com");
        repository.getConfig().save();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
        deleteDirectory(repoDir);
    }

    @Test
    void getCommitNote_whenNoNotesRef_returnsNull() throws Exception {
        RevCommit commit = makeInitialCommit();
        assertNull(service.getCommitNote(commit.getName()));
        assertNull(repository.exactRef(Constants.R_NOTES_COMMITS));
    }

    @Test
    void addCommitNote_writesNoteWithoutChangingCommitSha() throws Exception {
        RevCommit commit = makeInitialCommit();
        String sha = commit.getName();

        service.addCommitNote(sha, "Reviewed by Alice");

        String note = service.getCommitNote(sha);
        assertNotNull(note);
        assertTrue(note.contains("Reviewed by Alice"));
        assertEquals(sha, repository.resolve(Constants.HEAD).getName());
        assertNotNull(repository.exactRef(Constants.R_NOTES_COMMITS));
    }

    @Test
    void addCommitNote_replacesExistingNote() throws Exception {
        RevCommit commit = makeInitialCommit();
        String sha = commit.getName();
        service.addCommitNote(sha, "first");
        service.addCommitNote(sha, "second");

        String note = service.getCommitNote(sha);
        assertNotNull(note);
        assertTrue(note.contains("second"));
        assertFalse(note.contains("first"));
    }

    @Test
    void removeCommitNote_clearsNote() throws Exception {
        RevCommit commit = makeInitialCommit();
        String sha = commit.getName();
        service.addCommitNote(sha, "temporary");
        assertNotNull(service.getCommitNote(sha));

        service.removeCommitNote(sha);

        String after = service.getCommitNote(sha);
        assertTrue(after == null || after.isBlank());
    }

    @Test
    void adapt_includesNoteWhenPresent() throws Exception {
        RevCommit commit = makeInitialCommit();
        service.addCommitNote(commit.getName(), "from adapt");

        ScmRevisionInformation info = service.adapt(commit);
        assertTrue(info.hasNote());
        assertTrue(info.getNote().contains("from adapt"));
    }

    @Test
    void addCommitNote_onSecondCommit_doesNotAttachToFirst() throws Exception {
        RevCommit first = makeInitialCommit();
        writeFile("n.txt", "second\n");
        service.addFileToCommitStage("n.txt");
        RevCommit second = service.commit("second", "Test User", "test@example.com");

        service.addCommitNote(second.getName(), "only on second");

        assertNull(service.getCommitNote(first.getName()));
        assertTrue(service.getCommitNote(second.getName()).contains("only on second"));
    }

    @Test
    void getCommitNote_unknownSha_returnsNull() {
        assertNull(service.getCommitNote("0123456789abcdef0123456789abcdef01234567"));
        assertNull(service.getCommitNote(null));
        assertNull(service.getCommitNote(""));
    }

    @Test
    void addCommitNote_unknownSha_throws() {
        assertThrows(IOException.class, () ->
                service.addCommitNote("0123456789abcdef0123456789abcdef01234567", "nope"));
    }

    private RevCommit makeInitialCommit() throws Exception {
        writeFile("init.txt", "init\n");
        service.addFileToCommitStage("init.txt");
        return service.commit("initial commit", "Test User", "test@example.com");
    }

    private void writeFile(String name, String content) throws IOException {
        Files.writeString(repoDir.resolve(name), content);
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
