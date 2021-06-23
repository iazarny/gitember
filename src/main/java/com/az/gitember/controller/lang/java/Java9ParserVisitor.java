// Generated from Java9Parser.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.java;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Java9Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Java9ParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Java9Parser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(Java9Parser.LiteralContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(Java9Parser.PrimitiveTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#numericType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericType(Java9Parser.NumericTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#integralType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegralType(Java9Parser.IntegralTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#floatingPointType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatingPointType(Java9Parser.FloatingPointTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#referenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceType(Java9Parser.ReferenceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceType(Java9Parser.ClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType(Java9Parser.ClassTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType_lf_classOrInterfaceType(Java9Parser.ClassType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType_lfno_classOrInterfaceType(Java9Parser.ClassType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType(Java9Parser.InterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType_lf_classOrInterfaceType(Java9Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType_lfno_classOrInterfaceType(Java9Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeVariable(Java9Parser.TypeVariableContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(Java9Parser.ArrayTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#dims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDims(Java9Parser.DimsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameter(Java9Parser.TypeParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeParameterModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterModifier(Java9Parser.TypeParameterModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBound(Java9Parser.TypeBoundContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#additionalBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditionalBound(Java9Parser.AdditionalBoundContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(Java9Parser.TypeArgumentsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentList(Java9Parser.TypeArgumentListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgument(Java9Parser.TypeArgumentContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#wildcard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcard(Java9Parser.WildcardContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#wildcardBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcardBounds(Java9Parser.WildcardBoundsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#moduleName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModuleName(Java9Parser.ModuleNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(Java9Parser.PackageNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(Java9Parser.TypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#packageOrTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageOrTypeName(Java9Parser.PackageOrTypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#expressionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionName(Java9Parser.ExpressionNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodName(Java9Parser.MethodNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#ambiguousName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAmbiguousName(Java9Parser.AmbiguousNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(Java9Parser.CompilationUnitContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#ordinaryCompilation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#modularCompilation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModularCompilation(Java9Parser.ModularCompilationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#packageModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageModifier(Java9Parser.PackageModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(Java9Parser.ImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#singleTypeImportDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTypeImportDeclaration(Java9Parser.SingleTypeImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeImportOnDemandDeclaration(Java9Parser.TypeImportOnDemandDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#singleStaticImportDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleStaticImportDeclaration(Java9Parser.SingleStaticImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#staticImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticImportOnDemandDeclaration(Java9Parser.StaticImportOnDemandDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(Java9Parser.TypeDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#moduleDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModuleDeclaration(Java9Parser.ModuleDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#moduleDirective}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModuleDirective(Java9Parser.ModuleDirectiveContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#requiresModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRequiresModifier(Java9Parser.RequiresModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(Java9Parser.ClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#normalClassDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassModifier(Java9Parser.ClassModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameters(Java9Parser.TypeParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterList(Java9Parser.TypeParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#superclass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperclass(Java9Parser.SuperclassContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#superinterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceTypeList(Java9Parser.InterfaceTypeListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(Java9Parser.ClassBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBodyDeclaration(Java9Parser.ClassBodyDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassMemberDeclaration(Java9Parser.ClassMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#fieldModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldModifier(Java9Parser.FieldModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaratorList(Java9Parser.VariableDeclaratorListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarator(Java9Parser.VariableDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableDeclaratorId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaratorId(Java9Parser.VariableDeclaratorIdContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializer(Java9Parser.VariableInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannType(Java9Parser.UnannTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannPrimitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannPrimitiveType(Java9Parser.UnannPrimitiveTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannReferenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannReferenceType(Java9Parser.UnannReferenceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassOrInterfaceType(Java9Parser.UnannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannClassType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType(Java9Parser.UnannClassTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannClassType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType_lf_unannClassOrInterfaceType(Java9Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannClassType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType_lfno_unannClassOrInterfaceType(Java9Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType(Java9Parser.UnannInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannInterfaceType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType_lf_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannInterfaceType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType_lfno_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannTypeVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannTypeVariable(Java9Parser.UnannTypeVariableContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unannArrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannArrayType(Java9Parser.UnannArrayTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(Java9Parser.MethodDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodModifier(Java9Parser.MethodModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodHeader(Java9Parser.MethodHeaderContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#result}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult(Java9Parser.ResultContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(Java9Parser.FormalParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(Java9Parser.FormalParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(Java9Parser.FormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableModifier(Java9Parser.VariableModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#lastFormalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLastFormalParameter(Java9Parser.LastFormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#receiverParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReceiverParameter(Java9Parser.ReceiverParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#throws_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrows_(Java9Parser.Throws_Context ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#exceptionTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionTypeList(Java9Parser.ExceptionTypeListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#exceptionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionType(Java9Parser.ExceptionTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodBody(Java9Parser.MethodBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#instanceInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceInitializer(Java9Parser.InstanceInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#staticInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticInitializer(Java9Parser.StaticInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(Java9Parser.ConstructorDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constructorModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorModifier(Java9Parser.ConstructorModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constructorDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclarator(Java9Parser.ConstructorDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(Java9Parser.SimpleTypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constructorBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorBody(Java9Parser.ConstructorBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitConstructorInvocation(Java9Parser.ExplicitConstructorInvocationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(Java9Parser.EnumDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBody(Java9Parser.EnumBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantList(Java9Parser.EnumConstantListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstant(Java9Parser.EnumConstantContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumConstantModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantModifier(Java9Parser.EnumConstantModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBodyDeclarations(Java9Parser.EnumBodyDeclarationsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceDeclaration(Java9Parser.InterfaceDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#normalInterfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalInterfaceDeclaration(Java9Parser.NormalInterfaceDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceModifier(Java9Parser.InterfaceModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#extendsInterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtendsInterfaces(Java9Parser.ExtendsInterfacesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBody(Java9Parser.InterfaceBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMemberDeclaration(Java9Parser.InterfaceMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDeclaration(Java9Parser.ConstantDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constantModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantModifier(Java9Parser.ConstantModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodDeclaration(Java9Parser.InterfaceMethodDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodModifier(Java9Parser.InterfaceMethodModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotationTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeDeclaration(Java9Parser.AnnotationTypeDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotationTypeBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeBody(Java9Parser.AnnotationTypeBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotationTypeMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeMemberDeclaration(Java9Parser.AnnotationTypeMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotationTypeElementDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeElementDeclaration(Java9Parser.AnnotationTypeElementDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotationTypeElementModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeElementModifier(Java9Parser.AnnotationTypeElementModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(Java9Parser.DefaultValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(Java9Parser.AnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#normalAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalAnnotation(Java9Parser.NormalAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#elementValuePairList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePairList(Java9Parser.ElementValuePairListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePair(Java9Parser.ElementValuePairContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#elementValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValue(Java9Parser.ElementValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueArrayInitializer(Java9Parser.ElementValueArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#elementValueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueList(Java9Parser.ElementValueListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#markerAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMarkerAnnotation(Java9Parser.MarkerAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#singleElementAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleElementAnnotation(Java9Parser.SingleElementAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(Java9Parser.ArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableInitializerList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializerList(Java9Parser.VariableInitializerListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(Java9Parser.BlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#blockStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatements(Java9Parser.BlockStatementsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatement(Java9Parser.BlockStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#localVariableDeclarationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclaration(Java9Parser.LocalVariableDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(Java9Parser.StatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#statementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementNoShortIf(Java9Parser.StatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementWithoutTrailingSubstatement(Java9Parser.StatementWithoutTrailingSubstatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(Java9Parser.EmptyStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#labeledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatement(Java9Parser.LabeledStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#labeledStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatementNoShortIf(Java9Parser.LabeledStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(Java9Parser.ExpressionStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpression(Java9Parser.StatementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#ifThenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenStatement(Java9Parser.IfThenStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatement(Java9Parser.IfThenElseStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatementNoShortIf(Java9Parser.IfThenElseStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#assertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssertStatement(Java9Parser.AssertStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStatement(Java9Parser.SwitchStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#switchBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlock(Java9Parser.SwitchBlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlockStatementGroup(Java9Parser.SwitchBlockStatementGroupContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#switchLabels}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabels(Java9Parser.SwitchLabelsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabel(Java9Parser.SwitchLabelContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enumConstantName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantName(Java9Parser.EnumConstantNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(Java9Parser.WhileStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatementNoShortIf(Java9Parser.WhileStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(Java9Parser.DoStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(Java9Parser.ForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatementNoShortIf(Java9Parser.ForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#basicForStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicForStatement(Java9Parser.BasicForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#basicForStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicForStatementNoShortIf(Java9Parser.BasicForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#forInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInit(Java9Parser.ForInitContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#forUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdate(Java9Parser.ForUpdateContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#statementExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpressionList(Java9Parser.StatementExpressionListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enhancedForStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnhancedForStatement(Java9Parser.EnhancedForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#enhancedForStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnhancedForStatementNoShortIf(Java9Parser.EnhancedForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(Java9Parser.BreakStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStatement(Java9Parser.ContinueStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(Java9Parser.ReturnStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#throwStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrowStatement(Java9Parser.ThrowStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#synchronizedStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynchronizedStatement(Java9Parser.SynchronizedStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#tryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryStatement(Java9Parser.TryStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#catches}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatches(Java9Parser.CatchesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(Java9Parser.CatchClauseContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#catchFormalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchFormalParameter(Java9Parser.CatchFormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#catchType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchType(Java9Parser.CatchTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#finally_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFinally_(Java9Parser.Finally_Context ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#tryWithResourcesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryWithResourcesStatement(Java9Parser.TryWithResourcesStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#resourceSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceSpecification(Java9Parser.ResourceSpecificationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#resourceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceList(Java9Parser.ResourceListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(Java9Parser.ResourceContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#variableAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableAccess(Java9Parser.VariableAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(Java9Parser.PrimaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray(Java9Parser.PrimaryNoNewArrayContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_arrayAccess(Java9Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_arrayAccess(Java9Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassLiteral(Java9Parser.ClassLiteralContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression_lf_primary(Java9Parser.ClassInstanceCreationExpression_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#classInstanceCreationExpression_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression_lfno_primary(Java9Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentsOrDiamond(Java9Parser.TypeArgumentsOrDiamondContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#fieldAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess(Java9Parser.FieldAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#fieldAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess_lf_primary(Java9Parser.FieldAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#fieldAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess_lfno_primary(Java9Parser.FieldAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess(Java9Parser.ArrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess_lf_primary(Java9Parser.ArrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess_lfno_primary(Java9Parser.ArrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation(Java9Parser.MethodInvocationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodInvocation_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation_lf_primary(Java9Parser.MethodInvocation_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodInvocation_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(Java9Parser.ArgumentListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference(Java9Parser.MethodReferenceContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodReference_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference_lf_primary(Java9Parser.MethodReference_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#methodReference_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#arrayCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCreationExpression(Java9Parser.ArrayCreationExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#dimExprs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimExprs(Java9Parser.DimExprsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#dimExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimExpr(Java9Parser.DimExprContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(Java9Parser.ConstantExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(Java9Parser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(Java9Parser.LambdaExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#lambdaParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaParameters(Java9Parser.LambdaParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#inferredFormalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInferredFormalParameterList(Java9Parser.InferredFormalParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#lambdaBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaBody(Java9Parser.LambdaBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(Java9Parser.AssignmentExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(Java9Parser.AssignmentContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#leftHandSide}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftHandSide(Java9Parser.LeftHandSideContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(Java9Parser.AssignmentOperatorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(Java9Parser.ConditionalExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(Java9Parser.AndExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(Java9Parser.EqualityExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(Java9Parser.RelationalExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(Java9Parser.ShiftExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(Java9Parser.AdditiveExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(Java9Parser.UnaryExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#preIncrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreIncrementExpression(Java9Parser.PreIncrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#preDecrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreDecrementExpression(Java9Parser.PreDecrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpression(Java9Parser.PostfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#postIncrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIncrementExpression(Java9Parser.PostIncrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#postIncrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIncrementExpression_lf_postfixExpression(Java9Parser.PostIncrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#postDecrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostDecrementExpression(Java9Parser.PostDecrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#postDecrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostDecrementExpression_lf_postfixExpression(Java9Parser.PostDecrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(Java9Parser.CastExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java9Parser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(Java9Parser.IdentifierContext ctx);
}