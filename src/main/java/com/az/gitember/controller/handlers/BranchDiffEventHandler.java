package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.BranchDiffController;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.data.Const;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.IOException;
import java.util.List;


public class BranchDiffEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final String leftBranchName;
    private final String rightBranchName;

    public BranchDiffEventHandler(String leftBranchName, String rightBranchName) {
        this.leftBranchName = leftBranchName;
        this.rightBranchName = rightBranchName;
    }

    @Override
    public void handle(ActionEvent event) {

        Task<List<DiffEntry>> longTask = new Task<List<DiffEntry>>() {
            @Override
            protected List<DiffEntry> call() throws Exception {
                return Context.getGitRepoService().branchDiff(
                        leftBranchName,
                        rightBranchName,
                        new DefaultProgressMonitor((t, d) -> {
                            updateTitle(t);
                            updateProgress(d, 1.0);
                        })
                );
            }
        };

        launchLongTask(
                longTask,
                o -> {
                    List<DiffEntry> diffEntries = (List<DiffEntry>) o.getSource().getValue();
                    BranchDiffController branchDiffController =
                            (BranchDiffController) App.loadFXMLToNewStage(Const.View.BRANCH_DIFF, "Branch difference").getSecond();
                    branchDiffController.setData(leftBranchName, rightBranchName, diffEntries);
                },
                o -> Context.getMain(). showResult("Branch difference", "Failed to create difference between "
                        + leftBranchName + " and " + rightBranchName, Alert.AlertType.ERROR)
        );
    }


}
