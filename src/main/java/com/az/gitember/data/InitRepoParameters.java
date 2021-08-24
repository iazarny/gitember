package com.az.gitember.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InitRepoParameters {

    private StringProperty destinationFolder = new SimpleStringProperty("");
    private BooleanProperty initWithReame = new SimpleBooleanProperty(false);
    private BooleanProperty initWithIgnore = new SimpleBooleanProperty(false);
    private BooleanProperty initWithLfs = new SimpleBooleanProperty(false);

    public String getDestinationFolder() {
        return destinationFolder.get();
    }

    public StringProperty destinationFolderProperty() {
        return destinationFolder;
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder.set(destinationFolder);
    }

    public boolean isInitWithReame() {
        return initWithReame.get();
    }

    public BooleanProperty initWithReameProperty() {
        return initWithReame;
    }

    public void setInitWithReame(boolean initWithReame) {
        this.initWithReame.set(initWithReame);
    }

    public boolean isInitWithIgnore() {
        return initWithIgnore.get();
    }

    public BooleanProperty initWithIgnoreProperty() {
        return initWithIgnore;
    }

    public void setInitWithIgnore(boolean initWithIgnore) {
        this.initWithIgnore.set(initWithIgnore);
    }

    public boolean isInitWithLfs() {
        return initWithLfs.get();
    }

    public BooleanProperty initWithLfsProperty() {
        return initWithLfs;
    }

    public void setInitWithLfs(boolean initWithLfs) {
        this.initWithLfs.set(initWithLfs);
    }
}