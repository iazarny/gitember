package com.az.gitember.controller;

import com.az.gitember.App;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.ResourceBundle;

public class TextBrowser implements Initializable {
    public BorderPane borderPane;
    public CodeArea codeArea;
    public VirtualizedScrollPane<CodeArea> scrollPane;
    private String content;
    private String fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setStyle(LookAndFeelSet.CODE_AREA_CSS);

        scrollPane = new VirtualizedScrollPane(codeArea);
        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            codeArea.setMinHeight(newValue.doubleValue());
        });
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            codeArea.setMinWidth(newValue.doubleValue());
        });
        borderPane.setCenter(scrollPane);

    }

    public void enableEdit(boolean allow) {
        codeArea.setEditable(allow);
    }

    public void setText(String text, boolean diff) {
        content = text;
        Platform.runLater(
                () -> {
                    codeArea.appendText(content);


                    codeArea.setShowCaret(Caret.CaretVisibility.ON);
                    CaretNode cr = new CaretNode("main-caret", codeArea);
                    codeArea.addCaret(cr);


                    TextToSpanContentAdapter adapter = new TextToSpanContentAdapter(
                            codeArea.getText(), FilenameUtils.getExtension(fileName), diff);

                    codeArea.setParagraphGraphicFactory(GitemberLineNumberFactory.get(codeArea, adapter));

                    StyleSpans<Collection<String>> spans = adapter.computeHighlighting();
                    if (spans != null) {
                        codeArea.setStyleSpans(0, spans);
                    }

                    adapter.getDiffDecoration().entrySet().forEach(p -> {
                        codeArea.setParagraphStyle(p.getKey(), p.getValue());
                    });
                    codeArea.moveTo(0, 0);
                    codeArea.requestFollowCaret();
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
