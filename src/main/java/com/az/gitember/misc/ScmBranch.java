package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmBranch extends Pair<String, String> {

    public enum BranchType {
        LOCAL("local branch"),
        REMOTE("remote branch"),
        TAG("tag");

        String typeName;

        BranchType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    private boolean head;

    private BranchType branchType;

    private String remoteName;
    private String sha;

    public ScmBranch(String shortName, String fullName, BranchType branchType, String sha) {
        super(shortName, fullName);
        this.branchType = branchType;
        this.sha = sha;
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

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public void setBranchType(BranchType branchType) {
        this.branchType = branchType;
    }

    @Override
    public String toString() {
        return "ScmBranch{" +
                "short=" + super.getFirst() +
                "full=" + super.getSecond() +
                "head=" + head +
                ", branchType=" + branchType +
                ", remoteName='" + remoteName + '\'' +
                '}';
    }
}
