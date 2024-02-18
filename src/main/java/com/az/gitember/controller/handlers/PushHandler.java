package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.controller.Main;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.eclipse.jgit.transport.RefSpec;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PushHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(PushHandler.class.getName());

    private final ScmBranch scmBranch;

    public PushHandler(ScmBranch branchItem) {
        this.scmBranch = branchItem;
    }

    @Override
    public void handle(ActionEvent event) {

        if (scmBranch.getRemoteMergeName() == null) {

            final TextInputDialog dialog = new TextInputDialog(scmBranch.getShortName());
            dialog.setTitle("Branch name");
            dialog.setHeaderText("Remote branch will be created");
            dialog.setContentText("Please enter new remote branch name:");
            dialog.initOwner(App.getScene().getWindow());

            Optional<String> dialogResult = dialog.showAndWait();
            if (dialogResult.isPresent()) {

                scmBranch.setRemoteName(dialogResult.get());
                try {
                    String remoteMergeName = Context.getGitRepoService().trackRemote(scmBranch.getShortName(), dialogResult.get());
                    scmBranch.setRemoteMergeName(remoteMergeName);
                    log.log(Level.INFO, "Set track remote ok " + scmBranch);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot track " + scmBranch , e);
                    return;
                }
            } else {
                return;
            }
        }

        final RemoteRepoParameters repoParameters = new RemoteRepoParameters(Context.getCurrentProject());
        final RefSpec refSpec = new RefSpec(scmBranch.getFullName() + ":" + scmBranch.getRemoteMergeName());
        final Task<String> longTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return  Context.getGitRepoService().remoteRepositoryPush(
                        repoParameters,
                        refSpec,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }, DefaultProgressMonitor.Strategy.Step)
                );
            }
        };

        launchLongTask(
                longTask,
                o -> {
                    Context.saveSettings();
                    Context.updateAll();
                    Context.updateWorkingBranch();
                    Main.showResult("Repository",
                            "Repository push " + scmBranch.getRemoteMergeName() + " \n"
                                    + longTask.getValue(), Alert.AlertType.INFORMATION);
                    log.log(Level.INFO, "Push  ok " + scmBranch);
                },
                o -> {
                    log.log(Level.SEVERE, "Cannot push " + scmBranch , o.getSource().getException());
                    handleRemoteRepositoryException(this, longTask.getException(), repoParameters, event);
                }
        );


    }

}
