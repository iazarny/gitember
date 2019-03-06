package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.ui.ActionCellValueFactory;
import com.sun.javafx.binding.IntegerConstant;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.text.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class StatViewController implements Initializable {

    private GridPane gridPanel;

    public StatViewController() {

        gridPanel = new GridPane();
        GridPane.setHgrow(gridPanel, Priority.ALWAYS);
        GridPane.setFillWidth(gridPanel, true);
        gridPanel.setHgap(10);
        gridPanel.setVgap(10);


    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }


    public static Parent openStatWindow(Map<String, Integer> authorCnt, Map<String, Integer> authorCommits) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = StatViewController.class.getResource("/fxml/StatViewPane.fxml").openStream()) {
            final Parent view = fxmlLoader.load(is);
            final StatViewController historyViewController = fxmlLoader.getController();


            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            authorCnt.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(
                    e -> {
                        pieChartData.add(
                                new PieChart.Data(e.getKey(), e.getValue())
                        );
                    }
            );

            final PieChart chart = new PieChart(pieChartData);
            chart.setTitle("Code lines");
            chart.setLabelLineLength(10);
            chart.setLegendSide(Side.RIGHT);
            chart.setMinWidth(600);
            chart.setMinHeight(600);

            final Label caption = new Label("");

            caption.setTextFill(Color.DARKORANGE);
            caption.setStyle("-fx-font: 24 arial;");

            for (final PieChart.Data data : chart.getData()) {
                data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {
                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY() - chart.getHeight() / 2);
                                caption.setText(String.valueOf((int)data.getPieValue()) + " lines");
                            }
                        });
            }

            ObservableList<Pair<String, Integer>> commitCounter = FXCollections.observableArrayList();
            authorCommits.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(
                            e -> {
                                commitCounter.add(
                                        new Pair<String, Integer>(e.getKey(), e.getValue())
                                );
                            }
                    );

            TableView<Pair<String, Integer>> tableView = new TableView<>(commitCounter);
            TableColumn<Pair<String, Integer>, String> firstNameCol = new TableColumn("Author");
            TableColumn<Pair<String, Integer>, Integer> commitsCol = new TableColumn("Commits");
            tableView.getColumns().addAll(firstNameCol, commitsCol);
            firstNameCol.setCellValueFactory(c -> StringConstant.valueOf(  c.getValue().getFirst()  ));
            commitsCol.setCellValueFactory(c -> {
                return new ObservableValue<Integer>() {
                    @Override
                    public void addListener(ChangeListener<? super Integer> listener) {

                    }

                    @Override
                    public void removeListener(ChangeListener<? super Integer> listener) {

                    }

                    @Override
                    public Integer getValue() {
                        return c.getValue().getSecond();
                    }

                    @Override
                    public void addListener(InvalidationListener listener) {

                    }

                    @Override
                    public void removeListener(InvalidationListener listener) {

                    }
                };
            });


            final Scene scene = new Scene(historyViewController.gridPanel, 1024, 768);
            historyViewController.gridPanel.add(new Label("   "),0,0);
            historyViewController.gridPanel.add(chart,1,1);
            historyViewController.gridPanel.add(caption,1,1);
            historyViewController.gridPanel.add(tableView,2,1);

            final Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Statistic report");
            stage.getIcons().add(new Image(StatViewController.class.getClass().getResourceAsStream(Const.ICON)));
            stage.show();


            return view;


        }
    }
}
