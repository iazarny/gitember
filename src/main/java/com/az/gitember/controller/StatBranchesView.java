package com.az.gitember.controller;

import com.az.gitember.service.Context;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class StatBranchesView implements Initializable {

    public LineChart chart;
    public BorderPane mainBorderPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        mainBorderPanel.widthProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    chart.setMinWidth(newValue.doubleValue() - 20);
                }
        );
        mainBorderPanel.heightProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    chart.setMinHeight(newValue.doubleValue() - 20);
                }
        );

        Context.scmStatBranchLiveTimeProperty.addListener(
                (observable, oldValue, newValue) -> {
                    chart.getData().clear();

                    XYChart.Series series = new XYChart.Series();
                    series.setName("Branches quantity");

                    newValue.forEach(
                            blt -> {
                                float avg = 0;
                                if (blt.getQty() != 0) {
                                    avg = blt.getQty();
                                }
                                XYChart.Data data = new XYChart.Data( blt.getDate(), avg );
                                data.setNode(
                                        new StatHoverNode(
                                                "", (int) avg, 25, 20
                                        )
                                );
                                series.getData().add(data);
                            }
                    );

                    chart.getData().add(series);

                }
        );

    }

}
