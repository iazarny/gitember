package com.az.gitember.controller;

import com.az.gitember.service.Context;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainRecentProjects implements Initializable {

    private final static Logger log = Logger.getLogger(MainRecentProjects.class.getName());

    public VBox recentProjects;
    public VBox header;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Context.settingsProperty.addListener(observable -> {
            recentProjects.getChildren().clear();
            header.setVisible(true);
            Context.settingsProperty.get().getProjects().forEach(
                    p-> {

                        Hyperlink projectNameHl = new Hyperlink (p.getProjectHomeFolder());
                        projectNameHl.getStyleClass().add("projectlink"); //TODO
                        recentProjects.getChildren().add(projectNameHl);
                        projectNameHl.setOnAction(
                                event -> {
                                    try {
                                        Context.init(p.getProjectHomeFolder());
                                    } catch (Exception e) {
                                        Context.settingsProperty.getValue().getProjects().remove(p);
                                        Context.saveSettings();
                                        Context.readSettings();
                                        Context.getMain().showResult("Cannot load project ", "Cannot load project " + p.getProjectHomeFolder() + "\n   It will be removed from the list of recent projects", Alert.AlertType.WARNING);
                                        log.log(Level.WARNING, "Cannot load project {0}. {1}", new String[] {p.getProjectHomeFolder(), e.getMessage()});
                                    }
                                }
                        );

                    }
            );
        });

    }
}
