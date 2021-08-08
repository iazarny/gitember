package com.az.gitember.controller.handlers;

import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.SmudgeFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class LfsDownloadEventHandler implements EventHandler<ActionEvent> {

    private final ScmItem scmItem;

    public LfsDownloadEventHandler(final ScmItem scmItem) {
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {

        //String repoPath = Context.getGitRepoService().getRepository().getDirectory().getAbsolutePath().replace(".git", "");
        try {
            Path absFilePath = scmItem.getFilePath(ScmItem.BODY_TYPE.WORK_SPACE);
            try(FileInputStream fileInputStream = new FileInputStream(absFilePath.toFile())) {
                LfsPointer lfsPointer = LfsPointer.parseLfsPointer(fileInputStream);
                Lfs lfs = new Lfs(Context.getGitRepoService().getRepository());
                Collection<Path> rez = SmudgeFilter.downloadLfsResource(
                        lfs,
                        Context.getGitRepoService().getRepository(),
                        lfsPointer);
                System.out.println(rez.iterator().next() + " " + absFilePath.toFile());
                /*FileUtils.copyFile(
                        rez.iterator().next().toFile(),
                        absFilePath.toFile()
                );*/

            } catch (Exception e) {
                e.printStackTrace();
            }

            Context.getGitRepoService().checkoutFile(scmItem.getShortName(), CheckoutCommand.Stage.BASE );
        } catch (IOException e) {
            e.printStackTrace();
        }




    }


}
