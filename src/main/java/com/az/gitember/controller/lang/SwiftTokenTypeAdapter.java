package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.swift.Swift5Lexer;
import org.antlr.v4.runtime.Lexer;

public class SwiftTokenTypeAdapter extends BaseTokenTypeAdapter {

    private Swift5Lexer lexer;

    public SwiftTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (Swift5Lexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.AS && tokenType <= lexer.SELF_BIG) {
            return KEYWORD;
        } else if (tokenType >= lexer.FILE && tokenType <= lexer.SETTER) {
            return KEYWORD;
        } else if (tokenType >= lexer.LCURLY && tokenType <= lexer.RPAREN) {
            return BRACKET;
        } else if (tokenType == lexer.COMMA || tokenType == lexer.COLON || tokenType == lexer.SEMI ) {
            return SEMICOLON;
        } else if (tokenType >= lexer.Binary_literal && tokenType <= lexer.Floating_point_literal) {
            return DIGIT;
        } else if (tokenType >= lexer.Multi_line_extended_string_open && tokenType <= lexer.Line_comment) {
            return STRING;
        } else if (tokenType >= lexer.Block_comment && tokenType <= lexer.Quoted_multi_line_extended_text) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
