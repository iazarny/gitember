package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.fortran.Fortran77Lexer;
import org.antlr.v4.runtime.Lexer;

public class Fortran77TokenTypeAdapter extends BaseTokenTypeAdapter {

    private Fortran77Lexer lexer;

    public Fortran77TokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (Fortran77Lexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.PROGRAM && tokenType <= lexer.DOLLAR) {
            return KEYWORD;
        } else if (tokenType == lexer.COMMA ||  tokenType == lexer.COLON) {
            return SEMICOLON;
        } else if (tokenType == lexer.LPAREN ||  tokenType == lexer.RPAREN) {
            return BRACKET;
        } else if (tokenType >= lexer.TRUE &&  tokenType <= lexer.FALSE) {
            return STRING3;
        } else if (tokenType >= lexer.XCON &&  tokenType <= lexer.PRECISION) {
            return KEYWORD;
        } else if (tokenType == lexer.INTEGER || tokenType == lexer.RCON || tokenType == lexer.ICON) {
            return DIGIT;
        } else if (tokenType == lexer.LOGICAL  ) {
            return BOOLEAN;
        } else if (tokenType == lexer.STRINGLITERAL  ) {
            return STRING;
        } else if (tokenType == lexer.COMMENT  ) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
