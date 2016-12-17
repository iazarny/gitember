package com.az.gitember.misc;

/**
 *
 * Item attribute.
 *
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItemAttribute {

    private String name;
    private String oldName;

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

    @Override
    public String toString() {
        return "ScmItemAttribute{" +
                " name='" + name + '\'' +
                ", oldName='" + oldName + '\'' +
                '}';
    }
}
