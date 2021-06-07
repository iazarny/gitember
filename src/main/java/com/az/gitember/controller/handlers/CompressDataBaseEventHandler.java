package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

public class CompressDataBaseEventHandler extends   AbstractLontTaskEventHandler implements EventHandler<ActionEvent> {


    @Override
    public void handle(ActionEvent event) {
        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Context.getGitRepoService().compressDatabase(
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        })
                );
                return null;
            }
        };
        launchLongTask(
                longTask,
                o -> Context.getMain().showResult("Repository", "Repository database is compressed", Alert.AlertType.INFORMATION),
                o -> Context.getMain(). showResult("Repository", "Failed to compress\n" + o.getSource().getException().getMessage(), Alert.AlertType.ERROR)
        );
    }


}
