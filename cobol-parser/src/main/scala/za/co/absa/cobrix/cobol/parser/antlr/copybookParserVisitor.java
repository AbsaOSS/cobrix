/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package za.co.absa.cobrix.cobol.parser.antlr;

// Generated from copybookParser.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link copybookParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface copybookParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link copybookParser#main}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMain(copybookParser.MainContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(copybookParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(copybookParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(copybookParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(copybookParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(copybookParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#thru}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThru(copybookParser.ThruContext ctx);
	/**
	 * Visit a parse tree produced by the {@code values}
	 * labeled alternative in {@link copybookParser#plusMinusplusMinusprecision9precision9precision9precision9precision9precision9precision9precision9precision9precision9precision9precision9signPrecision9signPrecision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues(copybookParser.ValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#valuesFromTo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFromTo(copybookParser.ValuesFromToContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#valuesFrom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFrom(copybookParser.ValuesFromContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#valuesTo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesTo(copybookParser.ValuesToContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#specialValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialValues(copybookParser.SpecialValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#sorts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSorts(copybookParser.SortsContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#occursTo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOccursTo(copybookParser.OccursToContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#dependingOn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDependingOn(copybookParser.DependingOnContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#indexedBy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexedBy(copybookParser.IndexedByContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#occurs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOccurs(copybookParser.OccursContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#redefines}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRedefines(copybookParser.RedefinesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#renames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenames(copybookParser.RenamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#usageLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsageLiteral(copybookParser.UsageLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#groupUsageLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupUsageLiteral(copybookParser.GroupUsageLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#usage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsage(copybookParser.UsageContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#usageGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsageGroup(copybookParser.UsageGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#separateSign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeparateSign(copybookParser.SeparateSignContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#justified}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJustified(copybookParser.JustifiedContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(copybookParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plus}
	 * labeled alternative in {@link copybookParser#plusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlus(copybookParser.PlusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code minus}
	 * labeled alternative in {@link copybookParser#plusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinus(copybookParser.MinusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Nines}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Nines(copybookParser.Precision9NinesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Ss}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Ss(copybookParser.Precision9SsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Ps}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Ps(copybookParser.Precision9PsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Zs}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Zs(copybookParser.Precision9ZsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Vs}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Vs(copybookParser.Precision9VsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9ExplicitDot}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9ExplicitDot(copybookParser.Precision9ExplicitDotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9DecimalScaled}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9DecimalScaled(copybookParser.Precision9DecimalScaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9Scaled}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9Scaled(copybookParser.Precision9ScaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision9ScaledLead}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision9ScaledLead(copybookParser.Precision9ScaledLeadContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precisionZExplicitDot}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecisionZExplicitDot(copybookParser.PrecisionZExplicitDotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precisionZDecimalScaled}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecisionZDecimalScaled(copybookParser.PrecisionZDecimalScaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precisionZScaled}
	 * labeled alternative in {@link copybookParser#precision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecisionZScaled(copybookParser.PrecisionZScaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code leadingSign}
	 * labeled alternative in {@link copybookParser#signPrecision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeadingSign(copybookParser.LeadingSignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trailingSign}
	 * labeled alternative in {@link copybookParser#signPrecision9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrailingSign(copybookParser.TrailingSignContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#alphaX}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlphaX(copybookParser.AlphaXContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#alphaA}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlphaA(copybookParser.AlphaAContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#pictureLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPictureLiteral(copybookParser.PictureLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#pic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPic(copybookParser.PicContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSection(copybookParser.SectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#skipLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkipLiteral(copybookParser.SkipLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup(copybookParser.GroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive(copybookParser.PrimitiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#level66statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevel66statement(copybookParser.Level66statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#level88statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevel88statement(copybookParser.Level88statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybookParser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(copybookParser.ItemContext ctx);
}
