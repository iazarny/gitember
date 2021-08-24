package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplyStashEventHandler extends StatusUpdateEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(ApplyStashEventHandler.class.getName());

    private final ScmRevisionInformation scmItem;


    public ApplyStashEventHandler(ScmRevisionInformation scmItem) {
        super(true);
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);
        alert.setTitle("Question");
        alert.setContentText("Would you like to apply stash " + scmItem.getRevisionFullName() + "  ?");
        alert.initOwner(App.getScene().getWindow());
        alert.showAndWait().ifPresent( r -> {
            if (r == ButtonType.OK) {
                try {
                    Context.getGitRepoService().applyStash(scmItem.getRevisionFullName());
                    super.handle(event);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot apply stash " + scmItem.getRevisionFullName() , e);
                }
            }
        } );

    }
}
