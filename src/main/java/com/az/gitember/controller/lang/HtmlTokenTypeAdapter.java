package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.html.HTMLLexer;
import org.antlr.v4.runtime.Lexer;

public class HtmlTokenTypeAdapter extends BaseTokenTypeAdapter {

    private HTMLLexer lexer;

    public HtmlTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (HTMLLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType < lexer.TAG) {
            return KEYWORD;
        } else if (tokenType == lexer.ATTRIBUTE) {
            return STRING;
        } else if (tokenType < lexer.TAG_OPEN || tokenType < lexer.TAG_CLOSE) {
            return BRACKET;
        } else if (tokenType == lexer.HTML_COMMENT || tokenType == lexer.HTML_CONDITIONAL_COMMENT || tokenType == lexer.CDATA) {
            return COMMENT;
        } else if (tokenType == lexer.SCRIPT_BODY ||  tokenType == lexer.SCRIPT_SHORT_BODY ||  tokenType == lexer.STYLE_BODY || tokenType == lexer.STYLE_SHORT_BODY) {
            return EMBEDED;
        }
        return super.adaptToStyleClass(tokenType);
    }
}