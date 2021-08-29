package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Stage;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CheckoutCommand;

import java.util.List;
import java.util.logging.Logger;

public class MassRevertEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(MassRevertEventHandler.class.getName());
    private final List<ScmItem> selecteItems;


    public MassRevertEventHandler( List<ScmItem> selecteItems) {
        this.selecteItems = selecteItems;
    }


    @Override
    public void handle(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Revert changes");
        alert.setContentText("Do you really want to revert multiple items changes ?");
        alert.initOwner(App.getScene().getWindow());

        alert.showAndWait().ifPresent(
                r -> {

                    Task<Void> longTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {

                            DefaultProgressMonitor revMenitor = new DefaultProgressMonitor((t, d) -> {
                                updateTitle(t);
                                updateProgress(d, selecteItems.size());
                            }, DefaultProgressMonitor.Strategy.Step);
                            revMenitor.beginTask("Reverting", selecteItems.size());

                            if (r == ButtonType.OK) {
                                int idx = 0;
                                for (ScmItem processItem : selecteItems) {
                                    Context.getGitRepoService().checkoutFile(processItem.getShortName(), CheckoutCommand.Stage.BASE);
                                    revMenitor.update(idx++);
                                }
                            }

                            Context.updateStatus(new DefaultProgressMonitor((t, d) -> {
                                updateTitle(t);
                                updateProgress(d, 1.0);
                            }, DefaultProgressMonitor.Strategy.Step));
                            return null;
                        }
                    };

                    launchLongTask(
                            longTask,o -> {},
                            o -> {
                                Context.getMain().showResult("Revert", "Failed to revert " + selecteItems.size() + " items \n" +
                                        ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                            }
                    );


                }
        );

    }

}
