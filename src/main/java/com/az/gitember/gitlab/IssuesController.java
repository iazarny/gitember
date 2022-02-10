package com.az.gitember.gitlab;

import com.az.gitember.controller.PostInitializable;
import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.gitlab4j.api.Constants;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class IssuesController implements PostInitializable  {

    private final static Logger log = Logger.getLogger(IssuesController.class.getName());
    public VBox openHBox;
    public VBox todoHBox;
    public VBox closedHBox;

    private List<FxIssue> allIssues;
    private ObservableList<FxIssue> openIssues = FXCollections.observableList(new ArrayList<>());
    private ObservableList<FxIssue> todoIssues = FXCollections.observableList(new ArrayList<>());
    private ObservableList<FxIssue> closedIssues = FXCollections.observableList(new ArrayList<>());

    public void setAllIssues(List<FxIssue> allIssues) {
        final GitLabProject gitLabProject = Context.getGitLabProject();
        this.allIssues = allIssues;
        allIssues.stream().filter(issue -> {
            return issue.getState() == Constants.IssueState.CLOSED.toValue();
        }).forEach(i -> {
            closedIssues.add(i);
        });
        allIssues.stream().filter(issue -> {
            return issue.getState() == Constants.IssueState.REOPENED.toValue();
        }).forEach(i -> {
            openIssues.add(i);
        });
        allIssues.stream().filter(issue -> {
            return issue.getState() == Constants.IssueState.OPENED.toValue()
                    && !GitemberUtil.lstContains(issue.getLabels(), "to do");
        }).forEach(i -> {
            openIssues.add(i);
        });
        allIssues.stream().filter(issue -> {
            return issue.getState() == Constants.IssueState.OPENED.toValue()
                    && GitemberUtil.lstContains(issue.getLabels(), "to do");
        }).forEach(i -> {
            todoIssues.add(i);
        });

        openIssues.stream().forEach(i -> {
            openHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

        todoIssues.stream().forEach(i -> {
            todoHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

        closedIssues.stream().forEach(i -> {
            closedHBox.getChildren().add(new IssueThumbView(gitLabProject, i));
        });

    }

    @Override
    public void postInitialize(Stage stage) {

        stage.getScene().setCursor(Cursor.WAIT);

        Task<List<FxIssue>> longTask = new Task<List<FxIssue>>() {
            @Override
            protected List<FxIssue> call() throws Exception {

                List<FxIssue> fxissues = Context.getGitLabProject().getIssuesApi().getIssues().stream()
                        .map(FxIssue::new).collect(Collectors.toList());
                return fxissues;
            }
        };

        longTask.setOnFailed(event -> {
            log.log(Level.WARNING,
                    MessageFormat.format("Get gitlab issues error", event.getSource().getException()));
            setAllIssues(Collections.EMPTY_LIST);
            stage.getScene().setCursor(Cursor.DEFAULT);
        });

        longTask.setOnSucceeded(event -> {
            setAllIssues((List<FxIssue>) event.getSource().getValue());
            stage.getScene().setCursor(Cursor.DEFAULT);
        });

        new Thread(longTask).start();

    }
}
