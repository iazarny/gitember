package com.az.gitember.data;

import org.eclipse.jgit.api.MergeCommand;

public class MergeDialogResult {

    private String branchName;
    private String commitMessage;
    private boolean squash = true;
    private MergeCommand.FastForwardMode fastForwardMode = MergeCommand.FastForwardMode.FF;

    public MergeDialogResult(String branchName, String commitMessage,
                             boolean squash, MergeCommand.FastForwardMode fastForwardMode) {
        this.branchName = branchName;
        this.commitMessage = commitMessage;
        this.squash = squash;
        this.fastForwardMode = fastForwardMode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public boolean isSquash() {
        return squash;
    }

    public void setSquash(boolean squash) {
        this.squash = squash;
    }

    public MergeCommand.FastForwardMode getFastForwardMode() {
        return fastForwardMode;
    }

    public void setFastForwardMode(MergeCommand.FastForwardMode fastForwardMode) {
        this.fastForwardMode = fastForwardMode;
    }
}
