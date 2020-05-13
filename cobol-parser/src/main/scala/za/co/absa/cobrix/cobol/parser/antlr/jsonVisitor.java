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

// Generated from json.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link jsonParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface jsonVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link jsonParser#json}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson(jsonParser.JsonContext ctx);
	/**
	 * Visit a parse tree produced by {@link jsonParser#obj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObj(jsonParser.ObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link jsonParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(jsonParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link jsonParser#arr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArr(jsonParser.ArrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_string}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_string(jsonParser.Value_stringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_number}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_number(jsonParser.Value_numberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_obj}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_obj(jsonParser.Value_objContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_array}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_array(jsonParser.Value_arrayContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_true}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_true(jsonParser.Value_trueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_false}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_false(jsonParser.Value_falseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_null}
	 * labeled alternative in {@link jsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_null(jsonParser.Value_nullContext ctx);
}
