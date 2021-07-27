package com.az.gitember.controller;

import com.az.gitember.data.Pair;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.StyledTextArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

/**
 * TODO https://stackoverflow.com/questions/52130518/richtextfx-change-selected-text-color-and-line-number-background
 * line number column for empty lines
 */

public class GitemberLineNumberFactory implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS = new Insets(0.0, 10.0, 0.0, 10.0);

    private final TextToSpanContentAdapter textAdapter;

    public static IntFunction<Node> get(StyledTextArea<?, ?> area, TextToSpanContentAdapter textAdapter) {
        return get(area, digits -> "%0" + digits + "d", textAdapter);
    }

    public static IntFunction<Node> get(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter) {
        return new GitemberLineNumberFactory(area, format, textAdapter);
    }

    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;
    private final int maxDigits;

    private GitemberLineNumberFactory(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.maxDigits = String.valueOf(nParagraphs.getOrElse(1)).length();
        this.textAdapter = textAdapter;
    }

    @Override
    public Node apply(int idx) {
        Val<String> formatted = nParagraphs.map(n -> format(idx + 1));


        Label lineNo = new Label();
        lineNo.setStyle("-fx-background-color: background_color;");
        lineNo.setPadding(DEFAULT_INSETS);

        List<String> style = textAdapter.getDiffDecoration().get(idx);
        if (style != null) {
            lineNo.setStyle("");
            lineNo.getStyleClass().add(style.get(0));
        }

        /*highlightList
                .stream()
                .filter(range -> idx >= range.getFirst() && idx < range.getSecond())
                .forEach(range -> {
                    lineNo.setBackground(DEFAULT_BACKGROUND_NEW);
                });*/

        // bind label's text to a Val that stops observing area's paragraphs
        // when lineNo is removed from scene

        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

        return lineNo;
    }

    private String format(int x) {
        return String.format(format.apply(maxDigits), x);
    }
}
