package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.Ref;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CheckoutBranchEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CheckoutBranchEventHandler.class.getName());

    private final ScmBranch branchItem;

    public CheckoutBranchEventHandler(ScmBranch branchItem) {
        this.branchItem = branchItem;
    }

    @Override
    public void handle(ActionEvent event) {

        Task<Ref> longTask = new Task<Ref>() {
            @Override
            protected Ref call() throws Exception {
                return Context.getGitRepoService().checkoutBranch(
                        branchItem.getFullName(),
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
                        try {
                            Context.updateBranches();
                            Context.updateWorkingBranch();
                        } catch (Exception e) {
                           log.log( Level.SEVERE, "Cannot update branches. " + ExceptionUtils.getStackTrace(e));
                        }

                    }
                },
                o -> {
                    final Throwable e = o.getSource().getException().getCause() == null ?
                            o.getSource().getException() : o.getSource().getException().getCause();
                    final Level level;
                    String message = "Cannot checkout branch " + branchItem.getFullName() ;

                    if (e instanceof CheckoutConflictException) {
                        final CheckoutConflictException cce = (CheckoutConflictException) e;
                        final String conflictingFiles = cce.getConflictingPaths().stream().collect(Collectors.joining("\n"));
                        level = Level.WARNING;
                        message +=  " because of conflicts\n" + conflictingFiles;
                    } else {
                        level = Level.SEVERE;
                        message = "\n" + ExceptionUtils.getStackTrace(e);
                    }
                    log.log(level, message);
                    Context.getMain().showResult("Branch", message, Alert.AlertType.ERROR);
                }
        );

    }


}
