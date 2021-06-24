package com.az.gitember.controller;

import com.az.gitember.controller.lang.BaseTokenTypeAdapter;
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
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.io.BufferedReader;
import java.io.StringReader;
import java.rmi.dgc.Lease;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Adapt to Text for TextFlow.
 */
public class TextBrowserContentAdapter {

    public static final double FONT_SIZE = 20;

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;
    private boolean rpadLines;
    private int maxLineLength = 0;

    private Color backgroundColor = null;
    private final String content;
    private List<Token> parsedCode;
    private Iterator<Token> parsedCodeIterator;
    private Token token;
    private final LangResolver langResolver;



    /**
     * @param extension The file extension
     * @param patch     patch
     * @param leftSide  what side ro read left - old, right - new
     * @param rpadLines pad lined withs space to length of line with max length ?
     */
    public TextBrowserContentAdapter(final String content, final String extension, final EditList patch, boolean leftSide, boolean rpadLines, Color backgroundColor) {
        this(content, extension, false, true);
        this.patch = patch;
        this.leftSide = leftSide;
        this.rpadLines = rpadLines;
        this.backgroundColor = backgroundColor;
    }


    TextBrowserContentAdapter(final String content, final String extension, final boolean rawDiff, final boolean rpadLines) {
        this.langResolver = new LangResolver(extension, content);
        CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.resolve());
        commonTokenStream.fill();
        this.content = content;
        this.parsedCode = commonTokenStream.getTokens();
        this.parsedCodeIterator = parsedCode.iterator();
        this.token = parsedCodeIterator.next();
        this.rawDiff = rawDiff;
        this.rpadLines = rpadLines;

    }

    //todo refactor the name
    public List<Node> getText() {
        final List<String> lines = getLines(content);
        final List<Node> rez = new LinkedList<>();
        final int pos = positions(lines.size());
        final Iterator<String> linesIterator = lines.iterator();
        int cnt = 0;
        if (rpadLines) {
            lines.stream().mapToInt(String::length).max().ifPresent(
                    i -> this.maxLineLength = i
            );
            this.maxLineLength = Math.max(80, this.maxLineLength);
        }

        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            rez.addAll(lineToTexts(line, cnt, pos));
            cnt++;
        }
        return rez;
    }

    private List<Node> lineToTexts(String line, int linePos, int pos) {
        final List<Node> rez = new LinkedList<>();
        final String lineNum = StringUtils.leftPad(String.valueOf(linePos), pos, "0") + "  ";

        rez.add(createText(lineNum, "linenum", linePos));

        String adjustedByLength = line;
        if (rpadLines) {
            adjustedByLength = StringUtils.rightPad(line, maxLineLength, " ");
        }

        rez.addAll(lineToTexts(adjustedByLength, linePos));

        rez.add(new Text(" \n")); // TODO length of biggest line
        return rez;
    }

    List<Node> lineToTexts(final String line, final int linePos) {
        final List<Node> rez = new LinkedList<>();

        if (rawDiff) {
            final String style;
            if (line.startsWith("+")) {
                style = "diff-new";
            } else if (line.startsWith("-")) {
                style = "diff-deleted";
            } else if (line.startsWith("@@")) {
                style = "diff-modified";
            } else {
                style = "";
            }
            rez.add(createText(line, style, linePos));
       // } else if (pattern == null) { TODO default txt lexer
         //   rez.add(createText(line, "", linePos));
        } else {

            int start = 0;

            while (token != null && (linePos+1) == token.getLine())  {
                int tokenStart = token.getCharPositionInLine();
                if (start < tokenStart) {
                    rez.add(createText(line.substring(start, tokenStart), "", linePos));
                    start = tokenStart;
                } else {
                    String tokenText = token.getText();
                    int tokenLength = tokenText.length();
                    rez.add(createText(tokenText, langResolver.resolveAdapter().adaptToStyleClass(token.getType()), linePos));
                    start = tokenStart + tokenLength;
                    if (parsedCodeIterator.hasNext()) {
                        token = parsedCodeIterator.next();
                    } else {
                        break;
                    }
                }
            }

            if (start < line.length()) {
                rez.add(createText(line.substring(start), "", linePos));
            }

        }
        return rez;
    }



    private List<String> getLines(final String content) {
        return new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }

    private int positions(int cnt) {
        return String.valueOf(cnt).length();
    }




    private Node createText(final String str, final String style, final int linePos) {

        Text te = new Text(str);
        te.setFont(Font.font("Monospace", FONT_SIZE));
        te.getStyleClass().add(style); //the font background is not working
        te.getStyleClass().add("kwfont");

        HBox hb = new HBox(te);

        hb.setSpacing(0);
        /*
        Fun to visualize splitting
        hb.setSpacing(10);
        hb.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
                + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: blue;");*/

        //hb.setStyle("-fx-border-style: solid inside;"
         //       + "-fx-border-width: 1;-fx-border-color: gray;");

        if (this.rawDiff) {

            hb.setBackground(
                    new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
            hb.getStyleClass().add(style);

        }
        if (patch != null) {
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

        return hb;
    }

}
