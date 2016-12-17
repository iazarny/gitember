package com.az.gitember;

import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.StringConstant;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
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


    private GitRepositoryService repositoryService;
    private String fileName;
    private String treeName;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        revisionTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getRevisionFullName()));
        authorTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getAuthorName()));
        messageTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getFullMessage()));
        dateTableColumn.setCellValueFactory(c -> StringConstant.valueOf(c.getValue().getDate().toString()));
        actionTableColumn.setCellValueFactory( c -> new ActionCellValueFactory(null, c));
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
}
