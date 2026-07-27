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

// Generated from copybookParser.g4 by ANTLR 4.9.3
package za.co.absa.cobrix.cobol.parser.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class copybookParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		THRU_OR_THROUGH=1, ALL=2, ARE=3, ASCENDING=4, BINARY=5, BLANK=6, BY=7, 
		CHARACTER=8, CHARACTERS=9, COMP=10, COMP_0=11, COMP_1=12, COMP_2=13, COMP_3=14, 
		COMP_3U=15, COMP_4=16, COMP_5=17, COMP_9=18, COMPUTATIONAL=19, COMPUTATIONAL_0=20, 
		COMPUTATIONAL_1=21, COMPUTATIONAL_2=22, COMPUTATIONAL_3=23, COMPUTATIONAL_3U=24, 
		COMPUTATIONAL_4=25, COMPUTATIONAL_5=26, COMPUTATIONAL_9=27, COPY=28, DEPENDING=29, 
		DESCENDING=30, DISPLAY=31, EXTERNAL=32, FALSE=33, FROM=34, HIGH_VALUE=35, 
		HIGH_VALUES=36, INDEXED=37, IS=38, JUST=39, JUSTIFIED=40, KEY=41, LEADING=42, 
		LEFT=43, LOW_VALUE=44, LOW_VALUES=45, NULL=46, NULLS=47, NUMBER=48, NUMERIC=49, 
		OCCURS=50, ON=51, PACKED_DECIMAL=52, PIC=53, PICTURE=54, QUOTE=55, QUOTES=56, 
		REDEFINES=57, RENAMES=58, RIGHT=59, SEPARATE=60, SKIP1=61, SKIP2=62, SKIP3=63, 
		SIGN=64, SPACE=65, SPACES=66, THROUGH=67, THRU=68, TIMES=69, TO=70, TRAILING=71, 
		TRUE=72, USAGE=73, USING=74, VALUE=75, VALUES=76, WHEN=77, ZERO=78, ZEROS=79, 
		ZEROES=80, DOUBLEQUOTE=81, COMMACHAR=82, DOT=83, LPARENCHAR=84, MINUSCHAR=85, 
		PLUSCHAR=86, RPARENCHAR=87, SINGLEQUOTE=88, SLASHCHAR=89, TERMINAL=90, 
		COMMENT=91, NINES=92, A_S=93, P_S=94, X_S=95, N_S=96, S_S=97, Z_S=98, 
		V_S=99, P_NS=100, S_NS=101, Z_NS=102, V_NS=103, PRECISION_9_EXPLICIT_DOT=104, 
		PRECISION_9_DECIMAL_SCALED=105, PRECISION_9_DECIMAL_WITH_V=106, PRECISION_9_SCALED=107, 
		PRECISION_9_SCALED_LEAD=108, PRECISION_Z_EXPLICIT_DOT=109, PRECISION_Z_DECIMAL_SCALED=110, 
		PRECISION_Z_SCALED=111, LENGTH_TYPE_9=112, LENGTH_TYPE_9_1=113, LENGTH_TYPE_A=114, 
		LENGTH_TYPE_A_1=115, LENGTH_TYPE_P=116, LENGTH_TYPE_P_1=117, LENGTH_TYPE_X=118, 
		LENGTH_TYPE_X_1=119, LENGTH_TYPE_N=120, LENGTH_TYPE_N_1=121, LENGTH_TYPE_Z=122, 
		LENGTH_TYPE_Z_1=123, STRINGLITERAL=124, LEVEL_ROOT=125, LEVEL_REGULAR=126, 
		LEVEL_NUMBER_66=127, LEVEL_NUMBER_77=128, LEVEL_NUMBER_88=129, INTEGERLITERAL=130, 
		POSITIVELITERAL=131, NUMERICLITERAL=132, SINGLE_QUOTED_IDENTIFIER=133, 
		IDENTIFIER=134, CONTROL_Z=135, WS=136;
	public static final int
		RULE_main = 0, RULE_literal = 1, RULE_numericLiteral = 2, RULE_integerLiteral = 3, 
		RULE_booleanLiteral = 4, RULE_identifier = 5, RULE_thru = 6, RULE_values = 7, 
		RULE_valuesFromTo = 8, RULE_valuesFrom = 9, RULE_valuesTo = 10, RULE_specialValues = 11, 
		RULE_sorts = 12, RULE_occursTo = 13, RULE_dependingOn = 14, RULE_indexedBy = 15, 
		RULE_occurs = 16, RULE_redefines = 17, RULE_renames = 18, RULE_usageLiteral = 19, 
		RULE_groupUsageLiteral = 20, RULE_usage = 21, RULE_usageGroup = 22, RULE_separateSign = 23, 
		RULE_justified = 24, RULE_term = 25, RULE_plusMinus = 26, RULE_precision9 = 27, 
		RULE_signPrecision9 = 28, RULE_alphaX = 29, RULE_alphaN = 30, RULE_alphaA = 31, 
		RULE_pictureLiteral = 32, RULE_pic = 33, RULE_section = 34, RULE_skipLiteral = 35, 
		RULE_group = 36, RULE_primitive = 37, RULE_level66statement = 38, RULE_level88statement = 39, 
		RULE_item = 40;
	private static String[] makeRuleNames() {
		return new String[] {
			"main", "literal", "numericLiteral", "integerLiteral", "booleanLiteral", 
			"identifier", "thru", "values", "valuesFromTo", "valuesFrom", "valuesTo", 
			"specialValues", "sorts", "occursTo", "dependingOn", "indexedBy", "occurs", 
			"redefines", "renames", "usageLiteral", "groupUsageLiteral", "usage", 
			"usageGroup", "separateSign", "justified", "term", "plusMinus", "precision9", 
			"signPrecision9", "alphaX", "alphaN", "alphaA", "pictureLiteral", "pic", 
			"section", "skipLiteral", "group", "primitive", "level66statement", "level88statement", 
			"item"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "'\"'", "','", 
			"'.'", "'('", "'-'", "'+'", "')'", "'''", "'/'", null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "'01'", null, "'66'", "'77'", 
			"'88'", null, null, null, null, null, "'\u001A'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "THRU_OR_THROUGH", "ALL", "ARE", "ASCENDING", "BINARY", "BLANK", 
			"BY", "CHARACTER", "CHARACTERS", "COMP", "COMP_0", "COMP_1", "COMP_2", 
			"COMP_3", "COMP_3U", "COMP_4", "COMP_5", "COMP_9", "COMPUTATIONAL", "COMPUTATIONAL_0", 
			"COMPUTATIONAL_1", "COMPUTATIONAL_2", "COMPUTATIONAL_3", "COMPUTATIONAL_3U", 
			"COMPUTATIONAL_4", "COMPUTATIONAL_5", "COMPUTATIONAL_9", "COPY", "DEPENDING", 
			"DESCENDING", "DISPLAY", "EXTERNAL", "FALSE", "FROM", "HIGH_VALUE", "HIGH_VALUES", 
			"INDEXED", "IS", "JUST", "JUSTIFIED", "KEY", "LEADING", "LEFT", "LOW_VALUE", 
			"LOW_VALUES", "NULL", "NULLS", "NUMBER", "NUMERIC", "OCCURS", "ON", "PACKED_DECIMAL", 
			"PIC", "PICTURE", "QUOTE", "QUOTES", "REDEFINES", "RENAMES", "RIGHT", 
			"SEPARATE", "SKIP1", "SKIP2", "SKIP3", "SIGN", "SPACE", "SPACES", "THROUGH", 
			"THRU", "TIMES", "TO", "TRAILING", "TRUE", "USAGE", "USING", "VALUE", 
			"VALUES", "WHEN", "ZERO", "ZEROS", "ZEROES", "DOUBLEQUOTE", "COMMACHAR", 
			"DOT", "LPARENCHAR", "MINUSCHAR", "PLUSCHAR", "RPARENCHAR", "SINGLEQUOTE", 
			"SLASHCHAR", "TERMINAL", "COMMENT", "NINES", "A_S", "P_S", "X_S", "N_S", 
			"S_S", "Z_S", "V_S", "P_NS", "S_NS", "Z_NS", "V_NS", "PRECISION_9_EXPLICIT_DOT", 
			"PRECISION_9_DECIMAL_SCALED", "PRECISION_9_DECIMAL_WITH_V", "PRECISION_9_SCALED", 
			"PRECISION_9_SCALED_LEAD", "PRECISION_Z_EXPLICIT_DOT", "PRECISION_Z_DECIMAL_SCALED", 
			"PRECISION_Z_SCALED", "LENGTH_TYPE_9", "LENGTH_TYPE_9_1", "LENGTH_TYPE_A", 
			"LENGTH_TYPE_A_1", "LENGTH_TYPE_P", "LENGTH_TYPE_P_1", "LENGTH_TYPE_X", 
			"LENGTH_TYPE_X_1", "LENGTH_TYPE_N", "LENGTH_TYPE_N_1", "LENGTH_TYPE_Z", 
			"LENGTH_TYPE_Z_1", "STRINGLITERAL", "LEVEL_ROOT", "LEVEL_REGULAR", "LEVEL_NUMBER_66", 
			"LEVEL_NUMBER_77", "LEVEL_NUMBER_88", "INTEGERLITERAL", "POSITIVELITERAL", 
			"NUMERICLITERAL", "SINGLE_QUOTED_IDENTIFIER", "IDENTIFIER", "CONTROL_Z", 
			"WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "copybookParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public copybookParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class MainContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(copybookParser.EOF, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode CONTROL_Z() { return getToken(copybookParser.CONTROL_Z, 0); }
		public MainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_main; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitMain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MainContext main() throws RecognitionException {
		MainContext _localctx = new MainContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_main);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(82);
				item();
				}
				}
				setState(85); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SKIP1) | (1L << SKIP2) | (1L << SKIP3))) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & ((1L << (TERMINAL - 90)) | (1L << (COMMENT - 90)) | (1L << (LEVEL_ROOT - 90)) | (1L << (LEVEL_REGULAR - 90)) | (1L << (LEVEL_NUMBER_66 - 90)) | (1L << (LEVEL_NUMBER_88 - 90)))) != 0) );
			setState(88);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONTROL_Z) {
				{
				setState(87);
				match(CONTROL_Z);
				}
			}

			setState(90);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode STRINGLITERAL() { return getToken(copybookParser.STRINGLITERAL, 0); }
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public SpecialValuesContext specialValues() {
			return getRuleContext(SpecialValuesContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_literal);
		try {
			setState(96);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(92);
				match(STRINGLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				numericLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(94);
				booleanLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(95);
				specialValues();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericLiteralContext extends ParserRuleContext {
		public TerminalNode NUMERICLITERAL() { return getToken(copybookParser.NUMERICLITERAL, 0); }
		public PlusMinusContext plusMinus() {
			return getRuleContext(PlusMinusContext.class,0);
		}
		public TerminalNode ZERO() { return getToken(copybookParser.ZERO, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public TerminalNode PRECISION_9_EXPLICIT_DOT() { return getToken(copybookParser.PRECISION_9_EXPLICIT_DOT, 0); }
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_numericLiteral);
		int _la;
		try {
			setState(111);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(98);
					plusMinus();
					}
				}

				setState(101);
				match(NUMERICLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(102);
				match(ZERO);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(103);
					plusMinus();
					}
				}

				setState(106);
				integerLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(108);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(107);
					plusMinus();
					}
				}

				setState(110);
				match(PRECISION_9_EXPLICIT_DOT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerLiteralContext extends ParserRuleContext {
		public TerminalNode INTEGERLITERAL() { return getToken(copybookParser.INTEGERLITERAL, 0); }
		public TerminalNode NINES() { return getToken(copybookParser.NINES, 0); }
		public TerminalNode LEVEL_ROOT() { return getToken(copybookParser.LEVEL_ROOT, 0); }
		public TerminalNode LEVEL_REGULAR() { return getToken(copybookParser.LEVEL_REGULAR, 0); }
		public TerminalNode LEVEL_NUMBER_66() { return getToken(copybookParser.LEVEL_NUMBER_66, 0); }
		public TerminalNode LEVEL_NUMBER_77() { return getToken(copybookParser.LEVEL_NUMBER_77, 0); }
		public TerminalNode LEVEL_NUMBER_88() { return getToken(copybookParser.LEVEL_NUMBER_88, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			_la = _input.LA(1);
			if ( !(((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & ((1L << (NINES - 92)) | (1L << (LEVEL_ROOT - 92)) | (1L << (LEVEL_REGULAR - 92)) | (1L << (LEVEL_NUMBER_66 - 92)) | (1L << (LEVEL_NUMBER_77 - 92)) | (1L << (LEVEL_NUMBER_88 - 92)) | (1L << (INTEGERLITERAL - 92)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(copybookParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(copybookParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(copybookParser.IDENTIFIER, 0); }
		public TerminalNode THRU_OR_THROUGH() { return getToken(copybookParser.THRU_OR_THROUGH, 0); }
		public TerminalNode A_S() { return getToken(copybookParser.A_S, 0); }
		public TerminalNode P_S() { return getToken(copybookParser.P_S, 0); }
		public TerminalNode P_NS() { return getToken(copybookParser.P_NS, 0); }
		public TerminalNode X_S() { return getToken(copybookParser.X_S, 0); }
		public TerminalNode N_S() { return getToken(copybookParser.N_S, 0); }
		public TerminalNode S_S() { return getToken(copybookParser.S_S, 0); }
		public TerminalNode S_NS() { return getToken(copybookParser.S_NS, 0); }
		public TerminalNode Z_S() { return getToken(copybookParser.Z_S, 0); }
		public TerminalNode Z_NS() { return getToken(copybookParser.Z_NS, 0); }
		public TerminalNode V_S() { return getToken(copybookParser.V_S, 0); }
		public TerminalNode V_NS() { return getToken(copybookParser.V_NS, 0); }
		public TerminalNode SINGLE_QUOTED_IDENTIFIER() { return getToken(copybookParser.SINGLE_QUOTED_IDENTIFIER, 0); }
		public TerminalNode SIGN() { return getToken(copybookParser.SIGN, 0); }
		public TerminalNode LEADING() { return getToken(copybookParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(copybookParser.TRAILING, 0); }
		public TerminalNode SEPARATE() { return getToken(copybookParser.SEPARATE, 0); }
		public TerminalNode CHARACTER() { return getToken(copybookParser.CHARACTER, 0); }
		public TerminalNode ASCENDING() { return getToken(copybookParser.ASCENDING, 0); }
		public TerminalNode DESCENDING() { return getToken(copybookParser.DESCENDING, 0); }
		public TerminalNode KEY() { return getToken(copybookParser.KEY, 0); }
		public TerminalNode BINARY() { return getToken(copybookParser.BINARY, 0); }
		public TerminalNode DISPLAY() { return getToken(copybookParser.DISPLAY, 0); }
		public TerminalNode PACKED_DECIMAL() { return getToken(copybookParser.PACKED_DECIMAL, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybookParser.COMPUTATIONAL, 0); }
		public TerminalNode COMPUTATIONAL_0() { return getToken(copybookParser.COMPUTATIONAL_0, 0); }
		public TerminalNode COMPUTATIONAL_1() { return getToken(copybookParser.COMPUTATIONAL_1, 0); }
		public TerminalNode COMPUTATIONAL_2() { return getToken(copybookParser.COMPUTATIONAL_2, 0); }
		public TerminalNode COMPUTATIONAL_3() { return getToken(copybookParser.COMPUTATIONAL_3, 0); }
		public TerminalNode COMPUTATIONAL_3U() { return getToken(copybookParser.COMPUTATIONAL_3U, 0); }
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybookParser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybookParser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL_9() { return getToken(copybookParser.COMPUTATIONAL_9, 0); }
		public TerminalNode COMP() { return getToken(copybookParser.COMP, 0); }
		public TerminalNode COMP_0() { return getToken(copybookParser.COMP_0, 0); }
		public TerminalNode COMP_1() { return getToken(copybookParser.COMP_1, 0); }
		public TerminalNode COMP_2() { return getToken(copybookParser.COMP_2, 0); }
		public TerminalNode COMP_3() { return getToken(copybookParser.COMP_3, 0); }
		public TerminalNode COMP_3U() { return getToken(copybookParser.COMP_3U, 0); }
		public TerminalNode COMP_4() { return getToken(copybookParser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybookParser.COMP_5, 0); }
		public TerminalNode COMP_9() { return getToken(copybookParser.COMP_9, 0); }
		public TerminalNode REDEFINES() { return getToken(copybookParser.REDEFINES, 0); }
		public TerminalNode RENAMES() { return getToken(copybookParser.RENAMES, 0); }
		public TerminalNode VALUE() { return getToken(copybookParser.VALUE, 0); }
		public TerminalNode VALUES() { return getToken(copybookParser.VALUES, 0); }
		public TerminalNode OCCURS() { return getToken(copybookParser.OCCURS, 0); }
		public TerminalNode TIMES() { return getToken(copybookParser.TIMES, 0); }
		public TerminalNode DEPENDING() { return getToken(copybookParser.DEPENDING, 0); }
		public TerminalNode INDEXED() { return getToken(copybookParser.INDEXED, 0); }
		public TerminalNode BLANK() { return getToken(copybookParser.BLANK, 0); }
		public TerminalNode ZERO() { return getToken(copybookParser.ZERO, 0); }
		public TerminalNode ZEROS() { return getToken(copybookParser.ZEROS, 0); }
		public TerminalNode ZEROES() { return getToken(copybookParser.ZEROES, 0); }
		public TerminalNode SPACE() { return getToken(copybookParser.SPACE, 0); }
		public TerminalNode SPACES() { return getToken(copybookParser.SPACES, 0); }
		public TerminalNode HIGH_VALUE() { return getToken(copybookParser.HIGH_VALUE, 0); }
		public TerminalNode HIGH_VALUES() { return getToken(copybookParser.HIGH_VALUES, 0); }
		public TerminalNode LOW_VALUE() { return getToken(copybookParser.LOW_VALUE, 0); }
		public TerminalNode LOW_VALUES() { return getToken(copybookParser.LOW_VALUES, 0); }
		public TerminalNode NULL() { return getToken(copybookParser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(copybookParser.NULLS, 0); }
		public TerminalNode QUOTE() { return getToken(copybookParser.QUOTE, 0); }
		public TerminalNode QUOTES() { return getToken(copybookParser.QUOTES, 0); }
		public TerminalNode JUSTIFIED() { return getToken(copybookParser.JUSTIFIED, 0); }
		public TerminalNode JUST() { return getToken(copybookParser.JUST, 0); }
		public TerminalNode RIGHT() { return getToken(copybookParser.RIGHT, 0); }
		public TerminalNode PICTURE() { return getToken(copybookParser.PICTURE, 0); }
		public TerminalNode PIC() { return getToken(copybookParser.PIC, 0); }
		public TerminalNode TRUE() { return getToken(copybookParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(copybookParser.FALSE, 0); }
		public TerminalNode ARE() { return getToken(copybookParser.ARE, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public TerminalNode ON() { return getToken(copybookParser.ON, 0); }
		public TerminalNode BY() { return getToken(copybookParser.BY, 0); }
		public TerminalNode WHEN() { return getToken(copybookParser.WHEN, 0); }
		public TerminalNode TO() { return getToken(copybookParser.TO, 0); }
		public TerminalNode ALL() { return getToken(copybookParser.ALL, 0); }
		public TerminalNode USAGE() { return getToken(copybookParser.USAGE, 0); }
		public TerminalNode FROM() { return getToken(copybookParser.FROM, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_identifier);
		try {
			setState(199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(117);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				match(THRU_OR_THROUGH);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				match(A_S);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(120);
				match(P_S);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(121);
				match(P_NS);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(122);
				match(X_S);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(124);
				match(N_S);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(125);
				match(S_S);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(126);
				match(S_NS);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(127);
				match(Z_S);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(128);
				match(Z_NS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(129);
				match(V_S);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(130);
				match(V_NS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(131);
				match(SINGLE_QUOTED_IDENTIFIER);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(132);
				match(SIGN);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(133);
				match(LEADING);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(134);
				match(TRAILING);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(135);
				match(SEPARATE);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(136);
				match(CHARACTER);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(137);
				match(ASCENDING);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(138);
				match(DESCENDING);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(139);
				match(KEY);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(140);
				match(BINARY);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(141);
				match(DISPLAY);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(142);
				match(PACKED_DECIMAL);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(143);
				match(COMPUTATIONAL);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(144);
				match(COMPUTATIONAL_0);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(145);
				match(COMPUTATIONAL_1);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(146);
				match(COMPUTATIONAL_2);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(147);
				match(COMPUTATIONAL_3);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(148);
				match(COMPUTATIONAL_3U);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(149);
				match(COMPUTATIONAL_4);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(150);
				match(COMPUTATIONAL_5);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(151);
				match(COMPUTATIONAL_9);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(152);
				match(COMP);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(153);
				match(COMP_0);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(154);
				match(COMP_1);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(155);
				match(COMP_2);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(156);
				match(COMP_3);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(157);
				match(COMP_3U);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(158);
				match(COMP_4);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(159);
				match(COMP_5);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(160);
				match(COMP_9);
				}
				break;
			case 45:
				enterOuterAlt(_localctx, 45);
				{
				setState(161);
				match(REDEFINES);
				}
				break;
			case 46:
				enterOuterAlt(_localctx, 46);
				{
				setState(162);
				match(RENAMES);
				}
				break;
			case 47:
				enterOuterAlt(_localctx, 47);
				{
				setState(163);
				match(VALUE);
				}
				break;
			case 48:
				enterOuterAlt(_localctx, 48);
				{
				setState(164);
				match(VALUES);
				}
				break;
			case 49:
				enterOuterAlt(_localctx, 49);
				{
				setState(165);
				match(OCCURS);
				}
				break;
			case 50:
				enterOuterAlt(_localctx, 50);
				{
				setState(166);
				match(TIMES);
				}
				break;
			case 51:
				enterOuterAlt(_localctx, 51);
				{
				setState(167);
				match(DEPENDING);
				}
				break;
			case 52:
				enterOuterAlt(_localctx, 52);
				{
				setState(168);
				match(INDEXED);
				}
				break;
			case 53:
				enterOuterAlt(_localctx, 53);
				{
				setState(169);
				match(BLANK);
				}
				break;
			case 54:
				enterOuterAlt(_localctx, 54);
				{
				setState(170);
				match(ZERO);
				}
				break;
			case 55:
				enterOuterAlt(_localctx, 55);
				{
				setState(171);
				match(ZEROS);
				}
				break;
			case 56:
				enterOuterAlt(_localctx, 56);
				{
				setState(172);
				match(ZEROES);
				}
				break;
			case 57:
				enterOuterAlt(_localctx, 57);
				{
				setState(173);
				match(SPACE);
				}
				break;
			case 58:
				enterOuterAlt(_localctx, 58);
				{
				setState(174);
				match(SPACES);
				}
				break;
			case 59:
				enterOuterAlt(_localctx, 59);
				{
				setState(175);
				match(HIGH_VALUE);
				}
				break;
			case 60:
				enterOuterAlt(_localctx, 60);
				{
				setState(176);
				match(HIGH_VALUES);
				}
				break;
			case 61:
				enterOuterAlt(_localctx, 61);
				{
				setState(177);
				match(LOW_VALUE);
				}
				break;
			case 62:
				enterOuterAlt(_localctx, 62);
				{
				setState(178);
				match(LOW_VALUES);
				}
				break;
			case 63:
				enterOuterAlt(_localctx, 63);
				{
				setState(179);
				match(NULL);
				}
				break;
			case 64:
				enterOuterAlt(_localctx, 64);
				{
				setState(180);
				match(NULLS);
				}
				break;
			case 65:
				enterOuterAlt(_localctx, 65);
				{
				setState(181);
				match(QUOTE);
				}
				break;
			case 66:
				enterOuterAlt(_localctx, 66);
				{
				setState(182);
				match(QUOTES);
				}
				break;
			case 67:
				enterOuterAlt(_localctx, 67);
				{
				setState(183);
				match(JUSTIFIED);
				}
				break;
			case 68:
				enterOuterAlt(_localctx, 68);
				{
				setState(184);
				match(JUST);
				}
				break;
			case 69:
				enterOuterAlt(_localctx, 69);
				{
				setState(185);
				match(RIGHT);
				}
				break;
			case 70:
				enterOuterAlt(_localctx, 70);
				{
				setState(186);
				match(PICTURE);
				}
				break;
			case 71:
				enterOuterAlt(_localctx, 71);
				{
				setState(187);
				match(PIC);
				}
				break;
			case 72:
				enterOuterAlt(_localctx, 72);
				{
				setState(188);
				match(TRUE);
				}
				break;
			case 73:
				enterOuterAlt(_localctx, 73);
				{
				setState(189);
				match(FALSE);
				}
				break;
			case 74:
				enterOuterAlt(_localctx, 74);
				{
				setState(190);
				match(ARE);
				}
				break;
			case 75:
				enterOuterAlt(_localctx, 75);
				{
				setState(191);
				match(IS);
				}
				break;
			case 76:
				enterOuterAlt(_localctx, 76);
				{
				setState(192);
				match(ON);
				}
				break;
			case 77:
				enterOuterAlt(_localctx, 77);
				{
				setState(193);
				match(BY);
				}
				break;
			case 78:
				enterOuterAlt(_localctx, 78);
				{
				setState(194);
				match(WHEN);
				}
				break;
			case 79:
				enterOuterAlt(_localctx, 79);
				{
				setState(195);
				match(TO);
				}
				break;
			case 80:
				enterOuterAlt(_localctx, 80);
				{
				setState(196);
				match(ALL);
				}
				break;
			case 81:
				enterOuterAlt(_localctx, 81);
				{
				setState(197);
				match(USAGE);
				}
				break;
			case 82:
				enterOuterAlt(_localctx, 82);
				{
				setState(198);
				match(FROM);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ThruContext extends ParserRuleContext {
		public TerminalNode THRU_OR_THROUGH() { return getToken(copybookParser.THRU_OR_THROUGH, 0); }
		public ThruContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_thru; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitThru(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ThruContext thru() throws RecognitionException {
		ThruContext _localctx = new ThruContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_thru);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			match(THRU_OR_THROUGH);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuesContext extends ParserRuleContext {
		public List<ValuesFromToContext> valuesFromTo() {
			return getRuleContexts(ValuesFromToContext.class);
		}
		public ValuesFromToContext valuesFromTo(int i) {
			return getRuleContext(ValuesFromToContext.class,i);
		}
		public TerminalNode VALUE() { return getToken(copybookParser.VALUE, 0); }
		public TerminalNode VALUES() { return getToken(copybookParser.VALUES, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public TerminalNode ARE() { return getToken(copybookParser.ARE, 0); }
		public List<TerminalNode> COMMACHAR() { return getTokens(copybookParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(copybookParser.COMMACHAR, i);
		}
		public ValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesContext values() throws RecognitionException {
		ValuesContext _localctx = new ValuesContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_values);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
				{
				setState(203);
				match(VALUE);
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(204);
					match(IS);
					}
				}

				}
				break;
			case VALUES:
				{
				setState(207);
				match(VALUES);
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ARE) {
					{
					setState(208);
					match(ARE);
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(213);
			valuesFromTo();
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALL) | (1L << FALSE) | (1L << HIGH_VALUE) | (1L << HIGH_VALUES) | (1L << LOW_VALUE) | (1L << LOW_VALUES) | (1L << NULL) | (1L << NULLS) | (1L << QUOTE) | (1L << QUOTES))) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (SPACE - 65)) | (1L << (SPACES - 65)) | (1L << (TRUE - 65)) | (1L << (ZERO - 65)) | (1L << (ZEROS - 65)) | (1L << (ZEROES - 65)) | (1L << (COMMACHAR - 65)) | (1L << (MINUSCHAR - 65)) | (1L << (PLUSCHAR - 65)) | (1L << (NINES - 65)) | (1L << (PRECISION_9_EXPLICIT_DOT - 65)) | (1L << (STRINGLITERAL - 65)) | (1L << (LEVEL_ROOT - 65)) | (1L << (LEVEL_REGULAR - 65)) | (1L << (LEVEL_NUMBER_66 - 65)) | (1L << (LEVEL_NUMBER_77 - 65)))) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & ((1L << (LEVEL_NUMBER_88 - 129)) | (1L << (INTEGERLITERAL - 129)) | (1L << (NUMERICLITERAL - 129)))) != 0)) {
				{
				{
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(214);
					match(COMMACHAR);
					}
				}

				setState(217);
				valuesFromTo();
				}
				}
				setState(222);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuesFromToContext extends ParserRuleContext {
		public ValuesFromContext valuesFrom() {
			return getRuleContext(ValuesFromContext.class,0);
		}
		public ValuesToContext valuesTo() {
			return getRuleContext(ValuesToContext.class,0);
		}
		public ValuesFromToContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuesFromTo; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitValuesFromTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesFromToContext valuesFromTo() throws RecognitionException {
		ValuesFromToContext _localctx = new ValuesFromToContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_valuesFromTo);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			valuesFrom();
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(224);
				valuesTo();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuesFromContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ValuesFromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuesFrom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitValuesFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesFromContext valuesFrom() throws RecognitionException {
		ValuesFromContext _localctx = new ValuesFromContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_valuesFrom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			literal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuesToContext extends ParserRuleContext {
		public ThruContext thru() {
			return getRuleContext(ThruContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ValuesToContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuesTo; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitValuesTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesToContext valuesTo() throws RecognitionException {
		ValuesToContext _localctx = new ValuesToContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_valuesTo);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			thru();
			setState(230);
			literal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecialValuesContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(copybookParser.ALL, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode HIGH_VALUE() { return getToken(copybookParser.HIGH_VALUE, 0); }
		public TerminalNode HIGH_VALUES() { return getToken(copybookParser.HIGH_VALUES, 0); }
		public TerminalNode LOW_VALUE() { return getToken(copybookParser.LOW_VALUE, 0); }
		public TerminalNode LOW_VALUES() { return getToken(copybookParser.LOW_VALUES, 0); }
		public TerminalNode NULL() { return getToken(copybookParser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(copybookParser.NULLS, 0); }
		public TerminalNode QUOTE() { return getToken(copybookParser.QUOTE, 0); }
		public TerminalNode QUOTES() { return getToken(copybookParser.QUOTES, 0); }
		public TerminalNode SPACE() { return getToken(copybookParser.SPACE, 0); }
		public TerminalNode SPACES() { return getToken(copybookParser.SPACES, 0); }
		public TerminalNode ZERO() { return getToken(copybookParser.ZERO, 0); }
		public TerminalNode ZEROS() { return getToken(copybookParser.ZEROS, 0); }
		public TerminalNode ZEROES() { return getToken(copybookParser.ZEROES, 0); }
		public SpecialValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialValues; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitSpecialValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialValuesContext specialValues() throws RecognitionException {
		SpecialValuesContext _localctx = new SpecialValuesContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_specialValues);
		try {
			setState(247);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(232);
				match(ALL);
				setState(233);
				literal();
				}
				break;
			case HIGH_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(234);
				match(HIGH_VALUE);
				}
				break;
			case HIGH_VALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(235);
				match(HIGH_VALUES);
				}
				break;
			case LOW_VALUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(236);
				match(LOW_VALUE);
				}
				break;
			case LOW_VALUES:
				enterOuterAlt(_localctx, 5);
				{
				setState(237);
				match(LOW_VALUES);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(238);
				match(NULL);
				}
				break;
			case NULLS:
				enterOuterAlt(_localctx, 7);
				{
				setState(239);
				match(NULLS);
				}
				break;
			case QUOTE:
				enterOuterAlt(_localctx, 8);
				{
				setState(240);
				match(QUOTE);
				}
				break;
			case QUOTES:
				enterOuterAlt(_localctx, 9);
				{
				setState(241);
				match(QUOTES);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 10);
				{
				setState(242);
				match(SPACE);
				}
				break;
			case SPACES:
				enterOuterAlt(_localctx, 11);
				{
				setState(243);
				match(SPACES);
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 12);
				{
				setState(244);
				match(ZERO);
				}
				break;
			case ZEROS:
				enterOuterAlt(_localctx, 13);
				{
				setState(245);
				match(ZEROS);
				}
				break;
			case ZEROES:
				enterOuterAlt(_localctx, 14);
				{
				setState(246);
				match(ZEROES);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortsContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASCENDING() { return getToken(copybookParser.ASCENDING, 0); }
		public TerminalNode DESCENDING() { return getToken(copybookParser.DESCENDING, 0); }
		public TerminalNode KEY() { return getToken(copybookParser.KEY, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public SortsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sorts; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitSorts(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortsContext sorts() throws RecognitionException {
		SortsContext _localctx = new SortsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sorts);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(249);
			_la = _input.LA(1);
			if ( !(_la==ASCENDING || _la==DESCENDING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(251);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(250);
				match(KEY);
				}
				break;
			}
			setState(254);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(253);
				match(IS);
				}
				break;
			}
			setState(256);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OccursToContext extends ParserRuleContext {
		public TerminalNode TO() { return getToken(copybookParser.TO, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public OccursToContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_occursTo; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitOccursTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OccursToContext occursTo() throws RecognitionException {
		OccursToContext _localctx = new OccursToContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_occursTo);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			match(TO);
			setState(259);
			integerLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DependingOnContext extends ParserRuleContext {
		public TerminalNode DEPENDING() { return getToken(copybookParser.DEPENDING, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ON() { return getToken(copybookParser.ON, 0); }
		public DependingOnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dependingOn; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitDependingOn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DependingOnContext dependingOn() throws RecognitionException {
		DependingOnContext _localctx = new DependingOnContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_dependingOn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
			match(DEPENDING);
			setState(263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(262);
				match(ON);
				}
				break;
			}
			setState(265);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexedByContext extends ParserRuleContext {
		public TerminalNode INDEXED() { return getToken(copybookParser.INDEXED, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode BY() { return getToken(copybookParser.BY, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(copybookParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(copybookParser.IDENTIFIER, i);
		}
		public List<TerminalNode> COMMACHAR() { return getTokens(copybookParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(copybookParser.COMMACHAR, i);
		}
		public IndexedByContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexedBy; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitIndexedBy(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexedByContext indexedBy() throws RecognitionException {
		IndexedByContext _localctx = new IndexedByContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_indexedBy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			match(INDEXED);
			setState(269);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(268);
				match(BY);
				}
				break;
			}
			setState(271);
			identifier();
			setState(278);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMACHAR || _la==IDENTIFIER) {
				{
				{
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(272);
					match(COMMACHAR);
					}
				}

				setState(275);
				match(IDENTIFIER);
				}
				}
				setState(280);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OccursContext extends ParserRuleContext {
		public TerminalNode OCCURS() { return getToken(copybookParser.OCCURS, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public OccursToContext occursTo() {
			return getRuleContext(OccursToContext.class,0);
		}
		public TerminalNode TIMES() { return getToken(copybookParser.TIMES, 0); }
		public DependingOnContext dependingOn() {
			return getRuleContext(DependingOnContext.class,0);
		}
		public SortsContext sorts() {
			return getRuleContext(SortsContext.class,0);
		}
		public IndexedByContext indexedBy() {
			return getRuleContext(IndexedByContext.class,0);
		}
		public OccursContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_occurs; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitOccurs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OccursContext occurs() throws RecognitionException {
		OccursContext _localctx = new OccursContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_occurs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(OCCURS);
			setState(282);
			integerLiteral();
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(283);
				occursTo();
				}
			}

			setState(287);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TIMES) {
				{
				setState(286);
				match(TIMES);
				}
			}

			setState(290);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEPENDING) {
				{
				setState(289);
				dependingOn();
				}
			}

			setState(293);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASCENDING || _la==DESCENDING) {
				{
				setState(292);
				sorts();
				}
			}

			setState(296);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEXED) {
				{
				setState(295);
				indexedBy();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RedefinesContext extends ParserRuleContext {
		public TerminalNode REDEFINES() { return getToken(copybookParser.REDEFINES, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RedefinesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_redefines; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitRedefines(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RedefinesContext redefines() throws RecognitionException {
		RedefinesContext _localctx = new RedefinesContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_redefines);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			match(REDEFINES);
			setState(299);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RenamesContext extends ParserRuleContext {
		public TerminalNode RENAMES() { return getToken(copybookParser.RENAMES, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public ThruContext thru() {
			return getRuleContext(ThruContext.class,0);
		}
		public RenamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_renames; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitRenames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RenamesContext renames() throws RecognitionException {
		RenamesContext _localctx = new RenamesContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_renames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
			match(RENAMES);
			setState(302);
			identifier();
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(303);
				thru();
				setState(304);
				identifier();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UsageLiteralContext extends ParserRuleContext {
		public TerminalNode COMPUTATIONAL_0() { return getToken(copybookParser.COMPUTATIONAL_0, 0); }
		public TerminalNode COMPUTATIONAL_1() { return getToken(copybookParser.COMPUTATIONAL_1, 0); }
		public TerminalNode COMPUTATIONAL_2() { return getToken(copybookParser.COMPUTATIONAL_2, 0); }
		public TerminalNode COMPUTATIONAL_3() { return getToken(copybookParser.COMPUTATIONAL_3, 0); }
		public TerminalNode COMPUTATIONAL_3U() { return getToken(copybookParser.COMPUTATIONAL_3U, 0); }
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybookParser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybookParser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL_9() { return getToken(copybookParser.COMPUTATIONAL_9, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybookParser.COMPUTATIONAL, 0); }
		public TerminalNode COMP_0() { return getToken(copybookParser.COMP_0, 0); }
		public TerminalNode COMP_1() { return getToken(copybookParser.COMP_1, 0); }
		public TerminalNode COMP_2() { return getToken(copybookParser.COMP_2, 0); }
		public TerminalNode COMP_3() { return getToken(copybookParser.COMP_3, 0); }
		public TerminalNode COMP_3U() { return getToken(copybookParser.COMP_3U, 0); }
		public TerminalNode COMP_4() { return getToken(copybookParser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybookParser.COMP_5, 0); }
		public TerminalNode COMP_9() { return getToken(copybookParser.COMP_9, 0); }
		public TerminalNode COMP() { return getToken(copybookParser.COMP, 0); }
		public TerminalNode DISPLAY() { return getToken(copybookParser.DISPLAY, 0); }
		public TerminalNode BINARY() { return getToken(copybookParser.BINARY, 0); }
		public TerminalNode PACKED_DECIMAL() { return getToken(copybookParser.PACKED_DECIMAL, 0); }
		public UsageLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usageLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitUsageLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UsageLiteralContext usageLiteral() throws RecognitionException {
		UsageLiteralContext _localctx = new UsageLiteralContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_usageLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupUsageLiteralContext extends ParserRuleContext {
		public TerminalNode COMPUTATIONAL_0() { return getToken(copybookParser.COMPUTATIONAL_0, 0); }
		public TerminalNode COMPUTATIONAL_3() { return getToken(copybookParser.COMPUTATIONAL_3, 0); }
		public TerminalNode COMPUTATIONAL_3U() { return getToken(copybookParser.COMPUTATIONAL_3U, 0); }
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybookParser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybookParser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL_9() { return getToken(copybookParser.COMPUTATIONAL_9, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybookParser.COMPUTATIONAL, 0); }
		public TerminalNode COMP_0() { return getToken(copybookParser.COMP_0, 0); }
		public TerminalNode COMP_3() { return getToken(copybookParser.COMP_3, 0); }
		public TerminalNode COMP_3U() { return getToken(copybookParser.COMP_3U, 0); }
		public TerminalNode COMP_4() { return getToken(copybookParser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybookParser.COMP_5, 0); }
		public TerminalNode COMP_9() { return getToken(copybookParser.COMP_9, 0); }
		public TerminalNode COMP() { return getToken(copybookParser.COMP, 0); }
		public TerminalNode DISPLAY() { return getToken(copybookParser.DISPLAY, 0); }
		public TerminalNode BINARY() { return getToken(copybookParser.BINARY, 0); }
		public TerminalNode PACKED_DECIMAL() { return getToken(copybookParser.PACKED_DECIMAL, 0); }
		public GroupUsageLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupUsageLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitGroupUsageLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupUsageLiteralContext groupUsageLiteral() throws RecognitionException {
		GroupUsageLiteralContext _localctx = new GroupUsageLiteralContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_groupUsageLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UsageContext extends ParserRuleContext {
		public UsageLiteralContext usageLiteral() {
			return getRuleContext(UsageLiteralContext.class,0);
		}
		public TerminalNode USAGE() { return getToken(copybookParser.USAGE, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public UsageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usage; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitUsage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UsageContext usage() throws RecognitionException {
		UsageContext _localctx = new UsageContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_usage);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(312);
				match(USAGE);
				setState(314);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(313);
					match(IS);
					}
				}

				}
			}

			setState(318);
			usageLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UsageGroupContext extends ParserRuleContext {
		public GroupUsageLiteralContext groupUsageLiteral() {
			return getRuleContext(GroupUsageLiteralContext.class,0);
		}
		public TerminalNode USAGE() { return getToken(copybookParser.USAGE, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public UsageGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usageGroup; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitUsageGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UsageGroupContext usageGroup() throws RecognitionException {
		UsageGroupContext _localctx = new UsageGroupContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_usageGroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(320);
				match(USAGE);
				setState(322);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(321);
					match(IS);
					}
				}

				}
			}

			setState(326);
			groupUsageLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SeparateSignContext extends ParserRuleContext {
		public TerminalNode SIGN() { return getToken(copybookParser.SIGN, 0); }
		public TerminalNode LEADING() { return getToken(copybookParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(copybookParser.TRAILING, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public TerminalNode SEPARATE() { return getToken(copybookParser.SEPARATE, 0); }
		public TerminalNode CHARACTER() { return getToken(copybookParser.CHARACTER, 0); }
		public SeparateSignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_separateSign; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitSeparateSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SeparateSignContext separateSign() throws RecognitionException {
		SeparateSignContext _localctx = new SeparateSignContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_separateSign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
			match(SIGN);
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(329);
				match(IS);
				}
			}

			setState(332);
			_la = _input.LA(1);
			if ( !(_la==LEADING || _la==TRAILING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(334);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEPARATE) {
				{
				setState(333);
				match(SEPARATE);
				}
			}

			setState(337);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CHARACTER) {
				{
				setState(336);
				match(CHARACTER);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JustifiedContext extends ParserRuleContext {
		public TerminalNode JUSTIFIED() { return getToken(copybookParser.JUSTIFIED, 0); }
		public TerminalNode JUST() { return getToken(copybookParser.JUST, 0); }
		public TerminalNode RIGHT() { return getToken(copybookParser.RIGHT, 0); }
		public JustifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_justified; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitJustified(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JustifiedContext justified() throws RecognitionException {
		JustifiedContext _localctx = new JustifiedContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_justified);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(339);
			_la = _input.LA(1);
			if ( !(_la==JUST || _la==JUSTIFIED) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RIGHT) {
				{
				setState(340);
				match(RIGHT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TerminalNode TERMINAL() { return getToken(copybookParser.TERMINAL, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_term);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
			match(TERMINAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PlusMinusContext extends ParserRuleContext {
		public PlusMinusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_plusMinus; }
	 
		public PlusMinusContext() { }
		public void copyFrom(PlusMinusContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MinusContext extends PlusMinusContext {
		public TerminalNode MINUSCHAR() { return getToken(copybookParser.MINUSCHAR, 0); }
		public MinusContext(PlusMinusContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitMinus(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PlusContext extends PlusMinusContext {
		public TerminalNode PLUSCHAR() { return getToken(copybookParser.PLUSCHAR, 0); }
		public PlusContext(PlusMinusContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPlus(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PlusMinusContext plusMinus() throws RecognitionException {
		PlusMinusContext _localctx = new PlusMinusContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_plusMinus);
		try {
			setState(347);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUSCHAR:
				_localctx = new PlusContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				match(PLUSCHAR);
				}
				break;
			case MINUSCHAR:
				_localctx = new MinusContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(346);
				match(MINUSCHAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Precision9Context extends ParserRuleContext {
		public Precision9Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_precision9; }
	 
		public Precision9Context() { }
		public void copyFrom(Precision9Context ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Precision9ScaledLeadContext extends Precision9Context {
		public TerminalNode PRECISION_9_SCALED_LEAD() { return getToken(copybookParser.PRECISION_9_SCALED_LEAD, 0); }
		public Precision9ScaledLeadContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9ScaledLead(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrecisionZExplicitDotContext extends Precision9Context {
		public TerminalNode PRECISION_Z_EXPLICIT_DOT() { return getToken(copybookParser.PRECISION_Z_EXPLICIT_DOT, 0); }
		public PrecisionZExplicitDotContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecisionZExplicitDot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9ExplicitDotContext extends Precision9Context {
		public TerminalNode PRECISION_9_EXPLICIT_DOT() { return getToken(copybookParser.PRECISION_9_EXPLICIT_DOT, 0); }
		public Precision9ExplicitDotContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9ExplicitDot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9DecimalScaledWithVContext extends Precision9Context {
		public TerminalNode PRECISION_9_DECIMAL_WITH_V() { return getToken(copybookParser.PRECISION_9_DECIMAL_WITH_V, 0); }
		public Precision9DecimalScaledWithVContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9DecimalScaledWithV(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9ScaledContext extends Precision9Context {
		public TerminalNode PRECISION_9_SCALED() { return getToken(copybookParser.PRECISION_9_SCALED, 0); }
		public Precision9ScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Scaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9DecimalScaledContext extends Precision9Context {
		public TerminalNode PRECISION_9_DECIMAL_SCALED() { return getToken(copybookParser.PRECISION_9_DECIMAL_SCALED, 0); }
		public Precision9DecimalScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9DecimalScaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9ZsContext extends Precision9Context {
		public TerminalNode Z_S() { return getToken(copybookParser.Z_S, 0); }
		public Precision9ZsContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Zs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9NinesContext extends Precision9Context {
		public TerminalNode NINES() { return getToken(copybookParser.NINES, 0); }
		public Precision9NinesContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Nines(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9PsContext extends Precision9Context {
		public TerminalNode P_S() { return getToken(copybookParser.P_S, 0); }
		public Precision9PsContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Ps(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrecisionZScaledContext extends Precision9Context {
		public TerminalNode PRECISION_Z_SCALED() { return getToken(copybookParser.PRECISION_Z_SCALED, 0); }
		public PrecisionZScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecisionZScaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9VsContext extends Precision9Context {
		public TerminalNode V_S() { return getToken(copybookParser.V_S, 0); }
		public Precision9VsContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Vs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrecisionZDecimalScaledContext extends Precision9Context {
		public TerminalNode PRECISION_Z_DECIMAL_SCALED() { return getToken(copybookParser.PRECISION_Z_DECIMAL_SCALED, 0); }
		public PrecisionZDecimalScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecisionZDecimalScaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision9SsContext extends Precision9Context {
		public TerminalNode S_S() { return getToken(copybookParser.S_S, 0); }
		public Precision9SsContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Ss(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Precision9Context precision9() throws RecognitionException {
		Precision9Context _localctx = new Precision9Context(_ctx, getState());
		enterRule(_localctx, 54, RULE_precision9);
		try {
			setState(362);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NINES:
				_localctx = new Precision9NinesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(349);
				match(NINES);
				}
				break;
			case S_S:
				_localctx = new Precision9SsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(350);
				match(S_S);
				}
				break;
			case P_S:
				_localctx = new Precision9PsContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(351);
				match(P_S);
				}
				break;
			case Z_S:
				_localctx = new Precision9ZsContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(352);
				match(Z_S);
				}
				break;
			case V_S:
				_localctx = new Precision9VsContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(353);
				match(V_S);
				}
				break;
			case PRECISION_9_EXPLICIT_DOT:
				_localctx = new Precision9ExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(354);
				match(PRECISION_9_EXPLICIT_DOT);
				}
				break;
			case PRECISION_9_DECIMAL_SCALED:
				_localctx = new Precision9DecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(355);
				match(PRECISION_9_DECIMAL_SCALED);
				}
				break;
			case PRECISION_9_DECIMAL_WITH_V:
				_localctx = new Precision9DecimalScaledWithVContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(356);
				match(PRECISION_9_DECIMAL_WITH_V);
				}
				break;
			case PRECISION_9_SCALED:
				_localctx = new Precision9ScaledContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(357);
				match(PRECISION_9_SCALED);
				}
				break;
			case PRECISION_9_SCALED_LEAD:
				_localctx = new Precision9ScaledLeadContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(358);
				match(PRECISION_9_SCALED_LEAD);
				}
				break;
			case PRECISION_Z_EXPLICIT_DOT:
				_localctx = new PrecisionZExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(359);
				match(PRECISION_Z_EXPLICIT_DOT);
				}
				break;
			case PRECISION_Z_DECIMAL_SCALED:
				_localctx = new PrecisionZDecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(360);
				match(PRECISION_Z_DECIMAL_SCALED);
				}
				break;
			case PRECISION_Z_SCALED:
				_localctx = new PrecisionZScaledContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(361);
				match(PRECISION_Z_SCALED);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SignPrecision9Context extends ParserRuleContext {
		public SignPrecision9Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signPrecision9; }
	 
		public SignPrecision9Context() { }
		public void copyFrom(SignPrecision9Context ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TrailingSignContext extends SignPrecision9Context {
		public Precision9Context precision9() {
			return getRuleContext(Precision9Context.class,0);
		}
		public PlusMinusContext plusMinus() {
			return getRuleContext(PlusMinusContext.class,0);
		}
		public TrailingSignContext(SignPrecision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitTrailingSign(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LeadingSignContext extends SignPrecision9Context {
		public Precision9Context precision9() {
			return getRuleContext(Precision9Context.class,0);
		}
		public PlusMinusContext plusMinus() {
			return getRuleContext(PlusMinusContext.class,0);
		}
		public LeadingSignContext(SignPrecision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitLeadingSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignPrecision9Context signPrecision9() throws RecognitionException {
		SignPrecision9Context _localctx = new SignPrecision9Context(_ctx, getState());
		enterRule(_localctx, 56, RULE_signPrecision9);
		int _la;
		try {
			setState(371);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new LeadingSignContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(365);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(364);
					plusMinus();
					}
				}

				setState(367);
				precision9();
				}
				}
				break;
			case 2:
				_localctx = new TrailingSignContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(368);
				precision9();
				setState(369);
				plusMinus();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphaXContext extends ParserRuleContext {
		public TerminalNode X_S() { return getToken(copybookParser.X_S, 0); }
		public TerminalNode LENGTH_TYPE_X() { return getToken(copybookParser.LENGTH_TYPE_X, 0); }
		public AlphaXContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alphaX; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitAlphaX(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlphaXContext alphaX() throws RecognitionException {
		AlphaXContext _localctx = new AlphaXContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_alphaX);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(373);
			_la = _input.LA(1);
			if ( !(_la==X_S || _la==LENGTH_TYPE_X) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphaNContext extends ParserRuleContext {
		public TerminalNode N_S() { return getToken(copybookParser.N_S, 0); }
		public TerminalNode LENGTH_TYPE_N() { return getToken(copybookParser.LENGTH_TYPE_N, 0); }
		public AlphaNContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alphaN; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitAlphaN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlphaNContext alphaN() throws RecognitionException {
		AlphaNContext _localctx = new AlphaNContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_alphaN);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(375);
			_la = _input.LA(1);
			if ( !(_la==N_S || _la==LENGTH_TYPE_N) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphaAContext extends ParserRuleContext {
		public TerminalNode A_S() { return getToken(copybookParser.A_S, 0); }
		public TerminalNode LENGTH_TYPE_A() { return getToken(copybookParser.LENGTH_TYPE_A, 0); }
		public AlphaAContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alphaA; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitAlphaA(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlphaAContext alphaA() throws RecognitionException {
		AlphaAContext _localctx = new AlphaAContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_alphaA);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
			_la = _input.LA(1);
			if ( !(_la==A_S || _la==LENGTH_TYPE_A) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PictureLiteralContext extends ParserRuleContext {
		public TerminalNode PICTURE() { return getToken(copybookParser.PICTURE, 0); }
		public TerminalNode PIC() { return getToken(copybookParser.PIC, 0); }
		public PictureLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pictureLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPictureLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PictureLiteralContext pictureLiteral() throws RecognitionException {
		PictureLiteralContext _localctx = new PictureLiteralContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_pictureLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			_la = _input.LA(1);
			if ( !(_la==PIC || _la==PICTURE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PicContext extends ParserRuleContext {
		public PictureLiteralContext pictureLiteral() {
			return getRuleContext(PictureLiteralContext.class,0);
		}
		public AlphaXContext alphaX() {
			return getRuleContext(AlphaXContext.class,0);
		}
		public AlphaAContext alphaA() {
			return getRuleContext(AlphaAContext.class,0);
		}
		public AlphaNContext alphaN() {
			return getRuleContext(AlphaNContext.class,0);
		}
		public SignPrecision9Context signPrecision9() {
			return getRuleContext(SignPrecision9Context.class,0);
		}
		public UsageContext usage() {
			return getRuleContext(UsageContext.class,0);
		}
		public TerminalNode COMP_1() { return getToken(copybookParser.COMP_1, 0); }
		public TerminalNode USAGE() { return getToken(copybookParser.USAGE, 0); }
		public TerminalNode IS() { return getToken(copybookParser.IS, 0); }
		public TerminalNode COMP_2() { return getToken(copybookParser.COMP_2, 0); }
		public PicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pic; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PicContext pic() throws RecognitionException {
		PicContext _localctx = new PicContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_pic);
		int _la;
		try {
			setState(411);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(381);
				pictureLiteral();
				setState(395);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case X_S:
				case LENGTH_TYPE_X:
					{
					setState(382);
					alphaX();
					}
					break;
				case A_S:
				case LENGTH_TYPE_A:
					{
					setState(383);
					alphaA();
					}
					break;
				case N_S:
				case LENGTH_TYPE_N:
					{
					setState(384);
					alphaN();
					}
					break;
				case BINARY:
				case COMP:
				case COMP_0:
				case COMP_1:
				case COMP_2:
				case COMP_3:
				case COMP_3U:
				case COMP_4:
				case COMP_5:
				case COMP_9:
				case COMPUTATIONAL:
				case COMPUTATIONAL_0:
				case COMPUTATIONAL_1:
				case COMPUTATIONAL_2:
				case COMPUTATIONAL_3:
				case COMPUTATIONAL_3U:
				case COMPUTATIONAL_4:
				case COMPUTATIONAL_5:
				case COMPUTATIONAL_9:
				case DISPLAY:
				case PACKED_DECIMAL:
				case USAGE:
				case MINUSCHAR:
				case PLUSCHAR:
				case NINES:
				case P_S:
				case S_S:
				case Z_S:
				case V_S:
				case PRECISION_9_EXPLICIT_DOT:
				case PRECISION_9_DECIMAL_SCALED:
				case PRECISION_9_DECIMAL_WITH_V:
				case PRECISION_9_SCALED:
				case PRECISION_9_SCALED_LEAD:
				case PRECISION_Z_EXPLICIT_DOT:
				case PRECISION_Z_DECIMAL_SCALED:
				case PRECISION_Z_SCALED:
					{
					setState(393);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
					case 1:
						{
						setState(385);
						signPrecision9();
						setState(387);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
						case 1:
							{
							setState(386);
							usage();
							}
							break;
						}
						}
						break;
					case 2:
						{
						setState(390);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0) || _la==USAGE) {
							{
							setState(389);
							usage();
							}
						}

						setState(392);
						signPrecision9();
						}
						break;
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USAGE) {
					{
					setState(397);
					match(USAGE);
					setState(399);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IS) {
						{
						setState(398);
						match(IS);
						}
					}

					}
				}

				setState(403);
				match(COMP_1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(408);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USAGE) {
					{
					setState(404);
					match(USAGE);
					setState(406);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IS) {
						{
						setState(405);
						match(IS);
						}
					}

					}
				}

				setState(410);
				match(COMP_2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SectionContext extends ParserRuleContext {
		public TerminalNode LEVEL_ROOT() { return getToken(copybookParser.LEVEL_ROOT, 0); }
		public TerminalNode LEVEL_REGULAR() { return getToken(copybookParser.LEVEL_REGULAR, 0); }
		public SectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitSection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SectionContext section() throws RecognitionException {
		SectionContext _localctx = new SectionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			_la = _input.LA(1);
			if ( !(_la==LEVEL_ROOT || _la==LEVEL_REGULAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SkipLiteralContext extends ParserRuleContext {
		public TerminalNode SKIP1() { return getToken(copybookParser.SKIP1, 0); }
		public TerminalNode SKIP2() { return getToken(copybookParser.SKIP2, 0); }
		public TerminalNode SKIP3() { return getToken(copybookParser.SKIP3, 0); }
		public SkipLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skipLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitSkipLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SkipLiteralContext skipLiteral() throws RecognitionException {
		SkipLiteralContext _localctx = new SkipLiteralContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_skipLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SKIP1) | (1L << SKIP2) | (1L << SKIP3))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupContext extends ParserRuleContext {
		public SectionContext section() {
			return getRuleContext(SectionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<RedefinesContext> redefines() {
			return getRuleContexts(RedefinesContext.class);
		}
		public RedefinesContext redefines(int i) {
			return getRuleContext(RedefinesContext.class,i);
		}
		public List<UsageGroupContext> usageGroup() {
			return getRuleContexts(UsageGroupContext.class);
		}
		public UsageGroupContext usageGroup(int i) {
			return getRuleContext(UsageGroupContext.class,i);
		}
		public List<OccursContext> occurs() {
			return getRuleContexts(OccursContext.class);
		}
		public OccursContext occurs(int i) {
			return getRuleContext(OccursContext.class,i);
		}
		public List<ValuesContext> values() {
			return getRuleContexts(ValuesContext.class);
		}
		public ValuesContext values(int i) {
			return getRuleContext(ValuesContext.class,i);
		}
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
			section();
			setState(418);
			identifier();
			setState(425);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << REDEFINES))) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & ((1L << (USAGE - 73)) | (1L << (VALUE - 73)) | (1L << (VALUES - 73)))) != 0)) {
				{
				setState(423);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case REDEFINES:
					{
					setState(419);
					redefines();
					}
					break;
				case BINARY:
				case COMP:
				case COMP_0:
				case COMP_3:
				case COMP_3U:
				case COMP_4:
				case COMP_5:
				case COMP_9:
				case COMPUTATIONAL:
				case COMPUTATIONAL_0:
				case COMPUTATIONAL_3:
				case COMPUTATIONAL_3U:
				case COMPUTATIONAL_4:
				case COMPUTATIONAL_5:
				case COMPUTATIONAL_9:
				case DISPLAY:
				case PACKED_DECIMAL:
				case USAGE:
					{
					setState(420);
					usageGroup();
					}
					break;
				case OCCURS:
					{
					setState(421);
					occurs();
					}
					break;
				case VALUE:
				case VALUES:
					{
					setState(422);
					values();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(427);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(428);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimitiveContext extends ParserRuleContext {
		public SectionContext section() {
			return getRuleContext(SectionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<JustifiedContext> justified() {
			return getRuleContexts(JustifiedContext.class);
		}
		public JustifiedContext justified(int i) {
			return getRuleContext(JustifiedContext.class,i);
		}
		public List<OccursContext> occurs() {
			return getRuleContexts(OccursContext.class);
		}
		public OccursContext occurs(int i) {
			return getRuleContext(OccursContext.class,i);
		}
		public List<PicContext> pic() {
			return getRuleContexts(PicContext.class);
		}
		public PicContext pic(int i) {
			return getRuleContext(PicContext.class,i);
		}
		public List<RedefinesContext> redefines() {
			return getRuleContexts(RedefinesContext.class);
		}
		public RedefinesContext redefines(int i) {
			return getRuleContext(RedefinesContext.class,i);
		}
		public List<UsageContext> usage() {
			return getRuleContexts(UsageContext.class);
		}
		public UsageContext usage(int i) {
			return getRuleContext(UsageContext.class,i);
		}
		public List<ValuesContext> values() {
			return getRuleContexts(ValuesContext.class);
		}
		public ValuesContext values(int i) {
			return getRuleContext(ValuesContext.class,i);
		}
		public List<SeparateSignContext> separateSign() {
			return getRuleContexts(SeparateSignContext.class);
		}
		public SeparateSignContext separateSign(int i) {
			return getRuleContext(SeparateSignContext.class,i);
		}
		public TerminalNode BLANK() { return getToken(copybookParser.BLANK, 0); }
		public TerminalNode ZERO() { return getToken(copybookParser.ZERO, 0); }
		public TerminalNode WHEN() { return getToken(copybookParser.WHEN, 0); }
		public PrimitiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrimitive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveContext primitive() throws RecognitionException {
		PrimitiveContext _localctx = new PrimitiveContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_primitive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			section();
			setState(431);
			identifier();
			setState(441);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << JUST) | (1L << JUSTIFIED) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << PIC) | (1L << PICTURE) | (1L << REDEFINES))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (SIGN - 64)) | (1L << (USAGE - 64)) | (1L << (VALUE - 64)) | (1L << (VALUES - 64)))) != 0)) {
				{
				setState(439);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(432);
					justified();
					}
					break;
				case 2:
					{
					setState(433);
					occurs();
					}
					break;
				case 3:
					{
					setState(434);
					pic();
					}
					break;
				case 4:
					{
					setState(435);
					redefines();
					}
					break;
				case 5:
					{
					setState(436);
					usage();
					}
					break;
				case 6:
					{
					setState(437);
					values();
					}
					break;
				case 7:
					{
					setState(438);
					separateSign();
					}
					break;
				}
				}
				setState(443);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(449);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BLANK) {
				{
				setState(444);
				match(BLANK);
				setState(446);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHEN) {
					{
					setState(445);
					match(WHEN);
					}
				}

				setState(448);
				match(ZERO);
				}
			}

			setState(451);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Level66statementContext extends ParserRuleContext {
		public TerminalNode LEVEL_NUMBER_66() { return getToken(copybookParser.LEVEL_NUMBER_66, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RenamesContext renames() {
			return getRuleContext(RenamesContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Level66statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level66statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitLevel66statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Level66statementContext level66statement() throws RecognitionException {
		Level66statementContext _localctx = new Level66statementContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_level66statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(453);
			match(LEVEL_NUMBER_66);
			setState(454);
			identifier();
			setState(455);
			renames();
			setState(456);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Level88statementContext extends ParserRuleContext {
		public TerminalNode LEVEL_NUMBER_88() { return getToken(copybookParser.LEVEL_NUMBER_88, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ValuesContext values() {
			return getRuleContext(ValuesContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Level88statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level88statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitLevel88statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Level88statementContext level88statement() throws RecognitionException {
		Level88statementContext _localctx = new Level88statementContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_level88statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(458);
			match(LEVEL_NUMBER_88);
			setState(459);
			identifier();
			setState(460);
			values();
			setState(461);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ItemContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(copybookParser.COMMENT, 0); }
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public PrimitiveContext primitive() {
			return getRuleContext(PrimitiveContext.class,0);
		}
		public Level66statementContext level66statement() {
			return getRuleContext(Level66statementContext.class,0);
		}
		public Level88statementContext level88statement() {
			return getRuleContext(Level88statementContext.class,0);
		}
		public SkipLiteralContext skipLiteral() {
			return getRuleContext(SkipLiteralContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_item);
		try {
			setState(470);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(463);
				match(COMMENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(464);
				group();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(465);
				primitive();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(466);
				level66statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(467);
				level88statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(468);
				skipLiteral();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(469);
				term();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u008a\u01db\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\3\2\6\2"+
		"V\n\2\r\2\16\2W\3\2\5\2[\n\2\3\2\3\2\3\3\3\3\3\3\3\3\5\3c\n\3\3\4\5\4"+
		"f\n\4\3\4\3\4\3\4\5\4k\n\4\3\4\3\4\5\4o\n\4\3\4\5\4r\n\4\3\5\3\5\3\6\3"+
		"\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00ca\n\7\3\b\3"+
		"\b\3\t\3\t\5\t\u00d0\n\t\3\t\3\t\5\t\u00d4\n\t\5\t\u00d6\n\t\3\t\3\t\5"+
		"\t\u00da\n\t\3\t\7\t\u00dd\n\t\f\t\16\t\u00e0\13\t\3\n\3\n\5\n\u00e4\n"+
		"\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\5\r\u00fa\n\r\3\16\3\16\5\16\u00fe\n\16\3\16\5\16\u0101"+
		"\n\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\5\20\u010a\n\20\3\20\3\20\3\21"+
		"\3\21\5\21\u0110\n\21\3\21\3\21\5\21\u0114\n\21\3\21\7\21\u0117\n\21\f"+
		"\21\16\21\u011a\13\21\3\22\3\22\3\22\5\22\u011f\n\22\3\22\5\22\u0122\n"+
		"\22\3\22\5\22\u0125\n\22\3\22\5\22\u0128\n\22\3\22\5\22\u012b\n\22\3\23"+
		"\3\23\3\23\3\24\3\24\3\24\3\24\3\24\5\24\u0135\n\24\3\25\3\25\3\26\3\26"+
		"\3\27\3\27\5\27\u013d\n\27\5\27\u013f\n\27\3\27\3\27\3\30\3\30\5\30\u0145"+
		"\n\30\5\30\u0147\n\30\3\30\3\30\3\31\3\31\5\31\u014d\n\31\3\31\3\31\5"+
		"\31\u0151\n\31\3\31\5\31\u0154\n\31\3\32\3\32\5\32\u0158\n\32\3\33\3\33"+
		"\3\34\3\34\5\34\u015e\n\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\5\35\u016d\n\35\3\36\5\36\u0170\n\36\3\36\3\36\3"+
		"\36\3\36\5\36\u0176\n\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3#\3#\3#"+
		"\3#\5#\u0186\n#\3#\5#\u0189\n#\3#\5#\u018c\n#\5#\u018e\n#\3#\3#\5#\u0192"+
		"\n#\5#\u0194\n#\3#\3#\3#\5#\u0199\n#\5#\u019b\n#\3#\5#\u019e\n#\3$\3$"+
		"\3%\3%\3&\3&\3&\3&\3&\3&\7&\u01aa\n&\f&\16&\u01ad\13&\3&\3&\3\'\3\'\3"+
		"\'\3\'\3\'\3\'\3\'\3\'\3\'\7\'\u01ba\n\'\f\'\16\'\u01bd\13\'\3\'\3\'\5"+
		"\'\u01c1\n\'\3\'\5\'\u01c4\n\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3"+
		"*\3*\3*\3*\3*\3*\3*\5*\u01d9\n*\3*\2\2+\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPR\2\17\4\2^^\177\u0084\4\2"+
		"##JJ\4\2\6\6  \6\2\7\7\f\35!!\66\66\b\2\7\7\f\r\20\26\31\35!!\66\66\4"+
		"\2,,II\3\2)*\4\2aaxx\4\2bbzz\4\2__tt\3\2\678\3\2\177\u0080\3\2?A\2\u0262"+
		"\2U\3\2\2\2\4b\3\2\2\2\6q\3\2\2\2\bs\3\2\2\2\nu\3\2\2\2\f\u00c9\3\2\2"+
		"\2\16\u00cb\3\2\2\2\20\u00d5\3\2\2\2\22\u00e1\3\2\2\2\24\u00e5\3\2\2\2"+
		"\26\u00e7\3\2\2\2\30\u00f9\3\2\2\2\32\u00fb\3\2\2\2\34\u0104\3\2\2\2\36"+
		"\u0107\3\2\2\2 \u010d\3\2\2\2\"\u011b\3\2\2\2$\u012c\3\2\2\2&\u012f\3"+
		"\2\2\2(\u0136\3\2\2\2*\u0138\3\2\2\2,\u013e\3\2\2\2.\u0146\3\2\2\2\60"+
		"\u014a\3\2\2\2\62\u0155\3\2\2\2\64\u0159\3\2\2\2\66\u015d\3\2\2\28\u016c"+
		"\3\2\2\2:\u0175\3\2\2\2<\u0177\3\2\2\2>\u0179\3\2\2\2@\u017b\3\2\2\2B"+
		"\u017d\3\2\2\2D\u019d\3\2\2\2F\u019f\3\2\2\2H\u01a1\3\2\2\2J\u01a3\3\2"+
		"\2\2L\u01b0\3\2\2\2N\u01c7\3\2\2\2P\u01cc\3\2\2\2R\u01d8\3\2\2\2TV\5R"+
		"*\2UT\3\2\2\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2XZ\3\2\2\2Y[\7\u0089\2\2ZY"+
		"\3\2\2\2Z[\3\2\2\2[\\\3\2\2\2\\]\7\2\2\3]\3\3\2\2\2^c\7~\2\2_c\5\6\4\2"+
		"`c\5\n\6\2ac\5\30\r\2b^\3\2\2\2b_\3\2\2\2b`\3\2\2\2ba\3\2\2\2c\5\3\2\2"+
		"\2df\5\66\34\2ed\3\2\2\2ef\3\2\2\2fg\3\2\2\2gr\7\u0086\2\2hr\7P\2\2ik"+
		"\5\66\34\2ji\3\2\2\2jk\3\2\2\2kl\3\2\2\2lr\5\b\5\2mo\5\66\34\2nm\3\2\2"+
		"\2no\3\2\2\2op\3\2\2\2pr\7j\2\2qe\3\2\2\2qh\3\2\2\2qj\3\2\2\2qn\3\2\2"+
		"\2r\7\3\2\2\2st\t\2\2\2t\t\3\2\2\2uv\t\3\2\2v\13\3\2\2\2w\u00ca\7\u0088"+
		"\2\2x\u00ca\7\3\2\2y\u00ca\7_\2\2z\u00ca\7`\2\2{\u00ca\7f\2\2|\u00ca\7"+
		"a\2\2}\u00ca\3\2\2\2~\u00ca\7b\2\2\177\u00ca\7c\2\2\u0080\u00ca\7g\2\2"+
		"\u0081\u00ca\7d\2\2\u0082\u00ca\7h\2\2\u0083\u00ca\7e\2\2\u0084\u00ca"+
		"\7i\2\2\u0085\u00ca\7\u0087\2\2\u0086\u00ca\7B\2\2\u0087\u00ca\7,\2\2"+
		"\u0088\u00ca\7I\2\2\u0089\u00ca\7>\2\2\u008a\u00ca\7\n\2\2\u008b\u00ca"+
		"\7\6\2\2\u008c\u00ca\7 \2\2\u008d\u00ca\7+\2\2\u008e\u00ca\7\7\2\2\u008f"+
		"\u00ca\7!\2\2\u0090\u00ca\7\66\2\2\u0091\u00ca\7\25\2\2\u0092\u00ca\7"+
		"\26\2\2\u0093\u00ca\7\27\2\2\u0094\u00ca\7\30\2\2\u0095\u00ca\7\31\2\2"+
		"\u0096\u00ca\7\32\2\2\u0097\u00ca\7\33\2\2\u0098\u00ca\7\34\2\2\u0099"+
		"\u00ca\7\35\2\2\u009a\u00ca\7\f\2\2\u009b\u00ca\7\r\2\2\u009c\u00ca\7"+
		"\16\2\2\u009d\u00ca\7\17\2\2\u009e\u00ca\7\20\2\2\u009f\u00ca\7\21\2\2"+
		"\u00a0\u00ca\7\22\2\2\u00a1\u00ca\7\23\2\2\u00a2\u00ca\7\24\2\2\u00a3"+
		"\u00ca\7;\2\2\u00a4\u00ca\7<\2\2\u00a5\u00ca\7M\2\2\u00a6\u00ca\7N\2\2"+
		"\u00a7\u00ca\7\64\2\2\u00a8\u00ca\7G\2\2\u00a9\u00ca\7\37\2\2\u00aa\u00ca"+
		"\7\'\2\2\u00ab\u00ca\7\b\2\2\u00ac\u00ca\7P\2\2\u00ad\u00ca\7Q\2\2\u00ae"+
		"\u00ca\7R\2\2\u00af\u00ca\7C\2\2\u00b0\u00ca\7D\2\2\u00b1\u00ca\7%\2\2"+
		"\u00b2\u00ca\7&\2\2\u00b3\u00ca\7.\2\2\u00b4\u00ca\7/\2\2\u00b5\u00ca"+
		"\7\60\2\2\u00b6\u00ca\7\61\2\2\u00b7\u00ca\79\2\2\u00b8\u00ca\7:\2\2\u00b9"+
		"\u00ca\7*\2\2\u00ba\u00ca\7)\2\2\u00bb\u00ca\7=\2\2\u00bc\u00ca\78\2\2"+
		"\u00bd\u00ca\7\67\2\2\u00be\u00ca\7J\2\2\u00bf\u00ca\7#\2\2\u00c0\u00ca"+
		"\7\5\2\2\u00c1\u00ca\7(\2\2\u00c2\u00ca\7\65\2\2\u00c3\u00ca\7\t\2\2\u00c4"+
		"\u00ca\7O\2\2\u00c5\u00ca\7H\2\2\u00c6\u00ca\7\4\2\2\u00c7\u00ca\7K\2"+
		"\2\u00c8\u00ca\7$\2\2\u00c9w\3\2\2\2\u00c9x\3\2\2\2\u00c9y\3\2\2\2\u00c9"+
		"z\3\2\2\2\u00c9{\3\2\2\2\u00c9|\3\2\2\2\u00c9}\3\2\2\2\u00c9~\3\2\2\2"+
		"\u00c9\177\3\2\2\2\u00c9\u0080\3\2\2\2\u00c9\u0081\3\2\2\2\u00c9\u0082"+
		"\3\2\2\2\u00c9\u0083\3\2\2\2\u00c9\u0084\3\2\2\2\u00c9\u0085\3\2\2\2\u00c9"+
		"\u0086\3\2\2\2\u00c9\u0087\3\2\2\2\u00c9\u0088\3\2\2\2\u00c9\u0089\3\2"+
		"\2\2\u00c9\u008a\3\2\2\2\u00c9\u008b\3\2\2\2\u00c9\u008c\3\2\2\2\u00c9"+
		"\u008d\3\2\2\2\u00c9\u008e\3\2\2\2\u00c9\u008f\3\2\2\2\u00c9\u0090\3\2"+
		"\2\2\u00c9\u0091\3\2\2\2\u00c9\u0092\3\2\2\2\u00c9\u0093\3\2\2\2\u00c9"+
		"\u0094\3\2\2\2\u00c9\u0095\3\2\2\2\u00c9\u0096\3\2\2\2\u00c9\u0097\3\2"+
		"\2\2\u00c9\u0098\3\2\2\2\u00c9\u0099\3\2\2\2\u00c9\u009a\3\2\2\2\u00c9"+
		"\u009b\3\2\2\2\u00c9\u009c\3\2\2\2\u00c9\u009d\3\2\2\2\u00c9\u009e\3\2"+
		"\2\2\u00c9\u009f\3\2\2\2\u00c9\u00a0\3\2\2\2\u00c9\u00a1\3\2\2\2\u00c9"+
		"\u00a2\3\2\2\2\u00c9\u00a3\3\2\2\2\u00c9\u00a4\3\2\2\2\u00c9\u00a5\3\2"+
		"\2\2\u00c9\u00a6\3\2\2\2\u00c9\u00a7\3\2\2\2\u00c9\u00a8\3\2\2\2\u00c9"+
		"\u00a9\3\2\2\2\u00c9\u00aa\3\2\2\2\u00c9\u00ab\3\2\2\2\u00c9\u00ac\3\2"+
		"\2\2\u00c9\u00ad\3\2\2\2\u00c9\u00ae\3\2\2\2\u00c9\u00af\3\2\2\2\u00c9"+
		"\u00b0\3\2\2\2\u00c9\u00b1\3\2\2\2\u00c9\u00b2\3\2\2\2\u00c9\u00b3\3\2"+
		"\2\2\u00c9\u00b4\3\2\2\2\u00c9\u00b5\3\2\2\2\u00c9\u00b6\3\2\2\2\u00c9"+
		"\u00b7\3\2\2\2\u00c9\u00b8\3\2\2\2\u00c9\u00b9\3\2\2\2\u00c9\u00ba\3\2"+
		"\2\2\u00c9\u00bb\3\2\2\2\u00c9\u00bc\3\2\2\2\u00c9\u00bd\3\2\2\2\u00c9"+
		"\u00be\3\2\2\2\u00c9\u00bf\3\2\2\2\u00c9\u00c0\3\2\2\2\u00c9\u00c1\3\2"+
		"\2\2\u00c9\u00c2\3\2\2\2\u00c9\u00c3\3\2\2\2\u00c9\u00c4\3\2\2\2\u00c9"+
		"\u00c5\3\2\2\2\u00c9\u00c6\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00c8\3\2"+
		"\2\2\u00ca\r\3\2\2\2\u00cb\u00cc\7\3\2\2\u00cc\17\3\2\2\2\u00cd\u00cf"+
		"\7M\2\2\u00ce\u00d0\7(\2\2\u00cf\u00ce\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0"+
		"\u00d6\3\2\2\2\u00d1\u00d3\7N\2\2\u00d2\u00d4\7\5\2\2\u00d3\u00d2\3\2"+
		"\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d6\3\2\2\2\u00d5\u00cd\3\2\2\2\u00d5"+
		"\u00d1\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00de\5\22\n\2\u00d8\u00da\7"+
		"T\2\2\u00d9\u00d8\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00db\3\2\2\2\u00db"+
		"\u00dd\5\22\n\2\u00dc\u00d9\3\2\2\2\u00dd\u00e0\3\2\2\2\u00de\u00dc\3"+
		"\2\2\2\u00de\u00df\3\2\2\2\u00df\21\3\2\2\2\u00e0\u00de\3\2\2\2\u00e1"+
		"\u00e3\5\24\13\2\u00e2\u00e4\5\26\f\2\u00e3\u00e2\3\2\2\2\u00e3\u00e4"+
		"\3\2\2\2\u00e4\23\3\2\2\2\u00e5\u00e6\5\4\3\2\u00e6\25\3\2\2\2\u00e7\u00e8"+
		"\5\16\b\2\u00e8\u00e9\5\4\3\2\u00e9\27\3\2\2\2\u00ea\u00eb\7\4\2\2\u00eb"+
		"\u00fa\5\4\3\2\u00ec\u00fa\7%\2\2\u00ed\u00fa\7&\2\2\u00ee\u00fa\7.\2"+
		"\2\u00ef\u00fa\7/\2\2\u00f0\u00fa\7\60\2\2\u00f1\u00fa\7\61\2\2\u00f2"+
		"\u00fa\79\2\2\u00f3\u00fa\7:\2\2\u00f4\u00fa\7C\2\2\u00f5\u00fa\7D\2\2"+
		"\u00f6\u00fa\7P\2\2\u00f7\u00fa\7Q\2\2\u00f8\u00fa\7R\2\2\u00f9\u00ea"+
		"\3\2\2\2\u00f9\u00ec\3\2\2\2\u00f9\u00ed\3\2\2\2\u00f9\u00ee\3\2\2\2\u00f9"+
		"\u00ef\3\2\2\2\u00f9\u00f0\3\2\2\2\u00f9\u00f1\3\2\2\2\u00f9\u00f2\3\2"+
		"\2\2\u00f9\u00f3\3\2\2\2\u00f9\u00f4\3\2\2\2\u00f9\u00f5\3\2\2\2\u00f9"+
		"\u00f6\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00f8\3\2\2\2\u00fa\31\3\2\2"+
		"\2\u00fb\u00fd\t\4\2\2\u00fc\u00fe\7+\2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe"+
		"\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff\u0101\7(\2\2\u0100\u00ff\3\2\2\2\u0100"+
		"\u0101\3\2\2\2\u0101\u0102\3\2\2\2\u0102\u0103\5\f\7\2\u0103\33\3\2\2"+
		"\2\u0104\u0105\7H\2\2\u0105\u0106\5\b\5\2\u0106\35\3\2\2\2\u0107\u0109"+
		"\7\37\2\2\u0108\u010a\7\65\2\2\u0109\u0108\3\2\2\2\u0109\u010a\3\2\2\2"+
		"\u010a\u010b\3\2\2\2\u010b\u010c\5\f\7\2\u010c\37\3\2\2\2\u010d\u010f"+
		"\7\'\2\2\u010e\u0110\7\t\2\2\u010f\u010e\3\2\2\2\u010f\u0110\3\2\2\2\u0110"+
		"\u0111\3\2\2\2\u0111\u0118\5\f\7\2\u0112\u0114\7T\2\2\u0113\u0112\3\2"+
		"\2\2\u0113\u0114\3\2\2\2\u0114\u0115\3\2\2\2\u0115\u0117\7\u0088\2\2\u0116"+
		"\u0113\3\2\2\2\u0117\u011a\3\2\2\2\u0118\u0116\3\2\2\2\u0118\u0119\3\2"+
		"\2\2\u0119!\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u011c\7\64\2\2\u011c\u011e"+
		"\5\b\5\2\u011d\u011f\5\34\17\2\u011e\u011d\3\2\2\2\u011e\u011f\3\2\2\2"+
		"\u011f\u0121\3\2\2\2\u0120\u0122\7G\2\2\u0121\u0120\3\2\2\2\u0121\u0122"+
		"\3\2\2\2\u0122\u0124\3\2\2\2\u0123\u0125\5\36\20\2\u0124\u0123\3\2\2\2"+
		"\u0124\u0125\3\2\2\2\u0125\u0127\3\2\2\2\u0126\u0128\5\32\16\2\u0127\u0126"+
		"\3\2\2\2\u0127\u0128\3\2\2\2\u0128\u012a\3\2\2\2\u0129\u012b\5 \21\2\u012a"+
		"\u0129\3\2\2\2\u012a\u012b\3\2\2\2\u012b#\3\2\2\2\u012c\u012d\7;\2\2\u012d"+
		"\u012e\5\f\7\2\u012e%\3\2\2\2\u012f\u0130\7<\2\2\u0130\u0134\5\f\7\2\u0131"+
		"\u0132\5\16\b\2\u0132\u0133\5\f\7\2\u0133\u0135\3\2\2\2\u0134\u0131\3"+
		"\2\2\2\u0134\u0135\3\2\2\2\u0135\'\3\2\2\2\u0136\u0137\t\5\2\2\u0137)"+
		"\3\2\2\2\u0138\u0139\t\6\2\2\u0139+\3\2\2\2\u013a\u013c\7K\2\2\u013b\u013d"+
		"\7(\2\2\u013c\u013b\3\2\2\2\u013c\u013d\3\2\2\2\u013d\u013f\3\2\2\2\u013e"+
		"\u013a\3\2\2\2\u013e\u013f\3\2\2\2\u013f\u0140\3\2\2\2\u0140\u0141\5("+
		"\25\2\u0141-\3\2\2\2\u0142\u0144\7K\2\2\u0143\u0145\7(\2\2\u0144\u0143"+
		"\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0147\3\2\2\2\u0146\u0142\3\2\2\2\u0146"+
		"\u0147\3\2\2\2\u0147\u0148\3\2\2\2\u0148\u0149\5*\26\2\u0149/\3\2\2\2"+
		"\u014a\u014c\7B\2\2\u014b\u014d\7(\2\2\u014c\u014b\3\2\2\2\u014c\u014d"+
		"\3\2\2\2\u014d\u014e\3\2\2\2\u014e\u0150\t\7\2\2\u014f\u0151\7>\2\2\u0150"+
		"\u014f\3\2\2\2\u0150\u0151\3\2\2\2\u0151\u0153\3\2\2\2\u0152\u0154\7\n"+
		"\2\2\u0153\u0152\3\2\2\2\u0153\u0154\3\2\2\2\u0154\61\3\2\2\2\u0155\u0157"+
		"\t\b\2\2\u0156\u0158\7=\2\2\u0157\u0156\3\2\2\2\u0157\u0158\3\2\2\2\u0158"+
		"\63\3\2\2\2\u0159\u015a\7\\\2\2\u015a\65\3\2\2\2\u015b\u015e\7X\2\2\u015c"+
		"\u015e\7W\2\2\u015d\u015b\3\2\2\2\u015d\u015c\3\2\2\2\u015e\67\3\2\2\2"+
		"\u015f\u016d\7^\2\2\u0160\u016d\7c\2\2\u0161\u016d\7`\2\2\u0162\u016d"+
		"\7d\2\2\u0163\u016d\7e\2\2\u0164\u016d\7j\2\2\u0165\u016d\7k\2\2\u0166"+
		"\u016d\7l\2\2\u0167\u016d\7m\2\2\u0168\u016d\7n\2\2\u0169\u016d\7o\2\2"+
		"\u016a\u016d\7p\2\2\u016b\u016d\7q\2\2\u016c\u015f\3\2\2\2\u016c\u0160"+
		"\3\2\2\2\u016c\u0161\3\2\2\2\u016c\u0162\3\2\2\2\u016c\u0163\3\2\2\2\u016c"+
		"\u0164\3\2\2\2\u016c\u0165\3\2\2\2\u016c\u0166\3\2\2\2\u016c\u0167\3\2"+
		"\2\2\u016c\u0168\3\2\2\2\u016c\u0169\3\2\2\2\u016c\u016a\3\2\2\2\u016c"+
		"\u016b\3\2\2\2\u016d9\3\2\2\2\u016e\u0170\5\66\34\2\u016f\u016e\3\2\2"+
		"\2\u016f\u0170\3\2\2\2\u0170\u0171\3\2\2\2\u0171\u0176\58\35\2\u0172\u0173"+
		"\58\35\2\u0173\u0174\5\66\34\2\u0174\u0176\3\2\2\2\u0175\u016f\3\2\2\2"+
		"\u0175\u0172\3\2\2\2\u0176;\3\2\2\2\u0177\u0178\t\t\2\2\u0178=\3\2\2\2"+
		"\u0179\u017a\t\n\2\2\u017a?\3\2\2\2\u017b\u017c\t\13\2\2\u017cA\3\2\2"+
		"\2\u017d\u017e\t\f\2\2\u017eC\3\2\2\2\u017f\u018d\5B\"\2\u0180\u018e\5"+
		"<\37\2\u0181\u018e\5@!\2\u0182\u018e\5> \2\u0183\u0185\5:\36\2\u0184\u0186"+
		"\5,\27\2\u0185\u0184\3\2\2\2\u0185\u0186\3\2\2\2\u0186\u018c\3\2\2\2\u0187"+
		"\u0189\5,\27\2\u0188\u0187\3\2\2\2\u0188\u0189\3\2\2\2\u0189\u018a\3\2"+
		"\2\2\u018a\u018c\5:\36\2\u018b\u0183\3\2\2\2\u018b\u0188\3\2\2\2\u018c"+
		"\u018e\3\2\2\2\u018d\u0180\3\2\2\2\u018d\u0181\3\2\2\2\u018d\u0182\3\2"+
		"\2\2\u018d\u018b\3\2\2\2\u018e\u019e\3\2\2\2\u018f\u0191\7K\2\2\u0190"+
		"\u0192\7(\2\2\u0191\u0190\3\2\2\2\u0191\u0192\3\2\2\2\u0192\u0194\3\2"+
		"\2\2\u0193\u018f\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0195\3\2\2\2\u0195"+
		"\u019e\7\16\2\2\u0196\u0198\7K\2\2\u0197\u0199\7(\2\2\u0198\u0197\3\2"+
		"\2\2\u0198\u0199\3\2\2\2\u0199\u019b\3\2\2\2\u019a\u0196\3\2\2\2\u019a"+
		"\u019b\3\2\2\2\u019b\u019c\3\2\2\2\u019c\u019e\7\17\2\2\u019d\u017f\3"+
		"\2\2\2\u019d\u0193\3\2\2\2\u019d\u019a\3\2\2\2\u019eE\3\2\2\2\u019f\u01a0"+
		"\t\r\2\2\u01a0G\3\2\2\2\u01a1\u01a2\t\16\2\2\u01a2I\3\2\2\2\u01a3\u01a4"+
		"\5F$\2\u01a4\u01ab\5\f\7\2\u01a5\u01aa\5$\23\2\u01a6\u01aa\5.\30\2\u01a7"+
		"\u01aa\5\"\22\2\u01a8\u01aa\5\20\t\2\u01a9\u01a5\3\2\2\2\u01a9\u01a6\3"+
		"\2\2\2\u01a9\u01a7\3\2\2\2\u01a9\u01a8\3\2\2\2\u01aa\u01ad\3\2\2\2\u01ab"+
		"\u01a9\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ae\3\2\2\2\u01ad\u01ab\3\2"+
		"\2\2\u01ae\u01af\5\64\33\2\u01afK\3\2\2\2\u01b0\u01b1\5F$\2\u01b1\u01bb"+
		"\5\f\7\2\u01b2\u01ba\5\62\32\2\u01b3\u01ba\5\"\22\2\u01b4\u01ba\5D#\2"+
		"\u01b5\u01ba\5$\23\2\u01b6\u01ba\5,\27\2\u01b7\u01ba\5\20\t\2\u01b8\u01ba"+
		"\5\60\31\2\u01b9\u01b2\3\2\2\2\u01b9\u01b3\3\2\2\2\u01b9\u01b4\3\2\2\2"+
		"\u01b9\u01b5\3\2\2\2\u01b9\u01b6\3\2\2\2\u01b9\u01b7\3\2\2\2\u01b9\u01b8"+
		"\3\2\2\2\u01ba\u01bd\3\2\2\2\u01bb\u01b9\3\2\2\2\u01bb\u01bc\3\2\2\2\u01bc"+
		"\u01c3\3\2\2\2\u01bd\u01bb\3\2\2\2\u01be\u01c0\7\b\2\2\u01bf\u01c1\7O"+
		"\2\2\u01c0\u01bf\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1\u01c2\3\2\2\2\u01c2"+
		"\u01c4\7P\2\2\u01c3\u01be\3\2\2\2\u01c3\u01c4\3\2\2\2\u01c4\u01c5\3\2"+
		"\2\2\u01c5\u01c6\5\64\33\2\u01c6M\3\2\2\2\u01c7\u01c8\7\u0081\2\2\u01c8"+
		"\u01c9\5\f\7\2\u01c9\u01ca\5&\24\2\u01ca\u01cb\5\64\33\2\u01cbO\3\2\2"+
		"\2\u01cc\u01cd\7\u0083\2\2\u01cd\u01ce\5\f\7\2\u01ce\u01cf\5\20\t\2\u01cf"+
		"\u01d0\5\64\33\2\u01d0Q\3\2\2\2\u01d1\u01d9\7]\2\2\u01d2\u01d9\5J&\2\u01d3"+
		"\u01d9\5L\'\2\u01d4\u01d9\5N(\2\u01d5\u01d9\5P)\2\u01d6\u01d9\5H%\2\u01d7"+
		"\u01d9\5\64\33\2\u01d8\u01d1\3\2\2\2\u01d8\u01d2\3\2\2\2\u01d8\u01d3\3"+
		"\2\2\2\u01d8\u01d4\3\2\2\2\u01d8\u01d5\3\2\2\2\u01d8\u01d6\3\2\2\2\u01d8"+
		"\u01d7\3\2\2\2\u01d9S\3\2\2\29WZbejnq\u00c9\u00cf\u00d3\u00d5\u00d9\u00de"+
		"\u00e3\u00f9\u00fd\u0100\u0109\u010f\u0113\u0118\u011e\u0121\u0124\u0127"+
		"\u012a\u0134\u013c\u013e\u0144\u0146\u014c\u0150\u0153\u0157\u015d\u016c"+
		"\u016f\u0175\u0185\u0188\u018b\u018d\u0191\u0193\u0198\u019a\u019d\u01a9"+
		"\u01ab\u01b9\u01bb\u01c0\u01c3\u01d8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
