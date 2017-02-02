package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 24.12.2016.
 */
public interface ScmItemStatus {

    String CONFLICT = "Conflict";

    String CONFLICT_BOTH_DELETED = "CBD"; //Exists in base, but neither in ours nor in theirs.
    String CONFLICT_DELETED_BY_US = "DBU"; // Exists in base and theirs, but not in ours.
    String CONFLICT_DELETED_BY_THEM = "DBT"; //Exists in base and ours, but no in theirs.

    String CONFLICT_ADDED_BY_US = "ABU"; //Only exists in ours.
    String CONFLICT_ADDED_BY_THEM = "ABT"; // Only exists in theirs.
    String CONFLICT_BOTH_ADDED = "BA"; // Exists in ours and theirs, but not in base.
    String CONFLICT_BOTH_MODIFIED = "BM"; //Exists in all stages, content conflict.


    String ADDED = "Added";
    String CHANGED =  "Changed";
    String MISSED = "Missed";
    String MODIFIED = "Modified";
    String REMOVED = "Removed";
    String UNCOMMITED = "Uncommitted";
    String UNTRACKED = "Untracked";
    String UNTRACKED_FOLDER = "UntrackedFolder";

}
