package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.lib.Ref;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateBranchEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CreateBranchEventHandler.class.getName());

    private final String branchName;

    public CreateBranchEventHandler(String branchName) {
        this.branchName = branchName;

    }

    @Override
    public void handle(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New branch");
        dialog.setHeaderText("Create new branch");
        dialog.setContentText("Please enter new branch name:");
        dialog.initOwner(App.getScene().getWindow());

        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            try {
                Ref ref = Context.getGitRepoService().createBranch(
                        branchName,
                        dialogResult.get());
                Context.updateBranches();
                Context.getGitRepoService().checkoutBranch(ref.getName(), null);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create new local branch " +
                        dialogResult.get() + " from " + branchName, e);
                Context.getMain().showResult("Create new branch",
                        ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
            }
        }


    }
}
