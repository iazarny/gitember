package com.az.gitember.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LfsData {

    private boolean lfsSupport = false;
    private List<String> extentions = new ArrayList<>();

    public boolean isLfsSupport() {
        return lfsSupport;
    }

    public void setLfsSupport(boolean lfsSupport) {
        this.lfsSupport = lfsSupport;
    }

    public List<String> getExtentions() {
        return extentions;
    }

    public void setExtentions(List<String> extentions) {
        this.extentions = extentions;
    }

    @Override
    public String toString() {
        return "LfsData " + lfsSupport + " " + extentions.stream().collect(Collectors.joining(","));
    }
}
