// Generated from jvmBasic.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.basic;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link jvmBasicParser}.
 */
public interface jvmBasicListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(jvmBasicParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(jvmBasicParser.ProgContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(jvmBasicParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(jvmBasicParser.LineContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#amperoper}.
	 * @param ctx the parse tree
	 */
	void enterAmperoper(jvmBasicParser.AmperoperContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#amperoper}.
	 * @param ctx the parse tree
	 */
	void exitAmperoper(jvmBasicParser.AmperoperContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#linenumber}.
	 * @param ctx the parse tree
	 */
	void enterLinenumber(jvmBasicParser.LinenumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#linenumber}.
	 * @param ctx the parse tree
	 */
	void exitLinenumber(jvmBasicParser.LinenumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#amprstmt}.
	 * @param ctx the parse tree
	 */
	void enterAmprstmt(jvmBasicParser.AmprstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#amprstmt}.
	 * @param ctx the parse tree
	 */
	void exitAmprstmt(jvmBasicParser.AmprstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(jvmBasicParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(jvmBasicParser.StatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void enterVardecl(jvmBasicParser.VardeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void exitVardecl(jvmBasicParser.VardeclContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#printstmt1}.
	 * @param ctx the parse tree
	 */
	void enterPrintstmt1(jvmBasicParser.Printstmt1Context ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#printstmt1}.
	 * @param ctx the parse tree
	 */
	void exitPrintstmt1(jvmBasicParser.Printstmt1Context ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#printlist}.
	 * @param ctx the parse tree
	 */
	void enterPrintlist(jvmBasicParser.PrintlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#printlist}.
	 * @param ctx the parse tree
	 */
	void exitPrintlist(jvmBasicParser.PrintlistContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#getstmt}.
	 * @param ctx the parse tree
	 */
	void enterGetstmt(jvmBasicParser.GetstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#getstmt}.
	 * @param ctx the parse tree
	 */
	void exitGetstmt(jvmBasicParser.GetstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#letstmt}.
	 * @param ctx the parse tree
	 */
	void enterLetstmt(jvmBasicParser.LetstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#letstmt}.
	 * @param ctx the parse tree
	 */
	void exitLetstmt(jvmBasicParser.LetstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#variableassignment}.
	 * @param ctx the parse tree
	 */
	void enterVariableassignment(jvmBasicParser.VariableassignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#variableassignment}.
	 * @param ctx the parse tree
	 */
	void exitVariableassignment(jvmBasicParser.VariableassignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#relop}.
	 * @param ctx the parse tree
	 */
	void enterRelop(jvmBasicParser.RelopContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#relop}.
	 * @param ctx the parse tree
	 */
	void exitRelop(jvmBasicParser.RelopContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#neq}.
	 * @param ctx the parse tree
	 */
	void enterNeq(jvmBasicParser.NeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#neq}.
	 * @param ctx the parse tree
	 */
	void exitNeq(jvmBasicParser.NeqContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#ifstmt}.
	 * @param ctx the parse tree
	 */
	void enterIfstmt(jvmBasicParser.IfstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#ifstmt}.
	 * @param ctx the parse tree
	 */
	void exitIfstmt(jvmBasicParser.IfstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#forstmt1}.
	 * @param ctx the parse tree
	 */
	void enterForstmt1(jvmBasicParser.Forstmt1Context ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#forstmt1}.
	 * @param ctx the parse tree
	 */
	void exitForstmt1(jvmBasicParser.Forstmt1Context ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#forstmt2}.
	 * @param ctx the parse tree
	 */
	void enterForstmt2(jvmBasicParser.Forstmt2Context ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#forstmt2}.
	 * @param ctx the parse tree
	 */
	void exitForstmt2(jvmBasicParser.Forstmt2Context ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#nextstmt}.
	 * @param ctx the parse tree
	 */
	void enterNextstmt(jvmBasicParser.NextstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#nextstmt}.
	 * @param ctx the parse tree
	 */
	void exitNextstmt(jvmBasicParser.NextstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#inputstmt}.
	 * @param ctx the parse tree
	 */
	void enterInputstmt(jvmBasicParser.InputstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#inputstmt}.
	 * @param ctx the parse tree
	 */
	void exitInputstmt(jvmBasicParser.InputstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#readstmt}.
	 * @param ctx the parse tree
	 */
	void enterReadstmt(jvmBasicParser.ReadstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#readstmt}.
	 * @param ctx the parse tree
	 */
	void exitReadstmt(jvmBasicParser.ReadstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#dimstmt}.
	 * @param ctx the parse tree
	 */
	void enterDimstmt(jvmBasicParser.DimstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#dimstmt}.
	 * @param ctx the parse tree
	 */
	void exitDimstmt(jvmBasicParser.DimstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#gotostmt}.
	 * @param ctx the parse tree
	 */
	void enterGotostmt(jvmBasicParser.GotostmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#gotostmt}.
	 * @param ctx the parse tree
	 */
	void exitGotostmt(jvmBasicParser.GotostmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#gosubstmt}.
	 * @param ctx the parse tree
	 */
	void enterGosubstmt(jvmBasicParser.GosubstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#gosubstmt}.
	 * @param ctx the parse tree
	 */
	void exitGosubstmt(jvmBasicParser.GosubstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#pokestmt}.
	 * @param ctx the parse tree
	 */
	void enterPokestmt(jvmBasicParser.PokestmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#pokestmt}.
	 * @param ctx the parse tree
	 */
	void exitPokestmt(jvmBasicParser.PokestmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#callstmt}.
	 * @param ctx the parse tree
	 */
	void enterCallstmt(jvmBasicParser.CallstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#callstmt}.
	 * @param ctx the parse tree
	 */
	void exitCallstmt(jvmBasicParser.CallstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#hplotstmt}.
	 * @param ctx the parse tree
	 */
	void enterHplotstmt(jvmBasicParser.HplotstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#hplotstmt}.
	 * @param ctx the parse tree
	 */
	void exitHplotstmt(jvmBasicParser.HplotstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#vplotstmt}.
	 * @param ctx the parse tree
	 */
	void enterVplotstmt(jvmBasicParser.VplotstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#vplotstmt}.
	 * @param ctx the parse tree
	 */
	void exitVplotstmt(jvmBasicParser.VplotstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#plotstmt}.
	 * @param ctx the parse tree
	 */
	void enterPlotstmt(jvmBasicParser.PlotstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#plotstmt}.
	 * @param ctx the parse tree
	 */
	void exitPlotstmt(jvmBasicParser.PlotstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#ongotostmt}.
	 * @param ctx the parse tree
	 */
	void enterOngotostmt(jvmBasicParser.OngotostmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#ongotostmt}.
	 * @param ctx the parse tree
	 */
	void exitOngotostmt(jvmBasicParser.OngotostmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#ongosubstmt}.
	 * @param ctx the parse tree
	 */
	void enterOngosubstmt(jvmBasicParser.OngosubstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#ongosubstmt}.
	 * @param ctx the parse tree
	 */
	void exitOngosubstmt(jvmBasicParser.OngosubstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#vtabstmnt}.
	 * @param ctx the parse tree
	 */
	void enterVtabstmnt(jvmBasicParser.VtabstmntContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#vtabstmnt}.
	 * @param ctx the parse tree
	 */
	void exitVtabstmnt(jvmBasicParser.VtabstmntContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#htabstmnt}.
	 * @param ctx the parse tree
	 */
	void enterHtabstmnt(jvmBasicParser.HtabstmntContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#htabstmnt}.
	 * @param ctx the parse tree
	 */
	void exitHtabstmnt(jvmBasicParser.HtabstmntContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#himemstmt}.
	 * @param ctx the parse tree
	 */
	void enterHimemstmt(jvmBasicParser.HimemstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#himemstmt}.
	 * @param ctx the parse tree
	 */
	void exitHimemstmt(jvmBasicParser.HimemstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#lomemstmt}.
	 * @param ctx the parse tree
	 */
	void enterLomemstmt(jvmBasicParser.LomemstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#lomemstmt}.
	 * @param ctx the parse tree
	 */
	void exitLomemstmt(jvmBasicParser.LomemstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#datastmt}.
	 * @param ctx the parse tree
	 */
	void enterDatastmt(jvmBasicParser.DatastmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#datastmt}.
	 * @param ctx the parse tree
	 */
	void exitDatastmt(jvmBasicParser.DatastmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#datum}.
	 * @param ctx the parse tree
	 */
	void enterDatum(jvmBasicParser.DatumContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#datum}.
	 * @param ctx the parse tree
	 */
	void exitDatum(jvmBasicParser.DatumContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#waitstmt}.
	 * @param ctx the parse tree
	 */
	void enterWaitstmt(jvmBasicParser.WaitstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#waitstmt}.
	 * @param ctx the parse tree
	 */
	void exitWaitstmt(jvmBasicParser.WaitstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#xdrawstmt}.
	 * @param ctx the parse tree
	 */
	void enterXdrawstmt(jvmBasicParser.XdrawstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#xdrawstmt}.
	 * @param ctx the parse tree
	 */
	void exitXdrawstmt(jvmBasicParser.XdrawstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#drawstmt}.
	 * @param ctx the parse tree
	 */
	void enterDrawstmt(jvmBasicParser.DrawstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#drawstmt}.
	 * @param ctx the parse tree
	 */
	void exitDrawstmt(jvmBasicParser.DrawstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#defstmt}.
	 * @param ctx the parse tree
	 */
	void enterDefstmt(jvmBasicParser.DefstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#defstmt}.
	 * @param ctx the parse tree
	 */
	void exitDefstmt(jvmBasicParser.DefstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#tabstmt}.
	 * @param ctx the parse tree
	 */
	void enterTabstmt(jvmBasicParser.TabstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#tabstmt}.
	 * @param ctx the parse tree
	 */
	void exitTabstmt(jvmBasicParser.TabstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#speedstmt}.
	 * @param ctx the parse tree
	 */
	void enterSpeedstmt(jvmBasicParser.SpeedstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#speedstmt}.
	 * @param ctx the parse tree
	 */
	void exitSpeedstmt(jvmBasicParser.SpeedstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#rotstmt}.
	 * @param ctx the parse tree
	 */
	void enterRotstmt(jvmBasicParser.RotstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#rotstmt}.
	 * @param ctx the parse tree
	 */
	void exitRotstmt(jvmBasicParser.RotstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#scalestmt}.
	 * @param ctx the parse tree
	 */
	void enterScalestmt(jvmBasicParser.ScalestmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#scalestmt}.
	 * @param ctx the parse tree
	 */
	void exitScalestmt(jvmBasicParser.ScalestmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#colorstmt}.
	 * @param ctx the parse tree
	 */
	void enterColorstmt(jvmBasicParser.ColorstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#colorstmt}.
	 * @param ctx the parse tree
	 */
	void exitColorstmt(jvmBasicParser.ColorstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#hcolorstmt}.
	 * @param ctx the parse tree
	 */
	void enterHcolorstmt(jvmBasicParser.HcolorstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#hcolorstmt}.
	 * @param ctx the parse tree
	 */
	void exitHcolorstmt(jvmBasicParser.HcolorstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#hlinstmt}.
	 * @param ctx the parse tree
	 */
	void enterHlinstmt(jvmBasicParser.HlinstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#hlinstmt}.
	 * @param ctx the parse tree
	 */
	void exitHlinstmt(jvmBasicParser.HlinstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#vlinstmt}.
	 * @param ctx the parse tree
	 */
	void enterVlinstmt(jvmBasicParser.VlinstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#vlinstmt}.
	 * @param ctx the parse tree
	 */
	void exitVlinstmt(jvmBasicParser.VlinstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#onerrstmt}.
	 * @param ctx the parse tree
	 */
	void enterOnerrstmt(jvmBasicParser.OnerrstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#onerrstmt}.
	 * @param ctx the parse tree
	 */
	void exitOnerrstmt(jvmBasicParser.OnerrstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#prstmt}.
	 * @param ctx the parse tree
	 */
	void enterPrstmt(jvmBasicParser.PrstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#prstmt}.
	 * @param ctx the parse tree
	 */
	void exitPrstmt(jvmBasicParser.PrstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#instmt}.
	 * @param ctx the parse tree
	 */
	void enterInstmt(jvmBasicParser.InstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#instmt}.
	 * @param ctx the parse tree
	 */
	void exitInstmt(jvmBasicParser.InstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#storestmt}.
	 * @param ctx the parse tree
	 */
	void enterStorestmt(jvmBasicParser.StorestmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#storestmt}.
	 * @param ctx the parse tree
	 */
	void exitStorestmt(jvmBasicParser.StorestmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#recallstmt}.
	 * @param ctx the parse tree
	 */
	void enterRecallstmt(jvmBasicParser.RecallstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#recallstmt}.
	 * @param ctx the parse tree
	 */
	void exitRecallstmt(jvmBasicParser.RecallstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#liststmt}.
	 * @param ctx the parse tree
	 */
	void enterListstmt(jvmBasicParser.ListstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#liststmt}.
	 * @param ctx the parse tree
	 */
	void exitListstmt(jvmBasicParser.ListstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#popstmt}.
	 * @param ctx the parse tree
	 */
	void enterPopstmt(jvmBasicParser.PopstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#popstmt}.
	 * @param ctx the parse tree
	 */
	void exitPopstmt(jvmBasicParser.PopstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#amptstmt}.
	 * @param ctx the parse tree
	 */
	void enterAmptstmt(jvmBasicParser.AmptstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#amptstmt}.
	 * @param ctx the parse tree
	 */
	void exitAmptstmt(jvmBasicParser.AmptstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#includestmt}.
	 * @param ctx the parse tree
	 */
	void enterIncludestmt(jvmBasicParser.IncludestmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#includestmt}.
	 * @param ctx the parse tree
	 */
	void exitIncludestmt(jvmBasicParser.IncludestmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#endstmt}.
	 * @param ctx the parse tree
	 */
	void enterEndstmt(jvmBasicParser.EndstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#endstmt}.
	 * @param ctx the parse tree
	 */
	void exitEndstmt(jvmBasicParser.EndstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#returnstmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnstmt(jvmBasicParser.ReturnstmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#returnstmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnstmt(jvmBasicParser.ReturnstmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#restorestmt}.
	 * @param ctx the parse tree
	 */
	void enterRestorestmt(jvmBasicParser.RestorestmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#restorestmt}.
	 * @param ctx the parse tree
	 */
	void exitRestorestmt(jvmBasicParser.RestorestmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(jvmBasicParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(jvmBasicParser.NumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#func_}.
	 * @param ctx the parse tree
	 */
	void enterFunc_(jvmBasicParser.Func_Context ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#func_}.
	 * @param ctx the parse tree
	 */
	void exitFunc_(jvmBasicParser.Func_Context ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#signExpression}.
	 * @param ctx the parse tree
	 */
	void enterSignExpression(jvmBasicParser.SignExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#signExpression}.
	 * @param ctx the parse tree
	 */
	void exitSignExpression(jvmBasicParser.SignExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#exponentExpression}.
	 * @param ctx the parse tree
	 */
	void enterExponentExpression(jvmBasicParser.ExponentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#exponentExpression}.
	 * @param ctx the parse tree
	 */
	void exitExponentExpression(jvmBasicParser.ExponentExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#multiplyingExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplyingExpression(jvmBasicParser.MultiplyingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#multiplyingExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplyingExpression(jvmBasicParser.MultiplyingExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#addingExpression}.
	 * @param ctx the parse tree
	 */
	void enterAddingExpression(jvmBasicParser.AddingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#addingExpression}.
	 * @param ctx the parse tree
	 */
	void exitAddingExpression(jvmBasicParser.AddingExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(jvmBasicParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(jvmBasicParser.RelationalExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(jvmBasicParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(jvmBasicParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#var_}.
	 * @param ctx the parse tree
	 */
	void enterVar_(jvmBasicParser.Var_Context ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#var_}.
	 * @param ctx the parse tree
	 */
	void exitVar_(jvmBasicParser.Var_Context ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#varname}.
	 * @param ctx the parse tree
	 */
	void enterVarname(jvmBasicParser.VarnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#varname}.
	 * @param ctx the parse tree
	 */
	void exitVarname(jvmBasicParser.VarnameContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#varsuffix}.
	 * @param ctx the parse tree
	 */
	void enterVarsuffix(jvmBasicParser.VarsuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#varsuffix}.
	 * @param ctx the parse tree
	 */
	void exitVarsuffix(jvmBasicParser.VarsuffixContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#varlist}.
	 * @param ctx the parse tree
	 */
	void enterVarlist(jvmBasicParser.VarlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#varlist}.
	 * @param ctx the parse tree
	 */
	void exitVarlist(jvmBasicParser.VarlistContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#exprlist}.
	 * @param ctx the parse tree
	 */
	void enterExprlist(jvmBasicParser.ExprlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#exprlist}.
	 * @param ctx the parse tree
	 */
	void exitExprlist(jvmBasicParser.ExprlistContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#sqrfunc}.
	 * @param ctx the parse tree
	 */
	void enterSqrfunc(jvmBasicParser.SqrfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#sqrfunc}.
	 * @param ctx the parse tree
	 */
	void exitSqrfunc(jvmBasicParser.SqrfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#chrfunc}.
	 * @param ctx the parse tree
	 */
	void enterChrfunc(jvmBasicParser.ChrfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#chrfunc}.
	 * @param ctx the parse tree
	 */
	void exitChrfunc(jvmBasicParser.ChrfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#lenfunc}.
	 * @param ctx the parse tree
	 */
	void enterLenfunc(jvmBasicParser.LenfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#lenfunc}.
	 * @param ctx the parse tree
	 */
	void exitLenfunc(jvmBasicParser.LenfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#ascfunc}.
	 * @param ctx the parse tree
	 */
	void enterAscfunc(jvmBasicParser.AscfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#ascfunc}.
	 * @param ctx the parse tree
	 */
	void exitAscfunc(jvmBasicParser.AscfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#midfunc}.
	 * @param ctx the parse tree
	 */
	void enterMidfunc(jvmBasicParser.MidfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#midfunc}.
	 * @param ctx the parse tree
	 */
	void exitMidfunc(jvmBasicParser.MidfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#pdlfunc}.
	 * @param ctx the parse tree
	 */
	void enterPdlfunc(jvmBasicParser.PdlfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#pdlfunc}.
	 * @param ctx the parse tree
	 */
	void exitPdlfunc(jvmBasicParser.PdlfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#peekfunc}.
	 * @param ctx the parse tree
	 */
	void enterPeekfunc(jvmBasicParser.PeekfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#peekfunc}.
	 * @param ctx the parse tree
	 */
	void exitPeekfunc(jvmBasicParser.PeekfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#intfunc}.
	 * @param ctx the parse tree
	 */
	void enterIntfunc(jvmBasicParser.IntfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#intfunc}.
	 * @param ctx the parse tree
	 */
	void exitIntfunc(jvmBasicParser.IntfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#spcfunc}.
	 * @param ctx the parse tree
	 */
	void enterSpcfunc(jvmBasicParser.SpcfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#spcfunc}.
	 * @param ctx the parse tree
	 */
	void exitSpcfunc(jvmBasicParser.SpcfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#frefunc}.
	 * @param ctx the parse tree
	 */
	void enterFrefunc(jvmBasicParser.FrefuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#frefunc}.
	 * @param ctx the parse tree
	 */
	void exitFrefunc(jvmBasicParser.FrefuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#posfunc}.
	 * @param ctx the parse tree
	 */
	void enterPosfunc(jvmBasicParser.PosfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#posfunc}.
	 * @param ctx the parse tree
	 */
	void exitPosfunc(jvmBasicParser.PosfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#usrfunc}.
	 * @param ctx the parse tree
	 */
	void enterUsrfunc(jvmBasicParser.UsrfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#usrfunc}.
	 * @param ctx the parse tree
	 */
	void exitUsrfunc(jvmBasicParser.UsrfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#leftfunc}.
	 * @param ctx the parse tree
	 */
	void enterLeftfunc(jvmBasicParser.LeftfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#leftfunc}.
	 * @param ctx the parse tree
	 */
	void exitLeftfunc(jvmBasicParser.LeftfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#rightfunc}.
	 * @param ctx the parse tree
	 */
	void enterRightfunc(jvmBasicParser.RightfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#rightfunc}.
	 * @param ctx the parse tree
	 */
	void exitRightfunc(jvmBasicParser.RightfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#strfunc}.
	 * @param ctx the parse tree
	 */
	void enterStrfunc(jvmBasicParser.StrfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#strfunc}.
	 * @param ctx the parse tree
	 */
	void exitStrfunc(jvmBasicParser.StrfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#fnfunc}.
	 * @param ctx the parse tree
	 */
	void enterFnfunc(jvmBasicParser.FnfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#fnfunc}.
	 * @param ctx the parse tree
	 */
	void exitFnfunc(jvmBasicParser.FnfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#valfunc}.
	 * @param ctx the parse tree
	 */
	void enterValfunc(jvmBasicParser.ValfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#valfunc}.
	 * @param ctx the parse tree
	 */
	void exitValfunc(jvmBasicParser.ValfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#scrnfunc}.
	 * @param ctx the parse tree
	 */
	void enterScrnfunc(jvmBasicParser.ScrnfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#scrnfunc}.
	 * @param ctx the parse tree
	 */
	void exitScrnfunc(jvmBasicParser.ScrnfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#sinfunc}.
	 * @param ctx the parse tree
	 */
	void enterSinfunc(jvmBasicParser.SinfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#sinfunc}.
	 * @param ctx the parse tree
	 */
	void exitSinfunc(jvmBasicParser.SinfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#cosfunc}.
	 * @param ctx the parse tree
	 */
	void enterCosfunc(jvmBasicParser.CosfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#cosfunc}.
	 * @param ctx the parse tree
	 */
	void exitCosfunc(jvmBasicParser.CosfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#tanfunc}.
	 * @param ctx the parse tree
	 */
	void enterTanfunc(jvmBasicParser.TanfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#tanfunc}.
	 * @param ctx the parse tree
	 */
	void exitTanfunc(jvmBasicParser.TanfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#atnfunc}.
	 * @param ctx the parse tree
	 */
	void enterAtnfunc(jvmBasicParser.AtnfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#atnfunc}.
	 * @param ctx the parse tree
	 */
	void exitAtnfunc(jvmBasicParser.AtnfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#rndfunc}.
	 * @param ctx the parse tree
	 */
	void enterRndfunc(jvmBasicParser.RndfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#rndfunc}.
	 * @param ctx the parse tree
	 */
	void exitRndfunc(jvmBasicParser.RndfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#sgnfunc}.
	 * @param ctx the parse tree
	 */
	void enterSgnfunc(jvmBasicParser.SgnfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#sgnfunc}.
	 * @param ctx the parse tree
	 */
	void exitSgnfunc(jvmBasicParser.SgnfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#expfunc}.
	 * @param ctx the parse tree
	 */
	void enterExpfunc(jvmBasicParser.ExpfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#expfunc}.
	 * @param ctx the parse tree
	 */
	void exitExpfunc(jvmBasicParser.ExpfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#logfunc}.
	 * @param ctx the parse tree
	 */
	void enterLogfunc(jvmBasicParser.LogfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#logfunc}.
	 * @param ctx the parse tree
	 */
	void exitLogfunc(jvmBasicParser.LogfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#absfunc}.
	 * @param ctx the parse tree
	 */
	void enterAbsfunc(jvmBasicParser.AbsfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#absfunc}.
	 * @param ctx the parse tree
	 */
	void exitAbsfunc(jvmBasicParser.AbsfuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link jvmBasicParser#tabfunc}.
	 * @param ctx the parse tree
	 */
	void enterTabfunc(jvmBasicParser.TabfuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link jvmBasicParser#tabfunc}.
	 * @param ctx the parse tree
	 */
	void exitTabfunc(jvmBasicParser.TabfuncContext ctx);
}