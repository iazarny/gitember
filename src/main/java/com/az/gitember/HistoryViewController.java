package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.InputStream;
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
                        GitemberApp.getRepositoryService().getFileHistory(treeName, fileName)
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
            final String oldFile = GitemberApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);
            final String newFile = GitemberApp.getRepositoryService().saveFile(treeName, newRevisionName, fileName);
            final String diffFile = GitemberApp.getRepositoryService().saveDiff(treeName,oldRevisionName , newRevisionName, fileName);
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
                    GitemberApp.getRepositoryService().saveFile(treeName, revisionFullName, fileName),
                    fileName);
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }

    public static Parent openHistoryWindow(final String fileName, final String treeName) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = HistoryViewController.class.getResource("/fxml/HistoryViewPane.fxml").openStream()) {
            final Parent view = fxmlLoader.load(is);
            final HistoryViewController historyViewController = fxmlLoader.getController();
            historyViewController.setFileName(fileName);
            historyViewController.setTreeName(treeName);
            historyViewController.openHistory();

            final Scene scene = new Scene(view, 1024, 768);
            final Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(fileName);
            stage.getIcons().add(new Image(HistoryViewController.class.getClass().getResourceAsStream(Const.ICON)));
            stage.show();


            return view;


        }
    }
}
