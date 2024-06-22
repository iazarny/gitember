package com.az.gitember.controller.main;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ProjectCellFactory implements Callback<ListView<Project>, ListCell<Project>> {
    @Override
    public ListCell<Project> call(ListView<Project> settingsListView) {
        return new ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setText(GitemberUtil.getFolderName(item.getProjectHomeFolder()));
                }
            }
        };
    }

}
