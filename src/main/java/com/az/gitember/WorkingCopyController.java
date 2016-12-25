package com.az.gitember;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmItemStatus;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 23.12.2016.
 */
public class WorkingCopyController  implements Initializable {


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
                c -> StringConstant.valueOf(c.getValue().getAttribute().getStatus().stream().collect(Collectors.joining(" "))  )
        );

        selectTableColumn.setCellValueFactory(
               c -> {
                   boolean staged =
                           c.getValue().getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)
                           || c.getValue().getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED);
                   return new ReadOnlyBooleanWrapper(!staged);
               }
        );

        selectTableColumn.setCellFactory(p -> new CheckBoxTableCell<ScmItem, Boolean>());

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
                ScmItem item = (ScmItem)workingCopyTableView.getSelectionModel().getSelectedItem();
                if(item.getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)
                        || item.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED) ){
                    //System.out.println("remove " + item.getShortName());
                    //item.getAttribute().getStatus().remove(ScmItemStatus.ADDED);
                    //System.out.println("add " + item.getShortName());

                    MainApp.getRepositoryService().addFileToCommitStage(item.getShortName());

                    item.getAttribute().getStatus().remove(ScmItemStatus.MODIFIED);
                    item.getAttribute().getStatus().remove(ScmItemStatus.UNTRACKED);


                }
                workingCopyTableView.refresh();
            }
        }
    }
}
