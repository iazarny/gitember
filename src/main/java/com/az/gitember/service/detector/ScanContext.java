package com.az.gitember.service.detector;

import java.nio.file.Path;
import java.util.List;

/**
 * Represent source code . Instead or file reading several times just read it once
 * into scan context and uset it across several detectors.
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class ScanContext {
    private final Path file;
    private final List<String> lines;
    private final FileType fileType;
    private String sha; //sha commit

    public ScanContext(Path file, List<String> lines, FileType fileType) {
        this.file = file;
        this.lines = lines;
        this.fileType = fileType;
    }

    public Path getFile() {
        return file;
    }

    public List<String> getLines() {
        return lines;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }
}
