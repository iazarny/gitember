package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.kotlin.KotlinLexer;
import org.antlr.v4.runtime.Lexer;

public class KotlinTokenTypeAdapter extends BaseTokenTypeAdapter {

    private KotlinLexer lexer;

    public KotlinTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (KotlinLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.ShebangLine || tokenType == lexer.DelimitedComment || tokenType == lexer.LineComment|| tokenType == lexer.StrExpr_Comment) {
            return COMMENT;
        } else if (tokenType >= lexer.LPAREN && tokenType <= lexer.RCURL) {
            return BRACKET;
        } else if (tokenType >= lexer.RETURN_AT && tokenType <= lexer.REIFIED) {
            return KEYWORD;
        } else if (tokenType == lexer.COMMA || tokenType == lexer.COLON|| tokenType == lexer.SEMICOLON) {
            return SEMICOLON;
        } else if (tokenType == lexer.BooleanLiteral)  {
            return BOOLEAN;
        } else if (tokenType >= lexer.RealLiteral && tokenType <= lexer.BinLiteral) {
            return DIGIT;
        } else if (tokenType >= lexer.LineStrRef && tokenType <= lexer.StrExpr_IN) {
            return STRING;
        } else if (tokenType == lexer.QUOTE_OPEN || tokenType == lexer.QUOTE_CLOSE || tokenType == lexer.SINGLE_QUOTE|| tokenType == lexer.CharacterLiteral
                || tokenType == lexer.TRIPLE_QUOTE_OPEN || tokenType == lexer.TRIPLE_QUOTE_CLOSE ) {
            return STRING;
        }
        return super.adaptToStyleClass(tokenType);
    }
}