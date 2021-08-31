package com.az.gitember.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class Project implements Serializable, Comparable<Project>  {

    private String projectHomeFolder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;
    private String userName;

    protected String empId;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String userPwd;
    private String userKey;
    @JsonDeserialize(using = MaskStringValueDeSerializer.class)
    @JsonSerialize(using = MaskStringValueSerializer.class)
    private String keyPass;
    private String userCommitName;
    private String userCommitEmail;


    public Project() {
    }

    public Project(String projectHomeFolder, Date openTime) {
        this.projectHomeFolder = projectHomeFolder;
        this.openTime = openTime;
    }

    public String getUserCommitName() {
        return userCommitName;
    }

    public void setUserCommitName(String userCommitName) {
        this.userCommitName = userCommitName;
    }

    public String getUserCommitEmail() {
        return userCommitEmail;
    }

    public void setUserCommitEmail(String userCommitEmail) {
        this.userCommitEmail = userCommitEmail;
    }

    public String getProjectHomeFolder() {
        return projectHomeFolder;
    }

    public void setProjectHomeFolder(String projectHomeFolder) {
        this.projectHomeFolder = projectHomeFolder;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    @Override
    public int compareTo(Project o) {
        return o.getProjectHomeFolder().compareTo(this.getProjectHomeFolder());
    }

    @Override
    public int hashCode() {
        return getProjectHomeFolder().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ((Project)obj).getProjectHomeFolder().equals(getProjectHomeFolder());
    }
}
