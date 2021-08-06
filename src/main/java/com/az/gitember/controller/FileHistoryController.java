package com.az.gitember.controller;

import com.az.gitember.controller.handlers.DiffEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class FileHistoryController implements Initializable {

    public TableView historyTableView;
    public TableColumn<ScmRevisionInformation, String> revisionTableColumn;
    public TableColumn<ScmRevisionInformation, StackedFontIcon> actionTableColumn;
    public TableColumn<ScmRevisionInformation, String> authorTableColumn;
    public TableColumn<ScmRevisionInformation, String> dateTableColumn;
    public TableColumn<ScmRevisionInformation, String> messageTableColumn;
    public ContextMenu scmItemContextMenu;
    public MenuItem showDiffMenuItem;
    public MenuItem openFileMenuItem;

    private final ObservableList<ScmRevisionInformation> singleSiteHistory = FXCollections.observableList(new ArrayList<>());
    private String fileName ;
    private String treeName ;


    private LinkedList<ScmRevisionInformation> selectedItems = new LinkedList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        revisionTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue().getRevisionFullName()));
        authorTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue().getAuthorName()));
        messageTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(c.getValue().getFullMessage()));

        dateTableColumn.setCellValueFactory(c -> new SimpleObjectProperty(GitemberUtil.formatDate(c.getValue().getDate())));

        actionTableColumn.setCellValueFactory(c ->
                new WorkingcopyTableGraphicsValueFactory(c.getValue().getAffectedItems().get(0).getSecond().getStatus(), null));

        historyTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (historyTableView.getSelectionModel().getSelectedItems().size() == 1) {
                    // click on some other item, not additional
                    selectedItems.clear();
                }
                selectedItems.add((ScmRevisionInformation) newSelection);
                while (selectedItems.size() > 2) {
                    selectedItems.poll();
                }
            } else if (newSelection == null && oldSelection != null) { //deselection
                selectedItems.remove(oldSelection);
            }
            showDiffMenuItem.setDisable(selectedItems.size() < 2);
            openFileMenuItem.setDisable(selectedItems.size() != 1);

        });

        historyTableView.setItems(singleSiteHistory);
        fileName = Context.fileHistoryName.getValue();
        treeName = Context.fileHistoryTree.getValue();
        try {
            singleSiteHistory.addAll(
                    Context.getGitRepoService().getFileHistory(
                            fileName,
                            treeName)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void showDiffMenuItemClickHandler(ActionEvent actionEvent) throws Exception {

        if (selectedItems.size() == 2) {

            final ScmRevisionInformation oldRevision;
            final ScmRevisionInformation newRevision;

            if (selectedItems.get(0).getDate().getTime() < selectedItems.get(1).getDate().getTime()) {
                oldRevision = selectedItems.get(0);
                newRevision = selectedItems.get(1);
            } else {
                oldRevision = selectedItems.get(1);
                newRevision = selectedItems.get(0);
            }

            final String oldRevisionName = oldRevision.getRevisionFullName();
            final String newRevisionName = newRevision.getRevisionFullName();

            final ScmRevisionInformation revInfo = (ScmRevisionInformation) this.historyTableView.getSelectionModel().getSelectedItem();
            final ScmItem scmItem = revInfo.getAffectedItems().get(0);
            new DiffEventHandler(scmItem, oldRevisionName, newRevisionName).handle(actionEvent);
        }

    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {

        final ScmRevisionInformation revInfo = (ScmRevisionInformation) this.historyTableView.getSelectionModel().getSelectedItem();
        final ScmItem scmItem = revInfo.getAffectedItems().get(0);
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERION).handle(actionEvent);

    }

}
