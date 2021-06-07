package com.az.gitember.data;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class ScmStat {

    private Date date;
    private Map<String, Integer> totalLines;
    private Map<String, Integer> commits;

    public ScmStat() {
        totalLines = Collections.EMPTY_MAP;
        commits = Collections.EMPTY_MAP;
    }

    public ScmStat(Map<String, Integer> totalLines, Map<String, Integer> logMap) {
        this.totalLines = totalLines;
        this.commits = logMap;
    }

    public Map<String, Integer> getLogMap() {
        return commits;
    }

    public void setLogMap(Map<String, Integer> logMap) {
        this.commits = logMap;
    }


    public Map<String, Integer> getTotalLines() {
        int d = 45;
        return totalLines;
    }

    public void setTotalLines(Map<String, Integer> totalLines) {
        this.totalLines = totalLines;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
