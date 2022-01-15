package com.az.gitember.gitlab.model;

import javafx.beans.property.*;

import java.util.Date;

public class FxMilestone {


    private ObjectProperty<Date> createdAt = new SimpleObjectProperty<>();
    private StringProperty description= new SimpleStringProperty();
    private ObjectProperty<Date> startDate = new SimpleObjectProperty<>();
    private ObjectProperty<Date> dueDate = new SimpleObjectProperty<>();
    private IntegerProperty id = new SimpleIntegerProperty();
    private IntegerProperty iid = new SimpleIntegerProperty();
    private IntegerProperty projectId = new SimpleIntegerProperty();
    private IntegerProperty groupId = new SimpleIntegerProperty();
    private StringProperty state= new SimpleStringProperty();
    private StringProperty title= new SimpleStringProperty();
    private ObjectProperty<Date> updatedAt = new SimpleObjectProperty<>();


    public FxMilestone() {
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

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Date getStartDate() {
        return startDate.get();
    }

    public ObjectProperty<Date> startDateProperty() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate.set(startDate);
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

    public int getProjectId() {
        return projectId.get();
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId.set(projectId);
    }

    public int getGroupId() {
        return groupId.get();
    }

    public IntegerProperty groupIdProperty() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId.set(groupId);
    }

    public String getState() {
        return state.get();
    }

    public StringProperty stateProperty() {
        return state;
    }

    public void setState(String state) {
        this.state.set(state);
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

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public ObjectProperty<Date> updatedAtProperty() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt.set(updatedAt);
    }
}
