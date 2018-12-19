package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;
import com.az.gitember.ui.DerivedScrollEvent;
import com.az.gitember.ui.DerivedScrollEventType;
import com.az.gitember.ui.DiffLineNumberFactory;
import com.az.gitember.ui.ShaTextField;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.fxmisc.flowless.Cell;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyledTextArea;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.reactfx.util.FxTimer;
import org.reactfx.value.Var;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * Created by Igor_Azarny on 1- Dec- 2016.
 */
public class DiffViewController extends BaseFileViewController {


    private final static int NO_DIFF = -1;
    private final static int DELAY = 30;

    private GridPane gridPanel;

    int diffIdx = NO_DIFF;

    private Button buttonNext;
    private Button buttonPrev;


    private VirtualizedScrollPane<CodeArea> oldVSPane;
    private CodeArea oldCodeArea;
    private TextField oldLabel;

    private Pane scrollPane;

    private VirtualizedScrollPane newVSPane;
    private CodeArea newCodeArea;
    private TextField newLabel;

    private VirtualFlow oldVirtualFlow;
    private VirtualFlow newVirtualFlow;


    private Patch<String> patch;
    private List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> paragraphDiffList;
    private final List<Pair<Integer, Integer>> oldLinesToHighlight;
    private final List<Pair<Integer, Integer>> newLinesToHighlight;

    private double oldParagraphHeight = 22;
    private double newParagraphHeight = 22;

