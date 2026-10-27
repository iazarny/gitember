package com.az.gitember.service;

public class ExtensionInfo {

    private final String mimeType;
    private final String fileExtension;
    private final ExtType extType;

    public static enum ExtType{
        TEXT,
        IMAGE,
        UNKNOWN
    }

    public ExtensionInfo(String mimeType, String fileExtension, ExtType extType) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.extType = extType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public ExtType getExtType() {
        return extType;
    }
}
