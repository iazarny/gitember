package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.gitlab.model.FxIssue;
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

    private final FxIssue fxissue;
    private final GitLabProject glProject;

    private HBox tagsHBox;
    private HBox idHBox;




    public IssueThumbView(final GitLabProject glProject , final FxIssue fxissue) {
        this.fxissue = fxissue;
        this.glProject = glProject;

        Hyperlink issueTitleLabel  = new Hyperlink("#" + fxissue.getIid() + " " + fxissue.getTitle());
        issueTitleLabel.setWrapText(true);

        tagsHBox = new HBox();
        tagsHBox.setStyle("-fx-spacing: 10");
        GitLabUtil.fillContainerWithLabels(tagsHBox, glProject.getProjectLabels(), fxissue.getLabels());


        idHBox= new HBox();
        idHBox.setStyle("-fx-spacing: 10");
        if (fxissue.getAssignee() != null) {
            idHBox.getChildren().add(new Label(fxissue.getAssignee().getName()));
        }

        this.getChildren().addAll(issueTitleLabel, tagsHBox, idHBox );

        setStyle(LookAndFeelSet.ISSUE_THUMB_STTYLE);

        setSpacing(10);

        issueTitleLabel.setOnAction(actionEvent -> {

            try {
                IssueDetailController controller =
                        (IssueDetailController) App.loadFXMLToNewStage(Const.View.GitLab.ISSUE_DETAILS_VIEW, "Issue details").getSecond();
                controller.setIssue(glProject, fxissue);
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
