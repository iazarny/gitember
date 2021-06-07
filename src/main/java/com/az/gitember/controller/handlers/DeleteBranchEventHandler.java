package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.transport.RefSpec;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteBranchEventHandler extends AbstractLontTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(DeleteBranchEventHandler.class.getName());

    private final ScmBranch branchItem;

    public DeleteBranchEventHandler(ScmBranch branchItem) {
        this.branchItem = branchItem;
    }

    @Override
    public void handle(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setWidth(800);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setTitle("Question");
        alert.setContentText("Would you like to delete  " +  branchItem.getBranchType().getTypeName()+ " " + branchItem.getShortName() + " ?");
        alert.initOwner(App.getScene().getWindow());

        alert.showAndWait().ifPresent( r -> {
            if (r == ButtonType.OK) {
                try {
                    Context.getGitRepoService().deleteLocalBranch(branchItem.getFullName());


                    if (branchItem.getBranchType() == ScmBranch.BranchType.REMOTE
                            ||  (branchItem.getBranchType() == ScmBranch.BranchType.TAG && Context.getGitRepoService().isRepositoryHasRemoteUrl())) {
                        final String destPrefix;
                        if (branchItem.getBranchType() == ScmBranch.BranchType.REMOTE) {
                            destPrefix = "refs/heads/";
                        } else {
                            destPrefix = "";
                        }
                        RefSpec refSpec = new RefSpec().setSource(null).setDestination(destPrefix + branchItem.getShortName());
                        final RemoteRepoParameters repoParameters = new RemoteRepoParameters(Context.getCurrentProject());
                        Task<String> longTask = new Task<String>() {
                            @Override
                            protected String call() throws Exception {
                                return  Context.getGitRepoService().remoteRepositoryPush(
                                        repoParameters,
                                        refSpec,
                                        new DefaultProgressMonitor((t, d) -> {
                                            updateTitle(t);
                                            updateProgress(d, 1.0);
                                        })
                                );
                            }
                        };


                        launchLongTask(
                                longTask,
                                o -> {
                                    log.log(Level.INFO, "The " + branchItem.toString() + " was pushed to " + Context.getGitRepoService().getRepositoryRemoteUrl());
                                    try {
                                        Context.updateBranches();
                                    } catch (Exception e) {
                                        log.log(Level.SEVERE, "Cannot updaet branches ", e);
                                        Context.getMain().showResult("Repository", "Cannot update branch \n" + ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);

                                    }
                                    Context.updateTags();
                                },
                                o -> {
                                    handleRemoteRepositoryException(this, longTask.getException(), repoParameters, event);
                                }
                        );



                    } else {
                        Context.updateBranches();
                        Context.updateTags();
                    }




                } catch (Exception e) {
                    log.log(Level.SEVERE, "Cannot delete " + branchItem.getBranchType().getTypeName() + " " +
                            branchItem.getFullName() , e);
                    Context.getMain().showResult("Cannot delete " + branchItem.getFullName(),
                            ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
                }
            }

        } );



    }


}
