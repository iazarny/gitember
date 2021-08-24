package com.az.gitember.controller.handlers;

import com.az.gitember.service.Context;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Edit given file.
 */
public class SetRemoteUrlEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(SetRemoteUrlEventHandler.class.getName());

    private final String url;

    public SetRemoteUrlEventHandler(String url) {
        this.url = url;
    }

    @Override
    public void handle(ActionEvent event) {

        try {
            Context.getGitRepoService().setRemoteUrl(url);
        } catch (Exception ex) {
            String msg = "Cannot set remote name ";
            log.log(Level.WARNING, msg + url, ex);
            Context.getMain().showResult(msg,  ExceptionUtils.getStackTrace(ex), Alert.AlertType.ERROR);
        }

    }


}
