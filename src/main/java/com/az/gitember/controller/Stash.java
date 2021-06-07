package com.az.gitember.controller;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Stash implements Initializable {

    private final static Logger log = Logger.getLogger(Stash.class.getName());

    public TableView workingCopyTableView;
    public TableColumn<ScmItem, String> itemTableColumn;
    public TableColumn<ScmItem, FontIcon> statusTableColumn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        statusTableColumn.setCellValueFactory(
                c -> new WorkingcopyTableGraphicsValueFactory(c.getValue().getAttribute().getStatus())
        );
        itemTableColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getViewRepresentation()));
        workingCopyTableView.setItems(Context.stashItemsList);
    }
}
