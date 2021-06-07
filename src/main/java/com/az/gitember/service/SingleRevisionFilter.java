package com.az.gitember.service;


import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;


/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class SingleRevisionFilter extends RevFilter {

    private final String revisionCommitName;

    public SingleRevisionFilter(String revisionCommitName) {
        super();
        this.revisionCommitName = revisionCommitName;
    }

    @Override
    public boolean include(RevWalk walker, RevCommit cmit)
            throws StopWalkException {
        return cmit.getName().equals(revisionCommitName);
    }

    @Override
    public RevFilter clone() {
        return null;
    }


}
