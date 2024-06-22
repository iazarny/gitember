package com.az.gitember.controller.history.listener;

import com.az.gitember.App;
import com.az.gitember.data.Const;
import com.az.gitember.service.Context;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotLane;

import java.io.IOException;

public class HistoryCommitsTableViewChangeListener implements ChangeListener<PlotCommit<PlotLane>> {

    private final BorderPane mainBorderPanel;
    private final SplitPane splitPanel;

    private AnchorPane hostCommitViewPanel;


    public HistoryCommitsTableViewChangeListener(BorderPane mainBorderPanel, SplitPane splitPanel) {
        this.mainBorderPanel = mainBorderPanel;
        this.splitPanel = splitPanel;
    }

    @Override
    public void changed(ObservableValue<? extends PlotCommit<PlotLane>> observable, PlotCommit<PlotLane> oldValue, PlotCommit<PlotLane> newValue) {
        if (splitPanel.getItems().size() == 1) {
            hostCommitViewPanel = new AnchorPane();
            hostCommitViewPanel.prefHeight(330);
            hostCommitViewPanel.minHeight(250);
            splitPanel.getItems().add(hostCommitViewPanel);
            splitPanel.setDividerPositions(0.65);
            mainBorderPanel.layout();
        }
        if (newValue != null) {
            try {
                Context.scmRevCommitDetails.setValue(Context.getGitRepoService().adapt(newValue));
                final Parent commitView = App.loadFXML(Const.View.HISTORY_DETAIL).getFirst();
                hostCommitViewPanel.getChildren().clear();
                hostCommitViewPanel.getChildren().add(commitView);
            } catch (IOException e) {
                // TODO: Log
                e.printStackTrace();
            }
        }
    }
}
