package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.diff.DiffController;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiffEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(DiffEventHandler.class.getName());
    private final ScmItem item;
    private final String oldRev;
    private final String newRev;

    private RevCommit oldRevCommit;
    private RevCommit newRevCommit;

    private List<ScmRevisionInformation> fileRevs;

    public DiffEventHandler(final ScmItem item, final List<ScmRevisionInformation> fileRevs,
                            final RevCommit oldRevCommit, final RevCommit newRevCommit) {
        this.item = item;
        this.oldRev = oldRevCommit.getName();
        this.newRev = newRevCommit.getName();
        this.oldRevCommit = oldRevCommit;
        this.newRevCommit = newRevCommit;
        this.fileRevs = fileRevs;
    }



    public DiffEventHandler(final ScmItem item, final String oldRev, final String newRev) {
        this.item = item;
        this.oldRev = oldRev;
        this.newRev = newRev;
    }

    @Override
    public void handle(ActionEvent event) {
        try {
            final String fileName = item.getShortName();
            final Pair<Parent, Object> pair = App.loadFXMLToNewStage(Const.View.FILE_DIFF,
                    "Difference " + fileName);
            pair.getFirst().getStylesheets().add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());
            final DiffController diffViewer = (DiffController) pair.getSecond();
            diffViewer.setData(item,  fileRevs, oldRev, newRev);


            /*if (oldRevCommit != null) {
                diffViewer.setOldLabel(GitemberUtil.formatRev(oldRevCommit));
            }
            if (newRevCommit != null) {
                diffViewer.setNewLabel(GitemberUtil.formatRev(newRevCommit));
            }*/


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot show  difference for  " + item.getShortName() + " " + oldRev + " " + newRev, e);
            Context.getMain().showResult("Cannot show  difference for  " + item.getShortName(),
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }
    }


}
