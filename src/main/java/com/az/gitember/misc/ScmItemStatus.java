package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 24.12.2016.
 */
public interface ScmItemStatus {

    String CONFLICT = "Conflict";
    String CONFLICT_STAGE = "ConflictingState";
    String ADDED = "Added";
    String CHANGED =  "Changed";
    String MISSED = "Missed";
    String MODIFIED = "Modified";
    String REMOVED = "Removed";
    String UNCOMMITED = "Uncommitted";
    String UNTRACKED = "Untracked";
    String UNTRACKED_FOLDER = "UntrackedFolder";

}
