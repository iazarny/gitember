package com.az.gitember.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class InitRepoParameters {

    private StringProperty destinationFolder = new SimpleStringProperty("");
    private BooleanProperty initWithFiles = new SimpleBooleanProperty(false);

    public String getDestinationFolder() {
        return destinationFolder.get();
    }

    public StringProperty destinationFolderProperty() {
        return destinationFolder;
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder.set(destinationFolder);
    }

    public boolean isInitWithFiles() {
        return initWithFiles.get();
    }

    public BooleanProperty initWithFilesProperty() {
        return initWithFiles;
    }

    public void setInitWithFiles(boolean initWithFiles) {
        this.initWithFiles.set(initWithFiles);
    }
}