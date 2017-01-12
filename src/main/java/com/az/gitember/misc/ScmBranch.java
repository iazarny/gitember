package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmBranch extends Pair<String, String> {

    public enum BranchType {
        LOCAL,
        REMOTE,
        TAG
    }

    private boolean head;

    private BranchType branchType;

    private String remoteName;

    public ScmBranch(String shortName, String fullName, BranchType branchType) {
        super(shortName, fullName);
        this.branchType = branchType;
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
