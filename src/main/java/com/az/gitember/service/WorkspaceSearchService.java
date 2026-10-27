package com.az.gitember.service;

import com.az.gitember.data.Project;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Full-text search over the <em>current working copy</em> of every project in a workspace.
 *
 * <p>Each project gets its own Lucene index, stored separately from the per-project history index
 * (see {@link SearchService#HISTORY_INDEX_PREFIX} vs. {@link SearchService#WORKSPACE_INDEX_PREFIX}). Indexing is
 * incremental: on each run the working-tree file list is compared against what is already indexed,
 * so only new or changed files are re-read and deleted files are pruned. The first run over an empty
 * index therefore performs the initial full index; subsequent runs are cheap refreshes.
 *
 * <p>Only reasonably-sized text files are indexed — binary and very large files are skipped.
 */
public class WorkspaceSearchService {

    private static final Logger log = Logger.getLogger(WorkspaceSearchService.class.getName());

    /** Files larger than this (bytes) are skipped to keep indexing fast and the index small. */
    private static final long MAX_FILE_SIZE = 4L * 1024 * 1024;

    /** True when a project already has a working-copy index on disk. */
    public boolean isIndexed(Project project) {
        try (SearchService svc = SearchService.forProject(project)) {
            return svc.hasIndex();
        }
    }

    /** True when the collection is non-empty and <em>every</em> project is already indexed. */
    public boolean allIndexed(Collection<Project> projects) {
        return !projects.isEmpty() && projects.stream().allMatch(this::isIndexed);
    }

    /**
     * (Re)indexes a single project's working copy incrementally: adds new files, re-indexes files
     * whose modification time advanced, and removes files that no longer exist. Against an empty
     * index this is the initial full index.
     *
     * @param progress optional callback receiving (processed, total) file counts
     */
    public void indexProject(Project project, BiConsumer<Integer, Integer> progress) throws Exception {
        String home = project.getProjectHomeFolder();
        if (home == null || home.isBlank()) {
            return;
        }
        GitRepoService git = project.getGitRepoService();
        try (SearchService svc = SearchService.forProject(project)) {

            Map<String, Long> indexed = svc.indexedFileMtimes();
            // Guarantee an index exists on disk even when no file turns out to be indexable,
            // so the project counts as indexed and isn't re-scanned in full on every open.
            svc.ensureCreated();
            Set<String> current = new TreeSet<>();
            Set<String> workingTreeFiles = git.getWorkingTreeFiles();

            int total = workingTreeFiles.size();
            int done = 0;
            for (String path : workingTreeFiles) {
                File file = new File(home, path);
                if (isIndexable(file)) {
                    current.add(path);
                    long mtime = file.lastModified();
                    Long previous = indexed.get(path);
                    if (previous == null || previous < mtime) {
                        String body = readText(file);
                        if (body != null) {
                            svc.updateFileDoc(path, body, mtime);
                        }
                    }
                }
                if (progress != null) {
                    progress.accept(++done, total);
                }
            }

            // Prune files that were indexed before but are no longer part of the working copy.
            for (String indexedPath : indexed.keySet()) {
                if (!current.contains(indexedPath)) {
                    svc.deleteFileDoc(indexedPath);
                }
            }

            svc.commitIndex();
        }
    }

    /** Indexes every project sequentially; a failure on one project does not stop the others. */
    public void indexAll(Collection<Project> projects, BiConsumer<Project, Integer> perProjectProgress) {
        int processed = 0;
        for (Project project : projects) {
            try {
                indexProject(project, null);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cannot index working copy for " + project, ex);
            }
            if (perProjectProgress != null) {
                perProjectProgress.accept(project, ++processed);
            }
        }
    }

    /**
     * Searches the working-copy content of every project and returns, per project, the set of
     * matching repo-relative file paths. Projects with no match (or no index) are omitted.
     */
    public Map<Project, Set<String>> search(Collection<Project> projects, String term) {
        Map<Project, Set<String>> rez = new LinkedHashMap<>();
        for (Project project : projects) {
            try (SearchService svc = SearchService.forProject(project)) {
                if (!svc.hasIndex()) {
                    continue;
                }
                Set<String> matches = svc.searchFiles(term);
                if (!matches.isEmpty()) {
                    rez.put(project, matches);
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Search failed for " + project, ex);
            }
        }
        return rez;
    }

    private static boolean isIndexable(File file) {
        return file.isFile()
                && file.length() <= MAX_FILE_SIZE
                && ExtensionInfo.ExtType.TEXT == ExtensionMap.getExtensionType(file.getName());
    }

    private static String readText(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception ex) {
            log.log(Level.FINE, "Cannot read " + file, ex);
            return null;
        }
    }
}
