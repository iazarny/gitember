package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.rust.RustLexer;
import org.antlr.v4.runtime.Lexer;

public class RustTokenTypeAdapter extends BaseTokenTypeAdapter {

    private RustLexer lexer;

    public RustTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (RustLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.KW_AS && tokenType <= lexer.KW_DOLLARCRATE) {
            return KEYWORD;
        } else if (tokenType >= lexer.LINE_COMMENT && tokenType <= lexer.SHEBANG) {
            return COMMENT;
        } else if (tokenType >= lexer.CHAR_LITERAL && tokenType <= lexer.RAW_BYTE_STRING_LITERAL) {
            return STRING;
        } else if (tokenType >= lexer.INTEGER_LITERAL && tokenType <= lexer.FLOAT_LITERAL) {
            return DIGIT;
        } else if (tokenType >= lexer.COMMA && tokenType <= lexer.COLON) {
            return SEMICOLON;
        } else if (tokenType >= lexer.LCURLYBRACE && tokenType <= lexer.RPAREN) {
            return BRACKET;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
