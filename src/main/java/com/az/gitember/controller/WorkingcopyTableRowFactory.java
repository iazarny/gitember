package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.DiffWithDiskEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.controller.handlers.RevertEventHandler;
import com.az.gitember.controller.handlers.StageEventHandler;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.eclipse.jgit.api.CheckoutCommand;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public TableRow<ScmItem> call(TableView param) {
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
                return "";
            }

            @Override
            protected void updateItem(ScmItem item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(calculateStyle(item));
                if (!empty) {

                    setContextMenu(scmItemContextMenu);
                    setOnContextMenuRequested(
                            evt -> {

                                if (getTableView().getSelectionModel().getSelectedItems().size() > 1) {
                                    //multiple items are selected
                                    /*Allowed operations are -  delete , revert , stage, unstage.
                                     * By default all  operations are disable, however operation will be enabled for
                                     * group if it allowed for even single item in the group  and applied to particular items,
                                     * ie delete will be not called for missed files */
                                    boolean possibleDelete = false;
                                    boolean possibleRevert = false;
                                    boolean possibleStage = false;
                                    boolean possibleUnstage = false;
                                    final List<ScmItem> selecteItems = getTableView().getSelectionModel().getSelectedItems();
                                    for (ScmItem selectedItem : selecteItems) {
                                        final String seletectItemStatus = selectedItem.getAttribute().getStatus();
                                        if (is(seletectItemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED)) {
                                            possibleStage = true;
                                        }
                                        if (is(seletectItemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
                                            possibleUnstage = true;
                                        }
                                        if (is(seletectItemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.MISSED)) {
                                            possibleRevert = true;
                                        }
                                        if (is(seletectItemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED)) {
                                            possibleDelete = true;
                                        }
                                    }

                                    scmItemContextMenu.getItems().clear();

                                    if (possibleRevert) {
                                        MenuItem revert = new MenuItem(MI_REVERT_NAME);
                                        revert.setOnAction(e -> {

                                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                            alert.setTitle("Please confirm");
                                            alert.setHeaderText("Revert changes");
                                            alert.setContentText("Do you really want to revert multiple items changes ?");
                                            alert.initOwner(App.getScene().getWindow());

                                            alert.showAndWait().ifPresent(
                                                    r -> {

                                                        if (r == ButtonType.OK) {
                                                            for (ScmItem processItem : selecteItems) {
                                                                new RevertEventHandler( processItem).handle(null);
                                                            }
                                                        }
                                                    }
                                            );


                                        }  );
                                        revert.setGraphic(icons.get(MI_REVERT_NAME));
                                        scmItemContextMenu.getItems().add(revert);
                                    }

                                    if (possibleStage) {
                                        MenuItem stage = new MenuItem(MI_STAGE_MULTIPLE_NAME);
                                        stage.setOnAction(e -> {
                                            for (ScmItem processItem : selecteItems) {
                                                if (is(processItem.getAttribute().getStatus()).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED)) {
                                                    new StageEventHandler(getTableView(), processItem).handle(null);
                                                }
                                            }
                                        });
                                        stage.setGraphic(icons.get(MI_STAGE_NAME));
                                        scmItemContextMenu.getItems().add(stage);
                                    }

                                    if (possibleUnstage) {
                                        MenuItem unstage = new MenuItem(MI_UNSTAGE_MULTIPLE_NAME);
                                        unstage.setOnAction(e -> {
                                            for (ScmItem processItem : selecteItems) {
                                                if (is(processItem.getAttribute().getStatus()).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
                                                    new StageEventHandler(getTableView(), processItem).handle(null);
                                                }
                                            }
                                        });
                                        unstage.setGraphic(icons.get(MI_UNSTAGE_NAME));
                                        scmItemContextMenu.getItems().add(unstage);
                                    }

                                    if (possibleDelete) {
                                        MenuItem unstage = new MenuItem(MI_DELETE_NAME);
                                        unstage.setOnAction(e -> {
                                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                            alert.setTitle("Please confirm");
                                            alert.setHeaderText("Delete files");
                                            alert.setContentText(String.format("Do you really want to delete %d items ?", selecteItems.size()));
                                            alert.initOwner(App.getScene().getWindow());

                                            alert.showAndWait().ifPresent(
                                                    r -> {

                                                        if (r == ButtonType.OK) {
                                                            for (ScmItem processItem : selecteItems) {
                                                                if (is(processItem.getAttribute().getStatus()).oneOf(ScmItem.Status.ADDED, ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED)) {
                                                                    //new RevertEventHandler( processItem).handle(null);
                                                                }

                                                            }
                                                        }
                                                    }
                                            );
                                        });
                                        unstage.setGraphic(icons.get(MI_DELETE_NAME));
                                        scmItemContextMenu.getItems().add(unstage);

                                    }


                                } else if (getTableView().getSelectionModel().getSelectedItems().size() == 1) {
                                    // single item
                                    fillSibleItemMenu(item, param);
                                }

                            }
                    );

                }
            }

        };
    }

    private void fillSibleItemMenu(ScmItem item, TableView param) {
        final String itemStatus = item.getAttribute().getStatus();
        scmItemContextMenu.getItems().clear();

        if (is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED)) {
            MenuItem stage = new MenuItem(MI_STAGE_NAME);

            stage.setOnAction(new StageEventHandler(param, item));
/*
            stage.setOnAction(e -> {
                new StageEventHandler(param, item).handle(null);
            });
*/
            stage.setGraphic(icons.get(MI_STAGE_NAME));
            scmItemContextMenu.getItems().add(stage);
        }

        if (is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
            MenuItem unstage = new MenuItem(MI_UNSTAGE_NAME);
            unstage.setOnAction(new StageEventHandler(param, item));
            unstage.setGraphic(icons.get(MI_UNSTAGE_NAME));
            scmItemContextMenu.getItems().add(unstage);
        }

        if (is(itemStatus).oneOf(ScmItem.Status.CONFLICT)) {
            Menu resolve = new Menu("Resolve conflict");

            MenuItem markResolved = new MenuItem("Mark resolved");
            markResolved.setOnAction(new StageEventHandler(param, item));
            resolve.getItems().add(markResolved);

            MenuItem resolveUsingMine = new MenuItem("Using mine");
            resolveUsingMine.setOnAction(a -> {
                try {
                    Context.getGitRepoService().checkoutFile(item.getShortName(), CheckoutCommand.Stage.OURS);
                    new StageEventHandler(param, item).handle(null);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot resolve conflict using my changes {0}. {1}", new String[]{item.getShortName(), e.getMessage()});
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
                    log.log(Level.SEVERE, "Cannot resolve conflict using theirs changes {0}. {1}", new String[]{item.getShortName(), e.getMessage()});
                }
            });
            resolve.getItems().add(resolveUsingTheir);

            scmItemContextMenu.getItems().add(resolve);
        }

        scmItemContextMenu.getItems().add(new SeparatorMenuItem());

        if (!is(itemStatus).oneOf(ScmItem.Status.MISSED, ScmItem.Status.REMOVED)) {
            MenuItem open = new MenuItem(MI_OPEN_NAME);
            open.setOnAction(new OpenFileEventHandler(item, ScmItem.BODY_TYPE.WORK_SPACE));
            open.setGraphic(icons.get(MI_OPEN_NAME));
            scmItemContextMenu.getItems().add(open);
        }

        if (!is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED, ScmItem.Status.REMOVED)) {
            //MenuItem history = new MenuItem("History");  TODO
            //scmItemContextMenu.getItems().add(history);

            MenuItem diff = new MenuItem(MI_DIFF_REPO);
            diff.setOnAction(new DiffWithDiskEventHandler(item));
            diff.setGraphic(icons.get(MI_DIFF_REPO));
            scmItemContextMenu.getItems().add(diff);
        }

        if (is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.MISSED)) {
            MenuItem revert = new MenuItem(MI_REVERT_NAME);
            revert.setOnAction(new RevertEventHandler(item));
            revert.setGraphic(icons.get(MI_REVERT_NAME));
            scmItemContextMenu.getItems().add(revert);
        }
    }


    static Map<String, StackedFontIcon> icons = new HashMap<>();
    public static String MI_REVERT_NAME = "Revert ...";
    public static String MI_DIFF_REPO = "Diff with repository";
    public static String MI_OPEN_NAME = "Open";
    public static String MI_UNSTAGE_NAME = "Unstage item";
    public static String MI_UNSTAGE_MULTIPLE_NAME = "Unstage items";
    public static String MI_STAGE_NAME = "Stage item";
    public static String MI_STAGE_MULTIPLE_NAME = "Stage items";
    public static String MI_DELETE_NAME = "Phisical delete ... ";

    static {
        icons.put(MI_OPEN_NAME, GitemberUtil.create(new FontIcon(FontAwesome.FOLDER_OPEN)));
        icons.put(MI_REVERT_NAME, GitemberUtil.create(new FontIcon(FontAwesome.ROTATE_LEFT)));
        icons.put(MI_DIFF_REPO, GitemberUtil.create(new FontIcon(FontAwesome.EXCHANGE)));
        icons.put(MI_STAGE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.ARROW_CIRCLE_UP)));
        icons.put(MI_UNSTAGE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.ARROW_CIRCLE_DOWN)));
        icons.put(MI_DELETE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.REMOVE)));
    }


}
