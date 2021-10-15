package com.az.gitember.controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.jgit.blame.BlameResult;
import org.fxmisc.richtext.StyledTextArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.List;
import java.util.function.IntFunction;

/**
 * TODO https://stackoverflow.com/questions/52130518/richtextfx-change-selected-text-color-and-line-number-background
 * line number column for empty lines
 */

public class GitemberLineNumberFactory implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS = new Insets(0.0, 10.0, 0.0, 10.0);

    private final TextToSpanContentAdapter textAdapter;
    private final BlameResult blame;

    public static IntFunction<Node> get(StyledTextArea<?, ?> area, TextToSpanContentAdapter textAdapter, BlameResult blame) {
        if (blame != null) {
            final int maxLen = getMaxLen(blame);
            return get(area, digits -> "%0" + digits + "d [%-" + maxLen +"s]", textAdapter, blame);

        } else {
            return get(area, digits -> "%0" + digits + "d %s", textAdapter, blame);
        }

    }

    private static int getMaxLen(BlameResult blame) {
        int maxLen = 0;
        for (int i = 0; i < blame.getResultContents().size(); i++) {
            int len = blame.getSourceAuthor(i).getName().length();
            if (len > maxLen) {
                maxLen = len;
            }

        }
        return maxLen;
    }

    public static IntFunction<Node> get(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter,
            BlameResult blame) {
        return new GitemberLineNumberFactory(area, format, textAdapter, blame);
    }

    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;
    private final int maxDigits;
    private final StyledTextArea<?, ?> area;

    private GitemberLineNumberFactory(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter,
            BlameResult blame) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.maxDigits = String.valueOf(nParagraphs.getOrElse(1)).length();
        this.textAdapter = textAdapter;
        this.area = area;
        this.blame = blame;
    }

    public BlameResult getBlame() {
        return blame;
    }

    @Override
    public Node apply(int idx) {
        Val<String> formatted = nParagraphs.map(n -> format(idx));


        Label lineNo = new Label();
        lineNo.setStyle(LookAndFeelSet.CODE_AREA_LINE_NUM_CSS);
        lineNo.setPadding(DEFAULT_INSETS);

        applyStyle(lineNo, textAdapter.getDiffDecoration(area.getText()).get(idx));
        applyStyle(lineNo, textAdapter.getDecorateByPatch().get(idx));


        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

        return lineNo;
    }



    private String anotate(int lineIdx) {
        String author = "";
        if (blame != null && blame.getResultContents() != null) {
            try {
                author = blame.getSourceCommitter(lineIdx).getName();
            } catch (Exception e) {}

        }
        return author;
    }



    private void applyStyle(Label lineNo, List<String> style) {
        if (style != null) {
            lineNo.setStyle("");
            lineNo.getStyleClass().add(style.get(0));
        }
    }

    private String format(int x) {
        return String.format(format.apply(maxDigits), x+1, anotate(x)) ;
    }
}
