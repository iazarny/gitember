package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class WorkingcopyTableStatusCallback implements Callback<TableColumn<ScmItem, ScmItem>, TableCell<ScmItem, ScmItem>> {
    @Override
    public TableCell<ScmItem, ScmItem> call(TableColumn<ScmItem, ScmItem> param) {
        return new TableCell<ScmItem, ScmItem>() {

            @Override
            protected void updateItem(ScmItem item, boolean empty) {
                super.updateItem(item, empty);
                String color = null;
                if (!empty) {
                    switch (item.getAttribute().getStatus()) {
                        case ScmItem.Status.RENAMED:
                            color = "#10EA10;";
                            break;
                        case ScmItem.Status.ADDED:
                            color = "#10EA10;";
                            break;
                        case ScmItem.Status.CHANGED:
                            color = "#10EA10;";
                            break;
                        case ScmItem.Status.MODIFIED:
                            color = "#EA1010;";
                            break;
                        case ScmItem.Status.REMOVED:
                            color = "#10EA10;";
                            break;
                        case ScmItem.Status.MISSED:
                            color = "#EA1010;";
                            break;
                        case ScmItem.Status.CONFLICT:
                            color = "#D330FF;";
                            break;
                        case ScmItem.Status.UNTRACKED_FOLDER:
                            color = "#808080;";
                            break;
                        case ScmItem.Status.UNTRACKED:
                            color = "#808080;";
                            break;
                        default:
                            color ="";
                    }

                } else {
                    color = "transparent;";
                }

                setStyle("-fx-background-color: " + color);

            }
        };
    }
}
