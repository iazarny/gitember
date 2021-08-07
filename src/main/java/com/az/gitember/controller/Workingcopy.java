package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.Project;
import com.az.gitember.data.ScmFilterPredicateLfsOnOff;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
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
    public TableColumn<ScmItem, ScmItem> actionTableColumn;
    public Pane spacerPane;
    public TextField searchText;
    public CheckBox checkBoxShowLfs;

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

        selectTableColumn.setCellValueFactory( c -> new SimpleObjectProperty(c.getValue()));
        actionTableColumn.setCellValueFactory( c -> new SimpleObjectProperty(c.getValue()));

        selectTableColumn.setCellFactory(param -> new WorkingcopyTableStageTableCell());
        actionTableColumn.setCellFactory(param -> new WorkingcopyTableActionTableCell());


        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    workingCopyTableView.refresh();
                    if (oldValue != null && newValue != null && newValue.length() > oldValue.length() && newValue.contains(oldValue)) {
                        Settings settings = Context.settingsProperty.getValue();
                        settings.getSearchTerms().remove(oldValue);
                        settings.getSearchTerms().add(newValue);
                    }
                }
        );

        new ChangeListenerHistoryHint(searchText, Context.settingsProperty.getValue().getSearchTerms());

        workingCopyTableView.setRowFactory(new WorkingcopyTableRowFactory(this));

        workingCopyTableView.setItems(filteredList);

        HBox.setHgrow(spacerPane, Priority.ALWAYS);

        if (Context.getGitRepoService().isLfsRepo()) {
            checkBoxShowLfs.setVisible(true);
            checkBoxShowLfs.selectedProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        actionTableColumn.setVisible(newValue);

                        filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(newValue));

                    }
            );
            filteredList.setPredicate(new ScmFilterPredicateLfsOnOff(false));
        }

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
                .forEach(i -> stageUnstageItem((ScmItem) i));
    }


    public void mergeEventHandler(ActionEvent actionEvent) {

        new MergeBranchEventHandler(null).handle(actionEvent);

    }

    public void rebaseEventHandler(ActionEvent actionEvent) {

        new RebaseBranchEventHandler(null).handle(actionEvent);

    }

    public void checkoutEventHandler(ActionEvent actionEvent) {

        new CheckoutEventHandler().handle(actionEvent);

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


    public void refreshEventHandler(ActionEvent actionEvent) {
        Context.updateAll();
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
            } catch (GitAPIException e) {
                Context.getMain().showResult("Commit error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }


    //-------------------------------------------------------


}
