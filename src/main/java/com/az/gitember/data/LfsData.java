package com.az.gitember.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

public class LfsData {

    private BooleanProperty lfsSupport = new SimpleBooleanProperty(false);
    private ObservableList<String> extentions = FXCollections.observableArrayList();

    public boolean isLfsSupport() {
        return lfsSupport.get();
    }

    public BooleanProperty lfsSupportProperty() {
        return lfsSupport;
    }

    public void setLfsSupport(boolean lfsSupport) {
        this.lfsSupport.set(lfsSupport);
    }

    public ObservableList<String> getExtentions() {
        return extentions;
    }

    public void setExtentions(ObservableList<String> extentions) {
        this.extentions = extentions;
    }

    @Override
    public String toString() {
        return "LfsData " + lfsSupport.get() + " " + extentions.stream().collect(Collectors.joining(","));
    }
}
