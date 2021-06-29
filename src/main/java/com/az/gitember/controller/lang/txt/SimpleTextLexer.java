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
		T__9=10, T__10=11, T__11=12, T__12=13, NAME=14, NUMBER=15, DOUBLE=16, 
		EXPONENT=17, EXPONENT2=18, DOT=19, COMA=20, StringLiteral=21, WS=22;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "NAME", "NUMBER", "DOUBLE", "EXPONENT", 
			"EXPONENT2", "EXP", "DOT", "COMA", "StringLiteral", "StringCharacters", 
			"StringCharacter", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'true'", "'false'", "'{'", "'}'", "'('", "')'", "'['", "']'", 
			"'+'", "'-'", "'*'", "'/'", "'^'", null, null, null, null, null, "'.'", 
			"','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "NAME", "NUMBER", "DOUBLE", "EXPONENT", "EXPONENT2", "DOT", 
			"COMA", "StringLiteral", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\30\u008b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3"+
		"\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3"+
		"\r\3\16\3\16\3\17\6\17X\n\17\r\17\16\17Y\3\20\6\20]\n\20\r\20\16\20^\3"+
		"\21\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\24\3\24\5\24m\n\24"+
		"\3\24\6\24p\n\24\r\24\16\24q\3\25\3\25\3\26\3\26\3\27\3\27\5\27z\n\27"+
		"\3\27\3\27\3\30\6\30\177\n\30\r\30\16\30\u0080\3\31\3\31\3\32\6\32\u0086"+
		"\n\32\r\32\16\32\u0087\3\32\3\32\3Y\2\33\3\3\5\4\7\5\t\6\13\7\r\b\17\t"+
		"\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\2)\25+\26"+
		"-\27/\2\61\2\63\30\3\2\7\3\2\62;\4\2GGgg\4\2--//\6\2\f\f\17\17$$^^\5\2"+
		"\13\f\17\17\"\"\2\u008e\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2\63\3\2\2\2\3\65\3\2\2\2\5:\3\2\2\2\7@\3\2\2\2\tB\3\2\2\2\13D\3\2\2"+
		"\2\rF\3\2\2\2\17H\3\2\2\2\21J\3\2\2\2\23L\3\2\2\2\25N\3\2\2\2\27P\3\2"+
		"\2\2\31R\3\2\2\2\33T\3\2\2\2\35W\3\2\2\2\37\\\3\2\2\2!`\3\2\2\2#d\3\2"+
		"\2\2%g\3\2\2\2\'j\3\2\2\2)s\3\2\2\2+u\3\2\2\2-w\3\2\2\2/~\3\2\2\2\61\u0082"+
		"\3\2\2\2\63\u0085\3\2\2\2\65\66\7v\2\2\66\67\7t\2\2\678\7w\2\289\7g\2"+
		"\29\4\3\2\2\2:;\7h\2\2;<\7c\2\2<=\7n\2\2=>\7u\2\2>?\7g\2\2?\6\3\2\2\2"+
		"@A\7}\2\2A\b\3\2\2\2BC\7\177\2\2C\n\3\2\2\2DE\7*\2\2E\f\3\2\2\2FG\7+\2"+
		"\2G\16\3\2\2\2HI\7]\2\2I\20\3\2\2\2JK\7_\2\2K\22\3\2\2\2LM\7-\2\2M\24"+
		"\3\2\2\2NO\7/\2\2O\26\3\2\2\2PQ\7,\2\2Q\30\3\2\2\2RS\7\61\2\2S\32\3\2"+
		"\2\2TU\7`\2\2U\34\3\2\2\2VX\13\2\2\2WV\3\2\2\2XY\3\2\2\2YZ\3\2\2\2YW\3"+
		"\2\2\2Z\36\3\2\2\2[]\t\2\2\2\\[\3\2\2\2]^\3\2\2\2^\\\3\2\2\2^_\3\2\2\2"+
		"_ \3\2\2\2`a\5\37\20\2ab\7\60\2\2bc\5\37\20\2c\"\3\2\2\2de\5\37\20\2e"+
		"f\5\'\24\2f$\3\2\2\2gh\5!\21\2hi\5\'\24\2i&\3\2\2\2jl\t\3\2\2km\t\4\2"+
		"\2lk\3\2\2\2lm\3\2\2\2mo\3\2\2\2np\t\2\2\2on\3\2\2\2pq\3\2\2\2qo\3\2\2"+
		"\2qr\3\2\2\2r(\3\2\2\2st\7\60\2\2t*\3\2\2\2uv\7.\2\2v,\3\2\2\2wy\7$\2"+
		"\2xz\5/\30\2yx\3\2\2\2yz\3\2\2\2z{\3\2\2\2{|\7$\2\2|.\3\2\2\2}\177\5\61"+
		"\31\2~}\3\2\2\2\177\u0080\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081"+
		"\60\3\2\2\2\u0082\u0083\n\5\2\2\u0083\62\3\2\2\2\u0084\u0086\t\6\2\2\u0085"+
		"\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0088\3\2"+
		"\2\2\u0088\u0089\3\2\2\2\u0089\u008a\b\32\2\2\u008a\64\3\2\2\2\n\2Y^l"+
		"qy\u0080\u0087\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}