package com.az.gitember.misc;

import java.util.Date;
import java.util.List;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
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

    @Override
    public String toString() {
        return "ScmRevisionInformation{" +
                "revisionFullName='" + revisionFullName + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorEmail='" + authorEmail + '\'' +
                ", shortMessage='" + shortMessage + '\'' +
                ", fullMessage='" + fullMessage + '\'' +
                ", date=" + date +
                ", affectedItems=" + affectedItems +
                ", ref=" + ref +
                ", parents=" + parents +
                '}';
    }
}
