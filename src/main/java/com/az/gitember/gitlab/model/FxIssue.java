package com.az.gitember.gitlab.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.models.*;

import java.util.ArrayList;
import java.util.Date;

public class FxIssue {

    private ObjectProperty<Assignee> assignee = new SimpleObjectProperty<>();
    private ObservableList<Assignee> assignees = FXCollections.observableArrayList(new ArrayList<>());
    private ObjectProperty<Author> author = new SimpleObjectProperty<>();
    private BooleanProperty confidential = new SimpleBooleanProperty();
    private ObjectProperty<Date> createdAt = new SimpleObjectProperty<>();
    private ObjectProperty<Date> updatedAt = new SimpleObjectProperty<>();
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

    public Author getAuthor() {
        return author.get();
    }

    public ObjectProperty<Author> authorProperty() {
        return author;
    }

    public void setAuthor(Author author) {
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

    public Date getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<Date> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt.set(createdAt);
    }

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public ObjectProperty<Date> updatedAtProperty() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
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
