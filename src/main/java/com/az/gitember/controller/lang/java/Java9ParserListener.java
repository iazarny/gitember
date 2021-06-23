// Generated from Java9Parser.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.java;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Java9Parser}.
 */
public interface Java9ParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Java9Parser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(Java9Parser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(Java9Parser.LiteralContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(Java9Parser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(Java9Parser.PrimitiveTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#numericType}.
	 * @param ctx the parse tree
	 */
	void enterNumericType(Java9Parser.NumericTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#numericType}.
	 * @param ctx the parse tree
	 */
	void exitNumericType(Java9Parser.NumericTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#integralType}.
	 * @param ctx the parse tree
	 */
	void enterIntegralType(Java9Parser.IntegralTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#integralType}.
	 * @param ctx the parse tree
	 */
	void exitIntegralType(Java9Parser.IntegralTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#floatingPointType}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPointType(Java9Parser.FloatingPointTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#floatingPointType}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPointType(Java9Parser.FloatingPointTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#referenceType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceType(Java9Parser.ReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#referenceType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceType(Java9Parser.ReferenceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceType(Java9Parser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceType(Java9Parser.ClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classType}.
	 * @param ctx the parse tree
	 */
	void enterClassType(Java9Parser.ClassTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classType}.
	 * @param ctx the parse tree
	 */
	void exitClassType(Java9Parser.ClassTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassType_lf_classOrInterfaceType(Java9Parser.ClassType_lf_classOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassType_lf_classOrInterfaceType(Java9Parser.ClassType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassType_lfno_classOrInterfaceType(Java9Parser.ClassType_lfno_classOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassType_lfno_classOrInterfaceType(Java9Parser.ClassType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceType}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceType(Java9Parser.InterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceType}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceType(Java9Parser.InterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceType_lf_classOrInterfaceType(Java9Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceType_lf_classOrInterfaceType(Java9Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceType_lfno_classOrInterfaceType(Java9Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceType_lfno_classOrInterfaceType(Java9Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeVariable}.
	 * @param ctx the parse tree
	 */
	void enterTypeVariable(Java9Parser.TypeVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeVariable}.
	 * @param ctx the parse tree
	 */
	void exitTypeVariable(Java9Parser.TypeVariableContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(Java9Parser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(Java9Parser.ArrayTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#dims}.
	 * @param ctx the parse tree
	 */
	void enterDims(Java9Parser.DimsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#dims}.
	 * @param ctx the parse tree
	 */
	void exitDims(Java9Parser.DimsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameter(Java9Parser.TypeParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameter(Java9Parser.TypeParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeParameterModifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameterModifier(Java9Parser.TypeParameterModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeParameterModifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameterModifier(Java9Parser.TypeParameterModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeBound}.
	 * @param ctx the parse tree
	 */
	void enterTypeBound(Java9Parser.TypeBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeBound}.
	 * @param ctx the parse tree
	 */
	void exitTypeBound(Java9Parser.TypeBoundContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#additionalBound}.
	 * @param ctx the parse tree
	 */
	void enterAdditionalBound(Java9Parser.AdditionalBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#additionalBound}.
	 * @param ctx the parse tree
	 */
	void exitAdditionalBound(Java9Parser.AdditionalBoundContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(Java9Parser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(Java9Parser.TypeArgumentsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentList(Java9Parser.TypeArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentList(Java9Parser.TypeArgumentListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeArgument}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgument(Java9Parser.TypeArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeArgument}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgument(Java9Parser.TypeArgumentContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#wildcard}.
	 * @param ctx the parse tree
	 */
	void enterWildcard(Java9Parser.WildcardContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#wildcard}.
	 * @param ctx the parse tree
	 */
	void exitWildcard(Java9Parser.WildcardContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#wildcardBounds}.
	 * @param ctx the parse tree
	 */
	void enterWildcardBounds(Java9Parser.WildcardBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#wildcardBounds}.
	 * @param ctx the parse tree
	 */
	void exitWildcardBounds(Java9Parser.WildcardBoundsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#moduleName}.
	 * @param ctx the parse tree
	 */
	void enterModuleName(Java9Parser.ModuleNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#moduleName}.
	 * @param ctx the parse tree
	 */
	void exitModuleName(Java9Parser.ModuleNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#packageName}.
	 * @param ctx the parse tree
	 */
	void enterPackageName(Java9Parser.PackageNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#packageName}.
	 * @param ctx the parse tree
	 */
	void exitPackageName(Java9Parser.PackageNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(Java9Parser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(Java9Parser.TypeNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#packageOrTypeName}.
	 * @param ctx the parse tree
	 */
	void enterPackageOrTypeName(Java9Parser.PackageOrTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#packageOrTypeName}.
	 * @param ctx the parse tree
	 */
	void exitPackageOrTypeName(Java9Parser.PackageOrTypeNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#expressionName}.
	 * @param ctx the parse tree
	 */
	void enterExpressionName(Java9Parser.ExpressionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#expressionName}.
	 * @param ctx the parse tree
	 */
	void exitExpressionName(Java9Parser.ExpressionNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodName}.
	 * @param ctx the parse tree
	 */
	void enterMethodName(Java9Parser.MethodNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodName}.
	 * @param ctx the parse tree
	 */
	void exitMethodName(Java9Parser.MethodNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#ambiguousName}.
	 * @param ctx the parse tree
	 */
	void enterAmbiguousName(Java9Parser.AmbiguousNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#ambiguousName}.
	 * @param ctx the parse tree
	 */
	void exitAmbiguousName(Java9Parser.AmbiguousNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(Java9Parser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(Java9Parser.CompilationUnitContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#ordinaryCompilation}.
	 * @param ctx the parse tree
	 */
	void enterOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#ordinaryCompilation}.
	 * @param ctx the parse tree
	 */
	void exitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#modularCompilation}.
	 * @param ctx the parse tree
	 */
	void enterModularCompilation(Java9Parser.ModularCompilationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#modularCompilation}.
	 * @param ctx the parse tree
	 */
	void exitModularCompilation(Java9Parser.ModularCompilationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(Java9Parser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#packageModifier}.
	 * @param ctx the parse tree
	 */
	void enterPackageModifier(Java9Parser.PackageModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#packageModifier}.
	 * @param ctx the parse tree
	 */
	void exitPackageModifier(Java9Parser.PackageModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(Java9Parser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(Java9Parser.ImportDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#singleTypeImportDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterSingleTypeImportDeclaration(Java9Parser.SingleTypeImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#singleTypeImportDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitSingleTypeImportDeclaration(Java9Parser.SingleTypeImportDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeImportOnDemandDeclaration(Java9Parser.TypeImportOnDemandDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeImportOnDemandDeclaration(Java9Parser.TypeImportOnDemandDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#singleStaticImportDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterSingleStaticImportDeclaration(Java9Parser.SingleStaticImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#singleStaticImportDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitSingleStaticImportDeclaration(Java9Parser.SingleStaticImportDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#staticImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStaticImportOnDemandDeclaration(Java9Parser.StaticImportOnDemandDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#staticImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStaticImportOnDemandDeclaration(Java9Parser.StaticImportOnDemandDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(Java9Parser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(Java9Parser.TypeDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#moduleDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterModuleDeclaration(Java9Parser.ModuleDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#moduleDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitModuleDeclaration(Java9Parser.ModuleDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#moduleDirective}.
	 * @param ctx the parse tree
	 */
	void enterModuleDirective(Java9Parser.ModuleDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#moduleDirective}.
	 * @param ctx the parse tree
	 */
	void exitModuleDirective(Java9Parser.ModuleDirectiveContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#requiresModifier}.
	 * @param ctx the parse tree
	 */
	void enterRequiresModifier(Java9Parser.RequiresModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#requiresModifier}.
	 * @param ctx the parse tree
	 */
	void exitRequiresModifier(Java9Parser.RequiresModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(Java9Parser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(Java9Parser.ClassDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#normalClassDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#normalClassDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classModifier}.
	 * @param ctx the parse tree
	 */
	void enterClassModifier(Java9Parser.ClassModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classModifier}.
	 * @param ctx the parse tree
	 */
	void exitClassModifier(Java9Parser.ClassModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameters(Java9Parser.TypeParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameters(Java9Parser.TypeParametersContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameterList(Java9Parser.TypeParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameterList(Java9Parser.TypeParameterListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#superclass}.
	 * @param ctx the parse tree
	 */
	void enterSuperclass(Java9Parser.SuperclassContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#superclass}.
	 * @param ctx the parse tree
	 */
	void exitSuperclass(Java9Parser.SuperclassContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#superinterfaces}.
	 * @param ctx the parse tree
	 */
	void enterSuperinterfaces(Java9Parser.SuperinterfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#superinterfaces}.
	 * @param ctx the parse tree
	 */
	void exitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceTypeList}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceTypeList(Java9Parser.InterfaceTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceTypeList}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceTypeList(Java9Parser.InterfaceTypeListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classBody}.
	 * @param ctx the parse tree
	 */
	void enterClassBody(Java9Parser.ClassBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classBody}.
	 * @param ctx the parse tree
	 */
	void exitClassBody(Java9Parser.ClassBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassBodyDeclaration(Java9Parser.ClassBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassBodyDeclaration(Java9Parser.ClassBodyDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassMemberDeclaration(Java9Parser.ClassMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassMemberDeclaration(Java9Parser.ClassMemberDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFieldDeclaration(Java9Parser.FieldDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void enterFieldModifier(Java9Parser.FieldModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void exitFieldModifier(Java9Parser.FieldModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaratorList(Java9Parser.VariableDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaratorList(Java9Parser.VariableDeclaratorListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarator(Java9Parser.VariableDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarator(Java9Parser.VariableDeclaratorContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableDeclaratorId}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaratorId(Java9Parser.VariableDeclaratorIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableDeclaratorId}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaratorId(Java9Parser.VariableDeclaratorIdContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializer(Java9Parser.VariableInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializer(Java9Parser.VariableInitializerContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannType}.
	 * @param ctx the parse tree
	 */
	void enterUnannType(Java9Parser.UnannTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannType}.
	 * @param ctx the parse tree
	 */
	void exitUnannType(Java9Parser.UnannTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannPrimitiveType}.
	 * @param ctx the parse tree
	 */
	void enterUnannPrimitiveType(Java9Parser.UnannPrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannPrimitiveType}.
	 * @param ctx the parse tree
	 */
	void exitUnannPrimitiveType(Java9Parser.UnannPrimitiveTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannReferenceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannReferenceType(Java9Parser.UnannReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannReferenceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannReferenceType(Java9Parser.UnannReferenceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannClassOrInterfaceType(Java9Parser.UnannClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannClassOrInterfaceType(Java9Parser.UnannClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannClassType}.
	 * @param ctx the parse tree
	 */
	void enterUnannClassType(Java9Parser.UnannClassTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannClassType}.
	 * @param ctx the parse tree
	 */
	void exitUnannClassType(Java9Parser.UnannClassTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannClassType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannClassType_lf_unannClassOrInterfaceType(Java9Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannClassType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannClassType_lf_unannClassOrInterfaceType(Java9Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannClassType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannClassType_lfno_unannClassOrInterfaceType(Java9Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannClassType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannClassType_lfno_unannClassOrInterfaceType(Java9Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannInterfaceType(Java9Parser.UnannInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannInterfaceType(Java9Parser.UnannInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannInterfaceType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannInterfaceType_lf_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannInterfaceType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannInterfaceType_lf_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannInterfaceType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterUnannInterfaceType_lfno_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannInterfaceType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitUnannInterfaceType_lfno_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannTypeVariable}.
	 * @param ctx the parse tree
	 */
	void enterUnannTypeVariable(Java9Parser.UnannTypeVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannTypeVariable}.
	 * @param ctx the parse tree
	 */
	void exitUnannTypeVariable(Java9Parser.UnannTypeVariableContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unannArrayType}.
	 * @param ctx the parse tree
	 */
	void enterUnannArrayType(Java9Parser.UnannArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unannArrayType}.
	 * @param ctx the parse tree
	 */
	void exitUnannArrayType(Java9Parser.UnannArrayTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(Java9Parser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(Java9Parser.MethodDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodModifier}.
	 * @param ctx the parse tree
	 */
	void enterMethodModifier(Java9Parser.MethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodModifier}.
	 * @param ctx the parse tree
	 */
	void exitMethodModifier(Java9Parser.MethodModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodHeader}.
	 * @param ctx the parse tree
	 */
	void enterMethodHeader(Java9Parser.MethodHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodHeader}.
	 * @param ctx the parse tree
	 */
	void exitMethodHeader(Java9Parser.MethodHeaderContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#result}.
	 * @param ctx the parse tree
	 */
	void enterResult(Java9Parser.ResultContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#result}.
	 * @param ctx the parse tree
	 */
	void exitResult(Java9Parser.ResultContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(Java9Parser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(Java9Parser.FormalParameterListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(Java9Parser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(Java9Parser.FormalParametersContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(Java9Parser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(Java9Parser.FormalParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void enterVariableModifier(Java9Parser.VariableModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void exitVariableModifier(Java9Parser.VariableModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#lastFormalParameter}.
	 * @param ctx the parse tree
	 */
	void enterLastFormalParameter(Java9Parser.LastFormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#lastFormalParameter}.
	 * @param ctx the parse tree
	 */
	void exitLastFormalParameter(Java9Parser.LastFormalParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#receiverParameter}.
	 * @param ctx the parse tree
	 */
	void enterReceiverParameter(Java9Parser.ReceiverParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#receiverParameter}.
	 * @param ctx the parse tree
	 */
	void exitReceiverParameter(Java9Parser.ReceiverParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#throws_}.
	 * @param ctx the parse tree
	 */
	void enterThrows_(Java9Parser.Throws_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#throws_}.
	 * @param ctx the parse tree
	 */
	void exitThrows_(Java9Parser.Throws_Context ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#exceptionTypeList}.
	 * @param ctx the parse tree
	 */
	void enterExceptionTypeList(Java9Parser.ExceptionTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#exceptionTypeList}.
	 * @param ctx the parse tree
	 */
	void exitExceptionTypeList(Java9Parser.ExceptionTypeListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#exceptionType}.
	 * @param ctx the parse tree
	 */
	void enterExceptionType(Java9Parser.ExceptionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#exceptionType}.
	 * @param ctx the parse tree
	 */
	void exitExceptionType(Java9Parser.ExceptionTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodBody}.
	 * @param ctx the parse tree
	 */
	void enterMethodBody(Java9Parser.MethodBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodBody}.
	 * @param ctx the parse tree
	 */
	void exitMethodBody(Java9Parser.MethodBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#instanceInitializer}.
	 * @param ctx the parse tree
	 */
	void enterInstanceInitializer(Java9Parser.InstanceInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#instanceInitializer}.
	 * @param ctx the parse tree
	 */
	void exitInstanceInitializer(Java9Parser.InstanceInitializerContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#staticInitializer}.
	 * @param ctx the parse tree
	 */
	void enterStaticInitializer(Java9Parser.StaticInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#staticInitializer}.
	 * @param ctx the parse tree
	 */
	void exitStaticInitializer(Java9Parser.StaticInitializerContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(Java9Parser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(Java9Parser.ConstructorDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constructorModifier}.
	 * @param ctx the parse tree
	 */
	void enterConstructorModifier(Java9Parser.ConstructorModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constructorModifier}.
	 * @param ctx the parse tree
	 */
	void exitConstructorModifier(Java9Parser.ConstructorModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constructorDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclarator(Java9Parser.ConstructorDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constructorDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclarator(Java9Parser.ConstructorDeclaratorContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeName(Java9Parser.SimpleTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeName(Java9Parser.SimpleTypeNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constructorBody}.
	 * @param ctx the parse tree
	 */
	void enterConstructorBody(Java9Parser.ConstructorBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constructorBody}.
	 * @param ctx the parse tree
	 */
	void exitConstructorBody(Java9Parser.ConstructorBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 */
	void enterExplicitConstructorInvocation(Java9Parser.ExplicitConstructorInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 */
	void exitExplicitConstructorInvocation(Java9Parser.ExplicitConstructorInvocationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEnumDeclaration(Java9Parser.EnumDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEnumDeclaration(Java9Parser.EnumDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumBody}.
	 * @param ctx the parse tree
	 */
	void enterEnumBody(Java9Parser.EnumBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumBody}.
	 * @param ctx the parse tree
	 */
	void exitEnumBody(Java9Parser.EnumBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumConstantList}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstantList(Java9Parser.EnumConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumConstantList}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstantList(Java9Parser.EnumConstantListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstant(Java9Parser.EnumConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstant(Java9Parser.EnumConstantContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumConstantModifier}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstantModifier(Java9Parser.EnumConstantModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumConstantModifier}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstantModifier(Java9Parser.EnumConstantModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void enterEnumBodyDeclarations(Java9Parser.EnumBodyDeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void exitEnumBodyDeclarations(Java9Parser.EnumBodyDeclarationsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceDeclaration(Java9Parser.InterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceDeclaration(Java9Parser.InterfaceDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#normalInterfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterNormalInterfaceDeclaration(Java9Parser.NormalInterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#normalInterfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitNormalInterfaceDeclaration(Java9Parser.NormalInterfaceDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceModifier(Java9Parser.InterfaceModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceModifier(Java9Parser.InterfaceModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#extendsInterfaces}.
	 * @param ctx the parse tree
	 */
	void enterExtendsInterfaces(Java9Parser.ExtendsInterfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#extendsInterfaces}.
	 * @param ctx the parse tree
	 */
	void exitExtendsInterfaces(Java9Parser.ExtendsInterfacesContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBody(Java9Parser.InterfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBody(Java9Parser.InterfaceBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMemberDeclaration(Java9Parser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMemberDeclaration(Java9Parser.InterfaceMemberDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstantDeclaration(Java9Parser.ConstantDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstantDeclaration(Java9Parser.ConstantDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constantModifier}.
	 * @param ctx the parse tree
	 */
	void enterConstantModifier(Java9Parser.ConstantModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constantModifier}.
	 * @param ctx the parse tree
	 */
	void exitConstantModifier(Java9Parser.ConstantModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodDeclaration(Java9Parser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodDeclaration(Java9Parser.InterfaceMethodDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodModifier(Java9Parser.InterfaceMethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodModifier(Java9Parser.InterfaceMethodModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotationTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationTypeDeclaration(Java9Parser.AnnotationTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotationTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationTypeDeclaration(Java9Parser.AnnotationTypeDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotationTypeBody}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationTypeBody(Java9Parser.AnnotationTypeBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotationTypeBody}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationTypeBody(Java9Parser.AnnotationTypeBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotationTypeMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationTypeMemberDeclaration(Java9Parser.AnnotationTypeMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotationTypeMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationTypeMemberDeclaration(Java9Parser.AnnotationTypeMemberDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotationTypeElementDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationTypeElementDeclaration(Java9Parser.AnnotationTypeElementDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotationTypeElementDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationTypeElementDeclaration(Java9Parser.AnnotationTypeElementDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotationTypeElementModifier}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationTypeElementModifier(Java9Parser.AnnotationTypeElementModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotationTypeElementModifier}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationTypeElementModifier(Java9Parser.AnnotationTypeElementModifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void enterDefaultValue(Java9Parser.DefaultValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void exitDefaultValue(Java9Parser.DefaultValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(Java9Parser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(Java9Parser.AnnotationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#normalAnnotation}.
	 * @param ctx the parse tree
	 */
	void enterNormalAnnotation(Java9Parser.NormalAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#normalAnnotation}.
	 * @param ctx the parse tree
	 */
	void exitNormalAnnotation(Java9Parser.NormalAnnotationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#elementValuePairList}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePairList(Java9Parser.ElementValuePairListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#elementValuePairList}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePairList(Java9Parser.ElementValuePairListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePair(Java9Parser.ElementValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePair(Java9Parser.ElementValuePairContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#elementValue}.
	 * @param ctx the parse tree
	 */
	void enterElementValue(Java9Parser.ElementValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#elementValue}.
	 * @param ctx the parse tree
	 */
	void exitElementValue(Java9Parser.ElementValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterElementValueArrayInitializer(Java9Parser.ElementValueArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitElementValueArrayInitializer(Java9Parser.ElementValueArrayInitializerContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#elementValueList}.
	 * @param ctx the parse tree
	 */
	void enterElementValueList(Java9Parser.ElementValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#elementValueList}.
	 * @param ctx the parse tree
	 */
	void exitElementValueList(Java9Parser.ElementValueListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#markerAnnotation}.
	 * @param ctx the parse tree
	 */
	void enterMarkerAnnotation(Java9Parser.MarkerAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#markerAnnotation}.
	 * @param ctx the parse tree
	 */
	void exitMarkerAnnotation(Java9Parser.MarkerAnnotationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#singleElementAnnotation}.
	 * @param ctx the parse tree
	 */
	void enterSingleElementAnnotation(Java9Parser.SingleElementAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#singleElementAnnotation}.
	 * @param ctx the parse tree
	 */
	void exitSingleElementAnnotation(Java9Parser.SingleElementAnnotationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(Java9Parser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(Java9Parser.ArrayInitializerContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableInitializerList}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializerList(Java9Parser.VariableInitializerListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableInitializerList}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializerList(Java9Parser.VariableInitializerListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(Java9Parser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(Java9Parser.BlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#blockStatements}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatements(Java9Parser.BlockStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#blockStatements}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatements(Java9Parser.BlockStatementsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(Java9Parser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(Java9Parser.BlockStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#localVariableDeclarationStatement}.
	 * @param ctx the parse tree
	 */
	void enterLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#localVariableDeclarationStatement}.
	 * @param ctx the parse tree
	 */
	void exitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterLocalVariableDeclaration(Java9Parser.LocalVariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitLocalVariableDeclaration(Java9Parser.LocalVariableDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(Java9Parser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(Java9Parser.StatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#statementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterStatementNoShortIf(Java9Parser.StatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#statementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitStatementNoShortIf(Java9Parser.StatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 */
	void enterStatementWithoutTrailingSubstatement(Java9Parser.StatementWithoutTrailingSubstatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 */
	void exitStatementWithoutTrailingSubstatement(Java9Parser.StatementWithoutTrailingSubstatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(Java9Parser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(Java9Parser.EmptyStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatement(Java9Parser.LabeledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatement(Java9Parser.LabeledStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#labeledStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatementNoShortIf(Java9Parser.LabeledStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#labeledStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatementNoShortIf(Java9Parser.LabeledStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(Java9Parser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(Java9Parser.ExpressionStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void enterStatementExpression(Java9Parser.StatementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void exitStatementExpression(Java9Parser.StatementExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenStatement(Java9Parser.IfThenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenStatement(Java9Parser.IfThenStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenElseStatement(Java9Parser.IfThenElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenElseStatement(Java9Parser.IfThenElseStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterIfThenElseStatementNoShortIf(Java9Parser.IfThenElseStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitIfThenElseStatementNoShortIf(Java9Parser.IfThenElseStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#assertStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssertStatement(Java9Parser.AssertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#assertStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssertStatement(Java9Parser.AssertStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(Java9Parser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(Java9Parser.SwitchStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void enterSwitchBlock(Java9Parser.SwitchBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void exitSwitchBlock(Java9Parser.SwitchBlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 */
	void enterSwitchBlockStatementGroup(Java9Parser.SwitchBlockStatementGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 */
	void exitSwitchBlockStatementGroup(Java9Parser.SwitchBlockStatementGroupContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#switchLabels}.
	 * @param ctx the parse tree
	 */
	void enterSwitchLabels(Java9Parser.SwitchLabelsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#switchLabels}.
	 * @param ctx the parse tree
	 */
	void exitSwitchLabels(Java9Parser.SwitchLabelsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void enterSwitchLabel(Java9Parser.SwitchLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void exitSwitchLabel(Java9Parser.SwitchLabelContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enumConstantName}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstantName(Java9Parser.EnumConstantNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enumConstantName}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstantName(Java9Parser.EnumConstantNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(Java9Parser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(Java9Parser.WhileStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatementNoShortIf(Java9Parser.WhileStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatementNoShortIf(Java9Parser.WhileStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(Java9Parser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(Java9Parser.DoStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(Java9Parser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(Java9Parser.ForStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterForStatementNoShortIf(Java9Parser.ForStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitForStatementNoShortIf(Java9Parser.ForStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#basicForStatement}.
	 * @param ctx the parse tree
	 */
	void enterBasicForStatement(Java9Parser.BasicForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#basicForStatement}.
	 * @param ctx the parse tree
	 */
	void exitBasicForStatement(Java9Parser.BasicForStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#basicForStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterBasicForStatementNoShortIf(Java9Parser.BasicForStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#basicForStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitBasicForStatementNoShortIf(Java9Parser.BasicForStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#forInit}.
	 * @param ctx the parse tree
	 */
	void enterForInit(Java9Parser.ForInitContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#forInit}.
	 * @param ctx the parse tree
	 */
	void exitForInit(Java9Parser.ForInitContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void enterForUpdate(Java9Parser.ForUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void exitForUpdate(Java9Parser.ForUpdateContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#statementExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterStatementExpressionList(Java9Parser.StatementExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#statementExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitStatementExpressionList(Java9Parser.StatementExpressionListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enhancedForStatement}.
	 * @param ctx the parse tree
	 */
	void enterEnhancedForStatement(Java9Parser.EnhancedForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enhancedForStatement}.
	 * @param ctx the parse tree
	 */
	void exitEnhancedForStatement(Java9Parser.EnhancedForStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#enhancedForStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterEnhancedForStatementNoShortIf(Java9Parser.EnhancedForStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#enhancedForStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitEnhancedForStatementNoShortIf(Java9Parser.EnhancedForStatementNoShortIfContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStatement(Java9Parser.BreakStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStatement(Java9Parser.BreakStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStatement(Java9Parser.ContinueStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStatement(Java9Parser.ContinueStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(Java9Parser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(Java9Parser.ReturnStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void enterThrowStatement(Java9Parser.ThrowStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void exitThrowStatement(Java9Parser.ThrowStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#synchronizedStatement}.
	 * @param ctx the parse tree
	 */
	void enterSynchronizedStatement(Java9Parser.SynchronizedStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#synchronizedStatement}.
	 * @param ctx the parse tree
	 */
	void exitSynchronizedStatement(Java9Parser.SynchronizedStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#tryStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryStatement(Java9Parser.TryStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#tryStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryStatement(Java9Parser.TryStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#catches}.
	 * @param ctx the parse tree
	 */
	void enterCatches(Java9Parser.CatchesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#catches}.
	 * @param ctx the parse tree
	 */
	void exitCatches(Java9Parser.CatchesContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(Java9Parser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(Java9Parser.CatchClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#catchFormalParameter}.
	 * @param ctx the parse tree
	 */
	void enterCatchFormalParameter(Java9Parser.CatchFormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#catchFormalParameter}.
	 * @param ctx the parse tree
	 */
	void exitCatchFormalParameter(Java9Parser.CatchFormalParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#catchType}.
	 * @param ctx the parse tree
	 */
	void enterCatchType(Java9Parser.CatchTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#catchType}.
	 * @param ctx the parse tree
	 */
	void exitCatchType(Java9Parser.CatchTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#finally_}.
	 * @param ctx the parse tree
	 */
	void enterFinally_(Java9Parser.Finally_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#finally_}.
	 * @param ctx the parse tree
	 */
	void exitFinally_(Java9Parser.Finally_Context ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#tryWithResourcesStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryWithResourcesStatement(Java9Parser.TryWithResourcesStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#tryWithResourcesStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryWithResourcesStatement(Java9Parser.TryWithResourcesStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#resourceSpecification}.
	 * @param ctx the parse tree
	 */
	void enterResourceSpecification(Java9Parser.ResourceSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#resourceSpecification}.
	 * @param ctx the parse tree
	 */
	void exitResourceSpecification(Java9Parser.ResourceSpecificationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#resourceList}.
	 * @param ctx the parse tree
	 */
	void enterResourceList(Java9Parser.ResourceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#resourceList}.
	 * @param ctx the parse tree
	 */
	void exitResourceList(Java9Parser.ResourceListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#resource}.
	 * @param ctx the parse tree
	 */
	void enterResource(Java9Parser.ResourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#resource}.
	 * @param ctx the parse tree
	 */
	void exitResource(Java9Parser.ResourceContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#variableAccess}.
	 * @param ctx the parse tree
	 */
	void enterVariableAccess(Java9Parser.VariableAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#variableAccess}.
	 * @param ctx the parse tree
	 */
	void exitVariableAccess(Java9Parser.VariableAccessContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(Java9Parser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(Java9Parser.PrimaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray(Java9Parser.PrimaryNoNewArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray(Java9Parser.PrimaryNoNewArrayContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_arrayAccess}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lf_arrayAccess(Java9Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_arrayAccess}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lf_arrayAccess(Java9Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_arrayAccess}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lfno_arrayAccess(Java9Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_arrayAccess}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lfno_arrayAccess(Java9Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classLiteral}.
	 * @param ctx the parse tree
	 */
	void enterClassLiteral(Java9Parser.ClassLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classLiteral}.
	 * @param ctx the parse tree
	 */
	void exitClassLiteral(Java9Parser.ClassLiteralContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 */
	void enterClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 */
	void exitClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterClassInstanceCreationExpression_lf_primary(Java9Parser.ClassInstanceCreationExpression_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitClassInstanceCreationExpression_lf_primary(Java9Parser.ClassInstanceCreationExpression_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterClassInstanceCreationExpression_lfno_primary(Java9Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitClassInstanceCreationExpression_lfno_primary(Java9Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentsOrDiamond(Java9Parser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentsOrDiamond(Java9Parser.TypeArgumentsOrDiamondContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess(Java9Parser.FieldAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess(Java9Parser.FieldAccessContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#fieldAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess_lf_primary(Java9Parser.FieldAccess_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#fieldAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess_lf_primary(Java9Parser.FieldAccess_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#fieldAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess_lfno_primary(Java9Parser.FieldAccess_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#fieldAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess_lfno_primary(Java9Parser.FieldAccess_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayAccess}.
	 * @param ctx the parse tree
	 */
	void enterArrayAccess(Java9Parser.ArrayAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayAccess}.
	 * @param ctx the parse tree
	 */
	void exitArrayAccess(Java9Parser.ArrayAccessContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterArrayAccess_lf_primary(Java9Parser.ArrayAccess_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitArrayAccess_lf_primary(Java9Parser.ArrayAccess_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterArrayAccess_lfno_primary(Java9Parser.ArrayAccess_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitArrayAccess_lfno_primary(Java9Parser.ArrayAccess_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodInvocation}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation(Java9Parser.MethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodInvocation}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation(Java9Parser.MethodInvocationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodInvocation_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation_lf_primary(Java9Parser.MethodInvocation_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodInvocation_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation_lf_primary(Java9Parser.MethodInvocation_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodInvocation_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodInvocation_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(Java9Parser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(Java9Parser.ArgumentListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodReference}.
	 * @param ctx the parse tree
	 */
	void enterMethodReference(Java9Parser.MethodReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodReference}.
	 * @param ctx the parse tree
	 */
	void exitMethodReference(Java9Parser.MethodReferenceContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodReference_lf_primary}.
	 * @param ctx the parse tree
	 */
	void enterMethodReference_lf_primary(Java9Parser.MethodReference_lf_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodReference_lf_primary}.
	 * @param ctx the parse tree
	 */
	void exitMethodReference_lf_primary(Java9Parser.MethodReference_lf_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#methodReference_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void enterMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#methodReference_lfno_primary}.
	 * @param ctx the parse tree
	 */
	void exitMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#arrayCreationExpression}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreationExpression(Java9Parser.ArrayCreationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#arrayCreationExpression}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreationExpression(Java9Parser.ArrayCreationExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#dimExprs}.
	 * @param ctx the parse tree
	 */
	void enterDimExprs(Java9Parser.DimExprsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#dimExprs}.
	 * @param ctx the parse tree
	 */
	void exitDimExprs(Java9Parser.DimExprsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#dimExpr}.
	 * @param ctx the parse tree
	 */
	void enterDimExpr(Java9Parser.DimExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#dimExpr}.
	 * @param ctx the parse tree
	 */
	void exitDimExpr(Java9Parser.DimExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(Java9Parser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(Java9Parser.ConstantExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(Java9Parser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(Java9Parser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(Java9Parser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(Java9Parser.LambdaExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#lambdaParameters}.
	 * @param ctx the parse tree
	 */
	void enterLambdaParameters(Java9Parser.LambdaParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#lambdaParameters}.
	 * @param ctx the parse tree
	 */
	void exitLambdaParameters(Java9Parser.LambdaParametersContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#inferredFormalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterInferredFormalParameterList(Java9Parser.InferredFormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#inferredFormalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitInferredFormalParameterList(Java9Parser.InferredFormalParameterListContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#lambdaBody}.
	 * @param ctx the parse tree
	 */
	void enterLambdaBody(Java9Parser.LambdaBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#lambdaBody}.
	 * @param ctx the parse tree
	 */
	void exitLambdaBody(Java9Parser.LambdaBodyContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(Java9Parser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(Java9Parser.AssignmentExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(Java9Parser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(Java9Parser.AssignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#leftHandSide}.
	 * @param ctx the parse tree
	 */
	void enterLeftHandSide(Java9Parser.LeftHandSideContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#leftHandSide}.
	 * @param ctx the parse tree
	 */
	void exitLeftHandSide(Java9Parser.LeftHandSideContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(Java9Parser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(Java9Parser.AssignmentOperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(Java9Parser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(Java9Parser.ConditionalExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(Java9Parser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(Java9Parser.AndExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(Java9Parser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(Java9Parser.EqualityExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(Java9Parser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(Java9Parser.RelationalExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(Java9Parser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(Java9Parser.ShiftExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(Java9Parser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(Java9Parser.AdditiveExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(Java9Parser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(Java9Parser.UnaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#preIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPreIncrementExpression(Java9Parser.PreIncrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#preIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPreIncrementExpression(Java9Parser.PreIncrementExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#preDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPreDecrementExpression(Java9Parser.PreDecrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#preDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPreDecrementExpression(Java9Parser.PreDecrementExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpression(Java9Parser.PostfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpression(Java9Parser.PostfixExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#postIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostIncrementExpression(Java9Parser.PostIncrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#postIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostIncrementExpression(Java9Parser.PostIncrementExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#postIncrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostIncrementExpression_lf_postfixExpression(Java9Parser.PostIncrementExpression_lf_postfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#postIncrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostIncrementExpression_lf_postfixExpression(Java9Parser.PostIncrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#postDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostDecrementExpression(Java9Parser.PostDecrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#postDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostDecrementExpression(Java9Parser.PostDecrementExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#postDecrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostDecrementExpression_lf_postfixExpression(Java9Parser.PostDecrementExpression_lf_postfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#postDecrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostDecrementExpression_lf_postfixExpression(Java9Parser.PostDecrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(Java9Parser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(Java9Parser.CastExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Java9Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(Java9Parser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Java9Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(Java9Parser.IdentifierContext ctx);
}