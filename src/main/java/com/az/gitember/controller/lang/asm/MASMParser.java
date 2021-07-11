// Generated from MASM.g4 by ANTLR 4.9.2
package com.az.gitember.controller.lang.asm;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MASMParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		REGISTERS=1, KEYWORDS=2, DIRECTIVES=3, Identifier=4, DS=5, ES=6, CS=7, 
		SS=8, GS=9, FS=10, AH=11, AL=12, AX=13, BH=14, BL=15, BX=16, CH=17, CL=18, 
		CX=19, DH=20, DL=21, DX=22, SI=23, DI=24, SP=25, BP=26, EAX=27, EBX=28, 
		ECX=29, EDX=30, ESI=31, EDI=32, ESP=33, EBP=34, MOV=35, CMP=36, TEST=37, 
		PUSH=38, POP=39, IDIV=40, INC=41, DEC=42, NEG=43, MUL=44, DIV=45, IMUL=46, 
		NOT=47, SETPO=48, SETAE=49, SETNLE=50, SETC=51, SETNO=52, SETNB=53, SETP=54, 
		SETNGE=55, SETL=56, SETGE=57, SETPE=58, SETNL=59, SETNZ=60, SETNE=61, 
		SETNC=62, SETBE=63, SETNP=64, SETNS=65, SETO=66, SETNA=67, SETNAE=68, 
		SETZ=69, SETLE=70, SETNBE=71, SETS=72, SETE=73, SETB=74, SETA=75, SETG=76, 
		SETNG=77, XCHG=78, POPAD=79, AAA=80, POPA=81, POPFD=82, CWD=83, LAHF=84, 
		PUSHAD=85, PUSHF=86, AAS=87, BSWAP=88, PUSHFD=89, CBW=90, CWDE=91, XLAT=92, 
		AAM=93, AAD=94, CDQ=95, DAA=96, SAHF=97, DAS=98, INTO=99, IRET=100, CLC=101, 
		STC=102, CMC=103, CLD=104, STD=105, CLI=106, STI=107, MOVSB=108, MOVSW=109, 
		MOVSD=110, LODS=111, LODSB=112, LODSW=113, LODSD=114, STOS=115, STOSB=116, 
		STOSW=117, SOTSD=118, SCAS=119, SCASB=120, SCASW=121, SCASD=122, CMPS=123, 
		CMPSB=124, CMPSW=125, CMPSD=126, INSB=127, INSW=128, INSD=129, OUTSB=130, 
		OUTSW=131, OUTSD=132, ADC=133, ADD=134, SUB=135, CBB=136, XOR=137, OR=138, 
		JNBE=139, JNZ=140, JPO=141, JZ=142, JS=143, LOOPNZ=144, JGE=145, JB=146, 
		JNB=147, JO=148, JP=149, JNO=150, JNL=151, JNAE=152, LOOPZ=153, JMP=154, 
		JNP=155, LOOP=156, JL=157, JCXZ=158, JAE=159, JNGE=160, JA=161, LOOPNE=162, 
		LOOPE=163, JG=164, JNLE=165, JE=166, JNC=167, JC=168, JNA=169, JBE=170, 
		JLE=171, JPE=172, JNS=173, JECXZ=174, JNG=175, MOVZX=176, BSF=177, BSR=178, 
		LES=179, LEA=180, LDS=181, INS=182, OUTS=183, XADD=184, CMPXCHG=185, SHL=186, 
		SHR=187, ROR=188, ROL=189, RCL=190, SAL=191, RCR=192, SAR=193, SHRD=194, 
		SHLD=195, BTR=196, BT=197, BTC=198, CALL=199, INT=200, RETN=201, RET=202, 
		RETF=203, IN=204, OUT=205, REP=206, REPE=207, REPZ=208, REPNE=209, REPNZ=210, 
		ALPHA=211, CONST=212, CREF=213, XCREF=214, DATA=215, DOSSEG=216, ERR=217, 
		ERR1=218, ERR2=219, ERRE=220, ERRNZ=221, ERRDEF=222, ERRNDEF=223, ERRB=224, 
		ERRNB=225, ERRIDN=226, ERRDIF=227, EVEN=228, LIST=229, WIDTH=230, MASK=231, 
		SEQ=232, XLIST=233, EXIT=234, CODE=235, P386=236, MODEL=237, BYTE=238, 
		SBYTE=239, DB=240, WORD=241, SWORD=242, DW=243, DWORD=244, SDWORD=245, 
		DD=246, FWORD=247, DF=248, QWORD=249, DQ=250, TBYTE=251, DT=252, REAL4=253, 
		REAL8=254, REAL=255, FAR=256, NEAR=257, PROC=258, QUESTION=259, TIMES=260, 
		OFFSET=261, Hexnum=262, Integer=263, Octalnum=264, FloatingPointLiteral=265, 
		String=266, Etiqueta=267, Separator=268, WS=269, LINE_COMMENT=270;
	public static final int
		RULE_segments = 0;
	private static String[] makeRuleNames() {
		return new String[] {
			"segments"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, "'ds'", "'es'", "'cs'", "'ss'", "'gs'", 
			"'fs'", "'ah'", "'al'", "'ax'", "'bh'", "'bl'", "'bx'", "'ch'", "'cl'", 
			"'cx'", "'dh'", "'dl'", "'dx'", "'si'", "'di'", "'sp'", "'bp'", "'eax'", 
			"'ebx'", "'ecx'", "'edx'", "'esi'", "'edi'", "'esp'", "'ebp'", "'mov'", 
			"'cmp'", "'test'", "'push'", "'pop'", "'idiv'", null, "'dec'", "'neg'", 
			"'mul'", "'div'", "'imul'", "'not'", "'setpo'", "'setae'", "'setnle'", 
			"'setc'", "'setno'", "'setnb'", "'setp'", "'setnge'", "'setl'", "'setge'", 
			"'setpe'", "'setnl'", "'setnz'", "'setne'", "'setnc'", "'setbe'", "'setnp'", 
			"'setns'", "'seto'", "'setna'", "'setnae'", "'setz'", "'setle'", "'setnbe'", 
			"'sets'", "'sete'", "'setb'", "'seta'", "'setg'", "'setng'", "'xchg'", 
			"'popad'", "'aaa'", "'popa'", "'popfd'", "'cwd'", "'lahf'", "'pushad'", 
			"'pushf'", "'aas'", "'bswap'", "'pushfd'", "'cbw'", "'cwde'", "'xlat'", 
			"'aam'", "'aad'", "'cdq'", "'daa'", "'sahf'", "'das'", "'into'", "'iret'", 
			"'clc'", "'stc'", "'cmc'", "'cld'", "'std'", "'cli'", "'sti'", "'movsb'", 
			"'movsw'", "'movsd'", "'lods'", "'lodsb'", "'lodsw'", "'lodsd'", "'stos'", 
			"'stosb'", "'stosw'", "'sotsd'", "'scas'", "'scasb'", "'scasw'", "'scasd'", 
			"'cmps'", "'cmpsb'", "'cmpsw'", "'cmpsd'", "'insb'", "'insw'", "'insd'", 
			"'outsb'", "'outsw'", "'outsd'", "'adc'", "'add'", "'sub'", "'cbb'", 
			"'xor'", "'or'", "'jnbe'", "'jnz'", "'jpo'", "'jz'", "'js'", "'loopnz'", 
			"'jge'", "'jb'", "'jnb'", "'jo'", "'jp'", "'jno'", "'jnl'", "'jnae'", 
			"'loopz'", "'jmp'", "'jnp'", "'loop'", "'jl'", "'jcxz'", "'jae'", "'jnge'", 
			"'ja'", "'loopne'", "'loope'", "'jg'", "'jnle'", "'je'", "'jnc'", "'jc'", 
			"'jna'", "'jbe'", "'jle'", "'jpe'", "'jns'", "'jecxz'", "'jng'", "'movzx'", 
			"'bsf'", "'bsr'", "'les'", "'lea'", "'lds'", "'ins'", "'outs'", "'xadd'", 
			"'cmpxchg'", "'shl'", "'shr'", "'ror'", "'rol'", "'rcl'", "'sal'", "'rcr'", 
			"'sar'", "'shrd'", "'shld'", "'btr'", "'bt'", "'btc'", "'call'", "'int'", 
			"'retn'", "'ret'", "'retf'", "'in'", "'out'", "'rep'", "'repe'", "'repz'", 
			"'repne'", "'repnz'", "'.alpha'", "'.const'", "'.cref'", "'.xcref'", 
			null, "'dosseg'", "'.err'", "'.err1'", "'.err2'", "'.erre'", "'.errnz'", 
			"'.errdef'", "'.errndef'", "'.errb'", "'.errnb'", "'.erridn[i]'", "'.errdif[i]'", 
			"'even'", "'.list'", "'width'", "'mask'", "'.seq'", "'.xlist'", "'.exit'", 
			"'.code'", "'.386P'", "'.model'", "'byte'", "'sbyte'", "'db'", "'word'", 
			"'sword'", "'dw'", "'dword'", "'sdword'", "'dd'", "'fword'", "'df'", 
			"'qword'", "'dq'", "'tbyte'", "'dt'", "'real4'", "'real8'", "'real'", 
			"'far'", "'near'", "'proc'", "'?'", "'times'", "'offset'", null, null, 
			null, null, null, null, "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "REGISTERS", "KEYWORDS", "DIRECTIVES", "Identifier", "DS", "ES", 
			"CS", "SS", "GS", "FS", "AH", "AL", "AX", "BH", "BL", "BX", "CH", "CL", 
			"CX", "DH", "DL", "DX", "SI", "DI", "SP", "BP", "EAX", "EBX", "ECX", 
			"EDX", "ESI", "EDI", "ESP", "EBP", "MOV", "CMP", "TEST", "PUSH", "POP", 
			"IDIV", "INC", "DEC", "NEG", "MUL", "DIV", "IMUL", "NOT", "SETPO", "SETAE", 
			"SETNLE", "SETC", "SETNO", "SETNB", "SETP", "SETNGE", "SETL", "SETGE", 
			"SETPE", "SETNL", "SETNZ", "SETNE", "SETNC", "SETBE", "SETNP", "SETNS", 
			"SETO", "SETNA", "SETNAE", "SETZ", "SETLE", "SETNBE", "SETS", "SETE", 
			"SETB", "SETA", "SETG", "SETNG", "XCHG", "POPAD", "AAA", "POPA", "POPFD", 
			"CWD", "LAHF", "PUSHAD", "PUSHF", "AAS", "BSWAP", "PUSHFD", "CBW", "CWDE", 
			"XLAT", "AAM", "AAD", "CDQ", "DAA", "SAHF", "DAS", "INTO", "IRET", "CLC", 
			"STC", "CMC", "CLD", "STD", "CLI", "STI", "MOVSB", "MOVSW", "MOVSD", 
			"LODS", "LODSB", "LODSW", "LODSD", "STOS", "STOSB", "STOSW", "SOTSD", 
			"SCAS", "SCASB", "SCASW", "SCASD", "CMPS", "CMPSB", "CMPSW", "CMPSD", 
			"INSB", "INSW", "INSD", "OUTSB", "OUTSW", "OUTSD", "ADC", "ADD", "SUB", 
			"CBB", "XOR", "OR", "JNBE", "JNZ", "JPO", "JZ", "JS", "LOOPNZ", "JGE", 
			"JB", "JNB", "JO", "JP", "JNO", "JNL", "JNAE", "LOOPZ", "JMP", "JNP", 
			"LOOP", "JL", "JCXZ", "JAE", "JNGE", "JA", "LOOPNE", "LOOPE", "JG", "JNLE", 
			"JE", "JNC", "JC", "JNA", "JBE", "JLE", "JPE", "JNS", "JECXZ", "JNG", 
			"MOVZX", "BSF", "BSR", "LES", "LEA", "LDS", "INS", "OUTS", "XADD", "CMPXCHG", 
			"SHL", "SHR", "ROR", "ROL", "RCL", "SAL", "RCR", "SAR", "SHRD", "SHLD", 
			"BTR", "BT", "BTC", "CALL", "INT", "RETN", "RET", "RETF", "IN", "OUT", 
			"REP", "REPE", "REPZ", "REPNE", "REPNZ", "ALPHA", "CONST", "CREF", "XCREF", 
			"DATA", "DOSSEG", "ERR", "ERR1", "ERR2", "ERRE", "ERRNZ", "ERRDEF", "ERRNDEF", 
			"ERRB", "ERRNB", "ERRIDN", "ERRDIF", "EVEN", "LIST", "WIDTH", "MASK", 
			"SEQ", "XLIST", "EXIT", "CODE", "P386", "MODEL", "BYTE", "SBYTE", "DB", 
			"WORD", "SWORD", "DW", "DWORD", "SDWORD", "DD", "FWORD", "DF", "QWORD", 
			"DQ", "TBYTE", "DT", "REAL4", "REAL8", "REAL", "FAR", "NEAR", "PROC", 
			"QUESTION", "TIMES", "OFFSET", "Hexnum", "Integer", "Octalnum", "FloatingPointLiteral", 
			"String", "Etiqueta", "Separator", "WS", "LINE_COMMENT"
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
	public String getGrammarFileName() { return "MASM.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MASMParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SegmentsContext extends ParserRuleContext {
		public TerminalNode DS() { return getToken(MASMParser.DS, 0); }
		public TerminalNode ES() { return getToken(MASMParser.ES, 0); }
		public TerminalNode CS() { return getToken(MASMParser.CS, 0); }
		public TerminalNode SS() { return getToken(MASMParser.SS, 0); }
		public TerminalNode GS() { return getToken(MASMParser.GS, 0); }
		public TerminalNode FS() { return getToken(MASMParser.FS, 0); }
		public SegmentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_segments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MASMListener ) ((MASMListener)listener).enterSegments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MASMListener ) ((MASMListener)listener).exitSegments(this);
		}
	}

	public final SegmentsContext segments() throws RecognitionException {
		SegmentsContext _localctx = new SegmentsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_segments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DS) | (1L << ES) | (1L << CS) | (1L << SS) | (1L << GS) | (1L << FS))) != 0)) ) {
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0110\7\4\2\t\2\3"+
		"\2\3\2\3\2\2\2\3\2\2\3\3\2\7\f\2\5\2\4\3\2\2\2\4\5\t\2\2\2\5\3\3\2\2\2"+
		"\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}