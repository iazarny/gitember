package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
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


    private GitRepositoryService repositoryService;
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

            System.out.println("\n\n old " + oldSelection + " new " + newSelection);

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


            selectedItems.stream().forEach(
                    o -> {
                        System.out.println(o.getShortMessage());
                    }
            );
        });


    }

    public void setRepositoryService(GitRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
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
                        repositoryService.getFileHistory(treeName, fileName)
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
            final String oldFile = repositoryService.saveFile(treeName, oldRevision.getRevisionFullName(), fileName);
            final String newFile = repositoryService.saveFile(treeName, newRevision.getRevisionFullName(), fileName);
            final String diffFile = repositoryService.saveDiff(treeName, oldRevision.getRevisionFullName(), newRevision.getRevisionFullName(), fileName);
            final DiffViewController fileViewController = new DiffViewController();
            fileViewController.openFile(
                    oldFile, oldRevision.getRevisionFullName(),
                    newFile, newRevision.getRevisionFullName(),
                    diffFile);

        }



/*
* public void openDiffWithPreviosVersionMenuItemClickHandler(ActionEvent actionEvent) {

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        try {
            final String parentREvision = plotCommit.getParents().get(0); //todo determinate parent right
            final String oldFile = repositoryService.saveFile(treeName, parentREvision, fileName);
            final String newFile = repositoryService.saveFile(treeName, plotCommit.getRevisionFullName(), fileName);
            final String diffFile = repositoryService.saveDiff(treeName, plotCommit.getRevisionFullName(), fileName);
            final DiffViewController fileViewController = new DiffViewController();
            fileViewController.openFile(
                    oldFile, parentREvision,
                    newFile, plotCommit.getRevisionFullName(),
                    diffFile);
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }*/

    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        String revisionFullName = ((ScmRevisionInformation) historyTableView.getSelectionModel().getSelectedItem())
                .getRevisionFullName();
        try {
            final FileViewController fileViewController = new FileViewController();
            fileViewController.openFile(
                    repositoryService.saveFile(treeName, revisionFullName, fileName),
                    fileName);
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }
}
