package com.az.gitember;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmItemStatus;
import com.az.gitember.ui.CommitDialog;
import com.az.gitember.ui.StatusCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Igor_Azarny on 23.12.2016.
 */
public class WorkingCopyController implements Initializable {


    public TableView workingCopyTableView;
    public TableColumn<ScmItem, FontIcon> statusTableColumn;
    public TableColumn<ScmItem, Boolean> selectTableColumn;
    public TableColumn<ScmItem, String> itemTableColumn;
    public Button stashBtn;
    public Button commitBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        itemTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getShortName())
        );

        statusTableColumn.setCellValueFactory(
                c -> new StatusCellValueFactory(c.getValue().getAttribute().getStatus())
        ); //TODO add tooltip

        selectTableColumn.setCellValueFactory(
                c -> new ReadOnlyBooleanWrapper(!isUnstaged(c.getValue()))
        );

        selectTableColumn.setCellFactory(p -> new CheckBoxTableCell<ScmItem, Boolean>());

        workingCopyTableView.setRowFactory(
                tr -> {
                    return new TableRow<ScmItem>() {
                        @Override
                        protected void updateItem(ScmItem item, boolean empty) {
                            super.updateItem(item, empty);
                            super.updateItem(item, empty) ;
                            if (item == null) {
                                setStyle("");
                            } else if (item.getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)) {
                                //todo styles
                            } else if (item.getAttribute().getStatus().contains(ScmItemStatus.MISSED)) {
                            } else if (item.getAttribute().getStatus().contains(ScmItemStatus.ADDED)) {
                            } else if (item.getAttribute().getStatus().contains(ScmItemStatus.REMOVED)) {
                            } else if (item.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED)) {

                            }
                        }
                    };
                }
        );

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
        CommitDialog dialog = new CommitDialog(
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

    public void stashBtnHandler(ActionEvent actionEvent) throws Exception {
        MainApp.getRepositoryService().stash();
        open();

    }
}
