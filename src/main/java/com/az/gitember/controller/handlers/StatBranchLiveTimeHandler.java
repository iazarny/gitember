package com.az.gitember.controller.handlers;

import com.az.gitember.controller.main.MainTreeChangeListener;
import com.az.gitember.dialog.StatWPDialog;
import com.az.gitember.data.AverageLiveTime;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StatBranchLiveTimeHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final MainTreeChangeListener mainTreeChangeListener;
    private boolean brances = false;

    public StatBranchLiveTimeHandler(MainTreeChangeListener mainTreeChangeListener) {
        this.mainTreeChangeListener = mainTreeChangeListener;
    }

    public StatBranchLiveTimeHandler(MainTreeChangeListener mainTreeChangeListener, boolean brances) {
        this.mainTreeChangeListener = mainTreeChangeListener;
        this.brances = brances;
    }

    @Override
    public void handle(ActionEvent event) {

        final Collection<String> branches = Context.localBrancesProperty.get().stream()
                .map(scmBranch -> scmBranch.getFullName()).collect(Collectors.toUnmodifiableList());

        String dialogTitle;

        if (brances) {
            dialogTitle = "Branches";
        } else {
            dialogTitle = "Branch file time";
        }

        new StatWPDialog("Stat", dialogTitle , branches, false, !brances )
                .showAndWait().ifPresent(
                params -> {

                    final List<AverageLiveTime> branchLiveTimes = Context.getGitRepoService().getMergedBranches(params);
                    Context.scmStatListPropertyParam.setValue(params);
                    final String viewName;
                    if (brances) {
                        viewName = "statbranches";
                    } else {
                        viewName = "statbranchlifetime";
                    }

                    mainTreeChangeListener.changed(null, null, viewName);

                    Context.scmStatBranchLiveTimeProperty.setValue(branchLiveTimes);

                }
        );

    }
}
