package com.az.gitember.gitlab.model.attic;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.gitlab4j.api.models.CustomAttribute;
import org.gitlab4j.api.models.Identity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FxUser {

    private StringProperty bio= new SimpleStringProperty();
    private BooleanProperty bot = new SimpleBooleanProperty();
    private BooleanProperty canCreateGroup = new SimpleBooleanProperty();
    private BooleanProperty canCreateProject = new SimpleBooleanProperty();
    private IntegerProperty colorSchemeId = new SimpleIntegerProperty();
    private ObjectProperty<Date> confirmedAt = new SimpleObjectProperty<>();
    private ObjectProperty<Date> currentSignInAt = new SimpleObjectProperty<>();
    private ObservableList<CustomAttribute> customAttributes  = FXCollections.observableArrayList(new ArrayList<>());
    private BooleanProperty external = new SimpleBooleanProperty();
    private StringProperty externUid= new SimpleStringProperty();
    private IntegerProperty extraSharedRunnersMinutesLimit = new SimpleIntegerProperty();
    private ObservableList<Identity> identities = FXCollections.observableArrayList(new ArrayList<>());
    private BooleanProperty isAdmin = new SimpleBooleanProperty();
    private ObjectProperty<Date> lastActivityOn = new SimpleObjectProperty<>();
    private ObjectProperty<Date> lastSignInAt = new SimpleObjectProperty<>();
    private StringProperty linkedin= new SimpleStringProperty();
    private StringProperty location= new SimpleStringProperty();
    private StringProperty organization= new SimpleStringProperty();
    private BooleanProperty privateProfile = new SimpleBooleanProperty();
    private IntegerProperty projectsLimit = new SimpleIntegerProperty();
    private StringProperty provider= new SimpleStringProperty();
    private StringProperty publicEmail= new SimpleStringProperty();
    private IntegerProperty sharedRunnersMinutesLimit = new SimpleIntegerProperty();
    private StringProperty skype= new SimpleStringProperty();
    private StringProperty state= new SimpleStringProperty();
    private IntegerProperty themeId = new SimpleIntegerProperty();
    private StringProperty twitter= new SimpleStringProperty();
    private BooleanProperty twoFactorEnabled = new SimpleBooleanProperty();
    private StringProperty websiteUrl= new SimpleStringProperty();
    private BooleanProperty skipConfirmation = new SimpleBooleanProperty();

    public String getBio() {
        return bio.get();
    }

    public StringProperty bioProperty() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio.set(bio);
    }

    public boolean isBot() {
        return bot.get();
    }

    public BooleanProperty botProperty() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot.set(bot);
    }

    public boolean isCanCreateGroup() {
        return canCreateGroup.get();
    }

    public BooleanProperty canCreateGroupProperty() {
        return canCreateGroup;
    }

    public void setCanCreateGroup(boolean canCreateGroup) {
        this.canCreateGroup.set(canCreateGroup);
    }

    public boolean isCanCreateProject() {
        return canCreateProject.get();
    }

    public BooleanProperty canCreateProjectProperty() {
        return canCreateProject;
    }

    public void setCanCreateProject(boolean canCreateProject) {
        this.canCreateProject.set(canCreateProject);
    }

    public int getColorSchemeId() {
        return colorSchemeId.get();
    }

    public IntegerProperty colorSchemeIdProperty() {
        return colorSchemeId;
    }

    public void setColorSchemeId(int colorSchemeId) {
        this.colorSchemeId.set(colorSchemeId);
    }

    public Date getConfirmedAt() {
        return confirmedAt.get();
    }

    public ObjectProperty<Date> confirmedAtProperty() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt.set(confirmedAt);
    }

    public Date getCurrentSignInAt() {
        return currentSignInAt.get();
    }

    public ObjectProperty<Date> currentSignInAtProperty() {
        return currentSignInAt;
    }

    public void setCurrentSignInAt(Date currentSignInAt) {
        this.currentSignInAt.set(currentSignInAt);
    }

    public ObservableList<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(ObservableList<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public boolean isExternal() {
        return external.get();
    }

    public BooleanProperty externalProperty() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external.set(external);
    }

    public String getExternUid() {
        return externUid.get();
    }

    public StringProperty externUidProperty() {
        return externUid;
    }

    public void setExternUid(String externUid) {
        this.externUid.set(externUid);
    }

    public int getExtraSharedRunnersMinutesLimit() {
        return extraSharedRunnersMinutesLimit.get();
    }

    public IntegerProperty extraSharedRunnersMinutesLimitProperty() {
        return extraSharedRunnersMinutesLimit;
    }

    public void setExtraSharedRunnersMinutesLimit(int extraSharedRunnersMinutesLimit) {
        this.extraSharedRunnersMinutesLimit.set(extraSharedRunnersMinutesLimit);
    }

    public ObservableList<Identity> getIdentities() {
        return identities;
    }

    public void setIdentities(ObservableList<Identity> identities) {
        this.identities = identities;
    }

    public boolean isIsAdmin() {
        return isAdmin.get();
    }

    public BooleanProperty isAdminProperty() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin.set(isAdmin);
    }

    public Date getLastActivityOn() {
        return lastActivityOn.get();
    }

    public ObjectProperty<Date> lastActivityOnProperty() {
        return lastActivityOn;
    }

    public void setLastActivityOn(Date lastActivityOn) {
        this.lastActivityOn.set(lastActivityOn);
    }

    public Date getLastSignInAt() {
        return lastSignInAt.get();
    }

    public ObjectProperty<Date> lastSignInAtProperty() {
        return lastSignInAt;
    }

    public void setLastSignInAt(Date lastSignInAt) {
        this.lastSignInAt.set(lastSignInAt);
    }

    public String getLinkedin() {
        return linkedin.get();
    }

    public StringProperty linkedinProperty() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin.set(linkedin);
    }

    public String getLocation() {
        return location.get();
    }

    public StringProperty locationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getOrganization() {
        return organization.get();
    }

    public StringProperty organizationProperty() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization.set(organization);
    }

    public boolean isPrivateProfile() {
        return privateProfile.get();
    }

    public BooleanProperty privateProfileProperty() {
        return privateProfile;
    }

    public void setPrivateProfile(boolean privateProfile) {
        this.privateProfile.set(privateProfile);
    }

    public int getProjectsLimit() {
        return projectsLimit.get();
    }

    public IntegerProperty projectsLimitProperty() {
        return projectsLimit;
    }

    public void setProjectsLimit(int projectsLimit) {
        this.projectsLimit.set(projectsLimit);
    }

    public String getProvider() {
        return provider.get();
    }

    public StringProperty providerProperty() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider.set(provider);
    }

    public String getPublicEmail() {
        return publicEmail.get();
    }

    public StringProperty publicEmailProperty() {
        return publicEmail;
    }

    public void setPublicEmail(String publicEmail) {
        this.publicEmail.set(publicEmail);
    }

    public int getSharedRunnersMinutesLimit() {
        return sharedRunnersMinutesLimit.get();
    }

    public IntegerProperty sharedRunnersMinutesLimitProperty() {
        return sharedRunnersMinutesLimit;
    }

    public void setSharedRunnersMinutesLimit(int sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit.set(sharedRunnersMinutesLimit);
    }

    public String getSkype() {
        return skype.get();
    }

    public StringProperty skypeProperty() {
        return skype;
    }

    public void setSkype(String skype) {
        this.skype.set(skype);
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

    public int getThemeId() {
        return themeId.get();
    }

    public IntegerProperty themeIdProperty() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId.set(themeId);
    }

    public String getTwitter() {
        return twitter.get();
    }

    public StringProperty twitterProperty() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter.set(twitter);
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled.get();
    }

    public BooleanProperty twoFactorEnabledProperty() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled.set(twoFactorEnabled);
    }

    public String getWebsiteUrl() {
        return websiteUrl.get();
    }

    public StringProperty websiteUrlProperty() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl.set(websiteUrl);
    }

    public boolean isSkipConfirmation() {
        return skipConfirmation.get();
    }

    public BooleanProperty skipConfirmationProperty() {
        return skipConfirmation;
    }

    public void setSkipConfirmation(boolean skipConfirmation) {
        this.skipConfirmation.set(skipConfirmation);
    }
}
