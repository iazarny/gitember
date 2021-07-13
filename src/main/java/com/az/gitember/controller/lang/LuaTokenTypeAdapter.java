package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.kotlin.KotlinLexer;
import com.az.gitember.controller.lang.lua.LuaLexer;
import org.antlr.v4.runtime.Lexer;

public class LuaTokenTypeAdapter extends BaseTokenTypeAdapter {

    private LuaLexer lexer;

    public LuaTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.lexer = (LuaLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == lexer.SHEBANG || tokenType == lexer.COMMENT || tokenType == lexer.LINE_COMMENT) {
            return COMMENT;
        } else if (tokenType >= lexer.T__0 && tokenType <= lexer.T__54) {
            return KEYWORD;
        } else if (tokenType >= lexer.INT && tokenType <= lexer.HEX_FLOAT) {
            return DIGIT;
        } else if (tokenType == lexer.NORMALSTRING || tokenType == lexer.CHARSTRING || tokenType == lexer.LONGSTRING ) {
            return STRING;
        }
        return super.adaptToStyleClass(tokenType);
    }
}