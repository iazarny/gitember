package com.az.gitember.controller.handlers;

import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.controller.main.MainTreeChangeListener;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmStat;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.util.Set;

public class StarHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final MainTreeChangeListener mainTreeChangeListener;

    public StarHandler(MainTreeChangeListener mainTreeChangeListener) {
        this.mainTreeChangeListener = mainTreeChangeListener;
    }

    @Override
    public void handle(ActionEvent event) {

        Task<ScmStat> longTask = new Task<ScmStat>() {
            @Override
            protected ScmStat call() throws Exception {

                final Set<String> files = Context.getGitRepoService().getAllFiles();

                return Context.getGitRepoService().blame(
                        files,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        }
                        )
                );
            }

            ;
        };

        launchLongTask(
                longTask,
                o -> {
                    ScmStat scmStat = (ScmStat) o.getSource().getValue();
                    mainTreeChangeListener.changed(null, null, Const.View.STAT);
                    Context.scmStatProperty.setValue(scmStat);
                },
                o -> {
                    Context.getMain().showResult("Statistics", "Failed to get stat\n" + longTask.getException().getMessage(), Alert.AlertType.ERROR);
                }
        );

    }


}
