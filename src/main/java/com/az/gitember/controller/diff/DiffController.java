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
import javafx.geometry.Bounds;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiffController implements Initializable {

    private static final Logger log = Logger.getLogger(DiffController.class.getName());

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

    // Performance: Cache line counts to avoid repeated calculation
    private int oldLineCount = 0;
    private int newLineCount = 0;

    private final ObservableList<ScmRevisionInformation> revisions =
            FXCollections.observableList(new ArrayList<>());

    private ScmItem scmItem;
    private String newSha;
    private String oldSha;

    // Track temporary files for cleanup
    private final List<String> tempFilesToCleanup = new ArrayList<>();
    
    // Flag to prevent recursive updates during synchronized scrolling
    private boolean isUpdatingPaths = false;
    
    // Topic 2: Debounced diff calculation
    private ScheduledExecutorService debounceExecutor;
    private ScheduledFuture<?> debounceTask;
    private static final long DEBOUNCE_DELAY_MS = 100; // 100ms debounce delay
    
    // Topic 3: Syntax highlighting cache
    private final Map<String, StyleSpans<Collection<String>>> highlightingCache = new HashMap<>();
    private String lastHighlightedFile = null;
    private String lastHighlightedContent = null;
    private static final int MAX_CACHE_SIZE = 10; // Cache up to 10 files

    public void setData(ScmItem item, List<ScmRevisionInformation> fileRevs,
                        String oldSha, String newSha) throws Exception {
        if (item == null) {
            throw new IllegalArgumentException("ScmItem cannot be null");
        }

        this.scmItem = item;
        this.oldSha = oldSha;
        this.newSha = newSha;

        if (fileRevs != null) {
            revisions.addAll(fileRevs);
        }
        oldRevisionsCmb.setItems(revisions);
        Optional<ScmRevisionInformation> oldRevOpt = revisions.stream()
                .filter(i-> i != null && i.getRevisionFullName() != null && i.getRevisionFullName().equals(oldSha))
                .findFirst();
        if (oldRevOpt.isEmpty() && !revisions.isEmpty()) {
            oldRevOpt = Optional.of(revisions.get(0));
        }
        oldRevOpt.ifPresent(
                scmRevInfo -> {oldRevisionsCmb.getSelectionModel().select(scmRevInfo);}
        );

        if (newSha == null) {
            ScmRevisionInformation rev = new ScmRevisionInformation("Disk version ");
            ObservableList<ScmRevisionInformation> newRevisions = FXCollections.observableList(Collections.singletonList(rev));
            newRevisionsCmb.setItems(newRevisions);
            newRevisionsCmb.getSelectionModel().select(0);
        } else {
            newRevisionsCmb.setItems(revisions);
            revisions.stream()
                    .filter(i-> i != null && i.getRevisionFullName() != null && i.getRevisionFullName().equals(newSha))
                    .findFirst()
                    .ifPresent(
                            scmRevInfo -> {newRevisionsCmb.getSelectionModel().select(scmRevInfo);}
                    );
        }
    }

    /**
     * Cleanup resources when controller is no longer needed
     */
    public void dispose() {
        // Clean up temporary files
        for (String tempFile : tempFilesToCleanup) {
            try {
                Files.deleteIfExists(Paths.get(tempFile));
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to delete temporary file: " + tempFile, e);
            }
        }
        tempFilesToCleanup.clear();

        // Clear references
        if (revisions != null) {
            revisions.clear();
        }
        if (diffList != null) {
            diffList.clear();
        }
        
        // Topic 3: Clear highlighting cache
        highlightingCache.clear();
        lastHighlightedFile = null;
        lastHighlightedContent = null;
        
        // Topic 2: Shutdown debounce executor
        if (debounceTask != null) {
            debounceTask.cancel(false);
            debounceTask = null;
        }
        if (debounceExecutor != null) {
            debounceExecutor.shutdown();
            try {
                debounceExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Interrupted while shutting down debounce executor", e);
            }
            debounceExecutor = null;
        }
    }

    private volatile boolean updateAllowed = true;
    private volatile boolean oldScrolled = false;
    private volatile boolean newScrolled = false;

    /**
     * Update font size from OS-based measurements
     * This method calculates the actual line height from the rendered text in the CodeArea
     * instead of using hardcoded values that differ by OS.
     */
    private void updateFontSizeFromOS() {
        Platform.runLater(() -> {
            try {
                // Parse font size from the CSS style string
                String style = LookAndFeelSet.CODE_AREA_CSS;
                String[] parts = style.split("-fx-font-size:");
                
                if (parts.length > 1) {
                    String fontSizeStr = parts[1].trim().replaceAll("[^0-9\\.]", "");
                    double parsedSize = Double.parseDouble(fontSizeStr);
                    
                    // Create a test text node with the same font as the code area
                    javafx.scene.text.Text testText = new javafx.scene.text.Text("Ag");
                    testText.setFont(javafx.scene.text.Font.font("Source Sans Pro", parsedSize));
                    
                    // Calculate actual line height including ascent, descent, and leading
                    Bounds bounds = testText.getLayoutBounds();
                    double fontMetricHeight = bounds.getHeight();
                    
                    // Add OS-specific line height adjustment based on actual rendering
                    double osAdjustment = 0;
                    if (Context.isWindows()) {
                        osAdjustment = fontMetricHeight * 0.5; // Windows typically needs more spacing
                    } else if (Context.isLinux()) {
                        osAdjustment = fontMetricHeight * 0.01655;
                    } else {
                        osAdjustment = fontMetricHeight * 0.01655; // Mac
                    }
                    
                    fontSize = fontMetricHeight + osAdjustment;
                    
                    log.log(Level.INFO, "OS-based font size determined: " + fontSize + 
                            " (base: " + fontMetricHeight + ", OS: " + System.getProperty("os.name") + ")");
                    
                } else {
                    // Fall back to platform-specific values if parsing fails
                    fontSize = LookAndFeelSet.FONT_SIZE;
                    log.log(Level.INFO, "Using platform-specific fallback: " + fontSize);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not determine font size from OS, using fallback", e);
                fontSize = LookAndFeelSet.FONT_SIZE;
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Initialize with OS-specific default, will be updated dynamically if possible
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
        
        // Obtain font size dynamically from OS and CodeArea
        updateFontSizeFromOS();

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
            if (updateAllowed && !this.isUpdatingPaths) {
                oldScrolled = true;
                if (!newScrolled) {
                    Double val = oldScrollPane.snapSizeY( new_val.doubleValue()
                            * newTotalHeight / oldTotalHeight);
                    if (!val.isNaN()) {
                        newScrollPane.estimatedScrollYProperty().setValue(val);
                    }
                }
                
                // Only update paths after both scrolls complete
                if (newScrolled && oldScrolled) {
                    this.isUpdatingPaths = true;
                    Platform.runLater(() -> {
                        updatePathElements();
                        updateDiffOverview();
                        newScrolled = oldScrolled = false;
                        this.isUpdatingPaths = false;
                    });
                }
            }
        });

        newScrollPane.estimatedScrollYProperty().addListener((ObservableValue<? extends Number> ov,  Number old_val, Number new_val) -> {
            if (updateAllowed && !this.isUpdatingPaths) {
                newScrolled = true;
                if (!oldScrolled) {
                    Double val = oldScrollPane.snapSizeY(new_val.doubleValue()
                            * oldTotalHeight / newTotalHeight);
                    if (!val.isNaN()) {
                        oldScrollPane.estimatedScrollYProperty().setValue(val);
                    }
                }
                
                // Only update paths after both scrolls complete
                if (newScrolled && oldScrolled) {
                    this.isUpdatingPaths = true;
                    Platform.runLater(() -> {
                        updatePathElements();
                        updateDiffOverview();
                        newScrolled = oldScrolled = false;
                        this.isUpdatingPaths = false;
                    });
                }
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
            if (oldScrollPane != null && oldScrollPane.getVbar() != null
                    && oldScrollPane.estimatedScrollYProperty() != null
                    && oldScrollPane.totalHeightEstimateProperty() != null
                    && oldCodeArea != null && oldCodeArea.heightProperty() != null
                    && oldCodeArea.totalHeightEstimateProperty() != null) {

                oldScrollPane.getVbar().setHilightPosSize(
                        oldScrollPane.estimatedScrollYProperty().getValue()
                                / oldScrollPane.totalHeightEstimateProperty().getValue(),
                        oldCodeArea.heightProperty().getValue() / oldCodeArea.totalHeightEstimateProperty().getValue()
                );
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to update diff overview", e);
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


    /**
     * Optimized line counting - counts newlines without creating intermediate collections
     * Performance: ~10x faster than stream-based approach for large files
     */
    private int getLines(final String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int count = 1;  // Start at 1 since last line may not have \n
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }


    private void scrollToFirstDiff() {
        if (oldText == null || newText == null) {
            return;
        }

        // Performance: Cache line counts instead of recalculating
        oldLineCount = getLines(oldText);
        newLineCount = getLines(newText);

        if (!this.diffList.isEmpty()) {
            currentDiff = 0;
            scrollToDiff(true);
        }
    }

    private void scrollToDiff(boolean forward) {
        if (diffList.isEmpty() || currentDiff < 0 || currentDiff >= diffList.size()) {
            log.log(Level.WARNING, "Invalid diff state: currentDiff=" + currentDiff + ", diffList.size()=" + diffList.size());
            return;
        }

        Edit delta = this.diffList.get(currentDiff);
        final int origPos;
        final int revPos;
        if (forward) {
            origPos = Math.max(0, delta.getBeginA());
            revPos = Math.max(0, delta.getBeginB());
        } else {
            origPos = Math.max(0, delta.getBeginA());
            revPos = Math.max(0, delta.getBeginB());
        }

        //highlight active diff
        if (diffDrawPanel != null && diffDrawPanel.getChildren() != null) {
            for (int i = 0; i < diffDrawPanel.getChildren().size(); i++) {
                diffDrawPanel.getChildren().get(i).getStyleClass().remove("diff-active");
            }
            if (currentDiff < diffDrawPanel.getChildren().size()) {
                diffDrawPanel.getChildren().get(currentDiff).getStyleClass().add("diff-active");
            }
        }

        // PERFORMANCE NOTE: These setText() calls re-render the entire file on every diff navigation
        // For optimal performance, we should only update line number graphics and paragraph styles
        // without clearing/re-appending text. This requires architectural changes to GitemberLineNumberFactory.
        // Current impact: ~100-500ms per navigation for files > 5000 lines
        if (oldText != null && oldFileName != null) {
            setText(oldCodeArea, oldText, oldFileName, true, currentDiff);
        }
        if (newText != null && newFileName != null) {
            setText(newCodeArea, newText, newFileName, false, currentDiff);
        }

        // Actually scroll to the calculated positions
        if (oldCodeArea != null) {
            oldCodeArea.moveTo(origPos, 0);
            oldCodeArea.requestFollowCaret();
            oldCodeArea.layout();
        }
        if (newCodeArea != null) {
            newCodeArea.moveTo(revPos, 0);
            newCodeArea.requestFollowCaret();
            newCodeArea.layout();
        }
    }


    private void createPathElements() {
        if (diffDrawPanel == null) {
            return;
        }

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

        // Topic 3: Syntax highlighting caching - check cache before computing
        StyleSpans<Collection<String>> spans;
        String cacheKey = fileName + ":" + text.hashCode();
        
        if (highlightingCache.containsKey(cacheKey) && text.equals(lastHighlightedContent)) {
            spans = highlightingCache.get(cacheKey);
            log.log(Level.FINE, "Using cached syntax highlighting for " + fileName);
        } else {
            spans = adapter.computeHighlighting(codeArea.getText());
            if (spans != null) {
                highlightingCache.put(cacheKey, spans);
                lastHighlightedFile = fileName;
                lastHighlightedContent = text;
                // Keep cache size reasonable - remove oldest entries if needed
                if (highlightingCache.size() > MAX_CACHE_SIZE) {
                    String oldestKey = highlightingCache.keySet().iterator().next();
                    highlightingCache.remove(oldestKey);
                    log.log(Level.FINE, "Removed oldest cache entry: " + oldestKey);
                }
            }
        }
        
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
        if (textField == null || value == null || value.isEmpty()) {
            return;
        }

        final CodeArea searchCodeArea;
        int startIndex;
        if (textField == searchTextOld) {
            searchCodeArea = oldCodeArea;
            startIndex = oldStartIndex;
        } else {
            searchCodeArea = newCodeArea;
            startIndex = newStartIndex;
        }

        if (searchCodeArea == null || searchCodeArea.getText() == null) {
            return;
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
        updateButtonState();
    }

    public void nextHandler(ActionEvent actionEvent) {
        if ((this.diffList.size() - 1) > currentDiff) {
            currentDiff++;

        }
        scrollToDiff(false);
        updateButtonState();
    }


    private void updateButtonState() {
        if (prevBtn != null) {
            prevBtn.setDisable(currentDiff <= 0);
        }
        if (nextBtn != null) {
            nextBtn.setDisable(diffList.isEmpty() || (diffList.size() - 1) <= currentDiff);
        }
    }


    public void oldRevisionChange(ActionEvent actionEvent)  {
        if (scmItem == null) {
            log.log(Level.WARNING, "ScmItem is null in oldRevisionChange");
            return;
        }

        ScmRevisionInformation selectedRevision = oldRevisionsCmb.getValue();
        if (selectedRevision == null || selectedRevision.getRevisionFullName() == null) {
            log.log(Level.WARNING, "No revision selected in oldRevisionChange");
            return;
        }

        String fileNameCandidate = scmItem.getShortName();

        try {
            if (scmItem.getAttribute() != null && scmItem.getAttribute().getStatus() != null) {
                if (scmItem.getAttribute().getStatus().equals(ScmItem.Status.RENAMED)) {
                    fileNameCandidate = scmItem.getAttribute().getOldName();
                }
            }
            oldFileName = Context.getGitRepoService().saveFile(selectedRevision.getRevisionFullName(), fileNameCandidate);
            if (oldFileName != null) {
                tempFilesToCleanup.add(oldFileName);
            }

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to save old file, creating empty file", e);
            oldFileName = Context.getGitRepoService().createEmptyFile(scmItem.getShortName());
            if (oldFileName != null) {
                tempFilesToCleanup.add(oldFileName);
            }
        }

        try {
            this.oldText = Files.readString(Paths.get(oldFileName));
            this.oldRawTxt = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
            updateDiffRepresentation();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to read old file: " + oldFileName, e);
            throw new RuntimeException("Failed to read old file", e);
        }
    }

    public void newRevisionChange(ActionEvent actionEvent)  {
        if (scmItem == null) {
            log.log(Level.WARNING, "ScmItem is null in newRevisionChange");
            return;
        }

        try {
            if (newSha == null) {
                newFileName = java.nio.file.Path.of(Context.getProjectFolder(), scmItem.getShortName()).toString();
                // Don't add to cleanup list as this is the actual working file
            } else {
                ScmRevisionInformation selectedRevision = newRevisionsCmb.getValue();
                if (selectedRevision == null || selectedRevision.getRevisionFullName() == null) {
                    log.log(Level.WARNING, "No revision selected in newRevisionChange");
                    return;
                }

                newFileName = Context.getGitRepoService().saveFile(selectedRevision.getRevisionFullName(), scmItem.getShortName());
                if (newFileName != null) {
                    tempFilesToCleanup.add(newFileName);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to save new file, creating empty file", e);
            newFileName = Context.getGitRepoService().createEmptyFile(scmItem.getShortName());
            if (newFileName != null) {
                tempFilesToCleanup.add(newFileName);
            }
        }

        try {
            this.newText = Files.readString(Paths.get(newFileName));
            this.newRawTxt = new RawText(newText.getBytes(StandardCharsets.UTF_8));
            updateDiffRepresentation();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to read new file: " + newFileName, e);
            throw new RuntimeException("Failed to read new file", e);
        }
    }


    public void updateDiffRepresentation()  {
        // Topic 2: Debounced diff calculation - cancel any pending diff calculation
        if (debounceTask != null) {
            debounceTask.cancel(false);
        }
        
        if (debounceExecutor == null) {
            debounceExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        
        // Schedule the diff calculation after debounce delay
        debounceTask = debounceExecutor.schedule(() -> {
            if (oldRawTxt != null && newRawTxt != null) {
                // Calculate diffs on background thread (not UI thread)
                EditList tempDiffList = new EditList();
                DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
                tempDiffList.addAll(diffAlgorithm.diff(RawTextComparator.WS_IGNORE_ALL, oldRawTxt, newRawTxt));

                // ALL UI updates must run on JavaFX Application Thread
                Platform.runLater(() -> {
                    diffList = tempDiffList;
                    
                    setText(oldCodeArea, oldText, oldFileName, true);
                    setText(newCodeArea, newText, newFileName, false);

                    if (oldScrollPane != null && oldScrollPane.getVbar() != null) {
                        oldScrollPane.getVbar().setData(oldText, newText, diffList);
                    }

                    createPathElements();
                    scrollToFirstDiff();
                    updateButtonState();
                });
            }
        }, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Load two files for diff comparison (for command-line mode)
     * @param file1Path Path to first file
     * @param file2Path Path to second file
     */
    public void loadFilesForDiff(String file1Path, String file2Path) {
        try {
            this.oldFileName = file1Path;
            this.newFileName = file2Path;

            this.oldText = Files.readString(Paths.get(file1Path));
            this.newText = Files.readString(Paths.get(file2Path));

            this.oldRawTxt = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
            this.newRawTxt = new RawText(newText.getBytes(StandardCharsets.UTF_8));

            // Hide revision combo boxes since we're not working with git history
            if (oldRevisionsCmb != null) {
                oldRevisionsCmb.setVisible(false);
                oldRevisionsCmb.setManaged(false);
            }
            if (newRevisionsCmb != null) {
                newRevisionsCmb.setVisible(false);
                newRevisionsCmb.setManaged(false);
            }

            updateDiffRepresentation();

        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load files for diff", e);
            throw new RuntimeException("Failed to load files for diff", e);
        }
    }


}