package com.az.gitember.controller;

import com.az.gitember.data.LangDefinition;
import com.az.gitember.service.LangResolver;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Adapt to Text for TextFlow.
 */
public class TextBrowserContentAdapter {

    public static final double FONT_SIZE = 20;

    private LangResolver langResolver = new LangResolver();

    private boolean rawDiff = false;
    private Pattern pattern = null;
    private EditList patch = null;
    private boolean leftSide;
    private boolean rpadLines;
    private int maxLineLength = 0;

    private boolean multilineComment = false;
    private boolean sinlglineComment = false;
    private Color backgroundColor = null;

    /**
     * @param extension The file extension
     * @param patch     patch
     * @param leftSide  what side ro read left - old, right - new
     * @param rpadLines pad lined withs space to length of line with max length ?
     */
    public TextBrowserContentAdapter(final String extension, final EditList patch, boolean leftSide, boolean rpadLines, Color backgroundColor) {
        this(extension, false, true);
        this.patch = patch;
        this.leftSide = leftSide;
        this.rpadLines = rpadLines;
        this.backgroundColor = backgroundColor;
    }


    TextBrowserContentAdapter(final String extension, final boolean rawDiff, final boolean rpadLines) {

        this.rawDiff = rawDiff;
        this.rpadLines = rpadLines;

        langResolver.resolveLang(extension).ifPresent(ld -> {

            pattern = createPattern(ld);

        });
    }

    public List<Node> getText(final String content) {
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
        sinlglineComment = false;

        String styleClass = "";

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
        } else if (pattern == null) {
            rez.add(createText(line, "", linePos));
        } else {
            int start = 0;
            int end = 0;
            String candidate = "";
            boolean matcherFind = false;



            do {
                final String str = line.substring(start, end);
                final Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    matcherFind = true;
                    String matchedGroup = getMatchedGroup(matcher);
                    styleClass = calculateStyleClass(matchedGroup, linePos);
                    //TODO refactor - method, which create textext
                    if (matcher.start() == 0) {
                        rez.add(createText(str, styleClass, linePos));
                    } else {
                        if (multilineComment || sinlglineComment) {
                            rez.add(createText(str.substring(0, matcher.start()), styleClass, linePos));
                        } else {
                            rez.add(createText(str.substring(0, matcher.start()), "", linePos));
                        }
                        rez.add(createText(str.substring(matcher.start(), matcher.end()), styleClass, linePos));
                    }

                    if ("MLEND".equals(matchedGroup)) {
                        multilineComment = false;
                        sinlglineComment = false;
                    }
                    start = end;
                    candidate = "";
                } else {
                    matcherFind = false;
                    candidate = str;
                }
                end++;

            } while (end <= line.length());





            if (multilineComment) {
                styleClass = "mlcomment";
            }
            if (candidate.length() > 0) {
                rez.add(createText(candidate, (multilineComment || matcherFind ) ? styleClass : "", linePos));
            }
        }
        return rez;
    }

    private String getMatchedGroup(Matcher matcher) {
        return matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("BRACKET") != null ? "bracket" :
                        matcher.group("SEMICOLON") != null ? "semicolon" :
                                matcher.group("DIGIT") != null ? "digit" :
                                        matcher.group("STRING") != null ? "string" :
                                                matcher.group("STRING2") != null ? "string" :
                                                        matcher.group("STRING3") != null ? "string3" :
                                                                matcher.group("SLCOMMENT") != null ? "comment" :
                                                                        matcher.group("MLBEGIN") != null ? "MLBEGIN" :
                                                                                matcher.group("MLEND") != null ? "MLEND" :
                                                                                        null;
    }

    private List<String> getLines(final String content) {
        return new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }

    private int positions(int cnt) {
        return String.valueOf(cnt).length();
    }

    private String calculateStyleClass(final String matchedGroup, final int linePos) {

        String styleClass = matchedGroup;

        if ("comment".equals(matchedGroup)) {
            sinlglineComment = true;
        }
        if ("MLBEGIN".equals(matchedGroup)) {
            multilineComment = true;
        }
        if (multilineComment) {
            styleClass = "mlcomment";
        }
        if (sinlglineComment) {
            styleClass = "comment";
        }

        return styleClass;
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
        //        + "-fx-border-width: 1;-fx-border-color: gray;");

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

    private final Map<String, Pattern> langParserCache = new HashMap<>();

    private Pattern createPattern(LangDefinition ld) {

        return langParserCache.computeIfAbsent(ld.getName(), s -> {
            //work in test mode, however not in real
            String KEYWORD_PATTERN = "\\b(" + String.join("|", ld.getKeywords()) + ")(?=[^\"|^']*(?:\"[^\"]*\"[^\"]*)*$)";
            String BRACKET_PATTERN = "\\{|\\(|\\[|}|\\)|]";
            String SEMICOLON_PATTERN = ";";
            String DIGIT_PATTERN = "\\b\\d+\\b";
            String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
            String STRING_PATTERN2 = "'([^'\\\\]|\\\\.)*'";
            String STRING_PATTERN3 = "`([^`\\\\]|\\\\.)*`";
            String COMMENT_SINGELINE_PATTERN = "//.*";
            String COMMENT_MULTILINE_BEGIN_PATTERN = "/\\*";
            String COMMENT_MULTI_END_PATTERN = "\\*/";

            String exp = "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<DIGIT>" + DIGIT_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<STRING2>" + STRING_PATTERN2 + ")"
                    + "|(?<STRING3>" + STRING_PATTERN3 + ")";
            if (ld.isComments()) {
                exp += "|(?<SLCOMMENT>" + COMMENT_SINGELINE_PATTERN + ")"
                        + "|(?<MLBEGIN>" + COMMENT_MULTILINE_BEGIN_PATTERN + ")"
                        + "|(?<MLEND>" + COMMENT_MULTI_END_PATTERN + ")";
            }

            return Pattern.compile(exp, Pattern.CANON_EQ);

        });

    }

}
