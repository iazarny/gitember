// Generated from css3.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.css;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link css3Parser}.
 */
public interface css3Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link css3Parser#stylesheet}.
	 * @param ctx the parse tree
	 */
	void enterStylesheet(css3Parser.StylesheetContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#stylesheet}.
	 * @param ctx the parse tree
	 */
	void exitStylesheet(css3Parser.StylesheetContext ctx);

	/**
	 * Enter a parse tree produced by the {@code goodCharset}
	 * labeled alternative in {@link css3Parser#charset}.
	 * @param ctx the parse tree
	 */
	void enterGoodCharset(css3Parser.GoodCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code goodCharset}
	 * labeled alternative in {@link css3Parser#charset}.
	 * @param ctx the parse tree
	 */
	void exitGoodCharset(css3Parser.GoodCharsetContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badCharset}
	 * labeled alternative in {@link css3Parser#charset}.
	 * @param ctx the parse tree
	 */
	void enterBadCharset(css3Parser.BadCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badCharset}
	 * labeled alternative in {@link css3Parser#charset}.
	 * @param ctx the parse tree
	 */
	void exitBadCharset(css3Parser.BadCharsetContext ctx);

	/**
	 * Enter a parse tree produced by the {@code goodImport}
	 * labeled alternative in {@link css3Parser#imports}.
	 * @param ctx the parse tree
	 */
	void enterGoodImport(css3Parser.GoodImportContext ctx);
	/**
	 * Exit a parse tree produced by the {@code goodImport}
	 * labeled alternative in {@link css3Parser#imports}.
	 * @param ctx the parse tree
	 */
	void exitGoodImport(css3Parser.GoodImportContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badImport}
	 * labeled alternative in {@link css3Parser#imports}.
	 * @param ctx the parse tree
	 */
	void enterBadImport(css3Parser.BadImportContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badImport}
	 * labeled alternative in {@link css3Parser#imports}.
	 * @param ctx the parse tree
	 */
	void exitBadImport(css3Parser.BadImportContext ctx);

	/**
	 * Enter a parse tree produced by the {@code goodNamespace}
	 * labeled alternative in {@link css3Parser#namespace_}.
	 * @param ctx the parse tree
	 */
	void enterGoodNamespace(css3Parser.GoodNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code goodNamespace}
	 * labeled alternative in {@link css3Parser#namespace_}.
	 * @param ctx the parse tree
	 */
	void exitGoodNamespace(css3Parser.GoodNamespaceContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badNamespace}
	 * labeled alternative in {@link css3Parser#namespace_}.
	 * @param ctx the parse tree
	 */
	void enterBadNamespace(css3Parser.BadNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badNamespace}
	 * labeled alternative in {@link css3Parser#namespace_}.
	 * @param ctx the parse tree
	 */
	void exitBadNamespace(css3Parser.BadNamespaceContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#namespacePrefix}.
	 * @param ctx the parse tree
	 */
	void enterNamespacePrefix(css3Parser.NamespacePrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#namespacePrefix}.
	 * @param ctx the parse tree
	 */
	void exitNamespacePrefix(css3Parser.NamespacePrefixContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#media}.
	 * @param ctx the parse tree
	 */
	void enterMedia(css3Parser.MediaContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#media}.
	 * @param ctx the parse tree
	 */
	void exitMedia(css3Parser.MediaContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#mediaQueryList}.
	 * @param ctx the parse tree
	 */
	void enterMediaQueryList(css3Parser.MediaQueryListContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#mediaQueryList}.
	 * @param ctx the parse tree
	 */
	void exitMediaQueryList(css3Parser.MediaQueryListContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#mediaQuery}.
	 * @param ctx the parse tree
	 */
	void enterMediaQuery(css3Parser.MediaQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#mediaQuery}.
	 * @param ctx the parse tree
	 */
	void exitMediaQuery(css3Parser.MediaQueryContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#mediaType}.
	 * @param ctx the parse tree
	 */
	void enterMediaType(css3Parser.MediaTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#mediaType}.
	 * @param ctx the parse tree
	 */
	void exitMediaType(css3Parser.MediaTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#mediaExpression}.
	 * @param ctx the parse tree
	 */
	void enterMediaExpression(css3Parser.MediaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#mediaExpression}.
	 * @param ctx the parse tree
	 */
	void exitMediaExpression(css3Parser.MediaExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#mediaFeature}.
	 * @param ctx the parse tree
	 */
	void enterMediaFeature(css3Parser.MediaFeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#mediaFeature}.
	 * @param ctx the parse tree
	 */
	void exitMediaFeature(css3Parser.MediaFeatureContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#page}.
	 * @param ctx the parse tree
	 */
	void enterPage(css3Parser.PageContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#page}.
	 * @param ctx the parse tree
	 */
	void exitPage(css3Parser.PageContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#pseudoPage}.
	 * @param ctx the parse tree
	 */
	void enterPseudoPage(css3Parser.PseudoPageContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#pseudoPage}.
	 * @param ctx the parse tree
	 */
	void exitPseudoPage(css3Parser.PseudoPageContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#selectorGroup}.
	 * @param ctx the parse tree
	 */
	void enterSelectorGroup(css3Parser.SelectorGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#selectorGroup}.
	 * @param ctx the parse tree
	 */
	void exitSelectorGroup(css3Parser.SelectorGroupContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#selector}.
	 * @param ctx the parse tree
	 */
	void enterSelector(css3Parser.SelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#selector}.
	 * @param ctx the parse tree
	 */
	void exitSelector(css3Parser.SelectorContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#combinator}.
	 * @param ctx the parse tree
	 */
	void enterCombinator(css3Parser.CombinatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#combinator}.
	 * @param ctx the parse tree
	 */
	void exitCombinator(css3Parser.CombinatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#simpleSelectorSequence}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelectorSequence(css3Parser.SimpleSelectorSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#simpleSelectorSequence}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelectorSequence(css3Parser.SimpleSelectorSequenceContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#typeSelector}.
	 * @param ctx the parse tree
	 */
	void enterTypeSelector(css3Parser.TypeSelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#typeSelector}.
	 * @param ctx the parse tree
	 */
	void exitTypeSelector(css3Parser.TypeSelectorContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#typeNamespacePrefix}.
	 * @param ctx the parse tree
	 */
	void enterTypeNamespacePrefix(css3Parser.TypeNamespacePrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#typeNamespacePrefix}.
	 * @param ctx the parse tree
	 */
	void exitTypeNamespacePrefix(css3Parser.TypeNamespacePrefixContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#elementName}.
	 * @param ctx the parse tree
	 */
	void enterElementName(css3Parser.ElementNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#elementName}.
	 * @param ctx the parse tree
	 */
	void exitElementName(css3Parser.ElementNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#universal}.
	 * @param ctx the parse tree
	 */
	void enterUniversal(css3Parser.UniversalContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#universal}.
	 * @param ctx the parse tree
	 */
	void exitUniversal(css3Parser.UniversalContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#className}.
	 * @param ctx the parse tree
	 */
	void enterClassName(css3Parser.ClassNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#className}.
	 * @param ctx the parse tree
	 */
	void exitClassName(css3Parser.ClassNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#attrib}.
	 * @param ctx the parse tree
	 */
	void enterAttrib(css3Parser.AttribContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#attrib}.
	 * @param ctx the parse tree
	 */
	void exitAttrib(css3Parser.AttribContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#pseudo}.
	 * @param ctx the parse tree
	 */
	void enterPseudo(css3Parser.PseudoContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#pseudo}.
	 * @param ctx the parse tree
	 */
	void exitPseudo(css3Parser.PseudoContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#functionalPseudo}.
	 * @param ctx the parse tree
	 */
	void enterFunctionalPseudo(css3Parser.FunctionalPseudoContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#functionalPseudo}.
	 * @param ctx the parse tree
	 */
	void exitFunctionalPseudo(css3Parser.FunctionalPseudoContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(css3Parser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(css3Parser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(css3Parser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(css3Parser.NegationContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#negationArg}.
	 * @param ctx the parse tree
	 */
	void enterNegationArg(css3Parser.NegationArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#negationArg}.
	 * @param ctx the parse tree
	 */
	void exitNegationArg(css3Parser.NegationArgContext ctx);

	/**
	 * Enter a parse tree produced by the {@code goodOperator}
	 * labeled alternative in {@link css3Parser#operator_}.
	 * @param ctx the parse tree
	 */
	void enterGoodOperator(css3Parser.GoodOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code goodOperator}
	 * labeled alternative in {@link css3Parser#operator_}.
	 * @param ctx the parse tree
	 */
	void exitGoodOperator(css3Parser.GoodOperatorContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badOperator}
	 * labeled alternative in {@link css3Parser#operator_}.
	 * @param ctx the parse tree
	 */
	void enterBadOperator(css3Parser.BadOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badOperator}
	 * labeled alternative in {@link css3Parser#operator_}.
	 * @param ctx the parse tree
	 */
	void exitBadOperator(css3Parser.BadOperatorContext ctx);

	/**
	 * Enter a parse tree produced by the {@code goodProperty}
	 * labeled alternative in {@link css3Parser#property_}.
	 * @param ctx the parse tree
	 */
	void enterGoodProperty(css3Parser.GoodPropertyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code goodProperty}
	 * labeled alternative in {@link css3Parser#property_}.
	 * @param ctx the parse tree
	 */
	void exitGoodProperty(css3Parser.GoodPropertyContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badProperty}
	 * labeled alternative in {@link css3Parser#property_}.
	 * @param ctx the parse tree
	 */
	void enterBadProperty(css3Parser.BadPropertyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badProperty}
	 * labeled alternative in {@link css3Parser#property_}.
	 * @param ctx the parse tree
	 */
	void exitBadProperty(css3Parser.BadPropertyContext ctx);

	/**
	 * Enter a parse tree produced by the {@code knownRuleset}
	 * labeled alternative in {@link css3Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void enterKnownRuleset(css3Parser.KnownRulesetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code knownRuleset}
	 * labeled alternative in {@link css3Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void exitKnownRuleset(css3Parser.KnownRulesetContext ctx);

	/**
	 * Enter a parse tree produced by the {@code unknownRuleset}
	 * labeled alternative in {@link css3Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void enterUnknownRuleset(css3Parser.UnknownRulesetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unknownRuleset}
	 * labeled alternative in {@link css3Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void exitUnknownRuleset(css3Parser.UnknownRulesetContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#declarationList}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationList(css3Parser.DeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#declarationList}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationList(css3Parser.DeclarationListContext ctx);

	/**
	 * Enter a parse tree produced by the {@code knownDeclaration}
	 * labeled alternative in {@link css3Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterKnownDeclaration(css3Parser.KnownDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code knownDeclaration}
	 * labeled alternative in {@link css3Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitKnownDeclaration(css3Parser.KnownDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by the {@code unknownDeclaration}
	 * labeled alternative in {@link css3Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterUnknownDeclaration(css3Parser.UnknownDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unknownDeclaration}
	 * labeled alternative in {@link css3Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitUnknownDeclaration(css3Parser.UnknownDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#prio}.
	 * @param ctx the parse tree
	 */
	void enterPrio(css3Parser.PrioContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#prio}.
	 * @param ctx the parse tree
	 */
	void exitPrio(css3Parser.PrioContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(css3Parser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(css3Parser.ValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(css3Parser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(css3Parser.ExprContext ctx);

	/**
	 * Enter a parse tree produced by the {@code knownTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void enterKnownTerm(css3Parser.KnownTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code knownTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void exitKnownTerm(css3Parser.KnownTermContext ctx);

	/**
	 * Enter a parse tree produced by the {@code unknownTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void enterUnknownTerm(css3Parser.UnknownTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unknownTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void exitUnknownTerm(css3Parser.UnknownTermContext ctx);

	/**
	 * Enter a parse tree produced by the {@code badTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void enterBadTerm(css3Parser.BadTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code badTerm}
	 * labeled alternative in {@link css3Parser#term}.
	 * @param ctx the parse tree
	 */
	void exitBadTerm(css3Parser.BadTermContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#function_}.
	 * @param ctx the parse tree
	 */
	void enterFunction_(css3Parser.Function_Context ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#function_}.
	 * @param ctx the parse tree
	 */
	void exitFunction_(css3Parser.Function_Context ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#dxImageTransform}.
	 * @param ctx the parse tree
	 */
	void enterDxImageTransform(css3Parser.DxImageTransformContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#dxImageTransform}.
	 * @param ctx the parse tree
	 */
	void exitDxImageTransform(css3Parser.DxImageTransformContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#hexcolor}.
	 * @param ctx the parse tree
	 */
	void enterHexcolor(css3Parser.HexcolorContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#hexcolor}.
	 * @param ctx the parse tree
	 */
	void exitHexcolor(css3Parser.HexcolorContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(css3Parser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(css3Parser.NumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#percentage}.
	 * @param ctx the parse tree
	 */
	void enterPercentage(css3Parser.PercentageContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#percentage}.
	 * @param ctx the parse tree
	 */
	void exitPercentage(css3Parser.PercentageContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#dimension}.
	 * @param ctx the parse tree
	 */
	void enterDimension(css3Parser.DimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#dimension}.
	 * @param ctx the parse tree
	 */
	void exitDimension(css3Parser.DimensionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#unknownDimension}.
	 * @param ctx the parse tree
	 */
	void enterUnknownDimension(css3Parser.UnknownDimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#unknownDimension}.
	 * @param ctx the parse tree
	 */
	void exitUnknownDimension(css3Parser.UnknownDimensionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#any_}.
	 * @param ctx the parse tree
	 */
	void enterAny_(css3Parser.Any_Context ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#any_}.
	 * @param ctx the parse tree
	 */
	void exitAny_(css3Parser.Any_Context ctx);

	/**
	 * Enter a parse tree produced by the {@code unknownAtRule}
	 * labeled alternative in {@link css3Parser#atRule}.
	 * @param ctx the parse tree
	 */
	void enterUnknownAtRule(css3Parser.UnknownAtRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unknownAtRule}
	 * labeled alternative in {@link css3Parser#atRule}.
	 * @param ctx the parse tree
	 */
	void exitUnknownAtRule(css3Parser.UnknownAtRuleContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#atKeyword}.
	 * @param ctx the parse tree
	 */
	void enterAtKeyword(css3Parser.AtKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#atKeyword}.
	 * @param ctx the parse tree
	 */
	void exitAtKeyword(css3Parser.AtKeywordContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#unused}.
	 * @param ctx the parse tree
	 */
	void enterUnused(css3Parser.UnusedContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#unused}.
	 * @param ctx the parse tree
	 */
	void exitUnused(css3Parser.UnusedContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(css3Parser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(css3Parser.BlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#nestedStatement}.
	 * @param ctx the parse tree
	 */
	void enterNestedStatement(css3Parser.NestedStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#nestedStatement}.
	 * @param ctx the parse tree
	 */
	void exitNestedStatement(css3Parser.NestedStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#groupRuleBody}.
	 * @param ctx the parse tree
	 */
	void enterGroupRuleBody(css3Parser.GroupRuleBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#groupRuleBody}.
	 * @param ctx the parse tree
	 */
	void exitGroupRuleBody(css3Parser.GroupRuleBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsRule}.
	 * @param ctx the parse tree
	 */
	void enterSupportsRule(css3Parser.SupportsRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsRule}.
	 * @param ctx the parse tree
	 */
	void exitSupportsRule(css3Parser.SupportsRuleContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsCondition}.
	 * @param ctx the parse tree
	 */
	void enterSupportsCondition(css3Parser.SupportsConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsCondition}.
	 * @param ctx the parse tree
	 */
	void exitSupportsCondition(css3Parser.SupportsConditionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsConditionInParens}.
	 * @param ctx the parse tree
	 */
	void enterSupportsConditionInParens(css3Parser.SupportsConditionInParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsConditionInParens}.
	 * @param ctx the parse tree
	 */
	void exitSupportsConditionInParens(css3Parser.SupportsConditionInParensContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsNegation}.
	 * @param ctx the parse tree
	 */
	void enterSupportsNegation(css3Parser.SupportsNegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsNegation}.
	 * @param ctx the parse tree
	 */
	void exitSupportsNegation(css3Parser.SupportsNegationContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsConjunction}.
	 * @param ctx the parse tree
	 */
	void enterSupportsConjunction(css3Parser.SupportsConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsConjunction}.
	 * @param ctx the parse tree
	 */
	void exitSupportsConjunction(css3Parser.SupportsConjunctionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsDisjunction}.
	 * @param ctx the parse tree
	 */
	void enterSupportsDisjunction(css3Parser.SupportsDisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsDisjunction}.
	 * @param ctx the parse tree
	 */
	void exitSupportsDisjunction(css3Parser.SupportsDisjunctionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#supportsDeclarationCondition}.
	 * @param ctx the parse tree
	 */
	void enterSupportsDeclarationCondition(css3Parser.SupportsDeclarationConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#supportsDeclarationCondition}.
	 * @param ctx the parse tree
	 */
	void exitSupportsDeclarationCondition(css3Parser.SupportsDeclarationConditionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#generalEnclosed}.
	 * @param ctx the parse tree
	 */
	void enterGeneralEnclosed(css3Parser.GeneralEnclosedContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#generalEnclosed}.
	 * @param ctx the parse tree
	 */
	void exitGeneralEnclosed(css3Parser.GeneralEnclosedContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#var_}.
	 * @param ctx the parse tree
	 */
	void enterVar_(css3Parser.Var_Context ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#var_}.
	 * @param ctx the parse tree
	 */
	void exitVar_(css3Parser.Var_Context ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#calc}.
	 * @param ctx the parse tree
	 */
	void enterCalc(css3Parser.CalcContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#calc}.
	 * @param ctx the parse tree
	 */
	void exitCalc(css3Parser.CalcContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#calcSum}.
	 * @param ctx the parse tree
	 */
	void enterCalcSum(css3Parser.CalcSumContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#calcSum}.
	 * @param ctx the parse tree
	 */
	void exitCalcSum(css3Parser.CalcSumContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#calcProduct}.
	 * @param ctx the parse tree
	 */
	void enterCalcProduct(css3Parser.CalcProductContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#calcProduct}.
	 * @param ctx the parse tree
	 */
	void exitCalcProduct(css3Parser.CalcProductContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#calcValue}.
	 * @param ctx the parse tree
	 */
	void enterCalcValue(css3Parser.CalcValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#calcValue}.
	 * @param ctx the parse tree
	 */
	void exitCalcValue(css3Parser.CalcValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#fontFaceRule}.
	 * @param ctx the parse tree
	 */
	void enterFontFaceRule(css3Parser.FontFaceRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#fontFaceRule}.
	 * @param ctx the parse tree
	 */
	void exitFontFaceRule(css3Parser.FontFaceRuleContext ctx);

	/**
	 * Enter a parse tree produced by the {@code knownFontFaceDeclaration}
	 * labeled alternative in {@link css3Parser#fontFaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterKnownFontFaceDeclaration(css3Parser.KnownFontFaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code knownFontFaceDeclaration}
	 * labeled alternative in {@link css3Parser#fontFaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitKnownFontFaceDeclaration(css3Parser.KnownFontFaceDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by the {@code unknownFontFaceDeclaration}
	 * labeled alternative in {@link css3Parser#fontFaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterUnknownFontFaceDeclaration(css3Parser.UnknownFontFaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unknownFontFaceDeclaration}
	 * labeled alternative in {@link css3Parser#fontFaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitUnknownFontFaceDeclaration(css3Parser.UnknownFontFaceDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#keyframesRule}.
	 * @param ctx the parse tree
	 */
	void enterKeyframesRule(css3Parser.KeyframesRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#keyframesRule}.
	 * @param ctx the parse tree
	 */
	void exitKeyframesRule(css3Parser.KeyframesRuleContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#keyframesBlocks}.
	 * @param ctx the parse tree
	 */
	void enterKeyframesBlocks(css3Parser.KeyframesBlocksContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#keyframesBlocks}.
	 * @param ctx the parse tree
	 */
	void exitKeyframesBlocks(css3Parser.KeyframesBlocksContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#keyframeSelector}.
	 * @param ctx the parse tree
	 */
	void enterKeyframeSelector(css3Parser.KeyframeSelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#keyframeSelector}.
	 * @param ctx the parse tree
	 */
	void exitKeyframeSelector(css3Parser.KeyframeSelectorContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#viewport}.
	 * @param ctx the parse tree
	 */
	void enterViewport(css3Parser.ViewportContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#viewport}.
	 * @param ctx the parse tree
	 */
	void exitViewport(css3Parser.ViewportContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#counterStyle}.
	 * @param ctx the parse tree
	 */
	void enterCounterStyle(css3Parser.CounterStyleContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#counterStyle}.
	 * @param ctx the parse tree
	 */
	void exitCounterStyle(css3Parser.CounterStyleContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#fontFeatureValuesRule}.
	 * @param ctx the parse tree
	 */
	void enterFontFeatureValuesRule(css3Parser.FontFeatureValuesRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#fontFeatureValuesRule}.
	 * @param ctx the parse tree
	 */
	void exitFontFeatureValuesRule(css3Parser.FontFeatureValuesRuleContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#fontFamilyNameList}.
	 * @param ctx the parse tree
	 */
	void enterFontFamilyNameList(css3Parser.FontFamilyNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#fontFamilyNameList}.
	 * @param ctx the parse tree
	 */
	void exitFontFamilyNameList(css3Parser.FontFamilyNameListContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#fontFamilyName}.
	 * @param ctx the parse tree
	 */
	void enterFontFamilyName(css3Parser.FontFamilyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#fontFamilyName}.
	 * @param ctx the parse tree
	 */
	void exitFontFamilyName(css3Parser.FontFamilyNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#featureValueBlock}.
	 * @param ctx the parse tree
	 */
	void enterFeatureValueBlock(css3Parser.FeatureValueBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#featureValueBlock}.
	 * @param ctx the parse tree
	 */
	void exitFeatureValueBlock(css3Parser.FeatureValueBlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#featureType}.
	 * @param ctx the parse tree
	 */
	void enterFeatureType(css3Parser.FeatureTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#featureType}.
	 * @param ctx the parse tree
	 */
	void exitFeatureType(css3Parser.FeatureTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#featureValueDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFeatureValueDefinition(css3Parser.FeatureValueDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#featureValueDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFeatureValueDefinition(css3Parser.FeatureValueDefinitionContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#ident}.
	 * @param ctx the parse tree
	 */
	void enterIdent(css3Parser.IdentContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#ident}.
	 * @param ctx the parse tree
	 */
	void exitIdent(css3Parser.IdentContext ctx);

	/**
	 * Enter a parse tree produced by {@link css3Parser#ws}.
	 * @param ctx the parse tree
	 */
	void enterWs(css3Parser.WsContext ctx);
	/**
	 * Exit a parse tree produced by {@link css3Parser#ws}.
	 * @param ctx the parse tree
	 */
	void exitWs(css3Parser.WsContext ctx);
}