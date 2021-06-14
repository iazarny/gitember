package com.az.gitember.controller;

import com.az.gitember.controller.handlers.DiffWithDiskEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.controller.handlers.RevertEventHandler;
import com.az.gitember.controller.handlers.StageEventHandler;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.eclipse.jgit.api.CheckoutCommand;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.az.gitember.service.GitemberUtil.is;

public class WorkingcopyTableRowFactory implements Callback<TableView, TableRow> {

    private final static Logger log = Logger.getLogger(WorkingcopyTableRowFactory.class.getName());


    private ContextMenu scmItemContextMenu = new ContextMenu();

    private Workingcopy workingcopy;


    public WorkingcopyTableRowFactory(Workingcopy workingcopy) {
        //Just  place holder. Menu without any initial elements will not show even if item will nbe added on request menu
        scmItemContextMenu.getItems().add(new MenuItem(""));
        this.workingcopy = workingcopy;
    }

    @Override
    public TableRow call(TableView param) {
        return new TableRow<ScmItem>() {

            private String calculateStyle(final ScmItem scmItem) {
                if (scmItem != null) {
                    if (workingcopy.searchText.getText() != null
                            && workingcopy.searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                        if (scmItem.getShortName().toLowerCase().contains(
                                workingcopy.searchText.getText().toLowerCase())) {
                            return LookAndFeelSet.FOUND_ROW;
                        }

                    }

                }
                return  "";

            }


            @Override
            protected void updateItem(ScmItem item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(calculateStyle(item));
                if (!empty) {

                    setContextMenu(scmItemContextMenu);
                    setOnContextMenuRequested(
                            evt -> {

                                final String itemStatus = item.getAttribute().getStatus();
                                scmItemContextMenu.getItems().clear();

                                if(is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED)) {
                                    MenuItem stage = new MenuItem("Stage item");
                                    stage.setOnAction(e -> {
                                        new StageEventHandler(param, item).handle(null);
                                    });
                                    scmItemContextMenu.getItems().add(stage);
                                }

                                if(is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
                                    MenuItem unstage = new MenuItem("Unstage item");
                                    unstage.setOnAction(e -> {
                                        new StageEventHandler(param, item).handle(null);
                                    });
                                    scmItemContextMenu.getItems().add(unstage);
                                }

                                if(is(itemStatus).oneOf(ScmItem.Status.CONFLICT) ) {
                                    Menu resolve = new Menu("Resolve conflict");

                                    MenuItem markResolved = new MenuItem("Mark resolved");
                                    markResolved.setOnAction(a -> {
                                        new StageEventHandler(param, item).handle(null);
                                    });
                                    resolve.getItems().add(markResolved);

                                    MenuItem resolveUsingMine = new MenuItem("Using mine");
                                    resolveUsingMine.setOnAction(a -> {
                                        try {
                                            Context.getGitRepoService().checkoutFile(item.getShortName(), CheckoutCommand.Stage.OURS);
                                            new StageEventHandler(param, item).handle(null);
                                        } catch (IOException e) {
                                            log.log(Level.SEVERE, "Cannot resolve conflict using my changes {0}. {1}", new String[] {item.getShortName(), e.getMessage()});
                                            Context.getMain().showResult("Conflict using my changes", e.getMessage(), Alert.AlertType.ERROR);
                                        }
                                    });
                                    resolve.getItems().add(resolveUsingMine);

                                    MenuItem resolveUsingTheir = new MenuItem("Using their");
                                    resolveUsingTheir.setOnAction(a -> {
                                        try {
                                            Context.getGitRepoService().checkoutFile(item.getShortName(), CheckoutCommand.Stage.THEIRS);
                                            new StageEventHandler(param, item).handle(null);
                                        } catch (IOException e) {
                                            Context.getMain().showResult("Conflict using theirs changes", e.getMessage(), Alert.AlertType.ERROR);
                                            log.log(Level.SEVERE, "Cannot resolve conflict using theirs changes {0}. {1}", new String[] {item.getShortName(), e.getMessage()});
                                        }
                                    });
                                    resolve.getItems().add(resolveUsingTheir);

                                    scmItemContextMenu.getItems().add(resolve);
                                }

                                scmItemContextMenu.getItems().add(new SeparatorMenuItem());

                                if(!is(itemStatus).oneOf(ScmItem.Status.MISSED, ScmItem.Status.REMOVED) ) {
                                    MenuItem open = new MenuItem("Open");
                                    open.setOnAction(new OpenFileEventHandler(item, ScmItem.BODY_TYPE.WORK_SPACE));
                                    scmItemContextMenu.getItems().add(open);
                                }

                                if(!is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED, ScmItem.Status.REMOVED) ) {
                                    //MenuItem history = new MenuItem("History");  TODO
                                    //scmItemContextMenu.getItems().add(history);

                                    MenuItem diff = new MenuItem("Diff with repository");
                                    diff.setOnAction(new DiffWithDiskEventHandler(item));
                                    scmItemContextMenu.getItems().add(diff);
                                }

                                if(is(itemStatus).oneOf(ScmItem.Status.MODIFIED) ) {
                                    scmItemContextMenu.getItems().add(new SeparatorMenuItem());

                                    MenuItem revert = new MenuItem("Revert ...");
                                    revert.setOnAction(new RevertEventHandler(param, item));
                                    scmItemContextMenu.getItems().add(revert);
                                }

                            }
                    );

                    //scmItemContextMenu.get

                    /*
                    setOnContextMenuRequested(event -> {
                        boolean isConflict = item.getAttribute().getStatus().contains(ScmItemStatus.CONFLICT);
                        conflictResolveUsingMy.setVisible(isConflict);
                        conflictResolveUsingTheir.setVisible(isConflict);
                        conflictResolved.setVisible(isConflict);

                        stageFileMenuItem.setDisable(!isUnstaged(item));
                        unstageFileMenuItem.setDisable(isUnstaged(item));

                        revertMenuItem.setVisible(!isConflict);
                        showDiffMenuItem.setVisible(!isConflict);

                    });*/
                }
            }

        };
    }


    /*workingCopyTableView.setRowFactory(
                tr -> {
                    return new TableRow<ScmItem>() {

                        private String calculateStyle(final ScmItem scmItem) {
                            StringBuilder sb = new StringBuilder();
                            if (scmItem != null) {
                                if (WorkingCopyController.this.searchText.getText() != null
                                        && WorkingCopyController.this.searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                                    if (scmItem.getShortName().toLowerCase().contains(
                                            WorkingCopyController.this.searchText.getText().toLowerCase())) {
                                        //sb.append("-fx-font-weight: bold;");
                                        sb.append("-fx-font-weight: bold; ");
                                        sb.append("-fx-background-color: linear-gradient(#9fbed6 0%, #d0fad0 100%);");
                                    }

                                }

                            }
                            return  sb.toString();

                        }

                        @Override
                        protected void updateItem(ScmItem item, boolean empty) {
                            super.updateItem(item, empty);
                            setStyle(calculateStyle(item));
                            if (!empty) {
                                setContextMenu(scmItemContextMenu);
                                setOnContextMenuRequested(event -> {
                                    boolean isConflict = item.getAttribute().getStatus().contains(ScmItemStatus.CONFLICT);
                                    conflictResolveUsingMy.setVisible(isConflict);
                                    conflictResolveUsingTheir.setVisible(isConflict);
                                    conflictResolved.setVisible(isConflict);

                                    stageFileMenuItem.setDisable(!isUnstaged(item));
                                    unstageFileMenuItem.setDisable(isUnstaged(item));

                                    revertMenuItem.setVisible(!isConflict);
                                    showDiffMenuItem.setVisible(!isConflict);

                                });
                            }
                        }
                    };
                }
        );*/


}
