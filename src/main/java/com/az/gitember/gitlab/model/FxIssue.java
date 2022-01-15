package com.az.gitember.gitlab.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ValueNode;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.models.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FxIssue {

    private ObjectProperty<FxAssignee> assignee = new SimpleObjectProperty<>();
    private ObservableList<FxAssignee> assignees = FXCollections.observableArrayList(new ArrayList<>());
    private ObjectProperty<FxAuthor> author = new SimpleObjectProperty<>();
    private BooleanProperty confidential = new SimpleBooleanProperty();
    private ObjectProperty<Date> createdAt = new SimpleObjectProperty<>();
    private ObjectProperty<Date> updatedAt = new SimpleObjectProperty<>();
    private ObjectProperty<Date> closedAt = new SimpleObjectProperty<>();
    private ObjectProperty<FxUser> closedBy = new SimpleObjectProperty<>();
    private StringProperty description = new SimpleStringProperty();
    private ObjectProperty<Date> dueDate= new SimpleObjectProperty<>();

    private StringProperty actualId = new SimpleStringProperty();
    private StringProperty externalId = new SimpleStringProperty();
    private IntegerProperty id = new SimpleIntegerProperty();

    private IntegerProperty iid = new SimpleIntegerProperty();
    private IntegerProperty issueLinkId = new SimpleIntegerProperty();
    private ObservableList<String> labels = FXCollections.observableArrayList();
    private Milestone milestone;
    private IntegerProperty projectId = new SimpleIntegerProperty();
    private Constants.IssueState state;
    private Boolean subscribed;
    private StringProperty title;
    private IntegerProperty userNotesCount = new SimpleIntegerProperty();
    private StringProperty webUrl;
    private IntegerProperty weight = new SimpleIntegerProperty();
    private Boolean discussionLocked;
    private TimeStats timeStats;

    private IntegerProperty upvotes = new SimpleIntegerProperty();
    private IntegerProperty downvotes = new SimpleIntegerProperty();
    private IntegerProperty mergeRequestsCount = new SimpleIntegerProperty();
    private Boolean hasTasks;
    private StringProperty taskStatus;
    private Issue.TaskCompletionStatus taskCompletionStatus;

}
