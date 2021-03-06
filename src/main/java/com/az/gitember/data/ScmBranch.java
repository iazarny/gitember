package com.az.gitember.data;

/**
 * Created by Igor Azarny on 03 - Dec - 2016
 */
public class ScmBranch {


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
    private String remoteName;
    private String remoteMergeName;

    private final BranchType branchType;
    private final String sha;
    private final String shortName;
    private final String fullName;

    public static String getNameSafe(ScmBranch scmBranch) {
        if (scmBranch != null) {
           return scmBranch.getFullName();
        }
        return "";
    }

    public static String getNameExtSafe(ScmBranch scmBranch) {
        if (scmBranch != null) {
            if (scmBranch.getRemoteMergeName() == null ) {
                return scmBranch.getShortName();
            } else {
                return scmBranch.getShortName() + " (" + scmBranch.getRemoteMergeName() + ")";
            }
        }
        return "";
    }

    public ScmBranch(String shortName, String fullName, BranchType branchType, String sha) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.branchType = branchType;
        this.sha = sha;
    }


    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
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

    public String getRemoteMergeName() {
        return remoteMergeName;
    }

    public void setRemoteMergeName(String remoteMergeName) {
        this.remoteMergeName = remoteMergeName;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public String getSha() {
        return sha;
    }

    public String getNameExt() {
        if (getRemoteMergeName() == null ) {
            return  getShortName();
        } else {
            return getShortName() + " (" + getRemoteMergeName() + ")";
        }
    }

    @Override
    public String toString() {
        return "ScmBranch{" +
                "head=" + head +
                ", remoteName='" + remoteName + '\'' +
                ", remoteMergeName='" + remoteMergeName + '\'' +
                ", branchType=" + branchType +
                ", sha='" + sha + '\'' +
                ", shortName='" + shortName + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
