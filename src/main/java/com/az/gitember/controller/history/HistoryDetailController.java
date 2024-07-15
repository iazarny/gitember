package com.az.gitember.controller.history;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.common.GitemberLineNumberFactory;
import com.az.gitember.controller.common.TextToSpanContentAdapter;
import com.az.gitember.controller.editor.EditorController;
import com.az.gitember.controller.handlers.DiffWithDiskEventHandler;
import com.az.gitember.controller.handlers.ShowHistoryEventHandler;
import com.az.gitember.controller.workingcopy.WorkingcopyTableGraphicsValueFactory;
import com.az.gitember.controller.handlers.DiffEventHandler;
import com.az.gitember.controller.handlers.OpenFileEventHandler;
import com.az.gitember.data.*;
import com.az.gitember.service.Context;
import com.az.gitember.service.ExtensionMap;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HistoryDetailController implements Initializable {

    private final static Logger log = Logger.getLogger(HistoryDetailController.class.getName());

    public MenuItem openDiffMenuItem;
    public MenuItem diffWithPrevVersionMenuItem;
    public MenuItem diffWithCurrentVersionMenuItem;
    public MenuItem diffWithDiskVersionMenuItem;
    public Tab rawDiffTab;
    public Tab mainTab;
    public CodeArea codeArea;
    public String  content;
    public TextField searchText;

    @FXML
    private TextField msgLbl;

    @FXML
    private TextField authorLbl;

    @FXML
    private TextField emailLbl;

    @FXML
    private TextField dateLbl;

    @FXML
    private TextField shaLbl;

    @FXML
    private TextField refsLbl;

    @FXML
    private TextField parentLbl;

    @FXML
    private TableView changedFilesListView;

    @FXML
    private TableColumn<ScmItem, StackedFontIcon> actionTableColumn;

    @FXML
    private TableColumn<ScmItem, String> fileTableColumn;

    @FXML
    private ContextMenu scmItemContextMenu;

    private int startIndex = -1;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();

        msgLbl.setText(scmInfo.getFullMessage().replace("\n", ""));
        authorLbl.setText(scmInfo.getAuthorName());
        emailLbl.setText(scmInfo.getAuthorEmail());
        dateLbl.setText(scmInfo.getDateFormated());
        refsLbl.setText(
                scmInfo.getRef().stream().collect(Collectors.joining(", ")));
        parentLbl.setText(
                scmInfo.getParents().stream().collect(Collectors.joining(", "))
        );
        shaLbl.setText(scmInfo.getRevisionFullName());

        changedFilesListView.setItems(
                FXCollections.observableList(scmInfo.getAffectedItems())
        );


        Context.searchValue.addListener(
                (observable, oldValue, newValue) -> {
                    changedFilesListView.refresh();
                }
        );
        changedFilesListView.setRowFactory( tr -> {

            return new TableRow<ScmItem>() {
                @Override
                protected void updateItem(ScmItem item, boolean empty) {
                    super.updateItem(item, empty);
                    String style = "";
                    if (!empty) {
                        String searchTerm = Context.searchValue.getValueSafe().toLowerCase();
                        Map<String, Set<String>> searchResult = Context.searchResult.getValue();
                        if (searchResult != null && !searchResult.isEmpty()) {
                            Set<String> affectedFiles = searchResult.getOrDefault(
                                    Context.scmRevCommitDetails.getValue().getRevisionFullName(),
                                    Collections.EMPTY_SET
                            );
                            boolean found = item.getViewRepresentation().toLowerCase().contains(searchTerm)
                                    || affectedFiles.contains(item.getViewRepresentation());
                            if (found) {
                                style = LookAndFeelSet.FOUND_ROW;
                            }
                        }

                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                final ScmItem scmItem =
                                        (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
                                if (ExtensionMap.isTextExtension(scmItem.getShortName())) {
                                    openDiffPrevVersion(null);
                                } else {
                                    new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(null);
                                }
                            }
                        });
                    }
                    setStyle(style);

                }
            };

        });


        fileTableColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getViewRepresentation())
        );
        actionTableColumn.setCellValueFactory(
                c -> new WorkingcopyTableGraphicsValueFactory(c.getValue().getAttribute().getStatus(), null)
        );

        changedFilesListView.setOnContextMenuRequested( e-> {

            final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
            final boolean disableDiff = !ExtensionMap.isTextExtension(scmItem.getShortName());

            openDiffMenuItem.setDisable(disableDiff);
            diffWithPrevVersionMenuItem.setDisable(disableDiff);;
            diffWithCurrentVersionMenuItem.setDisable(disableDiff);;
            diffWithDiskVersionMenuItem.setDisable(disableDiff);;


        }  );

        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    searchValue(newValue);
                }
        );

    }

    public void openItemMenuItemClickHandler(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(actionEvent);
    }

    public void openRawDiff(ActionEvent actionEvent) {
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.RAW_DIFF).handle(actionEvent);
    }

    public void fileHistoryItemMenuItemClickHandler(ActionEvent actionEvent) throws IOException {
        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        new ShowHistoryEventHandler(scmInfo.getRevisionFullName(), scmItem).handle(actionEvent);
    }

    public void openDiffPrevVersion(ActionEvent actionEvent) {
        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final String newRev = scmInfo.getRevisionFullName();
        final RevCommit newRevCommit = Context.getGitRepoService().getRevCommitBySha(newRev);
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String oldRev;
        try {
            final List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                    scmItem.getShortName(),
                    Context.scmRevCommitDetails.get().getRevisionFullName()
            );
            oldRev = fileRevs.stream()
                    .filter(scm -> scm.getDate().before(newRevCommit.getAuthorIdent().getWhen()))
                    .findFirst().map(i -> i.getRevisionFullName()).orElseGet(() -> null);
            final RevCommit oldRevCommit = Context.getGitRepoService().getRevCommitBySha(oldRev);
            final DiffEventHandler eventHandler = new DiffEventHandler(scmItem, fileRevs, oldRevCommit, newRevCommit);
            eventHandler.handle(actionEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void openDiffLastVersion(ActionEvent actionEvent) throws Exception {
        final ScmRevisionInformation scmInfo = Context.scmRevCommitDetails.get();
        final String oldRev = scmInfo.getRevisionFullName();
        final RevCommit oldRevCommit = Context.getGitRepoService().getRevCommitBySha(oldRev);
        final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
        final String newRev;
        try {
            final List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                    scmItem.getShortName(),
                    null);//
            //Rev can be located in the three, so need to merge
            fileRevs.addAll(
                    Context.getGitRepoService().getFileHistory(
                            scmItem.getShortName(),
                            Context.scmRevCommitDetails.get().getRevisionFullName())
            );
            newRev = fileRevs.stream().distinct().collect(Collectors.toList()).stream()
                    //.filter(scm -> scm.getDate().after(oldRevCommit.getAuthorIdent().getWhen()))
                    .findFirst().map(i -> i.getRevisionFullName()).orElseGet(() -> null);
            final RevCommit newRevCommit = Context.getGitRepoService().getRevCommitBySha(newRev);
            final DiffEventHandler eventHandler = new DiffEventHandler(scmItem, fileRevs, oldRevCommit, newRevCommit);
            eventHandler.handle(actionEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public void openDiffFileVersion(ActionEvent actionEvent) {
        try {
            final ScmItem scmItem = (ScmItem) changedFilesListView.getSelectionModel().getSelectedItem();
            final List<ScmRevisionInformation> fileRevs = Context.getGitRepoService().getFileHistory(
                    scmItem.getShortName(),
                    null); //TODO current tree only, not all
            if (ExtensionMap.isTextExtension(scmItem.getShortName())) {
                new DiffWithDiskEventHandler(scmItem, fileRevs, scmItem.getCommitName()).handle(actionEvent);
            } else {
                new OpenFileEventHandler(scmItem, ScmItem.BODY_TYPE.COMMIT_VERSION).handle(actionEvent);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void tabSelectionChanged(Event event) {

        if (rawDiffTab.isSelected()) {

            TextToSpanContentAdapter adapter =
                    new TextToSpanContentAdapter(FilenameUtils.getExtension("rawDiff.txt"), true);

            content = Context.getGitRepoService().getRawDiff(
                    Context.scmRevCommitDetails.get().getRevisionFullName(),
                    null);
            codeArea.appendText(content);

            codeArea.setStyle(LookAndFeelSet.CODE_AREA_CSS);
            codeArea
                    .getScene()
                    .getStylesheets()
                    .add(this.getClass().getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());
            codeArea.setParagraphGraphicFactory(
                    GitemberLineNumberFactory.get(codeArea, adapter, null, -1));

            StyleSpans<Collection<String>> spans = adapter.computeHighlighting(codeArea.getText());
            if (spans != null) {
                codeArea.setStyleSpans(0, spans);
            }

            adapter.getDiffDecoration(codeArea.getText()).entrySet().forEach(p -> {
                codeArea.setParagraphStyle(p.getKey(), p.getValue());
            });

            codeArea.moveTo(0, 0);
            codeArea.setShowCaret(Caret.CaretVisibility.ON);
            codeArea.setLineHighlighterOn(true);
            codeArea.requestFollowCaret();

        }
    }


    public void nextSearch(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER) {
            startIndex++;
            searchValue(searchText.getText());
        }
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

    public void saveFile(ActionEvent actionEvent) {


        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter anyFilter = new FileChooser.ExtensionFilter("Any files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(anyFilter);

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
