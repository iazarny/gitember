package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.golang.GoLexer;
import org.antlr.v4.runtime.Lexer;

public class GoTokenTypeAdapter extends BaseTokenTypeAdapter {

    private GoLexer lexer;

    public GoTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (GoLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.BREAK && tokenType <= lexer.NIL_LIT) {
            return KEYWORD;
        } else if (tokenType == lexer.IDENTIFIER) {
            return  super.adaptToStyleClass(tokenType);
        } else if (tokenType >= lexer.L_PAREN && tokenType <= lexer.R_BRACKET) {
            return BRACKET;
        } else if (tokenType >= lexer.COMMA && tokenType <= lexer.COLON) {
            return SEMICOLON;
        } else if (tokenType >= lexer.DECIMAL_LIT && tokenType <= lexer.BIG_U_VALUE) {
            return DIGIT;
        } else if (tokenType == lexer.RAW_STRING_LIT || tokenType == lexer.INTERPRETED_STRING_LIT  ) {
            return STRING;
        } else if (tokenType == lexer.COMMENT || tokenType == lexer.LINE_COMMENT ) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
