package com.az.gitember.controller;

import com.az.gitember.controller.lang.BaseTokenTypeAdapter;
import com.az.gitember.controller.lang.LangResolver;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Adapt to Text for TextFlow.
 */
public class TextBrowserContentAdapter {

    public static Font FONT;
    public static String FONT_NAME = "Monospace";
    //public static String FONT_NAME = "Consolas";
    public static final double FONT_SIZE = 20;
    public static final double FONT_SYMBOL_WIDTH = 11.99;
    public static final double ROW_HEIGHT = FONT_SIZE + 4;

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;
    private final int maxLineLength;
    private final int lineNumWidth;
    private final ArrayList<String> lines;
    private final Map<Integer, List<Token>> tokensPerLine = new HashMap<>();
    private final Map<Integer, List<Node>> nodesPerLine = new HashMap<>();
    private final LangResolver langResolver;

    /**
     * @param extension The file extension
     * @param patch     patch
     * @param leftSide  what side ro read left - old, right - new
     */
    public TextBrowserContentAdapter(final String content, final String extension, final EditList patch, boolean leftSide) {
        this(content, extension, false);
        this.patch = patch;
        this.leftSide = leftSide;
    }

    TextBrowserContentAdapter(final String contentRaw, final String extension, final boolean rawDiff) {
        String content = GitemberUtil.replaceTabs(contentRaw);
        this.langResolver = new LangResolver(extension, content);
        this.rawDiff = rawDiff;
        this.lines = getLines(content);
        this.lineNumWidth = String.valueOf(lines.size()).length();
        this.maxLineLength = Math.max(lines.stream().mapToInt(String::length).max().getAsInt(), LookAndFeelSet.DEFAULT_LINE_LENGTH);

        final CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.getLexer());
        commonTokenStream.fill();
        final List<Token> parsedCode = commonTokenStream.getTokens();

