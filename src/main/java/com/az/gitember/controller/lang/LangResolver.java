package com.az.gitember.controller.lang;

import com.az.gitember.controller.lang.asm.MASMLexer;
import com.az.gitember.controller.lang.basic.jvmBasicLexer;
import com.az.gitember.controller.lang.cpp.CPP14Lexer;
import com.az.gitember.controller.lang.cpp.CSharpLexer;
import com.az.gitember.controller.lang.css.css3Lexer;
import com.az.gitember.controller.lang.erlang.ErlangLexer;
import com.az.gitember.controller.lang.fortran.Fortran77Lexer;
import com.az.gitember.controller.lang.golang.GoLexer;
import com.az.gitember.controller.lang.html.HTMLLexer;
import com.az.gitember.controller.lang.java.Java9Lexer;
import com.az.gitember.controller.lang.javascript.JavaScriptLexer;
import com.az.gitember.controller.lang.json.JSONLexer;
import com.az.gitember.controller.lang.kotlin.KotlinLexer;
import com.az.gitember.controller.lang.lua.LuaLexer;
import com.az.gitember.controller.lang.pascal.pascalLexer;
import com.az.gitember.controller.lang.py3.Python3Lexer;
import com.az.gitember.controller.lang.ruby.CorundumLexer;
import com.az.gitember.controller.lang.rust.RustLexer;
import com.az.gitember.controller.lang.sql.HiveLexer;
import com.az.gitember.controller.lang.swift.Swift5Lexer;
import com.az.gitember.controller.lang.txt.SimpleTextLexer;
import com.az.gitember.controller.lang.typescript.TypeScriptLexer;
import com.az.gitember.controller.lang.xml.XMLLexer;
import static  com.az.gitember.service.GitemberUtil.is;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

import java.util.Locale;

public class LangResolver {

    private final Lexer lexer;
    private final BaseTokenTypeAdapter tokenTypeAdapter;

    public LangResolver(String fileExtention, String content) {
        final CharStream charStream = new ANTLRInputStream(content);
        final String lcFileExt = fileExtention.toLowerCase(Locale.ROOT);

        if (is(lcFileExt).oneOf("java", "scala")) {
            this.lexer = new Java9Lexer(charStream );
            this.tokenTypeAdapter = new JavaTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("json")) {
            this.lexer = new JSONLexer(charStream );
            this.tokenTypeAdapter = new  JsonTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("asm", "masm")) {
            this.lexer = new MASMLexer(charStream );
            this.tokenTypeAdapter = new  AsmTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("bas")) {
            this.lexer = new jvmBasicLexer(charStream );
            this.tokenTypeAdapter = new  BasicTokenTypeAdapter(lexer);
        //} else if ("c".equalsIgnoreCase(fileExtention) || "h".equalsIgnoreCase(fileExtention)) {
         //   this.lexer = new CLexer(charStream );
         //   this.tokenTypeAdapter = new  CTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("c", "h", "cc", "cpp", "hh", "hpp")) {
            this.lexer = new CPP14Lexer(charStream );
            this.tokenTypeAdapter = new  CppTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("cs", "csharp")) {
            this.lexer = new CSharpLexer(charStream );
            this.tokenTypeAdapter = new  CSharpTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("css")) {
            this.lexer = new css3Lexer(charStream );
            this.tokenTypeAdapter = new  CssTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("erl")) {
            this.lexer = new ErlangLexer(charStream );
            this.tokenTypeAdapter = new  ErlangTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("f","for", "f77", "f03", "f90")) {
            this.lexer = new Fortran77Lexer(charStream );
            this.tokenTypeAdapter = new  Fortran77TokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("go","golang")) {
            this.lexer = new GoLexer(charStream );
            this.tokenTypeAdapter = new  GoTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("html","html")) {
            this.lexer = new HTMLLexer(charStream );
            this.tokenTypeAdapter = new HtmlTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("js")) {
            this.lexer = new JavaScriptLexer(charStream );
            this.tokenTypeAdapter = new JavaScriptTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("ts")) {
            this.lexer = new TypeScriptLexer(charStream );
            this.tokenTypeAdapter = new TypeScriptTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("kt","ktm", "kts")) {
            this.lexer = new KotlinLexer(charStream );
            this.tokenTypeAdapter = new  KotlinTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("lua")) {
            this.lexer = new LuaLexer(charStream );
            this.tokenTypeAdapter = new  LuaTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("pas")) {
            this.lexer = new pascalLexer(charStream );
            this.tokenTypeAdapter = new  PascalTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("py")) {
            this.lexer = new Python3Lexer(charStream );
            this.tokenTypeAdapter = new  Python3TokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("rb")) {
            this.lexer = new CorundumLexer(charStream );
            this.tokenTypeAdapter = new  RubyTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("rs")) {
            this.lexer = new RustLexer(charStream );
            this.tokenTypeAdapter = new  RustTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("swift")) {
            this.lexer = new Swift5Lexer(charStream );
            this.tokenTypeAdapter = new  SwiftTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("sql")) {
            this.lexer = new HiveLexer(charStream );
            this.tokenTypeAdapter = new  SqlTokenTypeAdapter(lexer);
        } else if (is(lcFileExt).oneOf("xml", "mxml", "fxml")) {
            this.lexer = new XMLLexer(charStream );
            this.tokenTypeAdapter = new  XmlTokenTypeAdapter(lexer);
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
