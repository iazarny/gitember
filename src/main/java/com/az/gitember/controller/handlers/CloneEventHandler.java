package com.az.gitember.controller.handlers;

import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitRepoService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloneEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CloneEventHandler.class.getName());

    private final RemoteRepoParameters params;

    public CloneEventHandler(RemoteRepoParameters params) {
        this.params = params;
    }

    @Override
    public void handle(ActionEvent event) {
        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                new GitRepoService().cloneRepository(
                        params,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }, DefaultProgressMonitor.Strategy.Step)
                );
                return null;
            }
        };
        launchLongTask(
                longTask,
                o -> {
                    {
                        try {

                            Project project = new Project();
                            project.setOpenTime(new Date());
                            project.setProjectHomeFolder(params.getDestinationFolder());
                            project.setUserName(params.getUserName());
                            project.setUserPwd(params.getUserPwd());
                            project.setKeyPass(params.getKeyPassPhrase());

                            Context.settingsProperty.get().getProjects().add(project);
                            Context.saveSettings();
                            Context.readSettings();
                            Context.init(params);

                            Context.settingsProperty.get().getProjects().stream()
                                    .filter(p -> p.getProjectHomeFolder().equalsIgnoreCase(params.getDestinationFolder()))
                                    .findFirst()
                                            .ifPresent(
                                                   p -> Context.getMain().projectsCmb.getSelectionModel().select(p)
                                            );

                            log.log(Level.INFO, "Git project  "  + params.getUrl() + "  cloned to" + params.getDestinationFolder());
                            Context.getMain().showResult("Repository", "Repository is cloned", Alert.AlertType.INFORMATION);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Cannot open " + params.getDestinationFolder(), e);
                            Context.getMain().showResult("Repository", "Cannot open \n" + ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
                        }

                    }
                },
                o -> {
                    log.log(Level.WARNING, "Cannot clone " + params.getUrl(), o.getSource().getException());
                    Context.getMain().showResult("Repository", "Cannot clone \n   " + ExceptionUtils.getStackTrace(o.getSource().getException()),
                            Alert.AlertType.ERROR);
                }
        );
    }
}
