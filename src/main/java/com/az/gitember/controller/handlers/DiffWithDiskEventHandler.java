package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.diff.DiffController;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.CommitInfo;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiffWithDiskEventHandler implements EventHandler<Event> {

    private final static Logger log = Logger.getLogger(DiffWithDiskEventHandler.class.getName());


    private final ScmItem item;
    private final String revision;

    public DiffWithDiskEventHandler(final ScmItem item) {
        this(item, null);
    }

    public DiffWithDiskEventHandler(final ScmItem item, final String revision) {

        this.item = item;
        this.revision = revision;

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
            String oldFile;
            try {
                oldFile = Context.getGitRepoService().saveFile( sha, fileName);
            } catch (Exception e) {
                oldFile = Context.getGitRepoService().creaeEmptyFile(fileName);
            }

            String newFile;
            try {
                newFile = Path.of(Context.getProjectFolder(), item.getShortName()).toString();
                Files.readString(Paths.get(newFile));
            } catch (Exception e) {
                newFile = Context.getGitRepoService().creaeEmptyFile(fileName);
            }

            final Pair<Parent, Object> pair = App.loadFXMLToNewStage(Const.View.FILE_DIFF,
                    "Difference with repository version " + fileName);
            pair.getFirst().getStylesheets().add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());
            final DiffController diffViewer = (DiffController) pair.getSecond();
            diffViewer.setData(oldFile, newFile);
            diffViewer.setOldLabel("Repository version ");
            if (item.getRevCommit() != null) {
                diffViewer.setOldLabel(GitemberUtil.formatRev(item.getRevCommit()));
            }
            diffViewer.setNewLabel("Disk version ");

        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot show  difference for  " + item.getShortName(), e);
            Context.getMain().showResult("Cannot show  difference for  " + item.getShortName(),
                    ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
        }
    }


}
