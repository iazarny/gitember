package com.az.gitember.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;



public class ProjectWatcher implements Runnable {
    private final Path path;
    private final ProjectChangeCallback callback;
    private volatile boolean stopFlag = false;

    public ProjectWatcher(Path path, ProjectChangeCallback callback) {
        this.path = path;
        this.callback = callback;
    }

    public ProjectWatcher(String path, ProjectChangeCallback callback) {
        this.path = Paths.get(path);
        this.callback = callback;
    }

    public void setStopFlag() {
        this.stopFlag = true;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the path with the watch service for specified events
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watch Service registered for directory: " + path.toString());

            // Poll for file system events on the WatchService
            WatchKey key;
            while ((key = watchService.take()) != null && !stopFlag) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    // Print the kind of event and the affected file
                    System.out.println("Event kind: " + event.kind() + ". File affected: " + event.context() + ".");
                    callback.onFileChange(event.kind(), (Path) event.context());

                }
                // Reset the key to receive further watch events
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
