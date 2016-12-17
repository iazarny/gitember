package com.az.gitember;

import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.ScmItem;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
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
import org.eclipse.jgit.revplot.PlotCommit;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.*;
import java.net.URL;
import java.util.*;
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

    private GitRepositoryService repositoryService;


    private ScmRevisionInformation plotCommit;
    private String treeName;
    private List<ScmItem> changedFiles;

    public void showPlotCommit() {
        this.msgLbl.setText(plotCommit.getFullMessage());
        this.authorLbl.setText(plotCommit.getAuthorName());
        this.emailLabel.setText(plotCommit.getAuthorEmail());
        this.dateLbl.setText(plotCommit.getDate().toString());
        final StringJoiner stringJoiner = new StringJoiner(", ");
        for (int i = 0; i < plotCommit.getRefCount(); i++) {
            stringJoiner.add(plotCommit.getRef().get(i));
        }
        this.refsLbl.setText(stringJoiner.toString());

        this.parentLbl.setText(
                plotCommit.getParents().stream().collect(Collectors.joining(", "))
        );

        this.shaLbl.setText(plotCommit.getRevisionFullName());

        changedFilesListView.setItems(
                FXCollections.observableArrayList(changedFiles)
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getAttribute().getName())
        );
        fileTableColumn.setContextMenu(scmItemContextMenu);
        actionTableColumn.setCellValueFactory(
                c -> {
                    return new ActionCellValueFactory(c, null);
                }
        );
    }

    public void setRepositoryService(final GitRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void fillData(final String treeName, ScmRevisionInformation plotCommit) throws Exception {
        this.treeName = treeName;
        this.plotCommit = plotCommit;
        this.changedFiles = repositoryService.getChangedFiles(treeName, plotCommit.getRevisionFullName());
    }

    public void openItemTableViewDoubleClickedHandler(MouseEvent event) {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
            openFile();
        }
    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {

        openFile();

    }

    public void openDiffWithPreviosVersionMenuItemClickHandler(ActionEvent actionEvent) {

        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String fileNameExtension = GitemberUtil.getExtension(fileName);

        try {

            File tempNew = File.createTempFile(
                    "gitember",
                    fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
            File tempOld = File.createTempFile(
                    "gitember",
                    fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
            try (OutputStream outputStreamNew = new FileOutputStream(tempNew);
                 OutputStream outputStreamOld = new FileOutputStream(tempOld); ) {
                String oldParent = "NA";
                for (String parent : plotCommit.getParents()) {
                    try {
                        repositoryService.saveFile(
                                treeName, parent, fileName, outputStreamOld);
                        oldParent = parent;
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

                repositoryService.saveFile(
                        treeName, plotCommit.getRevisionFullName(), fileName, outputStreamNew);
                final File diffFile = getDiffFile(fileName, plotCommit.getRevisionFullName());

                final DiffViewController fileViewController = new DiffViewController();
                fileViewController.openFile(
                        tempOld.getAbsolutePath(), oldParent,
                        tempNew.getAbsolutePath(), plotCommit.getRevisionFullName(),
                        diffFile.getAbsolutePath());
            }
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }

    private void openFile() {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        final String fileNameExtension = GitemberUtil.getExtension(fileName);

        try {

            File temp = File.createTempFile(
                    "gitember",
                    fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
            try (OutputStream outputStream = new FileOutputStream(temp)) {
                repositoryService.saveFile(
                        treeName, plotCommit.getRevisionFullName(), fileName, outputStream);

                /*if (GitemberUtil.isBinaryFile(temp)) {
                    if (Desktop.isDesktopSupported()) {
                        new Thread(() -> {
                            try {
                                Desktop.getDesktop().browse(temp.toURI()); //TODO bad idea, need to rethink
                            } catch (IOException e) {
                                e.printStackTrace(); //TODO error dialog

                            }
                        }).start();
                    }
                } else {*/
                final FileViewController fileViewController = new FileViewController();
                fileViewController.openFile(temp.getAbsolutePath(), fileName);
                //}
            }
        } catch (Exception e) {       //todo error dialog
            e.printStackTrace();
        }
    }


    public void openDiffItemMenuItemClickHandler(ActionEvent actionEvent) {


        try {

            final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
            final String fileName = scmItem.getAttribute().getName();
            final String revisionName = plotCommit.getRevisionFullName();
            final File temp = getDiffFile(fileName, revisionName);

            final FileViewController fileViewController = new FileViewController();
            fileViewController.openFile(temp.getAbsolutePath(), fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }





    }

    private File getDiffFile(String fileName, String revisionName) throws Exception {
        final String fileNameExtension = "diff";

        final File temp = File.createTempFile(
                "gitember",
                fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);

        try (OutputStream outputStream = new FileOutputStream(temp)) {
            repositoryService.saveDiff(treeName, revisionName, fileName, outputStream);

        }
        return temp;
    }

    public void historyMenuItemClickHandler(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String fileName = scmItem.getAttribute().getName();
        try {

            final FXMLLoader fxmlLoader = new FXMLLoader();
            try (InputStream is = getClass().getResource("/fxml/HistoryViewPane.fxml").openStream()) {
                final Parent view = fxmlLoader.load(is);
                final HistoryViewController historyViewController = fxmlLoader.getController();
                historyViewController.setFileName(fileName);
                historyViewController.setTreeName(treeName);
                historyViewController.setRepositoryService(repositoryService);
                historyViewController.openHistory();


                Scene scene = new Scene(view, 1024, 768);

                final Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle(fileName);
                stage.show();


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
