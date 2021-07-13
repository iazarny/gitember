package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.asm.MASMLexer;
import com.az.gitember.controller.lang.basic.jvmBasicLexer;
import com.az.gitember.controller.lang.c.CLexer;
import com.az.gitember.controller.lang.cpp.CPP14Lexer;
import com.az.gitember.controller.lang.cpp.CSharpLexer;
import com.az.gitember.controller.lang.css.css3Lexer;
import com.az.gitember.controller.lang.erlang.ErlangLexer;
import com.az.gitember.controller.lang.fortran.Fortran77Lexer;
import com.az.gitember.controller.lang.html.HTMLLexer;
import com.az.gitember.controller.lang.java.Java9Lexer;
import com.az.gitember.controller.lang.json.JSONLexer;
import com.az.gitember.controller.lang.txt.SimpleTextLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public class LangResolver {

    private final Lexer lexer;
    private final BaseTokenTypeAdapter tokenTypeAdapter;

    public LangResolver(String fileExtention, String content) {
        CharStream charStream = new ANTLRInputStream(content);
        if ("java".equalsIgnoreCase(fileExtention)) {
            this.lexer = new Java9Lexer(charStream );
            this.tokenTypeAdapter = new JavaTokenTypeAdapter(lexer);
        } else if ("json".equalsIgnoreCase(fileExtention)) {
            this.lexer = new JSONLexer(charStream );
            this.tokenTypeAdapter = new  JsonTokenTypeAdapter(lexer);
        } else if ("asm".equalsIgnoreCase(fileExtention)) {
            this.lexer = new MASMLexer(charStream );
            this.tokenTypeAdapter = new  AsmTokenTypeAdapter(lexer);
        } else if ("bas".equalsIgnoreCase(fileExtention)) {
            this.lexer = new jvmBasicLexer(charStream );
            this.tokenTypeAdapter = new  BasicTokenTypeAdapter(lexer);
        } else if ("c".equalsIgnoreCase(fileExtention) || "h".equalsIgnoreCase(fileExtention)) {
            this.lexer = new CLexer(charStream );
            this.tokenTypeAdapter = new  CTokenTypeAdapter(lexer);
        } else if ("cc".equalsIgnoreCase(fileExtention) || "cpp".equalsIgnoreCase(fileExtention)
                || "hh".equalsIgnoreCase(fileExtention)|| "hpp".equalsIgnoreCase(fileExtention)) {
            this.lexer = new CPP14Lexer(charStream );
            this.tokenTypeAdapter = new  CppTokenTypeAdapter(lexer);
        } else if ("cs".equalsIgnoreCase(fileExtention)) {
            this.lexer = new CSharpLexer(charStream );
            this.tokenTypeAdapter = new  CSharpTokenTypeAdapter(lexer);
        } else if ("css".equalsIgnoreCase(fileExtention)) {
            this.lexer = new css3Lexer(charStream );
            this.tokenTypeAdapter = new  CssTokenTypeAdapter(lexer);
        } else if ("erl".equalsIgnoreCase(fileExtention)) {
            this.lexer = new ErlangLexer(charStream );
            this.tokenTypeAdapter = new  ErlangTokenTypeAdapter(lexer);
        } else if ("f".equalsIgnoreCase(fileExtention) || "for".equalsIgnoreCase(fileExtention)
                || "f77".equalsIgnoreCase(fileExtention) || "f03".equalsIgnoreCase(fileExtention)
                || "f90".equalsIgnoreCase(fileExtention)) {
            this.lexer = new Fortran77Lexer(charStream );
            this.tokenTypeAdapter = new  Fortran77TokenTypeAdapter(lexer);
/*
        } else if ("html".equalsIgnoreCase(fileExtention) || "htm".equalsIgnoreCase(fileExtention)) {
            this.lexer = new HTMLLexer(charStream );
            this.tokenTypeAdapter = new HtmlTokenTypeAdapter(lexer);
*/
        } else  {
            this.lexer = new SimpleTextLexer(charStream );
            this.tokenTypeAdapter = new SimpleTextTypeAdapter(lexer);
        }
    }

    public Lexer getLexer() {
        return  lexer;
    }

    public BaseTokenTypeAdapter getAdapter() {
        return tokenTypeAdapter;
    }

}
