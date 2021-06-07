package com.az.gitember.controller;

import com.az.gitember.App;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TextBrowser implements Initializable {

    public TextFlow codeArea;
    public ScrollPane scrollPane;
    private String content;
    private String fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setText(String text, boolean diff) {

        //TODO setting to fxml
        codeArea.setPrefWidth(Region.USE_COMPUTED_SIZE);
        codeArea.setMinWidth(Region.USE_PREF_SIZE);

        TextBrowserContentAdapter adapter = new TextBrowserContentAdapter(FilenameUtils.getExtension(fileName), diff, true);
        codeArea.getChildren().addAll(
                adapter.getText(text)
        );
        content = new String(text);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void saveFile(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter anyFilter = new FileChooser.ExtensionFilter("Any files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(anyFilter);

        if (StringUtils.isNotBlank(FilenameUtils.getExtension(fileName))) {
            String ext = FilenameUtils.getExtension(fileName);
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(ext + " files (*." + ext + ")", "*." + ext);
            fileChooser.getExtensionFilters().add(extFilter);
        }

        fileChooser.setTitle("Save file");

        //Show save file dialog
        File file = fileChooser.showSaveDialog(App.getScene().getWindow());

        if (file != null) {

            try {
                Files.write(file.toPath(), this.content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
