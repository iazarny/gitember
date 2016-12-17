package com.az.gitember.ui;

import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItemCellFactory implements Callback<ListView, ListCell> {

    public static class ScmItemCell extends ListCell<ScmBranch> {
        @Override
        protected void updateItem(ScmBranch item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null || item.getShortName() == null) {
                setText(null);
            } else {
                setText(item.getShortName());
                if (item.isHead()) {
                    setText(item.getShortName());
                    setStyle("-fx-font-weight: bold;");

                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public ListCell call(final ListView param) {
        return new ScmItemCell();
    }

}
