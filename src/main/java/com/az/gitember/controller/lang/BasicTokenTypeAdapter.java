package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.basic.jvmBasicLexer;
import org.antlr.v4.runtime.Lexer;

public class BasicTokenTypeAdapter extends BaseTokenTypeAdapter {

    private jvmBasicLexer lexer;

    public BasicTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (jvmBasicLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.STRINGLITERAL) {
            return STRING;
        } else if (tokenType == lexer.COMMENT) {
            return COMMENT;  //,
        } else if (tokenType == lexer.NUMBER || tokenType == lexer.FLOAT) { // boolean
            return DIGIT;
        } else if (tokenType <= lexer.CLS) {
            return KEYWORD;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
