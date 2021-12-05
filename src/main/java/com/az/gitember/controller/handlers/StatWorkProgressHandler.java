package com.az.gitember.controller.handlers;

import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.controller.MainTreeChangeListener;
import com.az.gitember.controller.StatWPDialog;
import com.az.gitember.data.ScmStat;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class StatWorkProgressHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final MainTreeChangeListener mainTreeChangeListener;

    public StatWorkProgressHandler(MainTreeChangeListener mainTreeChangeListener) {
        this.mainTreeChangeListener = mainTreeChangeListener;
    }

    @Override
    public void handle(ActionEvent event) {

        final Collection<String> branches = Context.localBrancesProperty.get().stream()
                .map(scmBranch -> scmBranch.getFullName()).collect(Collectors.toUnmodifiableList());

        new StatWPDialog("Stat", "Commit statistics", branches, true, false)
                .showAndWait().ifPresent(
                        params -> {

                            final PlotCommitList<PlotLane> allCommits = Context.getGitRepoService().getCommitsByTree(
                                    params.getBranchName(),false, -1,null);

                            final PlotCommitList<PlotLane> lastCommitPerMonth = new PlotCommitList<>();
                            final PlotCommitList<PlotLane> src = selectLastCommitInMonth(allCommits);

                            if(params.isDelta()) {
                                params.setMonthQty(params.getMonthQty() + 1);
                            }

                            for (int i = 0; i < src.size(); i++) {
                                if ( src.size() - i <= params.getMonthQty() ) {
                                    lastCommitPerMonth.add(src.get(i));
                                }
                            }

                            final Task<List<ScmStat>> longTask = new Task<List<ScmStat>>() {
                                @Override
                                protected List<ScmStat> call() throws Exception {
                                    Context.scmStatListPropertyParam.setValue(params);
                                    return Context.getGitRepoService().blame(
                                            lastCommitPerMonth,
                                            new DefaultProgressMonitor((t, d) -> {
                                                updateTitle(t);
                                                updateProgress(d, 1.0);
                                            })
                                    );
                                };
                            };


                            launchLongTask(
                                    longTask,
                                    o -> {
                                        final List<ScmStat> scmStats = (List<ScmStat>) o.getSource().getValue();
                                        if (params.isDelta()) {
                                            calculateDeltaLines(scmStats);
                                        }
                                        for (int i = 0; i < scmStats.size() ; i++) {
                                            ScmStat scmStat = scmStats.get(i);
                                            scmStat.setTotalLines(  GitemberUtil.topValues(scmStat.getTotalLines(), params.getMaxPeople())  );
                                        }
                                        mainTreeChangeListener.changed(null, null, "statworkingprogress");
                                        Context.scmStatListProperty.setValue(scmStats);

                                    },
                                    o -> {
                                        Context.getMain().showResult("Statistics", "Failed to get extended stat\n" + longTask.getException().getMessage(), Alert.AlertType.ERROR);
                                    }
                            );
                        }
        );


    }

    /**
     * Select last commit in each month.
     * @param allCommits
     * @return
     */
    private PlotCommitList<PlotLane> selectLastCommitInMonth(final PlotCommitList<PlotLane> allCommits) {
        final PlotCommitList<PlotLane> lastCommitPerMonth = new PlotCommitList();

        PlotCommit lastInMonthPlotCommit = null;
        for (int i = allCommits.size() -1 ; i >=0 ; i--) {
            PlotCommit pc = allCommits.get(i);
            if (lastInMonthPlotCommit == null) {
                lastInMonthPlotCommit = pc;
            }
            Date dt = GitemberUtil.intToDate(pc.getCommitTime());
            Date lastInMonthDt = GitemberUtil.intToDate(lastInMonthPlotCommit.getCommitTime());
            if(dt.getMonth() != lastInMonthDt.getMonth()) {
                lastCommitPerMonth.add(lastInMonthPlotCommit);
            }
            lastInMonthPlotCommit = pc;
        }

        return lastCommitPerMonth;
    }

    /**
     * Calculate delta lines by comparison with previos month
     * @param scmStats
     */
    private void calculateDeltaLines(List<ScmStat> scmStats) {
        for (int i = scmStats.size() - 1; i > 0 ; i--) {
            ScmStat scmStatRightEnd = scmStats.get(i);
            ScmStat scmStatPrev = scmStats.get(i-1);
            scmStatPrev.getTotalLines().forEach(
                    (k,v) -> {
                        scmStatRightEnd.getTotalLines().computeIfPresent(
                                k,
                                (s, integer) -> integer - v //delta lines
                        );
                    }
            );
        }
        scmStats.remove(0);
    }
}
