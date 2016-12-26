package com.az.gitember.misc;

import java.util.List;

/**
 *
 * Item attribute.
 *
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItemAttribute {

    private String name;
    private String oldName;
    private List<String> status;

    public ScmItemAttribute(List<String> status) {
        this.status = status;
    }

    public ScmItemAttribute() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOldName(final String oldName) {
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ScmItemAttribute{" +
                " name='" + name + '\'' +
                ", oldName='" + oldName + '\'' +
                '}';
    }
}
