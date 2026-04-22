package com.az.gitember.data;

import java.util.Collections;
import java.util.List;

public class PullOperationResult {

    private final String status;
    private final String serverMessages;
    private final List<String> addedFiles;
    private final List<String> deletedFiles;
    private final List<String> changedFiles;

    /**
     * Full SHA-1 of HEAD after the pull, or {@code null} if it could not be
     * determined (e.g. the pull failed or the repo was already up-to-date and
     * HEAD did not move).
     */
    private final String newHeadSha;

    public PullOperationResult(String status,
                               String serverMessages,
                               List<String> addedFiles,
                               List<String> deletedFiles,
                               List<String> changedFiles,
                               String newHeadSha) {
        this.status         = status;
        this.serverMessages = serverMessages != null ? serverMessages.trim() : "";
        this.addedFiles     = addedFiles   != null ? addedFiles   : Collections.emptyList();
        this.deletedFiles   = deletedFiles != null ? deletedFiles : Collections.emptyList();
        this.changedFiles   = changedFiles != null ? changedFiles : Collections.emptyList();
        this.newHeadSha     = newHeadSha;
    }

    public String getStatus()             { return status; }
    public String getServerMessages()     { return serverMessages; }
    public List<String> getAddedFiles()   { return addedFiles; }
    public List<String> getDeletedFiles() { return deletedFiles; }
    public List<String> getChangedFiles() { return changedFiles; }

    /** Full SHA-1 of HEAD after the pull, or {@code null} if unavailable. */
    public String getNewHeadSha()         { return newHeadSha; }

    /**
     * Returns {@code true} when the pull resulted in merge conflicts that still
     * need to be resolved. The status string comes from JGit's
     * {@code MergeResult.MergeStatus} enum.
     */
    public boolean isConflicting() {
        return status != null && status.toLowerCase().contains("conflict");
    }

    /** Returns {@code true} when the remote had nothing new. */
    public boolean isAlreadyUpToDate() {
        return status != null && status.toLowerCase().contains("already");
    }

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
