package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.c.CLexer;
import com.az.gitember.controller.lang.java.Java9Lexer;
import org.antlr.v4.runtime.Lexer;

public class CTokenTypeAdapter extends BaseTokenTypeAdapter {

    private CLexer lexer;

    public CTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (CLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.T__1 && tokenType <= lexer.T__13) {
            return KEYWORD;
        } else if (tokenType == lexer.StringLiteral ) {
            return STRING;
        } else if (tokenType == lexer.Comma || tokenType == lexer.Semi || tokenType == lexer.Colon  ) {
            return SEMICOLON;
        /*} else if (tokenType == lexer.LeftParen || tokenType == lexer.LeftBrace || tokenType == lexer.LeftBracket  ) {
            return BRACKET;

        } else if (tokenType == lexer.RightParen || tokenType == lexer.RightBrace || tokenType == lexer.RightBracket  ) {
            return BRACKET;*/

        } else if (tokenType == lexer.Constant) {
            return DIGIT;
        } else if (tokenType == lexer.ComplexDefine || tokenType == lexer.IncludeDirective || tokenType == lexer.LineDirective || tokenType == lexer.PragmaDirective ) {
            return STRING3;
        } else if (tokenType == lexer.BlockComment || tokenType == lexer.LineComment ) {
            return COMMENT;
        } else if (tokenType == lexer.AsmBlock) {
            return EMBEDED;
        } else if (tokenType >= lexer.Auto && tokenType <= lexer.Ellipsis) {
            return KEYWORD;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
