package com.az.gitember.data;

import com.az.gitember.data.Project;
import com.az.gitember.service.GitemberUtil;
import javafx.util.StringConverter;
import org.apache.commons.lang3.ObjectUtils;

public class ProjectConverter extends StringConverter<Project> {
    @Override
    public String toString(Project project) {
        return project == null ? "" : GitemberUtil.getFolderName(project.getProjectHomeFolder());
    }

    @Override
    public Project fromString(String s) {
        return null;
    }
}
