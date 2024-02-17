package com.az.gitember.controller;


import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class MainTreeItemCellFactory implements Callback<TreeView<Object>, TreeCell<Object>> {

    private MainTreeContextMenuFactory contextMenuFactory = new MainTreeContextMenuFactory();

    public class ScmItemCell extends TreeCell<Object> {

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setContextMenu(null);
                setGraphic(null);
            } else {
                if (item instanceof String) {
                    setText(item.toString());
                    setGraphic(getTreeItem().getGraphic());
                    if ("Remote branches".equals(item)) {
                        setContextMenu(contextMenuFactory.createSearchContextMenu());
                    } else if ("Local branches".equals(item)) {
                        setContextMenu(contextMenuFactory.createSearchContextMenu());
                    } else if ("Tags".equals(item)) {
                        setContextMenu(contextMenuFactory.createTagContextMenu());
                    }


                } else if (item instanceof ScmBranch) {

                    //TODO add icon if local branch has diff by comparison with remote

                    final ScmBranch scmWorkingBranch = Context.workingBranch.getValue();
                    final String scmWorkingBranchName = ScmBranch.getNameSafe(scmWorkingBranch);
                    final ScmBranch scmBranch = (ScmBranch) item;


                    setContextMenu(contextMenuFactory.createContextMenu(scmBranch));

                    StackedFontIcon stackedFontIcon = new StackedFontIcon();
                    stackedFontIcon.setStyle("-fx-icon-color: text_color");

                    if (scmBranch.getBranchType() == ScmBranch.BranchType.LOCAL) {
                        if (scmWorkingBranchName.equalsIgnoreCase(scmBranch.getFullName())) {
                           setStyle("-fx-font-size: 110%;");
                           stackedFontIcon.getChildren().add(new FontIcon(FontAwesome.CHECK_SQUARE_O));
                        } else {
                            stackedFontIcon.getChildren().add(new FontIcon(FontAwesome.CODE_FORK));
                        }
                        setGraphic(stackedFontIcon);
                    } else if (scmBranch.getBranchType() == ScmBranch.BranchType.REMOTE) {
                        stackedFontIcon.getChildren().add(new FontIcon(FontAwesome.CODE_FORK));
                        setGraphic(stackedFontIcon);
                    } else if (scmBranch.getBranchType() == ScmBranch.BranchType.TAG) {
                        stackedFontIcon.getChildren().add(new FontIcon(FontAwesome.TAG));
                        setGraphic(stackedFontIcon);
                    }

                    setText(scmBranch.getNameExt());
                    scmBranch.getScmBranchTooltip().ifPresent(
                            t -> setTooltip(new Tooltip(t))
                    );

                } else if (item instanceof ScmRevisionInformation) {
                    final ScmRevisionInformation ri = (ScmRevisionInformation) item;
                    setText(ri.getNameExt());
                    setContextMenu(contextMenuFactory.createContextMenu(ri));
                } else {
                    setText(item.toString());
                }
            }
        }
    }


    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ScmItemCell();
    }


}
