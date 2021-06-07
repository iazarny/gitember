package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.DiffViewer;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiffEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(DiffEventHandler.class.getName());


    private final ScmItem item;
    private final String oldRev;
    private final String newRev;

    public DiffEventHandler(final ScmItem item, final String oldRev, final String newRev) {

        this.item = item;
        this.oldRev = oldRev;
        this.newRev = newRev;

    }

    @Override
    public void handle(ActionEvent event) {

        try {
            final String fileName = item.getShortName();

            final String oldFile;
            if (oldRev == null || ScmItem.Status.ADDED.equals(item.getAttribute().getStatus() )) {
                final File temp = File.createTempFile(Const.TEMP_FILE_PREFIX,"empty.txt");
                oldFile = temp.getAbsolutePath();
            } else {
                oldFile = Context.getGitRepoService().saveFile( oldRev, fileName);
            }

            final String newFile = Context.getGitRepoService().saveFile( newRev, fileName);

            final Pair<Parent, Object> pair = App.loadFXMLToNewStage("diffviewer",
                    "Difference " + fileName);
            pair.getFirst().getStylesheets().add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());
            final DiffViewer diffViewer = (DiffViewer) pair.getSecond();
            diffViewer.setData(oldFile, newFile);
            diffViewer.setOldLabel(oldRev);
            diffViewer.setNewLabel(newRev);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot show  difference for  " + item.getShortName() + " " + oldRev + " " + newRev, e);
            Context.getMain().showResult("Cannot show  difference for  " + item.getShortName(),
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }
    }


}
