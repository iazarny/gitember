package com.az.gitember.misc;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Igor_Azarny on 08.12.2016.
 */
public class HighlightProvider {


    private static String JAVA = "java";
    private static String DEFAULT = "default";
    private static Map<String, Pattern> patternPair = new HashMap<>();
    private static Map<String, String[]> keywordPair = new HashMap<String, String[]>() {{
        put(
                DEFAULT,
                new String[]{
                        "abstract", "assert", "boolean", "break", "byte",
                        "case", "catch", "char", "class", "const",
                        "continue", "default", "do", "double", "else",
                        "enum", "extends", "final", "finally", "float",
                        "for", "goto", "if", "implements", "import",
                        "instanceof", "int", "interface", "long", "native",
                        "new", "package", "private", "protected", "public",
                        "return", "short", "static", "strictfp", "super",
                        "switch", "synchronized", "this", "throw", "throws",
                        "transient", "try", "void", "volatile", "while",

                        "delete", "enumeration", "virtual", "dynamic", "object",
                        "export", "self", "delegate", "event", "explicit", "include",
                        "intern", "extern", "internal", "namespace", "ref", "sbyte",
                        "struct", "typeof", "uint", "ulong", "unchecked", "unsafe",
                        "ushort", "using", "lock", "is", "as", "readonly", "params",
                        "base", "done", "fi"
                }
        );
        put(
                JAVA,
                new String[]{
                        "abstract", "assert", "boolean", "break", "byte",
                        "case", "catch", "char", "class", "const",
                        "continue", "default", "do", "double", "else",
                        "enum", "extends", "final", "finally", "float",
                        "for", "goto", "if", "implements", "import",
                        "instanceof", "int", "interface", "long", "native",
                        "new", "package", "private", "protected", "public",
                        "return", "short", "static", "strictfp", "super",
                        "switch", "synchronized", "this", "throw", "throws",
                        "transient", "try", "void", "volatile", "while"
                }
        );
    }};

    static {
        patternPair.put(DEFAULT, createPattern(keywordPair.get(DEFAULT)));
    }

    public static Pattern resolvePattern(String fileName) {

        final String fileExtension = getFileExtension(fileName);

        if ( patternPair.keySet().contains(fileExtension)) {
            return patternPair.get(fileExtension);
        }

        if (keywordPair.keySet().contains(fileExtension)) {
            Pattern pattern = createPattern(keywordPair.get(fileExtension));
            patternPair.put(fileExtension, pattern);
            return pattern;
        }

        return patternPair.get(DEFAULT);
    }

    private static String getFileExtension(String fileName) {
        final String fileExtension = GitemberUtil.getExtension(fileName);
        if(fileExtension.isEmpty()) {
            return DEFAULT;
        }
        return fileExtension;
    }

    private static Pattern createPattern(String[] keywords) {
        String KEYWORD_PATTERN = "\\b(" + String.join("|", keywords) + ")\\b";
        String PAREN_PATTERN = "\\(|\\)";
        String BRACE_PATTERN = "\\{|\\}";
        String BRACKET_PATTERN = "\\[|\\]";
        String SEMICOLON_PATTERN = "\\;";
        String DIGIT_PATTERN = "\\p{Digit}+";
        String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
        //String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";


        return Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<DIGIT>" + DIGIT_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                      //  + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        );
    }


    public static StyleSpans<Collection<String>> computeHighlighting(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("DIGIT") != null ? "digit" :
                                                                    matcher.group("STRING") != null ? "string" :
                                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                                    null; /* never happens */
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singletonList(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }


}
