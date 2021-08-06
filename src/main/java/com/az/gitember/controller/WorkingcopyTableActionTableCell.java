package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

public class WorkingcopyTableActionTableCell extends TableCell<ScmItem, ScmItem> {

    private Button downdloadBtn = new Button("Load", new FontIcon(FontAwesome.DOWNLOAD));
    private Button openBtn = new Button("Open", new FontIcon(FontAwesome.FOLDER_OPEN));

    public WorkingcopyTableActionTableCell() {
        super();
    }

    @Override
    protected void updateItem(ScmItem item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            if (item != null && item.getAttribute() != null) {
                if (ScmItem.Status.LFS_FILE.equals(item.getAttribute().getSubstatus())) {
                    setGraphic(openBtn);
                } else if (ScmItem.Status.LFS_POINTER.equals(item.getAttribute().getSubstatus())) {
                    setGraphic(downdloadBtn);
                }

            } else {
                setGraphic(null);
            }

        }

    }


}
