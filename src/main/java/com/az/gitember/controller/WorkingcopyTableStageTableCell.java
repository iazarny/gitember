package com.az.gitember.controller;

import com.az.gitember.controller.handlers.StageEventHandler;
import com.az.gitember.data.ScmItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class WorkingcopyTableStageTableCell extends TableCell<ScmItem, ScmItem> {

    private final CheckBox checkBox = new CheckBox();
    private final HBox hBox = new HBox();

    public WorkingcopyTableStageTableCell() {
        super();
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("########## 1 " + getItem());
                new StageEventHandler(getTableView(),  getItem()).handle(null);
                System.out.println("########## 2 " + getItem());
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
                setGraphic(hBox);
            } else {
                checkBox.setSelected(item.staged().equals(1));
                setGraphic(checkBox);
            }

        }

    }


}
