package com.az.gitember.controller.gitlab;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.gitlab4j.api.models.Issue;

public class IssueThumbView extends VBox {

    private Issue issue;
    private HBox tagsHBox;
    private HBox idHBox;

    public IssueThumbView(Issue issue) {
        this.issue = issue;
        this.getChildren().add(new Label(issue.getTitle()));


    }


}
