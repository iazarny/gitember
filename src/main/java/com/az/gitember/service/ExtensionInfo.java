package com.az.gitember.service;

public class ExtensionInfo {

    private final String mimeType;
    private final String fileExtension;
    private final boolean text;

    public ExtensionInfo(String mimeType, String fileExtension, boolean text) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public boolean isText() {
        return text;
    }

}
