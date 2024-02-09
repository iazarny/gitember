package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Workingcopy implements Initializable {

    private final static Logger log = Logger.getLogger(Workingcopy.class.getName());

    public TableView workingCopyTableView;
    public TableColumn<ScmItem, ScmItem> selectTableColumn;
    public TableColumn<ScmItem, String> itemTableColumn;
    public TableColumn<ScmItem, ScmItem> itemTableColumnColorStatus;
    public TableColumn<ScmItem, StackedFontIcon> statusTableColumn;
    public TableColumn<ScmItem, String> itemLstChangesName;
    public TableColumn<ScmItem, String> itemLstChangesAuthor;
    public TableColumn<ScmItem, String> itemLstChangesDate;
    public TableColumn<ScmItem, ScmItem> actionTableColumn;
    public Pane spacerPane;
    public TextField searchText;
    public CheckBox checkBoxShowLfs;
    public CheckBox checkBoxShowLastChanges;


    private FilteredList filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        filteredList = new FilteredList(Context.statusList);

        statusTableColumn.setCellValueFactory(
                c -> new WorkingcopyTableGraphicsValueFactory
                        (
                                c.getValue().getAttribute().getStatus(),
                                c.getValue().getAttribute().getSubstatus()
                        )
        );

        itemTableColumnColorStatus.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue()));
        itemTableColumnColorStatus.setCellFactory(new WorkingcopyTableStatusCallback());

        itemTableColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getViewRepresentation()));

        itemLstChangesName.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getChangeNameSafe() ));
        itemLstChangesAuthor.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getChangeAuthorSafe() ));
        itemLstChangesDate.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getChangeDateSafe() ));

        checkBoxShowLastChanges.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue) {
                new StatusUpdateEventHandler(true, workerStateEvent -> {
                    itemLstChangesName.setVisible(newValue);
                    itemLstChangesAuthor.setVisible(newValue);
                    itemLstChangesDate.setVisible(newValue);
                }).handle(null);
            } else {
                itemLstChangesName.setVisible(newValue);
                itemLstChangesAuthor.setVisible(newValue);
                itemLstChangesDate.setVisible(newValue);
            }


        });

        checkBoxShowLfs.setVisible(Context.lfsRepo.getValue());
        checkBoxShowLfs.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    actionTableColumn.setVisible(newValue);

                    filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(newValue));

                }
        );
        filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(false));



        if (Context.lastChanges.get()) {
            checkBoxShowLastChanges.setSelected(true);
            itemLstChangesName.setVisible(true);
            itemLstChangesAuthor.setVisible(true);
            itemLstChangesDate.setVisible(true);

        }

        checkBoxShowLfs.selectedProperty().setValue(Context.showLfsFiles.get());

        Bindings.bindBidirectional(Context.lastChanges, checkBoxShowLastChanges.selectedProperty());
        Bindings.bindBidirectional(Context.showLfsFiles, checkBoxShowLfs.selectedProperty());

        selectTableColumn.setCellValueFactory( c -> new SimpleObjectProperty(c.getValue()));
        actionTableColumn.setCellValueFactory( c -> new SimpleObjectProperty(c.getValue()));

        selectTableColumn.setCellFactory(param -> new WorkingcopyTableStageTableCell());
        actionTableColumn.setCellFactory(param -> new WorkingcopyTableActionTableCell());


        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    //select row if search text is found
                    Iterator<ScmItem> iter = filteredList.iterator();
                    int firstFoundIdx = 0;
                    while ((iter.hasNext())) {
                        ScmItem item = iter.next();
                        if (searchText.getText() != null
                                && searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                            if (item.getShortName().toLowerCase().contains(
                                    searchText.getText().toLowerCase())) {
                                //workingCopyTableView.getSelectionModel().clearAndSelect(firstFoundIdx);
                                //workingCopyTableView.getSelectionModel().focus(firstFoundIdx);
                                workingCopyTableView.scrollTo(firstFoundIdx);
                                break;
                            }
                        }
                        firstFoundIdx++;
                    }
                    workingCopyTableView.refresh();

                    if (oldValue != null && newValue != null
                            && newValue.length() > oldValue.length()
                            && newValue.contains(oldValue)) {
                        Settings settings = Context.settingsProperty.getValue();
                        settings.getSearchTerms().remove(oldValue);
                        settings.getSearchTerms().add(newValue);
                    }
                }
        );

        new ChangeListenerHistoryHint(searchText, Context.settingsProperty.getValue().getSearchTerms());

        workingCopyTableView.setRowFactory(new WorkingcopyTableRowFactory(this));

        workingCopyTableView.setItems(filteredList);

        workingCopyTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        HBox.setHgrow(spacerPane, Priority.ALWAYS);

    }


    private void stageUnstageItem(ScmItem item) {

        new StageEventHandler(workingCopyTableView, item).handle(null);

    }

    public void stageAllEventHandler(ActionEvent actionEvent) {
        Context.statusList.stream()
                .filter(i -> (i.staged().equals(0)))
                .forEach(i -> stageUnstageItem(i));
    }

    public void unstageAllEventHandler(ActionEvent actionEvent) {
        Context.statusList.stream()
                .filter(i -> (i.staged().equals(1)))
                .forEach(i -> stageUnstageItem(i));
    }





    public void stashEventHandler(ActionEvent actionEvent) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);
        alert.setTitle("Question");
        alert.setContentText("Would you like to stash changes ?");
        alert.initOwner(App.getScene().getWindow());

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Context.getGitRepoService().stash();
                    Context.updateStash();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Cannot stash ", e.getMessage());
                }
                new StatusUpdateEventHandler(true).handle(null);
            }
        });


    }

    public void mergeEventHandler(ActionEvent actionEvent) {
        new MergeBranchEventHandler(null).handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void rebaseEventHandler(ActionEvent actionEvent) {
        new RebaseBranchEventHandler(null).handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void checkoutEventHandler(ActionEvent actionEvent) {
        new CheckoutEventHandler().handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void refreshEventHandler(ActionEvent actionEvent) {
        Context.updateAll();
        Context.updateWorkingBranch();
    }

    public void commitEventHandler(ActionEvent actionEvent) {

        final Project proj = Context.getCurrentProject();
        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String cfgCommitName = gitConfig.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME);
        final String cfgCommitEmail = gitConfig.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL);
        String commitName = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(proj.getUserCommitName(), cfgCommitName), proj.getUserName());
        String commitEmail = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(proj.getUserCommitEmail(), cfgCommitEmail), "");

        CommitDialog dialog = new CommitDialog(
                "",
                commitName,
                commitEmail,
                false,
                Collections.EMPTY_LIST

        );
        dialog.showAndWait().ifPresent(r -> {
            if (!commitEmail.equals(dialog.getUserName()) && !commitEmail.equalsIgnoreCase(dialog.getUserEmail())) {
                proj.setUserCommitName(dialog.getUserName());
                proj.setUserCommitEmail(dialog.getUserEmail());
                Context.saveSettings();
            }
            try {
                Context.settingsProperty.getValue().getCommitMsg().add(r);
                Context.getGitRepoService().commit(r, dialog.getUserName(), dialog.getUserEmail());
                new StatusUpdateEventHandler(true).handle(null);
                Context.updateBranches();
            } catch (GitAPIException e) {
                Context.getMain().showResult("Commit error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
        Context.updateWorkingBranch();
    }

}
