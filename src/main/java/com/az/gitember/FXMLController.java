package com.az.gitember;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.service.SettingsServiceImpl;
import com.az.gitember.ui.ScmItemCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {


    @FXML
    public Button cloneBtn;

    @FXML
    public Button fetchBtn;

    @FXML
    public Button pullBtn;

    @FXML
    public Button pushBtn;

    @FXML
    public Button openBtn;

    @FXML
    public Menu openRecentMenuItem;

    @FXML
    public TitledPane workSpaceTitlePane;

    @FXML
    private ListView localBranchesList;

    @FXML
    private ListView remoteBranchesList;

    @FXML
    private ListView tagList;

    @FXML
    private AnchorPane hostPanel;

    private SettingsServiceImpl settingsService = new SettingsServiceImpl();

    @FXML
    public void openHandler(ActionEvent actionEvent) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (settingsService.getLastProject() != null) {
            directoryChooser.setInitialDirectory(new File(settingsService.getLastProject()));
        }
        File selectedDirectory =
                directoryChooser.showDialog(MainApp.getMainStage());
        if(selectedDirectory != null){
            String absPath = selectedDirectory.getAbsolutePath();
            if (!absPath.endsWith(Const.GIT_FOLDER)) {
                absPath += File.separator + Const.GIT_FOLDER;
            }
            openRepository(absPath);
            openWorkingCopyHandler(actionEvent);
        }

    }

    public void openRepository(String absPath) throws Exception {
        MainApp.setCurrentRepositoryPath(absPath);
        MainApp.setRepositoryService(new GitRepositoryService(absPath));
        localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
        remoteBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getRemoteBranches()));
        tagList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getTags()));
        settingsService.saveRepository(absPath);
        this.pullBtn.setDisable(MainApp.getRepositoryService().getRemoteUrl() == null);
        MainApp.setTitle(Const.TITLE + MainApp.getCurrentRepositoryPathWOGit());
    }

    @FXML
    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final ScmItemCellFactory scmItemCellFactory = new ScmItemCellFactory();
        remoteBranchesList.setCellFactory(scmItemCellFactory);
        localBranchesList.setCellFactory(scmItemCellFactory);
        tagList.setCellFactory(scmItemCellFactory);

        try {

            openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
            settingsService.getRecentProjects().stream().forEach(
                    o -> {
                        MenuItem mi = new MenuItem(o.getFirst());
                        mi.setUserData(o.getSecond());
                        mi.setOnAction(
                                event -> {
                                    try {
                                        openRepository(o.getSecond());
                                        openWorkingCopyHandler(null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                        openRecentMenuItem.getItems().add(mi);
                    }

            );
            openRecentMenuItem.setDisable(
                    openRecentMenuItem.getItems().size() < 1
            );

        } catch (Exception e) {

        }


    }

    @FXML
    public void branchesListMouseClicked(Event event) throws Exception {
        ScmBranch scmBranch = ((ListView<ScmBranch>) event.getSource()).getSelectionModel().getSelectedItem();

        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/BranchViewPane.fxml").openStream()) {
            final Parent branchView = fxmlLoader.load(is);
            final BranchViewController branchViewController = fxmlLoader.getController();
            branchViewController.setTreeName(scmBranch.getFullName());
            branchViewController.open();
            hostPanel.getChildren().removeAll(hostPanel.getChildren());
            hostPanel.getChildren().add(branchView);
        }

    }

    public void cloneHandler(ActionEvent actionEvent) {
    }

    public void fetchHandler(ActionEvent actionEvent) {
    }

    public void pullHandler(ActionEvent actionEvent) throws GitAPIException {

        MainApp.getRepositoryService().remoteRepositoryPull();
    }

    public void pushHandler(ActionEvent actionEvent) {
    }

    public void exitHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void openWorkingCopyHandler(ActionEvent actionEvent) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/WorkingCopyPane.fxml").openStream()) {
            final Parent workCopyView = fxmlLoader.load(is);
            final WorkingCopyController workingCopyController = fxmlLoader.getController();
            workingCopyController.open();
            hostPanel.getChildren().removeAll(hostPanel.getChildren());
            hostPanel.getChildren().add(workCopyView);
        }

    }

    /**
     * Create new git repository.
     *
     * @param actionEvent
     */
    public void createRepositoryHandler(ActionEvent actionEvent)  throws Exception {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(settingsService.getUserHomeFolder()));
        final File selectedDirectory =
                directoryChooser.showDialog(MainApp.getMainStage());
        if(selectedDirectory != null){
            String absPath = selectedDirectory.getAbsolutePath();
            MainApp.getRepositoryService().createRepository(absPath);

            openRepository(absPath + File.separator + Const.GIT_FOLDER);
            openWorkingCopyHandler(null);
        }

    }
}
