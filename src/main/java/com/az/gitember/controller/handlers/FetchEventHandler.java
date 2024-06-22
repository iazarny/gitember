package com.az.gitember.controller.handlers;

import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FetchEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(FetchEventHandler.class.getName());


    private final RemoteRepoParameters repoParameters;
    private final String  branchName;

    public FetchEventHandler(RemoteRepoParameters repoParameters, String branchName) {
        this.repoParameters = repoParameters;
        this.branchName = branchName;
    }

    @Override
    public void handle(ActionEvent event)  {
        Task<String> longTask = new Task<String>() {

            @Override
            protected String call() throws Exception {
                return Context.getGitRepoService().remoteRepositoryFetch (
                        repoParameters,
                        branchName,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }
                        ));
            }
        };

        launchLongTask(
                longTask,
                o -> {
                    {
                        Context.saveSettings();
                        Context.updateAll();
                        Context.getMain().showResult("Repository", "Fetch ok\n" + o.getSource().getValue(), Alert.AlertType.INFORMATION);
                    }
                },
                o -> {
                    log.log(Level.WARNING,
                            MessageFormat.format("Fetch. error", o.getSource().getException()));
                    Context.getMain().showResult("Repository", "Cannot fetch\n" + o.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                }
        );
    }
}
