package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.py3.Python3Lexer;
import org.antlr.v4.runtime.Lexer;

public class Python3TokenTypeAdapter extends BaseTokenTypeAdapter {

    private Python3Lexer lexer;

    public Python3TokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (Python3Lexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.STRING || tokenType == lexer.STRING_LITERAL || tokenType == lexer.BYTES_LITERAL ) {
            return STRING;
        } else if (tokenType == lexer.NUMBER || tokenType == lexer.INTEGER
                || tokenType == lexer.FLOAT_NUMBER || tokenType == lexer.IMAG_NUMBER
                || tokenType == lexer.DECIMAL_INTEGER || tokenType == lexer.OCT_INTEGER
                || tokenType == lexer.HEX_INTEGER || tokenType == lexer.BIN_INTEGER
                || tokenType == lexer.FLOAT_NUMBER   ) {
            return DIGIT;
        } else if (tokenType == lexer.TRUE ||  tokenType == lexer.FALSE) {
            return BOOLEAN;
        } else if (tokenType >= lexer.DEF && tokenType <= lexer.AWAIT) {
            return KEYWORD;
        } else if (tokenType == lexer.OPEN_PAREN || tokenType == lexer.CLOSE_PAREN) {
            return BRACKET;
        } else if (tokenType == lexer.COMMA ||  tokenType == lexer.COLON||  tokenType == lexer.SEMI_COLON) {
            return SEMICOLON;
        } else if (tokenType == lexer.COMMENT ) {
            return COMMENT;

        }
        return super.adaptToStyleClass(tokenType);
    }

    @Override
    public boolean skip(int tokenType) {
        return tokenType == lexer.INDENT || tokenType == lexer.NEWLINE;
    }
}