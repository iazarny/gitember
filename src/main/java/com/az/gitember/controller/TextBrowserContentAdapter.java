package com.az.gitember.controller;

import com.az.gitember.controller.lang.LangResolver;
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

    public static final double FONT_SIZE = 20;
    public static final double FONT_SYMBOL_WIDTH =11.99;

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;
    private final int maxLineLength;
    private final int lineNumWidth;
    private final ArrayList<String> lines;

    private Color backgroundColor = null;

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
        this.backgroundColor = LookAndFeelSet.DIFF_FILL_COLOR;
    }


    TextBrowserContentAdapter(final String content, final String extension, final boolean rawDiff) {
        this.langResolver = new LangResolver(extension, content);
        this.rawDiff = rawDiff;
        this.lines = getLines(content);
        this.lineNumWidth = String.valueOf(lines.size()).length();
        this.maxLineLength = lines.stream().mapToInt(String::length).max().orElseGet(() -> 80);
    }

    public List<Node> getText() {

        final List<Node> rez = new LinkedList<>();
        final CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.getLexer());
        commonTokenStream.fill();
        final List<Token> parsedCode = commonTokenStream.getTokens();
        final Map<Integer, List<Token>> tokensPerLine = new HashMap<>();
        parsedCode.stream().forEach(token -> {
            List<Token> tokens = tokensPerLine.computeIfAbsent(token.getLine(), integer -> new ArrayList<>());
            tokens.add(token);
        });

        for (int line = 1; line < 1 + lines.size(); line++) {
            final int lineIdx = line - 1;
            final List<Token> tokens = tokensPerLine.get(line);
            final Node node = createLineNumbedNode(line);
            final String originalLine = lines.get(lineIdx);
            rez.add(node);

            if (tokens != null) {
                Token lastTokenInLine = tokens.get(tokens.size() - 1);
                Token prevToken = null;
                Iterator<Token> tokenIterator = tokens.iterator();
                while (tokenIterator.hasNext()) {
                    final Token token = tokenIterator.next();
                    final String tokenText = token.getText();
                    final String style = langResolver.getAdapter().adaptToStyleClass(token.getType());
                    rez.add(createSpacesBetweenTokens(line, originalLine, prevToken, token));
                    List<String> strings = this.getLines(tokenText);
                    if (strings.size() > 1) { //multiline token
                        int mlline = 0;
                        Iterator<String> stringIterator = strings.iterator();
                        while (stringIterator.hasNext()) {
                            if (mlline > 0) {
                                rez.add(createLineNumbedNode(line));
                            }
                            String commentString = stringIterator.next();
                            rez.add(createText(originalLine, commentString, style, lineIdx));
                            int lengtnTillEol;
                            if (mlline == 0) {
                                lengtnTillEol = this.maxLineLength - originalLine.length();
                            } else {
                                lengtnTillEol = this.maxLineLength - commentString.length();
                            }
                            rez.add(createText(originalLine, StringUtils.repeat(" ", lengtnTillEol), style, lineIdx)); // till end
                            mlline++;
                            if (mlline < (strings.size())) {
                                rez.add(new Text(" \n"));
                                line++;
                            }
                        }
                    } else { //single line
                        rez.add(createText(originalLine, tokenText, style, lineIdx));
                        if (lastTokenInLine == token) {
                            rez.add(createText(originalLine, StringUtils.repeat(" ", this.maxLineLength - tokenText.length() - token.getCharPositionInLine()), style, lineIdx));
                        }
                    }
                    prevToken = token;
                }

            } else { // empty line shall be right padded with spaces
                rez.add(createText(originalLine, StringUtils.repeat(" ", this.maxLineLength - originalLine.length()), "", lineIdx)); // till end
            }

            rez.add(new Text(" \n"));
        }


        return rez;
    }

    /**
     * Add empty string which is delimit the parsed tokens.
     * Can just a empty space from the begining of the line
     * or something = value;
     */
    private Node createSpacesBetweenTokens(int line, String originalLine, Token prevToken, Token token) {
        String empty;
        if (prevToken == null) {
            empty = StringUtils.repeat(" ", token.getCharPositionInLine());
        } else {
            empty = StringUtils.repeat(" ", token.getStartIndex() - prevToken.getStopIndex() - 1);
        }
        return createText(originalLine, empty, "", line - 1);
    }


    private Node createLineNumbedNode(int lineNum) {
        final String lineNumText = StringUtils.leftPad(String.valueOf(lineNum), this.lineNumWidth, "0") + "  ";
        Node node = createText("", lineNumText, "linenum", lineNum - 1);
        return node;
    }


    private ArrayList<String> getLines(final String content) {
        return (ArrayList) new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }


    private Node createText(final String lineString, final String tokenString, final String style, final int linePos) {

        Font font = Font.font("Monospace", FONT_SIZE);
        Text te = new Text(tokenString);
        te.setFont(font);
        te.getStyleClass().add(style); //the font background is not working
        te.getStyleClass().add("kwfont");

        HBox hb = new HBox(te);
        double width = FONT_SYMBOL_WIDTH * tokenString.length();


        hb.setMaxWidth(width);
        hb.setMinWidth(width);
        hb.setPrefWidth(width);

        hb.setSpacing(0);
        /*
        Fun to visualize splitting
        hb.setSpacing(10);
        hb.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
                + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: blue;");*/

        // hb.setStyle("-fx-border-style: solid inside;"
        //    + "-fx-border-width: 1;-fx-border-color: gray;");

        if (this.rawDiff) {
            decorateByRawDiff(lineString, hb);
        }

        if (patch != null) {
            decorateByPatch(linePos, hb);
        }

        return hb;
    }

    private void decorateByPatch(int linePos, HBox hb) {
        for (Edit delta : patch) {
            int origPos;
            int origLines;

            if (leftSide) {
                origPos = delta.getBeginA();
                origLines = delta.getLengthA();
            } else {
                origPos = delta.getBeginB();
                origLines = delta.getLengthB();
            }

            if (origPos <= linePos && linePos < (origLines + origPos)) {
                hb.setBackground(
                        new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    private void decorateByRawDiff(final String lineString, HBox hb) {
        final String style;
        if (lineString.startsWith("+")) {
            style = "diff-new";
        } else if (lineString.startsWith("-")) {
            style = "diff-deleted";
        } else if (lineString.startsWith("@@")) {
            style = "diff-modified";
        } else {
            style = "";
        }
        hb.setBackground(
                new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
        hb.getStyleClass().add(style);
    }

}
