package com.az.gitember.data;

import java.util.Optional;

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
    private int aheadCount = 0;
    private int behindCount = 0;

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
            return  scmBranch.getNameExt();
        }
        return "";

    }

    public String getNameExt() {
        if (getRemoteMergeName() == null ) {
            return  getShortName();
        } else {
            return String.format("%s -> %s",
                    getShortName(),
                    getRemoteMergeName()
            );
        }
    }



    public Optional<String> getScmBranchPushTooltip() {
        String tooltip = null;
        if (getAheadCount() > 0) {
            tooltip = String.format("%s ahead of %s on %d commit(s)", getShortName(), getRemoteMergeName(), getAheadCount() );
        }
        return Optional.ofNullable(tooltip);
    }
    public Optional<String> getScmBranchPullTooltip() {
        String tooltip = null;
        if (getBehindCount() > 0) {
            tooltip = String.format("%s behind of %s on %d commit(s)", getShortName(), getRemoteMergeName(), getBehindCount());
        }
        return Optional.ofNullable(tooltip);
    }

    public Optional<String> getScmBranchTooltip() {
        String tooltip = null;
        if (getAheadCount() > 0) {
            tooltip = String.format("%s ahead of %s on %d commit(s)", getShortName(), getRemoteMergeName(), getAheadCount() );
        }
        if (getBehindCount() > 0) {
            if (getAheadCount() > 0) {
                tooltip += String.format(" and behind on %d", getBehindCount());
            } else {
                tooltip = String.format("%s behind of %s on %d commit(s)", getShortName(), getRemoteMergeName(), getBehindCount());
            }
        }
        return Optional.ofNullable(tooltip);
    }



    public ScmBranch(String shortName, String fullName, BranchType branchType, String sha) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.branchType = branchType;
        this.sha = sha;
    }

    public ScmBranch(String shortName, String fullName, BranchType branchType, String sha, int aheadCount, int behindCount) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.branchType = branchType;
        this.sha = sha;
        this.aheadCount = aheadCount;
        this.behindCount = behindCount;
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

    public int getAheadCount() {
        return aheadCount;
    }

    public int getBehindCount() {
        return behindCount;
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
