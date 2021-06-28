package com.az.gitember.controller;

import com.az.gitember.controller.lang.LangResolver;
import com.az.gitember.service.GitemberUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
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


    TextBrowserContentAdapter(final String content, final String extension, final boolean rawDiff) {
        this.langResolver = new LangResolver(extension, content);
        this.rawDiff = rawDiff;
        this.lines = getLines(content);
        this.lineNumWidth = String.valueOf(lines.size()).length();
        this.maxLineLength = lines.stream().mapToInt(String::length).max().orElseGet(() -> 80);

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
        for (int line = 1; line < 1 + lines.size(); line++) {
            int lineIdx = line - 1;
            final List<Token> tokens = tokensPerLine.get(line);
            final String originalLine = lines.get(lineIdx);
            rez.add(createLineNumbedNode(lineIdx, ""));

            if (tokens != null) {
                Token lastTokenInLine = tokens.get(tokens.size() - 1);
                Token prevToken = null;
                Iterator<Token> tokenIterator = tokens.iterator();
                while (tokenIterator.hasNext()) {
                    final Token token = tokenIterator.next();
                    final String tokenText = token.getText();
                    final String style = langResolver.getAdapter().adaptToStyleClass(token.getType());
                    safeAdd(rez, createSpacesBetweenTokens(lineIdx, originalLine, prevToken, token, rez.get(rez.size() - 1)));
                    List<String> strings = this.getLines(tokenText);
                    if (strings.size() > 1) { //multiline token
                        int mlline = 0;
                        Iterator<String> stringIterator = strings.iterator();
                        while (stringIterator.hasNext()) {
                            if (mlline > 0) {
                                rez.add(createLineNumbedNode(lineIdx, "")); // debug
                            }
                            String commentString = stringIterator.next();
                            safeAdd(rez, createText(originalLine, commentString, style, lineIdx, "", rez.get(rez.size() - 1)));
                            int lengtnTillEol;
                            if (mlline == 0) {
                                lengtnTillEol = this.maxLineLength - originalLine.length();
                            } else {
                                lengtnTillEol = this.maxLineLength - commentString.length();
                            }
                            safeAdd(rez, createText(originalLine, StringUtils.repeat(" ", lengtnTillEol), style, lineIdx, "", rez.get(rez.size() - 1))); // till end
                            mlline++;
                            if (mlline < (strings.size())) {
                                rez.add(new Text(" \n"));
                                line++;
                                lineIdx++;
                            }
                        }
                    } else { //single line
                        safeAdd(rez, createText(originalLine, tokenText, style, lineIdx, "", rez.get(rez.size() - 1)));
                        if (lastTokenInLine == token) {
                            safeAdd(rez, createText(originalLine, StringUtils.repeat(" ", this.maxLineLength - tokenText.length() - token.getCharPositionInLine()), style, lineIdx, "", rez.get(rez.size() - 1)));
                        }
                    }
                    prevToken = token;
                }

            } else { // empty line shall be right padded with spaces
                safeAdd(rez, createText(originalLine, StringUtils.repeat(" ", this.maxLineLength - originalLine.length()), "", lineIdx, "", rez.get(rez.size() - 1))); // till end
            }

            rez.add(new Text(" \n"));
        }

        //Set line num
        rez.stream().filter(n -> n instanceof HBox).forEach(
                n -> {
                    List<Node> nodes = nodesPerLine.computeIfAbsent(((Integer) n.getUserData()), integer -> new ArrayList<Node>());
                    nodes.add(n);
                }
        );


        decorateByPatch();
        decorateByRawDiff();

        return rez;
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


    private HBox createLineNumbedNode(int lineIdx, String hhhh) {
        final String lineNumText = StringUtils.leftPad(String.valueOf(lineIdx), this.lineNumWidth, "0") + "  ";
        HBox node = createText("", lineNumText, "linenum", lineIdx, hhhh, null);
        return node;
    }


    /**
     * Create node if necessary.
     *
     * @return null is concatenated to the prev node otherwise new node.
     */
    private HBox createText(final String lineString, final String tokenString, final String style, final int lineIdx, String hhhhh, final Node prevNode) {

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
        Text te = new Text(tokenString + hhhhh);
        te.setFont(FONT);
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

    private void decorateByRawDiff() {
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
                    List<Node> nodes = nodesPerLine.get(lineIdx);
                    if (nodes != null) {
                        nodes.forEach(node -> {
                                    HBox hb = ((HBox) node);
                                    hb.setBackground(new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
                                    hb.getStyleClass().add(style);
                                }
                        );
                    }
                }
            }
        }
    }


    /**
     * Highlight new , deleted and changed lines by patch.
     */
    private void decorateByPatch() {
        if (patch != null) {
            for (Edit delta : patch) {
                int origPos = delta.getBeginB();
                int origLines = delta.getLengthB();
                Color color = GitemberUtil.getDiffColor(delta);
                if (leftSide) {
                    origPos = delta.getBeginA();
                    origLines = delta.getLengthA();
                }

                for (int line = origPos; line < (origLines + origPos); line++) {
                    List<Node> nodes = nodesPerLine.get(line);
                    if (nodes != null) {
                        nodes.forEach(node -> {
                                    ((HBox) node).setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                                }
                        );
                    }
                }
            }
        }
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
