package com.az.gitember.controller.gitlab;

import com.az.gitember.App;
import com.az.gitember.controller.HistoryDetail;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class IssuesController  implements Initializable {

    private final static Logger log = Logger.getLogger(IssuesController.class.getName());
    public VBox openHBox;
    public VBox todoHBox;
    public VBox closedHBox;


    private List<Issue> allIssues;
    private ObservableList<Issue> openIssues =  FXCollections.observableList(new ArrayList<>());
    private ObservableList<Issue> todoIssues = FXCollections.observableList(new ArrayList<>());
    private ObservableList<Issue> closedIssues =  FXCollections.observableList(new ArrayList<>());

    public void setAllIssues(List<Issue> allIssues) {
        this.allIssues = allIssues;
        allIssues.stream().filter( issue -> { return issue.getState() == Constants.IssueState.CLOSED; } ).forEach( i -> {closedIssues.add(i);});
        allIssues.stream().filter( issue -> { return issue.getState() == Constants.IssueState.REOPENED; } ).forEach( i -> {openIssues.add(i);});
        allIssues.stream().filter( issue -> { return issue.getState() == Constants.IssueState.OPENED
                && !GitemberUtil.lstContains(issue.getLabels(), "to do"); } ).forEach( i -> {openIssues.add(i);});
        allIssues.stream().filter( issue -> { return issue.getState() == Constants.IssueState.OPENED
                && GitemberUtil.lstContains(issue.getLabels(), "to do"); } ).forEach( i -> {todoIssues.add(i);});

        openIssues.stream().forEach(
                i -> {

                    openHBox.getChildren().add(new IssueThumbView(i));

                    /*try {
                        Pair<Parent, Object> thumbPair = App.loadFXMLToNewStage(Const.View.GitLab.ISSUE_THUMB_VIEW, "");
                        IssueThumbController thumbController = (IssueThumbController) thumbPair.getSecond();
                        Parent thumbParent = thumbPair.getFirst();
                        openHBox.getChildren().add(thumbParent);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                }
        );

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GitLabApi glAp = new GitLabApi(GitemberUtil.getServer(Context.getGitRepoService().getRepositoryRemoteUrl()), getAccessToken());
        IssuesApi issuesApi = glAp.getIssuesApi();
        try {
            setAllIssues(issuesApi.getIssues());
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
    }

    private String getAccessToken() {
        return Context.settingsProperty.get().getGitlabSettings().getAccessToken();
    }
}
