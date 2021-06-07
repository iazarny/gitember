package com.az.gitember.stat;

import java.util.Date;

public class StatDTO {

    private String title;
    private Date created_at;
    private Date merged_at;
    private int user_notes_count;
    private int upvotes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getMerged_at() {
        return merged_at;
    }

    public void setMerged_at(Date merged_at) {
        this.merged_at = merged_at;
    }

    public int getUser_notes_count() {
        return user_notes_count;
    }

    public void setUser_notes_count(int user_notes_count) {
        this.user_notes_count = user_notes_count;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }
}
