package com.az.gitember.controller.gitlab;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.gitlab4j.api.models.Issue;

public class IssueThumbView extends VBox {

    private Issue issue;
    private HBox tagsHBox;
    private HBox idHBox;

    public IssueThumbView(Issue issue) {
        this.issue = issue;

        Label issueTitleLabel  =new Label("#" + issue.getIid() + " " + issue.getTitle());
        issueTitleLabel.setWrapText(true);

        tagsHBox = new HBox();
        //tagsHBox.setStyle("-fx-padding: 5px");
        tagsHBox.setStyle("-fx-spacing: 10");
        issue.getLabels().stream().forEach(
                tag -> {
                    Label tagLabel = new Label(tag);

                    setStyle("-fx-padding: 5px; -fx-background-color: green");

                    tagsHBox.getChildren().add(tagLabel);
                }
        );

        idHBox= new HBox();
        idHBox.setStyle("-fx-spacing: 10");
        idHBox.getChildren().add(new Label(issue.getIid().toString()));
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        idHBox.getChildren().add(region);
        idHBox.getChildren().add(new Label(issue.getAuthor().getName()));


        this.getChildren().addAll(issueTitleLabel, tagsHBox );
        //this.getChildren().addAll(new Label(issue.getAuthor().getName()) );

        setStyle("-fx-padding: 5px; -fx-background-color: darkgray");

        //setMinHeight(100);
        //setPrefHeight(100);

        setSpacing(10);

    }

    private Region getNewRegion() {
        Region region = new Region();
        region.setMinHeight(20);
       // region.setMinWidth(20);
        return region;
    }


}
