package   com.az.gitember.controller; import com.az.gitember.data.SquarePos;


import com.az.gitember.service.GitemberUtil;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.TextFlow;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DiffViewer implements Initializable {

    private final  static double VER_HEAD_HEIGHT = 25;

    public TextField newLabel;
    public TextField oldLabel;
    public CodeArea oldTextFlow;
    public CodeArea newTextFlow;
    public Pane diffDrawPanel;
    public GridPane mainPanel;
    public VirtualizedScrollPane<CodeArea> oldScrollPane;
    public VirtualizedScrollPane<CodeArea> newScrollPane;
    public RowConstraints firstRowConstraint;
    public RowConstraints secondRowConstraint;

    private String oldText = null;
    private String newText = null;
    private EditList diffList = new EditList();

    private double fontSize;

    public void setData(String  oldFileName, String newFileName) throws IOException {

        this.oldText = Files.readString(Paths.get(oldFileName));
        this.newText = Files.readString(Paths.get(newFileName));

        RawText oldRawTet = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        RawText newRawTet = new RawText(newText.getBytes(StandardCharsets.UTF_8));

        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        RawTextComparator comparator = RawTextComparator.WS_IGNORE_ALL;

        diffList.addAll(diffAlgorithm.diff(comparator, oldRawTet, newRawTet));



        Runnable oldRunnable = () -> {setText(oldTextFlow,  oldText, oldFileName, true);};
        Runnable newRunnable = () -> {setText(newTextFlow,  newText, newFileName, false);};
        Platform.runLater(oldRunnable);
        Platform.runLater(newRunnable);

        createPathElements();
        init();
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

    //setTabSize method for text and texflow
    //not available, so just replace int the original text tab to 4 spaces
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        oldTextFlow = new CodeArea();
        oldTextFlow.setStyle("-fx-stroke: gray; -fx-background-color: blue; -fx-font: Monospace; -fx-font-size: 20;");

        newTextFlow = new CodeArea();
        newTextFlow.setStyle("-fx-color-label-visible: cyan; -fx-background-color: green; -fx-font: Monospace; -fx-font-size: 20;");

        oldScrollPane = new VirtualizedScrollPane(oldTextFlow);
        newScrollPane = new VirtualizedScrollPane(newTextFlow);

        mainPanel.add(oldScrollPane, 0, 1);
        mainPanel.add(newScrollPane, 2, 1);

        HBox.setHgrow(oldScrollPane, Priority.ALWAYS);
        HBox.setHgrow(newScrollPane, Priority.ALWAYS);




    }

    private void init() {
        fontSize = TextBrowserContentAdapter.FONT_SIZE + 4.0; // windows & lin

        oldScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,
                                                    Number old_val, Number new_val) -> {
            newScrollPane.estimatedScrollYProperty().setValue(
                    new_val.doubleValue() * newScrollPane.totalHeightEstimateProperty().getValue() / oldScrollPane.totalHeightEstimateProperty().getValue()
            );
            updatePathElements();
        });

        newScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,
                                                    Number old_val, Number new_val) -> {
            oldScrollPane.estimatedScrollYProperty().setValue(
                    new_val.doubleValue() * oldScrollPane.totalHeightEstimateProperty().getValue() / newScrollPane.totalHeightEstimateProperty().getValue()
            );
            updatePathElements();
        });

        newScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater( () -> updatePathElements() );
        });

        diffDrawPanel.widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater( () -> updatePathElements() );
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
    }

    private SquarePos getDiffPos(Edit delta) {

        final int origPos = delta.getBeginA();
        final int origLines = delta.getLengthA();
        final int revPos = delta.getBeginB();
        final int revLines = delta.getLengthB();

        int origBottomPos = origPos + origLines;
        int revBottomPos = revPos + revLines;

        System.out.println("??? " + oldTextFlow.getBoundsInParent().getMaxY()
                + " " + oldScrollPane.estimatedScrollYProperty().getValue()
                + " " + oldScrollPane.getHeight());

        double deltaY1 = oldScrollPane.estimatedScrollYProperty().getValue(); /* (oldTextFlow.getBoundsInParent().getMaxY()  - oldScrollPane.estimatedScrollYProperty().getValue())
                * oldScrollPane.estimatedScrollYProperty().getValue() / oldScrollPane.maxHeightProperty().getValue();*/

        double deltaY2 = newScrollPane.estimatedScrollYProperty().getValue();/*(newTextFlow.getBoundsInParent().getMaxY()  - newScrollPane.getViewportBounds().getHeight())
                * newScrollPane.getVvalue();*/
        int border_shift = 2; //uber node and 1 node border

        int x1 = -1;
        int y1 = (int) (origPos * fontSize - deltaY1) + border_shift;

        int x2 = (int) diffDrawPanel.getWidth() + 1;
        int y2 = (int) (revPos * fontSize - deltaY2) + border_shift;

        int x3 = x2;
        int y3 = (int) (revBottomPos * fontSize - deltaY2) + border_shift;

        int x4 = x1;
        int y4 = (int) (origBottomPos * fontSize - deltaY1) + border_shift;

        return new SquarePos(x1,y1,x2,y2,x3,y3,x4,y4);

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

            CubicCurveTo ccTo =  getCubicCurveTo(squarePos.getX1(), squarePos.getY1(), squarePos.getX2(), squarePos.getY2());
            curve0.setX(squarePos.getX2());
            curve0.setY(squarePos.getY2());
            curve0.setControlX1(ccTo.getControlX1());
            curve0.setControlX2(ccTo.getControlX2());
            curve0.setControlY1(ccTo.getControlY1());
            curve0.setControlY2(ccTo.getControlY2());


            lineTo0.setX(squarePos.getX3());
            lineTo0.setY(squarePos.getY3());

            ccTo =  getCubicCurveTo(squarePos.getX3(), squarePos.getY3(), squarePos.getX4(), squarePos.getY4());
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
        return  new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList()).size();
    }


    private void scrollToFirstDiff() {
        int totalOldLines = getLines(oldText);
        int totalNewLines = getLines(newText);
        if (totalNewLines > 40 && totalOldLines > 40) {
            //TODO collect and add to < > button navigation
            //TODO h scroll pos as well
            for (Edit delta : this.diffList) {
                final int origPos = delta.getBeginA();
                final int revPos = delta.getBeginB();
                /*oldScrollPane.setVvalue( (float)origPos / (float) totalOldLines);
                newScrollPane.setVvalue( (float)revPos / (float) totalNewLines);*/
                break;
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

    private void setText(final CodeArea textFlow,
                         final String text, final String fileName, final boolean leftSide) {

        textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        textFlow.setMinWidth(Region.USE_PREF_SIZE);

        textFlow.appendText(text);

        //long dt = System.currentTimeMillis();

        /*TextBrowserContentAdapter adapter = new TextBrowserContentAdapter(
                text,
                FilenameUtils.getExtension(fileName),
                this.diffList,
                leftSide  );

        System.out.println(" " + leftSide + " adapt    ms " + (System.currentTimeMillis() - dt));


        List<Node> nodes =  adapter.getText();
        textFlow.getChildren().addAll( nodes );
        System.out.println(" " + leftSide + " set text ms " + (System.currentTimeMillis() - dt));
        textFlow.layout();
        System.out.println(" " + leftSide + " layout ms " + (System.currentTimeMillis() - dt));*/

    }


}
