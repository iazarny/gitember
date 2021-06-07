package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome.FontAwesome;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class WorkingcopyTableGraphicsValueFactory implements ObservableValue<FontIcon> {

    private static Callable<FontIcon> fontIconUnknown = () -> new FontIcon(FontAwesome.QUESTION);

    static Map<String, Callable<FontIcon>> icons = new HashMap<>();

    static {
        icons.put(ScmItem.Status.RENAMED, () -> new FontIcon(FontAwesome.COPY));


        icons.put(ScmItem.Status.UNTRACKED, () -> new FontIcon(FontAwesome.QUESTION));
        icons.put(ScmItem.Status.ADDED, () -> new FontIcon(FontAwesome.PLUS_SQUARE_O));

        icons.put(ScmItem.Status.REMOVED, () -> new FontIcon(FontAwesome.MINUS_SQUARE_O));
        icons.put(ScmItem.Status.MISSED, () -> new FontIcon(FontAwesome.MINUS_SQUARE_O));

        icons.put(ScmItem.Status.MODIFIED, () -> new FontIcon(FontAwesome.EDIT));
        icons.put(ScmItem.Status.CHANGED, () -> new FontIcon(FontAwesome.EDIT));

        icons.put(ScmItem.Status.CONFLICT, () -> new FontIcon(FontAwesome.EXCHANGE));
        icons.put(ScmItem.Status.UNCOMMITED, () -> new FontIcon(FontAwesome.CHECK_SQUARE_O));
    }

    private final String status;

    public WorkingcopyTableGraphicsValueFactory(final String status) {
        this.status = status;
    }

    @Override
    public void addListener(ChangeListener<? super FontIcon> changeListener) {

    }

    @Override
    public void removeListener(ChangeListener<? super FontIcon> changeListener) {

    }

    @Override
    public FontIcon getValue() {
        try {
            return icons.getOrDefault(status, fontIconUnknown).call();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {

    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {

    }



}
