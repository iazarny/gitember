package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.StatusUpdateEventHandler;
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
            } else if (newValue.equals("stat")) {
                file = (String) newValue;
            } else if (newValue.equals("statworkingprogress")) {
                file = (String) newValue;
            } else if (newValue.equals("statbranchlifetime")) {
                file = (String) newValue;
            } else if (newValue.equals("statbranches")) {
                file = (String) newValue;
            } else if (newValue == Context.getMain().historyTreeItem) {
                file = "history";
                Context.updatePlotCommitList(ScmBranch.getNameSafe(Context.workingBranch.getValue()), true, null);
            } else if(newValue instanceof TreeItem
                    && ((TreeItem)newValue).getValue() instanceof ScmBranch) {
                file = "history";
                ScmBranch scmBranch = (ScmBranch)((TreeItem)newValue).getValue();
                Context.updatePlotCommitList(ScmBranch.getNameSafe(scmBranch), false, null);
            } else if (newValue instanceof TreeItem
                    && ((TreeItem)newValue).getValue() instanceof ScmRevisionInformation
                    && ((ScmRevisionInformation)((TreeItem)newValue).getValue()).getStashIndex() >= 0
            ) {
                file = "stash";
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

       nodes.filtered(n -> MEXRGE_BOX.equalsIgnoreCase(n.getId()))
                .forEach(n -> {
                    ((HBox) n).getChildrenUnmodifiable().forEach(
                            mbi -> {
                                if (MEXRGE_TOOLBAR.equalsIgnoreCase(mbi.getId())) {
                                    Context.getMain().mainToolBar.getItems().addAll(((ToolBar) mbi).getItems());
                                } else if (MEXRGE_MENYBAR.equalsIgnoreCase(mbi.getId())) {
                                    ((MenuBar) mbi).getMenus()
                                            .filtered(menu -> MEXRGE_MENU.equalsIgnoreCase(menu.getId()))
                                            .forEach(
                                                    mm -> Context.getMain().mainMenuBar.getMenus().add(2, mm)
                                            );
                                }
                            }
                    );
                });
        if (pane instanceof BorderPane) {
            ((BorderPane) pane).setBottom(null);
        }
    }

    //TODO MERGEXXXX make one
    private String MEXRGE_BOX = "MERGEBOX";
    private String MEXRGE_TOOLBAR = "MERGETB";
    private String MEXRGE_MENYBAR = "MERGEMBAR";
    private String MEXRGE_MENU = "MERGEMENU";
    private String MEXRGE_BTN = "MERGEBTN";

    /**
     * Undock added items.
     */
    private void unmergeMenu() {
        Context.getMain().mainMenuBar.getMenus().removeIf(menu -> MEXRGE_MENU.equals(menu.getId()));
        Context.getMain().mainMenuBar.getMenus().removeIf(menu -> MEXRGE_TOOLBAR.equals(menu.getId()));
        Context.getMain().mainMenuBar.getMenus().removeIf(menu -> MEXRGE_MENYBAR.equals(menu.getId()));
        Context.getMain().mainMenuBar.getMenus().removeIf(menu -> MEXRGE_MENU.equals(menu.getId()));
        Context.getMain().mainToolBar.getItems().removeIf(node -> MEXRGE_BTN.equals(node.getId()));
    }

}
