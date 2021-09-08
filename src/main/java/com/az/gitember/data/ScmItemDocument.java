package com.az.gitember.data;

import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.az.gitember.service.GitemberUtil.is;

public class ScmItemDocument  {


    private final String revision;
    private final String name;
    private final String body;

    public ScmItemDocument(ScmItem item)  {
        this.name = item.getCommitName();
        this.revision = item.getCommitName();

        if (ExtensionMap.isTextExtension(item.getShortName())) {
            byte [] b = new byte[0];
            try {
                b = item.getBody(ScmItem.BODY_TYPE.COMMIT_VERION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            body = new String(b);
        } else {
            body = "TODO apache tika";
        }
    }

    public String getRevision() {
        return revision;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }
}
