package com.az.gitember.ui;

import com.az.gitember.misc.Pair;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.richtext.StyledTextArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.List;
import java.util.function.IntFunction;

/**
 * Sorry ^C^V, because  original LineNumberFactory violates sOlid.
 */
public class DiffLineNumberFactory implements IntFunction<Node> {

    private List<Pair<Integer, Integer>> highlightList;
    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT =
            Font.font("monospace", FontPosture.ITALIC, 13);

    private static final Background DEFAULT_BACKGROUND =
            new Background(new BackgroundFill(Color.web("#ddd"), null, null));

    private static final Background DEFAULT_BACKGROUND_NEW =
            new Background(new BackgroundFill(Color.web("#6ace9f").deriveColor(1, 1, 1, 0.2), null, null));

    public static IntFunction<Node> get(
            StyledTextArea<?, ?> area,
            List<Pair<Integer, Integer>> highlightList) {
        return get(area, digits -> "%0" + digits + "d", highlightList);
    }

    public static IntFunction<Node> get(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            List<Pair<Integer, Integer>> highlightList) {
        return new DiffLineNumberFactory(area, format, highlightList);
    }

    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;

    private DiffLineNumberFactory(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            List<Pair<Integer, Integer>> highlightList) {
        nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.highlightList = highlightList;
    }

    @Override
    public Node apply(int idx) {
        Val<String> formatted = nParagraphs.map(n -> format(idx + 1, n));
        Label lineNo = new Label();
        lineNo.setFont(DEFAULT_FONT);
        lineNo.setBackground(DEFAULT_BACKGROUND);
        highlightList
                .stream()
                .filter(range -> idx >= range.getFirst() && idx < range.getSecond())
                .forEach(range -> {
                    lineNo.setBackground(DEFAULT_BACKGROUND_NEW);
                });
        lineNo.setTextFill(DEFAULT_TEXT_FILL);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.getStyleClass().add("lineno");

        // bind label's text to a Val that stops observing area's paragraphs
        // when lineNo is removed from scene
        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

        return lineNo;
    }


    private String format(int x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format(format.apply(digits), x);
    }
}