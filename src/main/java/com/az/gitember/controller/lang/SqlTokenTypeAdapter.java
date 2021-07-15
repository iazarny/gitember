package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.sql.HiveLexer;
import org.antlr.v4.runtime.Lexer;

public class SqlTokenTypeAdapter extends BaseTokenTypeAdapter {

    private HiveLexer lexer;

    public SqlTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (HiveLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.KW_TRUE || tokenType == lexer.KW_FALSE) {
            return BOOLEAN;
        } else if (tokenType == lexer.LINE_COMMENT) {
            return COMMENT;
        } else if (tokenType >= lexer.KW_ALL && tokenType <= lexer.KW_SYNC) {
            return KEYWORD;
        } else if (tokenType == lexer.StringLiteral) {
            return STRING;
        } else if (tokenType >= lexer.LPAREN && tokenType <= lexer.RCURLY) {
            return BRACKET;
        } else if (tokenType >= lexer.NumberLiteral && tokenType <= lexer.Number) {
            return DIGIT;
        } else if (tokenType == lexer.DOT || tokenType == lexer.COLON || tokenType == lexer.COMMA || tokenType == lexer.SEMICOLON ) {
            return SEMICOLON;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
