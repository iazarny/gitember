package com.az.gitember.data;

import java.util.Date;
import java.util.List;

public class ScmRevisionInformation {

    private String revisionFullName;
    private String authorName;
    private String authorEmail;
    private String shortMessage;
    private String fullMessage;
    private Date date;
    private List<ScmItem> affectedItems;
    private List<String> ref;
    private List<String> parents;
    private int stashIndex = -1;

    public int getStashIndex() {
        return stashIndex;
    }

    public void setStashIndex(int stashIndex) {
        this.stashIndex = stashIndex;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public List<String> getRef() {
        return ref;
    }

    public void setRef(List<String> ref) {
        this.ref = ref;
    }

    public int getRefCount() {
        return ref.size();
    }

    public String getRevisionFullName() {
        return revisionFullName;
    }

    public void setRevisionFullName(String revisionFullName) {
        this.revisionFullName = revisionFullName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ScmItem> getAffectedItems() {
        return affectedItems;
    }

    public void setAffectedItems(List<ScmItem> affectedItems) {
        this.affectedItems = affectedItems;
    }

    public String getNameExt() {
        final String msg;
        if (this.getStashIndex() >= 0) {
            return  "(" + getStashIndex() +") " + getShortMessage();
        } else {
            return getShortMessage();
        }
    }
}
