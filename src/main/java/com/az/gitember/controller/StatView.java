package com.az.gitember.controller;

import com.az.gitember.service.Context;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StatView implements Initializable {

    public PieChart chart;
    public TableView commitsDetails;
    public TableColumn<Object[], String>  itemTableColumnName;
    public TableColumn<Object[], Integer>  itemTableColumnCommits;
    public TableColumn<Object[], Integer>  itemTableColumnLines;
    public TableColumn<Object[], Integer>  itemTableColumnAvgLV;
    public Label caption;

    private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    private ObservableList<Object[]> commitCounter = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        chart.setData(pieChartData);
        commitsDetails.setItems(commitCounter);

        itemTableColumnName.setCellValueFactory(c ->   new SimpleStringProperty((String)c.getValue()[0])   );
        itemTableColumnCommits.setCellValueFactory(c ->   new SimpleIntegerProperty((int) c.getValue()[2]).asObject()   );
        itemTableColumnLines.setCellValueFactory(c ->   new SimpleIntegerProperty((int) c.getValue()[1]).asObject()   );
        itemTableColumnAvgLV.setCellValueFactory(c ->   new SimpleIntegerProperty(    (0 == (int) c.getValue()[2]) ? 0 :   ((int) c.getValue()[1] / (int) c.getValue()[2])).asObject()   );

        Context.scmStatProperty.addListener(
                (observable, oldValue, newValue) -> {
                    pieChartData.clear();
                    commitCounter.clear();




                    newValue.getTotalLines().entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue())
                            .forEach(
                                    e -> {
                                        final PieChart.Data chartData =  new PieChart.Data(e.getKey(), e.getValue());


                                        pieChartData.add( chartData );
                                        commitCounter.add(
                                                new Object [] {e.getKey(), e.getValue(), newValue.getLogMap().getOrDefault(e.getKey(), 0)}
                                        );
                                        chart.getChildrenUnmodifiable().stream().forEach(
                                                node -> {
                                                    if (node instanceof Parent) {
                                                        Parent parent = (Parent) node;
                                                        parent.getChildrenUnmodifiable().stream().forEach(
                                                                pn -> {
                                                                    if(pn instanceof Text) {
                                                                        Text t = (Text) pn;
                                                                        t.setStyle(LookAndFeelSet.PIECHART_LEGENF_STYLE);
                                                                    }
                                                                }
                                                        );

                                                    }

                                                }
                                        );
                                    }
                            );

                    caption.toFront();

                    for (final PieChart.Data data : chart.getData()) {

                        data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED,
                                new EventHandler<MouseEvent>() {
                                    @Override public void handle(MouseEvent e) {
                                        if (e.getTarget() instanceof Region) {
                                            Region reg = (Region) e.getTarget();
                                            caption.setLayoutX( e.getSceneX() -300  );
                                            caption.setLayoutY( e.getSceneY() - 90  );

                                            caption.setText( data.getPieValue() + " lines " );


                                        }

                                    }
                                });

                        data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                                new EventHandler<MouseEvent>() {
                                    @Override public void handle(MouseEvent e) {
                                        caption.setText("");
                                    }
                                });
                    }


                }


        );

    }

}
