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
            List<Token> tokens = tokensPerLine.computeIfAbsent(token.getLine(), integer -> new LinkedList<>());
            tokens.add(token);
        });

        for (int line = 1; line < 1 + lines.size(); line++) {
            List<Token> tokens = tokensPerLine.get(line);
            Node node = createLineNumbedNode(line);
            rez.add(node);

            if (tokens != null) {
                Token prevToken = null;
                Iterator<Token> tokenIterator = tokens.iterator();
                while (tokenIterator.hasNext()) {
                    final Token token = tokenIterator.next();
                    final String tokenText = token.getText();
                    final String style = langResolver.getAdapter().adaptToStyleClass(token.getType());
                    String empty;
                    if (prevToken == null) {
                        empty = StringUtils.repeat(" ", token.getCharPositionInLine());
                    } else {
                        empty = StringUtils.repeat(" ", token.getStartIndex() - prevToken.getStopIndex() - 1);
                    }
                    rez.add(createText(empty, "", 88888888));


                    List<String> strings = this.getLines(tokenText);
                    if (strings.size() > 1) {
                        //multiline token
                        int mlline = 0;
                        Iterator<String> stringIterator = strings.iterator();
                        while (stringIterator.hasNext()) {
                            if (mlline > 0) {
                                rez.add(createLineNumbedNode(line));
                            }
                            mlline++;

                            rez.add(createText(stringIterator.next(), style, 88888888));
                            if (mlline < (strings.size() )) {
                                rez.add(new Text(" \n"));
                                line++;
                            }

                        }

                    } else {
                        rez.add(createText(tokenText, style, 88888888));
                    }


                    prevToken = token;
                }

            }

            rez.add(new Text(" \n"));
        }

        /*Set<Integer> lineAdded =  new HashSet<>();
        int lineNum = 1;

        while (parsedCodeIter.hasNext()) {
            final Token token = parsedCodeIter.next();
            if (lineNum <= token.getLine() && !lineAdded.contains(lineNum)) {
                for (int i = lineNum; i <= token.getLine(); i++) {
                    if (lineNum != 1) {
                        rez.add(new Text(" \n"));
                    }
                    Node node = createLineNumbedNode(lineNum);
                    rez.add(node);
                    lineAdded.add(lineNum);
                    lineNum++;
                }
            }

            if (prevToken != null) {
                String empty;
                if (prevToken.getLine() == token.getLine()) {


                } else {
                    empty = StringUtils.repeat(" ", token.getCharPositionInLine());

                }
                rez.add(createText(empty, "", 88888888));
            }



        }*/
        return rez;
    }


    private Node createLineNumbedNode(int lineNum) {
        final String lineNumText = StringUtils.leftPad(String.valueOf(lineNum), this.lineNumWidth, "0") + "  ";
        Node node = createText(lineNumText, "linenum", 77777777);
        return node;
    }

    //todo refactor the name
    /*public List<Node> getNodes() {
        //
        final Iterator<String> linesIterator = lines.iterator();
        int cnt = 0;
        lines.stream().mapToInt(String::length).max().ifPresent(
                i -> this.maxLineLength = i
        );
        this.maxLineLength = Math.max(80, this.maxLineLength);

        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            rez.addAll(lineToTexts(line, cnt, pos));
            cnt++;
        }
        return rez;
    }*/



    /*private List<Node> lineToTexts(String line, int linePos, int pos) {
        final List<Node> rez = new LinkedList<>();
        final String lineNum = StringUtils.leftPad(String.valueOf(linePos), pos, "0") + "  ";

        rez.add(createText(lineNum, "linenum", linePos));

        String adjustedByLength = line;
        adjustedByLength = StringUtils.rightPad(line, maxLineLength, " ");

        rez.addAll(lineToTexts(adjustedByLength, linePos));

        rez.add(new Text(" \n")); // TODO length of biggest line
        return rez;
    }*/

    /*List<Node> lineToTexts(final String line, final int linePos) {
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
        } else {

            int start = 0;

            while (token != null && (linePos+1) == token.getLine())  {
                int tokenStart = token.getCharPositionInLine();
                if (start < tokenStart) {
                    rez.add(createText(line.substring(start, tokenStart), "", linePos));
                    start = tokenStart;
                } else {
                    final String tokenText = token.getText();
                    final int tokenLength = tokenText.length();
                    final String style = langResolver.getAdapter().adaptToStyleClass(token.getType());

                    //TODo multiline comment

                    rez.add(createText(tokenText, style, linePos));
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
    }*/


    private List<String> getLines(final String content) {
        return new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }


    private Node createText(final String string, final String style, final int linePos) {
        final String str = string;
        /*if (string.contains("\n")) {
            str = string.substring(0, string.indexOf("\n") -1 );
        } else {
            str = string;
        }*/

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
        //     + "-fx-border-width: 1;-fx-border-color: gray;");

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
