package com.az.gitember.controller.workingcopy;

import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.*;
import com.az.gitember.dialog.StashDialog;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.net.URL;
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
    //public CheckBox checkBoxShowLastChanges;
    private FilteredList filteredList;

    private String headSha = "HEAD";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            headSha = Context.getGitRepoService().getHead().getSha();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

        /*checkBoxShowLastChanges.selectedProperty().addListener((observable, oldValue, newValue) -> {

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


        });*/

        checkBoxShowLfs.setVisible(Context.lfsRepo.getValue());
        checkBoxShowLfs.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    actionTableColumn.setVisible(newValue);

                    filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(newValue));

                }
        );
        filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(false));



        if (Context.lastChanges.get()) {
           // checkBoxShowLastChanges.setSelected(true);
            itemLstChangesName.setVisible(true);
            itemLstChangesAuthor.setVisible(true);
            itemLstChangesDate.setVisible(true);

        }

        checkBoxShowLfs.selectedProperty().setValue(Context.showLfsFiles.get());

        //Bindings.bindBidirectional(Context.lastChanges, checkBoxShowLastChanges.selectedProperty());
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



        workingCopyTableView.setRowFactory(new WorkingcopyTableRowFactory(this, headSha));

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

        StashDialog stashDialog = new StashDialog();
        stashDialog
                .showAndWait()
                .ifPresent(
                        dialogRes -> {
                            try {
                                Context.getGitRepoService().stash(dialogRes.getCommitMessage());
                                Context.updateStash();
                            } catch (IOException e) {
                                log.log(Level.WARNING, "Cannot stash", e.getMessage());
                                Context.getMain().showResult("Cannot stash",
                                        ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);

                            }
                            new StatusUpdateEventHandler(true).handle(null);
                        }
                );

    }

    public void refreshEventHandler(ActionEvent actionEvent) {
        Context.updateAll();
        Context.updateWorkingBranch();
    }

    public void createDiffEventHandler(ActionEvent actionEvent) {
        String diff = Context.getGitRepoService().createDiff();
        new OpenFileEventHandler(diff).handle(actionEvent);
    }

}
