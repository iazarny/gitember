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

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;
    private final int maxLineLength;
    private final int lineNumWidth;
    private final List<String> lines;
    private Color backgroundColor = null;
    private final LangResolver langResolver;
    final String content;



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
        this.content = content;
    }

    //todo refactor the name
    public List<Node> getText() {
        final List<String> lines = getLines(content);
        final List<Node> rez = new LinkedList<>();
        final Iterator<String> linesIterator = lines.iterator();

        final CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.getLexer());
        commonTokenStream.fill();
        final List<Token> parsedCode = commonTokenStream.getTokens();
        parsedCodeIter = parsedCode.iterator();
        token = parsedCodeIter.next();

        int cnt = 0;

        while (linesIterator.hasNext()) {
            final String line = linesIterator.next();

            final String lineNum = StringUtils.leftPad(String.valueOf(cnt), lineNumWidth, "0") + "  ";
            rez.add(createText(lineNum, "linenum"));

            final String padLine = StringUtils.rightPad(line, maxLineLength, " ");
            if (rawDiff) {
                rez.add(createText(padLine, calculateRawDiffStyle(line)));
            } else {
                rez.addAll(lineToTexts(padLine,  cnt));
            }

            rez.add(new Text(" \n"));
            cnt++;
        }
        return rez;
    }

    Iterator<Token> parsedCodeIter ;
    Token token ;

    List<Node> lineToTexts(final String line,  final int linePos) {
        final List<Node> rez = new LinkedList<>();
        int start = 0;



            while (token != null && (linePos+1) == token.getLine())  {
                int tokenStart = token.getCharPositionInLine();
                if (start < tokenStart) {
                    rez.add(createText(line.substring(start, tokenStart), ""));
                    start = tokenStart;
                } else {
                    final String tokenText = token.getText();
                    final int tokenLength = tokenText.length();
                    final String style = langResolver.getAdapter().adaptToStyleClass(token.getType());

                    //TODo multiline comment

                    rez.add(createText(tokenText, style));
                    start = tokenStart + tokenLength;
                    if (parsedCodeIter.hasNext()) {
                        token = parsedCodeIter.next();
                    } else {
                        break;
                    }

                }
            }
            if (start < line.length()) {
                rez.add(createText(line.substring(start), ""));
            }
        return rez;
    }



    private Node createText(final String str, final String style) {

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
        //        + "-fx-border-width: 1;-fx-border-color: gray;");

        if (this.rawDiff) {

            hb.setBackground(
                    new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY)));
            hb.getStyleClass().add(style);

        }
        /*if (patch != null) {
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
        }*/

        return hb;
    }

    private String calculateRawDiffStyle(String line) {
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
        return style;
    }

    private List<String> getLines(final String content) {
        return new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }

}
