package com.az.gitember.ui;

import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItemCellFactory implements Callback<ListView<ScmBranch>, ListCell> {

    private  final ContextMenu contextMenu;

    public ScmItemCellFactory(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public static class ScmItemCell extends ListCell<ScmBranch> {

        private  final ContextMenu contextMenu;

        public ScmItemCell(ContextMenu contextMenu) {
            super();
            this.contextMenu = contextMenu;
        }

        @Override
        protected void updateItem(ScmBranch item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getShortName() == null) {
                setText(null);
                this.setContextMenu(null);
            } else {
                setText(item.getShortName());
                if (item.isHead()) {
                    setText(item.getShortName());
                    setStyle("-fx-font-weight: bold;");
                }
                this.setContextMenu(contextMenu);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public ListCell call(final ListView param) {
        ScmItemCell scmItemCell = new ScmItemCell(contextMenu);
        return scmItemCell;
    }

}
