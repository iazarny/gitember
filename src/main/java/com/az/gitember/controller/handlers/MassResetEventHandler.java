package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.az.gitember.service.GitemberUtil.is;

public class MassResetEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(MassResetEventHandler.class.getName());
    private final List<ScmItem> selecteItems;


    public MassResetEventHandler(List<ScmItem> selecteItems) {
        this.selecteItems = selecteItems;
    }


    @Override
    public void handle(ActionEvent event) {

        List<String> subSelecteItems = new LinkedList<>();
        for (ScmItem processItem : selecteItems) {
            if (is(processItem.getAttribute().getStatus()).oneOf(ScmItem.Status.ADDED, ScmItem.Status.CHANGED, ScmItem.Status.RENAMED, ScmItem.Status.REMOVED)) {
                subSelecteItems.add(processItem.getShortName());
            }
        }

        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                DefaultProgressMonitor revMenitor = new DefaultProgressMonitor((t, d) -> {
                    updateTitle(t);
                    updateProgress(d, subSelecteItems.size());
                }, DefaultProgressMonitor.Strategy.Step);
                revMenitor.beginTask("Unstage ", subSelecteItems.size());
                int idx = 0;
                for (String processItem : subSelecteItems) {
                    Context.getGitRepoService().removeFileFromCommitStage(processItem);
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
                    Context.getMain().showResult("Unstage ", "Failed to unstage  " + subSelecteItems.size() + " items \n" +
                            ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                }
        );

    }

}
