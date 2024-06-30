package com.az.gitember.controller.handlers;

import com.az.gitember.dialog.CreateTagDialog;
import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.data.Pair;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.transport.RefSpec;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateTagEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CreateTagEventHandler.class.getName());

    @Override
    public void handle(ActionEvent event) {

        CreateTagDialog dialog = new CreateTagDialog(
                "Tag",
                "Create tag",
                "",
                Context.getGitRepoService().isRepositoryHasRemoteUrl()
        );
        Optional<Pair<Boolean, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {

            String name = dialogResult.get().getSecond();

            try {
                 Context.getGitRepoService().createTag(name);
                 if (Context.getGitRepoService().isRepositoryHasRemoteUrl() && dialogResult.get().getFirst()) {
                     RefSpec refSpec = new RefSpec("refs/tags/" + name + ":refs/tags/" + name);
                     final RemoteRepoParameters repoParameters = new RemoteRepoParameters(Context.getCurrentProject().get());
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
                                 log.log(Level.INFO, "The " + name + " tag was pushed to " + Context.getGitRepoService().getRepositoryRemoteUrl());
                                 try {
                                     Context.updateBranches();
                                 } catch (Exception e) {
                                     log.log(Level.WARNING, "Cannot update branches ",e);
                                 }
                                 Context.updateTags();
                             },
                             o -> {
                                 handleRemoteRepositoryException(this, longTask.getException(), repoParameters, event);
                             }
                     );
                 }
                 Context.updateTags();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create tag " + name, e);
                Context.getMain().showResult("Cannot create tag "  + name,
                        ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

}
