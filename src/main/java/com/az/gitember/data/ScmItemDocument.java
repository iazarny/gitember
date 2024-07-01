package com.az.gitember.data;

import com.az.gitember.service.ExtensionMap;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ScmItemDocument  {


    private final String revision;
    private final String name;
    private final String body;

    public ScmItemDocument(ScmItem item)  {
        this.name = item.getShortName();
        this.revision = item.getCommitName();

        if (ExtensionMap.isTextExtension(item.getShortName())) {
            body = getContent(item);
        } else {
            body = getContentMedatada(item);
        }
    }

    public ScmItemDocument(String revision, String name) {
        this.revision = revision;
        this.name = name;
        this.body = null;
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
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(item.getBody(ScmItem.BODY_TYPE.COMMIT_VERSION))) {
            parser.parse(stream, handler, metadata);
            if ("text/plain".equalsIgnoreCase(metadata.get("Content-Type"))) {
                return getContent(item);
            } else {
                return handler.toString() + "\n" + metadata.toString();
            }

        } catch (NoSuchMethodError e) {
            return "";
        } catch (Exception e) {
            return "";
        }
    }


    private String getContent(ScmItem item) {
        final String body;
        byte [] b = new byte[0];
        try {
            b = item.getBody(ScmItem.BODY_TYPE.COMMIT_VERSION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        body = new String(b);
        return body;
    }

}
