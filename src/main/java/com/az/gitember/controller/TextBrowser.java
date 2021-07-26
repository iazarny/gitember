package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.service.GitemberUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class TextBrowser implements Initializable {
    public BorderPane borderPane;
    public CodeArea codeArea;
    public VirtualizedScrollPane scrollPane;
    private String content;
    private String fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        codeArea = new CodeArea();
        codeArea.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(2), Insets.EMPTY)));
        scrollPane = new VirtualizedScrollPane(codeArea);


        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            codeArea.setMinHeight(newValue.doubleValue());
        });
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            codeArea.setMinWidth(newValue.doubleValue());
        });
        borderPane.setCenter(scrollPane);

    }

    public void setText(String text, boolean diff) {
        content = text;
        Platform.runLater(
                () -> {
                    codeArea.appendText(content);
                    TextToSpanContentAdapter adapter = new TextToSpanContentAdapter(
                            codeArea.getText(), FilenameUtils.getExtension(fileName), diff);
                    codeArea.setStyleSpans(0,adapter.computeHighlighting() );
                    codeArea.setStyle(11, Collections.singletonList("debug"));
                    if (diff) {
                        adapter.decorateByRawDiff().forEach( p -> {
                            codeArea.setParagraphStyle(p.getFirst(), p.getSecond());
                        });
                    }

                    codeArea.setParagraphGraphicFactory(GitemberLineNumberFactory.get(codeArea, Collections.EMPTY_LIST));
                }
        );

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
