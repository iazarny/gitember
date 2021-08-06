package com.az.gitember.controller;

import com.az.gitember.controller.handlers.StageEventHandler;
import com.az.gitember.data.ScmItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public class WorkingcopyTableStageTableCell extends TableCell<ScmItem, ScmItem> {

    private CheckBox checkBox = new CheckBox();

    public WorkingcopyTableStageTableCell() {
        super();
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if ( (getItem().staged().equals(1) && oldValue) ||  (getItem().staged().equals(0) && newValue) ) {
                    new StageEventHandler(getTableView(),  getItem()).handle(null);
                }
            }
        });

    }

    @Override
    protected void updateItem(ScmItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (item.staged().equals(-1)) {
                setText(null);
                setGraphic(null);
            } else {
                checkBox.setSelected(item.staged().equals(1));
                setGraphic(checkBox);
            }

        }

    }


}
