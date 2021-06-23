package com.az.gitember.controller.lang.java.impl;

import com.az.gitember.controller.lang.java.Java9ParserBaseVisitor;
import com.az.gitember.controller.lang.java.Java9ParserVisitor;
import org.antlr.v4.runtime.tree.*;

import java.util.LinkedList;
import java.util.List;

public class Java9ParserVisitorImpl<T> extends Java9ParserBaseVisitor<T> implements Java9ParserVisitor<T> {


    private List<TerminalNode> parsedCode = new LinkedList<>();

    public T visitTerminal(TerminalNode node) {
        parsedCode.add(node);
        return super.visitTerminal(node);

    }

    public List<TerminalNode> getParsedCode() {
        return parsedCode;
    }


}
