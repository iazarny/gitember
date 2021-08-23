package com.az.gitember.data;

import java.util.function.Predicate;

public class ScmFilterPredicateLfsOnOff implements Predicate {

    private boolean filter;

    public ScmFilterPredicateLfsOnOff(boolean filter) {
        this.filter = filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(Object o) {
        ScmItem item = (ScmItem) o;
        return filter ? true : !ScmItem.Status.LFS.equals(item.getAttribute().getStatus());
    }
}
