package com.az.gitember.ui;

import javafx.scene.control.MenuItem;

/**
 * Created by Igor_Azarny on 14.01.2017.
 */
public class MenuItemAvailbility {

    private final MenuItem menuItem;
    private final boolean localVisible;
    private final boolean tagVisible;
    private final boolean remoteVisible;
    private final boolean stashVisible;

    public MenuItemAvailbility(MenuItem menuItem,
                               boolean localVisible, boolean tagVisible,
                               boolean remoteVisible, boolean stashVisible) {
        this.menuItem = menuItem;
        this.localVisible = localVisible;
        this.tagVisible = tagVisible;
        this.remoteVisible = remoteVisible;
        this.stashVisible = stashVisible;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public boolean isLocalVisible() {
        return localVisible;
    }

    public boolean isTagVisible() {
        return tagVisible;
    }

    public boolean isRemoteVisible() {
        return remoteVisible;
    }

    public boolean isStashVisible() {
        return stashVisible;
    }
}
