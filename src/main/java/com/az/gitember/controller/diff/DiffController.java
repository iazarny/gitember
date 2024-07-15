package com.az.gitember.controller.diff;

import com.az.gitember.control.VirtualizedOverviewScrollPane;
import com.az.gitember.controller.common.GitemberLineNumberFactory;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.common.ScmRevisionInformationCellFactory;
import com.az.gitember.controller.common.ScmRevisionInformationConverter;
import com.az.gitember.controller.common.TextToSpanContentAdapter;
import com.az.gitember.controller.handlers.EscEventHandler;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.data.SquarePos;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DiffController implements Initializable {

    public CodeArea oldCodeArea;
    public CodeArea newCodeArea;
    public Pane diffDrawPanel;
    public GridPane mainPanel;
    public VirtualizedOverviewScrollPane<CodeArea> oldScrollPane;
    public VirtualizedScrollPane<CodeArea> newScrollPane;
    public TextField searchTextOld;
    public TextField searchTextNew;
    public ToolBar navBar;
    public Button prevBtn;
    public Button nextBtn;
    public ComboBox<ScmRevisionInformation> oldRevisionsCmb;
    public ComboBox<ScmRevisionInformation> newRevisionsCmb;

    private String oldText = null;
    private String newText = null;

    private EditList diffList = new EditList();

    private int oldStartIndex = -1;
    private int newStartIndex = -1;
    private int currentDiff = -1;

    private String oldFileName;
    private String newFileName;

    private RawText oldRawTxt = null;
    private RawText newRawTxt = null;

    private double fontSize;

    private final ObservableList<ScmRevisionInformation> revisions =
            FXCollections.observableList(new ArrayList<>());

    private ScmItem scmItem;
    private String newSha;
    private String oldSha;

    public void setData(ScmItem item, List<ScmRevisionInformation> fileRevs,
                        String oldSha, String newSha) throws Exception {
        this.scmItem = item;
        this.oldSha = oldSha;
        this.newSha = newSha;
        revisions.addAll(fileRevs);
        oldRevisionsCmb.setItems(revisions);
        Optional<ScmRevisionInformation> oldRevOpt = revisions.stream()
                .filter(i-> i.getRevisionFullName().equals(oldSha))
                .findFirst();
        if (oldRevOpt.isEmpty()) {
            oldRevOpt = Optional.of(revisions.get(0));
        }
        oldRevOpt.ifPresent(
                scmRevInfo -> {oldRevisionsCmb.getSelectionModel().select(scmRevInfo);}
        );

        if (newSha == null) {
            ScmRevisionInformation rev = new ScmRevisionInformation("Disk version ");
            ObservableList<ScmRevisionInformation> revisions = FXCollections.observableList(Collections.singletonList(rev));
            newRevisionsCmb.setItems(revisions);
            newRevisionsCmb.getSelectionModel().select(0);
        } else {
            newRevisionsCmb.setItems(revisions);
            revisions.stream()
                    .filter(i-> i.getRevisionFullName().equals(newSha))
                    .findFirst()
                    .ifPresent(
                            scmRevInfo -> {newRevisionsCmb.getSelectionModel().select(scmRevInfo);}
                    );
        }




    }



    private boolean updateAllowed = true;
    boolean oldScrolled = false;
    boolean newScrolled = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        fontSize = LookAndFeelSet.FONT_SIZE;

        oldRevisionsCmb.setConverter(new ScmRevisionInformationConverter());
        oldRevisionsCmb.setCellFactory(new ScmRevisionInformationCellFactory());

        newRevisionsCmb.setConverter(new ScmRevisionInformationConverter());
        newRevisionsCmb.setCellFactory(new ScmRevisionInformationCellFactory());

        oldCodeArea = new CodeArea();
        oldCodeArea.setStyle(LookAndFeelSet.CODE_AREA_CSS);
        oldCodeArea.setPrefWidth(Region.USE_COMPUTED_SIZE);
        oldCodeArea.setMinWidth(Region.USE_PREF_SIZE);
        oldCodeArea.setEditable(false);

        newCodeArea = new CodeArea();
        newCodeArea.setStyle(LookAndFeelSet.CODE_AREA_CSS);
        newCodeArea.setPrefWidth(Region.USE_COMPUTED_SIZE);
        newCodeArea.setMinWidth(Region.USE_PREF_SIZE);
        newCodeArea.setEditable(false);

        initCodePanels();

        mainPanel.addEventHandler(KeyEvent.KEY_PRESSED, new EscEventHandler(mainPanel));

        diffDrawPanel.widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                updatePathElements();
                updateDiffOverview();
            });
        });

        searchTextOld.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    searchValue(searchTextOld, newValue);
                }
        );
        searchTextNew.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    searchValue(searchTextNew, newValue);
                }
        );

        navBar.toFront();
    }


    private void initCodePanels() {

        if (oldScrollPane != null) {
            mainPanel.getChildren().remove(oldScrollPane);
            mainPanel.getChildren().remove(newScrollPane);
        }

        oldScrollPane = new VirtualizedOverviewScrollPane<>(oldCodeArea);
        newScrollPane = new VirtualizedScrollPane<>(newCodeArea);

        VBox.setVgrow(oldScrollPane, Priority.ALWAYS);
        VBox.setVgrow(oldScrollPane, Priority.ALWAYS);
        VBox.setVgrow(newScrollPane, Priority.ALWAYS);
        HBox.setHgrow(oldScrollPane, Priority.ALWAYS);
        HBox.setHgrow(newScrollPane, Priority.ALWAYS);

        oldScrollPane.setPrefHeight(2024);

        mainPanel.add(oldScrollPane, 0, 1);
        mainPanel.add(newScrollPane, 2, 1);

        mainPanel.layout();

        try {
            ScrollBar hbar = (ScrollBar) GitemberUtil.getField(oldScrollPane, "hbar");
            hbar.addEventHandler(MouseEvent.ANY,      event -> {

                if (MouseEvent.DRAG_DETECTED == event.getEventType()
                        || (MouseEvent.MOUSE_ENTERED == event.getEventType())
                        || (MouseEvent.MOUSE_MOVED == event.getEventType())) {
                    updateAllowed = false;
                } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()
                        || (MouseEvent.MOUSE_EXITED_TARGET == event.getEventType() && MouseButton.NONE == event.getButton()) ) {
                    updateAllowed = true;
                }

            });


            hbar = (ScrollBar) GitemberUtil.getField(newScrollPane, "hbar");
            hbar.addEventHandler(MouseEvent.ANY,      event -> {
                if (MouseEvent.DRAG_DETECTED == event.getEventType()
                        || (MouseEvent.MOUSE_ENTERED == event.getEventType())
                        || (MouseEvent.MOUSE_MOVED == event.getEventType())) {
                    updateAllowed = false;
                } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()
                        || (MouseEvent.MOUSE_EXITED_TARGET == event.getEventType() && MouseButton.NONE == event.getButton()) ) {
                    updateAllowed = true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        oldScrollPane.totalHeightEstimateProperty().addListener(
                (ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
                    if (new_val != null) {
                        oldTotalHeight = new_val.doubleValue();
                    }

                }
        );

        newScrollPane.totalHeightEstimateProperty().addListener(
                (ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
                    if (new_val != null) {
                        newTotalHeight = new_val.doubleValue();
                    }
                }
        );

        oldScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
            if (updateAllowed) {
                oldScrolled = true;
                if (!newScrolled) {
                    Double val = oldScrollPane.snapSizeY( new_val.doubleValue()
                            * newTotalHeight / oldTotalHeight);
                    if (!val.isNaN()) {
                        newScrollPane.estimatedScrollYProperty().setValue(val);
                    }
                }
            }
        });

        newScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
            if (updateAllowed) {
                newScrolled = true;
                if (!oldScrolled) {
                    Double val = oldScrollPane.snapSizeY(new_val.doubleValue()
                            * oldTotalHeight / newTotalHeight);
                    if (!val.isNaN()) {
                        oldScrollPane.estimatedScrollYProperty().setValue(val);
                    }
                }
                updatePathElements();
            }
        });


        oldScrollPane.estimatedScrollYProperty().addListener(observable -> {
            if (newScrolled && oldScrolled) {
                updatePathElements();
                updateDiffOverview();
                newScrolled = oldScrolled = false;
            }
        });



        newScrollPane.estimatedScrollYProperty().addListener(observable -> {
            if (newScrolled && oldScrolled) {
                updatePathElements();
                updateDiffOverview();
                newScrolled = oldScrolled = false;
            }
        });


        oldScrollPane.getVbar().windowOffsetPercentProperty().addListener((newValue) -> {
            double perCent = oldScrollPane.getVbar().getWindowOffsetPercent();
            double oldScrollPanelNewVal = oldScrollPane.totalHeightEstimateProperty().getValue() * perCent;
            double newScrollPanelNewVal = newScrollPane.totalHeightEstimateProperty().getValue() * perCent;
            oldScrollPane.estimatedScrollYProperty().setValue(oldScrollPanelNewVal);
            newScrollPane.estimatedScrollYProperty().setValue(newScrollPanelNewVal);
            newScrolled = oldScrolled = true;
        });

        newScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                updatePathElements();
                updateDiffOverview();
            });
        });
    }

    double oldTotalHeight = 1;
    double newTotalHeight = 1;


    private void updateDiffOverview() {
        try  {
            oldScrollPane.getVbar().setHilightPosSize(oldScrollPane.estimatedScrollYProperty().getValue()
                            / oldScrollPane.totalHeightEstimateProperty().getValue(),
                    oldCodeArea.heightProperty().getValue() / oldCodeArea.totalHeightEstimateProperty().getValue()
            );
        } catch (Exception e) {

        }

    }

    private SquarePos getDiffPos(Edit delta) {

        final int leftPos = delta.getBeginA();
        final int leftLines = delta.getLengthA();
        final int rightPos = delta.getBeginB();
        final int rightLines = delta.getLengthB();

        int origBottomPos = leftPos + leftLines;
        int revBottomPos = rightPos + rightLines;


        double deltaY1 = oldScrollPane.estimatedScrollYProperty().getValue();

        double deltaY2 = newScrollPane.estimatedScrollYProperty().getValue();
        int border_shift = 0; //uber node and 1 node border


        int x1 = -1;
        int y1 = (int) (leftPos * fontSize - deltaY1) + border_shift;

        int x2 = (int) diffDrawPanel.getWidth() + 1;
        int y2 = (int) (rightPos * fontSize - deltaY2) + border_shift;

        int x3 = x2;
        int y3 = (int) (revBottomPos * fontSize - deltaY2) + border_shift;

        int x4 = x1;
        int y4 = (int) (origBottomPos * fontSize - deltaY1) + border_shift;

        return new SquarePos(x1, y1, x2, y2, x3, y3, x4, y4);

    }

    private void updatePathElements() {

        for (int i = 0; i < diffDrawPanel.getChildren().size(); i++) {
            final Path path = (Path) diffDrawPanel.getChildren().get(i);

            final Edit delta = this.diffList.get(i);

            SquarePos squarePos = getDiffPos(delta);

            MoveTo moveTo = (MoveTo) path.getElements().get(0);
            CubicCurveTo curve0 = (CubicCurveTo) path.getElements().get(1);
            LineTo lineTo0 = (LineTo) path.getElements().get(2);
            CubicCurveTo curve1 = (CubicCurveTo) path.getElements().get(3);
            LineTo lineTo1 = (LineTo) path.getElements().get(4);

            moveTo.setX(squarePos.getX1());
            moveTo.setY(squarePos.getY1());

            CubicCurveTo ccTo = getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY2());
            curve0.setX(squarePos.getX2());
            curve0.setY(squarePos.getY2());
            curve0.setControlX1(ccTo.getControlX1());
            curve0.setControlX2(ccTo.getControlX2());
            curve0.setControlY1(ccTo.getControlY1());
            curve0.setControlY2(ccTo.getControlY2());


            lineTo0.setX(squarePos.getX3());
            lineTo0.setY(squarePos.getY3());

            ccTo = getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
            curve1.setX(squarePos.getX4());
            curve1.setY(squarePos.getY4());
            curve1.setControlX1(ccTo.getControlX1());
            curve1.setControlX2(ccTo.getControlX2());
            curve1.setControlY1(ccTo.getControlY1());
            curve1.setControlY2(ccTo.getControlY2());

            lineTo1.setX(squarePos.getX1());
            lineTo1.setY(squarePos.getY1());

        }
    }


    private int getLines(final String content) {
        return new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList()).size();
    }


    private void scrollToFirstDiff() {
        int totalOldLines = getLines(oldText);
        int totalNewLines = getLines(newText);
        //if (totalNewLines > 40 && totalOldLines > 40) {
            if (!this.diffList.isEmpty()) {
                currentDiff = 0;
                scrollToDiff(true);
            }
        //}
    }

    private void scrollToDiff(boolean forward) {
        Edit delta = this.diffList.get(currentDiff);
        final int origPos;
        final int revPos;
        if (forward) {
            origPos = Math.max(0, Math.min(delta.getEndA(), delta.getEndB() ) - 5);
            revPos = Math.max(0, Math.min(delta.getEndA(), delta.getEndB() ) -5);

        } else {
            origPos = Math.max(0, Math.min(delta.getBeginA(), delta.getBeginB() )  + 5);
            revPos = Math.max(0, Math.min(delta.getBeginA(), delta.getBeginB() ) + 5);
        }

        //oldCodeArea.moveTo(origPos, 0);
        //newCodeArea.moveTo(revPos, 0);
        oldCodeArea.requestFollowCaret();
        oldCodeArea.layout();
        newCodeArea.requestFollowCaret();
        newCodeArea.layout();

        //updateButtonState();

        //highlight active diff
        for (int i = 0; i <diffDrawPanel.getChildren().size(); i++) {
            diffDrawPanel.getChildren().get(i).getStyleClass().remove("diff-active");
        }
        diffDrawPanel.getChildren().get(currentDiff).getStyleClass().add("diff-active");

        setText(oldCodeArea, oldText, oldFileName, true, currentDiff);
        setText(newCodeArea, newText, newFileName, false, currentDiff);

        //System.out.println(" orig pos " + origPos + " rev " + revPos);
        //oldScrollPane.estimatedScrollYProperty().setValue(fontSize * origPos);
        //newScrollPane.estimatedScrollYProperty().setValue(fontSize * revPos);

    }


    private void createPathElements() {

        diffDrawPanel.getChildren().clear();
        for (Edit delta : this.diffList) {
            SquarePos squarePos = getDiffPos(delta);
            MoveTo moveTo = new MoveTo(squarePos.getX1(), squarePos.getY1());
            CubicCurveTo curve0 = getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY4());
            LineTo lineTo0 = new LineTo(squarePos.getX3(), squarePos.getY3());
            CubicCurveTo curve1 = getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
            LineTo lineTo1 = new LineTo(squarePos.getX1(), squarePos.getY1());

            Path path = new Path();
            path.getElements().addAll(moveTo, curve0, lineTo0, curve1, lineTo1);
            path.getStyleClass().add(GitemberUtil.getDiffSyleClass(delta, "diff"));

            diffDrawPanel.getChildren().add(path);
        }
    }

    private CubicCurveTo getCubicCurveTo(int x1, int y1, int x2, int y2) {
        int controlPointDeltaX = 15;
        return new CubicCurveTo(
                x2 > x1 ? diffDrawPanel.getWidth() - controlPointDeltaX : controlPointDeltaX, y1,
                x2 > x1 ? controlPointDeltaX : diffDrawPanel.getWidth() - controlPointDeltaX, y2,
                x2, y2);
    }

    private void setText(final CodeArea codeArea,
                         final String text, final String fileName, final boolean leftSide) {
        setText(codeArea,text,fileName,leftSide,-1);
    }

    private void setText(final CodeArea codeArea,
                         final String text, final String fileName,
                         final boolean leftSide,
                         final int activeParagrah) {

        codeArea.clear();
        codeArea.appendText(text);

        TextToSpanContentAdapter adapter = new TextToSpanContentAdapter(
                FilenameUtils.getExtension(fileName),
                this.diffList, leftSide, activeParagrah);

        StyleSpans<Collection<String>> spans = adapter.computeHighlighting(codeArea.getText());
        if (spans != null) {
            codeArea.setStyleSpans(0, spans);
        }

        Map<Integer, List<String>> decoration = adapter.getDecorateByPatch(activeParagrah);
        decoration.forEach(codeArea::setParagraphStyle);

        codeArea.setParagraphGraphicFactory(
                GitemberLineNumberFactory.get(codeArea, adapter, null, activeParagrah));


    }




    public void repeatSearch(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.getSource() == searchTextOld) {
                oldStartIndex++;
                searchValue((TextField) keyEvent.getSource(), searchTextOld.getText());
            } else {
                newStartIndex++;
                searchValue((TextField) keyEvent.getSource(), searchTextNew.getText());
            }
        }
    }

    public void searchValue(TextField textField, String value) {
        final CodeArea searchCodeArea;
        int startIndex;
        if (textField == searchTextOld) {
            searchCodeArea = oldCodeArea;
            startIndex = oldStartIndex;
        } else {
            searchCodeArea = newCodeArea;
            startIndex = newStartIndex;
        }

        startIndex = searchCodeArea.getText().indexOf(value, startIndex);
        if (startIndex == -1) {
            startIndex = searchCodeArea.getText().indexOf(value);
        }

        if (startIndex > -1) {
            searchCodeArea.moveTo(startIndex);
            searchCodeArea.selectRange(startIndex, startIndex + value.length());

        } else {
            searchCodeArea.selectRange(0, 0);
        }
        searchCodeArea.requestFollowCaret();

        if (textField == searchTextOld) {
            oldStartIndex = startIndex;
        } else {
            newStartIndex = startIndex;
        }

    }

    public void prevHandler(ActionEvent actionEvent) {
        if (0 < currentDiff) {
            currentDiff--;

        }
        scrollToDiff(true);
    }

    public void nextHandler(ActionEvent actionEvent) {
        if ((this.diffList.size() - 1) > currentDiff) {
            currentDiff++;

        }
        scrollToDiff(false);
    }


    private void updateButtonState() {
        prevBtn.setDisable(currentDiff <= 0);
        nextBtn.setDisable( ( this.diffList.size() - 1)
                <= currentDiff );
    }


    public void oldRevisionChange(ActionEvent actionEvent)  {
        String filaNameCandidate =  scmItem.getShortName();

        try {
            if (scmItem.getAttribute().getStatus() != null) {
                if (scmItem.getAttribute().getStatus().equals(ScmItem.Status.RENAMED)) {
                    filaNameCandidate = scmItem.getAttribute().getOldName();
                }
            }
            oldFileName = Context.getGitRepoService().saveFile( oldRevisionsCmb.getValue().getRevisionFullName(), filaNameCandidate);

        } catch (Exception e) {
            oldFileName = Context.getGitRepoService().createEmptyFile(scmItem.getShortName());
        }
        try {
            this.oldText = Files.readString(Paths.get(oldFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.oldRawTxt = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        updateDiffRepresentation();
        oldRevisionsCmb.setDisable(true);
    }

    public void newRevisionChange(ActionEvent actionEvent)  {
        try {
            if (newSha == null) {
                newFileName = java.nio.file.Path.of(Context.getProjectFolder(), scmItem.getShortName()).toString();
            } else {
                newFileName = Context.getGitRepoService().saveFile( newRevisionsCmb.getValue().getRevisionFullName(), scmItem.getShortName());
            }
        } catch (Exception e) {
            newFileName = Context.getGitRepoService().createEmptyFile(scmItem.getShortName());
        }
        try {
            this.newText = Files.readString(Paths.get(newFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.newRawTxt = new RawText(newText.getBytes(StandardCharsets.UTF_8));
        updateDiffRepresentation();
        newRevisionsCmb.setDisable(true);
    }


    public void updateDiffRepresentation()  {

        if (oldRawTxt != null && newRawTxt != null) {
            setText(oldCodeArea, oldText, oldFileName, true);
            setText(newCodeArea, newText, newFileName, false);
            //initCodePanels();
            DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
            diffList.addAll(diffAlgorithm.diff(RawTextComparator.WS_IGNORE_ALL, oldRawTxt, newRawTxt));
            oldScrollPane.getVbar().setData(oldText, newText, diffList);
            createPathElements();
            scrollToFirstDiff();
        }
    }


}