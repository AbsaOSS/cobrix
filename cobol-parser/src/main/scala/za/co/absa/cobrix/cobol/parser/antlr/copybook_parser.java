package za.co.absa.cobrix.cobol.parser.antlr;

// Generated from copybook_parser.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class copybook_parser extends Parser {
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
		S_S=92, Z_S=93, PRECISION_9_EXPLICIT_DOT=94, PRECISION_9_DECIMAL_SCALED=95, 
		PRECISION_9_SCALED=96, PRECISION_9_SCALED_LEAD=97, PRECISION_Z_EXPLICIT_DOT=98, 
		PRECISION_Z_DECIMAL_SCALED=99, PRECISION_Z_SCALED=100, LENGTH_TYPE_9=101, 
		LENGTH_TYPE_A=102, LENGTH_TYPE_P=103, LENGTH_TYPE_X=104, LENGTH_TYPE_Z=105, 
		STRINGLITERAL=106, LEVEL_ROOT=107, LEVEL_REGULAR=108, LEVEL_NUMBER_66=109, 
		LEVEL_NUMBER_77=110, LEVEL_NUMBER_88=111, INTEGERLITERAL=112, NUMERICLITERAL=113, 
		SINGLE_QUOTED_IDENTIFIER=114, IDENTIFIER=115, CONTROL_Z=116, WS=117;
	public static final int
		RULE_main = 0, RULE_literal = 1, RULE_numericLiteral = 2, RULE_integerLiteral = 3, 
		RULE_booleanLiteral = 4, RULE_identifier = 5, RULE_thru = 6, RULE_values = 7, 
		RULE_valuesFromTo = 8, RULE_valuesFrom = 9, RULE_valuesTo = 10, RULE_specialValues = 11, 
		RULE_sorts = 12, RULE_occurs_to = 13, RULE_depending_on = 14, RULE_indexed_by = 15, 
		RULE_occurs = 16, RULE_redefines = 17, RULE_renames = 18, RULE_usageLiteral = 19, 
		RULE_usage = 20, RULE_separate_sign = 21, RULE_justified = 22, RULE_term = 23, 
		RULE_plus_minus = 24, RULE_precision_9 = 25, RULE_sign_precision_9 = 26, 
		RULE_alpha_x = 27, RULE_alpha_a = 28, RULE_pictureLiteral = 29, RULE_pic = 30, 
		RULE_section = 31, RULE_skipLiteral = 32, RULE_group = 33, RULE_primitive = 34, 
		RULE_level66statement = 35, RULE_level88statement = 36, RULE_item = 37;
	private static String[] makeRuleNames() {
		return new String[] {
			"main", "literal", "numericLiteral", "integerLiteral", "booleanLiteral", 
			"identifier", "thru", "values", "valuesFromTo", "valuesFrom", "valuesTo", 
			"specialValues", "sorts", "occurs_to", "depending_on", "indexed_by", 
			"occurs", "redefines", "renames", "usageLiteral", "usage", "separate_sign", 
			"justified", "term", "plus_minus", "precision_9", "sign_precision_9", 
			"alpha_x", "alpha_a", "pictureLiteral", "pic", "section", "skipLiteral", 
			"group", "primitive", "level66statement", "level88statement", "item"
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
			null, "'01'", null, "'66'", "'77'", "'88'", null, null, null, null, "'\u001A'"
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
			"NINES", "A_S", "P_S", "X_S", "S_S", "Z_S", "PRECISION_9_EXPLICIT_DOT", 
			"PRECISION_9_DECIMAL_SCALED", "PRECISION_9_SCALED", "PRECISION_9_SCALED_LEAD", 
			"PRECISION_Z_EXPLICIT_DOT", "PRECISION_Z_DECIMAL_SCALED", "PRECISION_Z_SCALED", 
			"LENGTH_TYPE_9", "LENGTH_TYPE_A", "LENGTH_TYPE_P", "LENGTH_TYPE_X", "LENGTH_TYPE_Z", 
			"STRINGLITERAL", "LEVEL_ROOT", "LEVEL_REGULAR", "LEVEL_NUMBER_66", "LEVEL_NUMBER_77", 
			"LEVEL_NUMBER_88", "INTEGERLITERAL", "NUMERICLITERAL", "SINGLE_QUOTED_IDENTIFIER", 
			"IDENTIFIER", "CONTROL_Z", "WS"
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
	public String getGrammarFileName() { return "copybook_parser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public copybook_parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class MainContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(copybook_parser.EOF, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode CONTROL_Z() { return getToken(copybook_parser.CONTROL_Z, 0); }
		public MainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_main; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitMain(this);
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
			setState(77); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(76);
				item();
				}
				}
				setState(79); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 57)) & ~0x3f) == 0 && ((1L << (_la - 57)) & ((1L << (SKIP1 - 57)) | (1L << (SKIP2 - 57)) | (1L << (SKIP3 - 57)) | (1L << (TERMINAL - 57)) | (1L << (COMMENT - 57)) | (1L << (LEVEL_ROOT - 57)) | (1L << (LEVEL_REGULAR - 57)) | (1L << (LEVEL_NUMBER_66 - 57)) | (1L << (LEVEL_NUMBER_88 - 57)))) != 0) );
			setState(82);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONTROL_Z) {
				{
				setState(81);
				match(CONTROL_Z);
				}
			}

			setState(84);
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
		public TerminalNode STRINGLITERAL() { return getToken(copybook_parser.STRINGLITERAL, 0); }
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_literal);
		try {
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				match(STRINGLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
				numericLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(88);
				booleanLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(89);
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
		public TerminalNode NUMERICLITERAL() { return getToken(copybook_parser.NUMERICLITERAL, 0); }
		public Plus_minusContext plus_minus() {
			return getRuleContext(Plus_minusContext.class,0);
		}
		public TerminalNode ZERO() { return getToken(copybook_parser.ZERO, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_numericLiteral);
		int _la;
		try {
			setState(101);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(92);
					plus_minus();
					}
				}

				setState(95);
				match(NUMERICLITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(96);
				match(ZERO);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(98);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(97);
					plus_minus();
					}
				}

				setState(100);
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
		public TerminalNode INTEGERLITERAL() { return getToken(copybook_parser.INTEGERLITERAL, 0); }
		public TerminalNode NINES() { return getToken(copybook_parser.NINES, 0); }
		public TerminalNode LEVEL_ROOT() { return getToken(copybook_parser.LEVEL_ROOT, 0); }
		public TerminalNode LEVEL_REGULAR() { return getToken(copybook_parser.LEVEL_REGULAR, 0); }
		public TerminalNode LEVEL_NUMBER_66() { return getToken(copybook_parser.LEVEL_NUMBER_66, 0); }
		public TerminalNode LEVEL_NUMBER_77() { return getToken(copybook_parser.LEVEL_NUMBER_77, 0); }
		public TerminalNode LEVEL_NUMBER_88() { return getToken(copybook_parser.LEVEL_NUMBER_88, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
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
			setState(103);
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
		public TerminalNode TRUE() { return getToken(copybook_parser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(copybook_parser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitBooleanLiteral(this);
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
			setState(105);
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
		public TerminalNode IDENTIFIER() { return getToken(copybook_parser.IDENTIFIER, 0); }
		public TerminalNode THRU_OR_THROUGH() { return getToken(copybook_parser.THRU_OR_THROUGH, 0); }
		public TerminalNode A_S() { return getToken(copybook_parser.A_S, 0); }
		public TerminalNode P_S() { return getToken(copybook_parser.P_S, 0); }
		public TerminalNode X_S() { return getToken(copybook_parser.X_S, 0); }
		public TerminalNode S_S() { return getToken(copybook_parser.S_S, 0); }
		public TerminalNode Z_S() { return getToken(copybook_parser.Z_S, 0); }
		public TerminalNode SINGLE_QUOTED_IDENTIFIER() { return getToken(copybook_parser.SINGLE_QUOTED_IDENTIFIER, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitIdentifier(this);
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
			setState(107);
			_la = _input.LA(1);
			if ( !(_la==THRU_OR_THROUGH || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & ((1L << (A_S - 89)) | (1L << (P_S - 89)) | (1L << (X_S - 89)) | (1L << (S_S - 89)) | (1L << (Z_S - 89)) | (1L << (SINGLE_QUOTED_IDENTIFIER - 89)) | (1L << (IDENTIFIER - 89)))) != 0)) ) {
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
		public TerminalNode THRU_OR_THROUGH() { return getToken(copybook_parser.THRU_OR_THROUGH, 0); }
		public ThruContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_thru; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitThru(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ThruContext thru() throws RecognitionException {
		ThruContext _localctx = new ThruContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_thru);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
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
		public TerminalNode VALUE() { return getToken(copybook_parser.VALUE, 0); }
		public TerminalNode VALUES() { return getToken(copybook_parser.VALUES, 0); }
		public TerminalNode IS() { return getToken(copybook_parser.IS, 0); }
		public TerminalNode ARE() { return getToken(copybook_parser.ARE, 0); }
		public List<TerminalNode> COMMACHAR() { return getTokens(copybook_parser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(copybook_parser.COMMACHAR, i);
		}
		public ValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesContext values() throws RecognitionException {
		ValuesContext _localctx = new ValuesContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_values);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
				{
				setState(111);
				match(VALUE);
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(112);
					match(IS);
					}
				}

				}
				break;
			case VALUES:
				{
				setState(115);
				match(VALUES);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ARE) {
					{
					setState(116);
					match(ARE);
					}
				}

				}
				break;
			case ALL:
			case FALSE:
			case HIGH_VALUE:
			case HIGH_VALUES:
			case LOW_VALUE:
			case LOW_VALUES:
			case NULL:
			case NULLS:
			case QUOTE:
			case QUOTES:
			case SPACE:
			case SPACES:
			case TRUE:
			case ZERO:
			case ZEROS:
			case ZEROES:
			case MINUSCHAR:
			case PLUSCHAR:
			case NINES:
			case STRINGLITERAL:
			case LEVEL_ROOT:
			case LEVEL_REGULAR:
			case LEVEL_NUMBER_66:
			case LEVEL_NUMBER_77:
			case LEVEL_NUMBER_88:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				break;
			default:
				break;
			}
			setState(121);
			valuesFromTo();
			setState(128);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(123);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMACHAR) {
						{
						setState(122);
						match(COMMACHAR);
						}
					}

					setState(125);
					valuesFromTo();
					}
					} 
				}
				setState(130);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitValuesFromTo(this);
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
			setState(131);
			valuesFrom();
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(132);
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitValuesFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesFromContext valuesFrom() throws RecognitionException {
		ValuesFromContext _localctx = new ValuesFromContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_valuesFrom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitValuesTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesToContext valuesTo() throws RecognitionException {
		ValuesToContext _localctx = new ValuesToContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_valuesTo);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			thru();
			setState(138);
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
		public TerminalNode ALL() { return getToken(copybook_parser.ALL, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode HIGH_VALUE() { return getToken(copybook_parser.HIGH_VALUE, 0); }
		public TerminalNode HIGH_VALUES() { return getToken(copybook_parser.HIGH_VALUES, 0); }
		public TerminalNode LOW_VALUE() { return getToken(copybook_parser.LOW_VALUE, 0); }
		public TerminalNode LOW_VALUES() { return getToken(copybook_parser.LOW_VALUES, 0); }
		public TerminalNode NULL() { return getToken(copybook_parser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(copybook_parser.NULLS, 0); }
		public TerminalNode QUOTE() { return getToken(copybook_parser.QUOTE, 0); }
		public TerminalNode QUOTES() { return getToken(copybook_parser.QUOTES, 0); }
		public TerminalNode SPACE() { return getToken(copybook_parser.SPACE, 0); }
		public TerminalNode SPACES() { return getToken(copybook_parser.SPACES, 0); }
		public TerminalNode ZERO() { return getToken(copybook_parser.ZERO, 0); }
		public TerminalNode ZEROS() { return getToken(copybook_parser.ZEROS, 0); }
		public TerminalNode ZEROES() { return getToken(copybook_parser.ZEROES, 0); }
		public SpecialValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialValues; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitSpecialValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialValuesContext specialValues() throws RecognitionException {
		SpecialValuesContext _localctx = new SpecialValuesContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_specialValues);
		try {
			setState(155);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(140);
				match(ALL);
				setState(141);
				literal();
				}
				break;
			case HIGH_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				match(HIGH_VALUE);
				}
				break;
			case HIGH_VALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(143);
				match(HIGH_VALUES);
				}
				break;
			case LOW_VALUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(144);
				match(LOW_VALUE);
				}
				break;
			case LOW_VALUES:
				enterOuterAlt(_localctx, 5);
				{
				setState(145);
				match(LOW_VALUES);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(146);
				match(NULL);
				}
				break;
			case NULLS:
				enterOuterAlt(_localctx, 7);
				{
				setState(147);
				match(NULLS);
				}
				break;
			case QUOTE:
				enterOuterAlt(_localctx, 8);
				{
				setState(148);
				match(QUOTE);
				}
				break;
			case QUOTES:
				enterOuterAlt(_localctx, 9);
				{
				setState(149);
				match(QUOTES);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 10);
				{
				setState(150);
				match(SPACE);
				}
				break;
			case SPACES:
				enterOuterAlt(_localctx, 11);
				{
				setState(151);
				match(SPACES);
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 12);
				{
				setState(152);
				match(ZERO);
				}
				break;
			case ZEROS:
				enterOuterAlt(_localctx, 13);
				{
				setState(153);
				match(ZEROS);
				}
				break;
			case ZEROES:
				enterOuterAlt(_localctx, 14);
				{
				setState(154);
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
		public TerminalNode ASCENDING() { return getToken(copybook_parser.ASCENDING, 0); }
		public TerminalNode DESCENDING() { return getToken(copybook_parser.DESCENDING, 0); }
		public TerminalNode KEY() { return getToken(copybook_parser.KEY, 0); }
		public TerminalNode IS() { return getToken(copybook_parser.IS, 0); }
		public SortsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sorts; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitSorts(this);
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
			setState(157);
			_la = _input.LA(1);
			if ( !(_la==ASCENDING || _la==DESCENDING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY) {
				{
				setState(158);
				match(KEY);
				}
			}

			setState(162);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(161);
				match(IS);
				}
			}

			setState(164);
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

	public static class Occurs_toContext extends ParserRuleContext {
		public TerminalNode TO() { return getToken(copybook_parser.TO, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public Occurs_toContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_occurs_to; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitOccurs_to(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Occurs_toContext occurs_to() throws RecognitionException {
		Occurs_toContext _localctx = new Occurs_toContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_occurs_to);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			match(TO);
			setState(167);
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

	public static class Depending_onContext extends ParserRuleContext {
		public TerminalNode DEPENDING() { return getToken(copybook_parser.DEPENDING, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ON() { return getToken(copybook_parser.ON, 0); }
		public Depending_onContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_depending_on; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitDepending_on(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Depending_onContext depending_on() throws RecognitionException {
		Depending_onContext _localctx = new Depending_onContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_depending_on);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(DEPENDING);
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(170);
				match(ON);
				}
			}

			setState(173);
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

	public static class Indexed_byContext extends ParserRuleContext {
		public TerminalNode INDEXED() { return getToken(copybook_parser.INDEXED, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode BY() { return getToken(copybook_parser.BY, 0); }
		public Indexed_byContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexed_by; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitIndexed_by(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Indexed_byContext indexed_by() throws RecognitionException {
		Indexed_byContext _localctx = new Indexed_byContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_indexed_by);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(INDEXED);
			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BY) {
				{
				setState(176);
				match(BY);
				}
			}

			setState(179);
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
		public TerminalNode OCCURS() { return getToken(copybook_parser.OCCURS, 0); }
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public Occurs_toContext occurs_to() {
			return getRuleContext(Occurs_toContext.class,0);
		}
		public TerminalNode TIMES() { return getToken(copybook_parser.TIMES, 0); }
		public Depending_onContext depending_on() {
			return getRuleContext(Depending_onContext.class,0);
		}
		public SortsContext sorts() {
			return getRuleContext(SortsContext.class,0);
		}
		public Indexed_byContext indexed_by() {
			return getRuleContext(Indexed_byContext.class,0);
		}
		public OccursContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_occurs; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitOccurs(this);
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
			setState(181);
			match(OCCURS);
			setState(182);
			integerLiteral();
			setState(184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(183);
				occurs_to();
				}
			}

			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TIMES) {
				{
				setState(186);
				match(TIMES);
				}
			}

			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEPENDING) {
				{
				setState(189);
				depending_on();
				}
			}

			setState(193);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASCENDING || _la==DESCENDING) {
				{
				setState(192);
				sorts();
				}
			}

			setState(196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEXED) {
				{
				setState(195);
				indexed_by();
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
		public TerminalNode REDEFINES() { return getToken(copybook_parser.REDEFINES, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RedefinesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_redefines; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitRedefines(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RedefinesContext redefines() throws RecognitionException {
		RedefinesContext _localctx = new RedefinesContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_redefines);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(REDEFINES);
			setState(199);
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
		public TerminalNode RENAMES() { return getToken(copybook_parser.RENAMES, 0); }
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitRenames(this);
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
			setState(201);
			match(RENAMES);
			setState(202);
			identifier();
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THRU_OR_THROUGH) {
				{
				setState(203);
				thru();
				setState(204);
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
		public TerminalNode COMPUTATIONAL_0() { return getToken(copybook_parser.COMPUTATIONAL_0, 0); }
		public TerminalNode COMPUTATIONAL_1() { return getToken(copybook_parser.COMPUTATIONAL_1, 0); }
		public TerminalNode COMPUTATIONAL_2() { return getToken(copybook_parser.COMPUTATIONAL_2, 0); }
		public TerminalNode COMPUTATIONAL_3() { return getToken(copybook_parser.COMPUTATIONAL_3, 0); }
		public TerminalNode COMPUTATIONAL_4() { return getToken(copybook_parser.COMPUTATIONAL_4, 0); }
		public TerminalNode COMPUTATIONAL_5() { return getToken(copybook_parser.COMPUTATIONAL_5, 0); }
		public TerminalNode COMPUTATIONAL() { return getToken(copybook_parser.COMPUTATIONAL, 0); }
		public TerminalNode COMP_0() { return getToken(copybook_parser.COMP_0, 0); }
		public TerminalNode COMP_1() { return getToken(copybook_parser.COMP_1, 0); }
		public TerminalNode COMP_2() { return getToken(copybook_parser.COMP_2, 0); }
		public TerminalNode COMP_3() { return getToken(copybook_parser.COMP_3, 0); }
		public TerminalNode COMP_4() { return getToken(copybook_parser.COMP_4, 0); }
		public TerminalNode COMP_5() { return getToken(copybook_parser.COMP_5, 0); }
		public TerminalNode COMP() { return getToken(copybook_parser.COMP, 0); }
		public TerminalNode DISPLAY() { return getToken(copybook_parser.DISPLAY, 0); }
		public TerminalNode BINARY() { return getToken(copybook_parser.BINARY, 0); }
		public TerminalNode PACKED_DECIMAL() { return getToken(copybook_parser.PACKED_DECIMAL, 0); }
		public UsageLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usageLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitUsageLiteral(this);
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
			setState(208);
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

	public static class UsageContext extends ParserRuleContext {
		public UsageLiteralContext usageLiteral() {
			return getRuleContext(UsageLiteralContext.class,0);
		}
		public TerminalNode USAGE() { return getToken(copybook_parser.USAGE, 0); }
		public TerminalNode IS() { return getToken(copybook_parser.IS, 0); }
		public UsageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usage; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitUsage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UsageContext usage() throws RecognitionException {
		UsageContext _localctx = new UsageContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_usage);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USAGE) {
				{
				setState(210);
				match(USAGE);
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IS) {
					{
					setState(211);
					match(IS);
					}
				}

				}
			}

			setState(216);
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

	public static class Separate_signContext extends ParserRuleContext {
		public TerminalNode SIGN() { return getToken(copybook_parser.SIGN, 0); }
		public TerminalNode LEADING() { return getToken(copybook_parser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(copybook_parser.TRAILING, 0); }
		public TerminalNode IS() { return getToken(copybook_parser.IS, 0); }
		public TerminalNode SEPARATE() { return getToken(copybook_parser.SEPARATE, 0); }
		public TerminalNode CHARACTER() { return getToken(copybook_parser.CHARACTER, 0); }
		public Separate_signContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_separate_sign; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitSeparate_sign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Separate_signContext separate_sign() throws RecognitionException {
		Separate_signContext _localctx = new Separate_signContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_separate_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(SIGN);
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IS) {
				{
				setState(219);
				match(IS);
				}
			}

			setState(222);
			_la = _input.LA(1);
			if ( !(_la==LEADING || _la==TRAILING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(224);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEPARATE) {
				{
				setState(223);
				match(SEPARATE);
				}
			}

			setState(227);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CHARACTER) {
				{
				setState(226);
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
		public TerminalNode JUSTIFIED() { return getToken(copybook_parser.JUSTIFIED, 0); }
		public TerminalNode JUST() { return getToken(copybook_parser.JUST, 0); }
		public TerminalNode RIGHT() { return getToken(copybook_parser.RIGHT, 0); }
		public JustifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_justified; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitJustified(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JustifiedContext justified() throws RecognitionException {
		JustifiedContext _localctx = new JustifiedContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_justified);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			_la = _input.LA(1);
			if ( !(_la==JUST || _la==JUSTIFIED) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RIGHT) {
				{
				setState(230);
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
		public TerminalNode TERMINAL() { return getToken(copybook_parser.TERMINAL, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_term);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
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

	public static class Plus_minusContext extends ParserRuleContext {
		public Plus_minusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_plus_minus; }
	 
		public Plus_minusContext() { }
		public void copyFrom(Plus_minusContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MinusContext extends Plus_minusContext {
		public TerminalNode MINUSCHAR() { return getToken(copybook_parser.MINUSCHAR, 0); }
		public MinusContext(Plus_minusContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitMinus(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PlusContext extends Plus_minusContext {
		public TerminalNode PLUSCHAR() { return getToken(copybook_parser.PLUSCHAR, 0); }
		public PlusContext(Plus_minusContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPlus(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Plus_minusContext plus_minus() throws RecognitionException {
		Plus_minusContext _localctx = new Plus_minusContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_plus_minus);
		try {
			setState(237);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUSCHAR:
				_localctx = new PlusContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(235);
				match(PLUSCHAR);
				}
				break;
			case MINUSCHAR:
				_localctx = new MinusContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(236);
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

	public static class Precision_9Context extends ParserRuleContext {
		public Precision_9Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_precision_9; }
	 
		public Precision_9Context() { }
		public void copyFrom(Precision_9Context ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Precision_9_zsContext extends Precision_9Context {
		public TerminalNode Z_S() { return getToken(copybook_parser.Z_S, 0); }
		public Precision_9_zsContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_zs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_scaled_leadContext extends Precision_9Context {
		public TerminalNode PRECISION_9_SCALED_LEAD() { return getToken(copybook_parser.PRECISION_9_SCALED_LEAD, 0); }
		public Precision_9_scaled_leadContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_scaled_lead(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_ninesContext extends Precision_9Context {
		public TerminalNode NINES() { return getToken(copybook_parser.NINES, 0); }
		public Precision_9_ninesContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_nines(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_ssContext extends Precision_9Context {
		public TerminalNode S_S() { return getToken(copybook_parser.S_S, 0); }
		public Precision_9_ssContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_ss(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_decimal_scaledContext extends Precision_9Context {
		public TerminalNode PRECISION_9_DECIMAL_SCALED() { return getToken(copybook_parser.PRECISION_9_DECIMAL_SCALED, 0); }
		public Precision_9_decimal_scaledContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_decimal_scaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_z_explicit_dotContext extends Precision_9Context {
		public TerminalNode PRECISION_Z_EXPLICIT_DOT() { return getToken(copybook_parser.PRECISION_Z_EXPLICIT_DOT, 0); }
		public Precision_z_explicit_dotContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_z_explicit_dot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_explicit_dotContext extends Precision_9Context {
		public TerminalNode PRECISION_9_EXPLICIT_DOT() { return getToken(copybook_parser.PRECISION_9_EXPLICIT_DOT, 0); }
		public Precision_9_explicit_dotContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_explicit_dot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_z_scaledContext extends Precision_9Context {
		public TerminalNode PRECISION_Z_SCALED() { return getToken(copybook_parser.PRECISION_Z_SCALED, 0); }
		public Precision_z_scaledContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_z_scaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_9_scaledContext extends Precision_9Context {
		public TerminalNode PRECISION_9_SCALED() { return getToken(copybook_parser.PRECISION_9_SCALED, 0); }
		public Precision_9_scaledContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_9_scaled(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precision_z_decimal_scaledContext extends Precision_9Context {
		public TerminalNode PRECISION_Z_DECIMAL_SCALED() { return getToken(copybook_parser.PRECISION_Z_DECIMAL_SCALED, 0); }
		public Precision_z_decimal_scaledContext(Precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrecision_z_decimal_scaled(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Precision_9Context precision_9() throws RecognitionException {
		Precision_9Context _localctx = new Precision_9Context(_ctx, getState());
		enterRule(_localctx, 50, RULE_precision_9);
		try {
			setState(249);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NINES:
				_localctx = new Precision_9_ninesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(239);
				match(NINES);
				}
				break;
			case S_S:
				_localctx = new Precision_9_ssContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(240);
				match(S_S);
				}
				break;
			case Z_S:
				_localctx = new Precision_9_zsContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(241);
				match(Z_S);
				}
				break;
			case PRECISION_9_EXPLICIT_DOT:
				_localctx = new Precision_9_explicit_dotContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(242);
				match(PRECISION_9_EXPLICIT_DOT);
				}
				break;
			case PRECISION_9_DECIMAL_SCALED:
				_localctx = new Precision_9_decimal_scaledContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(243);
				match(PRECISION_9_DECIMAL_SCALED);
				}
				break;
			case PRECISION_9_SCALED:
				_localctx = new Precision_9_scaledContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(244);
				match(PRECISION_9_SCALED);
				}
				break;
			case PRECISION_9_SCALED_LEAD:
				_localctx = new Precision_9_scaled_leadContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(245);
				match(PRECISION_9_SCALED_LEAD);
				}
				break;
			case PRECISION_Z_EXPLICIT_DOT:
				_localctx = new Precision_z_explicit_dotContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(246);
				match(PRECISION_Z_EXPLICIT_DOT);
				}
				break;
			case PRECISION_Z_DECIMAL_SCALED:
				_localctx = new Precision_z_decimal_scaledContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(247);
				match(PRECISION_Z_DECIMAL_SCALED);
				}
				break;
			case PRECISION_Z_SCALED:
				_localctx = new Precision_z_scaledContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(248);
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

	public static class Sign_precision_9Context extends ParserRuleContext {
		public Sign_precision_9Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign_precision_9; }
	 
		public Sign_precision_9Context() { }
		public void copyFrom(Sign_precision_9Context ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Trailing_signContext extends Sign_precision_9Context {
		public Precision_9Context precision_9() {
			return getRuleContext(Precision_9Context.class,0);
		}
		public Plus_minusContext plus_minus() {
			return getRuleContext(Plus_minusContext.class,0);
		}
		public Trailing_signContext(Sign_precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitTrailing_sign(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Leading_signContext extends Sign_precision_9Context {
		public Precision_9Context precision_9() {
			return getRuleContext(Precision_9Context.class,0);
		}
		public Plus_minusContext plus_minus() {
			return getRuleContext(Plus_minusContext.class,0);
		}
		public Leading_signContext(Sign_precision_9Context ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitLeading_sign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sign_precision_9Context sign_precision_9() throws RecognitionException {
		Sign_precision_9Context _localctx = new Sign_precision_9Context(_ctx, getState());
		enterRule(_localctx, 52, RULE_sign_precision_9);
		int _la;
		try {
			setState(259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				_localctx = new Leading_signContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUSCHAR || _la==PLUSCHAR) {
					{
					setState(251);
					plus_minus();
					}
				}

				setState(254);
				precision_9();
				}
				}
				break;
			case 2:
				_localctx = new Trailing_signContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(255);
				precision_9();
				setState(257);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(256);
					plus_minus();
					}
					break;
				}
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

	public static class Alpha_xContext extends ParserRuleContext {
		public TerminalNode X_S() { return getToken(copybook_parser.X_S, 0); }
		public TerminalNode LENGTH_TYPE_X() { return getToken(copybook_parser.LENGTH_TYPE_X, 0); }
		public Alpha_xContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alpha_x; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitAlpha_x(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Alpha_xContext alpha_x() throws RecognitionException {
		Alpha_xContext _localctx = new Alpha_xContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_alpha_x);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
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

	public static class Alpha_aContext extends ParserRuleContext {
		public TerminalNode X_S() { return getToken(copybook_parser.X_S, 0); }
		public TerminalNode LENGTH_TYPE_X() { return getToken(copybook_parser.LENGTH_TYPE_X, 0); }
		public Alpha_aContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alpha_a; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitAlpha_a(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Alpha_aContext alpha_a() throws RecognitionException {
		Alpha_aContext _localctx = new Alpha_aContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_alpha_a);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
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

	public static class PictureLiteralContext extends ParserRuleContext {
		public TerminalNode PICTURE() { return getToken(copybook_parser.PICTURE, 0); }
		public TerminalNode PIC() { return getToken(copybook_parser.PIC, 0); }
		public PictureLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pictureLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPictureLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PictureLiteralContext pictureLiteral() throws RecognitionException {
		PictureLiteralContext _localctx = new PictureLiteralContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_pictureLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
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
		public Alpha_xContext alpha_x() {
			return getRuleContext(Alpha_xContext.class,0);
		}
		public Alpha_aContext alpha_a() {
			return getRuleContext(Alpha_aContext.class,0);
		}
		public Sign_precision_9Context sign_precision_9() {
			return getRuleContext(Sign_precision_9Context.class,0);
		}
		public UsageContext usage() {
			return getRuleContext(UsageContext.class,0);
		}
		public PicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pic; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PicContext pic() throws RecognitionException {
		PicContext _localctx = new PicContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_pic);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			pictureLiteral();
			setState(280);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				{
				setState(268);
				alpha_x();
				}
				break;
			case 2:
				{
				setState(269);
				alpha_a();
				}
				break;
			case 3:
				{
				setState(278);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
				case 1:
					{
					setState(270);
					sign_precision_9();
					setState(272);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
					case 1:
						{
						setState(271);
						usage();
						}
						break;
					}
					}
					break;
				case 2:
					{
					setState(275);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << PACKED_DECIMAL))) != 0) || _la==USAGE) {
						{
						setState(274);
						usage();
						}
					}

					setState(277);
					sign_precision_9();
					}
					break;
				}
				}
				break;
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

	public static class SectionContext extends ParserRuleContext {
		public TerminalNode LEVEL_ROOT() { return getToken(copybook_parser.LEVEL_ROOT, 0); }
		public TerminalNode LEVEL_REGULAR() { return getToken(copybook_parser.LEVEL_REGULAR, 0); }
		public SectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitSection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SectionContext section() throws RecognitionException {
		SectionContext _localctx = new SectionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(282);
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
		public TerminalNode SKIP1() { return getToken(copybook_parser.SKIP1, 0); }
		public TerminalNode SKIP2() { return getToken(copybook_parser.SKIP2, 0); }
		public TerminalNode SKIP3() { return getToken(copybook_parser.SKIP3, 0); }
		public SkipLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skipLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitSkipLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SkipLiteralContext skipLiteral() throws RecognitionException {
		SkipLiteralContext _localctx = new SkipLiteralContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_skipLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
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
		public List<UsageContext> usage() {
			return getRuleContexts(UsageContext.class);
		}
		public UsageContext usage(int i) {
			return getRuleContext(UsageContext.class,i);
		}
		public List<OccursContext> occurs() {
			return getRuleContexts(OccursContext.class);
		}
		public OccursContext occurs(int i) {
			return getRuleContext(OccursContext.class,i);
		}
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			section();
			setState(287);
			identifier();
			setState(293);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << REDEFINES))) != 0) || _la==USAGE) {
				{
				setState(291);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case REDEFINES:
					{
					setState(288);
					redefines();
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
					{
					setState(289);
					usage();
					}
					break;
				case OCCURS:
					{
					setState(290);
					occurs();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(295);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(296);
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
		public List<Separate_signContext> separate_sign() {
			return getRuleContexts(Separate_signContext.class);
		}
		public Separate_signContext separate_sign(int i) {
			return getRuleContext(Separate_signContext.class,i);
		}
		public TerminalNode BLANK() { return getToken(copybook_parser.BLANK, 0); }
		public TerminalNode ZERO() { return getToken(copybook_parser.ZERO, 0); }
		public TerminalNode WHEN() { return getToken(copybook_parser.WHEN, 0); }
		public PrimitiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitPrimitive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveContext primitive() throws RecognitionException {
		PrimitiveContext _localctx = new PrimitiveContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_primitive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			section();
			setState(299);
			identifier();
			setState(309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALL) | (1L << BINARY) | (1L << COMP) | (1L << COMP_0) | (1L << COMP_1) | (1L << COMP_2) | (1L << COMP_3) | (1L << COMP_4) | (1L << COMP_5) | (1L << COMPUTATIONAL) | (1L << COMPUTATIONAL_0) | (1L << COMPUTATIONAL_1) | (1L << COMPUTATIONAL_2) | (1L << COMPUTATIONAL_3) | (1L << COMPUTATIONAL_4) | (1L << COMPUTATIONAL_5) | (1L << DISPLAY) | (1L << FALSE) | (1L << HIGH_VALUE) | (1L << HIGH_VALUES) | (1L << JUST) | (1L << JUSTIFIED) | (1L << LOW_VALUE) | (1L << LOW_VALUES) | (1L << NULL) | (1L << NULLS) | (1L << OCCURS) | (1L << PACKED_DECIMAL) | (1L << PIC) | (1L << PICTURE) | (1L << QUOTE) | (1L << QUOTES) | (1L << REDEFINES) | (1L << SIGN) | (1L << SPACE) | (1L << SPACES))) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (TRUE - 68)) | (1L << (USAGE - 68)) | (1L << (VALUE - 68)) | (1L << (VALUES - 68)) | (1L << (ZERO - 68)) | (1L << (ZEROS - 68)) | (1L << (ZEROES - 68)) | (1L << (MINUSCHAR - 68)) | (1L << (PLUSCHAR - 68)) | (1L << (NINES - 68)) | (1L << (STRINGLITERAL - 68)) | (1L << (LEVEL_ROOT - 68)) | (1L << (LEVEL_REGULAR - 68)) | (1L << (LEVEL_NUMBER_66 - 68)) | (1L << (LEVEL_NUMBER_77 - 68)) | (1L << (LEVEL_NUMBER_88 - 68)) | (1L << (INTEGERLITERAL - 68)) | (1L << (NUMERICLITERAL - 68)))) != 0)) {
				{
				setState(307);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case JUST:
				case JUSTIFIED:
					{
					setState(300);
					justified();
					}
					break;
				case OCCURS:
					{
					setState(301);
					occurs();
					}
					break;
				case PIC:
				case PICTURE:
					{
					setState(302);
					pic();
					}
					break;
				case REDEFINES:
					{
					setState(303);
					redefines();
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
					{
					setState(304);
					usage();
					}
					break;
				case ALL:
				case FALSE:
				case HIGH_VALUE:
				case HIGH_VALUES:
				case LOW_VALUE:
				case LOW_VALUES:
				case NULL:
				case NULLS:
				case QUOTE:
				case QUOTES:
				case SPACE:
				case SPACES:
				case TRUE:
				case VALUE:
				case VALUES:
				case ZERO:
				case ZEROS:
				case ZEROES:
				case MINUSCHAR:
				case PLUSCHAR:
				case NINES:
				case STRINGLITERAL:
				case LEVEL_ROOT:
				case LEVEL_REGULAR:
				case LEVEL_NUMBER_66:
				case LEVEL_NUMBER_77:
				case LEVEL_NUMBER_88:
				case INTEGERLITERAL:
				case NUMERICLITERAL:
					{
					setState(305);
					values();
					}
					break;
				case SIGN:
					{
					setState(306);
					separate_sign();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(311);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(317);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BLANK) {
				{
				setState(312);
				match(BLANK);
				setState(314);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHEN) {
					{
					setState(313);
					match(WHEN);
					}
				}

				setState(316);
				match(ZERO);
				}
			}

			setState(319);
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
		public TerminalNode LEVEL_NUMBER_66() { return getToken(copybook_parser.LEVEL_NUMBER_66, 0); }
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitLevel66statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Level66statementContext level66statement() throws RecognitionException {
		Level66statementContext _localctx = new Level66statementContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_level66statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(321);
			match(LEVEL_NUMBER_66);
			setState(322);
			identifier();
			setState(323);
			renames();
			setState(324);
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
		public TerminalNode LEVEL_NUMBER_88() { return getToken(copybook_parser.LEVEL_NUMBER_88, 0); }
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitLevel88statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Level88statementContext level88statement() throws RecognitionException {
		Level88statementContext _localctx = new Level88statementContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_level88statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			match(LEVEL_NUMBER_88);
			setState(327);
			identifier();
			setState(328);
			values();
			setState(329);
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
		public TerminalNode COMMENT() { return getToken(copybook_parser.COMMENT, 0); }
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
			if ( visitor instanceof copybook_parserVisitor ) return ((copybook_parserVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_item);
		try {
			setState(338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(331);
				match(COMMENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(332);
				group();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(333);
				primitive();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(334);
				level66statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(335);
				level88statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(336);
				skipLiteral();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(337);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3w\u0157\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3\2\6\2P\n\2\r\2\16\2Q\3\2"+
		"\5\2U\n\2\3\2\3\2\3\3\3\3\3\3\3\3\5\3]\n\3\3\4\5\4`\n\4\3\4\3\4\3\4\5"+
		"\4e\n\4\3\4\5\4h\n\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\5\tt\n\t"+
		"\3\t\3\t\5\tx\n\t\5\tz\n\t\3\t\3\t\5\t~\n\t\3\t\7\t\u0081\n\t\f\t\16\t"+
		"\u0084\13\t\3\n\3\n\5\n\u0088\n\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u009e\n\r\3\16\3\16"+
		"\5\16\u00a2\n\16\3\16\5\16\u00a5\n\16\3\16\3\16\3\17\3\17\3\17\3\20\3"+
		"\20\5\20\u00ae\n\20\3\20\3\20\3\21\3\21\5\21\u00b4\n\21\3\21\3\21\3\22"+
		"\3\22\3\22\5\22\u00bb\n\22\3\22\5\22\u00be\n\22\3\22\5\22\u00c1\n\22\3"+
		"\22\5\22\u00c4\n\22\3\22\5\22\u00c7\n\22\3\23\3\23\3\23\3\24\3\24\3\24"+
		"\3\24\3\24\5\24\u00d1\n\24\3\25\3\25\3\26\3\26\5\26\u00d7\n\26\5\26\u00d9"+
		"\n\26\3\26\3\26\3\27\3\27\5\27\u00df\n\27\3\27\3\27\5\27\u00e3\n\27\3"+
		"\27\5\27\u00e6\n\27\3\30\3\30\5\30\u00ea\n\30\3\31\3\31\3\32\3\32\5\32"+
		"\u00f0\n\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u00fc"+
		"\n\33\3\34\5\34\u00ff\n\34\3\34\3\34\3\34\5\34\u0104\n\34\5\34\u0106\n"+
		"\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3 \3 \3 \5 \u0113\n \3 \5 \u0116"+
		"\n \3 \5 \u0119\n \5 \u011b\n \3!\3!\3\"\3\"\3#\3#\3#\3#\3#\7#\u0126\n"+
		"#\f#\16#\u0129\13#\3#\3#\3$\3$\3$\3$\3$\3$\3$\3$\3$\7$\u0136\n$\f$\16"+
		"$\u0139\13$\3$\3$\5$\u013d\n$\3$\5$\u0140\n$\3$\3$\3%\3%\3%\3%\3%\3&\3"+
		"&\3&\3&\3&\3\'\3\'\3\'\3\'\3\'\3\'\3\'\5\'\u0155\n\'\3\'\2\2(\2\4\6\b"+
		"\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJL\2\r\4"+
		"\2ZZmr\4\2\37\37FF\5\2\3\3[_tu\4\2\6\6\34\34\6\2\7\7\f\31\35\35\62\62"+
		"\4\2((EE\3\2%&\4\2]]jj\3\2\63\64\3\2mn\3\2;=\2\u0181\2O\3\2\2\2\4\\\3"+
		"\2\2\2\6g\3\2\2\2\bi\3\2\2\2\nk\3\2\2\2\fm\3\2\2\2\16o\3\2\2\2\20y\3\2"+
		"\2\2\22\u0085\3\2\2\2\24\u0089\3\2\2\2\26\u008b\3\2\2\2\30\u009d\3\2\2"+
		"\2\32\u009f\3\2\2\2\34\u00a8\3\2\2\2\36\u00ab\3\2\2\2 \u00b1\3\2\2\2\""+
		"\u00b7\3\2\2\2$\u00c8\3\2\2\2&\u00cb\3\2\2\2(\u00d2\3\2\2\2*\u00d8\3\2"+
		"\2\2,\u00dc\3\2\2\2.\u00e7\3\2\2\2\60\u00eb\3\2\2\2\62\u00ef\3\2\2\2\64"+
		"\u00fb\3\2\2\2\66\u0105\3\2\2\28\u0107\3\2\2\2:\u0109\3\2\2\2<\u010b\3"+
		"\2\2\2>\u010d\3\2\2\2@\u011c\3\2\2\2B\u011e\3\2\2\2D\u0120\3\2\2\2F\u012c"+
		"\3\2\2\2H\u0143\3\2\2\2J\u0148\3\2\2\2L\u0154\3\2\2\2NP\5L\'\2ON\3\2\2"+
		"\2PQ\3\2\2\2QO\3\2\2\2QR\3\2\2\2RT\3\2\2\2SU\7v\2\2TS\3\2\2\2TU\3\2\2"+
		"\2UV\3\2\2\2VW\7\2\2\3W\3\3\2\2\2X]\7l\2\2Y]\5\6\4\2Z]\5\n\6\2[]\5\30"+
		"\r\2\\X\3\2\2\2\\Y\3\2\2\2\\Z\3\2\2\2\\[\3\2\2\2]\5\3\2\2\2^`\5\62\32"+
		"\2_^\3\2\2\2_`\3\2\2\2`a\3\2\2\2ah\7s\2\2bh\7L\2\2ce\5\62\32\2dc\3\2\2"+
		"\2de\3\2\2\2ef\3\2\2\2fh\5\b\5\2g_\3\2\2\2gb\3\2\2\2gd\3\2\2\2h\7\3\2"+
		"\2\2ij\t\2\2\2j\t\3\2\2\2kl\t\3\2\2l\13\3\2\2\2mn\t\4\2\2n\r\3\2\2\2o"+
		"p\7\3\2\2p\17\3\2\2\2qs\7I\2\2rt\7$\2\2sr\3\2\2\2st\3\2\2\2tz\3\2\2\2"+
		"uw\7J\2\2vx\7\5\2\2wv\3\2\2\2wx\3\2\2\2xz\3\2\2\2yq\3\2\2\2yu\3\2\2\2"+
		"yz\3\2\2\2z{\3\2\2\2{\u0082\5\22\n\2|~\7P\2\2}|\3\2\2\2}~\3\2\2\2~\177"+
		"\3\2\2\2\177\u0081\5\22\n\2\u0080}\3\2\2\2\u0081\u0084\3\2\2\2\u0082\u0080"+
		"\3\2\2\2\u0082\u0083\3\2\2\2\u0083\21\3\2\2\2\u0084\u0082\3\2\2\2\u0085"+
		"\u0087\5\24\13\2\u0086\u0088\5\26\f\2\u0087\u0086\3\2\2\2\u0087\u0088"+
		"\3\2\2\2\u0088\23\3\2\2\2\u0089\u008a\5\4\3\2\u008a\25\3\2\2\2\u008b\u008c"+
		"\5\16\b\2\u008c\u008d\5\4\3\2\u008d\27\3\2\2\2\u008e\u008f\7\4\2\2\u008f"+
		"\u009e\5\4\3\2\u0090\u009e\7!\2\2\u0091\u009e\7\"\2\2\u0092\u009e\7*\2"+
		"\2\u0093\u009e\7+\2\2\u0094\u009e\7,\2\2\u0095\u009e\7-\2\2\u0096\u009e"+
		"\7\65\2\2\u0097\u009e\7\66\2\2\u0098\u009e\7?\2\2\u0099\u009e\7@\2\2\u009a"+
		"\u009e\7L\2\2\u009b\u009e\7M\2\2\u009c\u009e\7N\2\2\u009d\u008e\3\2\2"+
		"\2\u009d\u0090\3\2\2\2\u009d\u0091\3\2\2\2\u009d\u0092\3\2\2\2\u009d\u0093"+
		"\3\2\2\2\u009d\u0094\3\2\2\2\u009d\u0095\3\2\2\2\u009d\u0096\3\2\2\2\u009d"+
		"\u0097\3\2\2\2\u009d\u0098\3\2\2\2\u009d\u0099\3\2\2\2\u009d\u009a\3\2"+
		"\2\2\u009d\u009b\3\2\2\2\u009d\u009c\3\2\2\2\u009e\31\3\2\2\2\u009f\u00a1"+
		"\t\5\2\2\u00a0\u00a2\7\'\2\2\u00a1\u00a0\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2"+
		"\u00a4\3\2\2\2\u00a3\u00a5\7$\2\2\u00a4\u00a3\3\2\2\2\u00a4\u00a5\3\2"+
		"\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a7\5\f\7\2\u00a7\33\3\2\2\2\u00a8\u00a9"+
		"\7D\2\2\u00a9\u00aa\5\b\5\2\u00aa\35\3\2\2\2\u00ab\u00ad\7\33\2\2\u00ac"+
		"\u00ae\7\61\2\2\u00ad\u00ac\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\3"+
		"\2\2\2\u00af\u00b0\5\f\7\2\u00b0\37\3\2\2\2\u00b1\u00b3\7#\2\2\u00b2\u00b4"+
		"\7\t\2\2\u00b3\u00b2\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5"+
		"\u00b6\5\f\7\2\u00b6!\3\2\2\2\u00b7\u00b8\7\60\2\2\u00b8\u00ba\5\b\5\2"+
		"\u00b9\u00bb\5\34\17\2\u00ba\u00b9\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bd"+
		"\3\2\2\2\u00bc\u00be\7C\2\2\u00bd\u00bc\3\2\2\2\u00bd\u00be\3\2\2\2\u00be"+
		"\u00c0\3\2\2\2\u00bf\u00c1\5\36\20\2\u00c0\u00bf\3\2\2\2\u00c0\u00c1\3"+
		"\2\2\2\u00c1\u00c3\3\2\2\2\u00c2\u00c4\5\32\16\2\u00c3\u00c2\3\2\2\2\u00c3"+
		"\u00c4\3\2\2\2\u00c4\u00c6\3\2\2\2\u00c5\u00c7\5 \21\2\u00c6\u00c5\3\2"+
		"\2\2\u00c6\u00c7\3\2\2\2\u00c7#\3\2\2\2\u00c8\u00c9\7\67\2\2\u00c9\u00ca"+
		"\5\f\7\2\u00ca%\3\2\2\2\u00cb\u00cc\78\2\2\u00cc\u00d0\5\f\7\2\u00cd\u00ce"+
		"\5\16\b\2\u00ce\u00cf\5\f\7\2\u00cf\u00d1\3\2\2\2\u00d0\u00cd\3\2\2\2"+
		"\u00d0\u00d1\3\2\2\2\u00d1\'\3\2\2\2\u00d2\u00d3\t\6\2\2\u00d3)\3\2\2"+
		"\2\u00d4\u00d6\7G\2\2\u00d5\u00d7\7$\2\2\u00d6\u00d5\3\2\2\2\u00d6\u00d7"+
		"\3\2\2\2\u00d7\u00d9\3\2\2\2\u00d8\u00d4\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9"+
		"\u00da\3\2\2\2\u00da\u00db\5(\25\2\u00db+\3\2\2\2\u00dc\u00de\7>\2\2\u00dd"+
		"\u00df\7$\2\2\u00de\u00dd\3\2\2\2\u00de\u00df\3\2\2\2\u00df\u00e0\3\2"+
		"\2\2\u00e0\u00e2\t\7\2\2\u00e1\u00e3\7:\2\2\u00e2\u00e1\3\2\2\2\u00e2"+
		"\u00e3\3\2\2\2\u00e3\u00e5\3\2\2\2\u00e4\u00e6\7\n\2\2\u00e5\u00e4\3\2"+
		"\2\2\u00e5\u00e6\3\2\2\2\u00e6-\3\2\2\2\u00e7\u00e9\t\b\2\2\u00e8\u00ea"+
		"\79\2\2\u00e9\u00e8\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea/\3\2\2\2\u00eb\u00ec"+
		"\7X\2\2\u00ec\61\3\2\2\2\u00ed\u00f0\7T\2\2\u00ee\u00f0\7S\2\2\u00ef\u00ed"+
		"\3\2\2\2\u00ef\u00ee\3\2\2\2\u00f0\63\3\2\2\2\u00f1\u00fc\7Z\2\2\u00f2"+
		"\u00fc\7^\2\2\u00f3\u00fc\7_\2\2\u00f4\u00fc\7`\2\2\u00f5\u00fc\7a\2\2"+
		"\u00f6\u00fc\7b\2\2\u00f7\u00fc\7c\2\2\u00f8\u00fc\7d\2\2\u00f9\u00fc"+
		"\7e\2\2\u00fa\u00fc\7f\2\2\u00fb\u00f1\3\2\2\2\u00fb\u00f2\3\2\2\2\u00fb"+
		"\u00f3\3\2\2\2\u00fb\u00f4\3\2\2\2\u00fb\u00f5\3\2\2\2\u00fb\u00f6\3\2"+
		"\2\2\u00fb\u00f7\3\2\2\2\u00fb\u00f8\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fb"+
		"\u00fa\3\2\2\2\u00fc\65\3\2\2\2\u00fd\u00ff\5\62\32\2\u00fe\u00fd\3\2"+
		"\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0106\5\64\33\2\u0101"+
		"\u0103\5\64\33\2\u0102\u0104\5\62\32\2\u0103\u0102\3\2\2\2\u0103\u0104"+
		"\3\2\2\2\u0104\u0106\3\2\2\2\u0105\u00fe\3\2\2\2\u0105\u0101\3\2\2\2\u0106"+
		"\67\3\2\2\2\u0107\u0108\t\t\2\2\u01089\3\2\2\2\u0109\u010a\t\t\2\2\u010a"+
		";\3\2\2\2\u010b\u010c\t\n\2\2\u010c=\3\2\2\2\u010d\u011a\5<\37\2\u010e"+
		"\u011b\58\35\2\u010f\u011b\5:\36\2\u0110\u0112\5\66\34\2\u0111\u0113\5"+
		"*\26\2\u0112\u0111\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0119\3\2\2\2\u0114"+
		"\u0116\5*\26\2\u0115\u0114\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u0117\3\2"+
		"\2\2\u0117\u0119\5\66\34\2\u0118\u0110\3\2\2\2\u0118\u0115\3\2\2\2\u0119"+
		"\u011b\3\2\2\2\u011a\u010e\3\2\2\2\u011a\u010f\3\2\2\2\u011a\u0118\3\2"+
		"\2\2\u011b?\3\2\2\2\u011c\u011d\t\13\2\2\u011dA\3\2\2\2\u011e\u011f\t"+
		"\f\2\2\u011fC\3\2\2\2\u0120\u0121\5@!\2\u0121\u0127\5\f\7\2\u0122\u0126"+
		"\5$\23\2\u0123\u0126\5*\26\2\u0124\u0126\5\"\22\2\u0125\u0122\3\2\2\2"+
		"\u0125\u0123\3\2\2\2\u0125\u0124\3\2\2\2\u0126\u0129\3\2\2\2\u0127\u0125"+
		"\3\2\2\2\u0127\u0128\3\2\2\2\u0128\u012a\3\2\2\2\u0129\u0127\3\2\2\2\u012a"+
		"\u012b\5\60\31\2\u012bE\3\2\2\2\u012c\u012d\5@!\2\u012d\u0137\5\f\7\2"+
		"\u012e\u0136\5.\30\2\u012f\u0136\5\"\22\2\u0130\u0136\5> \2\u0131\u0136"+
		"\5$\23\2\u0132\u0136\5*\26\2\u0133\u0136\5\20\t\2\u0134\u0136\5,\27\2"+
		"\u0135\u012e\3\2\2\2\u0135\u012f\3\2\2\2\u0135\u0130\3\2\2\2\u0135\u0131"+
		"\3\2\2\2\u0135\u0132\3\2\2\2\u0135\u0133\3\2\2\2\u0135\u0134\3\2\2\2\u0136"+
		"\u0139\3\2\2\2\u0137\u0135\3\2\2\2\u0137\u0138\3\2\2\2\u0138\u013f\3\2"+
		"\2\2\u0139\u0137\3\2\2\2\u013a\u013c\7\b\2\2\u013b\u013d\7K\2\2\u013c"+
		"\u013b\3\2\2\2\u013c\u013d\3\2\2\2\u013d\u013e\3\2\2\2\u013e\u0140\7L"+
		"\2\2\u013f\u013a\3\2\2\2\u013f\u0140\3\2\2\2\u0140\u0141\3\2\2\2\u0141"+
		"\u0142\5\60\31\2\u0142G\3\2\2\2\u0143\u0144\7o\2\2\u0144\u0145\5\f\7\2"+
		"\u0145\u0146\5&\24\2\u0146\u0147\5\60\31\2\u0147I\3\2\2\2\u0148\u0149"+
		"\7q\2\2\u0149\u014a\5\f\7\2\u014a\u014b\5\20\t\2\u014b\u014c\5\60\31\2"+
		"\u014cK\3\2\2\2\u014d\u0155\7Y\2\2\u014e\u0155\5D#\2\u014f\u0155\5F$\2"+
		"\u0150\u0155\5H%\2\u0151\u0155\5J&\2\u0152\u0155\5B\"\2\u0153\u0155\5"+
		"\60\31\2\u0154\u014d\3\2\2\2\u0154\u014e\3\2\2\2\u0154\u014f\3\2\2\2\u0154"+
		"\u0150\3\2\2\2\u0154\u0151\3\2\2\2\u0154\u0152\3\2\2\2\u0154\u0153\3\2"+
		"\2\2\u0155M\3\2\2\2/QT\\_dgswy}\u0082\u0087\u009d\u00a1\u00a4\u00ad\u00b3"+
		"\u00ba\u00bd\u00c0\u00c3\u00c6\u00d0\u00d6\u00d8\u00de\u00e2\u00e5\u00e9"+
		"\u00ef\u00fb\u00fe\u0103\u0105\u0112\u0115\u0118\u011a\u0125\u0127\u0135"+
		"\u0137\u013c\u013f\u0154";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}