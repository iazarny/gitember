package com.az.gitember.controller.handlers;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.SmudgeFilter;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LfsDownloadEventHandler extends AbstractLongTaskEventHandler  implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(LfsDownloadEventHandler.class.getName());

    private final ScmItem scmItem;

    public LfsDownloadEventHandler(final ScmItem scmItem) {
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {

        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
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
                return null;
            }
        };

        launchLongTask(
                longTask,
                o -> {
                    //TODO non blocking message on UI !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    log.log(Level.INFO, "Large file {} downloaded ", scmItem.getShortName());
                },
                o -> {
                    String msg = "Cannot load large file ";
                    log.log(Level.SEVERE, msg , o.getSource().getException());
                    Context.getMain().showResult(msg,
                            ExceptionUtils.getStackTrace(o.getSource().getException()), Alert.AlertType.ERROR);
                }
        );


    }


}
