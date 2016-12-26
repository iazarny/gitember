package com.az.gitember;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmItemStatus;
import com.az.gitember.ui.TextAreaInputDialog;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 23.12.2016.
 */
public class WorkingCopyController implements Initializable {


    public TableView workingCopyTableView;
    public TableColumn<ScmItem, String> statusTableColumn;
    public TableColumn<ScmItem, Boolean> selectTableColumn;
    public TableColumn<ScmItem, String> itemTableColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        itemTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getShortName())
        );

        statusTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getAttribute().getStatus().stream().collect(Collectors.joining(" ")))
        );

        selectTableColumn.setCellValueFactory(
                c -> new ReadOnlyBooleanWrapper(!isUnstaged(c.getValue()))
        );

        selectTableColumn.setCellFactory(p -> new CheckBoxTableCell<ScmItem, Boolean>());

    }

    private boolean isUnstaged(ScmItem scmItem) {
        return scmItem.getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)
                || scmItem.getAttribute().getStatus().contains(ScmItemStatus.MISSED)
                || scmItem.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED);
    }

    public void open() throws Exception {
        workingCopyTableView.setItems(
                FXCollections.observableArrayList(
                        MainApp.getRepositoryService().getStatuses()));
    }


    public void addItemToCommitEventHandler(Event event) throws Exception {
        if (event.getTarget() instanceof CheckBoxTableCell) {
            CheckBoxTableCell cell = (CheckBoxTableCell) event.getTarget();
            if (cell.getTableColumn() == this.selectTableColumn) {
                ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
                if (isUnstaged(item)) {

                    if (item.getAttribute().getStatus().contains(ScmItemStatus.MISSED)) {

                        MainApp.getRepositoryService().removeMissedFile(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.MISSED);
                        item.getAttribute().getStatus().add(ScmItemStatus.REMOVED);
                    } else if (item.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED)) {
                        MainApp.getRepositoryService().addFileToCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.UNTRACKED);
                        item.getAttribute().getStatus().add(ScmItemStatus.ADDED);
                        item.getAttribute().getStatus().add(ScmItemStatus.CHANGED);
                        item.getAttribute().getStatus().add(ScmItemStatus.UNCOMMITED);
                    } else {
                        MainApp.getRepositoryService().addFileToCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.MODIFIED);
                    }


                }
                workingCopyTableView.refresh();
            }
            //TODO V2 unstage changes
        }
    }

    public void commitBtnHandler(ActionEvent actionEvent) throws Exception {
        TextAreaInputDialog dialog = new TextAreaInputDialog(
                "TODO history of commit messasge",
                MainApp.getRepositoryService().getUserName(),
                MainApp.getRepositoryService().getUserEmail()
        );
        dialog.setTitle("Commit message");
        dialog.setHeaderText("Provide commit message");
        dialog.setContentText("Message:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            MainApp.getRepositoryService().setUserEmail(dialog.getUserEmail());
            MainApp.getRepositoryService().setUserName(dialog.getUserName());
            MainApp.getRepositoryService().commit(result.get());
            open();
        }
    }

    public void refreshBtnHandler(ActionEvent actionEvent) throws Exception {
        open();
    }
}
