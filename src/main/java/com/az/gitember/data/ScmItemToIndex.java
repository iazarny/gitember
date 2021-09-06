package com.az.gitember.data;

import com.az.gitember.service.ExtensionMap;

import java.io.IOException;

import static com.az.gitember.data.ScmItem.BODY_TYPE.COMMIT_VERION;

public class ScmItemToIndex  {

    private String revision;
    private String name;
    private String body;

    public ScmItemToIndex(ScmItem scmItem) {
        this.revision = scmItem.getCommitName();
        this.name = scmItem.getShortName();
        if (ExtensionMap.isTextExtension(this.name)) {
            try {
                byte [] b = scmItem.getBody(ScmItem.BODY_TYPE.COMMIT_VERION);
                body = new String(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //TODO else mate via apache tika
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
