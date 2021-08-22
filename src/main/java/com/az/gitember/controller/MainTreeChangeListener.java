package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.StatusUpdateEventHandler;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTreeChangeListener implements ChangeListener {

    private final static Logger log = Logger.getLogger(MainTreeChangeListener.class.getName());
    private final static String MEXRGE_ITEM = "MERGEITEM";

    @Override
    public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
        resolveView(newValue).ifPresent(
                pane -> {

                    unmergeMenu();
                    Context.getMain().hostPanel.getChildren().clear();
                    Context.getMain().hostPanel.getChildren().add(pane);
                    mergeMenu(pane);

                }
        );
    }

    private Optional<Parent> resolveView(Object newValue) {
        String file = null;
        try {
            if (newValue == Context.getMain().workingCopyTreeItem) {
                file = "workingcopy";
                if(Context.statusList.isEmpty()) { //TODO or is changed
                    Platform.runLater( () ->   new StatusUpdateEventHandler(false).handle(null));
                }
            } else if (newValue.equals(Const.View.STAT)) {
                file = (String) newValue;
            } else if (newValue.equals(Const.View.STAT_WORK_PROGRESS)) {
                file = (String) newValue;
            } else if (newValue.equals(Const.View.STAT_BRANCH_LIFETOME)) {
                file = (String) newValue;
            } else if (newValue.equals(Const.View.STAT_BRANCHES)) {
                file = (String) newValue;
            } else if (newValue == Context.getMain().historyTreeItem) {
                file = Const.View.HISTORY;
                Context.updatePlotCommitList(ScmBranch.getNameSafe(Context.workingBranch.getValue()), true, null);
            } else if(newValue instanceof TreeItem
                    && ((TreeItem)newValue).getValue() instanceof ScmBranch) {
                file = Const.View.HISTORY;
                ScmBranch scmBranch = (ScmBranch)((TreeItem)newValue).getValue();
                Context.updatePlotCommitList(ScmBranch.getNameSafe(scmBranch), false, null);
            } else if (newValue instanceof TreeItem
                    && ((TreeItem)newValue).getValue() instanceof ScmRevisionInformation
                    && ((ScmRevisionInformation)((TreeItem)newValue).getValue()).getStashIndex() >= 0
            ) {
                file = Const.View.STASH;
                ScmRevisionInformation ri = (ScmRevisionInformation)((TreeItem)newValue).getValue();
                Context.stashItemsList.clear();
                Context.stashItemsList.addAll(ri.getAffectedItems());

            } else {
                return Optional.empty();
            }
            Context.mainPaneName.setValue(file);
            return Optional.of(App.loadFXML(file).getFirst());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot load fxml {0}. {1}", new String[] {file, e.getMessage()});
        }
        return Optional.empty();
    }

    private void mergeMenu(Parent pane) {

        final ObservableList<Node> nodes;

        if (pane instanceof SplitPane) {
            nodes = ((SplitPane) pane).getItems();
        } else if(pane instanceof BorderPane) {
            nodes = pane.getChildrenUnmodifiable();
        } else {
            nodes = FXCollections.emptyObservableList();
        }

       nodes.filtered(n -> n instanceof HBox &&  MEXRGE_ITEM.equalsIgnoreCase(n.getId()))
                .forEach(n -> {
                    ((HBox) n).getChildrenUnmodifiable().forEach(
                            mbi -> {
                                if (mbi instanceof ToolBar && MEXRGE_ITEM.equalsIgnoreCase(mbi.getId())) {
                                    Context.getMain().mainToolBar.getItems().addAll(((ToolBar) mbi).getItems());
                                } else if (mbi instanceof MenuBar &&  MEXRGE_ITEM.equalsIgnoreCase(mbi.getId())) {
                                    ((MenuBar) mbi).getMenus()
                                            .filtered(menu -> MEXRGE_ITEM.equalsIgnoreCase(menu.getId()))
                                            .forEach(
                                                    mm -> Context.getMain().mainMenuBar.getMenus().add(1, mm)
                                            );
                                }
                            }
                    );
                });
        if (pane instanceof BorderPane) {
            ((BorderPane) pane).setBottom(null);
        }
    }



    /**
     * Undock added items.
     */
    private void unmergeMenu() {
        Context.getMain().mainMenuBar.getMenus().removeIf(menu -> MEXRGE_ITEM.equals(menu.getId()));
        Context.getMain().mainToolBar.getItems().removeIf(node -> MEXRGE_ITEM.equals(node.getId()));
    }

}
