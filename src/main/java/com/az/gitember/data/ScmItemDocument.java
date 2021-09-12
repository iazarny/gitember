package com.az.gitember.data;

import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitemberUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            body = getContent(item);
        } else {
            body = getContentMedatada(item);
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

    private String getContentMedatada(ScmItem item) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(item.getBody(ScmItem.BODY_TYPE.COMMIT_VERION))) {
            parser.parse(stream, handler, metadata);
            if ("text/plain".equalsIgnoreCase(metadata.get("Content-Type"))) {
                return getContent(item);
            } else {
                return handler.toString() + "\n" + metadata.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private String getContent(ScmItem item) {
        final String body;
        byte [] b = new byte[0];
        try {
            b = item.getBody(ScmItem.BODY_TYPE.COMMIT_VERION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        body = new String(b);
        return body;
    }

}
