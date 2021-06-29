package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.txt.SimpleTextLexer;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

public class SimpleTextTypeAdapter extends BaseTokenTypeAdapter {

    private SimpleTextLexer textLexer;

    public SimpleTextTypeAdapter(Lexer lexer) {

        super(lexer);

        this.textLexer = (SimpleTextLexer) lexer;
/*
* Про сто камент для проверки */
    }

    @Override
    public String adaptToStyleClass(int tokenType) {

        if (tokenType == textLexer.T__0 || tokenType == textLexer.T__1) { // boolean
            return BOOLEAN;  //,
        } else if (tokenType == textLexer.DOUBLE || tokenType == textLexer.NUMBER|| tokenType == textLexer.EXPONENT|| tokenType == textLexer.EXPONENT2) {
            return DIGIT;
        } else if (tokenType >= textLexer.T__2 && tokenType <= textLexer.T__7) {
            return BRACKET;
        } else if (tokenType >= textLexer.T__8 && tokenType <= textLexer.T__12) {
            return SEMICOLON;        }
        else if (tokenType == textLexer.DOT && tokenType == textLexer.COMA) {
            return SEMICOLON;
        } else if (tokenType == textLexer.StringLiteral ) {
            return STRING;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
