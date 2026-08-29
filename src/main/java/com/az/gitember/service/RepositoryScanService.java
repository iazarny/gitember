package com.az.gitember.service;

import com.az.gitember.data.Const;
import com.az.gitember.data.Project;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scans the conventional source-code folders under the user home for git work-trees.
 *
 * <p>Used on first start — when {@code gitember2.json} holds no repositories yet — to
 * pre-populate the welcome screen. The scan is depth-limited and never descends into a
 * folder that is already a repository, so submodules and vendored clones are not listed
 * as separate projects.
 */
public class RepositoryScanService {

    private static final Logger log = Logger.getLogger(RepositoryScanService.class.getName());

    private static final String SYSTEM_PROP_USER_HOME = "user.home";

    /**
     * Folders under $HOME probed for repositories, in scan order. Several entries differ only
     * in case: on a case-insensitive file system (macOS, Windows) they collapse to one root,
     * see {@link #resolveRoots()}.
     */
    public static final List<String> SCAN_FOLDERS = List.of(
            "Projects", "projects", "Project", "project", "dev", "code", "src", "Workspace", "workspace", "git");

    /** How many levels below a scan root a repository is still discovered. */
    private static final int MAX_DEPTH = 4;

    /** Directory names never descended into: build output, dependency caches, tool state. */
    private static final Set<String> SKIP_FOLDERS = new HashSet<>(Set.of(
            "node_modules", "target", "build", "out", "dist", "bin", "obj",
            "vendor", "Pods", "venv", "__pycache__", "Library"));

    /** Safety cap, so a pathological home folder cannot flood the recent-projects list. */
    private static final int MAX_RESULTS = 200;

    /**
     * Walks the scan roots and collects the normalized home folder of every repository found.
     *
     * @param onFound optional callback invoked for each repository as soon as it is discovered,
     *                on the calling (background) thread; may be {@code null}
     * @return all repository home folders found, in discovery order
     */
    public List<String> scan(Consumer<String> onFound) {
        List<String> found = new ArrayList<>();
        for (Path root : resolveRoots()) {
            log.log(Level.FINE, "Scanning {0} for git repositories", root);
            scanFolder(root, 0, found, onFound);
        }
        return found;
    }

    /** The existing {@link #SCAN_FOLDERS} under $HOME, de-duplicated by real path. */
    List<Path> resolveRoots() {
        Path home = Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME));
        Map<String, Path> byRealPath = new LinkedHashMap<>();
        try {
            byRealPath.put(home.toRealPath().toString(), home);
        } catch (IOException e) {

        }

        for (String name : SCAN_FOLDERS) {
            Path candidate = home.resolve(name);
            if (Files.isDirectory(candidate)) {
                try {
                    byRealPath.putIfAbsent(candidate.toRealPath().toString(), candidate);
                } catch (IOException e) {
                    log.log(Level.FINE, "Cannot resolve " + candidate, e);
                }
            }
        }
        return new ArrayList<>(byRealPath.values());
    }

    private void scanFolder(Path dir, int depth, List<String> found, Consumer<String> onFound) {
        if (depth > MAX_DEPTH || found.size() >= MAX_RESULTS || Thread.currentThread().isInterrupted()) {
            return;
        }
        // ".git" is a folder in a normal clone and a file in a linked worktree / submodule.
        if (Files.exists(dir.resolve(Const.GIT_FOLDER))) {
            String home = Project.normalizeHome(dir.toString());
            found.add(home);
            if (onFound != null) {
                onFound.accept(home);
            }
            return;
        }
        try (DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
            for (Path child : children) {
                if (isScannable(child)) {
                    scanFolder(child, depth + 1, found, onFound);
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            log.log(Level.FINE, "Cannot list " + dir, e);
        }
    }

    /** Skips hidden folders, known build/dependency folders, symlinks (cycles) and plain files. */
    private boolean isScannable(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        String name = fileName.toString();
        return !name.startsWith(".")
                && !SKIP_FOLDERS.contains(name)
                && !Files.isSymbolicLink(path)
                && Files.isDirectory(path);
    }
}
