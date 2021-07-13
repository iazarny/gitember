// Generated from pascal.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.pascal;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link pascalParser}.
 */
public interface pascalListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link pascalParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(pascalParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(pascalParser.ProgramContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#programHeading}.
	 * @param ctx the parse tree
	 */
	void enterProgramHeading(pascalParser.ProgramHeadingContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#programHeading}.
	 * @param ctx the parse tree
	 */
	void exitProgramHeading(pascalParser.ProgramHeadingContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(pascalParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(pascalParser.IdentifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(pascalParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(pascalParser.BlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#usesUnitsPart}.
	 * @param ctx the parse tree
	 */
	void enterUsesUnitsPart(pascalParser.UsesUnitsPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#usesUnitsPart}.
	 * @param ctx the parse tree
	 */
	void exitUsesUnitsPart(pascalParser.UsesUnitsPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#labelDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void enterLabelDeclarationPart(pascalParser.LabelDeclarationPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#labelDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void exitLabelDeclarationPart(pascalParser.LabelDeclarationPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(pascalParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(pascalParser.LabelContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#constantDefinitionPart}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefinitionPart(pascalParser.ConstantDefinitionPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#constantDefinitionPart}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefinitionPart(pascalParser.ConstantDefinitionPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefinition(pascalParser.ConstantDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefinition(pascalParser.ConstantDefinitionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#constantChr}.
	 * @param ctx the parse tree
	 */
	void enterConstantChr(pascalParser.ConstantChrContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#constantChr}.
	 * @param ctx the parse tree
	 */
	void exitConstantChr(pascalParser.ConstantChrContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(pascalParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(pascalParser.ConstantContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unsignedNumber}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedNumber(pascalParser.UnsignedNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unsignedNumber}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedNumber(pascalParser.UnsignedNumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unsignedInteger}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedInteger(pascalParser.UnsignedIntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unsignedInteger}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedInteger(pascalParser.UnsignedIntegerContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unsignedReal}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedReal(pascalParser.UnsignedRealContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unsignedReal}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedReal(pascalParser.UnsignedRealContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(pascalParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(pascalParser.SignContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#bool_}.
	 * @param ctx the parse tree
	 */
	void enterBool_(pascalParser.Bool_Context ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#bool_}.
	 * @param ctx the parse tree
	 */
	void exitBool_(pascalParser.Bool_Context ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(pascalParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(pascalParser.StringContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#typeDefinitionPart}.
	 * @param ctx the parse tree
	 */
	void enterTypeDefinitionPart(pascalParser.TypeDefinitionPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#typeDefinitionPart}.
	 * @param ctx the parse tree
	 */
	void exitTypeDefinitionPart(pascalParser.TypeDefinitionPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void enterTypeDefinition(pascalParser.TypeDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void exitTypeDefinition(pascalParser.TypeDefinitionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#functionType}.
	 * @param ctx the parse tree
	 */
	void enterFunctionType(pascalParser.FunctionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#functionType}.
	 * @param ctx the parse tree
	 */
	void exitFunctionType(pascalParser.FunctionTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#procedureType}.
	 * @param ctx the parse tree
	 */
	void enterProcedureType(pascalParser.ProcedureTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#procedureType}.
	 * @param ctx the parse tree
	 */
	void exitProcedureType(pascalParser.ProcedureTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#type_}.
	 * @param ctx the parse tree
	 */
	void enterType_(pascalParser.Type_Context ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#type_}.
	 * @param ctx the parse tree
	 */
	void exitType_(pascalParser.Type_Context ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void enterSimpleType(pascalParser.SimpleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void exitSimpleType(pascalParser.SimpleTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#scalarType}.
	 * @param ctx the parse tree
	 */
	void enterScalarType(pascalParser.ScalarTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#scalarType}.
	 * @param ctx the parse tree
	 */
	void exitScalarType(pascalParser.ScalarTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#subrangeType}.
	 * @param ctx the parse tree
	 */
	void enterSubrangeType(pascalParser.SubrangeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#subrangeType}.
	 * @param ctx the parse tree
	 */
	void exitSubrangeType(pascalParser.SubrangeTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeIdentifier(pascalParser.TypeIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeIdentifier(pascalParser.TypeIdentifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#structuredType}.
	 * @param ctx the parse tree
	 */
	void enterStructuredType(pascalParser.StructuredTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#structuredType}.
	 * @param ctx the parse tree
	 */
	void exitStructuredType(pascalParser.StructuredTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unpackedStructuredType}.
	 * @param ctx the parse tree
	 */
	void enterUnpackedStructuredType(pascalParser.UnpackedStructuredTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unpackedStructuredType}.
	 * @param ctx the parse tree
	 */
	void exitUnpackedStructuredType(pascalParser.UnpackedStructuredTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#stringtype}.
	 * @param ctx the parse tree
	 */
	void enterStringtype(pascalParser.StringtypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#stringtype}.
	 * @param ctx the parse tree
	 */
	void exitStringtype(pascalParser.StringtypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(pascalParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(pascalParser.ArrayTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(pascalParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(pascalParser.TypeListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#indexType}.
	 * @param ctx the parse tree
	 */
	void enterIndexType(pascalParser.IndexTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#indexType}.
	 * @param ctx the parse tree
	 */
	void exitIndexType(pascalParser.IndexTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#componentType}.
	 * @param ctx the parse tree
	 */
	void enterComponentType(pascalParser.ComponentTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#componentType}.
	 * @param ctx the parse tree
	 */
	void exitComponentType(pascalParser.ComponentTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#recordType}.
	 * @param ctx the parse tree
	 */
	void enterRecordType(pascalParser.RecordTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#recordType}.
	 * @param ctx the parse tree
	 */
	void exitRecordType(pascalParser.RecordTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void enterFieldList(pascalParser.FieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void exitFieldList(pascalParser.FieldListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#fixedPart}.
	 * @param ctx the parse tree
	 */
	void enterFixedPart(pascalParser.FixedPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#fixedPart}.
	 * @param ctx the parse tree
	 */
	void exitFixedPart(pascalParser.FixedPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#recordSection}.
	 * @param ctx the parse tree
	 */
	void enterRecordSection(pascalParser.RecordSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#recordSection}.
	 * @param ctx the parse tree
	 */
	void exitRecordSection(pascalParser.RecordSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#variantPart}.
	 * @param ctx the parse tree
	 */
	void enterVariantPart(pascalParser.VariantPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#variantPart}.
	 * @param ctx the parse tree
	 */
	void exitVariantPart(pascalParser.VariantPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#tag}.
	 * @param ctx the parse tree
	 */
	void enterTag(pascalParser.TagContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#tag}.
	 * @param ctx the parse tree
	 */
	void exitTag(pascalParser.TagContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#variant}.
	 * @param ctx the parse tree
	 */
	void enterVariant(pascalParser.VariantContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#variant}.
	 * @param ctx the parse tree
	 */
	void exitVariant(pascalParser.VariantContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#setType}.
	 * @param ctx the parse tree
	 */
	void enterSetType(pascalParser.SetTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#setType}.
	 * @param ctx the parse tree
	 */
	void exitSetType(pascalParser.SetTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#baseType}.
	 * @param ctx the parse tree
	 */
	void enterBaseType(pascalParser.BaseTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#baseType}.
	 * @param ctx the parse tree
	 */
	void exitBaseType(pascalParser.BaseTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#fileType}.
	 * @param ctx the parse tree
	 */
	void enterFileType(pascalParser.FileTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#fileType}.
	 * @param ctx the parse tree
	 */
	void exitFileType(pascalParser.FileTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#pointerType}.
	 * @param ctx the parse tree
	 */
	void enterPointerType(pascalParser.PointerTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#pointerType}.
	 * @param ctx the parse tree
	 */
	void exitPointerType(pascalParser.PointerTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#variableDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarationPart(pascalParser.VariableDeclarationPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#variableDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarationPart(pascalParser.VariableDeclarationPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(pascalParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(pascalParser.VariableDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#procedureAndFunctionDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void enterProcedureAndFunctionDeclarationPart(pascalParser.ProcedureAndFunctionDeclarationPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#procedureAndFunctionDeclarationPart}.
	 * @param ctx the parse tree
	 */
	void exitProcedureAndFunctionDeclarationPart(pascalParser.ProcedureAndFunctionDeclarationPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#procedureOrFunctionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterProcedureOrFunctionDeclaration(pascalParser.ProcedureOrFunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#procedureOrFunctionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitProcedureOrFunctionDeclaration(pascalParser.ProcedureOrFunctionDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterProcedureDeclaration(pascalParser.ProcedureDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitProcedureDeclaration(pascalParser.ProcedureDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(pascalParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(pascalParser.FormalParameterListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#formalParameterSection}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterSection(pascalParser.FormalParameterSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#formalParameterSection}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterSection(pascalParser.FormalParameterSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#parameterGroup}.
	 * @param ctx the parse tree
	 */
	void enterParameterGroup(pascalParser.ParameterGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#parameterGroup}.
	 * @param ctx the parse tree
	 */
	void exitParameterGroup(pascalParser.ParameterGroupContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(pascalParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(pascalParser.IdentifierListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#constList}.
	 * @param ctx the parse tree
	 */
	void enterConstList(pascalParser.ConstListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#constList}.
	 * @param ctx the parse tree
	 */
	void exitConstList(pascalParser.ConstListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(pascalParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(pascalParser.FunctionDeclarationContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#resultType}.
	 * @param ctx the parse tree
	 */
	void enterResultType(pascalParser.ResultTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#resultType}.
	 * @param ctx the parse tree
	 */
	void exitResultType(pascalParser.ResultTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(pascalParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(pascalParser.StatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unlabelledStatement}.
	 * @param ctx the parse tree
	 */
	void enterUnlabelledStatement(pascalParser.UnlabelledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unlabelledStatement}.
	 * @param ctx the parse tree
	 */
	void exitUnlabelledStatement(pascalParser.UnlabelledStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#simpleStatement}.
	 * @param ctx the parse tree
	 */
	void enterSimpleStatement(pascalParser.SimpleStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#simpleStatement}.
	 * @param ctx the parse tree
	 */
	void exitSimpleStatement(pascalParser.SimpleStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(pascalParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(pascalParser.AssignmentStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(pascalParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(pascalParser.VariableContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(pascalParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(pascalParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#relationaloperator}.
	 * @param ctx the parse tree
	 */
	void enterRelationaloperator(pascalParser.RelationaloperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#relationaloperator}.
	 * @param ctx the parse tree
	 */
	void exitRelationaloperator(pascalParser.RelationaloperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#simpleExpression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpression(pascalParser.SimpleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#simpleExpression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpression(pascalParser.SimpleExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#additiveoperator}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveoperator(pascalParser.AdditiveoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#additiveoperator}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveoperator(pascalParser.AdditiveoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(pascalParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(pascalParser.TermContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#multiplicativeoperator}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeoperator(pascalParser.MultiplicativeoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#multiplicativeoperator}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeoperator(pascalParser.MultiplicativeoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#signedFactor}.
	 * @param ctx the parse tree
	 */
	void enterSignedFactor(pascalParser.SignedFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#signedFactor}.
	 * @param ctx the parse tree
	 */
	void exitSignedFactor(pascalParser.SignedFactorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(pascalParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(pascalParser.FactorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#unsignedConstant}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedConstant(pascalParser.UnsignedConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#unsignedConstant}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedConstant(pascalParser.UnsignedConstantContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#functionDesignator}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDesignator(pascalParser.FunctionDesignatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#functionDesignator}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDesignator(pascalParser.FunctionDesignatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(pascalParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(pascalParser.ParameterListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#set_}.
	 * @param ctx the parse tree
	 */
	void enterSet_(pascalParser.Set_Context ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#set_}.
	 * @param ctx the parse tree
	 */
	void exitSet_(pascalParser.Set_Context ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#elementList}.
	 * @param ctx the parse tree
	 */
	void enterElementList(pascalParser.ElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#elementList}.
	 * @param ctx the parse tree
	 */
	void exitElementList(pascalParser.ElementListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(pascalParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(pascalParser.ElementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#procedureStatement}.
	 * @param ctx the parse tree
	 */
	void enterProcedureStatement(pascalParser.ProcedureStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#procedureStatement}.
	 * @param ctx the parse tree
	 */
	void exitProcedureStatement(pascalParser.ProcedureStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#actualParameter}.
	 * @param ctx the parse tree
	 */
	void enterActualParameter(pascalParser.ActualParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#actualParameter}.
	 * @param ctx the parse tree
	 */
	void exitActualParameter(pascalParser.ActualParameterContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#parameterwidth}.
	 * @param ctx the parse tree
	 */
	void enterParameterwidth(pascalParser.ParameterwidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#parameterwidth}.
	 * @param ctx the parse tree
	 */
	void exitParameterwidth(pascalParser.ParameterwidthContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#gotoStatement}.
	 * @param ctx the parse tree
	 */
	void enterGotoStatement(pascalParser.GotoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#gotoStatement}.
	 * @param ctx the parse tree
	 */
	void exitGotoStatement(pascalParser.GotoStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(pascalParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(pascalParser.EmptyStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#empty_}.
	 * @param ctx the parse tree
	 */
	void enterEmpty_(pascalParser.Empty_Context ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#empty_}.
	 * @param ctx the parse tree
	 */
	void exitEmpty_(pascalParser.Empty_Context ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#structuredStatement}.
	 * @param ctx the parse tree
	 */
	void enterStructuredStatement(pascalParser.StructuredStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#structuredStatement}.
	 * @param ctx the parse tree
	 */
	void exitStructuredStatement(pascalParser.StructuredStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(pascalParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(pascalParser.CompoundStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(pascalParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(pascalParser.StatementsContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void enterConditionalStatement(pascalParser.ConditionalStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void exitConditionalStatement(pascalParser.ConditionalStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(pascalParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(pascalParser.IfStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void enterCaseStatement(pascalParser.CaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void exitCaseStatement(pascalParser.CaseStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#caseListElement}.
	 * @param ctx the parse tree
	 */
	void enterCaseListElement(pascalParser.CaseListElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#caseListElement}.
	 * @param ctx the parse tree
	 */
	void exitCaseListElement(pascalParser.CaseListElementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#repetetiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterRepetetiveStatement(pascalParser.RepetetiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#repetetiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitRepetetiveStatement(pascalParser.RepetetiveStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(pascalParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(pascalParser.WhileStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#repeatStatement}.
	 * @param ctx the parse tree
	 */
	void enterRepeatStatement(pascalParser.RepeatStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#repeatStatement}.
	 * @param ctx the parse tree
	 */
	void exitRepeatStatement(pascalParser.RepeatStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(pascalParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(pascalParser.ForStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#forList}.
	 * @param ctx the parse tree
	 */
	void enterForList(pascalParser.ForListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#forList}.
	 * @param ctx the parse tree
	 */
	void exitForList(pascalParser.ForListContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#initialValue}.
	 * @param ctx the parse tree
	 */
	void enterInitialValue(pascalParser.InitialValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#initialValue}.
	 * @param ctx the parse tree
	 */
	void exitInitialValue(pascalParser.InitialValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#finalValue}.
	 * @param ctx the parse tree
	 */
	void enterFinalValue(pascalParser.FinalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#finalValue}.
	 * @param ctx the parse tree
	 */
	void exitFinalValue(pascalParser.FinalValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#withStatement}.
	 * @param ctx the parse tree
	 */
	void enterWithStatement(pascalParser.WithStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#withStatement}.
	 * @param ctx the parse tree
	 */
	void exitWithStatement(pascalParser.WithStatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link pascalParser#recordVariableList}.
	 * @param ctx the parse tree
	 */
	void enterRecordVariableList(pascalParser.RecordVariableListContext ctx);
	/**
	 * Exit a parse tree produced by {@link pascalParser#recordVariableList}.
	 * @param ctx the parse tree
	 */
	void exitRecordVariableList(pascalParser.RecordVariableListContext ctx);
}