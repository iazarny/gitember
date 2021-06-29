// Generated from SimpleText.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.txt;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SimpleTextParser}.
 */
public interface SimpleTextListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SimpleTextParser#bool}.
	 * @param ctx the parse tree
	 */
	void enterBool(SimpleTextParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleTextParser#bool}.
	 * @param ctx the parse tree
	 */
	void exitBool(SimpleTextParser.BoolContext ctx);

	/**
	 * Enter a parse tree produced by {@link SimpleTextParser#brakets}.
	 * @param ctx the parse tree
	 */
	void enterBrakets(SimpleTextParser.BraketsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleTextParser#brakets}.
	 * @param ctx the parse tree
	 */
	void exitBrakets(SimpleTextParser.BraketsContext ctx);

	/**
	 * Enter a parse tree produced by {@link SimpleTextParser#math}.
	 * @param ctx the parse tree
	 */
	void enterMath(SimpleTextParser.MathContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleTextParser#math}.
	 * @param ctx the parse tree
	 */
	void exitMath(SimpleTextParser.MathContext ctx);
}