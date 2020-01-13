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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		THRU_OR_THROUGH=1, ALL=2, ARE=3, ASCENDING=4, BINARY=5, BLANK=6, BY=7, 
		CHARACTER=8, CHARACTERS=9, COMP=10, COMP_0=11, COMP_1=12, COMP_2=13, COMP_3=14, 
		COMP_4=15, COMP_5=16, COMPUTATIONAL=17, COMPUTATIONAL_0=18, COMPUTATIONAL_1=19, 
		COMPUTATIONAL_2=20, COMPUTATIONAL_3=21, COMPUTATIONAL_4=22, COMPUTATIONAL_5=23, 
		COPY=24, DEPENDING=25, DESCENDING=26, DISPLAY=27, EXTERNAL=28, FALSE=29, 
		FROM=30, HIGH_VALUE=31, HIGH_VALUES=32, INDEXED=33, IS=34, JUST=35, JUSTIFIED=36, 
		KEY=37, LEADING=38, LEFT=39, LOW_VALUE=40, LOW_VALUES=41, NULL=42, NULLS=43, 
		NUMBER=44, NUMERIC=45, OCCURS=46, ON=47, PACKED_DECIMAL=48, PIC=49, PICTURE=50, 
		QUOTE=51, QUOTES=52, REDEFINES=53, RENAMES=54, RIGHT=55, SEPARATE=56, 
		SKIP1=57, SKIP2=58, SKIP3=59, SIGN=60, SPACE=61, SPACES=62, THROUGH=63, 
		THRU=64, TIMES=65, TO=66, TRAILING=67, TRUE=68, USAGE=69, USING=70, VALUE=71, 
		VALUES=72, WHEN=73, ZERO=74, ZEROS=75, ZEROES=76, DOUBLEQUOTE=77, COMMACHAR=78, 
		DOT=79, LPARENCHAR=80, MINUSCHAR=81, PLUSCHAR=82, RPARENCHAR=83, SINGLEQUOTE=84, 
		SLASHCHAR=85, TERMINAL=86, COMMENT=87, NINES=88, A_S=89, P_S=90, X_S=91, 
		S_S=92, Z_S=93, V_S=94, P_NS=95, S_NS=96, Z_NS=97, V_NS=98, PRECISION_9_EXPLICIT_DOT=99, 
		PRECISION_9_DECIMAL_SCALED=100, PRECISION_9_SCALED=101, PRECISION_9_SCALED_LEAD=102, 
		PRECISION_Z_EXPLICIT_DOT=103, PRECISION_Z_DECIMAL_SCALED=104, PRECISION_Z_SCALED=105, 
		LENGTH_TYPE_9=106, LENGTH_TYPE_9_1=107, LENGTH_TYPE_A=108, LENGTH_TYPE_A_1=109, 
		LENGTH_TYPE_P=110, LENGTH_TYPE_P_1=111, LENGTH_TYPE_X=112, LENGTH_TYPE_X_1=113, 
		LENGTH_TYPE_Z=114, LENGTH_TYPE_Z_1=115, STRINGLITERAL=116, LEVEL_ROOT=117, 
		LEVEL_REGULAR=118, LEVEL_NUMBER_66=119, LEVEL_NUMBER_77=120, LEVEL_NUMBER_88=121, 
		INTEGERLITERAL=122, POSITIVELITERAL=123, NUMERICLITERAL=124, SINGLE_QUOTED_IDENTIFIER=125, 
		IDENTIFIER=126, CONTROL_Z=127, WS=128;
	public static final int
		RULE_main = 0, RULE_literal = 1, RULE_numericLiteral = 2, RULE_integerLiteral = 3, 
		RULE_booleanLiteral = 4, RULE_identifier = 5, RULE_thru = 6, RULE_values = 7, 
		RULE_valuesFromTo = 8, RULE_valuesFrom = 9, RULE_valuesTo = 10, RULE_specialValues = 11, 
		RULE_sorts = 12, RULE_occursTo = 13, RULE_dependingOn = 14, RULE_indexedBy = 15, 
		RULE_occurs = 16, RULE_redefines = 17, RULE_renames = 18, RULE_usageLiteral = 19, 
		RULE_groupUsageLiteral = 20, RULE_usage = 21, RULE_usageGroup = 22, RULE_separateSign = 23, 
		RULE_justified = 24, RULE_term = 25, RULE_plusMinus = 26, RULE_precision9 = 27, 
		RULE_signPrecision9 = 28, RULE_alphaX = 29, RULE_alphaA = 30, RULE_pictureLiteral = 31, 
		RULE_pic = 32, RULE_section = 33, RULE_skipLiteral = 34, RULE_group = 35, 
		RULE_primitive = 36, RULE_level66statement = 37, RULE_level88statement = 38, 
		RULE_item = 39;
	private static String[] makeRuleNames() {
		return new String[] {
			"main", "literal", "numericLiteral", "integerLiteral", "booleanLiteral", 
			"identifier", "thru", "values", "valuesFromTo", "valuesFrom", "valuesTo", 
			"specialValues", "sorts", "occursTo", "dependingOn", "indexedBy", "occurs", 
			"redefines", "renames", "usageLiteral", "groupUsageLiteral", "usage", 
			"usageGroup", "separateSign", "justified", "term", "plusMinus", "precision9", 
			"signPrecision9", "alphaX", "alphaA", "pictureLiteral", "pic", "section", 
			"skipLiteral", "group", "primitive", "level66statement", "level88statement", 
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
			null, null, null, null, null, "'\"'", "','", "'.'", "'('", "'-'", "'+'", 
			"')'", "'''", "'/'", null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "'01'", 
			null, "'66'", "'77'", "'88'", null, null, null, null, null, "'\u001A'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "THRU_OR_THROUGH", "ALL", "ARE", "ASCENDING", "BINARY", "BLANK", 
			"BY", "CHARACTER", "CHARACTERS", "COMP", "COMP_0", "COMP_1", "COMP_2", 
			"COMP_3", "COMP_4", "COMP_5", "COMPUTATIONAL", "COMPUTATIONAL_0", "COMPUTATIONAL_1", 
			"COMPUTATIONAL_2", "COMPUTATIONAL_3", "COMPUTATIONAL_4", "COMPUTATIONAL_5", 
			"COPY", "DEPENDING", "DESCENDING", "DISPLAY", "EXTERNAL", "FALSE", "FROM", 
			"HIGH_VALUE", "HIGH_VALUES", "INDEXED", "IS", "JUST", "JUSTIFIED", "KEY", 
			"LEADING", "LEFT", "LOW_VALUE", "LOW_VALUES", "NULL", "NULLS", "NUMBER", 
			"NUMERIC", "OCCURS", "ON", "PACKED_DECIMAL", "PIC", "PICTURE", "QUOTE", 
			"QUOTES", "REDEFINES", "RENAMES", "RIGHT", "SEPARATE", "SKIP1", "SKIP2", 
			"SKIP3", "SIGN", "SPACE", "SPACES", "THROUGH", "THRU", "TIMES", "TO", 
			"TRAILING", "TRUE", "USAGE", "USING", "VALUE", "VALUES", "WHEN", "ZERO", 
			"ZEROS", "ZEROES", "DOUBLEQUOTE", "COMMACHAR", "DOT", "LPARENCHAR", "MINUSCHAR", 
			"PLUSCHAR", "RPARENCHAR", "SINGLEQUOTE", "SLASHCHAR", "TERMINAL", "COMMENT", 
			"NINES", "A_S", "P_S", "X_S", "S_S", "Z_S", "V_S", "P_NS", "S_NS", "Z_NS", 
			"V_NS", "PRECISION_9_EXPLICIT_DOT", "PRECISION_9_DECIMAL_SCALED", "PRECISION_9_SCALED", 
			"PRECISION_9_SCALED_LEAD", "PRECISION_Z_EXPLICIT_DOT", "PRECISION_Z_DECIMAL_SCALED", 
			"PRECISION_Z_SCALED", "LENGTH_TYPE_9", "LENGTH_TYPE_9_1", "LENGTH_TYPE_A", 
			"LENGTH_TYPE_A_1", "LENGTH_TYPE_P", "LENGTH_TYPE_P_1", "LENGTH_TYPE_X", 
			"LENGTH_TYPE_X_1", "LENGTH_TYPE_Z", "LENGTH_TYPE_Z_1", "STRINGLITERAL", 
			"LEVEL_ROOT", "LEVEL_REGULAR", "LEVEL_NUMBER_66", "LEVEL_NUMBER_77", 
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
			setState(81); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(80);
				item();
				}
				}
				setState(83); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SKIP1) | (1L << SKIP2) | (1L << SKIP3))) != 0) || ((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & ((1L << (TERMINAL - 86)) | (1L << (COMMENT - 86)) | (1L << (LEVEL_ROOT - 86)) | (1L << (LEVEL_REGULAR - 86)) | (1L << (LEVEL_NUMBER_66 - 86)) | (1L << (LEVEL_NUMBER_88 - 86)))) != 0) );
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONTROL_Z) {
				{
				setState(85);
				match(CONTROL_Z);
				}
			}

			setState(88);
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
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				match(STRINGLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				numericLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				booleanLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(93);
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
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(96);
					plusMinus();
					}
				}

				setState(99);
				match(NUMERICLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(100);
				match(ZERO);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(102);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(101);
					plusMinus();
					}
				}

				setState(104);
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
			setState(107);
			_la = _input.LA(1);
			if ( !(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & ((1L << (NINES - 88)) | (1L << (LEVEL_ROOT - 88)) | (1L << (LEVEL_REGULAR - 88)) | (1L << (LEVEL_NUMBER_66 - 88)) | (1L << (LEVEL_NUMBER_77 - 88)) | (1L << (LEVEL_NUMBER_88 - 88)) | (1L << (INTEGERLITERAL - 88)))) != 0)) ) {
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
			setState(109);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			_la = _input.LA(1);
			if ( !(_la==THRU_OR_THROUGH || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & ((1L << (A_S - 89)) | (1L << (P_S - 89)) | (1L << (X_S - 89)) | (1L << (S_S - 89)) | (1L << (Z_S - 89)) | (1L << (V_S - 89)) | (1L << (P_NS - 89)) | (1L << (S_NS - 89)) | (1L << (Z_NS - 89)) | (1L << (V_NS - 89)) | (1L << (SINGLE_QUOTED_IDENTIFIER - 89)) | (1L << (IDENTIFIER - 89)))) != 0)) ) {
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
			setState(113);
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
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
				{
				setState(115);
				match(VALUE);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(116);
					match(IS);
					}
				}

				}
				break;
			case VALUES:
				{
				setState(119);
				match(VALUES);
				setState(121);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ARE) {
					{
					setState(120);
					match(ARE);
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(125);
			valuesFromTo();
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALL) | (1L << FALSE) | (1L << HIGH_VALUE) | (1L << HIGH_VALUES) | (1L << LOW_VALUE) | (1L << LOW_VALUES) | (1L << NULL) | (1L << NULLS) | (1L << QUOTE) | (1L << QUOTES) | (1L << SPACE) | (1L << SPACES))) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (TRUE - 68)) | (1L << (ZERO - 68)) | (1L << (ZEROS - 68)) | (1L << (ZEROES - 68)) | (1L << (COMMACHAR - 68)) | (1L << (MINUSCHAR - 68)) | (1L << (PLUSCHAR - 68)) | (1L << (NINES - 68)) | (1L << (STRINGLITERAL - 68)) | (1L << (LEVEL_ROOT - 68)) | (1L << (LEVEL_REGULAR - 68)) | (1L << (LEVEL_NUMBER_66 - 68)) | (1L << (LEVEL_NUMBER_77 - 68)) | (1L << (LEVEL_NUMBER_88 - 68)) | (1L << (INTEGERLITERAL - 68)) | (1L << (NUMERICLITERAL - 68)))) != 0)) {
				{
				{
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(126);
					match(COMMACHAR);
					}
				}

				setState(129);
				valuesFromTo();
				}
				}
				setState(134);
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
			setState(135);
			valuesFrom();
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(136);
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
			setState(139);
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
			setState(141);
			thru();
			setState(142);
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
			setState(159);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(144);
				match(ALL);
				setState(145);
				literal();
				}
				break;
			case HIGH_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				match(HIGH_VALUE);
				}
				break;
			case HIGH_VALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(147);
				match(HIGH_VALUES);
				}
				break;
			case LOW_VALUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(148);
				match(LOW_VALUE);
				}
				break;
			case LOW_VALUES:
				enterOuterAlt(_localctx, 5);
				{
				setState(149);
				match(LOW_VALUES);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(150);
				match(NULL);
				}
				break;
			case NULLS:
				enterOuterAlt(_localctx, 7);
				{
				setState(151);
				match(NULLS);
				}
				break;
			case QUOTE:
				enterOuterAlt(_localctx, 8);
				{
				setState(152);
				match(QUOTE);
				}
				break;
			case QUOTES:
				enterOuterAlt(_localctx, 9);
				{
				setState(153);
				match(QUOTES);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 10);
				{
				setState(154);
				match(SPACE);
				}
				break;
			case SPACES:
				enterOuterAlt(_localctx, 11);
				{
				setState(155);
				match(SPACES);
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 12);
				{
				setState(156);
				match(ZERO);
				}
				break;
			case ZEROS:
				enterOuterAlt(_localctx, 13);
				{
				setState(157);
				match(ZEROS);
				}
				break;
			case ZEROES:
				enterOuterAlt(_localctx, 14);
				{
				setState(158);
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
			setState(161);
			_la = _input.LA(1);
			if ( !(_la==ASCENDING || _la==DESCENDING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY) {
				{
				setState(162);
				match(KEY);
				}
			}

			setState(166);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(165);
				match(IS);
				}
			}

			setState(168);
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
			setState(170);
			match(TO);
			setState(171);
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
			setState(173);
			match(DEPENDING);
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(174);
				match(ON);
				}
			}

			setState(177);
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
			setState(179);
			match(INDEXED);
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BY) {
				{
				setState(180);
				match(BY);
				}
			}

			setState(183);
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
			setState(185);
			match(OCCURS);
			setState(186);
			integerLiteral();
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(187);
				occursTo();
				}
			}

			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TIMES) {
				{
				setState(190);
				match(TIMES);
				}
			}

			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEPENDING) {
				{
				setState(193);
				dependingOn();
				}
			}

			setState(197);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASCENDING || _la==DESCENDING) {
				{
				setState(196);
				sorts();
				}
			}

			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEXED) {
				{
				setState(199);
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
			setState(202);
			match(REDEFINES);
			setState(203);
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
			setState(205);
			match(RENAMES);
			setState(206);
			identifier();
			setState(210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(207);
				thru();
				setState(208);
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
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybookParser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybookParser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybookParser.COMPUTATIONAL, 0); }
		public TerminalNode COMP_0() { return getToken(copybookParser.COMP_0, 0); }
		public TerminalNode COMP_1() { return getToken(copybookParser.COMP_1, 0); }
		public TerminalNode COMP_2() { return getToken(copybookParser.COMP_2, 0); }
		public TerminalNode COMP_3() { return getToken(copybookParser.COMP_3, 0); }
		public TerminalNode COMP_4() { return getToken(copybookParser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybookParser.COMP_5, 0); }
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
			setState(212);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0)) ) {
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
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybookParser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybookParser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybookParser.COMPUTATIONAL, 0); }
		public TerminalNode COMP_0() { return getToken(copybookParser.COMP_0, 0); }
		public TerminalNode COMP_3() { return getToken(copybookParser.COMP_3, 0); }
		public TerminalNode COMP_4() { return getToken(copybookParser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybookParser.COMP_5, 0); }
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
			setState(214);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0)) ) {
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
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(216);
				match(USAGE);
				setState(218);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(217);
					match(IS);
					}
				}

				}
			}

			setState(222);
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
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(224);
				match(USAGE);
				setState(226);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(225);
					match(IS);
					}
				}

				}
			}

			setState(230);
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
			setState(232);
			match(SIGN);
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(233);
				match(IS);
				}
			}

			setState(236);
			_la = _input.LA(1);
			if ( !(_la==LEADING || _la==TRAILING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(238);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEPARATE) {
				{
				setState(237);
				match(SEPARATE);
				}
			}

			setState(241);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CHARACTER) {
				{
				setState(240);
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
			setState(243);
			_la = _input.LA(1);
			if ( !(_la==JUST || _la==JUSTIFIED) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RIGHT) {
				{
				setState(244);
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
			setState(247);
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
			setState(251);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUSCHAR:
				_localctx = new PlusContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(249);
				match(PLUSCHAR);
				}
				break;
			case MINUSCHAR:
				_localctx = new MinusContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
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
			setState(265);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NINES:
				_localctx = new Precision9NinesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(253);
				match(NINES);
				}
				break;
			case S_S:
				_localctx = new Precision9SsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(254);
				match(S_S);
				}
				break;
			case P_S:
				_localctx = new Precision9PsContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(255);
				match(P_S);
				}
				break;
			case Z_S:
				_localctx = new Precision9ZsContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(256);
				match(Z_S);
				}
				break;
			case V_S:
				_localctx = new Precision9VsContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(257);
				match(V_S);
				}
				break;
			case PRECISION_9_EXPLICIT_DOT:
				_localctx = new Precision9ExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(258);
				match(PRECISION_9_EXPLICIT_DOT);
				}
				break;
			case PRECISION_9_DECIMAL_SCALED:
				_localctx = new Precision9DecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(259);
				match(PRECISION_9_DECIMAL_SCALED);
				}
				break;
			case PRECISION_9_SCALED:
				_localctx = new Precision9ScaledContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(260);
				match(PRECISION_9_SCALED);
				}
				break;
			case PRECISION_9_SCALED_LEAD:
				_localctx = new Precision9ScaledLeadContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(261);
				match(PRECISION_9_SCALED_LEAD);
				}
				break;
			case PRECISION_Z_EXPLICIT_DOT:
				_localctx = new PrecisionZExplicitDotContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(262);
				match(PRECISION_Z_EXPLICIT_DOT);
				}
				break;
			case PRECISION_Z_DECIMAL_SCALED:
				_localctx = new PrecisionZDecimalScaledContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(263);
				match(PRECISION_Z_DECIMAL_SCALED);
				}
				break;
			case PRECISION_Z_SCALED:
				_localctx = new PrecisionZScaledContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(264);
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
			setState(274);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				_localctx = new LeadingSignContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(268);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(267);
					plusMinus();
					}
				}

				setState(270);
				precision9();
				}
				}
				break;
			case 2:
				_localctx = new TrailingSignContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(271);
				precision9();
				setState(272);
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
			setState(276);
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
		enterRule(_localctx, 60, RULE_alphaA);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
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
		enterRule(_localctx, 62, RULE_pictureLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(280);
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
		public SignPrecision9Context signPrecision9() {
			return getRuleContext(SignPrecision9Context.class,0);
		}
		public UsageContext usage() {
			return getRuleContext(UsageContext.class,0);
		}
		public TerminalNode COMP_1() { return getToken(copybookParser.COMP_1, 0); }
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
		enterRule(_localctx, 64, RULE_pic);
		int _la;
		try {
			setState(299);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PIC:
			case PICTURE:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				pictureLiteral();
				setState(295);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case X_S:
				case LENGTH_TYPE_X:
					{
					setState(283);
					alphaX();
					}
					break;
				case A_S:
				case LENGTH_TYPE_A:
					{
					setState(284);
					alphaA();
					}
					break;
				case BINARY:
				case COMP:
				case COMP_0:
				case COMP_1:
				case COMP_2:
				case COMP_3:
				case COMP_4:
				case COMP_5:
				case COMPUTATIONAL:
				case COMPUTATIONAL_0:
				case COMPUTATIONAL_1:
				case COMPUTATIONAL_2:
				case COMPUTATIONAL_3:
				case COMPUTATIONAL_4:
				case COMPUTATIONAL_5:
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
					setState(293);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
					case 1:
						{
						setState(285);
						signPrecision9();
						setState(287);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
						case 1:
							{
							setState(286);
							usage();
							}
							break;
						}
						}
						break;
					case 2:
						{
						setState(290);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0) || _la==USAGE) {
							{
							setState(289);
							usage();
							}
						}

						setState(292);
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
			case COMP_1:
				enterOuterAlt(_localctx, 2);
				{
				setState(297);
				match(COMP_1);
				}
				break;
			case COMP_2:
				enterOuterAlt(_localctx, 3);
				{
				setState(298);
				match(COMP_2);
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
		enterRule(_localctx, 66, RULE_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
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
		enterRule(_localctx, 68, RULE_skipLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303);
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
		enterRule(_localctx, 70, RULE_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			section();
			setState(306);
			identifier();
			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << REDEFINES))) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & ((1L << (USAGE - 69)) | (1L << (VALUE - 69)) | (1L << (VALUES - 69)))) != 0)) {
				{
				setState(311);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case REDEFINES:
					{
					setState(307);
					redefines();
					}
					break;
				case BINARY:
				case COMP:
				case COMP_0:
				case COMP_3:
				case COMP_4:
				case COMP_5:
				case COMPUTATIONAL:
				case COMPUTATIONAL_0:
				case COMPUTATIONAL_3:
				case COMPUTATIONAL_4:
				case COMPUTATIONAL_5:
				case DISPLAY:
				case PACKED_DECIMAL:
				case USAGE:
					{
					setState(308);
					usageGroup();
					}
					break;
				case OCCURS:
					{
					setState(309);
					occurs();
					}
					break;
				case VALUE:
				case VALUES:
					{
					setState(310);
					values();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(315);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(316);
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
		enterRule(_localctx, 72, RULE_primitive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			section();
			setState(319);
			identifier();
			setState(329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << JUST) | (1L << JUSTIFIED) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << PIC) | (1L << PICTURE) | (1L << REDEFINES) | (1L << SIGN))) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & ((1L << (USAGE - 69)) | (1L << (VALUE - 69)) | (1L << (VALUES - 69)))) != 0)) {
				{
				setState(327);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
				case 1:
					{
					setState(320);
					justified();
					}
					break;
				case 2:
					{
					setState(321);
					occurs();
					}
					break;
				case 3:
					{
					setState(322);
					pic();
					}
					break;
				case 4:
					{
					setState(323);
					redefines();
					}
					break;
				case 5:
					{
					setState(324);
					usage();
					}
					break;
				case 6:
					{
					setState(325);
					values();
					}
					break;
				case 7:
					{
					setState(326);
					separateSign();
					}
					break;
				}
				}
				setState(331);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(337);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BLANK) {
				{
				setState(332);
				match(BLANK);
				setState(334);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHEN) {
					{
					setState(333);
					match(WHEN);
					}
				}

				setState(336);
				match(ZERO);
				}
			}

			setState(339);
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
		enterRule(_localctx, 74, RULE_level66statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(341);
			match(LEVEL_NUMBER_66);
			setState(342);
			identifier();
			setState(343);
			renames();
			setState(344);
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
		enterRule(_localctx, 76, RULE_level88statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
			match(LEVEL_NUMBER_88);
			setState(347);
			identifier();
			setState(348);
			values();
			setState(349);
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
		enterRule(_localctx, 78, RULE_item);
		try {
			setState(358);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(351);
				match(COMMENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(352);
				group();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(353);
				primitive();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(354);
				level66statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(355);
				level88statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(356);
				skipLiteral();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(357);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0082\u016b\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\3\2\6\2T\n\2\r"+
		"\2\16\2U\3\2\5\2Y\n\2\3\2\3\2\3\3\3\3\3\3\3\3\5\3a\n\3\3\4\5\4d\n\4\3"+
		"\4\3\4\3\4\5\4i\n\4\3\4\5\4l\n\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3"+
		"\t\5\tx\n\t\3\t\3\t\5\t|\n\t\5\t~\n\t\3\t\3\t\5\t\u0082\n\t\3\t\7\t\u0085"+
		"\n\t\f\t\16\t\u0088\13\t\3\n\3\n\5\n\u008c\n\n\3\13\3\13\3\f\3\f\3\f\3"+
		"\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u00a2\n"+
		"\r\3\16\3\16\5\16\u00a6\n\16\3\16\5\16\u00a9\n\16\3\16\3\16\3\17\3\17"+
		"\3\17\3\20\3\20\5\20\u00b2\n\20\3\20\3\20\3\21\3\21\5\21\u00b8\n\21\3"+
		"\21\3\21\3\22\3\22\3\22\5\22\u00bf\n\22\3\22\5\22\u00c2\n\22\3\22\5\22"+
		"\u00c5\n\22\3\22\5\22\u00c8\n\22\3\22\5\22\u00cb\n\22\3\23\3\23\3\23\3"+
		"\24\3\24\3\24\3\24\3\24\5\24\u00d5\n\24\3\25\3\25\3\26\3\26\3\27\3\27"+
		"\5\27\u00dd\n\27\5\27\u00df\n\27\3\27\3\27\3\30\3\30\5\30\u00e5\n\30\5"+
		"\30\u00e7\n\30\3\30\3\30\3\31\3\31\5\31\u00ed\n\31\3\31\3\31\5\31\u00f1"+
		"\n\31\3\31\5\31\u00f4\n\31\3\32\3\32\5\32\u00f8\n\32\3\33\3\33\3\34\3"+
		"\34\5\34\u00fe\n\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\5\35\u010c\n\35\3\36\5\36\u010f\n\36\3\36\3\36\3\36\3\36\5"+
		"\36\u0115\n\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3\"\3\"\3\"\5\"\u0122\n\""+
		"\3\"\5\"\u0125\n\"\3\"\5\"\u0128\n\"\5\"\u012a\n\"\3\"\3\"\5\"\u012e\n"+
		"\"\3#\3#\3$\3$\3%\3%\3%\3%\3%\3%\7%\u013a\n%\f%\16%\u013d\13%\3%\3%\3"+
		"&\3&\3&\3&\3&\3&\3&\3&\3&\7&\u014a\n&\f&\16&\u014d\13&\3&\3&\5&\u0151"+
		"\n&\3&\5&\u0154\n&\3&\3&\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\3)\3"+
		")\3)\3)\3)\5)\u0169\n)\3)\2\2*\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36"+
		" \"$&(*,.\60\62\64\668:<>@BDFHJLNP\2\17\4\2ZZw|\4\2\37\37FF\5\2\3\3[d"+
		"\177\u0080\4\2\6\6\34\34\6\2\7\7\f\31\35\35\62\62\b\2\7\7\f\r\20\24\27"+
		"\31\35\35\62\62\4\2((EE\3\2%&\4\2]]rr\4\2[[nn\3\2\63\64\3\2wx\3\2;=\2"+
		"\u0198\2S\3\2\2\2\4`\3\2\2\2\6k\3\2\2\2\bm\3\2\2\2\no\3\2\2\2\fq\3\2\2"+
		"\2\16s\3\2\2\2\20}\3\2\2\2\22\u0089\3\2\2\2\24\u008d\3\2\2\2\26\u008f"+
		"\3\2\2\2\30\u00a1\3\2\2\2\32\u00a3\3\2\2\2\34\u00ac\3\2\2\2\36\u00af\3"+
		"\2\2\2 \u00b5\3\2\2\2\"\u00bb\3\2\2\2$\u00cc\3\2\2\2&\u00cf\3\2\2\2(\u00d6"+
		"\3\2\2\2*\u00d8\3\2\2\2,\u00de\3\2\2\2.\u00e6\3\2\2\2\60\u00ea\3\2\2\2"+
		"\62\u00f5\3\2\2\2\64\u00f9\3\2\2\2\66\u00fd\3\2\2\28\u010b\3\2\2\2:\u0114"+
		"\3\2\2\2<\u0116\3\2\2\2>\u0118\3\2\2\2@\u011a\3\2\2\2B\u012d\3\2\2\2D"+
		"\u012f\3\2\2\2F\u0131\3\2\2\2H\u0133\3\2\2\2J\u0140\3\2\2\2L\u0157\3\2"+
		"\2\2N\u015c\3\2\2\2P\u0168\3\2\2\2RT\5P)\2SR\3\2\2\2TU\3\2\2\2US\3\2\2"+
		"\2UV\3\2\2\2VX\3\2\2\2WY\7\u0081\2\2XW\3\2\2\2XY\3\2\2\2YZ\3\2\2\2Z[\7"+
		"\2\2\3[\3\3\2\2\2\\a\7v\2\2]a\5\6\4\2^a\5\n\6\2_a\5\30\r\2`\\\3\2\2\2"+
		"`]\3\2\2\2`^\3\2\2\2`_\3\2\2\2a\5\3\2\2\2bd\5\66\34\2cb\3\2\2\2cd\3\2"+
		"\2\2de\3\2\2\2el\7~\2\2fl\7L\2\2gi\5\66\34\2hg\3\2\2\2hi\3\2\2\2ij\3\2"+
		"\2\2jl\5\b\5\2kc\3\2\2\2kf\3\2\2\2kh\3\2\2\2l\7\3\2\2\2mn\t\2\2\2n\t\3"+
		"\2\2\2op\t\3\2\2p\13\3\2\2\2qr\t\4\2\2r\r\3\2\2\2st\7\3\2\2t\17\3\2\2"+
		"\2uw\7I\2\2vx\7$\2\2wv\3\2\2\2wx\3\2\2\2x~\3\2\2\2y{\7J\2\2z|\7\5\2\2"+
		"{z\3\2\2\2{|\3\2\2\2|~\3\2\2\2}u\3\2\2\2}y\3\2\2\2~\177\3\2\2\2\177\u0086"+
		"\5\22\n\2\u0080\u0082\7P\2\2\u0081\u0080\3\2\2\2\u0081\u0082\3\2\2\2\u0082"+
		"\u0083\3\2\2\2\u0083\u0085\5\22\n\2\u0084\u0081\3\2\2\2\u0085\u0088\3"+
		"\2\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\21\3\2\2\2\u0088"+
		"\u0086\3\2\2\2\u0089\u008b\5\24\13\2\u008a\u008c\5\26\f\2\u008b\u008a"+
		"\3\2\2\2\u008b\u008c\3\2\2\2\u008c\23\3\2\2\2\u008d\u008e\5\4\3\2\u008e"+
		"\25\3\2\2\2\u008f\u0090\5\16\b\2\u0090\u0091\5\4\3\2\u0091\27\3\2\2\2"+
		"\u0092\u0093\7\4\2\2\u0093\u00a2\5\4\3\2\u0094\u00a2\7!\2\2\u0095\u00a2"+
		"\7\"\2\2\u0096\u00a2\7*\2\2\u0097\u00a2\7+\2\2\u0098\u00a2\7,\2\2\u0099"+
		"\u00a2\7-\2\2\u009a\u00a2\7\65\2\2\u009b\u00a2\7\66\2\2\u009c\u00a2\7"+
		"?\2\2\u009d\u00a2\7@\2\2\u009e\u00a2\7L\2\2\u009f\u00a2\7M\2\2\u00a0\u00a2"+
		"\7N\2\2\u00a1\u0092\3\2\2\2\u00a1\u0094\3\2\2\2\u00a1\u0095\3\2\2\2\u00a1"+
		"\u0096\3\2\2\2\u00a1\u0097\3\2\2\2\u00a1\u0098\3\2\2\2\u00a1\u0099\3\2"+
		"\2\2\u00a1\u009a\3\2\2\2\u00a1\u009b\3\2\2\2\u00a1\u009c\3\2\2\2\u00a1"+
		"\u009d\3\2\2\2\u00a1\u009e\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a0\3\2"+
		"\2\2\u00a2\31\3\2\2\2\u00a3\u00a5\t\5\2\2\u00a4\u00a6\7\'\2\2\u00a5\u00a4"+
		"\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7\u00a9\7$\2\2\u00a8"+
		"\u00a7\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ab\5\f"+
		"\7\2\u00ab\33\3\2\2\2\u00ac\u00ad\7D\2\2\u00ad\u00ae\5\b\5\2\u00ae\35"+
		"\3\2\2\2\u00af\u00b1\7\33\2\2\u00b0\u00b2\7\61\2\2\u00b1\u00b0\3\2\2\2"+
		"\u00b1\u00b2\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b4\5\f\7\2\u00b4\37"+
		"\3\2\2\2\u00b5\u00b7\7#\2\2\u00b6\u00b8\7\t\2\2\u00b7\u00b6\3\2\2\2\u00b7"+
		"\u00b8\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00ba\5\f\7\2\u00ba!\3\2\2\2"+
		"\u00bb\u00bc\7\60\2\2\u00bc\u00be\5\b\5\2\u00bd\u00bf\5\34\17\2\u00be"+
		"\u00bd\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c1\3\2\2\2\u00c0\u00c2\7C"+
		"\2\2\u00c1\u00c0\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3"+
		"\u00c5\5\36\20\2\u00c4\u00c3\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c7\3"+
		"\2\2\2\u00c6\u00c8\5\32\16\2\u00c7\u00c6\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8"+
		"\u00ca\3\2\2\2\u00c9\u00cb\5 \21\2\u00ca\u00c9\3\2\2\2\u00ca\u00cb\3\2"+
		"\2\2\u00cb#\3\2\2\2\u00cc\u00cd\7\67\2\2\u00cd\u00ce\5\f\7\2\u00ce%\3"+
		"\2\2\2\u00cf\u00d0\78\2\2\u00d0\u00d4\5\f\7\2\u00d1\u00d2\5\16\b\2\u00d2"+
		"\u00d3\5\f\7\2\u00d3\u00d5\3\2\2\2\u00d4\u00d1\3\2\2\2\u00d4\u00d5\3\2"+
		"\2\2\u00d5\'\3\2\2\2\u00d6\u00d7\t\6\2\2\u00d7)\3\2\2\2\u00d8\u00d9\t"+
		"\7\2\2\u00d9+\3\2\2\2\u00da\u00dc\7G\2\2\u00db\u00dd\7$\2\2\u00dc\u00db"+
		"\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00df\3\2\2\2\u00de\u00da\3\2\2\2\u00de"+
		"\u00df\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1\5(\25\2\u00e1-\3\2\2\2"+
		"\u00e2\u00e4\7G\2\2\u00e3\u00e5\7$\2\2\u00e4\u00e3\3\2\2\2\u00e4\u00e5"+
		"\3\2\2\2\u00e5\u00e7\3\2\2\2\u00e6\u00e2\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7"+
		"\u00e8\3\2\2\2\u00e8\u00e9\5*\26\2\u00e9/\3\2\2\2\u00ea\u00ec\7>\2\2\u00eb"+
		"\u00ed\7$\2\2\u00ec\u00eb\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00ee\3\2"+
		"\2\2\u00ee\u00f0\t\b\2\2\u00ef\u00f1\7:\2\2\u00f0\u00ef\3\2\2\2\u00f0"+
		"\u00f1\3\2\2\2\u00f1\u00f3\3\2\2\2\u00f2\u00f4\7\n\2\2\u00f3\u00f2\3\2"+
		"\2\2\u00f3\u00f4\3\2\2\2\u00f4\61\3\2\2\2\u00f5\u00f7\t\t\2\2\u00f6\u00f8"+
		"\79\2\2\u00f7\u00f6\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8\63\3\2\2\2\u00f9"+
		"\u00fa\7X\2\2\u00fa\65\3\2\2\2\u00fb\u00fe\7T\2\2\u00fc\u00fe\7S\2\2\u00fd"+
		"\u00fb\3\2\2\2\u00fd\u00fc\3\2\2\2\u00fe\67\3\2\2\2\u00ff\u010c\7Z\2\2"+
		"\u0100\u010c\7^\2\2\u0101\u010c\7\\\2\2\u0102\u010c\7_\2\2\u0103\u010c"+
		"\7`\2\2\u0104\u010c\7e\2\2\u0105\u010c\7f\2\2\u0106\u010c\7g\2\2\u0107"+
		"\u010c\7h\2\2\u0108\u010c\7i\2\2\u0109\u010c\7j\2\2\u010a\u010c\7k\2\2"+
		"\u010b\u00ff\3\2\2\2\u010b\u0100\3\2\2\2\u010b\u0101\3\2\2\2\u010b\u0102"+
		"\3\2\2\2\u010b\u0103\3\2\2\2\u010b\u0104\3\2\2\2\u010b\u0105\3\2\2\2\u010b"+
		"\u0106\3\2\2\2\u010b\u0107\3\2\2\2\u010b\u0108\3\2\2\2\u010b\u0109\3\2"+
		"\2\2\u010b\u010a\3\2\2\2\u010c9\3\2\2\2\u010d\u010f\5\66\34\2\u010e\u010d"+
		"\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u0115\58\35\2\u0111"+
		"\u0112\58\35\2\u0112\u0113\5\66\34\2\u0113\u0115\3\2\2\2\u0114\u010e\3"+
		"\2\2\2\u0114\u0111\3\2\2\2\u0115;\3\2\2\2\u0116\u0117\t\n\2\2\u0117=\3"+
		"\2\2\2\u0118\u0119\t\13\2\2\u0119?\3\2\2\2\u011a\u011b\t\f\2\2\u011bA"+
		"\3\2\2\2\u011c\u0129\5@!\2\u011d\u012a\5<\37\2\u011e\u012a\5> \2\u011f"+
		"\u0121\5:\36\2\u0120\u0122\5,\27\2\u0121\u0120\3\2\2\2\u0121\u0122\3\2"+
		"\2\2\u0122\u0128\3\2\2\2\u0123\u0125\5,\27\2\u0124\u0123\3\2\2\2\u0124"+
		"\u0125\3\2\2\2\u0125\u0126\3\2\2\2\u0126\u0128\5:\36\2\u0127\u011f\3\2"+
		"\2\2\u0127\u0124\3\2\2\2\u0128\u012a\3\2\2\2\u0129\u011d\3\2\2\2\u0129"+
		"\u011e\3\2\2\2\u0129\u0127\3\2\2\2\u012a\u012e\3\2\2\2\u012b\u012e\7\16"+
		"\2\2\u012c\u012e\7\17\2\2\u012d\u011c\3\2\2\2\u012d\u012b\3\2\2\2\u012d"+
		"\u012c\3\2\2\2\u012eC\3\2\2\2\u012f\u0130\t\r\2\2\u0130E\3\2\2\2\u0131"+
		"\u0132\t\16\2\2\u0132G\3\2\2\2\u0133\u0134\5D#\2\u0134\u013b\5\f\7\2\u0135"+
		"\u013a\5$\23\2\u0136\u013a\5.\30\2\u0137\u013a\5\"\22\2\u0138\u013a\5"+
		"\20\t\2\u0139\u0135\3\2\2\2\u0139\u0136\3\2\2\2\u0139\u0137\3\2\2\2\u0139"+
		"\u0138\3\2\2\2\u013a\u013d\3\2\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2"+
		"\2\2\u013c\u013e\3\2\2\2\u013d\u013b\3\2\2\2\u013e\u013f\5\64\33\2\u013f"+
		"I\3\2\2\2\u0140\u0141\5D#\2\u0141\u014b\5\f\7\2\u0142\u014a\5\62\32\2"+
		"\u0143\u014a\5\"\22\2\u0144\u014a\5B\"\2\u0145\u014a\5$\23\2\u0146\u014a"+
		"\5,\27\2\u0147\u014a\5\20\t\2\u0148\u014a\5\60\31\2\u0149\u0142\3\2\2"+
		"\2\u0149\u0143\3\2\2\2\u0149\u0144\3\2\2\2\u0149\u0145\3\2\2\2\u0149\u0146"+
		"\3\2\2\2\u0149\u0147\3\2\2\2\u0149\u0148\3\2\2\2\u014a\u014d\3\2\2\2\u014b"+
		"\u0149\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u0153\3\2\2\2\u014d\u014b\3\2"+
		"\2\2\u014e\u0150\7\b\2\2\u014f\u0151\7K\2\2\u0150\u014f\3\2\2\2\u0150"+
		"\u0151\3\2\2\2\u0151\u0152\3\2\2\2\u0152\u0154\7L\2\2\u0153\u014e\3\2"+
		"\2\2\u0153\u0154\3\2\2\2\u0154\u0155\3\2\2\2\u0155\u0156\5\64\33\2\u0156"+
		"K\3\2\2\2\u0157\u0158\7y\2\2\u0158\u0159\5\f\7\2\u0159\u015a\5&\24\2\u015a"+
		"\u015b\5\64\33\2\u015bM\3\2\2\2\u015c\u015d\7{\2\2\u015d\u015e\5\f\7\2"+
		"\u015e\u015f\5\20\t\2\u015f\u0160\5\64\33\2\u0160O\3\2\2\2\u0161\u0169"+
		"\7Y\2\2\u0162\u0169\5H%\2\u0163\u0169\5J&\2\u0164\u0169\5L\'\2\u0165\u0169"+
		"\5N(\2\u0166\u0169\5F$\2\u0167\u0169\5\64\33\2\u0168\u0161\3\2\2\2\u0168"+
		"\u0162\3\2\2\2\u0168\u0163\3\2\2\2\u0168\u0164\3\2\2\2\u0168\u0165\3\2"+
		"\2\2\u0168\u0166\3\2\2\2\u0168\u0167\3\2\2\2\u0169Q\3\2\2\2\61UX`chkw"+
		"{}\u0081\u0086\u008b\u00a1\u00a5\u00a8\u00b1\u00b7\u00be\u00c1\u00c4\u00c7"+
		"\u00ca\u00d4\u00dc\u00de\u00e4\u00e6\u00ec\u00f0\u00f3\u00f7\u00fd\u010b"+
		"\u010e\u0114\u0121\u0124\u0127\u0129\u012d\u0139\u013b\u0149\u014b\u0150"+
		"\u0153\u0168";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
