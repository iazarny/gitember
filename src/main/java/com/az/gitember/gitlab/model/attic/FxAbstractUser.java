package com.az.gitember.gitlab.model.attic;

import javafx.beans.property.*;
import org.gitlab4j.api.models.AbstractUser;

import java.util.Date;

public class FxAbstractUser {

    private StringProperty avatarUrl = new SimpleStringProperty();
    private ObjectProperty<Date> createdAt = new SimpleObjectProperty<>();
    private StringProperty email = new SimpleStringProperty();
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty state = new SimpleStringProperty();
    private StringProperty username = new SimpleStringProperty();
    private StringProperty webUrl = new SimpleStringProperty();

    public FxAbstractUser(final AbstractUser user) {
        setAvatarUrl(user.getAvatarUrl());
        createdAt.set(user.getCreatedAt());
        setEmail(user.getEmail());
        setId(user.getId());
        setName(user.getName());
        setState(user.getState());
        setUsername(user.getUsername());
        setWebUrl(user.getWebUrl());
    }

    public FxAbstractUser() {
    }

    public String getAvatarUrl() {
        return avatarUrl.get();
    }

    public StringProperty avatarUrlProperty() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl.set(avatarUrl);
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

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
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

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
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
}
