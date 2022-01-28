package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.data.Const;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gitlab4j.api.models.Issue;

import java.util.List;

public class IssueThumbView extends VBox {

    private final Issue issue;
    private final GitLabProject glProject;

    private HBox tagsHBox;
    private HBox idHBox;




    public IssueThumbView(final GitLabProject glProject , final Issue issue) {
        this.issue = issue;
        this.glProject = glProject;

        Hyperlink issueTitleLabel  = new Hyperlink("#" + issue.getIid() + " " + issue.getTitle());
        issueTitleLabel.setWrapText(true);

        tagsHBox = new HBox();
        tagsHBox.setStyle("-fx-spacing: 10");
        GitLabUtil.fillContainerWithLabels(tagsHBox, glProject.getProjectLabels(), issue.getLabels());


        idHBox= new HBox();
        idHBox.setStyle("-fx-spacing: 10");
        if (issue.getAssignee() != null) {
            idHBox.getChildren().add(new Label(issue.getAssignee().getName()));
        }

        this.getChildren().addAll(issueTitleLabel, tagsHBox, idHBox );

        setStyle("-fx-padding: 5px; -fx-background-color: #121212");

        setSpacing(10);

        issueTitleLabel.setOnAction(actionEvent -> {

            try {
                IssueDetailController controller =
                        (IssueDetailController) App.loadFXMLToNewStage(Const.View.GitLab.ISSUE_DETAILS_VIEW, "Issue details").getSecond();
                controller.setIssue(glProject, issue);
            } catch (Exception e) {
               Context.getMain().showResult("Error", ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
            }
        });

    }

    private Region getNewRegion() {
        Region region = new Region();
        region.setMinHeight(20);
       // region.setMinWidth(20);
        return region;
    }


}
