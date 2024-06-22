package com.az.gitember.controller.workingcopy;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.eclipse.jgit.diff.DiffEntry;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class WorkingcopyTableGraphicsValueFactory implements ObservableValue<StackedFontIcon> {

    private static Callable<StackedFontIcon> fontIconUnknown = () -> GitemberUtil.create(new FontIcon(FontAwesome.QUESTION));

    static Map<String, Callable<StackedFontIcon>> icons = new HashMap<>();

    static {
        icons.put(ScmItem.Status.RENAMED, () -> GitemberUtil.create(new FontIcon(FontAwesome.COPY)));


        icons.put(ScmItem.Status.UNTRACKED, () -> GitemberUtil.create(new FontIcon(FontAwesome.QUESTION)));
        icons.put(ScmItem.Status.ADDED, () -> GitemberUtil.create(new FontIcon(FontAwesome.PLUS_SQUARE_O)));

        icons.put(ScmItem.Status.REMOVED, () -> GitemberUtil.create(new FontIcon(FontAwesome.MINUS_SQUARE_O)));
        icons.put(ScmItem.Status.MISSED, () -> GitemberUtil.create(new FontIcon(FontAwesome.MINUS_SQUARE_O)));

        icons.put(ScmItem.Status.MODIFIED, () -> GitemberUtil.create(new FontIcon(FontAwesome.EDIT)));
        icons.put(ScmItem.Status.CHANGED, () -> GitemberUtil.create(new FontIcon(FontAwesome.EDIT)));

        icons.put(ScmItem.Status.CONFLICT, () -> GitemberUtil.create(new FontIcon(FontAwesome.EXCHANGE)));
        icons.put(ScmItem.Status.UNCOMMITED, () -> GitemberUtil.create(new FontIcon(FontAwesome.CHECK_SQUARE_O)));

        icons.put(ScmItem.Status.LFS_POINTER, () -> GitemberUtil.create(new FontIcon(FontAwesome.FILE_O))); //DOWNLOAD
        icons.put(ScmItem.Status.LFS_FILE, () -> GitemberUtil.create(new FontIcon(FontAwesome.FILE_PICTURE_O)));
        //icons.put(ScmItem.Status.LFS, () -> GitemberUtil.create(new FontIcon()));

        //Branch diff support


        icons.put(DiffEntry.ChangeType.ADD.name(), () -> GitemberUtil.create(new FontIcon(FontAwesome.PLUS_SQUARE_O)));
        icons.put(DiffEntry.ChangeType.MODIFY.name(), () -> GitemberUtil.create(new FontIcon(FontAwesome.EDIT)));
        icons.put(DiffEntry.ChangeType.DELETE.name(), () -> GitemberUtil.create(new FontIcon(FontAwesome.MINUS_SQUARE_O)));
        icons.put(DiffEntry.ChangeType.COPY.name(), () -> GitemberUtil.create(new FontIcon(FontAwesome.COPY)));
        icons.put(DiffEntry.ChangeType.RENAME.name(), () -> GitemberUtil.create(new FontIcon(FontAwesome.FILE_MOVIE_O)));

    }

    private final String status;
    private final String subStatus;

    public WorkingcopyTableGraphicsValueFactory(final String status, final String subStatus) {
        this.status = status;
        this.subStatus = subStatus;
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
            return icons.getOrDefault(status, icons.getOrDefault(subStatus, fontIconUnknown)).call();
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
