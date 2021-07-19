// Generated from SimpleText.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.txt;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SimpleTextLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, NAME=20, SEMI=21, COMMA=22, DOT=23, ELLIPSIS=24, COLONCOLON=25, 
		NUMBER=26, DOUBLE=27, EXPONENT=28, EXPONENT2=29, StringLiteral=30, WS=31;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "NAME", "SEMI", "COMMA", "DOT", "ELLIPSIS", "COLONCOLON", 
			"NUMBER", "DOUBLE", "EXPONENT", "EXPONENT2", "EXP", "StringLiteral", 
			"StringCharacters", "StringCharacter", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'true'", "'false'", "'{'", "'}'", "'('", "')'", "'['", "']'", 
			"'+'", "'-'", "'*'", "'/'", "'^'", "'!'", "'@'", "'#'", "'$'", "'%'", 
			"'&'", null, "';'", "','", "'.'", "'...'", "'::'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "NAME", "SEMI", "COMMA", 
			"DOT", "ELLIPSIS", "COLONCOLON", "NUMBER", "DOUBLE", "EXPONENT", "EXPONENT2", 
			"StringLiteral", "WS"
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


	public SimpleTextLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SimpleText.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2!\u00b2\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4"+
		"\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r"+
		"\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24"+
		"\3\24\3\25\6\25v\n\25\r\25\16\25w\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3"+
		"\31\3\31\3\31\3\32\3\32\3\32\3\33\6\33\u0088\n\33\r\33\16\33\u0089\3\34"+
		"\3\34\3\34\3\34\3\35\3\35\3\35\3\36\3\36\3\36\3\37\3\37\5\37\u0098\n\37"+
		"\3\37\6\37\u009b\n\37\r\37\16\37\u009c\3 \3 \5 \u00a1\n \3 \3 \3!\6!\u00a6"+
		"\n!\r!\16!\u00a7\3\"\3\"\3#\6#\u00ad\n#\r#\16#\u00ae\3#\3#\3w\2$\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37=\2?"+
		" A\2C\2E!\3\2\7\3\2\62;\4\2GGgg\4\2--//\6\2\f\f\17\17$$^^\5\2\13\f\17"+
		"\17\"\"\2\u00b5\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3"+
		"\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2"+
		"\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3"+
		"\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2"+
		"\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\2"+
		"9\3\2\2\2\2;\3\2\2\2\2?\3\2\2\2\2E\3\2\2\2\3G\3\2\2\2\5L\3\2\2\2\7R\3"+
		"\2\2\2\tT\3\2\2\2\13V\3\2\2\2\rX\3\2\2\2\17Z\3\2\2\2\21\\\3\2\2\2\23^"+
		"\3\2\2\2\25`\3\2\2\2\27b\3\2\2\2\31d\3\2\2\2\33f\3\2\2\2\35h\3\2\2\2\37"+
		"j\3\2\2\2!l\3\2\2\2#n\3\2\2\2%p\3\2\2\2\'r\3\2\2\2)u\3\2\2\2+y\3\2\2\2"+
		"-{\3\2\2\2/}\3\2\2\2\61\177\3\2\2\2\63\u0083\3\2\2\2\65\u0087\3\2\2\2"+
		"\67\u008b\3\2\2\29\u008f\3\2\2\2;\u0092\3\2\2\2=\u0095\3\2\2\2?\u009e"+
		"\3\2\2\2A\u00a5\3\2\2\2C\u00a9\3\2\2\2E\u00ac\3\2\2\2GH\7v\2\2HI\7t\2"+
		"\2IJ\7w\2\2JK\7g\2\2K\4\3\2\2\2LM\7h\2\2MN\7c\2\2NO\7n\2\2OP\7u\2\2PQ"+
		"\7g\2\2Q\6\3\2\2\2RS\7}\2\2S\b\3\2\2\2TU\7\177\2\2U\n\3\2\2\2VW\7*\2\2"+
		"W\f\3\2\2\2XY\7+\2\2Y\16\3\2\2\2Z[\7]\2\2[\20\3\2\2\2\\]\7_\2\2]\22\3"+
		"\2\2\2^_\7-\2\2_\24\3\2\2\2`a\7/\2\2a\26\3\2\2\2bc\7,\2\2c\30\3\2\2\2"+
		"de\7\61\2\2e\32\3\2\2\2fg\7`\2\2g\34\3\2\2\2hi\7#\2\2i\36\3\2\2\2jk\7"+
		"B\2\2k \3\2\2\2lm\7%\2\2m\"\3\2\2\2no\7&\2\2o$\3\2\2\2pq\7\'\2\2q&\3\2"+
		"\2\2rs\7(\2\2s(\3\2\2\2tv\13\2\2\2ut\3\2\2\2vw\3\2\2\2wx\3\2\2\2wu\3\2"+
		"\2\2x*\3\2\2\2yz\7=\2\2z,\3\2\2\2{|\7.\2\2|.\3\2\2\2}~\7\60\2\2~\60\3"+
		"\2\2\2\177\u0080\7\60\2\2\u0080\u0081\7\60\2\2\u0081\u0082\7\60\2\2\u0082"+
		"\62\3\2\2\2\u0083\u0084\7<\2\2\u0084\u0085\7<\2\2\u0085\64\3\2\2\2\u0086"+
		"\u0088\t\2\2\2\u0087\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u0087\3\2"+
		"\2\2\u0089\u008a\3\2\2\2\u008a\66\3\2\2\2\u008b\u008c\5\65\33\2\u008c"+
		"\u008d\7\60\2\2\u008d\u008e\5\65\33\2\u008e8\3\2\2\2\u008f\u0090\5\65"+
		"\33\2\u0090\u0091\5=\37\2\u0091:\3\2\2\2\u0092\u0093\5\67\34\2\u0093\u0094"+
		"\5=\37\2\u0094<\3\2\2\2\u0095\u0097\t\3\2\2\u0096\u0098\t\4\2\2\u0097"+
		"\u0096\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u009a\3\2\2\2\u0099\u009b\t\2"+
		"\2\2\u009a\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\u009a\3\2\2\2\u009c"+
		"\u009d\3\2\2\2\u009d>\3\2\2\2\u009e\u00a0\7$\2\2\u009f\u00a1\5A!\2\u00a0"+
		"\u009f\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a3\7$"+
		"\2\2\u00a3@\3\2\2\2\u00a4\u00a6\5C\"\2\u00a5\u00a4\3\2\2\2\u00a6\u00a7"+
		"\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8B\3\2\2\2\u00a9"+
		"\u00aa\n\5\2\2\u00aaD\3\2\2\2\u00ab\u00ad\t\6\2\2\u00ac\u00ab\3\2\2\2"+
		"\u00ad\u00ae\3\2\2\2\u00ae\u00ac\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b0"+
		"\3\2\2\2\u00b0\u00b1\b#\2\2\u00b1F\3\2\2\2\n\2w\u0089\u0097\u009c\u00a0"+
		"\u00a7\u00ae\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}