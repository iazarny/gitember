package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmBranch extends Pair<String, String> {

    private boolean head;

    private String objectIdName;

    public ScmBranch(String shortName, String fullName) {
        super(shortName, fullName);
    }

    public ScmBranch(String shortName, String fullName, String objectIdName) {
        super(shortName, fullName);
        this.objectIdName = objectIdName;
    }

    public String getShortName() {
        return super.getFirst();
    }

    public String getFullName() {
        return super.getSecond();
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    public String getObjectIdName() {
        return objectIdName;
    }

    public void setObjectIdName(String objectIdName) {
        this.objectIdName = objectIdName;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
