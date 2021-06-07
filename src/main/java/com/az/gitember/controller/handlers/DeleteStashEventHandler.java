package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteStashEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(DeleteStashEventHandler.class.getName());

    private final ScmRevisionInformation scmItem;


    public DeleteStashEventHandler(ScmRevisionInformation scmItem) {
        this.scmItem = scmItem;
    }

    @Override
    public void handle(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(String.format("Delete stash %s", scmItem.getShortMessage()));
        alert.setContentText(String.format("Do you really want to delete %s stash ?", scmItem.getShortMessage()));
        alert.initOwner(App.getScene().getWindow());

        alert.showAndWait().ifPresent( r -> {

            if (r == ButtonType.OK) {
                try {
                    Context.getGitRepoService().deleteStash(scmItem.getStashIndex());
                    Context.updateStash();
                    Platform.runLater(() -> {
                        if(Context.stashProperty.getValue().isEmpty()) {
                            Context.getMain().getMainTreeView().getSelectionModel().select(1);
                        } else {
                            TreeView treeView = Context.getMain().getMainTreeView();
                            TreeItem stashes = Context.getMain().getStashRoot();
                            treeView.getSelectionModel().select(stashes.getChildren().get(0));
                        }
                    });

                } catch (Exception e) {
                    log.log(Level.SEVERE, "Cannot delete stash " + scmItem.getRevisionFullName() , e);
                    Context.getMain().showResult("Cannot apply stash " + scmItem.getRevisionFullName(),
                            ExceptionUtils.getRootCause(e).getMessage(), Alert.AlertType.ERROR);
                }
            }
        } );
    }
}
