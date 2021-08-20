package com.az.gitember.controller.handlers;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.SmudgeFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LfsDownloadEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(LfsDownloadEventHandler.class.getName());

    private final ScmItem scmItem;

    public LfsDownloadEventHandler(final ScmItem scmItem) {
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {

        try {
            final Path absFilePath = scmItem.getFilePath(ScmItem.BODY_TYPE.WORK_SPACE);
            try(FileInputStream fileInputStream = new FileInputStream(absFilePath.toFile())) {
                LfsPointer lfsPointer = LfsPointer.parseLfsPointer(fileInputStream);
                Lfs lfs = new Lfs(Context.getGitRepoService().getRepository());
                Collection<Path> rez = SmudgeFilter.downloadLfsResource(
                        lfs,
                        Context.getGitRepoService().getRepository(),
                        lfsPointer);
            }

            Context.getGitRepoService().checkoutFile(scmItem.getShortName(), CheckoutCommand.Stage.BASE );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot load large file  " , e);
            Context.getMain().showResult("Cannot load large file ",
                    ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
        }

    }


}
