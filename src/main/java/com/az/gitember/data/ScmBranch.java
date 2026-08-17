package com.az.gitember.data;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by Igor Azarny on 03 - Dec - 2016
 */
public class ScmBranch implements Comparable<ScmBranch> {



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
    public int compareTo(@NonNull ScmBranch o) {
        int ret = sha.compareTo(o.sha);
        if (ret == 0) {
            ret = fullName.compareTo(o.fullName);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScmBranch scmBranch = (ScmBranch) o;
        return Objects.equals(sha, scmBranch.sha) && Objects.equals(fullName, scmBranch.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha, fullName);
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
