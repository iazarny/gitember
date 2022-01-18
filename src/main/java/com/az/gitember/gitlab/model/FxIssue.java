package com.az.gitember.gitlab.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.models.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class FxIssue {

    private ObjectProperty<Assignee> assignee = new SimpleObjectProperty<>();
    private ObservableList<Assignee> assignees = FXCollections.observableArrayList(new ArrayList<>());
    private StringProperty author = new SimpleStringProperty();
    private BooleanProperty confidential = new SimpleBooleanProperty();
    private ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDate> updatedAt = new SimpleObjectProperty<>();
    private ObjectProperty<Date> closedAt = new SimpleObjectProperty<>();
    private ObjectProperty<User> closedBy = new SimpleObjectProperty<>();
    private StringProperty description = new SimpleStringProperty();
    private ObjectProperty<Date> dueDate= new SimpleObjectProperty<>();

    private StringProperty actualId = new SimpleStringProperty();
    private StringProperty externalId = new SimpleStringProperty();
    private IntegerProperty id = new SimpleIntegerProperty();

    private IntegerProperty iid = new SimpleIntegerProperty();
    private IntegerProperty issueLinkId = new SimpleIntegerProperty();
    private ObservableList<String> labels = FXCollections.observableArrayList();
    private ObjectProperty<Milestone> milestone= new SimpleObjectProperty<>();
    private IntegerProperty projectId = new SimpleIntegerProperty();
    private ObjectProperty<Constants.IssueState> state= new SimpleObjectProperty<>();
    private BooleanProperty subscribed = new SimpleBooleanProperty();
    private StringProperty title= new SimpleStringProperty();
    private IntegerProperty userNotesCount = new SimpleIntegerProperty();
    private StringProperty webUrl= new SimpleStringProperty();
    private IntegerProperty weight = new SimpleIntegerProperty();
    private BooleanProperty discussionLocked = new SimpleBooleanProperty();
    private ObjectProperty<TimeStats> timeStats = new SimpleObjectProperty();

    private IntegerProperty upvotes = new SimpleIntegerProperty();
    private IntegerProperty downvotes = new SimpleIntegerProperty();
    private IntegerProperty mergeRequestsCount = new SimpleIntegerProperty();
    private BooleanProperty hasTasks = new SimpleBooleanProperty();
    private StringProperty taskStatus= new SimpleStringProperty();
    private ObjectProperty<Issue.TaskCompletionStatus> taskCompletionStatus = new SimpleObjectProperty<>();


    public FxIssue(final Issue issue) {
        this.assignee.setValue(issue.getAssignee());
        this.assignees.setAll(issue.getAssignees());
        this.author.setValue(issue.getAuthor().getName());

        this.confidential.setValue(issue.getConfidential());
        this.createdAt.setValue(issue.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        this.updatedAt.setValue(issue.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        this.closedAt.setValue(issue.getClosedAt());
        this.closedBy.setValue(issue.getClosedBy());
        this.description.setValue(issue.getDescription());
        this.dueDate.setValue(issue.getDueDate());
        this.actualId.setValue(issue.getActualId().asText());
        this.externalId.setValue(issue.getExternalId());
        this.id.setValue(issue.getId());
        this.iid.setValue(issue.getIid());
        this.issueLinkId.setValue(issue.getIssueLinkId());
        this.labels.setAll(issue.getLabels());
        this.milestone.setValue(issue.getMilestone());
        this.projectId.setValue(issue.getProjectId());
        this.state.setValue(issue.getState());
        this.subscribed.setValue(issue.getSubscribed());
        this.title.setValue(issue.getTitle());
        this.userNotesCount.setValue(issue.getUserNotesCount());
        this.webUrl.setValue(issue.getWebUrl());
        this.weight.setValue(issue.getWeight());
        this.discussionLocked.setValue(issue.getDiscussionLocked());
        this.timeStats.setValue(issue.getTimeStats());
        this.upvotes.setValue(issue.getUpvotes());
        this.downvotes.setValue(issue.getDownvotes());
        this.mergeRequestsCount.setValue(issue.getMergeRequestsCount());
        this.hasTasks.setValue(issue.getHasTasks());
        this.taskStatus.setValue(issue.getTaskStatus());
        this.taskCompletionStatus.setValue(issue.getTaskCompletionStatus());


    }

    public FxIssue() {
    }

    public Assignee getAssignee() {
        return assignee.get();
    }

    public ObjectProperty<Assignee> assigneeProperty() {
        return assignee;
    }

    public void setAssignee(Assignee assignee) {
        this.assignee.set(assignee);
    }

    public ObservableList<Assignee> getAssignees() {
        return assignees;
    }

    public void setAssignees(ObservableList<Assignee> assignees) {
        this.assignees = assignees;
    }

    public String getAuthor() {
        return author.get();
    }

    public StringProperty authorProperty() {
        return author;
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public boolean isConfidential() {
        return confidential.get();
    }

    public BooleanProperty confidentialProperty() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential.set(confidential);
    }

    public LocalDate getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<LocalDate> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt.set(createdAt);
    }

    public LocalDate getUpdatedAt() {
        return updatedAt.get();
    }

    public ObjectProperty<LocalDate> updatedAtProperty() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public Date getClosedAt() {
        return closedAt.get();
    }

    public ObjectProperty<Date> closedAtProperty() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt.set(closedAt);
    }

    public User getClosedBy() {
        return closedBy.get();
    }

    public ObjectProperty<User> closedByProperty() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy.set(closedBy);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Date getDueDate() {
        return dueDate.get();
    }

    public ObjectProperty<Date> dueDateProperty() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate.set(dueDate);
    }

    public String getActualId() {
        return actualId.get();
    }

    public StringProperty actualIdProperty() {
        return actualId;
    }

    public void setActualId(String actualId) {
        this.actualId.set(actualId);
    }

    public String getExternalId() {
        return externalId.get();
    }

    public StringProperty externalIdProperty() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId.set(externalId);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getIid() {
        return iid.get();
    }

    public IntegerProperty iidProperty() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid.set(iid);
    }

    public int getIssueLinkId() {
        return issueLinkId.get();
    }

    public IntegerProperty issueLinkIdProperty() {
        return issueLinkId;
    }

    public void setIssueLinkId(int issueLinkId) {
        this.issueLinkId.set(issueLinkId);
    }

    public ObservableList<String> getLabels() {
        return labels;
    }

    public void setLabels(ObservableList<String> labels) {
        this.labels = labels;
    }

    public Milestone getMilestone() {
        return milestone.get();
    }

    public ObjectProperty<Milestone> milestoneProperty() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone.set(milestone);
    }

    public int getProjectId() {
        return projectId.get();
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId.set(projectId);
    }

    public Constants.IssueState getState() {
        return state.get();
    }

    public ObjectProperty<Constants.IssueState> stateProperty() {
        return state;
    }

    public void setState(Constants.IssueState state) {
        this.state.set(state);
    }

    public boolean isSubscribed() {
        return subscribed.get();
    }

    public BooleanProperty subscribedProperty() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed.set(subscribed);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public int getUserNotesCount() {
        return userNotesCount.get();
    }

    public IntegerProperty userNotesCountProperty() {
        return userNotesCount;
    }

    public void setUserNotesCount(int userNotesCount) {
        this.userNotesCount.set(userNotesCount);
    }

    public String getWebUrl() {
        return webUrl.get();
    }

    public StringProperty webUrlProperty() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl.set(webUrl);
    }

    public int getWeight() {
        return weight.get();
    }

    public IntegerProperty weightProperty() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight.set(weight);
    }

    public boolean isDiscussionLocked() {
        return discussionLocked.get();
    }

    public BooleanProperty discussionLockedProperty() {
        return discussionLocked;
    }

    public void setDiscussionLocked(boolean discussionLocked) {
        this.discussionLocked.set(discussionLocked);
    }

    public TimeStats getTimeStats() {
        return timeStats.get();
    }

    public ObjectProperty<TimeStats> timeStatsProperty() {
        return timeStats;
    }

    public void setTimeStats(TimeStats timeStats) {
        this.timeStats.set(timeStats);
    }

    public int getUpvotes() {
        return upvotes.get();
    }

    public IntegerProperty upvotesProperty() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes.set(upvotes);
    }

    public int getDownvotes() {
        return downvotes.get();
    }

    public IntegerProperty downvotesProperty() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes.set(downvotes);
    }

    public int getMergeRequestsCount() {
        return mergeRequestsCount.get();
    }

    public IntegerProperty mergeRequestsCountProperty() {
        return mergeRequestsCount;
    }

    public void setMergeRequestsCount(int mergeRequestsCount) {
        this.mergeRequestsCount.set(mergeRequestsCount);
    }

    public boolean isHasTasks() {
        return hasTasks.get();
    }

    public BooleanProperty hasTasksProperty() {
        return hasTasks;
    }

    public void setHasTasks(boolean hasTasks) {
        this.hasTasks.set(hasTasks);
    }

    public String getTaskStatus() {
        return taskStatus.get();
    }

    public StringProperty taskStatusProperty() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus.set(taskStatus);
    }

    public Issue.TaskCompletionStatus getTaskCompletionStatus() {
        return taskCompletionStatus.get();
    }

    public ObjectProperty<Issue.TaskCompletionStatus> taskCompletionStatusProperty() {
        return taskCompletionStatus;
    }

    public void setTaskCompletionStatus(Issue.TaskCompletionStatus taskCompletionStatus) {
        this.taskCompletionStatus.set(taskCompletionStatus);
    }
}