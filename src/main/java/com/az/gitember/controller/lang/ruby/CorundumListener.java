// Generated from Corundum.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.ruby;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CorundumParser}.
 */
public interface CorundumListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CorundumParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(CorundumParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(CorundumParser.ProgContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(CorundumParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(CorundumParser.Expression_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CorundumParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CorundumParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#global_get}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_get(CorundumParser.Global_getContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#global_get}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_get(CorundumParser.Global_getContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#global_set}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_set(CorundumParser.Global_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#global_set}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_set(CorundumParser.Global_setContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#global_result}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_result(CorundumParser.Global_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#global_result}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_result(CorundumParser.Global_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_inline_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_inline_call(CorundumParser.Function_inline_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_inline_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_inline_call(CorundumParser.Function_inline_callContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#require_block}.
	 * @param ctx the parse tree
	 */
	void enterRequire_block(CorundumParser.Require_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#require_block}.
	 * @param ctx the parse tree
	 */
	void exitRequire_block(CorundumParser.Require_blockContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#pir_inline}.
	 * @param ctx the parse tree
	 */
	void enterPir_inline(CorundumParser.Pir_inlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#pir_inline}.
	 * @param ctx the parse tree
	 */
	void exitPir_inline(CorundumParser.Pir_inlineContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#pir_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterPir_expression_list(CorundumParser.Pir_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#pir_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitPir_expression_list(CorundumParser.Pir_expression_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition(CorundumParser.Function_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition(CorundumParser.Function_definitionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition_body}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition_body(CorundumParser.Function_definition_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition_body}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition_body(CorundumParser.Function_definition_bodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition_header}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition_header(CorundumParser.Function_definition_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition_header}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition_header(CorundumParser.Function_definition_headerContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(CorundumParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(CorundumParser.Function_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition_params}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition_params(CorundumParser.Function_definition_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition_params}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition_params(CorundumParser.Function_definition_paramsContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition_params_list}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition_params_list(CorundumParser.Function_definition_params_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition_params_list}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition_params_list(CorundumParser.Function_definition_params_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_definition_param_id}.
	 * @param ctx the parse tree
	 */
	void enterFunction_definition_param_id(CorundumParser.Function_definition_param_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_definition_param_id}.
	 * @param ctx the parse tree
	 */
	void exitFunction_definition_param_id(CorundumParser.Function_definition_param_idContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#return_statement}.
	 * @param ctx the parse tree
	 */
	void enterReturn_statement(CorundumParser.Return_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#return_statement}.
	 * @param ctx the parse tree
	 */
	void exitReturn_statement(CorundumParser.Return_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(CorundumParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(CorundumParser.Function_callContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_call_param_list}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call_param_list(CorundumParser.Function_call_param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_call_param_list}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call_param_list(CorundumParser.Function_call_param_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_call_params}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call_params(CorundumParser.Function_call_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_call_params}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call_params(CorundumParser.Function_call_paramsContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_param}.
	 * @param ctx the parse tree
	 */
	void enterFunction_param(CorundumParser.Function_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_param}.
	 * @param ctx the parse tree
	 */
	void exitFunction_param(CorundumParser.Function_paramContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_unnamed_param}.
	 * @param ctx the parse tree
	 */
	void enterFunction_unnamed_param(CorundumParser.Function_unnamed_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_unnamed_param}.
	 * @param ctx the parse tree
	 */
	void exitFunction_unnamed_param(CorundumParser.Function_unnamed_paramContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_named_param}.
	 * @param ctx the parse tree
	 */
	void enterFunction_named_param(CorundumParser.Function_named_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_named_param}.
	 * @param ctx the parse tree
	 */
	void exitFunction_named_param(CorundumParser.Function_named_paramContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#function_call_assignment}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call_assignment(CorundumParser.Function_call_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#function_call_assignment}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call_assignment(CorundumParser.Function_call_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#all_result}.
	 * @param ctx the parse tree
	 */
	void enterAll_result(CorundumParser.All_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#all_result}.
	 * @param ctx the parse tree
	 */
	void exitAll_result(CorundumParser.All_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#elsif_statement}.
	 * @param ctx the parse tree
	 */
	void enterElsif_statement(CorundumParser.Elsif_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#elsif_statement}.
	 * @param ctx the parse tree
	 */
	void exitElsif_statement(CorundumParser.Elsif_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#if_elsif_statement}.
	 * @param ctx the parse tree
	 */
	void enterIf_elsif_statement(CorundumParser.If_elsif_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#if_elsif_statement}.
	 * @param ctx the parse tree
	 */
	void exitIf_elsif_statement(CorundumParser.If_elsif_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#if_statement}.
	 * @param ctx the parse tree
	 */
	void enterIf_statement(CorundumParser.If_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#if_statement}.
	 * @param ctx the parse tree
	 */
	void exitIf_statement(CorundumParser.If_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#unless_statement}.
	 * @param ctx the parse tree
	 */
	void enterUnless_statement(CorundumParser.Unless_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#unless_statement}.
	 * @param ctx the parse tree
	 */
	void exitUnless_statement(CorundumParser.Unless_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#while_statement}.
	 * @param ctx the parse tree
	 */
	void enterWhile_statement(CorundumParser.While_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#while_statement}.
	 * @param ctx the parse tree
	 */
	void exitWhile_statement(CorundumParser.While_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#for_statement}.
	 * @param ctx the parse tree
	 */
	void enterFor_statement(CorundumParser.For_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#for_statement}.
	 * @param ctx the parse tree
	 */
	void exitFor_statement(CorundumParser.For_statementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#init_expression}.
	 * @param ctx the parse tree
	 */
	void enterInit_expression(CorundumParser.Init_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#init_expression}.
	 * @param ctx the parse tree
	 */
	void exitInit_expression(CorundumParser.Init_expressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#all_assignment}.
	 * @param ctx the parse tree
	 */
	void enterAll_assignment(CorundumParser.All_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#all_assignment}.
	 * @param ctx the parse tree
	 */
	void exitAll_assignment(CorundumParser.All_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#for_init_list}.
	 * @param ctx the parse tree
	 */
	void enterFor_init_list(CorundumParser.For_init_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#for_init_list}.
	 * @param ctx the parse tree
	 */
	void exitFor_init_list(CorundumParser.For_init_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#cond_expression}.
	 * @param ctx the parse tree
	 */
	void enterCond_expression(CorundumParser.Cond_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#cond_expression}.
	 * @param ctx the parse tree
	 */
	void exitCond_expression(CorundumParser.Cond_expressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#loop_expression}.
	 * @param ctx the parse tree
	 */
	void enterLoop_expression(CorundumParser.Loop_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#loop_expression}.
	 * @param ctx the parse tree
	 */
	void exitLoop_expression(CorundumParser.Loop_expressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#for_loop_list}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop_list(CorundumParser.For_loop_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#for_loop_list}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop_list(CorundumParser.For_loop_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#statement_body}.
	 * @param ctx the parse tree
	 */
	void enterStatement_body(CorundumParser.Statement_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#statement_body}.
	 * @param ctx the parse tree
	 */
	void exitStatement_body(CorundumParser.Statement_bodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#statement_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterStatement_expression_list(CorundumParser.Statement_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#statement_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitStatement_expression_list(CorundumParser.Statement_expression_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(CorundumParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(CorundumParser.AssignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#dynamic_assignment}.
	 * @param ctx the parse tree
	 */
	void enterDynamic_assignment(CorundumParser.Dynamic_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#dynamic_assignment}.
	 * @param ctx the parse tree
	 */
	void exitDynamic_assignment(CorundumParser.Dynamic_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#int_assignment}.
	 * @param ctx the parse tree
	 */
	void enterInt_assignment(CorundumParser.Int_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#int_assignment}.
	 * @param ctx the parse tree
	 */
	void exitInt_assignment(CorundumParser.Int_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#float_assignment}.
	 * @param ctx the parse tree
	 */
	void enterFloat_assignment(CorundumParser.Float_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#float_assignment}.
	 * @param ctx the parse tree
	 */
	void exitFloat_assignment(CorundumParser.Float_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#string_assignment}.
	 * @param ctx the parse tree
	 */
	void enterString_assignment(CorundumParser.String_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#string_assignment}.
	 * @param ctx the parse tree
	 */
	void exitString_assignment(CorundumParser.String_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#initial_array_assignment}.
	 * @param ctx the parse tree
	 */
	void enterInitial_array_assignment(CorundumParser.Initial_array_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#initial_array_assignment}.
	 * @param ctx the parse tree
	 */
	void exitInitial_array_assignment(CorundumParser.Initial_array_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#array_assignment}.
	 * @param ctx the parse tree
	 */
	void enterArray_assignment(CorundumParser.Array_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#array_assignment}.
	 * @param ctx the parse tree
	 */
	void exitArray_assignment(CorundumParser.Array_assignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#array_definition}.
	 * @param ctx the parse tree
	 */
	void enterArray_definition(CorundumParser.Array_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#array_definition}.
	 * @param ctx the parse tree
	 */
	void exitArray_definition(CorundumParser.Array_definitionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#array_definition_elements}.
	 * @param ctx the parse tree
	 */
	void enterArray_definition_elements(CorundumParser.Array_definition_elementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#array_definition_elements}.
	 * @param ctx the parse tree
	 */
	void exitArray_definition_elements(CorundumParser.Array_definition_elementsContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#array_selector}.
	 * @param ctx the parse tree
	 */
	void enterArray_selector(CorundumParser.Array_selectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#array_selector}.
	 * @param ctx the parse tree
	 */
	void exitArray_selector(CorundumParser.Array_selectorContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#dynamic_result}.
	 * @param ctx the parse tree
	 */
	void enterDynamic_result(CorundumParser.Dynamic_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#dynamic_result}.
	 * @param ctx the parse tree
	 */
	void exitDynamic_result(CorundumParser.Dynamic_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#dynamic_}.
	 * @param ctx the parse tree
	 */
	void enterDynamic_(CorundumParser.Dynamic_Context ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#dynamic_}.
	 * @param ctx the parse tree
	 */
	void exitDynamic_(CorundumParser.Dynamic_Context ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#int_result}.
	 * @param ctx the parse tree
	 */
	void enterInt_result(CorundumParser.Int_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#int_result}.
	 * @param ctx the parse tree
	 */
	void exitInt_result(CorundumParser.Int_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#float_result}.
	 * @param ctx the parse tree
	 */
	void enterFloat_result(CorundumParser.Float_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#float_result}.
	 * @param ctx the parse tree
	 */
	void exitFloat_result(CorundumParser.Float_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#string_result}.
	 * @param ctx the parse tree
	 */
	void enterString_result(CorundumParser.String_resultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#string_result}.
	 * @param ctx the parse tree
	 */
	void exitString_result(CorundumParser.String_resultContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#comparison_list}.
	 * @param ctx the parse tree
	 */
	void enterComparison_list(CorundumParser.Comparison_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#comparison_list}.
	 * @param ctx the parse tree
	 */
	void exitComparison_list(CorundumParser.Comparison_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(CorundumParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(CorundumParser.ComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#comp_var}.
	 * @param ctx the parse tree
	 */
	void enterComp_var(CorundumParser.Comp_varContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#comp_var}.
	 * @param ctx the parse tree
	 */
	void exitComp_var(CorundumParser.Comp_varContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(CorundumParser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(CorundumParser.LvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#rvalue}.
	 * @param ctx the parse tree
	 */
	void enterRvalue(CorundumParser.RvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#rvalue}.
	 * @param ctx the parse tree
	 */
	void exitRvalue(CorundumParser.RvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#break_expression}.
	 * @param ctx the parse tree
	 */
	void enterBreak_expression(CorundumParser.Break_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#break_expression}.
	 * @param ctx the parse tree
	 */
	void exitBreak_expression(CorundumParser.Break_expressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#literal_t}.
	 * @param ctx the parse tree
	 */
	void enterLiteral_t(CorundumParser.Literal_tContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#literal_t}.
	 * @param ctx the parse tree
	 */
	void exitLiteral_t(CorundumParser.Literal_tContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#float_t}.
	 * @param ctx the parse tree
	 */
	void enterFloat_t(CorundumParser.Float_tContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#float_t}.
	 * @param ctx the parse tree
	 */
	void exitFloat_t(CorundumParser.Float_tContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#int_t}.
	 * @param ctx the parse tree
	 */
	void enterInt_t(CorundumParser.Int_tContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#int_t}.
	 * @param ctx the parse tree
	 */
	void exitInt_t(CorundumParser.Int_tContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#bool_t}.
	 * @param ctx the parse tree
	 */
	void enterBool_t(CorundumParser.Bool_tContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#bool_t}.
	 * @param ctx the parse tree
	 */
	void exitBool_t(CorundumParser.Bool_tContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#nil_t}.
	 * @param ctx the parse tree
	 */
	void enterNil_t(CorundumParser.Nil_tContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#nil_t}.
	 * @param ctx the parse tree
	 */
	void exitNil_t(CorundumParser.Nil_tContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(CorundumParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(CorundumParser.Id_Context ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#id_global}.
	 * @param ctx the parse tree
	 */
	void enterId_global(CorundumParser.Id_globalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#id_global}.
	 * @param ctx the parse tree
	 */
	void exitId_global(CorundumParser.Id_globalContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#id_function}.
	 * @param ctx the parse tree
	 */
	void enterId_function(CorundumParser.Id_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#id_function}.
	 * @param ctx the parse tree
	 */
	void exitId_function(CorundumParser.Id_functionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#terminator}.
	 * @param ctx the parse tree
	 */
	void enterTerminator(CorundumParser.TerminatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#terminator}.
	 * @param ctx the parse tree
	 */
	void exitTerminator(CorundumParser.TerminatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#else_token}.
	 * @param ctx the parse tree
	 */
	void enterElse_token(CorundumParser.Else_tokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#else_token}.
	 * @param ctx the parse tree
	 */
	void exitElse_token(CorundumParser.Else_tokenContext ctx);

	/**
	 * Enter a parse tree produced by {@link CorundumParser#crlf}.
	 * @param ctx the parse tree
	 */
	void enterCrlf(CorundumParser.CrlfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CorundumParser#crlf}.
	 * @param ctx the parse tree
	 */
	void exitCrlf(CorundumParser.CrlfContext ctx);
}