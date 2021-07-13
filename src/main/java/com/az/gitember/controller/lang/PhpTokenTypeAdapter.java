package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.php.PhpLexer;
import org.antlr.v4.runtime.Lexer;

public class PhpTokenTypeAdapter extends BaseTokenTypeAdapter {

    private PhpLexer lexer;

    public PhpTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (PhpLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.HtmlText) {
            return EMBEDED;
        } else if (tokenType == lexer.HtmlComment || tokenType == lexer.SingleLineComment|| tokenType == lexer.ShellStyleComment|| tokenType == lexer.MultiLineComment) {
            return COMMENT;
        } else if (tokenType >= lexer.Abstract && tokenType <= lexer.DoubleArrow) {
            return KEYWORD;
        } else if (tokenType == lexer.DoubleColon || tokenType == lexer.Comma|| tokenType == lexer.SemiColon) {
            return SEMICOLON;
        } else if (tokenType == lexer.BoolType)  {
            return BOOLEAN;
        } else if (tokenType >= lexer.Octal && tokenType <= lexer.Binary) {
            return DIGIT;
        } else if (tokenType == lexer.Comment) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}