package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.ResourceBundle;


//TODO alert in case of close and text changes
public class TextBrowser implements Initializable {
    public BorderPane borderPane;
    public CodeArea codeArea;
    public VirtualizedScrollPane<CodeArea> scrollPane;
    public Button saveBtn;
    private String content;
    private String fileName;
    private boolean overwrite;

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
        this.codeArea.setEditable(allow);
        if (allow) {
            saveBtn.setText("Save");
            saveBtn.setDisable(true);
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Force owerwrite to everwrite the same file
     */
    public void setForceOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }


    public void setText(String text, boolean diff) {

        TextToSpanContentAdapter adapter = new TextToSpanContentAdapter(FilenameUtils.getExtension(fileName), diff);
        this.content = text;

        Platform.runLater(
                () -> {
                    codeArea.appendText(content);

                    codeArea.setParagraphGraphicFactory(GitemberLineNumberFactory.get(codeArea, adapter));

                    // initial color
                    StyleSpans<Collection<String>> spans = adapter.computeHighlighting(codeArea.getText());
                    if (spans != null) {
                        codeArea.setStyleSpans(0, spans);
                    }

                    //re-coloring when editing
                    codeArea.richChanges()
                            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                            .subscribe(change -> {
                                {
                                    codeArea.setStyleSpans(0, adapter.computeHighlighting(codeArea.getText())  );
                                    saveBtn.setDisable(false);
                                }
                            });

                    adapter.getDiffDecoration(codeArea.getText()).entrySet().forEach(p -> {
                        codeArea.setParagraphStyle(p.getKey(), p.getValue());
                    });

                    codeArea.moveTo(0, 0);
                    codeArea.setShowCaret(Caret.CaretVisibility.ON);
                    codeArea.setLineHighlighterOn(true);
                    codeArea.requestFollowCaret();
                }
        );

    }


    public void saveFile(ActionEvent actionEvent) {

        if (overwrite) {

            String workignDir = Context.getGitRepoService().getRepository().getDirectory().getAbsolutePath().replace(".git", "");
            Path path = Path.of(workignDir, fileName);
            try {
                Files.write(path, codeArea.getText().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                saveBtn.setDisable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {



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
}
