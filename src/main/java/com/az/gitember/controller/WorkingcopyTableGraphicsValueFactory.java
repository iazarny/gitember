package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class WorkingcopyTableGraphicsValueFactory implements ObservableValue<StackedFontIcon> {

    private static Callable<StackedFontIcon> fontIconUnknown = () -> create(new FontIcon(FontAwesome.QUESTION));

    static Map<String, Callable<StackedFontIcon>> icons = new HashMap<>();

    static {
        icons.put(ScmItem.Status.RENAMED, () -> create(new FontIcon(FontAwesome.COPY)));


        icons.put(ScmItem.Status.UNTRACKED, () -> create(new FontIcon(FontAwesome.QUESTION)));
        icons.put(ScmItem.Status.ADDED, () -> create(new FontIcon(FontAwesome.PLUS_SQUARE_O)));

        icons.put(ScmItem.Status.REMOVED, () -> create(new FontIcon(FontAwesome.MINUS_SQUARE_O)));
        icons.put(ScmItem.Status.MISSED, () -> create(new FontIcon(FontAwesome.MINUS_SQUARE_O)));

        icons.put(ScmItem.Status.MODIFIED, () -> create(new FontIcon(FontAwesome.EDIT)));
        icons.put(ScmItem.Status.CHANGED, () -> create(new FontIcon(FontAwesome.EDIT)));

        icons.put(ScmItem.Status.CONFLICT, () -> create(new FontIcon(FontAwesome.EXCHANGE)));
        icons.put(ScmItem.Status.UNCOMMITED, () -> create(new FontIcon(FontAwesome.CHECK_SQUARE_O)));
    }

    private final String status;

    public WorkingcopyTableGraphicsValueFactory(final String status) {
        this.status = status;
    }

    @Override
    public void addListener(ChangeListener<? super StackedFontIcon> changeListener) {

    }

    @Override
    public void removeListener(ChangeListener<? super StackedFontIcon> changeListener) {

    }

    @Override
    public StackedFontIcon getValue() {
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

    private static StackedFontIcon create(final FontIcon fontIcon) {
        final StackedFontIcon stackedFontIcon = new StackedFontIcon();
        stackedFontIcon.setStyle("-fx-icon-color: text_color");
        stackedFontIcon.getChildren().add(fontIcon);
        return stackedFontIcon;
    }

}
