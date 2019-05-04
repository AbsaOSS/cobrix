package za.co.absa.cobrix.cobol.parser.antlr;

// Generated from copybook_parser.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link copybook_parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface copybook_parserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link copybook_parser#main}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMain(copybook_parser.MainContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(copybook_parser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(copybook_parser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(copybook_parser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(copybook_parser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(copybook_parser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#thru}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThru(copybook_parser.ThruContext ctx);
	/**
	 * Visit a parse tree produced by the {@code values}
	 * labeled alternative in {@link copybook_parser#plus_minusplus_minusprecision_9precision_9precision_9precision_9precision_9precision_9precision_9precision_9precision_9precision_9sign_precision_9sign_precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues(copybook_parser.ValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#valuesFromTo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFromTo(copybook_parser.ValuesFromToContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#valuesFrom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFrom(copybook_parser.ValuesFromContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#valuesTo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesTo(copybook_parser.ValuesToContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#specialValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialValues(copybook_parser.SpecialValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#sorts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSorts(copybook_parser.SortsContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#occurs_to}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOccurs_to(copybook_parser.Occurs_toContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#depending_on}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDepending_on(copybook_parser.Depending_onContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#indexed_by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexed_by(copybook_parser.Indexed_byContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#occurs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOccurs(copybook_parser.OccursContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#redefines}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRedefines(copybook_parser.RedefinesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#renames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenames(copybook_parser.RenamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#usageLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsageLiteral(copybook_parser.UsageLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#usage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsage(copybook_parser.UsageContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#separate_sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeparate_sign(copybook_parser.Separate_signContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#justified}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJustified(copybook_parser.JustifiedContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(copybook_parser.TermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plus}
	 * labeled alternative in {@link copybook_parser#plus_minus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlus(copybook_parser.PlusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code minus}
	 * labeled alternative in {@link copybook_parser#plus_minus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinus(copybook_parser.MinusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_nines}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_nines(copybook_parser.Precision_9_ninesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_ss}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_ss(copybook_parser.Precision_9_ssContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_zs}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_zs(copybook_parser.Precision_9_zsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_explicit_dot}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_explicit_dot(copybook_parser.Precision_9_explicit_dotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_decimal_scaled}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_decimal_scaled(copybook_parser.Precision_9_decimal_scaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_scaled}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_scaled(copybook_parser.Precision_9_scaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_9_scaled_lead}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_9_scaled_lead(copybook_parser.Precision_9_scaled_leadContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_z_explicit_dot}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_z_explicit_dot(copybook_parser.Precision_z_explicit_dotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_z_decimal_scaled}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_z_decimal_scaled(copybook_parser.Precision_z_decimal_scaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precision_z_scaled}
	 * labeled alternative in {@link copybook_parser#precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_z_scaled(copybook_parser.Precision_z_scaledContext ctx);
	/**
	 * Visit a parse tree produced by the {@code leading_sign}
	 * labeled alternative in {@link copybook_parser#sign_precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeading_sign(copybook_parser.Leading_signContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trailing_sign}
	 * labeled alternative in {@link copybook_parser#sign_precision_9}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrailing_sign(copybook_parser.Trailing_signContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#alpha_x}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlpha_x(copybook_parser.Alpha_xContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#alpha_a}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlpha_a(copybook_parser.Alpha_aContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#pictureLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPictureLiteral(copybook_parser.PictureLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#pic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPic(copybook_parser.PicContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSection(copybook_parser.SectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#skipLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkipLiteral(copybook_parser.SkipLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup(copybook_parser.GroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive(copybook_parser.PrimitiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#level66statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevel66statement(copybook_parser.Level66statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#level88statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevel88statement(copybook_parser.Level88statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link copybook_parser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(copybook_parser.ItemContext ctx);
}