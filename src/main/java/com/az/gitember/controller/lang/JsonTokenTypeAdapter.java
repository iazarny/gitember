package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.json.JSONLexer;
import org.antlr.v4.runtime.Lexer;

public class JsonTokenTypeAdapter extends BaseTokenTypeAdapter {

    private JSONLexer jsonLexer;

    public JsonTokenTypeAdapter(Lexer lexer) {

        super(lexer);

        this.jsonLexer = (JSONLexer) lexer;

    }

    @Override
    public String adaptToStyleClass(int tokenType) {
        if (tokenType == jsonLexer.STRING) {
            return STRING;
        } else if (tokenType == jsonLexer.T__1) {
            return SEMICOLON;  //,
        } else if (tokenType == jsonLexer.T__6) { // boolean
            return BOOLEAN;  //,
        } else if (tokenType == jsonLexer.T__3) {
            return SEMICOLON;
        } else if (tokenType == jsonLexer.T__0 || tokenType == jsonLexer.T__2 || tokenType == jsonLexer.T__4 || tokenType == jsonLexer.T__5) {
            return BRACKET;
        } else if (tokenType == jsonLexer.NUMBER) {
            return DIGIT;
        }
        return super.adaptToStyleClass(tokenType);
    }
}
