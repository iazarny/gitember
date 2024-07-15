package com.az.gitember.controller.workingcopy;

import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.az.gitember.service.GitemberUtil.is;

public class WorkingcopyTableRowFactory implements Callback<TableView, TableRow> {

    private final static Logger log = Logger.getLogger(WorkingcopyTableRowFactory.class.getName());


    private ContextMenu scmItemContextMenu = new ContextMenu();

    private Workingcopy workingcopy;
    private String headSha;


    public WorkingcopyTableRowFactory(Workingcopy workingcopy, String headSha) {
        //Just  place holder. Menu without any initial elements will not show even if item will
        // not be added on request menu
        scmItemContextMenu.getItems().add(new MenuItem(""));
        this.workingcopy = workingcopy;
        this.headSha = headSha;
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

                	setOnMouseClicked(event -> {
                		if (event.getClickCount() == 2) {
                            try {
                                final List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                                        item.getShortName(),
                                        null); //TODO current tree only, not all
                                if (!fileRevs.isEmpty()) {
                                    new DiffWithDiskEventHandler(item, fileRevs).handle(event);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                	});

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
                                        revert.setOnAction(new MassRevertEventHandler(selecteItems));
                                        revert.setGraphic(icons.get(MI_REVERT_NAME));
                                        scmItemContextMenu.getItems().add(revert);
                                    }

                                    if (possibleStage) {
                                        MenuItem stage = new MenuItem(MI_STAGE_MULTIPLE_NAME);
                                        stage.setOnAction(new MassStageEventHandler(selecteItems));
                                        stage.setGraphic(icons.get(MI_STAGE_NAME));
                                        scmItemContextMenu.getItems().add(stage);
                                    }

                                    if (possibleUnstage) {
                                        MenuItem unstage = new MenuItem(MI_UNSTAGE_MULTIPLE_NAME);
                                        unstage.setOnAction(new MassResetEventHandler(selecteItems));
                                        unstage.setGraphic(icons.get(MI_UNSTAGE_NAME));
                                        scmItemContextMenu.getItems().add(unstage);
                                    }

                                    if (possibleDelete) {
                                        MenuItem unstage = new MenuItem(MI_DELETE_NAME);
                                        unstage.setOnAction(new MassDeleteEventHandler(selecteItems));
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

        //stage
        if (is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED)) {
            MenuItem stage = new MenuItem(MI_STAGE_NAME);
            stage.setOnAction(new StageEventHandler(param, item));
            stage.setGraphic(icons.get(MI_STAGE_NAME));
            scmItemContextMenu.getItems().add(stage);
        }

        //unstage
        if (is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
            MenuItem unstage = new MenuItem(MI_UNSTAGE_NAME);
            unstage.setOnAction(new StageEventHandler(param, item));
            unstage.setGraphic(icons.get(MI_UNSTAGE_NAME));
            scmItemContextMenu.getItems().add(unstage);
        }


        //diff
        //if (!is(itemStatus).oneOf(ScmItem.Status.ADDED, ScmItem.Status.UNTRACKED, ScmItem.Status.MISSED, ScmItem.Status.REMOVED)) {
        if (is(itemStatus).oneOf(ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
            MenuItem diff = new MenuItem(MI_DIFF_REPO);
            try {
                final List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                        item.getShortName(),
                        null); //TODO current tree only, not all
                diff.setOnAction(e -> new DiffWithDiskEventHandler(item,fileRevs).handle(e));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            diff.setGraphic(icons.get(MI_DIFF_REPO));
            scmItemContextMenu.getItems().add(diff);
        }

        //revert
        if (is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.MISSED)) {
            MenuItem revert = new MenuItem(MI_REVERT_NAME);
            revert.setOnAction(new RevertEventHandler(item));
            revert.setGraphic(icons.get(MI_REVERT_NAME));
            scmItemContextMenu.getItems().add(revert);
        }


        //resolve conflict
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


        //history
        if (is(itemStatus).oneOf(ScmItem.Status.MODIFIED, ScmItem.Status.MISSED)) {
            MenuItem history = new MenuItem(MI_HISTORY);
            history.setOnAction(new ShowHistoryEventHandler(headSha ,item));
            history.setGraphic(icons.get(MI_HISTORY));
            scmItemContextMenu.getItems().add(history);
            scmItemContextMenu.getItems().add(new SeparatorMenuItem());
        }


        //open
        if (!is(itemStatus).oneOf(ScmItem.Status.MISSED, ScmItem.Status.REMOVED)) {
            MenuItem open = new MenuItem(MI_OPEN_NAME);
            open.setOnAction(new OpenFileEventHandler(item, ScmItem.BODY_TYPE.WORK_SPACE));
            open.setGraphic(icons.get(MI_OPEN_NAME));
            scmItemContextMenu.getItems().add(open);
        }


        //delete
        if (!is(itemStatus).oneOf(ScmItem.Status.MISSED, ScmItem.Status.REMOVED)) {
            MenuItem nottracked = new MenuItem(MI_DELETE_NAME);
            nottracked.setOnAction(new MassDeleteEventHandler(Collections.singletonList(item)));
            nottracked.setGraphic(icons.get(MI_DELETE_NAME));
            scmItemContextMenu.getItems().add(nottracked);
        }


    }


    static Map<String, StackedFontIcon> icons = new HashMap<>();
    public static String MI_REVERT_NAME = "Revert ...";
    public static String MI_DIFF_REPO = "Diff with repository";
    public static String MI_HISTORY = "History";
    public static String MI_OPEN_NAME = "Open";
    public static String MI_UNSTAGE_NAME = "Unstage item";
    public static String MI_UNSTAGE_MULTIPLE_NAME = "Unstage items";
    public static String MI_STAGE_NAME = "Stage item";
    public static String MI_STAGE_MULTIPLE_NAME = "Stage items";
    public static String MI_DELETE_NAME = "Physical delete ... ";

    static {
        icons.put(MI_OPEN_NAME, GitemberUtil.create(new FontIcon(FontAwesome.FOLDER_OPEN)));
        icons.put(MI_REVERT_NAME, GitemberUtil.create(new FontIcon(FontAwesome.ROTATE_LEFT)));
        icons.put(MI_DIFF_REPO, GitemberUtil.create(new FontIcon(FontAwesome.EXCHANGE)));
        icons.put(MI_STAGE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.PLUS)));
        icons.put(MI_UNSTAGE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.MINUS)));
        icons.put(MI_DELETE_NAME, GitemberUtil.create(new FontIcon(FontAwesome.TRASH)));
        icons.put(MI_HISTORY, GitemberUtil.create(new FontIcon(FontAwesome.HISTORY)));
    }


}
