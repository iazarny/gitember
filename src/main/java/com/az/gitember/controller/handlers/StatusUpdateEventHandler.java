package com.az.gitember.controller.handlers;

import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusUpdateEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final boolean showProgressMonitor;
    private final boolean workingCopyOnly;
    private final Consumer<WorkerStateEvent> onOk;


    private final static Logger log = Logger.getLogger(StatusUpdateEventHandler.class.getName());

    public StatusUpdateEventHandler(boolean showProgressMonitor) {
        this(showProgressMonitor, false, workerStateEvent -> {});
    }

    public StatusUpdateEventHandler(boolean showProgressMonitor, boolean workingCopyOnly) {
        this(showProgressMonitor, workingCopyOnly, workerStateEvent -> {});
    }

    public StatusUpdateEventHandler(boolean showProgressMonitor, final Consumer<WorkerStateEvent> onOk) {
        this(showProgressMonitor, false, onOk);
    }

    public StatusUpdateEventHandler(boolean showProgressMonitor, boolean workingCopyOnly, final Consumer<WorkerStateEvent> onOk) {
        this.showProgressMonitor = showProgressMonitor;
        this.workingCopyOnly = workingCopyOnly;
        this.onOk = onOk;
    }

    @Override
    public void handle(ActionEvent event) {


        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                ProgressMonitor progressMonitor = null;
                if (showProgressMonitor) {
                    progressMonitor = new DefaultProgressMonitor((t, d) -> {
                        updateTitle(t);
                        updateProgress(d, 1.0);
                    }, DefaultProgressMonitor.Strategy.Step);
                }

                Context.updateStatus(progressMonitor, workingCopyOnly);

                return null;
            }
        };

        launchLongTask(
                longTask,
                onOk,
                o -> {
                    log.log(Level.SEVERE, "Cannot update status " , longTask.getException());
                    Context.getMain().showResult("Cannot update status " ,
                            ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                }
        );


    }

}
