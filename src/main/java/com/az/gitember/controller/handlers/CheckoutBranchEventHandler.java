package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.lib.Ref;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutBranchEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CheckoutBranchEventHandler.class.getName());

    private final ScmBranch branchItem;

    public CheckoutBranchEventHandler(ScmBranch branchItem) {
        this.branchItem = branchItem;
    }

    @Override
    public void handle(ActionEvent event) {

        try {

            Task<Ref> longTask = new Task<Ref>() {
                @Override
                protected Ref call() throws Exception {
                    return Context.getGitRepoService().checkoutBranch(
                            branchItem.getFullName(),
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
                        Context.getMain().showResult("Branch", "Checkout\n" + o.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                    }
            );


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot checkout branch " + branchItem.getFullName() , e);
            Context.getMain().showResult("Checkout branch",
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }

    }



    /*@Override
    public void handle(ActionEvent event) {
        String branchName = Context.workingBranch.get().getRemoteMergeName();
        RemoteRepoParameters repoParameters = new RemoteRepoParameters();
        Task<String> longTask = new Task<String>() {

            @Override
            protected String call() {
                return Context.getGitRepoService().remoteRepositoryPull(
                        repoParameters,
                        branchName,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }
                        ));
            }
        };

        lanchLongTask(
                longTask,
                o -> {
                    {
                        Context.getMain().showResult("Repository", "Pull ok\n" + o.getSource().getValue(), Alert.AlertType.INFORMATION);
                    }
                },
                o -> {
                    Context.getMain().showResult("Repository", "Cannot pull\n" + o.getSource().getValue(), Alert.AlertType.ERROR);
                }
        );
    }*/
}
