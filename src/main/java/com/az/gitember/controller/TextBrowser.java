package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.data.Const;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.blame.BlameResult;
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
    public TextField searchText;
    public CheckBox annotationCb;
    private String content;
    private String fileName;
    private boolean overwrite;
    private boolean diff;
    private int startIndex = -1;
    private ScmItem scmItem;
    private TextToSpanContentAdapter adapter;

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

        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    searchValue(newValue);
                }
        );

        searchText.setOnKeyPressed(
                evt -> {
                    if (evt.getCode() == KeyCode.ENTER) {
                        startIndex++;
                        searchValue(searchText.getText());
                    }
                }
        );

        annotationCb.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    BlameResult blameResult = null;
                    try {
                        if (newValue) {
                            blameResult = Context.getGitRepoService().blame(scmItem);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    codeArea.setParagraphGraphicFactory(
                            GitemberLineNumberFactory.get(codeArea, adapter, blameResult, -1));

                }
        );

    }

    private void searchValue(String newValue) {
        startIndex = codeArea.getText().indexOf(newValue, startIndex);
        if (startIndex == -1) {
            startIndex = codeArea.getText().indexOf(newValue);
        }

        if ( startIndex == -1 && StringUtils.isNotBlank(newValue)) {
            return;

        } else if (startIndex > -1) {
            codeArea.moveTo(startIndex);
            codeArea.selectRange(startIndex,  startIndex + newValue.length());

        } else {
            codeArea.selectRange(0, 0);
        }
        codeArea.requestFollowCaret();

    }

    public void enableEdit(boolean allow) {
        this.codeArea.setEditable(allow);
        if (allow) {
            saveBtn.setText("Save");
            saveBtn.setDisable(true);
            annotationCb.setVisible(false);
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDiff(boolean diff) {
        this.diff = diff;
    }

    /**
     * Force set flag to the same file when save command handled.
     */
    public void setForceOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }


    public void setText(String text) {

        this.adapter = new TextToSpanContentAdapter(FilenameUtils.getExtension(fileName), diff);
        this.content = text;

        Platform.runLater(
                () -> {

                    codeArea.appendText(content);
                    codeArea.setParagraphGraphicFactory(
                            GitemberLineNumberFactory.get(codeArea, adapter, null,-1)
                    );

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

            String workignDir = Context.getGitRepoService().getRepository().getDirectory().getAbsolutePath().replace(Const.GIT_FOLDER, "");
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

    public void setScmItem(ScmItem scmItem) {
        this.scmItem = scmItem;
    }
}
