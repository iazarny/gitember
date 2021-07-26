package com.az.gitember.controller;

import com.az.gitember.controller.lang.BaseTokenTypeAdapter;
import com.az.gitember.controller.lang.LangResolver;
import com.az.gitember.data.Pair;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Adapt to Text for TextFlow.
 */
public class TextToSpanContentAdapter {

    public static final double FONT_SIZE = 20;
    public static final double FONT_SYMBOL_WIDTH = 11.99;
    public static final double ROW_HEIGHT = FONT_SIZE + 4;

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;
    private final int maxLineLength;
    private final int lineNumWidth;
    private final ArrayList<String> lines;
    private final LangResolver langResolver;
    private final List<Token> parsedCode;
    private final String content;

    /**
     * @param extension The file extension
     * @param patch     patch
     * @param leftSide  what side ro read left - old, right - new
     */
    public TextToSpanContentAdapter(final String content, final String extension, final EditList patch, boolean leftSide) {
        this(content, extension, false);
        this.patch = patch;
        this.leftSide = leftSide;
    }

    TextToSpanContentAdapter(final String contentRaw, final String extension, final boolean rawDiff) {
        content = GitemberUtil.replaceTabs(contentRaw);
        this.langResolver = new LangResolver(extension, content);
        this.rawDiff = rawDiff;
        this.lines = getLines(content);
        this.lineNumWidth = String.valueOf(lines.size()).length();
        this.maxLineLength = Math.max(lines.stream().mapToInt(String::length).max().getAsInt(), LookAndFeelSet.DEFAULT_LINE_LENGTH);

        final CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.getLexer());
        commonTokenStream.fill();
        parsedCode = commonTokenStream.getTokens();

    }

    public StyleSpans<Collection<String>> computeHighlighting() {

        final StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        int lastKwEnd = 0;

        final BaseTokenTypeAdapter adapter = langResolver.getAdapter();


        final Iterator<Token> tokenIterator = parsedCode.iterator();
        Token token = tokenIterator.next();
        for (int i = 0; i < content.length(); i++) {
            int startIdx = token.getStartIndex();
            int stopIdx = token.getStopIndex() + 1;

            if (i == token.getStartIndex()) {
                int len = startIdx - lastKwEnd;
                spansBuilder.add(Collections.emptyList(), len);

                final String style = adapter.adaptToStyleClass(token.getType());
                spansBuilder.add(Collections.singletonList(style), stopIdx - startIdx);
                lastKwEnd = stopIdx;
                token = tokenIterator.next();

                //System.out.println(">>> " + startIdx + " " + stopIdx + "   " + (stopIdx - startIdx) + "   [" + token.getText() + "] real [" + content.substring(startIdx, stopIdx)  + "] " + style);
            }
        }

        StyleSpans<Collection<String>> rez = spansBuilder.create();
        return rez;

    }


    public List<Pair<Integer, Collection>> decorateByRawDiff() {
        if (rawDiff) {

            List<Pair<Integer, Collection>> rez = new ArrayList<>();

            for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
                final String line = lines.get(lineIdx);
                final String style;
                if (line.startsWith("+")) {
                    style = "diff-new";
                } else if (line.startsWith("-")) {
                    style = "diff-deleted";
                } else if (line.startsWith("@@")) {
                    style = "diff-modified";
                } else {
                    style = null;
                }
                if (style != null) {
                    rez.add(new Pair<>(lineIdx, Collections.singletonList(style)));
                }
            }
            return  rez;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Highlight new , deleted and changed lines by patch.
     */
    private void decorateByPatch(final ArrayList<Node> rows) {
        if (patch != null) {
            for (Edit delta : patch) {
                int origPos = delta.getBeginB();
                int origLines = delta.getLengthB();
                String styleClass = GitemberUtil.getDiffSyleClass(delta, "diff-line");
                if (leftSide) {
                    origPos = delta.getBeginA();
                    origLines = delta.getLengthA();
                }

                for (int line = origPos; line < (origLines + origPos); line++) {
                    HBox row = (HBox) rows.get(line);
                    row.getStyleClass().add(styleClass);
                }
            }
        }
    }


    private ArrayList<String> getLines(final String content) {
        return (ArrayList) new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }


}
