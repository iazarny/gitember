package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.cpp.CSharpLexer;
import org.antlr.v4.runtime.Lexer;

public class CSharpTokenTypeAdapter extends BaseTokenTypeAdapter {

    private CSharpLexer lexer;

    public CSharpTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (CSharpLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.SINGLE_LINE_DOC_COMMENT && tokenType <= lexer.DELIMITED_COMMENT) {
            return COMMENT;
        } else if (tokenType >= lexer.SHARP &&  tokenType <= lexer.YIELD) {
            return KEYWORD;
        } else if (tokenType == lexer.IDENTIFIER) {
            return super.adaptToStyleClass(tokenType);
        } else if (tokenType == lexer.LITERAL_ACCESS || tokenType == lexer.CHARACTER_LITERAL || tokenType == lexer.REGULAR_STRING
                || tokenType == lexer.VERBATIUM_STRING  ) {
            return STRING;
        } else if (tokenType == lexer.INTERPOLATED_REGULAR_STRING_START || tokenType == lexer.INTERPOLATED_VERBATIUM_STRING_START  ) {
            return STRING3;
        } else if (tokenType == lexer.INTEGER_LITERAL || tokenType == lexer.HEX_INTEGER_LITERAL
                || tokenType == lexer.BIN_INTEGER_LITERAL || tokenType == lexer.REAL_LITERAL  ) {
            return DIGIT;
        } else if (tokenType >= lexer.OPEN_BRACE && tokenType <= lexer.CLOSE_PARENS ) {
            return BRACKET;
        } else if (tokenType >= lexer.DOT && tokenType <= lexer.SEMICOLON ) {
            return SEMICOLON;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
