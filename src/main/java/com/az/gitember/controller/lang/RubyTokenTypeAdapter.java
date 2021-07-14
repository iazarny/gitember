package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.ruby.CorundumLexer;
import org.antlr.v4.runtime.Lexer;

public class RubyTokenTypeAdapter extends BaseTokenTypeAdapter {

    private CorundumLexer lexer;

    public RubyTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (CorundumLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.COMMA && tokenType <= lexer.SEMICOLON) {
            return SEMICOLON;
        } else if (tokenType >= lexer.REQUIRE && tokenType <= lexer.FOR) {
            return KEYWORD;
        } else if (tokenType >= lexer.LEFT_RBRACKET && tokenType <= lexer.RIGHT_SBRACKET) {
            return BRACKET;
        } else if (tokenType == lexer.NIL ) {
            return BOOLEAN;
        } else if (tokenType == lexer.LITERAL ) {
            return STRING;
        } else if (tokenType == lexer.SL_COMMENT || tokenType == lexer.ML_COMMENT ) {
            return COMMENT;
        } else if (tokenType >= lexer.INT && tokenType <= lexer.FLOAT) {
            return DIGIT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
