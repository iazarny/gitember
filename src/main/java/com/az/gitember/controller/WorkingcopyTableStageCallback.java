package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class WorkingcopyTableStageCallback implements Callback<TableColumn<ScmItem, ScmItem>, TableCell<ScmItem, ScmItem>> {

    @Override
    public TableCell<ScmItem, ScmItem> call(TableColumn<ScmItem, ScmItem> param) {
        return new WorkingcopyTableStageTableCell();
    }


}
