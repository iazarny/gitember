package com.az.gitember;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.az.gitember.misc.ScmBranch;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.ScmItemCellFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {


    @FXML
    private ListView localBranchesList;

    @FXML
    private ListView remoteBranchesList;

    @FXML
    private ListView tagList;

    @FXML
    private AnchorPane hostPanel;

    private GitRepositoryService repositoryService;

    @FXML
    public void fillDataHandler(ActionEvent actionEvent) {

        try {
            repositoryService = new GitRepositoryService("C:/dev/epam/caga/project/multirepo/cgbs-pricing/.git");

            localBranchesList.setItems(FXCollections.observableList(repositoryService.getLocalBranches()));
            remoteBranchesList.setItems(FXCollections.observableList(repositoryService.getRemoteBranches()));
            tagList.setItems(FXCollections.observableList(repositoryService.getTags()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exitActionHandler(ActionEvent actionEvent) {
        System.exit(0);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final ScmItemCellFactory scmItemCellFactory = new ScmItemCellFactory();
        remoteBranchesList.setCellFactory(scmItemCellFactory);
        localBranchesList.setCellFactory(scmItemCellFactory);
        tagList.setCellFactory(scmItemCellFactory);
    }

    @FXML
    public void branchesListMouseClicked(Event event) {

        ScmBranch scmBranch = ((ListView<ScmBranch>) event.getSource()).getSelectionModel().getSelectedItem();

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader();
            try (InputStream is = getClass().getResource("/fxml/BranchViewPane.fxml").openStream()) {
                final Parent branchView = fxmlLoader.load(is);
                final BranchViewController branchViewController = fxmlLoader.getController();

                branchViewController.setTreeName(scmBranch.getFullName());
                branchViewController.setRepositoryService(repositoryService);
                branchViewController.open();



                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(branchView);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
