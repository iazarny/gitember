package com.az.gitember.controller.handlers;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stage unstage event handler.
 */
public class StageEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(StageEventHandler.class.getName());

    private final TableView tableView;
    private final ScmItem item;

    public StageEventHandler(TableView tableView, ScmItem item) {
        this.tableView = tableView;
        this.item = item;
    }

    @Override
    public void handle(ActionEvent event) {


        try {
            if (ScmItem.Status.RENAMED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().renameFile(item.getShortName(), item.getAttribute().getOldName() );
                Context.statusList.remove(item);
            } else if (ScmItem.Status.MISSED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().removeFile(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.REMOVED);


            } else if (ScmItem.Status.ADDED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().removeFileFromCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.UNTRACKED);
            } else if (ScmItem.Status.CHANGED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().removeFileFromCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.MODIFIED);
            } else if (ScmItem.Status.REMOVED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().removeFileFromCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.MISSED);

            } else if (ScmItem.Status.UNTRACKED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().addFileToCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.ADDED);
            } else if (ScmItem.Status.UNTRACKED_FOLDER.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().addFileToCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.ADDED);
            } else if (ScmItem.Status.MODIFIED.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().addFileToCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.CHANGED);
            } else if (ScmItem.Status.CONFLICT.equals(item.getAttribute().getStatus())) {
                Context.getGitRepoService().addFileToCommitStage(item.getShortName());
                item.getAttribute().setStatus(ScmItem.Status.CHANGED);
            }

            tableView.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Cannot change status of {0}. {1}", new String[] {"" + item, e.getMessage()});
        }

    }

}
