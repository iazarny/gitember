package com.az.gitember.data;

import java.util.Collections;
import java.util.List;

public class PullOperationResult {

    private final String status;
    private final List<String> addedFiles;
    private final List<String> deletedFiles;
    private final List<String> changedFiles;

    public PullOperationResult(String status,
                               List<String> addedFiles,
                               List<String> deletedFiles,
                               List<String> changedFiles) {
        this.status       = status;
        this.addedFiles   = addedFiles   != null ? addedFiles   : Collections.emptyList();
        this.deletedFiles = deletedFiles != null ? deletedFiles : Collections.emptyList();
        this.changedFiles = changedFiles != null ? changedFiles : Collections.emptyList();
    }

    public String getStatus()           { return status; }
    public List<String> getAddedFiles() { return addedFiles; }
    public List<String> getDeletedFiles(){ return deletedFiles; }
    public List<String> getChangedFiles(){ return changedFiles; }

    public boolean hasChanges() {
        return !addedFiles.isEmpty() || !deletedFiles.isEmpty() || !changedFiles.isEmpty();
    }

    /** Short human-readable summary for the status bar. */
    public String toStatusString() {
        if (hasChanges()) {
            return String.format("+%d added / -%d deleted / ~%d changed",
                    addedFiles.size(), deletedFiles.size(), changedFiles.size());
        }
        return status;
    }
}
