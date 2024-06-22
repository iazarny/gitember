package com.az.gitember.data;

import org.eclipse.jgit.api.ResetCommand;

public class ResetDialogResult {

    private final ResetCommand.ResetType resetType;

    public ResetDialogResult(ResetCommand.ResetType resetType) {
        this.resetType = resetType;
    }

    public ResetCommand.ResetType getResetType() {
        return resetType;
    }
}
