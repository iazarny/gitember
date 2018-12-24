package com.az.gitember;

import com.az.gitember.misc.*;
import com.az.gitember.ui.ActionCellValueFactory;
import com.az.gitember.ui.AutoCompleteTextField;
import com.az.gitember.ui.ShaTextField;
import com.sun.javafx.binding.StringConstant;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class CommitViewController implements Initializable {

    @FXML
    public Menu stashMenu;

    @FXML
    private ShaTextField msgLbl;

    @FXML
    private ShaTextField authorLbl;

    @FXML
    private ShaTextField emailLabel;

    @FXML
    private ShaTextField dateLbl;

    @FXML
    private ShaTextField shaLbl;

    @FXML
    private ShaTextField refsLbl;

    @FXML
    private ShaTextField parentLbl;

    @FXML
    private TableView changedFilesListView;

    @FXML
    private TableColumn<ScmItem, FontIcon> actionTableColumn;

    @FXML
    private TableColumn<ScmItem, String> fileTableColumn;

    @FXML
    private ContextMenu scmItemContextMenu;

    private ScmRevisionInformation scmRevisionInformation;
    private int stashIndex;
    private Consumer<ScmRevisionInformation> onStashDelete = null;
    private String treeName;
    private List<ScmItem> changedFiles;
    private AutoCompleteTextField searchText = null;

    public void showPlotCommit() {
        this.msgLbl.setText(scmRevisionInformation.getFullMessage());
        this.authorLbl.setText(scmRevisionInformation.getAuthorName());
        this.emailLabel.setText(scmRevisionInformation.getAuthorEmail());
        this.dateLbl.setText(GitemberUtil.formatDate(scmRevisionInformation.getDate()));
        final StringJoiner stringJoiner = new StringJoiner(", ");
        if (scmRevisionInformation.getRef() != null){
            for (int i = 0; i < scmRevisionInformation.getRefCount(); i++) {
                stringJoiner.add(scmRevisionInformation.getRef().get(i));
            }
        }

        this.refsLbl.setText(stringJoiner.toString());

        this.parentLbl.setText(
                scmRevisionInformation.getParents().stream().collect(Collectors.joining(", "))
        );

        this.shaLbl.setText(scmRevisionInformation.getRevisionFullName());

        changedFilesListView.setItems(
                FXCollections.observableArrayList(changedFiles)
        );

        changedFilesListView.setRowFactory(
                tr -> {
                    return new TableRow<ScmItem>() {

                        private String calculateStyle(final ScmItem scmItem) {
                            if (scmItem != null
                                    && CommitViewController.this.searchText != null
                                    && CommitViewController.this.searchText.getText() != null
                                    && CommitViewController.this.searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                                if (scmItem.getSecond().getName().toLowerCase().contains(CommitViewController.this.searchText.getText().toLowerCase())) {
                                    return "-fx-font-weight: bold;";
                                }

                            }
                            return "";

                        }

                        @Override
                        protected void updateItem(ScmItem item, boolean empty) {
                            super.updateItem(item, empty);
                            setStyle(calculateStyle(item));
                        }
                    };
                }
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTableColumn.setCellValueFactory( c -> StringConstant.valueOf(c.getValue().getAttribute().getName()) );
        fileTableColumn.setContextMenu(scmItemContextMenu);
        actionTableColumn.setCellValueFactory(c -> new ActionCellValueFactory(c, null));
    }


    public void fillData(final String treeName, ScmRevisionInformation revInfo) throws Exception {
        this.treeName = treeName;
        this.scmRevisionInformation = revInfo;
        this.changedFiles = revInfo.getAffectedItems();
                //GitemberApp.getRepositoryService().getChangedFiles(treeName, plotCommit.getRevisionFullName());
    }

    public void openItemTableViewDoubleClickedHandler(MouseEvent event) {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
            openFile();
        }
    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        openFile();
    }

    private void openFile() {
        final String revisionFullName = scmRevisionInformation.getRevisionFullName();
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        try {
            final FileViewController fileViewController = new FileViewController();
            fileViewController.openFile(
                    GitemberApp.getRepositoryService().saveFile(treeName, revisionFullName, fileName),
                    fileName);
        } catch (Exception e) {
            GitemberApp.showResult("Could not open file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public void openDiffItemMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String revisionName = scmRevisionInformation.getRevisionFullName();
        final String path = GitemberApp.getRepositoryService().saveDiff(treeName, revisionName, fileName);
        final FileViewController fileViewController = new FileViewController();
        fileViewController.openFile(path, fileName);
    }

    public void openDiffWithPreviosVersionMenuItemClickHandler(ActionEvent actionEvent) throws Exception {

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String parentREvision = scmRevisionInformation.getParents().get(0); //todo determinate parent right
        final String oldFile = GitemberApp.getRepositoryService().saveFile(treeName, parentREvision, fileName);
        final String newFile = GitemberApp.getRepositoryService().saveFile(treeName, scmRevisionInformation.getRevisionFullName(), fileName);
        final String diffFile = GitemberApp.getRepositoryService().saveDiff(treeName, scmRevisionInformation.getRevisionFullName(), fileName);
        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, parentREvision,
                newFile, scmRevisionInformation.getRevisionFullName(),
                diffFile);
    }

    /**
     * Show difference between disk and selected version.
     * @param actionEvent event
     * @throws Exception
     */
    public void openDiffWithFileOnDiskMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String oldRevisionName = scmRevisionInformation.getRevisionFullName();
        final String oldFile = GitemberApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);

        final String newFile = GitemberApp.getCurrentRepositoryPathWOGit() + File.separator + fileName;

        List<String> newFileLines = Files.readAllLines(Paths.get(newFile));
        List<String> oldFileLines = Files.readAllLines(Paths.get(oldFile));
        Patch<String> pathc = DiffUtils.diff(oldFileLines, newFileLines);


        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, oldRevisionName,
                newFile, "On disk",
                pathc);

    }


    /**
     * Show difference
     * @param actionEvent event
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public void openDiffWithLatestVersionMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String oldRevisionName = scmRevisionInformation.getRevisionFullName();
        final String oldFile = GitemberApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);

        final ScmRevisionInformation lastCommit = GitemberApp.getRepositoryService().getFileHistory(treeName, fileName, 1).get(0);

        final String newRevisionName = lastCommit.getRevisionFullName();

        final String newFile = GitemberApp.getRepositoryService().saveFile(treeName, newRevisionName, fileName);
        final String diffFile = GitemberApp.getRepositoryService().saveDiff(treeName, oldRevisionName, newRevisionName, fileName);
        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, oldRevisionName,
                newFile, newRevisionName,
                diffFile);

    }

    /**
     * Handle open history command
     * @param actionEvent event
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public void historyMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        HistoryViewController.openHistoryWindow(
                scmItem.getAttribute().getName(),
                treeName);
    }

    /**
     * Apply stash.
     * @param actionEvent
     */
    @SuppressWarnings("unused")
    public void applyStashHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().applyStash(scmRevisionInformation);
    }

    /**
     * Delete stash
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void deleteStashHandler(ActionEvent actionEvent) {
        if (GitemberApp.getGitemberService().deleteStash(
                scmRevisionInformation,
                stashIndex
        )) {
            if (onStashDelete != null) {
                onStashDelete.accept(scmRevisionInformation);
            }
            GitemberApp.showResult("Deleted stash", Alert.AlertType.INFORMATION);
        }

    }

    public static Parent openCommitViewWindow(final ScmRevisionInformation info,
                                              final int stashIndex,
                                              final String treeName,
                                              final MenuBar menuBar,
                                              final ToolBar mainToolBar,
                                              final Consumer<ScmRevisionInformation> onStashDelete) {

        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = CommitViewController.class.getResource("/fxml/CommitViewPane.fxml").openStream()) {
            final Parent commitView = fxmlLoader.load(is);
            final CommitViewController commitViewController = fxmlLoader.getController();
            commitViewController.stashIndex = stashIndex;
            commitViewController.onStashDelete = onStashDelete;
            commitViewController.fillData(
                    treeName,
                    info
            );
            commitViewController.showPlotCommit();

            if (menuBar != null) {
                menuBar.getMenus().add(2, commitViewController.stashMenu);
            }
            if (mainToolBar!= null) {
                Pane spacerPane = new Pane();
                HBox.setHgrow(spacerPane, Priority.ALWAYS);
                spacerPane.setId(Const.MERGED);

                commitViewController.searchText = new AutoCompleteTextField();
                commitViewController.searchText.setId(Const.MERGED);
                commitViewController.searchText.getEntries().addAll(GitemberApp.entries);
                commitViewController.searchText.textProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            commitViewController.changedFilesListView.refresh();
                            if (oldValue != null
                                    && newValue!= null
                                    && newValue.length() > oldValue.length()
                                    && newValue.contains(oldValue)) {

                                GitemberApp.entries.remove(oldValue);
                                GitemberApp.entries.add(newValue);
                            }
                        }
                );


                mainToolBar.getItems().add(spacerPane);
                mainToolBar.getItems().add(commitViewController.searchText);
            }

            return commitView;
        } catch (Exception e) {
            e.printStackTrace(); //todo log
        }
        return null;
    }



}
