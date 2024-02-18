package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PullHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(PullHandler.class.getName());

    private final ScmBranch scmBranch;

    public PullHandler(ScmBranch scmBranch) {
        this.scmBranch = scmBranch;
    }

    @Override
    public void handle(ActionEvent event) {

        String branchName = scmBranch.getRemoteMergeName();
        RemoteRepoParameters repoParameters = new RemoteRepoParameters(Context.getCurrentProject());
        Task<String> longTask = new Task<String>() {

            @Override
            protected String call() throws Exception {
                return Context.getGitRepoService().remoteRepositoryPull(
                        repoParameters,
                        branchName,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }, DefaultProgressMonitor.Strategy.Step
                        ));
            }
        };

        launchLongTask(
                longTask,
                o -> {
                    {
                        Context.saveSettings();
                        Context.updateAll();
                        Context.updateWorkingBranch();
                        Context.getMain().showResult("Repository",
                                "Pull ok\n" + o.getSource().getValue(),
                                Alert.AlertType.INFORMATION);
                    }
                },
                o -> {
                    log.log(Level.WARNING,
                            MessageFormat.format("Pull. Fail {0} ", o.getSource().getException().getMessage()), o.getSource().getException());
                    //Context.getMain().showResult("Repository", "Cannot pull\n" + o.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                    handleRemoteRepositoryException(this, longTask.getException(), repoParameters, event);
                }
        );

    }

}
