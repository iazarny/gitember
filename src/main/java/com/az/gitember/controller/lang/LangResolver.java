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
import com.az.gitember.controller.lang.txt.SimpleTextLexer;
import com.az.gitember.controller.lang.typescript.TypeScriptLexer;
import com.az.gitember.data.Is;
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

        if (Is.string(lcFileExt).in("java")) {
            this.lexer = new Java9Lexer(charStream );
            this.tokenTypeAdapter = new JavaTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("json")) {
            this.lexer = new JSONLexer(charStream );
            this.tokenTypeAdapter = new  JsonTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("asm", "masm")) {
            this.lexer = new MASMLexer(charStream );
            this.tokenTypeAdapter = new  AsmTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("bas")) {
            this.lexer = new jvmBasicLexer(charStream );
            this.tokenTypeAdapter = new  BasicTokenTypeAdapter(lexer);
        //} else if ("c".equalsIgnoreCase(fileExtention) || "h".equalsIgnoreCase(fileExtention)) {
         //   this.lexer = new CLexer(charStream );
         //   this.tokenTypeAdapter = new  CTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("c", "h", "cc", "cpp", "hh", "hpp")) {
            this.lexer = new CPP14Lexer(charStream );
            this.tokenTypeAdapter = new  CppTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("cs", "csharp")) {
            this.lexer = new CSharpLexer(charStream );
            this.tokenTypeAdapter = new  CSharpTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("css")) {
            this.lexer = new css3Lexer(charStream );
            this.tokenTypeAdapter = new  CssTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("erl")) {
            this.lexer = new ErlangLexer(charStream );
            this.tokenTypeAdapter = new  ErlangTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("f","for", "f77", "f03", "f90")) {
            this.lexer = new Fortran77Lexer(charStream );
            this.tokenTypeAdapter = new  Fortran77TokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("go","golang")) {
            this.lexer = new GoLexer(charStream );
            this.tokenTypeAdapter = new  GoTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("html","html")) {
            this.lexer = new HTMLLexer(charStream );
            this.tokenTypeAdapter = new HtmlTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("js")) {
            this.lexer = new JavaScriptLexer(charStream );
            this.tokenTypeAdapter = new JavaScriptTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("ts")) {
            this.lexer = new TypeScriptLexer(charStream );
            this.tokenTypeAdapter = new TypeScriptTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("kt","ktm", "kts")) {
            this.lexer = new KotlinLexer(charStream );
            this.tokenTypeAdapter = new  KotlinTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("lua")) {
            this.lexer = new LuaLexer(charStream );
            this.tokenTypeAdapter = new  LuaTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("pas")) {
            this.lexer = new pascalLexer(charStream );
            this.tokenTypeAdapter = new  PascalTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("py")) {
            this.lexer = new Python3Lexer(charStream );
            this.tokenTypeAdapter = new  Python3TokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("rb")) {
            this.lexer = new CorundumLexer(charStream );
            this.tokenTypeAdapter = new  RubyTokenTypeAdapter(lexer);
        } else if (Is.string(lcFileExt).in("rs")) {
            this.lexer = new RustLexer(charStream );
            this.tokenTypeAdapter = new  RustTokenTypeAdapter(lexer);
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
