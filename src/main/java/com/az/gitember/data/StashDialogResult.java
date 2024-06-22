package com.az.gitember.data;

import org.eclipse.jgit.api.MergeCommand;

public class StashDialogResult {

    private String commitMessage;

    public StashDialogResult(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

}
