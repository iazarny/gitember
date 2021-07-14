package com.az.gitember.controller.lang;

import org.antlr.v4.runtime.Lexer;

public class BaseTokenTypeAdapter {

    public static String KEYWORD = "keyword";
    public static String BRACKET = "bracket";
    public static String SEMICOLON = "semicolon";
    public static String DIGIT = "digit";
    public static String STRING = "string";
    public static String STRING3 = "string3";
    public static String COMMENT = "comment";
    public static String BOOLEAN = "boolean";
    public static String DEFAULT = "default";
    public static String EMBEDED = "embeded";
    public static String DEBUG = "debug";

    private final Lexer lexer;

    public BaseTokenTypeAdapter(Lexer lexer) {
        this.lexer = lexer;
    }

    public String adaptToStyleClass(int tokenType) {
        return DEFAULT;
    }

    public boolean skip(int tokenType) {
        return false;
    }

    public Lexer getLexer() {
        return lexer;
    }
}
