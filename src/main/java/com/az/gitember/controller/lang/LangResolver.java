package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.java.Java9Lexer;
import com.az.gitember.controller.lang.json.JSONLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public class LangResolver {

    private final Lexer lexer;
    private final BaseTokenTypeAdapter baseTokenTypeAdapter;
    private final String fileExtention;

    public LangResolver(String fileExtention, String content) {
        this.fileExtention = fileExtention;
        CharStream charStream = new ANTLRInputStream(content);
        if ("java".equalsIgnoreCase(fileExtention)) {
            this.lexer = new Java9Lexer(charStream );
            this.baseTokenTypeAdapter = new JavaTokenTypeAdapter(lexer);
        } else /*if ("json".equalsIgnoreCase(fileExtention))*/ {
            this.lexer = new JSONLexer(charStream );
            this.baseTokenTypeAdapter = new  JsonTokenTypeAdapter(lexer);
        }
    }

    public Lexer resolve() {
        return  lexer;
    }

    public BaseTokenTypeAdapter resolveAdapter() {

        return baseTokenTypeAdapter;
    }

}
