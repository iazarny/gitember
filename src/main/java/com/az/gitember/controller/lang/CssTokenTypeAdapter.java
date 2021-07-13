package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.css.css3Lexer;
import org.antlr.v4.runtime.Lexer;

public class CssTokenTypeAdapter extends BaseTokenTypeAdapter {

    private css3Lexer lexer;

    public CssTokenTypeAdapter(Lexer lexer) {
        super(lexer);
        this.lexer = (css3Lexer) lexer;
    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.T__0 && tokenType <= lexer.T__14) {
            return BRACKET;
        }

        if (tokenType == lexer.Comment) {
            return COMMENT;
        } else if (tokenType >= lexer.Includes &&  tokenType <= lexer.MediaOnly) {
            return KEYWORD;
        } else if (tokenType == lexer.Number) {
            return DIGIT;
        } else if (tokenType == lexer.String_ ) {
            return STRING;
        } else if (tokenType >= lexer.PrefixMatch &&  tokenType <= lexer.To) {
            return KEYWORD;
        } else if (tokenType == lexer.Variable || tokenType == lexer.Var  ) {
            return STRING3;
        } else if (tokenType == lexer.Function_) {
            return EMBEDED;

        }
        return super.adaptToStyleClass(tokenType);
    }
}
