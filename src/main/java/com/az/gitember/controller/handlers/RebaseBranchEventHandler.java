package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RebaseBranchEventHandler extends StatusUpdateEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(RebaseBranchEventHandler.class.getName());

    private final String preselectedBranch;

    public RebaseBranchEventHandler(String preselectedBranch) {
        super(true);
        this.preselectedBranch = preselectedBranch;
    }

    @Override
    public void handle(ActionEvent event) {

        //TODO exclude current branch

        final String branchName = Context.workingBranch.getValue().getFullName();

        final Collection<String> branches = Context.localBrancesProperty.get().stream()
                .map(scmBranch -> scmBranch.getFullName()).collect(Collectors.toUnmodifiableList());

        final ChoiceDialog<String> dialog = new ChoiceDialog<>(preselectedBranch, branches);
        dialog.setTitle("Rebase");
        dialog.setHeaderText("Rebase to " + branchName );
        dialog.setContentText("Choose branch:");
        dialog.initOwner(App.getScene().getWindow());
        dialog.showAndWait().ifPresent(
                r -> {
                    try {
                        Context.getGitRepoService().rebaseBranch(r);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Cannot rebase " + r + " " + branchName,  e.getMessage());
                        Context.getMain().showResult("Rebase result", e.getMessage(), Alert.AlertType.ERROR);
                    }
                    super.handle(event);
                }
        );


    }


}
