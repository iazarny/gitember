package com.az.gitember.controller.stash;

import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.controller.workingcopy.WorkingcopyTableGraphicsValueFactory;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class StashController implements Initializable {

    private final static Logger log = Logger.getLogger(StashController.class.getName());

    public TableView workingCopyTableView;
    public TableColumn<ScmItem, String> itemTableColumn;
    public TableColumn<ScmItem, StackedFontIcon> statusTableColumn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        statusTableColumn.setCellValueFactory(
                c -> new WorkingcopyTableGraphicsValueFactory(c.getValue().getAttribute().getStatus(), null)
        );
        itemTableColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getViewRepresentation()));
        workingCopyTableView.setItems(Context.stashItemsList);
    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) this.workingCopyTableView.getSelectionModel().getSelectedItem();
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(actionEvent);
    }
}
