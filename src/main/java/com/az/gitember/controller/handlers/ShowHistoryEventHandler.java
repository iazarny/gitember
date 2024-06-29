package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ShowHistoryEventHandler implements EventHandler<ActionEvent> {

    private final String sha;
    private final  ScmItem scmItem;

    public ShowHistoryEventHandler(final String sha, ScmItem scmItem) {
        this.sha = sha;
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {
        final String treeName = Context.getGitRepoService().getBranchName(sha);
        final String fileName = scmItem.getShortName();

        Context.fileHistoryTree.setValue(treeName);
        Context.fileHistoryName.setValue(fileName);

        App.loadFXMLToNewStage(Const.View.FILE_HISTORY, "History");

    }
}