    public DiffViewController() {

        super();

        oldLinesToHighlight = new ArrayList<>();
        oldCodeArea = new CodeArea();
        oldCodeArea.setEditable(false);
        oldCodeArea.setParagraphGraphicFactory(DiffLineNumberFactory.get(oldCodeArea, oldLinesToHighlight));

        oldLabel = new ShaTextField();

        oldVSPane = new VirtualizedScrollPane<>(oldCodeArea, ScrollPane.ScrollBarPolicy.AS_NEEDED, ScrollPane.ScrollBarPolicy.NEVER);
        StackPane oldStackPane = new StackPane(oldVSPane);

        scrollPane = new Pane();
        scrollPane.setPrefWidth(75);

        newLinesToHighlight = new ArrayList<>();
        newCodeArea = new CodeArea();
        newCodeArea.setEditable(false);
        newCodeArea.setParagraphGraphicFactory(DiffLineNumberFactory.get(newCodeArea, newLinesToHighlight));
        newLabel = new ShaTextField();
        newVSPane = new VirtualizedScrollPane<>(newCodeArea);
        StackPane newStackPane = new StackPane(newVSPane);

        newStackPane.setPrefHeight(2000);


        gridPanel = new GridPane();

        ColumnConstraints oldColumn = new ColumnConstraints();
        oldColumn.setPercentWidth(45);
        gridPanel.getColumnConstraints().add(oldColumn);

        ColumnConstraints scrollColumn = new ColumnConstraints();
        scrollColumn.setPercentWidth(10);
        scrollColumn.setHalignment(HPos.CENTER);
        gridPanel.getColumnConstraints().add(scrollColumn);

        ColumnConstraints newlColumn = new ColumnConstraints();
        newlColumn.setPercentWidth(45);
        gridPanel.getColumnConstraints().add(newlColumn);

        buttonNext = new Button();
        buttonNext.setTooltip(new Tooltip("Next"));
        buttonNext.setGraphic(new FontIcon(FontAwesome.FORWARD));

        buttonPrev = new Button();
        buttonPrev.setTooltip(new Tooltip("Prev"));
        buttonPrev.setGraphic(new FontIcon(FontAwesome.BACKWARD));

        gridPanel.add(new ToolBar(new HBox(new Label("Revision:  "), oldLabel)), 0, 0);

        gridPanel.add(new ToolBar(new HBox(new Label("Revision:  "), newLabel)), 2, 0);

        gridPanel.add(scrollPane, 1, 1);
        gridPanel.add(oldStackPane, 0, 1);
        gridPanel.add(newStackPane, 2, 1);

        Region leftr = new Region();
        Region leftl = new Region();
        HBox.setHgrow(leftr,  Priority.ALWAYS);
        HBox.setHgrow(leftl,  Priority.ALWAYS);
        gridPanel.add(new HBox(leftl, buttonPrev, buttonNext, leftr), 1, 0);

        Field field = FieldUtils.getField(oldCodeArea.getClass(), "virtualFlow", true);
        try {
            oldVirtualFlow = (VirtualFlow) field.get(oldCodeArea);
            newVirtualFlow = (VirtualFlow) field.get(newCodeArea);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    private void eventSubscription() {


        final DerivedScrollEvent secScrollEvt = new DerivedScrollEvent(DerivedScrollEventType.ALL);


        /*oldCodeArea.estimatedScrollYProperty().addListener(
                (observable1, oldValue1, newValue1) -> {newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0);}

        );
        oldCodeArea.estimatedScrollXProperty().addListener(
                (observable1, oldValue1, newValue1) -> {newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0);}

        );*/
        oldVSPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            newVSPane.fireEvent(secScrollEvt);
            //paintChanges(e.getDeltaY(), 0);
            FxTimer.runLater( Duration.ofMillis(DELAY),  () -> paintChanges(0, 0) );
        });
        oldVSPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0); } );
        oldVSPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> { newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0); });
        oldVSPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> { newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0); });
        oldVSPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> { newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0); });
        oldVSPane.addEventFilter(KeyEvent.KEY_RELEASED, e -> { newVSPane.fireEvent(secScrollEvt); paintChanges(0, 0); });
        oldVSPane.addEventFilter(DerivedScrollEventType.ALL, e -> {
            oldCodeArea.estimatedScrollXProperty().setValue(
                    newCodeArea.estimatedScrollXProperty().getValue() / newCodeArea.totalWidthEstimateProperty().getValue() *
                            oldCodeArea.totalWidthEstimateProperty().getValue()
            );
            oldCodeArea.estimatedScrollYProperty().setValue(
                    newCodeArea.estimatedScrollYProperty().getValue() / newCodeArea.totalHeightEstimateProperty().getValue() *
                            oldCodeArea.totalHeightEstimateProperty().getValue()
            );
        });


        newVSPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            oldVSPane.fireEvent(secScrollEvt);
            //paintChanges(0, e.getDeltaY());
            FxTimer.runLater( Duration.ofMillis(DELAY),  () -> paintChanges(0, 0) ); });
        newVSPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { oldVSPane.fireEvent(secScrollEvt);  paintChanges(0, 0);});
        newVSPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> { oldVSPane.fireEvent(secScrollEvt);  paintChanges(0, 0);});
        newVSPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> { oldVSPane.fireEvent(secScrollEvt);  paintChanges(0, 0);});
        newVSPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> { oldVSPane.fireEvent(secScrollEvt);  paintChanges(0, 0);});
        newVSPane.addEventFilter(KeyEvent.KEY_RELEASED, e -> { oldVSPane.fireEvent(secScrollEvt);  paintChanges(0, 0);});
        newVSPane.addEventFilter(DerivedScrollEventType.ALL, e -> {
            newCodeArea.estimatedScrollXProperty().setValue(
                    oldCodeArea.estimatedScrollXProperty().getValue() / oldCodeArea.totalWidthEstimateProperty().getValue() *
                            newCodeArea.totalWidthEstimateProperty().getValue()
            );
            newCodeArea.estimatedScrollYProperty().setValue(
                    oldCodeArea.estimatedScrollYProperty().getValue() / oldCodeArea.totalHeightEstimateProperty().getValue() *
                            newCodeArea.totalHeightEstimateProperty().getValue()
            );
        });


        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            paintChanges(newValue.doubleValue(), this.scrollPane.getHeight(), 0, 0);
        });
        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            paintChanges(this.scrollPane.getWidth(), newValue.doubleValue(), 0, 0);
        });


        buttonNext.setOnAction(
                event -> {
                    if (diffIdx != NO_DIFF && diffIdx < paragraphDiffList.size() - 1) {
                        diffIdx++;
                        oldCodeArea.moveTo(paragraphDiffList.get(diffIdx).getFirst().getFirst(), 0);
                        newCodeArea.moveTo(paragraphDiffList.get(diffIdx).getSecond().getFirst(), 0);
                        oldCodeArea.requestFollowCaret();
                        oldCodeArea.layout();
                        newCodeArea.requestFollowCaret();
                        newCodeArea.layout();
                        newVSPane.fireEvent(secScrollEvt);
                        oldVSPane.fireEvent(secScrollEvt);
                        FxTimer.runLater( Duration.ofMillis(DELAY),  () -> paintChanges(0, 0) );
                    }
                }
        );

        buttonPrev.setOnAction(
                event -> {
                    if (diffIdx != NO_DIFF && diffIdx > 0) {
                        diffIdx--;
                        oldCodeArea.moveTo(paragraphDiffList.get(diffIdx).getFirst().getFirst(), 0);
                        newCodeArea.moveTo(paragraphDiffList.get(diffIdx).getSecond().getFirst(), 0);
                        oldCodeArea.requestFollowCaret();
                        oldCodeArea.layout();
                        newCodeArea.requestFollowCaret();
                        newCodeArea.layout();
                        newVSPane.fireEvent(secScrollEvt);
                        oldVSPane.fireEvent(secScrollEvt);
                        FxTimer.runLater( Duration.ofMillis(DELAY),  () -> paintChanges(0, 0) );
                    }
                }
        );


    }


    /**
     * Paint changes after scroll or resize event.
     *
     * @param oldCodeAreaDeltaY delta y introduced before event for old code area.
     * @param newCodeAreaDeltaY delta y introduced before event for new code area.
     */
    private void paintChanges(double oldCodeAreaDeltaY, double newCodeAreaDeltaY) {
        paintChanges(this.scrollPane.getWidth(), this.scrollPane.getHeight(), oldCodeAreaDeltaY, newCodeAreaDeltaY);
    }

    /**
     * Paint changes after scroll or resize event.
     *
     * @param oldCodeAreaDeltaY delta y introduced before event for old code area.
     * @param newCodeAreaDeltaY delta y introduced before event for new code area.
     * @param paintAreaWidth    painting area width
     * @param paintAreHeight    painting area height
     */
    private void paintChanges(double paintAreaWidth, double paintAreHeight,
                              double oldCodeAreaDeltaY, double newCodeAreaDeltaY) {


        List<Pair<Integer, Pair<Double, Double>>> oldParagraphBeginPoints = getParagraphBeginPints(oldVirtualFlow);
        List<Pair<Integer, Pair<Double, Double>>> newParagraphBeginPoints = getParagraphBeginPints(newVirtualFlow);

        adjustParagraphHeight(oldParagraphBeginPoints, newParagraphBeginPoints);

        int oldCodeFirstParagraphVisible = oldParagraphBeginPoints.get(0).getFirst();
        int oldCodeFirstVisibleParagraphY = oldParagraphBeginPoints.get(0).getSecond().getSecond().intValue() + (int) oldCodeAreaDeltaY;


        int newCodeFirstParagraphVisible = newParagraphBeginPoints.get(0).getFirst();
        int newCodeFirstVisibleParagraphY = newParagraphBeginPoints.get(0).getSecond().getSecond().intValue() + (int) newCodeAreaDeltaY;


        scrollPane.getChildren().removeAll(scrollPane.getChildren());
        for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair : paragraphDiffList) {
            scrollPane.getChildren().add(
                    getPathForChanges(
                            pair.getFirst().getFirst(), pair.getFirst().getSecond(), pair.getSecond().getFirst(), pair.getSecond().getSecond(),
                            paintAreaWidth, paintAreHeight,
                            this.oldParagraphHeight, this.newParagraphHeight,
                            oldCodeFirstParagraphVisible, oldCodeFirstVisibleParagraphY,
                            newCodeFirstParagraphVisible, newCodeFirstVisibleParagraphY,
                            Color.rgb(106, 206, 159)
                    )
            );
        }

    }

    /**
     * Calculate difference between new and old text.
     *
     * @return list of pairs, where each item has pair
     * of begin and end position of items, which are different.
     */
    private List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> calculateParagraphDifference() {
        final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> paragraphDiffList = new ArrayList<>();
        for (Delta<String> delta : patch.getDeltas()) {
            int origPos = delta.getOriginal().getPosition();
            int origLines = delta.getOriginal().getLines().size();
            int revPos = delta.getRevised().getPosition();
            int revLines = delta.getRevised().getLines().size();
            int origBottomPos = origPos + origLines;
            int revBottomPos = revPos + revLines;

            while (origBottomPos > 0 && revBottomPos > 00
                    && newCodeArea.getParagraphs().size() > revBottomPos
                    && oldCodeArea.getParagraphs().size() > origBottomPos
                    && oldCodeArea.getParagraph(origBottomPos).getText().equals(newCodeArea.getParagraph(revBottomPos).getText())
                    ) {
                origBottomPos--;
                revBottomPos--;
            }

            origBottomPos++;
            revBottomPos++;

            while (newCodeArea.getParagraphs().size() > origPos
                    && oldCodeArea.getParagraphs().size() > revPos
                    && oldCodeArea.getParagraph(origPos).getText().equals(newCodeArea.getParagraph(revPos).getText())
                    ) {
                origPos++;
                revPos++;
            }

            if (origPos > origBottomPos) {
                origBottomPos = origPos;
            }
            if (revPos > revBottomPos) {
                revBottomPos = revPos;
            }
            paragraphDiffList.add(
                    new Pair<>(
                            new Pair<>(origPos, origBottomPos - origPos),
                            new Pair<>(revPos, revBottomPos - revPos)
                    )
            );
            oldLinesToHighlight.add(new Pair<>(origPos, origBottomPos));
            newLinesToHighlight.add(new Pair<>(revPos, revBottomPos));
        }
        return paragraphDiffList;
    }

    private Path getPathForChanges(int oldLine, int oldQty, int newLine, int newQty,
                                   double paintAreaWidth, double paintAreHeight,
                                   double oldParagraphHeight, double newParagraphHeight,
                                   int oldCodeFirstParagraphVisible, int oldCodeFirstVisibleParagraphY,
                                   int newCodeFirstParagraphVisible, int newCodeFirstVisibleParagraphY,
                                   Color color) {


        int x1 = -1;
        int y1 = (int) (oldCodeFirstVisibleParagraphY + oldParagraphHeight * (oldLine - oldCodeFirstParagraphVisible));

        int x2 = (int) paintAreaWidth + 1;
        int y2 = (int) (newCodeFirstVisibleParagraphY + newParagraphHeight * (newLine - newCodeFirstParagraphVisible));

        int x3 = x2;
        int y3 = (int) (newCodeFirstVisibleParagraphY + newParagraphHeight * (newLine + newQty - newCodeFirstParagraphVisible));

        int x4 = x1;
        int y4 = (int) (oldCodeFirstVisibleParagraphY + oldParagraphHeight * (oldLine + oldQty - oldCodeFirstParagraphVisible));

        Path path = new Path();
        MoveTo moveTo = new MoveTo(x1, y1);
        CubicCurveTo curve0 = getCubicCurveTo(x1, y1, x2, y2);
        LineTo lineTo0 = new LineTo(x3, y3);
        CubicCurveTo curve1 = getCubicCurveTo(x3, y3, x4, y4);
        LineTo lineTo1 = new LineTo(x1, y1);

        path.setStroke(color.deriveColor(1, 1, 1, 0.3));
        path.setStrokeWidth(1);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setFill(color.deriveColor(1, 1, 1, 0.15));

        path.getElements().addAll(moveTo, curve0, lineTo0, curve1, lineTo1);
        return path;
    }

    private CubicCurveTo getCubicCurveTo(int x1, int y1, int x2, int y2) {
        int controlPointDeltaX = 10;
        return new CubicCurveTo(
                x2 > x1 ? scrollPane.getWidth() - controlPointDeltaX : controlPointDeltaX, y1,
                x2 > x1 ? controlPointDeltaX : scrollPane.getWidth() - controlPointDeltaX, y2,
                x2, y2);
    }

    private void adjustParagraphHeight(List<Pair<Integer, Pair<Double, Double>>> oldParagraphBeginPoints,
                                       List<Pair<Integer, Pair<Double, Double>>> newParagraphBeginPoints) {
        if (oldParagraphBeginPoints.size() > 1) {
            oldParagraphHeight = (oldParagraphBeginPoints.get(1).getSecond().getSecond().doubleValue() -
                    oldParagraphBeginPoints.get(0).getSecond().getSecond().doubleValue());
        }

        if (newParagraphBeginPoints.size() > 1) {
            newParagraphHeight = (newParagraphBeginPoints.get(1).getSecond().getSecond().doubleValue() -
                    newParagraphBeginPoints.get(0).getSecond().getSecond().doubleValue());
        }
    }


    private List<Pair<Integer, Pair<Double, Double>>> getParagraphBeginPints(final VirtualFlow flow) {

        final List<Pair<Integer, Pair<Double, Double>>> paragraphBeginPoints;
        if (flow.visibleCells().isEmpty()) {
            paragraphBeginPoints = Collections.singletonList(
                    new Pair<>(Integer.valueOf(0), new Pair<>(Double.valueOf(0), Double.valueOf(0)))
            );
        } else {
            paragraphBeginPoints = new ArrayList<>(flow.visibleCells().size());
            for (Object o : flow.visibleCells()) {
                Node node = ((Cell) o).getNode();
                Field field = FieldUtils.getField(node.getClass(), "index", true);

                try {
                    Var<Integer> index = (Var<Integer>) field.get(node);
                    Pair<Integer, Pair<Double, Double>> rez = new Pair<>(
                            index.getValue(),
                            new Pair<>(node.getLayoutX(), node.getLayoutY())
                    );
                    paragraphBeginPoints.add(rez);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return paragraphBeginPoints;
    }

    public void openFile(String title, String oldFileName, String oldRevision, String newFileName,
                         String newRevision, Patch<String> pathc) throws IOException {

        this.patch = pathc;

        oldLabel.setText(oldRevision);
        newLabel.setText(newRevision);

        fillCodeArea(oldCodeArea, oldFileName);
        fillCodeArea(newCodeArea, newFileName);

        paragraphDiffList = calculateParagraphDifference();

        highlightParagraphDifference();

        paragraphDiffList
                .stream()
                .findFirst()
                .ifPresent(pairPairPair -> {
                    diffIdx = 0;
                    oldCodeArea.moveTo(pairPairPair.getFirst().getFirst(), 0);
                    oldCodeArea.requestFollowCaret();
                    oldCodeArea.layout();
                    newCodeArea.moveTo(pairPairPair.getSecond().getFirst(), 0);
                    newCodeArea.requestFollowCaret();
                    newCodeArea.layout();
        });


        Scene scene = new Scene(gridPanel, 1024, 768);
        scene.getStylesheets().add(this.getClass().getResource(Const.KEYWORDS_CSS).toExternalForm());
        scene.getStylesheets().add(this.getClass().getResource(Const.DEFAULT_CSS).toExternalForm());

        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream(Const.ICON)));
        stage.show();

        FxTimer.runLater( Duration.ofMillis(DELAY),  () -> paintChanges(0, 0));

        eventSubscription();

    }

    private void highlightParagraphDifference() {
        Collection styleClass = Collections.singleton("diff");
        for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> ppair : paragraphDiffList) {
            for (int i = ppair.getFirst().getFirst(); i< ppair.getFirst().getFirst() + ppair.getFirst().getSecond(); i++) {
                if(i < oldCodeArea.getParagraphs().size()) {
                    ((StyledTextArea) oldCodeArea).setParagraphStyle(i, styleClass);
                }
            }
            for (int i = ppair.getSecond().getFirst(); i< ppair.getSecond().getFirst() + ppair.getSecond().getSecond(); i++) {
                if(i < newCodeArea.getParagraphs().size()) {
                    ((StyledTextArea) newCodeArea).setParagraphStyle(i, styleClass);
                }
            }

        }
    }


    public void openFile(String title, String oldFileName, String oldRevision, String newFileName,
                         String newRevision, String diffFileName) throws IOException {

        openFile(title, oldFileName, oldRevision, newFileName, newRevision,
                DiffUtils.parseUnifiedDiff(Files.readAllLines(Paths.get(diffFileName))));

    }
}
