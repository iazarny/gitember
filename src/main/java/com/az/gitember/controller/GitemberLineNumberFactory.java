package com.az.gitember.controller;

import com.az.gitember.service.GitemberUtil;
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
    private final int maxLen;

    public static IntFunction<Node> get(StyledTextArea<?, ?> area, TextToSpanContentAdapter textAdapter, BlameResult blame, int activeParagraph) {
        return get(area, digits -> "%0" + digits + "d %s", textAdapter, blame, activeParagraph);
    }

    private static int getMaxLen(BlameResult blame) {
        int maxLen = 0;
        for (int i = 0; i < blame.getResultContents().size(); i++) {
            if (blame.getSourceCommit(i) != null) {
                String line = GitemberUtil.formatDateOnly(GitemberUtil.intToDate(blame.getSourceCommit(i).getCommitTime()))
                        + " " + blame.getSourceAuthor(i).getName();
                int len = line.length();
                if (len > maxLen) {
                    maxLen = len;
                }
            }
        }
        return maxLen;
    }

    public static IntFunction<Node> get(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter,
            BlameResult blame,
            int activeParagraph) {
        return new GitemberLineNumberFactory(area, format, textAdapter, blame, activeParagraph);
    }

    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;
    private final int maxDigits;
    private final int activeParagraph;
    private final StyledTextArea<?, ?> area;

    private GitemberLineNumberFactory(
            StyledTextArea<?, ?> area,
            IntFunction<String> format,
            TextToSpanContentAdapter textAdapter,
            BlameResult blame,
            int activeParagraph) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.activeParagraph = activeParagraph;
        this.maxDigits = String.valueOf(nParagraphs.getOrElse(1)).length();
        this.textAdapter = textAdapter;
        this.area = area;
        this.blame = blame;
        if (blame == null) {
            maxLen = 0;
        } else {
            maxLen = getMaxLen(blame);
        }
    }

    public BlameResult getBlame() {
        return blame;
    }

    @Override
    public Node apply(int idx) {
        Val<String> formatted = nParagraphs.map(n -> format(idx));

        Label lineNo = new Label();
        if (blame != null) {
            int width = maxLen * 20;
            lineNo.setMaxWidth(width);
            lineNo.setMinWidth(width);
            lineNo.setPrefWidth(width);

        }
        lineNo.setStyle(LookAndFeelSet.CODE_AREA_LINE_NUM_CSS);
        lineNo.setPadding(DEFAULT_INSETS);

        applyStyle(lineNo, textAdapter.getDiffDecoration(area.getText()).get(idx));
        applyStyle(lineNo, textAdapter.getDecorateByPatch(activeParagraph).get(idx));

        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
        return lineNo;
    }

    private String anotate(int lineIdx) {
        String author = "";
        if (blame != null && blame.getResultContents() != null) {
            try {
                if (blame.getSourceCommit(lineIdx) != null) {
                    author =  " " + GitemberUtil.formatDateOnlyShort(
                            GitemberUtil.intToDate(blame.getSourceCommit(lineIdx).getCommitTime()))
                            + " " + blame.getSourceAuthor(lineIdx).getName();
                }
            } catch (Exception e) {}
        }
        return author;
    }

    private void applyStyle(Label lineNo, List<String> style) {
        if (style != null) {
            lineNo.setStyle("");
            lineNo.getStyleClass().addAll(style);
        }
    }

    private String format(int x) {
        return String.format(format.apply(maxDigits), x+1, anotate(x)) ;
    }
}
