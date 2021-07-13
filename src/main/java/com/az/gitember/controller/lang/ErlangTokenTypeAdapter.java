package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.erlang.ErlangLexer;
import org.antlr.v4.runtime.Lexer;

public class ErlangTokenTypeAdapter extends BaseTokenTypeAdapter {

    private ErlangLexer lexer;

    public ErlangTokenTypeAdapter(Lexer lexer) {
        super(lexer);
        this.lexer = (ErlangLexer) lexer;
    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.T__0 && tokenType <= lexer.T__14) {
            return BRACKET;
        }

        if (tokenType == lexer.Comment) {
            return COMMENT;
        } else if (tokenType >= lexer.TokFloat &&  tokenType <= lexer.TokInteger) {
            return DIGIT;
        } else if (tokenType >= lexer.TokChar &&  tokenType <= lexer.TokString) {
            return STRING;
        } else if (tokenType == lexer.AttrName) {
            return STRING3;
        } else if (tokenType == lexer.Comment) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
