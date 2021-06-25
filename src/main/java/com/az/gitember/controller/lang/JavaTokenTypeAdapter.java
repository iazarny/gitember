package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.java.Java9Lexer;
import org.antlr.v4.runtime.Lexer;

public class JavaTokenTypeAdapter extends BaseTokenTypeAdapter {

    private Java9Lexer java9Lexer;

    public JavaTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.java9Lexer = (Java9Lexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType < java9Lexer.IntegerLiteral) {
            return KEYWORD;
        } else if (tokenType == java9Lexer.CharacterLiteral || tokenType == java9Lexer.StringLiteral) {
            return STRING;
        } else if (tokenType == java9Lexer.NullLiteral || tokenType == java9Lexer.BooleanLiteral) {
            return KEYWORD;
        } else if (tokenType < java9Lexer.LPAREN) {
            return DIGIT;
        } else if (tokenType < java9Lexer.SEMI) {
            return BRACKET;
        } else if (tokenType == java9Lexer.COMMENT || tokenType == java9Lexer.LINE_COMMENT ) {
            return COMMENT;
        } else if (tokenType == java9Lexer.SEMI) {
            return SEMICOLON;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
