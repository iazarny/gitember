package com.az.gitember.service;


import java.nio.file.Path;
import java.nio.file.WatchEvent;

@FunctionalInterface
public interface ProjectChangeCallback {
    void onFileChange(WatchEvent.Kind<?> kind, Path fileName);

}
