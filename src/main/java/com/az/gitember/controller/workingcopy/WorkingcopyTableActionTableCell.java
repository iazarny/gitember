package com.az.gitember.controller.workingcopy;

import com.az.gitember.controller.handlers.LfsDownloadEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.data.ScmItem;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

public class WorkingcopyTableActionTableCell extends TableCell<ScmItem, ScmItem> {
    private final Button downdloadBtn;
    private final Button openBtn;

    public WorkingcopyTableActionTableCell() {

        super();

        StackedFontIcon openIcon = new StackedFontIcon();
        openIcon.getChildren().add(new FontIcon(FontAwesome.FOLDER_OPEN));
        openIcon.setStyle("-fx-icon-color: text_color");

        StackedFontIcon loadIcon = new StackedFontIcon();
        loadIcon.getChildren().add(new FontIcon(FontAwesome.DOWNLOAD));
        loadIcon.setStyle("-fx-icon-color: text_color");

        downdloadBtn = new Button("Load", loadIcon);
        openBtn = new Button("Open", openIcon);

    }

    @Override
    protected void updateItem(ScmItem item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (empty) {
            setGraphic(null);
        } else {

            if (item != null && item.getAttribute() != null) {

                openBtn.setOnAction(
                        new OpenFileEventHandler(item, ScmItem.BODY_TYPE.WORK_SPACE)
                );

                downdloadBtn.setOnAction( event -> {
                    new LfsDownloadEventHandler(item).handle(event);
                    item.getAttribute().setSubstatus(ScmItem.Status.LFS_FILE);
                    getTableView().refresh();
                });

                if (ScmItem.Status.LFS.equals(item.getAttribute().getStatus())) {
                    if (ScmItem.Status.LFS_FILE.equals(item.getAttribute().getSubstatus())) {
                        setGraphic(openBtn);
                    } else if (ScmItem.Status.LFS_POINTER.equals(item.getAttribute().getSubstatus())) {
                        setGraphic(downdloadBtn);
                    }

                } else {
                    if (ScmItem.Status.LFS_FILE.equals(item.getAttribute().getSubstatus())) {
                        setGraphic(openBtn);
                    }
                }

            } else {
                setGraphic(null);
            }

        }

    }

}
