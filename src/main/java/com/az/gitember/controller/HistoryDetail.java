package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.DiffEventHandler;
import com.az.gitember.controller.handlers.DiffWithDiskEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.data.CommitInfo;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HistoryDetail implements Initializable {

    private final static Logger log = Logger.getLogger(HistoryDetail.class.getName());

    @FXML
    public Menu stashMenu;
    public MenuItem openDiffMenuItem;
    public MenuItem diffWithPrevVersionMenuItem;
    public MenuItem diffWithCurrentVersionMenuItem;
    public MenuItem diffWithDiskVersionMenuItem;

    @FXML
    private TextField msgLbl;

    @FXML
    private TextField authorLbl;

    @FXML
    private TextField emailLbl;

    @FXML
    private TextField dateLbl;

    @FXML
    private TextField shaLbl;

    @FXML
    private TextField refsLbl;

    @FXML
    private TextField parentLbl;

    @FXML
    private TableView changedFilesListView;

    @FXML
    private TableColumn<ScmItem, StackedFontIcon> actionTableColumn;

    @FXML
    private TableColumn<ScmItem, String> fileTableColumn;

    @FXML
    private ContextMenu scmItemContextMenu;


    @Override
    public void initialize(URL location, ResourceBundle resources) {



        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();

        msgLbl.setText(scmInfo.getFullMessage().replace("\n", ""));
        authorLbl.setText(scmInfo.getAuthorName());
        emailLbl.setText(scmInfo.getAuthorEmail());
        dateLbl.setText(GitemberUtil.formatDate(scmInfo.getDate()));
        refsLbl.setText(
                scmInfo.getRef().stream().collect(Collectors.joining(", ")));
        parentLbl.setText(
                scmInfo.getParents().stream().collect(Collectors.joining(", "))
        );
        shaLbl.setText(scmInfo.getRevisionFullName());

        changedFilesListView.setItems(
                FXCollections.observableList(scmInfo.getAffectedItems())
        );


        Context.searchValue.addListener(
                (observable, oldValue, newValue) -> {
                    changedFilesListView.refresh();
                }
        );
        changedFilesListView.setRowFactory( tr -> {

            return new TableRow<ScmItem>() {
                @Override
                protected void updateItem(ScmItem item, boolean empty) {
                    super.updateItem(item, empty);
                    String style = "";
                    if (!empty) {
                        String searchTerm = Context.searchValue.getValueSafe().toLowerCase();

                        Map<String, Set<String>> searchResult = Context.searchResult.getValue();

                        if (searchResult != null && !searchResult.isEmpty()) {
                            Set<String> affectedFiles = searchResult.getOrDefault(
                                    Context.scmRevCommitDetails.getValue().getRevisionFullName(),
                                    Collections.EMPTY_SET
                            );

                            boolean found = item.getViewRepresentation().toLowerCase().contains(searchTerm)
                                    || affectedFiles.contains(item.getViewRepresentation());


                            if (found) {
                                style = LookAndFeelSet.FOUND_ROW;

                            }

                        }

                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                //new DiffWithDiskEventHandler(item).handle(event);
                                openDiffFileVersion(null);
                            }
                        });



                    }
                    setStyle(style);

                }
            };

        });


        fileTableColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getViewRepresentation())
        );
        actionTableColumn.setCellValueFactory(
                c -> new WorkingcopyTableGraphicsValueFactory(c.getValue().getAttribute().getStatus(), null)
        );

        changedFilesListView.setOnContextMenuRequested( e-> {

            final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
            final boolean disableDiff = !ExtensionMap.isTextExtension(scmItem.getShortName());

            openDiffMenuItem.setDisable(disableDiff);
            diffWithPrevVersionMenuItem.setDisable(disableDiff);;
            diffWithCurrentVersionMenuItem.setDisable(disableDiff);;
            diffWithDiskVersionMenuItem.setDisable(disableDiff);;


        }  );

    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(actionEvent);
    }

    public void openRawDiff(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.RAW_DIFF).handle(actionEvent);
    }

    public void fileHistoryItemMenuItemClickHandler(ActionEvent actionEvent) throws IOException {
        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final String sha = scmInfo.getRevisionFullName();
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String treeName = Context.getGitRepoService().getBranchName(sha);
        final String fileName = scmItem.getShortName();

        Context.fileHistoryTree.setValue(treeName);
        Context.fileHistoryName.setValue(fileName);

        App.loadFXMLToNewStage(Const.View.FILE_HISTORY, "History");

    }

    public void openDiffPrevVersion(ActionEvent actionEvent) {

        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final String oldRev;
        if (scmInfo.getParents().isEmpty()) {

            oldRev = null;
        } else {
            oldRev = scmInfo.getParents().get(0); //TODO is it right parrent ?
        }

        final String newRev = scmInfo.getRevisionFullName();

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final DiffEventHandler eventHandler = new DiffEventHandler(scmItem, oldRev, newRev);
        eventHandler.handle(actionEvent);

    }

    public void openDiffLastVersion(ActionEvent actionEvent) throws Exception {

        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final String oldRev = scmInfo.getRevisionFullName();

        final CommitInfo head = Context.getGitRepoService().getHead();
        final String newRev = head.getSha();

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final DiffEventHandler eventHandler = new DiffEventHandler(scmItem, oldRev, newRev);
        eventHandler.handle(actionEvent);
    }

    public void openDiffFileVersion(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        if (ExtensionMap.isTextExtension(scmItem.getShortName())) {
            openDiffPrevVersion(actionEvent);
        } else {
            new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(actionEvent);
        }

    }
}
