package com.az.gitember.controller.recentprojects;

import com.az.gitember.data.Project;
import com.az.gitember.service.Context;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecentProjectsController implements Initializable {

    private final static Logger log = Logger.getLogger(RecentProjectsController.class.getName());

    public VBox recentProjects;
    public VBox header;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Context.settingsProperty.addListener(observable -> {
            recentProjects.getChildren().clear();
            header.setVisible(true);
            Context.settingsProperty.get().getProjects().forEach(
                    p-> {

                        Hyperlink projectNameHl = new Hyperlink (p.getProjectHomeFolder().replace(".git", ""));
                        projectNameHl.getStyleClass().add("projectlink");
                        recentProjects.getChildren().add(projectNameHl);


                        MenuItem menuItem = new MenuItem();
                        menuItem.setText("Remove from list");
                        menuItem.setOnAction(event -> {
                            removeProjectFromList(p);
                            recentProjects.getChildren().remove(projectNameHl);
                        });
                        ContextMenu contextMenu = new ContextMenu();
                        contextMenu.getItems().add(menuItem);

                        projectNameHl.setContextMenu(contextMenu);

                        projectNameHl.setOnAction(
                                event -> {
                                    try {
                                        //Context.init(p.getProjectHomeFolder(), getClass().getName());
                                        Context.getMain().projectsCmb.getSelectionModel().select(p);
                                    } catch (Exception e) {
                                        log.log(Level.WARNING, "Cannot load project " + p.getProjectHomeFolder(), e);
                                    }
                                }
                        );

                    }
            );
        });

    }

    private void removeProjectFromList(Project p) {
        Context.settingsProperty.getValue().getProjects().remove(p);
        Context.saveSettings();
        Context.readSettings();
    }
}
