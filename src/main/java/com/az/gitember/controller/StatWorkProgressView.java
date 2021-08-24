package com.az.gitember.controller;

import com.az.gitember.service.Context;
import javafx.fxml.Initializable;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

public class StatWorkProgressView implements Initializable {

    public StackedBarChart chart;
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


        Context.scmStatListProperty.addListener(
                (observableValue, oldValue, newValue) -> {
                    chart.getData().clear();
                    if (Context.scmStatListPropertyParam.get().isPerMonth()) {
                        perMonth(newValue);
                    } else {
                        perPeople(newValue);
                    }

                    chart.setTitle(getChartName());
                }
        );
    }

    private String getChartName() {
        String chartTitle = "Total lines ";
        if (Context.scmStatListPropertyParam.get().isDelta()) {
            chartTitle  = "Delta lines ";
        }

        if (Context.scmStatListPropertyParam.get().isPerMonth()) {
            chartTitle  += " each month ";
        } else {
            chartTitle  += " each month per people ";
        }

        return chartTitle;
    }


    private void perPeople(java.util.List<com.az.gitember.data.ScmStat> newValue) {
        newValue.forEach(
                scmStat -> {
                    XYChart.Series<String, Number> dataSeries = new XYChart.Series<String, Number>();
                    dataSeries.setName(new SimpleDateFormat("yyyy MMM").format(scmStat.getDate()));
                    scmStat.getTotalLines().entrySet().stream()
                            .sorted((es0, es1) -> es1.getKey().compareTo(es0.getKey()))
                            .forEach(es -> {
                                XYChart.Data<String, Number> chartData = new XYChart.Data<String, Number>(es.getKey(), es.getValue());
                                chartData.setNode(
                                        new StatHoverNode(es.getKey(), es.getValue())
                                );
                                dataSeries.getData().add(chartData);

                            });
                    chart.getData().add(dataSeries);
                }
        );
    }

    private void perMonth(java.util.List<com.az.gitember.data.ScmStat> newValue) {
        //Collect all names
        Set<String> names = new TreeSet<>();
        newValue.forEach(scmStat -> {
            names.addAll(scmStat.getTotalLines().keySet());
        });
        names.forEach(name -> {
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<String, Number>();
            dataSeries.setName(name);
            newValue.forEach(scmStat -> {
                if ( scmStat.getTotalLines().getOrDefault(name, 0) >= 0 ) {
                    int value  = scmStat.getTotalLines().getOrDefault(name, 0);
                    XYChart.Data<String, Number> chartData = new XYChart.Data<String, Number>(
                            new SimpleDateFormat("yyyy MMM").format(scmStat.getDate()),
                            value

                    );
                    chartData.setNode(
                            new StatHoverNode(name + " at " +new SimpleDateFormat("yyyy MMM").format(scmStat.getDate()), value)
                    );
                    dataSeries.getData().add(chartData);
                }

            });
            chart.getData().add(dataSeries);

        });
    }

}
