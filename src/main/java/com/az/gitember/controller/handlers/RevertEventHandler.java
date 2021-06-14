package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Stage;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CheckoutCommand;

import java.util.logging.Logger;

public class RevertEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(StageEventHandler.class.getName());

    private final TableView param;
    private final ScmItem item;
    private final Stage stage;

    public RevertEventHandler(TableView param, ScmItem item) {
        this.item = item;
        this.param = param;
        this.stage = null;
    }

    public RevertEventHandler(TableView param, ScmItem item, Stage stage) {
        this.param = param;
        this.item = item;
        this.stage = stage;
    }

    @Override
    public void handle(ActionEvent event) {
        if (item != null) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setWidth(600);
            alert.setTitle("Question");
            alert.setContentText("Would you like to revert " + item.getShortName() + " changes ?");
            alert.initOwner(App.getScene().getWindow());
            alert.showAndWait().ifPresent( r -> {
                if (r == ButtonType.OK) {

                    Task<Void> longTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Context.getGitRepoService().checkoutFile(item.getShortName(), adaptStage(stage) );
                            Context.updateStatus(new DefaultProgressMonitor((t, d) -> {
                                updateTitle(t);
                                updateProgress(d, 1.0);
                            }));
                            return null;
                        }
                    };

                    launchLongTask(
                            longTask,o -> {},
                            o -> {
                                Context.getMain().showResult("Revert", "Failed to revert " + item.getShortName() + "\n" +
                                        ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                            }
                    );
                }
            });

        }
    }

    private CheckoutCommand.Stage adaptStage(Stage stage) {
        if (stage == Stage.OURS) {
            return CheckoutCommand.Stage.OURS;
        } else if (stage == Stage.THEIRS) {
            return CheckoutCommand.Stage.THEIRS;
        }
        return CheckoutCommand.Stage.BASE;
    }

}
