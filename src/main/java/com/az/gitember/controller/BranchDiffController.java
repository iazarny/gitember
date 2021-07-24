package com.az.gitember.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import org.eclipse.jgit.diff.DiffEntry;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlled for branch difference.
 */
public class BranchDiffController implements Initializable {

    public TableView<DiffEntry> diffTableView;
    public TableColumn<DiffEntry, StackedFontIcon> actionTableColumn;
    public TableColumn<DiffEntry, String> leftTableColumn;
    public TableColumn<DiffEntry, String> rightTableColumn;

    private final ObservableList<DiffEntry> observableDiffEntry = FXCollections.observableList(new ArrayList<>());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        actionTableColumn.setCellValueFactory(c ->
                new WorkingcopyTableGraphicsValueFactory(c.getValue().getChangeType().name()));

        leftTableColumn.setCellValueFactory(c -> getName(c.getValue().getOldPath()));
        rightTableColumn.setCellValueFactory(c -> getName(c.getValue().getNewPath()));

        diffTableView.setItems(observableDiffEntry);
        diffTableView.setRowFactory(new BranchDiffTableRowFactory());
    }

    public void setData(String leftBranchName, String rightBranchName, List<DiffEntry> diffEntries) {
        leftTableColumn.setGraphic(new Text(leftBranchName));
        rightTableColumn.setGraphic(new Text(rightBranchName));
        observableDiffEntry.addAll(diffEntries);
    }

    private SimpleObjectProperty getName(String fileName) {
        final String name;
        if ("/dev/null".equals(fileName)) {
            name = "";
        } else {
            name = fileName;
        }
        return new SimpleObjectProperty(name);
    }

}
