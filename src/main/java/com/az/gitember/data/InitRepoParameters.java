package com.az.gitember.data;

public class InitRepoParameters {

    private String destinationFolder = "";
    private boolean initWithReame = false;
    private boolean initWithIgnore = false;
    private boolean initWithLfs = false;

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public boolean isInitWithReame() {
        return initWithReame;
    }

    public void setInitWithReame(boolean initWithReame) {
        this.initWithReame = initWithReame;
    }

    public boolean isInitWithIgnore() {
        return initWithIgnore;
    }

    public void setInitWithIgnore(boolean initWithIgnore) {
        this.initWithIgnore = initWithIgnore;
    }

    public boolean isInitWithLfs() {
        return initWithLfs;
    }

    public void setInitWithLfs(boolean initWithLfs) {
        this.initWithLfs = initWithLfs;
    }
}
