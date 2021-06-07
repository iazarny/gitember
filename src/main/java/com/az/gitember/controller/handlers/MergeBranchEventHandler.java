package com.az.gitember.controller.handlers;

import com.az.gitember.controller.MergeDialog;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.MergeResult;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MergeBranchEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(MergeBranchEventHandler.class.getName());

    private final String preselectedBranch;

    public MergeBranchEventHandler(String preselectedBranch) {
        this.preselectedBranch = preselectedBranch;
    }

    @Override
    public void handle(ActionEvent event) {

        Collection<String> branches = Context.localBrancesProperty.get().stream()
                .map(scmBranch -> scmBranch.getFullName()).collect(Collectors.toUnmodifiableList());

        //TODO exclude current branch

        new MergeDialog(branches, preselectedBranch)
                .showAndWait()
                .ifPresent(
                        pair -> {
                            try {
                                MergeResult mergeResult = Context.getGitRepoService().mergeBranch(
                                        pair.getFirst(),
                                        pair.getSecond()
                                );
                                String msg = Context.getGitRepoService().getMessage(mergeResult);
                                Context.getMain().showResult("Merge result", msg, Alert.AlertType.INFORMATION);

                            } catch (Exception e) {
                                log.log(Level.SEVERE, "Cannot merge " + pair.getFirst(), e);
                                Context.getMain().showResult("Merge result", ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
                            }
                        }
                );

    }


}
