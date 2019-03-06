package com.az.gitember.misc;

import java.util.Map;

/**
 * Created by igor on 05.03.2019.
 */
public class ScmStat {

    private Map<String, Integer> total;
    private Map<String, Integer> logMap;

    public ScmStat(Map<String, Integer> total, Map<String, Integer> logMap) {
        this.total = total;
        this.logMap = logMap;
    }

    public Map<String, Integer> getLogMap() {
        return logMap;
    }

    public void setLogMap(Map<String, Integer> logMap) {
        this.logMap = logMap;
    }


    public Map<String, Integer> getTotal() {
        return total;
    }

    public void setTotal(Map<String, Integer> total) {
        this.total = total;
    }
}
