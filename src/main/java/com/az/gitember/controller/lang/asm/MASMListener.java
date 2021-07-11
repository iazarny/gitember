// Generated from MASM.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.asm;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MASMParser}.
 */
public interface MASMListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MASMParser#segments}.
	 * @param ctx the parse tree
	 */
	void enterSegments(MASMParser.SegmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MASMParser#segments}.
	 * @param ctx the parse tree
	 */
	void exitSegments(MASMParser.SegmentsContext ctx);
}