package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.dialog.LoginDialog;
import com.az.gitember.data.Project;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.TransportException;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractLongTaskEventHandler {

    private final static Logger log = Logger.getLogger(AbstractLongTaskEventHandler.class.getName());

    protected void launchLongTask(final Task longTask, final Consumer<WorkerStateEvent> onOk, final Consumer<WorkerStateEvent> onError) {
        Context.getMain().toolBar.setVisible(true);

        Context.getMain().progressBar.progressProperty().bind(longTask.progressProperty());

        Context.getMain().operationName.textProperty().bind(longTask.titleProperty());
        App.getStage().getScene().setCursor(Cursor.WAIT);
        longTask.setOnSucceeded(new LongTaskFinishHandler(App.getStage().getScene(),
                Context.getMain().progressBar, Context.getMain().operationName, Context.getMain().toolBar, onOk));
        longTask.setOnFailed(new LongTaskFinishHandler(App.getStage().getScene(),
                Context.getMain().progressBar, Context.getMain().operationName, Context.getMain().toolBar, onError));
        new Thread(longTask).start();
    }

    protected void handleRemoteRepositoryException(final EventHandler handler, final Throwable exception, final RemoteRepoParameters repoParameters, final ActionEvent event) {
        if (exception instanceof TransportException) {
            Throwable root = ExceptionUtils.getRootCause(exception);
            TransportException te = (TransportException) exception;
            if (root instanceof java.nio.channels.UnresolvedAddressException ) {
                Context.getMain().showResult("Repository", "Connection to " + repoParameters.getUrl() + " failed", Alert.AlertType.ERROR);
            } else {
                LoginDialog ld = new LoginDialog(
                        "Login",
                        exception.getMessage(),
                        repoParameters
                );
                ld.showAndWait().ifPresent(
                        newParams -> {
                            Context.getCurrentProject().ifPresent(
                                    p -> {
                                        p.setUserName(newParams.getUserName());
                                        p.setUserPwd(newParams.getUserPwd());
                                        handler.handle(event);
                                    }
                            );
                        }
                );
                log.log(Level.WARNING, "Repository " + repoParameters.getUrl()
                        + " authorization is failed for " + repoParameters.getUserName() + " Will try one more time", exception);
            }
        } else {
            Context.getMain().showResult("Repository", "Failed \n" + exception.getMessage(), Alert.AlertType.ERROR);
            log.log(Level.SEVERE, "Repository " + repoParameters.getUrl()
                    + " operation is failed for " + repoParameters.getUserName(), exception); //TODO more informative

        }
    }

}
