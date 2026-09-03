package com.az.gitember.service;

import com.az.gitember.data.Settings;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves the commit-message template shown in the commit dialog when AI generation is off.
 * Gitember's own template (Settings) wins; otherwise {@code commit.template} from local then
 * global git config is read.
 */
public class CommitTemplateService {

    private static final Logger log = Logger.getLogger(CommitTemplateService.class.getName());

    public static String resolve() {
        GitRepoService repo = Context.getGitRepoService();
        return resolve(Context.getSettings(), repo);
    }

    public static String resolve(Settings settings, GitRepoService repo) {
        String template = "";
        if (settings != null && StringUtils.isNotBlank(settings.getCommitTemplate())) {
            template = settings.getCommitTemplate();
        } else {
            template = readGitTemplateFile(repo);
        }
        return template != null ? template : "";
    }

    public static String readGitTemplateFile(GitRepoService repo) {
        String text = "";
        String configured = gitTemplatePath(repo);
        if (StringUtils.isNotBlank(configured)) {
            Path file = expandTemplatePath(configured, repo);
            if (file != null && Files.isRegularFile(file)) {
                try {
                    text = Files.readString(file);
                } catch (Exception ex) {
                    log.log(Level.FINE, "Cannot read git commit.template " + file, ex);
                }
            }
        }
        return text;
    }

    static String gitTemplatePath(GitRepoService repo) {
        String path = null;
        if (repo != null) {
            Repository repository = repo.getRepository();
            if (repository != null) {
                path = repository.getConfig().getString("commit", null, "template");
            }
        }
        if (StringUtils.isBlank(path)) {
            try {
                StoredConfig global = SystemReader.getInstance().openUserConfig(null, FS.detect());
                global.load();
                path = global.getString("commit", null, "template");
            } catch (Exception ex) {
                log.log(Level.FINE, "Cannot read global commit.template", ex);
            }
        }
        return path;
    }

    static Path expandTemplatePath(String configured, GitRepoService repo) {
        Path path = null;
        if (StringUtils.isNotBlank(configured)) {
            String expanded = configured.trim();
            if (expanded.startsWith("~/") || expanded.equals("~")) {
                expanded = System.getProperty("user.home") + expanded.substring(1);
            }
            Path candidate = Path.of(expanded);
            if (!candidate.isAbsolute() && repo != null && repo.getRepository() != null) {
                candidate = repo.getRepository().getWorkTree().toPath().resolve(expanded);
            }
            path = candidate;
        }
        return path;
    }
}
