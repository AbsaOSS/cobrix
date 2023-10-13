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

// Generated from copybookParser.g4 by ANTLR 4.8
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
		PRECISION_9_DECIMAL_SCALED=105, PRECISION_9_SCALED=106, PRECISION_9_SCALED_LEAD=107, 
		PRECISION_Z_EXPLICIT_DOT=108, PRECISION_Z_DECIMAL_SCALED=109, PRECISION_Z_SCALED=110, 
		LENGTH_TYPE_9=111, LENGTH_TYPE_9_1=112, LENGTH_TYPE_A=113, LENGTH_TYPE_A_1=114, 
		LENGTH_TYPE_P=115, LENGTH_TYPE_P_1=116, LENGTH_TYPE_X=117, LENGTH_TYPE_X_1=118, 
		LENGTH_TYPE_N=119, LENGTH_TYPE_N_1=120, LENGTH_TYPE_Z=121, LENGTH_TYPE_Z_1=122, 
		STRINGLITERAL=123, LEVEL_ROOT=124, LEVEL_REGULAR=125, LEVEL_NUMBER_66=126, 
		LEVEL_NUMBER_77=127, LEVEL_NUMBER_88=128, INTEGERLITERAL=129, POSITIVELITERAL=130, 
		NUMERICLITERAL=131, SINGLE_QUOTED_IDENTIFIER=132, IDENTIFIER=133, CONTROL_Z=134, 
		WS=135;
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
			null, null, null, null, null, null, "'01'", null, "'66'", "'77'", "'88'", 
			null, null, null, null, null, "'\u001A'"
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
			"PRECISION_9_DECIMAL_SCALED", "PRECISION_9_SCALED", "PRECISION_9_SCALED_LEAD", 
			"PRECISION_Z_EXPLICIT_DOT", "PRECISION_Z_DECIMAL_SCALED", "PRECISION_Z_SCALED", 
			"LENGTH_TYPE_9", "LENGTH_TYPE_9_1", "LENGTH_TYPE_A", "LENGTH_TYPE_A_1", 
			"LENGTH_TYPE_P", "LENGTH_TYPE_P_1", "LENGTH_TYPE_X", "LENGTH_TYPE_X_1", 
			"LENGTH_TYPE_N", "LENGTH_TYPE_N_1", "LENGTH_TYPE_Z", "LENGTH_TYPE_Z_1", 
			"STRINGLITERAL", "LEVEL_ROOT", "LEVEL_REGULAR", "LEVEL_NUMBER_66", "LEVEL_NUMBER_77", 
			"LEVEL_NUMBER_88", "INTEGERLITERAL", "POSITIVELITERAL", "NUMERICLITERAL", 
			"SINGLE_QUOTED_IDENTIFIER", "IDENTIFIER", "CONTROL_Z", "WS"
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
			setState(107);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
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
			setState(109);
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
			setState(111);
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
			setState(128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(113);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(114);
				match(THRU_OR_THROUGH);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(115);
				match(A_S);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(116);
				match(P_S);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(117);
				match(P_NS);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(118);
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
				setState(120);
				match(N_S);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(121);
				match(S_S);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(122);
				match(S_NS);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(123);
				match(Z_S);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(124);
				match(Z_NS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(125);
				match(V_S);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(126);
				match(V_NS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(127);
				match(SINGLE_QUOTED_IDENTIFIER);
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
			setState(130);
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
			setState(140);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
				{
				setState(132);
				match(VALUE);
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(133);
					match(IS);
					}
				}

				}
				break;
			case VALUES:
				{
				setState(136);
				match(VALUES);
				setState(138);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ARE) {
					{
					setState(137);
					match(ARE);
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(142);
			valuesFromTo();
			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALL) | (1L << FALSE) | (1L << HIGH_VALUE) | (1L << HIGH_VALUES) | (1L << LOW_VALUE) | (1L << LOW_VALUES) | (1L << NULL) | (1L << NULLS) | (1L << QUOTE) | (1L << QUOTES))) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (SPACE - 65)) | (1L << (SPACES - 65)) | (1L << (TRUE - 65)) | (1L << (ZERO - 65)) | (1L << (ZEROS - 65)) | (1L << (ZEROES - 65)) | (1L << (COMMACHAR - 65)) | (1L << (MINUSCHAR - 65)) | (1L << (PLUSCHAR - 65)) | (1L << (NINES - 65)) | (1L << (STRINGLITERAL - 65)) | (1L << (LEVEL_ROOT - 65)) | (1L << (LEVEL_REGULAR - 65)) | (1L << (LEVEL_NUMBER_66 - 65)) | (1L << (LEVEL_NUMBER_77 - 65)) | (1L << (LEVEL_NUMBER_88 - 65)))) != 0) || _la==INTEGERLITERAL || _la==NUMERICLITERAL) {
				{
				{
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(143);
					match(COMMACHAR);
					}
				}

				setState(146);
				valuesFromTo();
				}
				}
				setState(151);
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
			setState(152);
			valuesFrom();
			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(153);
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
			setState(156);
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
			setState(158);
			thru();
			setState(159);
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
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				match(ALL);
				setState(162);
				literal();
				}
				break;
			case HIGH_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				match(HIGH_VALUE);
				}
				break;
			case HIGH_VALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(164);
				match(HIGH_VALUES);
				}
				break;
			case LOW_VALUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(165);
				match(LOW_VALUE);
				}
				break;
			case LOW_VALUES:
				enterOuterAlt(_localctx, 5);
				{
				setState(166);
				match(LOW_VALUES);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(167);
				match(NULL);
				}
				break;
			case NULLS:
				enterOuterAlt(_localctx, 7);
				{
				setState(168);
				match(NULLS);
				}
				break;
			case QUOTE:
				enterOuterAlt(_localctx, 8);
				{
				setState(169);
				match(QUOTE);
				}
				break;
			case QUOTES:
				enterOuterAlt(_localctx, 9);
				{
				setState(170);
				match(QUOTES);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 10);
				{
				setState(171);
				match(SPACE);
				}
				break;
			case SPACES:
				enterOuterAlt(_localctx, 11);
				{
				setState(172);
				match(SPACES);
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 12);
				{
				setState(173);
				match(ZERO);
				}
				break;
			case ZEROS:
				enterOuterAlt(_localctx, 13);
				{
				setState(174);
				match(ZEROS);
				}
				break;
			case ZEROES:
				enterOuterAlt(_localctx, 14);
				{
				setState(175);
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
			setState(178);
			_la = _input.LA(1);
			if ( !(_la==ASCENDING || _la==DESCENDING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY) {
				{
				setState(179);
				match(KEY);
				}
			}

			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(182);
				match(IS);
				}
			}

			setState(185);
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
			setState(187);
			match(TO);
			setState(188);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(DEPENDING);
			setState(192);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(191);
				match(ON);
				}
			}

			setState(194);
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
			setState(196);
			match(INDEXED);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BY) {
				{
				setState(197);
				match(BY);
				}
			}

			setState(200);
			identifier();
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMACHAR || _la==IDENTIFIER) {
				{
				{
				setState(202);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(201);
					match(COMMACHAR);
					}
				}

				setState(204);
				match(IDENTIFIER);
				}
				}
				setState(209);
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
			setState(210);
			match(OCCURS);
			setState(211);
			integerLiteral();
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(212);
				occursTo();
				}
			}

			setState(216);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TIMES) {
				{
				setState(215);
				match(TIMES);
				}
			}

			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEPENDING) {
				{
				setState(218);
				dependingOn();
				}
			}

			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASCENDING || _la==DESCENDING) {
				{
				setState(221);
				sorts();
				}
			}

			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEXED) {
				{
				setState(224);
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
			setState(227);
			match(REDEFINES);
			setState(228);
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
			setState(230);
			match(RENAMES);
			setState(231);
			identifier();
			setState(235);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(232);
				thru();
				setState(233);
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
			setState(237);
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
			setState(239);
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
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(241);
				match(USAGE);
				setState(243);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(242);
					match(IS);
					}
				}

				}
			}

			setState(247);
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
			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(249);
				match(USAGE);
				setState(251);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(250);
					match(IS);
					}
				}

				}
			}

			setState(255);
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
			setState(257);
			match(SIGN);
			setState(259);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(258);
				match(IS);
				}
			}

			setState(261);
			_la = _input.LA(1);
			if ( !(_la==LEADING || _la==TRAILING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(263);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEPARATE) {
				{
				setState(262);
				match(SEPARATE);
				}
			}

			setState(266);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CHARACTER) {
				{
				setState(265);
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
			setState(268);
			_la = _input.LA(1);
			if ( !(_la==JUST || _la==JUSTIFIED) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RIGHT) {
				{
				setState(269);
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
			setState(272);
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
			setState(276);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUSCHAR:
				_localctx = new PlusContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(274);
				match(PLUSCHAR);
				}
				break;
			case MINUSCHAR:
				_localctx = new MinusContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(275);
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
	public static class Precision9ZsContext extends Precision9Context {
		public TerminalNode Z_S() { return getToken(copybookParser.Z_S, 0); }
		public Precision9ZsContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Zs(this);
			else return visitor.visitChildren(this);
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
	public static class Precision9NinesContext extends Precision9Context {
		public TerminalNode NINES() { return getToken(copybookParser.NINES, 0); }
		public Precision9NinesContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Nines(this);
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
	public static class Precision9ScaledContext extends Precision9Context {
		public TerminalNode PRECISION_9_SCALED() { return getToken(copybookParser.PRECISION_9_SCALED, 0); }
		public Precision9ScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9Scaled(this);
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
	public static class Precision9DecimalScaledContext extends Precision9Context {
		public TerminalNode PRECISION_9_DECIMAL_SCALED() { return getToken(copybookParser.PRECISION_9_DECIMAL_SCALED, 0); }
		public Precision9DecimalScaledContext(Precision9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybookParserVisitor ) return ((copybookParserVisitor<? extends T>)visitor).visitPrecision9DecimalScaled(this);
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
			setState(290);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NINES:
				_localctx = new Precision9NinesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(278);
				match(NINES);
				}
				break;
			case S_S:
				_localctx = new Precision9SsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(279);
				match(S_S);
				}
				break;
			case P_S:
				_localctx = new Precision9PsContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(280);
				match(P_S);
				}
				break;
			case Z_S:
				_localctx = new Precision9ZsContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(281);
				match(Z_S);
				}
				break;
			case V_S:
				_localctx = new Precision9VsContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(282);
				match(V_S);
				}
				break;
			case PRECISION_9_EXPLICIT_DOT:
				_localctx = new Precision9ExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(283);
				match(PRECISION_9_EXPLICIT_DOT);
				}
				break;
			case PRECISION_9_DECIMAL_SCALED:
				_localctx = new Precision9DecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(284);
				match(PRECISION_9_DECIMAL_SCALED);
				}
				break;
			case PRECISION_9_SCALED:
				_localctx = new Precision9ScaledContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(285);
				match(PRECISION_9_SCALED);
				}
				break;
			case PRECISION_9_SCALED_LEAD:
				_localctx = new Precision9ScaledLeadContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(286);
				match(PRECISION_9_SCALED_LEAD);
				}
				break;
			case PRECISION_Z_EXPLICIT_DOT:
				_localctx = new PrecisionZExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(287);
				match(PRECISION_Z_EXPLICIT_DOT);
				}
				break;
			case PRECISION_Z_DECIMAL_SCALED:
				_localctx = new PrecisionZDecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(288);
				match(PRECISION_Z_DECIMAL_SCALED);
				}
				break;
			case PRECISION_Z_SCALED:
				_localctx = new PrecisionZScaledContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(289);
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
			setState(299);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				_localctx = new LeadingSignContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(293);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(292);
					plusMinus();
					}
				}

				setState(295);
				precision9();
				}
				}
				break;
			case 2:
				_localctx = new TrailingSignContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(296);
				precision9();
				setState(297);
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
			setState(301);
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
			setState(303);
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
			setState(305);
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
			setState(307);
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
			setState(339);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(309);
				pictureLiteral();
				setState(323);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case X_S:
				case LENGTH_TYPE_X:
					{
					setState(310);
					alphaX();
					}
					break;
				case A_S:
				case LENGTH_TYPE_A:
					{
					setState(311);
					alphaA();
					}
					break;
				case N_S:
				case LENGTH_TYPE_N:
					{
					setState(312);
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
				case PRECISION_9_SCALED:
				case PRECISION_9_SCALED_LEAD:
				case PRECISION_Z_EXPLICIT_DOT:
				case PRECISION_Z_DECIMAL_SCALED:
				case PRECISION_Z_SCALED:
					{
					setState(321);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
					case 1:
						{
						setState(313);
						signPrecision9();
						setState(315);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
						case 1:
							{
							setState(314);
							usage();
							}
							break;
						}
						}
						break;
					case 2:
						{
						setState(318);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0) || _la==USAGE) {
							{
							setState(317);
							usage();
							}
						}

						setState(320);
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
				setState(329);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USAGE) {
					{
					setState(325);
					match(USAGE);
					setState(327);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IS) {
						{
						setState(326);
						match(IS);
						}
					}

					}
				}

				setState(331);
				match(COMP_1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(336);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USAGE) {
					{
					setState(332);
					match(USAGE);
					setState(334);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IS) {
						{
						setState(333);
						match(IS);
						}
					}

					}
				}

				setState(338);
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
			setState(341);
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
			setState(343);
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
			setState(345);
			section();
			setState(346);
			identifier();
			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << REDEFINES))) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & ((1L << (USAGE - 73)) | (1L << (VALUE - 73)) | (1L << (VALUES - 73)))) != 0)) {
				{
				setState(351);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case REDEFINES:
					{
					setState(347);
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
					setState(348);
					usageGroup();
					}
					break;
				case OCCURS:
					{
					setState(349);
					occurs();
					}
					break;
				case VALUE:
				case VALUES:
					{
					setState(350);
					values();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(356);
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
			setState(358);
			section();
			setState(359);
			identifier();
			setState(369);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_3U) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMP_9) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_3U) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << COMPUTATIONAL_9) | (1L << DISPLAY) | (1L << JUST) | (1L << JUSTIFIED) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << PIC) | (1L << PICTURE) | (1L << REDEFINES))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (SIGN - 64)) | (1L << (USAGE - 64)) | (1L << (VALUE - 64)) | (1L << (VALUES - 64)))) != 0)) {
				{
				setState(367);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
				case 1:
					{
					setState(360);
					justified();
					}
					break;
				case 2:
					{
					setState(361);
					occurs();
					}
					break;
				case 3:
					{
					setState(362);
					pic();
					}
					break;
				case 4:
					{
					setState(363);
					redefines();
					}
					break;
				case 5:
					{
					setState(364);
					usage();
					}
					break;
				case 6:
					{
					setState(365);
					values();
					}
					break;
				case 7:
					{
					setState(366);
					separateSign();
					}
					break;
				}
				}
				setState(371);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BLANK) {
				{
				setState(372);
				match(BLANK);
				setState(374);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHEN) {
					{
					setState(373);
					match(WHEN);
					}
				}

				setState(376);
				match(ZERO);
				}
			}

			setState(379);
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
			setState(381);
			match(LEVEL_NUMBER_66);
			setState(382);
			identifier();
			setState(383);
			renames();
			setState(384);
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
			setState(386);
			match(LEVEL_NUMBER_88);
			setState(387);
			identifier();
			setState(388);
			values();
			setState(389);
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
			setState(398);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(391);
				match(COMMENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(392);
				group();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(393);
				primitive();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(394);
				level66statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(395);
				level88statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(396);
				skipLiteral();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(397);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0089\u0193\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\3\2\6\2"+
		"V\n\2\r\2\16\2W\3\2\5\2[\n\2\3\2\3\2\3\3\3\3\3\3\3\3\5\3c\n\3\3\4\5\4"+
		"f\n\4\3\4\3\4\3\4\5\4k\n\4\3\4\5\4n\n\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u0083\n\7\3\b\3\b\3"+
		"\t\3\t\5\t\u0089\n\t\3\t\3\t\5\t\u008d\n\t\5\t\u008f\n\t\3\t\3\t\5\t\u0093"+
		"\n\t\3\t\7\t\u0096\n\t\f\t\16\t\u0099\13\t\3\n\3\n\5\n\u009d\n\n\3\13"+
		"\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\5\r\u00b3\n\r\3\16\3\16\5\16\u00b7\n\16\3\16\5\16\u00ba\n\16"+
		"\3\16\3\16\3\17\3\17\3\17\3\20\3\20\5\20\u00c3\n\20\3\20\3\20\3\21\3\21"+
		"\5\21\u00c9\n\21\3\21\3\21\5\21\u00cd\n\21\3\21\7\21\u00d0\n\21\f\21\16"+
		"\21\u00d3\13\21\3\22\3\22\3\22\5\22\u00d8\n\22\3\22\5\22\u00db\n\22\3"+
		"\22\5\22\u00de\n\22\3\22\5\22\u00e1\n\22\3\22\5\22\u00e4\n\22\3\23\3\23"+
		"\3\23\3\24\3\24\3\24\3\24\3\24\5\24\u00ee\n\24\3\25\3\25\3\26\3\26\3\27"+
		"\3\27\5\27\u00f6\n\27\5\27\u00f8\n\27\3\27\3\27\3\30\3\30\5\30\u00fe\n"+
		"\30\5\30\u0100\n\30\3\30\3\30\3\31\3\31\5\31\u0106\n\31\3\31\3\31\5\31"+
		"\u010a\n\31\3\31\5\31\u010d\n\31\3\32\3\32\5\32\u0111\n\32\3\33\3\33\3"+
		"\34\3\34\5\34\u0117\n\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\5\35\u0125\n\35\3\36\5\36\u0128\n\36\3\36\3\36\3\36\3"+
		"\36\5\36\u012e\n\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3#\3#\3#\3#\5"+
		"#\u013e\n#\3#\5#\u0141\n#\3#\5#\u0144\n#\5#\u0146\n#\3#\3#\5#\u014a\n"+
		"#\5#\u014c\n#\3#\3#\3#\5#\u0151\n#\5#\u0153\n#\3#\5#\u0156\n#\3$\3$\3"+
		"%\3%\3&\3&\3&\3&\3&\3&\7&\u0162\n&\f&\16&\u0165\13&\3&\3&\3\'\3\'\3\'"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\7\'\u0172\n\'\f\'\16\'\u0175\13\'\3\'\3\'\5\'"+
		"\u0179\n\'\3\'\5\'\u017c\n\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3*"+
		"\3*\3*\3*\3*\3*\3*\5*\u0191\n*\3*\2\2+\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPR\2\17\4\2^^~\u0083\4\2##JJ\4"+
		"\2\6\6  \6\2\7\7\f\35!!\66\66\b\2\7\7\f\r\20\26\31\35!!\66\66\4\2,,II"+
		"\3\2)*\4\2aaww\4\2bbyy\4\2__ss\3\2\678\3\2~\177\3\2?A\2\u01d4\2U\3\2\2"+
		"\2\4b\3\2\2\2\6m\3\2\2\2\bo\3\2\2\2\nq\3\2\2\2\f\u0082\3\2\2\2\16\u0084"+
		"\3\2\2\2\20\u008e\3\2\2\2\22\u009a\3\2\2\2\24\u009e\3\2\2\2\26\u00a0\3"+
		"\2\2\2\30\u00b2\3\2\2\2\32\u00b4\3\2\2\2\34\u00bd\3\2\2\2\36\u00c0\3\2"+
		"\2\2 \u00c6\3\2\2\2\"\u00d4\3\2\2\2$\u00e5\3\2\2\2&\u00e8\3\2\2\2(\u00ef"+
		"\3\2\2\2*\u00f1\3\2\2\2,\u00f7\3\2\2\2.\u00ff\3\2\2\2\60\u0103\3\2\2\2"+
		"\62\u010e\3\2\2\2\64\u0112\3\2\2\2\66\u0116\3\2\2\28\u0124\3\2\2\2:\u012d"+
		"\3\2\2\2<\u012f\3\2\2\2>\u0131\3\2\2\2@\u0133\3\2\2\2B\u0135\3\2\2\2D"+
		"\u0155\3\2\2\2F\u0157\3\2\2\2H\u0159\3\2\2\2J\u015b\3\2\2\2L\u0168\3\2"+
		"\2\2N\u017f\3\2\2\2P\u0184\3\2\2\2R\u0190\3\2\2\2TV\5R*\2UT\3\2\2\2VW"+
		"\3\2\2\2WU\3\2\2\2WX\3\2\2\2XZ\3\2\2\2Y[\7\u0088\2\2ZY\3\2\2\2Z[\3\2\2"+
		"\2[\\\3\2\2\2\\]\7\2\2\3]\3\3\2\2\2^c\7}\2\2_c\5\6\4\2`c\5\n\6\2ac\5\30"+
		"\r\2b^\3\2\2\2b_\3\2\2\2b`\3\2\2\2ba\3\2\2\2c\5\3\2\2\2df\5\66\34\2ed"+
		"\3\2\2\2ef\3\2\2\2fg\3\2\2\2gn\7\u0085\2\2hn\7P\2\2ik\5\66\34\2ji\3\2"+
		"\2\2jk\3\2\2\2kl\3\2\2\2ln\5\b\5\2me\3\2\2\2mh\3\2\2\2mj\3\2\2\2n\7\3"+
		"\2\2\2op\t\2\2\2p\t\3\2\2\2qr\t\3\2\2r\13\3\2\2\2s\u0083\7\u0087\2\2t"+
		"\u0083\7\3\2\2u\u0083\7_\2\2v\u0083\7`\2\2w\u0083\7f\2\2x\u0083\7a\2\2"+
		"y\u0083\3\2\2\2z\u0083\7b\2\2{\u0083\7c\2\2|\u0083\7g\2\2}\u0083\7d\2"+
		"\2~\u0083\7h\2\2\177\u0083\7e\2\2\u0080\u0083\7i\2\2\u0081\u0083\7\u0086"+
		"\2\2\u0082s\3\2\2\2\u0082t\3\2\2\2\u0082u\3\2\2\2\u0082v\3\2\2\2\u0082"+
		"w\3\2\2\2\u0082x\3\2\2\2\u0082y\3\2\2\2\u0082z\3\2\2\2\u0082{\3\2\2\2"+
		"\u0082|\3\2\2\2\u0082}\3\2\2\2\u0082~\3\2\2\2\u0082\177\3\2\2\2\u0082"+
		"\u0080\3\2\2\2\u0082\u0081\3\2\2\2\u0083\r\3\2\2\2\u0084\u0085\7\3\2\2"+
		"\u0085\17\3\2\2\2\u0086\u0088\7M\2\2\u0087\u0089\7(\2\2\u0088\u0087\3"+
		"\2\2\2\u0088\u0089\3\2\2\2\u0089\u008f\3\2\2\2\u008a\u008c\7N\2\2\u008b"+
		"\u008d\7\5\2\2\u008c\u008b\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008f\3\2"+
		"\2\2\u008e\u0086\3\2\2\2\u008e\u008a\3\2\2\2\u008f\u0090\3\2\2\2\u0090"+
		"\u0097\5\22\n\2\u0091\u0093\7T\2\2\u0092\u0091\3\2\2\2\u0092\u0093\3\2"+
		"\2\2\u0093\u0094\3\2\2\2\u0094\u0096\5\22\n\2\u0095\u0092\3\2\2\2\u0096"+
		"\u0099\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098\21\3\2\2"+
		"\2\u0099\u0097\3\2\2\2\u009a\u009c\5\24\13\2\u009b\u009d\5\26\f\2\u009c"+
		"\u009b\3\2\2\2\u009c\u009d\3\2\2\2\u009d\23\3\2\2\2\u009e\u009f\5\4\3"+
		"\2\u009f\25\3\2\2\2\u00a0\u00a1\5\16\b\2\u00a1\u00a2\5\4\3\2\u00a2\27"+
		"\3\2\2\2\u00a3\u00a4\7\4\2\2\u00a4\u00b3\5\4\3\2\u00a5\u00b3\7%\2\2\u00a6"+
		"\u00b3\7&\2\2\u00a7\u00b3\7.\2\2\u00a8\u00b3\7/\2\2\u00a9\u00b3\7\60\2"+
		"\2\u00aa\u00b3\7\61\2\2\u00ab\u00b3\79\2\2\u00ac\u00b3\7:\2\2\u00ad\u00b3"+
		"\7C\2\2\u00ae\u00b3\7D\2\2\u00af\u00b3\7P\2\2\u00b0\u00b3\7Q\2\2\u00b1"+
		"\u00b3\7R\2\2\u00b2\u00a3\3\2\2\2\u00b2\u00a5\3\2\2\2\u00b2\u00a6\3\2"+
		"\2\2\u00b2\u00a7\3\2\2\2\u00b2\u00a8\3\2\2\2\u00b2\u00a9\3\2\2\2\u00b2"+
		"\u00aa\3\2\2\2\u00b2\u00ab\3\2\2\2\u00b2\u00ac\3\2\2\2\u00b2\u00ad\3\2"+
		"\2\2\u00b2\u00ae\3\2\2\2\u00b2\u00af\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b2"+
		"\u00b1\3\2\2\2\u00b3\31\3\2\2\2\u00b4\u00b6\t\4\2\2\u00b5\u00b7\7+\2\2"+
		"\u00b6\u00b5\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b9\3\2\2\2\u00b8\u00ba"+
		"\7(\2\2\u00b9\u00b8\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb"+
		"\u00bc\5\f\7\2\u00bc\33\3\2\2\2\u00bd\u00be\7H\2\2\u00be\u00bf\5\b\5\2"+
		"\u00bf\35\3\2\2\2\u00c0\u00c2\7\37\2\2\u00c1\u00c3\7\65\2\2\u00c2\u00c1"+
		"\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4\u00c5\5\f\7\2\u00c5"+
		"\37\3\2\2\2\u00c6\u00c8\7\'\2\2\u00c7\u00c9\7\t\2\2\u00c8\u00c7\3\2\2"+
		"\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\u00d1\5\f\7\2\u00cb\u00cd"+
		"\7T\2\2\u00cc\u00cb\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce"+
		"\u00d0\7\u0087\2\2\u00cf\u00cc\3\2\2\2\u00d0\u00d3\3\2\2\2\u00d1\u00cf"+
		"\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2!\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d4"+
		"\u00d5\7\64\2\2\u00d5\u00d7\5\b\5\2\u00d6\u00d8\5\34\17\2\u00d7\u00d6"+
		"\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00da\3\2\2\2\u00d9\u00db\7G\2\2\u00da"+
		"\u00d9\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00dd\3\2\2\2\u00dc\u00de\5\36"+
		"\20\2\u00dd\u00dc\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00e0\3\2\2\2\u00df"+
		"\u00e1\5\32\16\2\u00e0\u00df\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e3\3"+
		"\2\2\2\u00e2\u00e4\5 \21\2\u00e3\u00e2\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4"+
		"#\3\2\2\2\u00e5\u00e6\7;\2\2\u00e6\u00e7\5\f\7\2\u00e7%\3\2\2\2\u00e8"+
		"\u00e9\7<\2\2\u00e9\u00ed\5\f\7\2\u00ea\u00eb\5\16\b\2\u00eb\u00ec\5\f"+
		"\7\2\u00ec\u00ee\3\2\2\2\u00ed\u00ea\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee"+
		"\'\3\2\2\2\u00ef\u00f0\t\5\2\2\u00f0)\3\2\2\2\u00f1\u00f2\t\6\2\2\u00f2"+
		"+\3\2\2\2\u00f3\u00f5\7K\2\2\u00f4\u00f6\7(\2\2\u00f5\u00f4\3\2\2\2\u00f5"+
		"\u00f6\3\2\2\2\u00f6\u00f8\3\2\2\2\u00f7\u00f3\3\2\2\2\u00f7\u00f8\3\2"+
		"\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00fa\5(\25\2\u00fa-\3\2\2\2\u00fb\u00fd"+
		"\7K\2\2\u00fc\u00fe\7(\2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe"+
		"\u0100\3\2\2\2\u00ff\u00fb\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\3\2"+
		"\2\2\u0101\u0102\5*\26\2\u0102/\3\2\2\2\u0103\u0105\7B\2\2\u0104\u0106"+
		"\7(\2\2\u0105\u0104\3\2\2\2\u0105\u0106\3\2\2\2\u0106\u0107\3\2\2\2\u0107"+
		"\u0109\t\7\2\2\u0108\u010a\7>\2\2\u0109\u0108\3\2\2\2\u0109\u010a\3\2"+
		"\2\2\u010a\u010c\3\2\2\2\u010b\u010d\7\n\2\2\u010c\u010b\3\2\2\2\u010c"+
		"\u010d\3\2\2\2\u010d\61\3\2\2\2\u010e\u0110\t\b\2\2\u010f\u0111\7=\2\2"+
		"\u0110\u010f\3\2\2\2\u0110\u0111\3\2\2\2\u0111\63\3\2\2\2\u0112\u0113"+
		"\7\\\2\2\u0113\65\3\2\2\2\u0114\u0117\7X\2\2\u0115\u0117\7W\2\2\u0116"+
		"\u0114\3\2\2\2\u0116\u0115\3\2\2\2\u0117\67\3\2\2\2\u0118\u0125\7^\2\2"+
		"\u0119\u0125\7c\2\2\u011a\u0125\7`\2\2\u011b\u0125\7d\2\2\u011c\u0125"+
		"\7e\2\2\u011d\u0125\7j\2\2\u011e\u0125\7k\2\2\u011f\u0125\7l\2\2\u0120"+
		"\u0125\7m\2\2\u0121\u0125\7n\2\2\u0122\u0125\7o\2\2\u0123\u0125\7p\2\2"+
		"\u0124\u0118\3\2\2\2\u0124\u0119\3\2\2\2\u0124\u011a\3\2\2\2\u0124\u011b"+
		"\3\2\2\2\u0124\u011c\3\2\2\2\u0124\u011d\3\2\2\2\u0124\u011e\3\2\2\2\u0124"+
		"\u011f\3\2\2\2\u0124\u0120\3\2\2\2\u0124\u0121\3\2\2\2\u0124\u0122\3\2"+
		"\2\2\u0124\u0123\3\2\2\2\u01259\3\2\2\2\u0126\u0128\5\66\34\2\u0127\u0126"+
		"\3\2\2\2\u0127\u0128\3\2\2\2\u0128\u0129\3\2\2\2\u0129\u012e\58\35\2\u012a"+
		"\u012b\58\35\2\u012b\u012c\5\66\34\2\u012c\u012e\3\2\2\2\u012d\u0127\3"+
		"\2\2\2\u012d\u012a\3\2\2\2\u012e;\3\2\2\2\u012f\u0130\t\t\2\2\u0130=\3"+
		"\2\2\2\u0131\u0132\t\n\2\2\u0132?\3\2\2\2\u0133\u0134\t\13\2\2\u0134A"+
		"\3\2\2\2\u0135\u0136\t\f\2\2\u0136C\3\2\2\2\u0137\u0145\5B\"\2\u0138\u0146"+
		"\5<\37\2\u0139\u0146\5@!\2\u013a\u0146\5> \2\u013b\u013d\5:\36\2\u013c"+
		"\u013e\5,\27\2\u013d\u013c\3\2\2\2\u013d\u013e\3\2\2\2\u013e\u0144\3\2"+
		"\2\2\u013f\u0141\5,\27\2\u0140\u013f\3\2\2\2\u0140\u0141\3\2\2\2\u0141"+
		"\u0142\3\2\2\2\u0142\u0144\5:\36\2\u0143\u013b\3\2\2\2\u0143\u0140\3\2"+
		"\2\2\u0144\u0146\3\2\2\2\u0145\u0138\3\2\2\2\u0145\u0139\3\2\2\2\u0145"+
		"\u013a\3\2\2\2\u0145\u0143\3\2\2\2\u0146\u0156\3\2\2\2\u0147\u0149\7K"+
		"\2\2\u0148\u014a\7(\2\2\u0149\u0148\3\2\2\2\u0149\u014a\3\2\2\2\u014a"+
		"\u014c\3\2\2\2\u014b\u0147\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u014d\3\2"+
		"\2\2\u014d\u0156\7\16\2\2\u014e\u0150\7K\2\2\u014f\u0151\7(\2\2\u0150"+
		"\u014f\3\2\2\2\u0150\u0151\3\2\2\2\u0151\u0153\3\2\2\2\u0152\u014e\3\2"+
		"\2\2\u0152\u0153\3\2\2\2\u0153\u0154\3\2\2\2\u0154\u0156\7\17\2\2\u0155"+
		"\u0137\3\2\2\2\u0155\u014b\3\2\2\2\u0155\u0152\3\2\2\2\u0156E\3\2\2\2"+
		"\u0157\u0158\t\r\2\2\u0158G\3\2\2\2\u0159\u015a\t\16\2\2\u015aI\3\2\2"+
		"\2\u015b\u015c\5F$\2\u015c\u0163\5\f\7\2\u015d\u0162\5$\23\2\u015e\u0162"+
		"\5.\30\2\u015f\u0162\5\"\22\2\u0160\u0162\5\20\t\2\u0161\u015d\3\2\2\2"+
		"\u0161\u015e\3\2\2\2\u0161\u015f\3\2\2\2\u0161\u0160\3\2\2\2\u0162\u0165"+
		"\3\2\2\2\u0163\u0161\3\2\2\2\u0163\u0164\3\2\2\2\u0164\u0166\3\2\2\2\u0165"+
		"\u0163\3\2\2\2\u0166\u0167\5\64\33\2\u0167K\3\2\2\2\u0168\u0169\5F$\2"+
		"\u0169\u0173\5\f\7\2\u016a\u0172\5\62\32\2\u016b\u0172\5\"\22\2\u016c"+
		"\u0172\5D#\2\u016d\u0172\5$\23\2\u016e\u0172\5,\27\2\u016f\u0172\5\20"+
		"\t\2\u0170\u0172\5\60\31\2\u0171\u016a\3\2\2\2\u0171\u016b\3\2\2\2\u0171"+
		"\u016c\3\2\2\2\u0171\u016d\3\2\2\2\u0171\u016e\3\2\2\2\u0171\u016f\3\2"+
		"\2\2\u0171\u0170\3\2\2\2\u0172\u0175\3\2\2\2\u0173\u0171\3\2\2\2\u0173"+
		"\u0174\3\2\2\2\u0174\u017b\3\2\2\2\u0175\u0173\3\2\2\2\u0176\u0178\7\b"+
		"\2\2\u0177\u0179\7O\2\2\u0178\u0177\3\2\2\2\u0178\u0179\3\2\2\2\u0179"+
		"\u017a\3\2\2\2\u017a\u017c\7P\2\2\u017b\u0176\3\2\2\2\u017b\u017c\3\2"+
		"\2\2\u017c\u017d\3\2\2\2\u017d\u017e\5\64\33\2\u017eM\3\2\2\2\u017f\u0180"+
		"\7\u0080\2\2\u0180\u0181\5\f\7\2\u0181\u0182\5&\24\2\u0182\u0183\5\64"+
		"\33\2\u0183O\3\2\2\2\u0184\u0185\7\u0082\2\2\u0185\u0186\5\f\7\2\u0186"+
		"\u0187\5\20\t\2\u0187\u0188\5\64\33\2\u0188Q\3\2\2\2\u0189\u0191\7]\2"+
		"\2\u018a\u0191\5J&\2\u018b\u0191\5L\'\2\u018c\u0191\5N(\2\u018d\u0191"+
		"\5P)\2\u018e\u0191\5H%\2\u018f\u0191\5\64\33\2\u0190\u0189\3\2\2\2\u0190"+
		"\u018a\3\2\2\2\u0190\u018b\3\2\2\2\u0190\u018c\3\2\2\2\u0190\u018d\3\2"+
		"\2\2\u0190\u018e\3\2\2\2\u0190\u018f\3\2\2\2\u0191S\3\2\2\28WZbejm\u0082"+
		"\u0088\u008c\u008e\u0092\u0097\u009c\u00b2\u00b6\u00b9\u00c2\u00c8\u00cc"+
		"\u00d1\u00d7\u00da\u00dd\u00e0\u00e3\u00ed\u00f5\u00f7\u00fd\u00ff\u0105"+
		"\u0109\u010c\u0110\u0116\u0124\u0127\u012d\u013d\u0140\u0143\u0145\u0149"+
		"\u014b\u0150\u0152\u0155\u0161\u0163\u0171\u0173\u0178\u017b\u0190";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}