package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.diff.DiffController;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiffWithDiskEventHandler implements EventHandler<Event> {

    private final static Logger log = Logger.getLogger(DiffWithDiskEventHandler.class.getName());


    private final ScmItem item;
    private final String revision;

    private List<ScmRevisionInformation> fileRevs;

    public DiffWithDiskEventHandler(final ScmItem item, final List<ScmRevisionInformation> fileRevs) {
        this(item, fileRevs, null);
    }

    public DiffWithDiskEventHandler(final ScmItem item, final List<ScmRevisionInformation> fileRevs, final String revision) {

        this.item = item;
        this.revision = revision;
        this.fileRevs = fileRevs;

    }

    @Override
    public void handle(Event event) {

        try {
            final String fileName = item.getShortName();
            final String sha;
            if (revision == null) {
                final CommitInfo head = Context.getGitRepoService().getHead();
                sha = head.getSha();
            } else {
                sha = revision;
            }

            System.out.println(">>>>> sha " + sha + " for compare with " + item.getShortName());




            final Pair<Parent, Object> pair = App.loadFXMLToNewStage(Const.View.FILE_DIFF,
                    "Difference with repository version " + fileName);
            pair.getFirst().getStylesheets().add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());

            final DiffController diffViewer = (DiffController) pair.getSecond();
            diffViewer.setData(item,  fileRevs, sha, null);
            //diffViewer.setOldLabel("Repository version ");
            if (item.getRevCommit() != null) {
                //diffViewer.setOldLabel(GitemberUtil.formatRev(item.getRevCommit()));
            }


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot show  difference for  " + item.getShortName(), e);
            Context.getMain().showResult("Cannot show  difference for  " + item.getShortName(),
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }
    }


}