        parsedCode.stream().forEach(token -> {
            List<Token> tokens = tokensPerLine.computeIfAbsent(token.getLine(), integer -> new ArrayList<>());
            tokens.add(token);
        });
        FONT = Font.font(FONT_NAME, FONT_SIZE);
    }

    public List<Node> getText() {
        final List<Node> rez = new ArrayList<>(1024);
        for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
            List<Token> tokens = tokensPerLine.get(lineIdx + 1);
            final String originalLine = lines.get(lineIdx);
            rez.add(createLineNumbedNode(lineIdx, ""));
            if (tokens != null) {
                Token prevToken = null;
                Iterator<Token> tokenIterator = tokens.iterator();
                while (tokenIterator.hasNext()) {
                    final Token token = tokenIterator.next();
                    final int tokenType = token.getType();
                    final BaseTokenTypeAdapter adapter = langResolver.getAdapter();
                    if (!adapter.skip(tokenType)) {
                        final String tokenText = token.getText();
                        final String style = adapter.adaptToStyleClass(tokenType);
                        //System.out.println(lineIdx + "   " + tokenType + " [" + tokenText + "]  <--" + style);
                        safeAdd(rez, createSpacesBetweenTokens(lineIdx, originalLine, prevToken, token, rez.get(rez.size() - 1)));
                        List<String> strings = this.getLines(tokenText);
                        if (strings.size() > 1) { //multiline token
                            int processedLines = processMultilineToken(rez, lineIdx, originalLine, style, strings);
                            lineIdx += processedLines;
                            if (tokensPerLine.get(lineIdx + 1) != null) {
                                tokens = tokensPerLine.get(lineIdx + 1);
                                tokenIterator = tokens.iterator();
                            }
                        } else { //single line
                            safeAdd(rez, createText(originalLine, tokenText, style, lineIdx, "", rez.get(rez.size() - 1)));
                        }
                        prevToken = token;
                    }
                }
            }
        }
        //Set line num
        rez.stream().filter(n -> n instanceof HBox).forEach(
                n -> {
                    List<Node> nodes = nodesPerLine.computeIfAbsent(((Integer) n.getUserData()), integer -> new ArrayList<Node>());
                    nodes.add(n);
                }
        );

        double rowWidth = (this.maxLineLength + this.lineNumWidth) * FONT_SYMBOL_WIDTH;

        final ArrayList<Node> rows = new ArrayList<>(1024);
        for (int line = 0; line < lines.size(); line++) {
            HBox row = new HBox();
            row.setMaxWidth(rowWidth);
            row.setMinWidth(rowWidth);
            row.setPrefWidth(rowWidth);
            row.setMaxHeight(ROW_HEIGHT);
            row.setMinHeight(ROW_HEIGHT);
            row.setPrefHeight(ROW_HEIGHT);
            //row.setStyle("-fx-border-style: solid inside; -fx-border-width: 1;-fx-border-color: red;");
            row.getChildren().addAll(nodesPerLine.get(line));
            rows.add(row);
        }
        decorateByPatch(rows);
        decorateByRawDiff(rows);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(rows);
        vBox.setMaxWidth(rowWidth);
        vBox.setMinWidth(rowWidth);
        vBox.setPrefWidth(rowWidth);
        return Collections.singletonList(vBox);
    }

    private void decorateByRawDiff(final ArrayList<Node> rows) {
        if (rawDiff) {
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
                    HBox row = (HBox) rows.get(lineIdx);
                    row.getStyleClass().add(style);
                }
            }
        }
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



    private int processMultilineToken(List<Node> rez, int lineIdx, String originalLine, String style, List<String> strings) {
        int mlline = 0;
        Iterator<String> stringIterator = strings.iterator();
        while (stringIterator.hasNext()) {
            if (mlline > 0) {
                rez.add(createLineNumbedNode(lineIdx, "")); // debug
            }
            String commentString = stringIterator.next();
            safeAdd(rez, createText(originalLine, commentString, style, lineIdx, "", rez.get(rez.size() - 1)));
            mlline++;
            if (mlline < (strings.size())) {
                lineIdx++;
            }
        }
        return strings.size() - 1;
    }

    /**
     * Add empty string which is delimit the parsed tokens.
     * * Can just a empty space from the begining of the line
     * or something = value;
     */
    private HBox createSpacesBetweenTokens(int lineIdx, String originalLine, Token prevToken, Token token, Node prevHBox) {
        int charsQty = prevToken == null ? token.getCharPositionInLine() : token.getStartIndex() - prevToken.getStopIndex() - 1;
        String empty = StringUtils.repeat(" ", charsQty);
        return createText(originalLine, empty, "default", lineIdx, "", prevHBox);
    }


    private HBox createLineNumbedNode(int lineIdx, String debugString) {
        final String lineNumText = StringUtils.leftPad(String.valueOf(lineIdx + 1), this.lineNumWidth, "0") + "  ";
        HBox node = createText("", lineNumText, "linenum", lineIdx, debugString, null);
        return node;
    }


    /**
     * Create node if necessary.
     *
     * @return null is concatenated to the prev node otherwise new node.
     */
    private HBox createText(final String lineString, final String tokenStringRaw, final String style, final int lineIdx, String debugString, final Node prevNode) {
        String tokenString = tokenStringRaw.replace("\n", "").replace("\r", "");
        if (prevNode instanceof HBox) {
            final HBox prevHBox = (HBox) prevNode;
            final Text prevText = (Text) prevHBox.getChildren().get(0);
            if (prevText.getStyleClass().contains(style)) {
                String newTExt = prevText.getText() + tokenString;
                prevText.setText(newTExt);
                adjustHBoxWidth(newTExt, prevHBox);
                return null;
            }
        }
        Text te = new Text(tokenString + debugString);
        te.setFont(FONT);
        //te.setTabSize(4);
        te.getStyleClass().add(style); //the font background is not working so background will be added to the hbox
        te.getStyleClass().add("kwfont");
        HBox hb = new HBox(te);
        hb.setUserData(lineIdx);
        hb.setSpacing(0);
        adjustHBoxWidth(tokenString, hb);

        /* Fun to visualize splitting */
        //hb.setStyle("-fx-border-style: solid inside; -fx-border-width: 1;-fx-border-color: gray;");
        return hb;
    }

    private void adjustHBoxWidth(String tokenString, HBox hb) {
        double width = FONT_SYMBOL_WIDTH * tokenString.length();
        hb.setMaxWidth(width);
        hb.setMinWidth(width);
        hb.setPrefWidth(width);
    }


    private ArrayList<String> getLines(final String content) {
        return (ArrayList) new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }

    private void safeAdd(List<Node> lst, Node node) {
        if (node != null) {
            lst.add(node);
        }
    }
}
