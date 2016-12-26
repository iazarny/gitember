package com.az.gitember;

import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class CommitViewController implements Initializable {

    @FXML
    private Label msgLbl;

    @FXML
    private Label authorLbl;

    @FXML
    private Label emailLabel;

    @FXML
    private Label dateLbl;

    @FXML
    private Label shaLbl;

    @FXML
    private Label refsLbl;

    @FXML
    private Label parentLbl;

    @FXML
    private TableView changedFilesListView;

    @FXML
    private TableColumn<ScmItem, FontIcon> actionTableColumn;

    @FXML
    private TableColumn<ScmItem, String> fileTableColumn;

    @FXML
    private ContextMenu scmItemContextMenu;

    private ScmRevisionInformation scmRevisionInformation;
    private String treeName;
    private List<ScmItem> changedFiles;

    public void showPlotCommit() {
        this.msgLbl.setText(scmRevisionInformation.getFullMessage());
        this.authorLbl.setText(scmRevisionInformation.getAuthorName());
        this.emailLabel.setText(scmRevisionInformation.getAuthorEmail());
        this.dateLbl.setText(scmRevisionInformation.getDate().toString());
        final StringJoiner stringJoiner = new StringJoiner(", ");
        for (int i = 0; i < scmRevisionInformation.getRefCount(); i++) {
            stringJoiner.add(scmRevisionInformation.getRef().get(i));
        }
        this.refsLbl.setText(stringJoiner.toString());

        this.parentLbl.setText(
                scmRevisionInformation.getParents().stream().collect(Collectors.joining(", "))
        );

        this.shaLbl.setText(scmRevisionInformation.getRevisionFullName());

        changedFilesListView.setItems(
                FXCollections.observableArrayList(changedFiles)
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTableColumn.setCellValueFactory( c -> StringConstant.valueOf(c.getValue().getAttribute().getName()) );
        fileTableColumn.setContextMenu(scmItemContextMenu);
        actionTableColumn.setCellValueFactory(c -> new ActionCellValueFactory(c, null));
    }


    public void fillData(final String treeName, ScmRevisionInformation plotCommit) throws Exception {
        this.treeName = treeName;
        this.scmRevisionInformation = plotCommit;
        this.changedFiles = MainApp.getRepositoryService().getChangedFiles(treeName, plotCommit.getRevisionFullName());
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
                    MainApp.getRepositoryService().saveFile(treeName, revisionFullName, fileName),
                    fileName);
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }

    }


    public void openDiffItemMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String revisionName = scmRevisionInformation.getRevisionFullName();
        final String path = MainApp.getRepositoryService().saveDiff(treeName, revisionName, fileName);
        final FileViewController fileViewController = new FileViewController();
        fileViewController.openFile(path, fileName);
    }

    public void openDiffWithPreviosVersionMenuItemClickHandler(ActionEvent actionEvent) throws Exception {

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String parentREvision = scmRevisionInformation.getParents().get(0); //todo determinate parent right
        final String oldFile = MainApp.getRepositoryService().saveFile(treeName, parentREvision, fileName);
        final String newFile = MainApp.getRepositoryService().saveFile(treeName, scmRevisionInformation.getRevisionFullName(), fileName);
        final String diffFile = MainApp.getRepositoryService().saveDiff(treeName, scmRevisionInformation.getRevisionFullName(), fileName);
        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, parentREvision,
                newFile, scmRevisionInformation.getRevisionFullName(),
                diffFile);
    }

    public void openDiffWithFileOnDiskMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String oldRevisionName = scmRevisionInformation.getRevisionFullName();
        final String oldFile = MainApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);

        final String newFile = MainApp.getCurrentRepositoryPathWOGit() + File.separator + fileName;

        List<String> newFileLines = Files.readAllLines(Paths.get(newFile));
        List<String> oldFileLines = Files.readAllLines(Paths.get(oldFile));
        Patch<String> pathc = DiffUtils.diff(oldFileLines, newFileLines);

        System.out.println(newFile);

        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, oldRevisionName,
                newFile, "On disk",
                pathc);

    }


    public void openDiffWithLatestVersionMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String oldRevisionName = scmRevisionInformation.getRevisionFullName();
        final String oldFile = MainApp.getRepositoryService().saveFile(treeName, oldRevisionName, fileName);

        final ScmRevisionInformation lastCommit = MainApp.getRepositoryService().getFileHistory(treeName, fileName, 1).get(0);

        final String newRevisionName = lastCommit.getRevisionFullName();

        final String newFile = MainApp.getRepositoryService().saveFile(treeName, newRevisionName, fileName);
        final String diffFile = MainApp.getRepositoryService().saveDiff(treeName, oldRevisionName, newRevisionName, fileName);
        final DiffViewController fileViewController = new DiffViewController();
        fileViewController.openFile(
                new File(fileName).getName(),
                oldFile, oldRevisionName,
                newFile, newRevisionName,
                diffFile);

    }

    public void historyMenuItemClickHandler(ActionEvent actionEvent) throws Exception {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/HistoryViewPane.fxml").openStream()) {
            final Parent view = fxmlLoader.load(is);
            final HistoryViewController historyViewController = fxmlLoader.getController();
            historyViewController.setFileName(fileName);
            historyViewController.setTreeName(treeName);
            historyViewController.openHistory();


            Scene scene = new Scene(view, 1024, 768);

            final Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(fileName);
            stage.show();


        }
    }


}
