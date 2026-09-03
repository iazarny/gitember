package com.az.gitember.service;

import com.az.gitember.data.Settings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommitTemplateServiceTest {

    private Path repoDir;
    private Repository repository;
    private GitRepoService service;

    @BeforeEach
    void setUp() throws Exception {
        repoDir = Files.createTempDirectory("gitember-template-");
        repository = Git.init().setDirectory(repoDir.toFile()).call().getRepository();
        service = new GitRepoService(repository);
    }

    @AfterEach
    void tearDown() throws Exception {
        repository.close();
        if (Files.exists(repoDir)) {
            Files.walk(repoDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void resolve_prefersSettingsOverGitTemplateFile() throws Exception {
        Path gitFile = repoDir.resolve("git-template.txt");
        Files.writeString(gitFile, "from git file\n");
        StoredConfig cfg = repository.getConfig();
        cfg.setString("commit", null, "template", gitFile.toString());
        cfg.save();

        Settings settings = new Settings();
        settings.setCommitTemplate("from gitember settings");

        assertEquals("from gitember settings",
                CommitTemplateService.resolve(settings, service));
    }

    @Test
    void resolve_usesGitCommitTemplateWhenSettingsBlank() throws Exception {
        Path gitFile = repoDir.resolve("git-template.txt");
        Files.writeString(gitFile, "feat: \n\n# why\n");
        StoredConfig cfg = repository.getConfig();
        cfg.setString("commit", null, "template", gitFile.toString());
        cfg.save();

        Settings settings = new Settings();
        settings.setCommitTemplate("  ");

        assertEquals("feat: \n\n# why\n",
                CommitTemplateService.resolve(settings, service));
    }

    @Test
    void resolve_emptyWhenNothingConfigured() {
        assertEquals("", CommitTemplateService.resolve(new Settings(), service));
    }

    @Test
    void expandTemplatePath_resolvesRelativeToWorkTree() {
        Path expanded = CommitTemplateService.expandTemplatePath("tmpl.txt", service);
        assertTrue(expanded.isAbsolute());
        assertEquals(repoDir.resolve("tmpl.txt"), expanded);
    }
}
