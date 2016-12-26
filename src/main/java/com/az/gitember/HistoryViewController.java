package com.az.gitember;

import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class HistoryViewController implements Initializable {

    @FXML
    public TableView historyTableView;

    @FXML
    public TableColumn<ScmRevisionInformation, String> revisionTableColumn;

    @FXML
    public TableColumn<ScmRevisionInformation, FontIcon> actionTableColumn;

    @FXML
    public TableColumn<ScmRevisionInformation, String> authorTableColumn;

    @FXML
    public TableColumn<ScmRevisionInformation, String> dateTableColumn;

    @FXML
    public TableColumn<ScmRevisionInformation, String> messageTableColumn;

    @FXML
    public ContextMenu scmItemContextMenu;

    @FXML
    public MenuItem showDiffMenuItem;

    @FXML
    public MenuItem openFileMenuItem;


    private String fileName;
    private String treeName;
    private LinkedList<ScmRevisionInformation> selectedItems = new LinkedList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        revisionTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getRevisionFullName()));
        authorTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getAuthorName()));
        messageTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getFullMessage()));
        dateTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getDate().toString()));
        actionTableColumn.setCellValueFactory(c -> new ActionCellValueFactory(null, c));
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


    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public void openHistory() throws Exception {
        historyTableView.setItems(
                FXCollections.observableArrayList(
                        MainApp.getRepositoryService().getFileHistory(treeName, fileName)
                )
        );
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

            //todo copy past from commit controller # openDiffWithLatestVersionMenuItemClickHandler
            final String oldFile = MainApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);
            final String newFile = MainApp.getRepositoryService().saveFile(treeName, newRevisionName, fileName);
            final String diffFile = MainApp.getRepositoryService().saveDiff(treeName,oldRevisionName , newRevisionName, fileName);
            final DiffViewController fileViewController = new DiffViewController();
            fileViewController.openFile(
                    new File(fileName).getName(),
                    oldFile, oldRevisionName,
                    newFile, newRevisionName,
                    diffFile);

        }

    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        String revisionFullName = ((ScmRevisionInformation) historyTableView.getSelectionModel().getSelectedItem())
                .getRevisionFullName();
        try {
            final FileViewController fileViewController = new FileViewController();
            fileViewController.openFile(
                    MainApp.getRepositoryService().saveFile(treeName, revisionFullName, fileName),
                    fileName);
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }
}
