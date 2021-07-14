package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.rust.RustLexer;
import com.az.gitember.controller.lang.scala.ScalaLexer;
import org.antlr.v4.runtime.Lexer;

public class ScalaTokenTypeAdapter extends BaseTokenTypeAdapter {

    private ScalaLexer lexer;

    public ScalaTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (ScalaLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType >= lexer.T__0 && tokenType <= lexer.T__60) {
            return KEYWORD;
        } else if (tokenType == lexer.BooleanLiteral) {
            return BOOLEAN;
        } else if ((tokenType >= lexer.CharacterLiteral && tokenType <= lexer.SymbolLiteral) || tokenType <= lexer.StringLiteral) {
            return STRING;
        } else if (tokenType >= lexer.IntegerLiteral && tokenType <= lexer.FloatingPointLiteral) {
            return DIGIT;
        } else if (tokenType == lexer.Paren) {
            return BRACKET;
        } else if (tokenType >= lexer.Delim && tokenType <= lexer.Semi) {
            return SEMICOLON;
        } else if (tokenType >= lexer.COMMENT && tokenType <= lexer.LINE_COMMENT) {
            return COMMENT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
