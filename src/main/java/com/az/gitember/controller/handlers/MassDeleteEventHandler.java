package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.logging.Logger;

import static com.az.gitember.service.GitemberUtil.is;

public class MassDeleteEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(MassDeleteEventHandler.class.getName());
    private final List<ScmItem> selecteItems;


    public MassDeleteEventHandler(List<ScmItem> selecteItems) {
        this.selecteItems = selecteItems;
    }


    @Override
    public void handle(ActionEvent event) {


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Dangerous. Please confirm");
        alert.setHeaderText("Delete files !");
        alert.setContentText(String.format("Do you really want to delete %d item(s) ?", selecteItems.size()));
        alert.initOwner(App.getScene().getWindow());

        alert.showAndWait().ifPresent(
                r -> {
                    if (r == ButtonType.OK) {
                        Task<Void> longTask = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {

                                DefaultProgressMonitor revMenitor = new DefaultProgressMonitor((t, d) -> {
                                    updateTitle(t);
                                    updateProgress(d, selecteItems.size());
                                }, DefaultProgressMonitor.Strategy.Step);
                                revMenitor.beginTask("Delete  ", selecteItems.size());
                                int idx = 0;

                                for (ScmItem processItem : selecteItems) {
                                    if (is(processItem.getAttribute().getStatus()).oneOf(ScmItem.Status.ADDED, ScmItem.Status.MODIFIED, ScmItem.Status.UNTRACKED)) {
                                        Context.getGitRepoService().unlink(processItem.getShortName());
                                    }
                                    revMenitor.update(idx++);
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
                                    Context.getMain().showResult("Delete ", "Failed to delete  selected items \n" +
                                            ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                                }
                        );

                    }
                }
        );

    }

}
