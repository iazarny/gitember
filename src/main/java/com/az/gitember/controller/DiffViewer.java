package com.az.gitember.controller;

import com.az.gitember.control.VirtualizedOverviewScrollPane;
import com.az.gitember.controller.handlers.EscEventHandler;
import com.az.gitember.data.SquarePos;
import com.az.gitember.service.GitemberUtil;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
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
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DiffViewer implements Initializable {

	private final static double VER_HEAD_HEIGHT = 30;

    public TextField newLabel;
    public TextField oldLabel;
    public CodeArea oldCodeArea;
    public CodeArea newCodeArea;
    public Pane diffDrawPanel;
    public GridPane mainPanel;
    public VirtualizedOverviewScrollPane<CodeArea> oldScrollPane;
    public VirtualizedScrollPane<CodeArea> newScrollPane;
    public RowConstraints firstRowConstraint;
    public RowConstraints secondRowConstraint;
    public TextField searchTextOld;
    public TextField searchTextNew;

    private String oldText = null;
    private String newText = null;
    private EditList diffList = new EditList();

    private int oldStartIndex = -1;
    private int newStartIndex = -1;

    private double fontSize;


    public void setData(String oldFileName, String newFileName) throws IOException {

        this.oldText = Files.readString(Paths.get(oldFileName));
        this.newText = Files.readString(Paths.get(newFileName));

        RawText oldRawTxt = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        RawText newRawTxt = new RawText(newText.getBytes(StandardCharsets.UTF_8));

        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        RawTextComparator comparator = RawTextComparator.WS_IGNORE_ALL;

        diffList.addAll(diffAlgorithm.diff(comparator, oldRawTxt, newRawTxt));

        setText(oldCodeArea, oldText, oldFileName, true);
        setText(newCodeArea, newText, newFileName, false);

        oldScrollPane.getVbar().setData(oldText, newText, diffList);

        createPathElements();
        scrollToFirstDiff();

    }

    public void setOldLabel(String text) {
        oldLabel.setText(text);
        oldLabel.setEditable(false);
        oldLabel.getStyleClass().add("copy-label");
    }

    public void setNewLabel(String text) {
        newLabel.setText(text);
        newLabel.setEditable(false);
        newLabel.getStyleClass().add("copy-label");
    }

    private boolean updateAllowed = true;
    private boolean drawAllowed = true;

    boolean oldScrolled = false;
    boolean newScrolled = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        fontSize = LookAndFeelSet.FONT_SIZE;

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

        oldScrollPane = new VirtualizedOverviewScrollPane<>(oldCodeArea);
        newScrollPane = new VirtualizedScrollPane<>(newCodeArea);

        mainPanel.add(oldScrollPane, 0, 1);
        mainPanel.add(newScrollPane, 2, 1);

        VBox.setVgrow(oldScrollPane, Priority.ALWAYS);
        VBox.setVgrow(oldScrollPane, Priority.ALWAYS);
        VBox.setVgrow(newScrollPane, Priority.ALWAYS);
        HBox.setHgrow(oldScrollPane, Priority.ALWAYS);
        HBox.setHgrow(newScrollPane, Priority.ALWAYS);

        oldScrollPane.setPrefHeight(2021);

		mainPanel.addEventHandler(KeyEvent.KEY_PRESSED, new EscEventHandler(mainPanel));
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

        oldScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
            if (updateAllowed) {
                oldScrolled = true;
                if (!newScrolled) {
                    Double val = oldScrollPane.snapSizeY( new_val.doubleValue() * newScrollPane.totalHeightEstimateProperty().getValue() / oldScrollPane.totalHeightEstimateProperty().getValue());
                    newScrollPane.estimatedScrollYProperty().setValue(val);
                }
            }
        });

        oldScrollPane.estimatedScrollYProperty().addListener(observable -> {
            if (newScrolled && oldScrolled) {
                updatePathElements();
                updateDiffOverview();
                newScrolled = oldScrolled = false;
            }
        });

        newScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
            if (updateAllowed) {
                newScrolled = true;
                if (!oldScrolled) {
                    Double val = oldScrollPane.snapSizeY(new_val.doubleValue() * oldScrollPane.totalHeightEstimateProperty().getValue() / newScrollPane.totalHeightEstimateProperty().getValue());
                    oldScrollPane.estimatedScrollYProperty().setValue(val);
                }
                updatePathElements();

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

        diffDrawPanel.widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                updatePathElements();
                updateDiffOverview();
            });
        });

        //Fix the top of table row size
        firstRowConstraint.setMaxHeight(VER_HEAD_HEIGHT);
        firstRowConstraint.setMinHeight(VER_HEAD_HEIGHT);
        firstRowConstraint.setPrefHeight(VER_HEAD_HEIGHT);

        mainPanel.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    double val = newValue.doubleValue() - VER_HEAD_HEIGHT;
                    secondRowConstraint.setMaxHeight(val);
                    secondRowConstraint.setMinHeight(val);
                    secondRowConstraint.setPrefHeight(val);
                }
        );

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
    }


    private void updateDiffOverview() {
        oldScrollPane.getVbar().setHilightPosSize(oldScrollPane.estimatedScrollYProperty().getValue()
                        / oldScrollPane.totalHeightEstimateProperty().getValue(),
                oldCodeArea.heightProperty().getValue() / oldCodeArea.totalHeightEstimateProperty().getValue()
        );
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
        if (totalNewLines > 40 && totalOldLines > 40) {
            //TODO collect and add to < > button navigation
            //TODO h scroll pos as well
            if (!this.diffList.isEmpty()) {
                Edit delta = this.diffList.get(0);
                final int origPos = Math.max(0, Math.min(delta.getBeginA(), delta.getBeginA() - 5));
                final int revPos = Math.max(0, Math.min(delta.getBeginA(), delta.getBeginB() - 5));
                oldCodeArea.moveTo(origPos, 0);
                newCodeArea.moveTo(revPos, 0);
                oldCodeArea.requestFollowCaret();
                oldCodeArea.layout();
                newCodeArea.requestFollowCaret();
                newCodeArea.layout();
            }

        }
    }

    private void createPathElements() {

        for (Edit delta : this.diffList) {
            SquarePos squarePos = getDiffPos(delta);
            MoveTo moveTo = new MoveTo(squarePos.getX1(), squarePos.getY1());
            CubicCurveTo curve0 = getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY4());
            LineTo lineTo0 = new LineTo(squarePos.getX3(), squarePos.getY3());
            CubicCurveTo curve1 = getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
            LineTo lineTo1 = new LineTo(squarePos.getX1(), squarePos.getY1());

            Path path = new Path();
            path.getElements().addAll(moveTo, curve0, lineTo0, curve1, lineTo1);
            path.getStyleClass().add(GitemberUtil.getDiffSyleClass(delta, "diff-path"));

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

        codeArea.appendText(text);

        TextToSpanContentAdapter adapter = new TextToSpanContentAdapter(
                FilenameUtils.getExtension(fileName),
                this.diffList, leftSide);

        codeArea.setParagraphGraphicFactory(
                GitemberLineNumberFactory.get(codeArea, adapter, null));

        StyleSpans<Collection<String>> spans = adapter.computeHighlighting(codeArea.getText());
        if (spans != null) {
            codeArea.setStyleSpans(0, spans);
        }

        adapter.getDecorateByPatch().forEach(codeArea::setParagraphStyle);

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
}