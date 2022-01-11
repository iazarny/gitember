package com.az.gitember.controller.handlers.gitlab;

import com.az.gitember.data.Settings;
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
public class SetAccessTokenEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(SetAccessTokenEventHandler.class.getName());

    private final String token;

    public SetAccessTokenEventHandler(String token) {
        this.token = token;
    }

    @Override
    public void handle(ActionEvent event) {

        try {
            Context.settingsProperty.get().getGitlabSettings().setAccessToken(token);
            Context.saveSettings();
        } catch (Exception ex) {
            String msg = "Cannot set gitlab access token ";
            log.log(Level.WARNING, msg , ex);
            Context.getMain().showResult(msg,  ExceptionUtils.getStackTrace(ex), Alert.AlertType.ERROR);
        }

    }


}
