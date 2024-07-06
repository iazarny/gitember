package com.az.gitember.service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;


public class ProjectWatcher implements Runnable {

    private final Path rootPath;
    private final Map<WatchKey, Path> watchKeys;
    private final ProjectChangeCallback callback;
    private final WatchService watchService;

    public ProjectWatcher(Path rootPath, ProjectChangeCallback callback) throws IOException {
        this.rootPath = rootPath;
        this.callback = callback;
        this.watchKeys = new HashMap<>();
        this.watchService = FileSystems.getDefault().newWatchService();
        registerAll(rootPath);
    }

    public ProjectWatcher(String rootPath, ProjectChangeCallback callback) throws IOException {
        this(Paths.get(rootPath), callback);
    }

    private void registerAll(final Path root) throws IOException {
        // Register directory and subdirectories
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String candidate = dir.toString();
                if (!isCandidateEligibleToRegister(candidate)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeys.put(key, dir);
        //System.out.println("Registered " + dir);
    }

    @Override
    public void run() {
        try {
            while (true) {
                WatchKey key = watchService.take();
                Path dir = watchKeys.get(key);
                if (dir == null) {
                    continue;
                }


                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path name = (Path) event.context();
                    Path child = dir.resolve(name);



                    // Register new subdirectory if created
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child)) {
                                registerAll(child);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Invoke the callback
                        if (isCandidateEligibleToRegister(child.toString())) {
                            callback.onFileChange(kind, child);
                            //System.out.println(child);
                        }

                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    watchKeys.remove(key);
                    if (watchKeys.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            shutDown();
            Thread.currentThread().interrupt();
        }
    }

    private void shutDown() {
        watchKeys.forEach((k,v) -> {
            k.cancel();
        });
        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isCandidateEligibleToRegister(String candidate) {

        return !(
                candidate.endsWith(".git")
                        ||candidate.endsWith(".github")
                        ||candidate.endsWith("target")
                        ||candidate.endsWith("build")
                        ||candidate.endsWith("dist")
                        ||candidate.endsWith("bin")
                        ||candidate.endsWith("obj")
                        ||candidate.endsWith("lib")
                        ||candidate.endsWith("__pycache__")
                        ||candidate.endsWith(".stack-work")
                        ||candidate.endsWith("dist-newstyle")
                        ||candidate.endsWith("pkg")
                        ||candidate.endsWith("vendor/bundle")
                        ||candidate.endsWith("bundle")
        );

    }


}
