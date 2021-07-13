package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.c.CLexer;
import com.az.gitember.controller.lang.cpp.CPP14Lexer;
import org.antlr.v4.runtime.Lexer;

public class CppTokenTypeAdapter extends BaseTokenTypeAdapter {

    private CPP14Lexer lexer;

    public CppTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (CPP14Lexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.IntegerLiteral || tokenType == lexer.HexadecimalLiteral || tokenType == lexer.OctalLiteral
                || tokenType == lexer.BinaryLiteral|| tokenType == lexer.FloatingLiteral|| tokenType == lexer.DecimalLiteral) {
            return DIGIT;
        } else if (tokenType == lexer.BooleanLiteral ||  tokenType == lexer.PointerLiteral) {
            return BOOLEAN;
        } else if (tokenType == lexer.StringLiteral) {
            return STRING;
        } else if (tokenType == lexer.Comma || tokenType == lexer.Semi || tokenType == lexer.Colon  || tokenType == lexer.Doublecolon  ) {
            return SEMICOLON;
        } else if (tokenType == lexer.LeftParen || tokenType == lexer.LeftBrace || tokenType == lexer.LeftBracket  ) {
            return BRACKET;
        } else if (tokenType == lexer.RightParen || tokenType == lexer.RightBrace || tokenType == lexer.RightBracket  ) {
            return BRACKET;
        } else if (tokenType == lexer.Identifier) {
            return super.adaptToStyleClass(tokenType);
        } else if (tokenType == lexer.Directive ) {
            return STRING3;
        } else if (tokenType == lexer.BlockComment || tokenType == lexer.LineComment ) {
            return COMMENT;
        } else if (tokenType == lexer.Asm) {
            return EMBEDED;
        } else if (tokenType >= lexer.Auto && tokenType <= lexer.Ellipsis) {
            return KEYWORD;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
