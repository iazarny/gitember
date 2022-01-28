package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;

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
    private GitLabProject gitLabProject;


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

       // openHBox.setSpacing(10);
        openHBox.setStyle("-fx-spacing: 10");
        openIssues.stream().forEach(i -> {
            openHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

        //.setSpacing(10);
        todoHBox.setStyle("-fx-spacing: 10");
        todoIssues.stream().forEach(i -> {
            todoHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

        closedHBox.setStyle("-fx-spacing: 10");
        closedIssues.stream().forEach(i -> {
            closedHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        gitLabProject = new GitLabProject(Context.getCurrentProject().getGitLabProjectId());
        try {
            setAllIssues(gitLabProject.getIssuesApi().getIssues());
        } catch (GitLabApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
