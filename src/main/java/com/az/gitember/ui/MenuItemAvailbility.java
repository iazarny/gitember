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

    public MenuItemAvailbility(MenuItem menuItem, boolean localVisible, boolean tagVisible, boolean remoteVisible) {
        this.menuItem = menuItem;
        this.localVisible = localVisible;
        this.tagVisible = tagVisible;
        this.remoteVisible = remoteVisible;
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
}
