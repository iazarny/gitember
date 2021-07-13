package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.javascript.JavaScriptLexer;
import org.antlr.v4.runtime.Lexer;

public class JavaScriptTokenTypeAdapter extends BaseTokenTypeAdapter {

    private JavaScriptLexer lexer;

    public JavaScriptTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (JavaScriptLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if ((tokenType >= lexer.HashBangLine && tokenType <= lexer.SingleLineComment) || tokenType == lexer.HtmlComment|| tokenType == lexer.CDataComment) {
            return COMMENT;
        } else if (tokenType == lexer.RegularExpressionLiteral)  {
            return STRING3;
        } else if (tokenType >= lexer.OpenBracket && tokenType <= lexer.CloseBrace) {
            return BRACKET;
        } else if (tokenType == lexer.SemiColon || tokenType < lexer.Colon|| tokenType < lexer.Comma) {
            return SEMICOLON;
        } else if (tokenType == lexer.BooleanLiteral)  {
            return BOOLEAN;
        } else if (tokenType >= lexer.DecimalLiteral && tokenType <= lexer.BigDecimalIntegerLiteral) {
            return DIGIT;
        } else if (tokenType >= lexer.Break && tokenType <= lexer.Yield) {
            return KEYWORD;
        } else if (tokenType == lexer.StringLiteral)  {
            return STRING;
        }
        return super.adaptToStyleClass(tokenType);
    }
}