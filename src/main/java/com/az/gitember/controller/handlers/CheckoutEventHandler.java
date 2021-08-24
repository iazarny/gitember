package com.az.gitember.controller.handlers;

import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceDialog;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CheckoutEventHandler extends StatusUpdateEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(CheckoutEventHandler.class.getName());

    public CheckoutEventHandler() {
        super(true);
    }

    @Override
    public void handle(ActionEvent event) {
        final String branchName = Context.workingBranch.getValue().getFullName();

        final Collection<String> branches = Context.localBrancesProperty.get().stream()
                .map(scmBranch -> scmBranch.getFullName()).collect(Collectors.toUnmodifiableList());

        final ChoiceDialog<String> dialog = new ChoiceDialog<>(null, branches);
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Please, select branch to checkout " );
        dialog.setContentText("Available branches:");
        dialog.showAndWait().ifPresent(
                r -> {

                    try {
                        Context.getGitRepoService().checkoutBranch(r, null);
                        super.handle(event);
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Cannot checkout " + r + " . Current branch is " + branchName, e);
                    }

                }
        );
    }

}
