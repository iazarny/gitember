package com.az.gitember.data;

import java.util.List;

public class ScmItemAttribute {

    private String name;
    private String oldName;
    private String status;
    private String substatus;

    public ScmItemAttribute() {
    }

    public String getSubstatus() {
        return substatus;
    }

    public void setSubstatus(String substatus) {
        this.substatus = substatus;
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

    public ScmItemAttribute withOldName(final String oldName) {
        this.oldName = oldName;
        return this;
    }

    public String getOldName() {
        return oldName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ScmItemAttribute withStatus(String status) {
        this.status = status;
        return this;
    }
}
