package com.az.gitember.module.history;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.controller.handlers.CheckoutRevisionhEventHandler;
import com.az.gitember.data.Const;
import com.az.gitember.data.Settings;
import com.az.gitember.module.history.factory.HistoryCommitsTableLaneCellFactory;
import com.az.gitember.module.history.factory.HistoryCommitsTableRowFactory;
import com.az.gitember.module.history.listener.HistoryCommitsTableViewChangeListener;
import com.az.gitember.module.history.renderer.HistoryPlotCommitRenderer;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryController implements Initializable {
    private final static Logger log = Logger.getLogger(HistoryController.class.getName());

    private static final int HEIGH = 40;

    @FXML
    public AnchorPane hostHistoryViewPanel;
    public SplitPane splitPanel;
    public BorderPane mainBorderPanel;
    public Pane spacerPane;

    private HistoryPlotCommitRenderer plotCommitRenderer = new HistoryPlotCommitRenderer();

    private ChangeListenerHistoryHint changeListenerHistoryHint;

    @FXML
    private TextField searchText;

    @FXML
    private TableColumn<PlotCommit<PlotLane>, Canvas> laneTableColumn;

    @FXML
    private TableColumn<PlotCommit<PlotLane>, String> dateTableColumn;

    @FXML
    private TableColumn<PlotCommit<PlotLane>, String> authorTableColumn;

    @FXML
    public TableColumn<PlotCommit<PlotLane>, String> shaTableColumn;

    @FXML
    private TableView commitsTableView;


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        commitsTableView.setFixedCellSize(HEIGH);
        commitsTableView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(new HistoryCommitsTableViewChangeListener(mainBorderPanel, splitPanel));

        commitsTableView.setRowFactory(tableView -> new HistoryCommitsTableRowFactory(searchText));

        commitsTableView.widthProperty().addListener((observable, oldValue, newValue) -> {
            laneTableColumn.setPrefWidth(newValue.doubleValue() - authorTableColumn.getWidth() - dateTableColumn.getWidth() - shaTableColumn.getWidth() - 20);
        });
        laneTableColumn.setCellValueFactory(c -> new HistoryCommitsTableLaneCellFactory(c.getValue(), plotCommitRenderer, HEIGH));

        shaTableColumn.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(c.getValue().getName())
        );

        authorTableColumn.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(c.getValue().getAuthorIdent().getName())
        );

        dateTableColumn.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        GitemberUtil.formatDate(GitemberUtil.intToDate(c.getValue().getCommitTime()))
                )
        );


        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if ( newValue != null
                            && newValue .length() > Const.SEARCH_LIMIT_CHAR ) {

                        Map<String, Set<String>> searchRez = Context.getGitRepoService().search(
                                (List<PlotCommit>) Context.plotCommitList,
                                newValue,
                                Context.getCurrentProject().isIndexed());

                        Settings settings =  Context.settingsProperty.getValue();
                        settings.getSearchTerms().remove(oldValue);
                        settings.getSearchTerms().add(newValue);
                        Context.searchValue.setValue(newValue);
                        Context.searchResult.setValue(searchRez);

                        Iterator<PlotCommit> commits = Context.plotCommitList.iterator();
                        int idx = 0;
                        while (commits.hasNext()) {
                            PlotCommit commit = commits.next();
                            if (searchRez.containsKey(commit.getName())) {
                                commitsTableView.scrollTo(idx);
                                break;
                            }
                            idx++;
                        }
                        commitsTableView.refresh();
                    } else {
                        Context.searchResult.setValue(null);
                        commitsTableView.refresh();
                    }
                }
        );

        changeListenerHistoryHint = new ChangeListenerHistoryHint(searchText, Context.settingsProperty.getValue().getSearchTerms());

        HBox.setHgrow(spacerPane, Priority.ALWAYS);
        commitsTableView.setItems(Context.plotCommitList);
    }

    public void checkoutMenuItemClickHandler(ActionEvent actionEvent) {

        new CheckoutRevisionhEventHandler((RevCommit) commitsTableView.getSelectionModel().getSelectedItem(), null).handle(actionEvent);


    }

    public void cherryPickMenuItemClickHandler(ActionEvent actionEvent) {

        final RevCommit revCommit = (RevCommit) commitsTableView.getSelectionModel().getSelectedItem();

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Cherry pick");
        alert.setContentText("Do you really want to apply changes from \n"
                + revCommit.getName() + " ?");
        alert.initOwner(App.getScene().getWindow());
        alert.setWidth(alert.getWidth() * 2);
        alert.setHeight(alert.getHeight() * 1.5);

        alert.showAndWait().ifPresent( r-> {

            try {
                if (r == ButtonType.OK) {
                    CherryPickResult cherryPickResult = Context.getGitRepoService().cherryPick(revCommit);
                }
            } catch (IOException e) {
                Context.getMain().showResult("Cherry pick is failed ",
                        ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
            } finally {
                Context.updateStatus(null);
            }

        } );

    }

    public void checkoutAsMenuItemClickHandler(ActionEvent actionEvent) {


        final RevCommit revCommit = (RevCommit) commitsTableView.getSelectionModel().getSelectedItem();
        final TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Checkout revision");
        dialog.setHeaderText("New branch for revision");
        dialog.setContentText("Please enter new branch name for revision:");
        dialog.initOwner(App.getScene().getWindow());

        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            String newName = dialogResult.get();
            try {
                new CheckoutRevisionhEventHandler(revCommit , newName).handle(actionEvent);
                Context.updateBranches();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create branch " +
                        dialogResult.get() + " from commit " + revCommit.getName(), e);
                Context.getMain().showResult("Create new branch",
                        ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
            }
        }

    }
}
