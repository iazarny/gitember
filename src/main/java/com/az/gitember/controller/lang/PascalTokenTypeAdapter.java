package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.pascal.pascalLexer;
import org.antlr.v4.runtime.Lexer;

public class PascalTokenTypeAdapter extends BaseTokenTypeAdapter {

    private pascalLexer lexer;

    public PascalTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (pascalLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.AND && tokenType <= lexer.WITH) {
            return KEYWORD;
        } else if (tokenType >= lexer.COMMA && tokenType <= lexer.SEMI) {
            return SEMICOLON;
        } else if (tokenType >= lexer.LPAREN && tokenType <= lexer.RBRACK2) {
            return BRACKET;
        } else if (tokenType >= lexer.LCURLY && tokenType <= lexer.RCURLY) {
            return BRACKET;
        } else if (tokenType == lexer.TRUE ||  tokenType == lexer.FALSE) {
            return BOOLEAN;
        } else if (tokenType == lexer.COMMENT_1 ||  tokenType == lexer.COMMENT_2) {
            return COMMENT;
        } else if (tokenType == lexer.STRING_LITERAL ) {
            return STRING;
        } else if (tokenType == lexer.NUM_INT  || tokenType == lexer.NUM_REAL) {
            return DIGIT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}