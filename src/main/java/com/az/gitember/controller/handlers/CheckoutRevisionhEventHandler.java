package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutRevisionhEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CheckoutRevisionhEventHandler.class.getName());

    private final RevCommit revCommit;
    private final String newBranchName;

    public CheckoutRevisionhEventHandler(RevCommit revCommit, String newBranchName) {
        this.revCommit = revCommit;
        this.newBranchName = newBranchName;
    }

    @Override
    public void handle(ActionEvent event) {


        try {

            Task<Ref> longTask = new Task<Ref>() {
                @Override
                protected Ref call() throws Exception {
                    return Context.getGitRepoService().checkoutRevCommit(
                            revCommit,
                            newBranchName,
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
                            try {
                                Context.updateBranches();
                                Context.updateWorkingBranch();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    o -> {
                        Context.getMain().showResult("Revision", "Checkout revision\n"
                                + ExceptionUtils.getStackTrace(o.getSource().getException()), Alert.AlertType.ERROR);

                    }
            );


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot checkout revision " + revCommit , e);
            Context.getMain().showResult("Checkout revision",
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }

    }

}
