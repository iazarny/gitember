package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.xml.XMLLexer;
import org.antlr.v4.runtime.Lexer;

public class XmlTokenTypeAdapter extends BaseTokenTypeAdapter {

    private XMLLexer lexer;

    public XmlTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (XMLLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.COMMENT && tokenType <= lexer.DTD) {
            return COMMENT;
        } else if (tokenType == lexer.TEXT) {
            return super.adaptToStyleClass(tokenType);
        } else if (tokenType >= lexer.OPEN && tokenType <= lexer.SLASH) {
            return KEYWORD;
        } else if (tokenType == lexer.STRING) {
            return STRING;
        } else if (tokenType == lexer.Name) {
            return KEYWORD;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
