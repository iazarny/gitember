package com.az.gitember;

import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.ui.PlotCommitRenderer;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class BranchViewController implements Initializable {

    private final static Logger log = Logger.getLogger(BranchViewController.class.getName());

    private static final int HEIGH = 30;

    @FXML
    private TableColumn<PlotCommit, Canvas> laneTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> dateTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> messageTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> authorTableColumn;

    @FXML
    private TableView commitsTableView;

    @FXML
    private AnchorPane hostCommitViewPanel;

    private PlotCommitRenderer plotCommitRenderer = new PlotCommitRenderer();

    private String treeName;

    private int plotWidth = 5 * HEIGH;

    private PlotCommitList<PlotLane> plotCommits;



    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        commitsTableView.setFixedCellSize(HEIGH);
        commitsTableView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<PlotCommit>() {

                    @Override
                    public void changed(final ObservableValue<? extends PlotCommit> observable,
                                        final PlotCommit oldValue,
                                        final PlotCommit newValue) {

                        Parent commitView = CommitViewController.openCommitViewWindow(
                                GitemberApp.getRepositoryService().adapt(newValue, null),
                                -1, treeName, null, null);
                        hostCommitViewPanel.getChildren().removeAll(hostCommitViewPanel.getChildren());
                        hostCommitViewPanel.getChildren().add(commitView);
                    }

                });

        laneTableColumn.setCellValueFactory(

                c -> {
                    return new ObservableValue<Canvas>() {
                        @Override
                        public Canvas getValue() {
                            Canvas canvas = new Canvas(plotWidth, HEIGH);
                            GraphicsContext gc = canvas.getGraphicsContext2D();
                            plotCommitRenderer.render(gc, c.getValue(), HEIGH);
                            return canvas;
                        }

                        @Override
                        public void addListener(InvalidationListener listener) {
                        }

                        @Override
                        public void removeListener(InvalidationListener listener) {
                        }

                        @Override
                        public void addListener(ChangeListener<? super Canvas> listener) {
                        }

                        @Override
                        public void removeListener(ChangeListener<? super Canvas> listener) {
                        }

                    };
                }
        );

        authorTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getCommitterIdent().getName())
        );
        messageTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getShortMessage())
        );
        dateTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(GitemberUtil.intToDate(c.getValue().getCommitTime()).toString())
        );
    }


    public void open() throws Exception {
        this.plotCommits = GitemberApp.getRepositoryService().getCommitsByTree(this.treeName);
        commitsTableView.setItems(FXCollections.observableArrayList(plotCommits));
        plotWidth = calculateLineColumnWidth(plotCommits);
        laneTableColumn.setPrefWidth(plotWidth);

    }


    private int calculateLineColumnWidth(PlotCommitList<PlotLane> plotCommits) {
        return 36 + 12 * plotCommits.stream().mapToInt(p -> p.getLane().getPosition()).max().orElse(0);
    }

    public static Parent openBranchHistory(final ScmBranch scmBranch) {
        try {
            if (scmBranch != null) {
                final FXMLLoader fxmlLoader = new FXMLLoader();
                try (InputStream is = BranchViewController.class.getResource("/fxml/BranchViewPane.fxml").openStream()) {
                    final Parent branchView = fxmlLoader.load(is);
                    final BranchViewController branchViewController = fxmlLoader.getController();
                    branchViewController.treeName = scmBranch.getFullName();
                    branchViewController.open();
                    return branchView;
                }

            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot open branch view", e);
        }
        return null;
    }
}
