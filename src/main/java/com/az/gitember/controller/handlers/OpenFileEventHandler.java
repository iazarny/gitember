package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.TextBrowser;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Edit given file.
 */
public class OpenFileEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(OpenFileEventHandler.class.getName());

    private final ScmItem item;

    private final ScmItem.BODY_TYPE bodyType;

    private boolean forceText = false;
    private boolean editble = false;


    public OpenFileEventHandler(ScmItem item, ScmItem.BODY_TYPE bodyType) {
        this.item = item;
        this.bodyType = bodyType;
    }

    public void setForceText(boolean forceText) {
        this.forceText = forceText;
    }
    public void setEditable(boolean editble) {
        this.editble = editble;
    }

    @Override
    public void handle(ActionEvent event) {

        final String fileName = item.getShortName();

        try {

            if (ScmItem.BODY_TYPE.RAW_DIFF == bodyType) {
                final String text = new String(item.getBody(bodyType));
                openFile(fileName, text,true);
            } else if (forceText || ExtensionMap.isTextExtension(item.getShortName())) {
                String text;
                try {
                    text = new String(item.getBody(bodyType));
                } catch (Exception e) {
                    text = "";
                }
                openFile(fileName, text, false);
            } else {
                String pathToFile = item.getFilePath(bodyType).toString();
                App.getShell().showDocument(pathToFile);
            }

        } catch (IOException ex) {
            log.log(Level.WARNING, "Cannot read file " + Path.of(Context.getProjectFolder(), fileName).toString() , ex);
            Context.getMain().showResult("Cannot read file " + Path.of(Context.getProjectFolder(), fileName).toString(),
                    ex.getMessage(), Alert.AlertType.ERROR);
        }

    }

    private void openFile(final String fileName, final String text, final boolean rawDiff) throws IOException {
        final Pair<Parent, Object> pair = App.loadFXML(Const.View.EDITOR);
        final Scene scene = new Scene(pair.getFirst());
        App.getjMetro().setScene(scene);
        scene.getStylesheets().add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());
        final TextBrowser textBrowser = (TextBrowser)pair.getSecond();

        textBrowser.enableEdit(editble);
        textBrowser.setFileName(fileName);
        textBrowser.setText(text, rawDiff);

        final Stage editorStage = new Stage();
        editorStage.getIcons().add(new Image(this.getClass().getResourceAsStream(Const.ICON)));
        editorStage.setScene(scene);
        editorStage.setTitle(fileName);
        editorStage.show();
    }

}
