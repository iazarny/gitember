package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.java.Java9Lexer;
import com.az.gitember.controller.lang.json.JSONLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public class LangResolver {

    private final Lexer lexer;
    private final BaseTokenTypeAdapter baseTokenTypeAdapter;

    public LangResolver(String fileExtention, String content) {
        CharStream charStream = new ANTLRInputStream(content);
        if ("java".equalsIgnoreCase(fileExtention)) {
            this.lexer = new Java9Lexer(charStream );
            this.baseTokenTypeAdapter = new JavaTokenTypeAdapter(lexer);
        } else /*if ("json".equalsIgnoreCase(fileExtention))*/ {
            this.lexer = new JSONLexer(charStream );
            this.baseTokenTypeAdapter = new  JsonTokenTypeAdapter(lexer);
        }
    }

    public Lexer getLexer() {
        return  lexer;
    }

    public BaseTokenTypeAdapter getAdapter() {
        return baseTokenTypeAdapter;
    }

}
