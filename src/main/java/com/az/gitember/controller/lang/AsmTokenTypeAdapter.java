package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.asm.MASMLexer;
import org.antlr.v4.runtime.Lexer;

public class AsmTokenTypeAdapter extends BaseTokenTypeAdapter {

    private final MASMLexer lexer;

    public AsmTokenTypeAdapter(Lexer lexer) {
        super(lexer);
        this.lexer = (MASMLexer) lexer;
    }

    @Override
    public String adaptToStyleClass(int tokenType) {

        if (tokenType == lexer.KEYWORDS) {
            return KEYWORD;
        } else if (tokenType == lexer.DIRECTIVES) {
            return KEYWORD;        }
        else if (tokenType == lexer.REGISTERS) {
            return BOOLEAN;
        } else if (tokenType == lexer.Hexnum || tokenType == lexer.Integer|| tokenType == lexer.Octalnum || tokenType == lexer.FloatingPointLiteral) {
            return DIGIT;
        } else if (tokenType == lexer.String) {
            return STRING;
        } else if (tokenType == lexer.Etiqueta   ) {
            return STRING3;
        } else if (tokenType == lexer.Separator) {
            return SEMICOLON;
        } else if (tokenType == lexer.LINE_COMMENT) {
            return COMMENT;
        }


        return super.adaptToStyleClass(tokenType);
    }


}
