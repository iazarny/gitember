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
        if (tokenType < lexer.HTML_COMMENT || tokenType < lexer.DTD) {
            return COMMENT;
        } else if (tokenType == lexer.SCRIPTLET) {
            return EMBEDED;
        } else if (tokenType >= lexer.SCRIPT_OPEN && tokenType < lexer.TAG_OPEN) {
            return EMBEDED;
        } else if (tokenType >= lexer.SCRIPT_BODY && tokenType < lexer.STYLE_BODY) {
            return EMBEDED;
        } else if (tokenType == lexer.ATTVALUE_VALUE)  {
            return STRING3;
        } else if (tokenType == lexer.ATTRIBUTE)  {
            return STRING;
        } else if (tokenType == lexer.TAG_OPEN || tokenType == lexer.TAG_CLOSE || tokenType == lexer.TAG_NAME || tokenType == lexer.TAG_SLASH || tokenType == lexer.TAG_SLASH_CLOSE )  {
            return KEYWORD;
        }
        return super.adaptToStyleClass(tokenType);
    }
}