
/** 
This grammar is generated with antlrworks in order to parse an asm source
code.First of all the lexical rules established here do not replace the ones 
generated in JFlex we used in EditorASM to color the tokens, instead they are used
to provide the necessary tokens for the parser.
**/

/*
    Ported to Antlr4 by Tom Everett <tom@khubla.com>
    Some fixes for gitember by Igor Azarny <iazarny@yahoo.com>
*/
grammar MASM;




segments
   : DS
   | ES
   | CS
   | SS
   | GS
   | FS
   ;

REGISTERS
   : AH   | AL   | AX   | BH   | BL   | BX   | CH   | CL
   | CX   | DH   | DL   | DX   | SI   | DI   | SP   | BP
   | EAX   | EBX   | ECX   | EDX   | ESI   | EDI   | ESP   | EBP
   ;

KEYWORDS
   : MOV   | CMP   | TEST   | PUSH   | POP   | IDIV   | INC   | DEC
   | NEG   | MUL   | DIV   | IMUL   | NOT   | SETPO   | SETAE   | SETNLE
   | SETC   | SETNO   | SETNB   | SETP   | SETNGE   | SETL   | SETGE   | SETPE
   | SETNL   | SETNZ   | SETNE   | SETNC   | SETBE   | SETNP   | SETNS   | SETO
   | SETNA   | SETNAE   | SETZ   | SETLE   | SETNBE   | SETS   | SETE   | SETB
   | SETA   | SETG   | SETNG   | XCHG   | POPAD   | AAA   | POPA   | POPFD
   | CWD   | LAHF   | PUSHAD   | PUSHF   | AAS   | BSWAP   | PUSHFD   | CBW
   | CWDE   | XLAT   | AAM   | AAD   | CDQ   | DAA   | SAHF   | DAS   | INTO
   | IRET   | CLC   | STC   | CMC   | CLD   | STD   | CLI   | STI   | MOVSB
   | MOVSW   | MOVSD   | LODS   | LODSB   | LODSW  | LODSD   | STOS   | STOSB
   | STOSW   | SOTSD   | SCAS   | SCASB   | SCASW   | SCASD   | CMPS   | CMPSB
   | CMPSW   | CMPSD   | INSB  | INSW   | INSD   | OUTSB   | OUTSW   | OUTSD
   | ADC   | ADD   | SUB   | CBB   | XOR   | OR   | JNBE   | JNZ
   | JPO   | JZ   | JS   | LOOPNZ   | JGE   | JB   | JNB   | JO
   | JP   | JNO   | JNL   | JNAE   | LOOPZ   | JMP   | JNP   | LOOP
   | JL   | JCXZ   | JAE   | JNGE   | JA   | LOOPNE   | LOOPE   | JG
   | JNLE   | JE   | JNC   | JC   | JNA   | JBE   | JLE   | JPE
   | JNS   | JECXZ  | JNG   | MOVZX   | BSF   | BSR   | LES   | LEA
   | LDS   | INS   | OUTS   | XADD   | CMPXCHG   | SHL   | SHR   | ROR
   | ROL   | RCL   | SAL   | RCR   | SAR   | SHRD   | SHLD   | BTR
   | BT   | BTC   | CALL   | INT   | RETN   | RET   | RETF   | IN
   | OUT   | REP   | REPE   | REPZ   | REPNE   | REPNZ   | BYTE  | SBYTE
  | DB  | WORD  | SWORD  | DW  | DWORD  | SDWORD  | DD  | FWORD  | DF  | QWORD  | DQ  | TBYTE
  | DT  | REAL4  | REAL8  | REAL | FAR  | NEAR  | PROC  | QUESTION  | TIMES | OFFSET | 'invoke' | 'extern' | 'public' | 'end'
  ;

DIRECTIVES
   : ALPHA   | CONST   | CREF   | XCREF   | DATA   | DOSSEG   | ERR
   | ERR1   | ERR2   | ERRE   | ERRNZ   | ERRDEF   | ERRNDEF   | ERRB   | ERRNB
   | ERRIDN   | ERRDIF   | EVEN   | LIST   | WIDTH   | MASK   | SEQ   | XLIST
   | EXIT   | CODE   | MODEL   | P386
   ;


Identifier
   : Letter ('_' | Letter | Digit)*
   ;


DS
   : 'ds'
   ;


ES
   : 'es'
   ;


CS
   : 'cs'
   ;


SS
   : 'ss'
   ;


GS
   : 'gs'
   ;


FS
   : 'fs'
   ;


AH
   : 'ah'
   ;


AL
   : 'al'
   ;


AX
   : 'ax'
   ;


BH
   : 'bh'
   ;


BL
   : 'bl'
   ;


BX
   : 'bx'
   ;


CH
   : 'ch'
   ;


CL
   : 'cl'
   ;


CX
   : 'cx'
   ;


DH
   : 'dh'
   ;


DL
   : 'dl'
   ;


DX
   : 'dx'
   ;


SI
   : 'si'
   ;


DI
   : 'di'
   ;


SP
   : 'sp'
   ;


BP
   : 'bp'
   ;


EAX
   : 'eax'
   ;


EBX
   : 'ebx'
   ;


ECX
   : 'ecx'
   ;


EDX
   : 'edx'
   ;


ESI
   : 'esi'
   ;


EDI
   : 'edi'
   ;


ESP
   : 'esp'
   ;


EBP
   : 'ebp'
   ;


MOV
   : 'mov'
   ;


CMP
   : 'cmp'
   ;


TEST
   : 'test'
   ;


PUSH
   : 'push'
   ;


POP
   : 'pop'
   ;


IDIV
   : 'idiv'
   ;


INC
   : 'includelib' | 'include'
   ;


DEC
   : 'dec'
   ;


NEG
   : 'neg'
   ;


MUL
   : 'mul'
   ;


DIV
   : 'div'
   ;


IMUL
   : 'imul'
   ;


NOT
   : 'not'
   ;


SETPO
   : 'setpo'
   ;


SETAE
   : 'setae'
   ;


SETNLE
   : 'setnle'
   ;


SETC
   : 'setc'
   ;


SETNO
   : 'setno'
   ;


SETNB
   : 'setnb'
   ;


SETP
   : 'setp'
   ;


SETNGE
   : 'setnge'
   ;


SETL
   : 'setl'
   ;


SETGE
   : 'setge'
   ;


SETPE
   : 'setpe'
   ;


SETNL
   : 'setnl'
   ;


SETNZ
   : 'setnz'
   ;


SETNE
   : 'setne'
   ;


SETNC
   : 'setnc'
   ;


SETBE
   : 'setbe'
   ;


SETNP
   : 'setnp'
   ;


SETNS
   : 'setns'
   ;


SETO
   : 'seto'
   ;


SETNA
   : 'setna'
   ;


SETNAE
   : 'setnae'
   ;


SETZ
   : 'setz'
   ;


SETLE
   : 'setle'
   ;


SETNBE
   : 'setnbe'
   ;


SETS
   : 'sets'
   ;


SETE
   : 'sete'
   ;


SETB
   : 'setb'
   ;


SETA
   : 'seta'
   ;


SETG
   : 'setg'
   ;


SETNG
   : 'setng'
   ;


XCHG
   : 'xchg'
   ;


POPAD
   : 'popad'
   ;


AAA
   : 'aaa'
   ;


POPA
   : 'popa'
   ;


POPFD
   : 'popfd'
   ;


CWD
   : 'cwd'
   ;


LAHF
   : 'lahf'
   ;


PUSHAD
   : 'pushad'
   ;


PUSHF
   : 'pushf'
   ;


AAS
   : 'aas'
   ;


BSWAP
   : 'bswap'
   ;


PUSHFD
   : 'pushfd'
   ;


CBW
   : 'cbw'
   ;


CWDE
   : 'cwde'
   ;


XLAT
   : 'xlat'
   ;


AAM
   : 'aam'
   ;


AAD
   : 'aad'
   ;


CDQ
   : 'cdq'
   ;


DAA
   : 'daa'
   ;


SAHF
   : 'sahf'
   ;


DAS
   : 'das'
   ;


INTO
   : 'into'
   ;


IRET
   : 'iret'
   ;


CLC
   : 'clc'
   ;


STC
   : 'stc'
   ;


CMC
   : 'cmc'
   ;


CLD
   : 'cld'
   ;


STD
   : 'std'
   ;


CLI
   : 'cli'
   ;


STI
   : 'sti'
   ;


MOVSB
   : 'movsb'
   ;


MOVSW
   : 'movsw'
   ;


MOVSD
   : 'movsd'
   ;


LODS
   : 'lods'
   ;


LODSB
   : 'lodsb'
   ;


LODSW
   : 'lodsw'
   ;


LODSD
   : 'lodsd'
   ;


STOS
   : 'stos'
   ;


STOSB
   : 'stosb'
   ;


STOSW
   : 'stosw'
   ;


SOTSD
   : 'sotsd'
   ;


SCAS
   : 'scas'
   ;


SCASB
   : 'scasb'
   ;


SCASW
   : 'scasw'
   ;


SCASD
   : 'scasd'
   ;


CMPS
   : 'cmps'
   ;


CMPSB
   : 'cmpsb'
   ;


CMPSW
   : 'cmpsw'
   ;


CMPSD
   : 'cmpsd'
   ;


INSB
   : 'insb'
   ;


INSW
   : 'insw'
   ;


INSD
   : 'insd'
   ;


OUTSB
   : 'outsb'
   ;


OUTSW
   : 'outsw'
   ;


OUTSD
   : 'outsd'
   ;


ADC
   : 'adc'
   ;


ADD
   : 'add'
   ;


SUB
   : 'sub'
   ;


CBB
   : 'cbb'
   ;


XOR
   : 'xor'
   ;


OR
   : 'or'
   ;


JNBE
   : 'jnbe'
   ;


JNZ
   : 'jnz'
   ;


JPO
   : 'jpo'
   ;


JZ
   : 'jz'
   ;


JS
   : 'js'
   ;


LOOPNZ
   : 'loopnz'
   ;


JGE
   : 'jge'
   ;


JB
   : 'jb'
   ;


JNB
   : 'jnb'
   ;


JO
   : 'jo'
   ;


JP
   : 'jp'
   ;


JNO
   : 'jno'
   ;


JNL
   : 'jnl'
   ;


JNAE
   : 'jnae'
   ;


LOOPZ
   : 'loopz'
   ;


JMP
   : 'jmp'
   ;


JNP
   : 'jnp'
   ;


LOOP
   : 'loop'
   ;


JL
   : 'jl'
   ;


JCXZ
   : 'jcxz'
   ;


JAE
   : 'jae'
   ;


JNGE
   : 'jnge'
   ;


JA
   : 'ja'
   ;


LOOPNE
   : 'loopne'
   ;


LOOPE
   : 'loope'
   ;


JG
   : 'jg'
   ;


JNLE
   : 'jnle'
   ;


JE
   : 'je'
   ;


JNC
   : 'jnc'
   ;


JC
   : 'jc'
   ;


JNA
   : 'jna'
   ;


JBE
   : 'jbe'
   ;


JLE
   : 'jle'
   ;


JPE
   : 'jpe'
   ;


JNS
   : 'jns'
   ;


JECXZ
   : 'jecxz'
   ;


JNG
   : 'jng'
   ;


MOVZX
   : 'movzx'
   ;


BSF
   : 'bsf'
   ;


BSR
   : 'bsr'
   ;


LES
   : 'les'
   ;


LEA
   : 'lea'
   ;


LDS
   : 'lds'
   ;


INS
   : 'ins'
   ;


OUTS
   : 'outs'
   ;


XADD
   : 'xadd'
   ;


CMPXCHG
   : 'cmpxchg'
   ;


SHL
   : 'shl'
   ;


SHR
   : 'shr'
   ;


ROR
   : 'ror'
   ;


ROL
   : 'rol'
   ;


RCL
   : 'rcl'
   ;


SAL
   : 'sal'
   ;


RCR
   : 'rcr'
   ;


SAR
   : 'sar'
   ;


SHRD
   : 'shrd'
   ;


SHLD
   : 'shld'
   ;


BTR
   : 'btr'
   ;


BT
   : 'bt'
   ;


BTC
   : 'btc'
   ;


CALL
   : 'call'
   ;


INT
   : 'int'
   ;


RETN
   : 'retn'
   ;


RET
   : 'ret'
   ;


RETF
   : 'retf'
   ;


IN
   : 'in'
   ;


OUT
   : 'out'
   ;


REP
   : 'rep'
   ;


REPE
   : 'repe'
   ;


REPZ
   : 'repz'
   ;


REPNE
   : 'repne'
   ;


REPNZ
   : 'repnz'
   ;


ALPHA
   : '.alpha'
   ;


CONST
   : '.const'
   ;


CREF
   : '.cref'
   ;


XCREF
   : '.xcref'
   ;


DATA
   : '.data' | 'data' | 'data?'
   ;

DOSSEG
   : 'dosseg'
   ;


ERR
   : '.err'
   ;


ERR1
   : '.err1'
   ;


ERR2
   : '.err2'
   ;


ERRE
   : '.erre'
   ;


ERRNZ
   : '.errnz'
   ;


ERRDEF
   : '.errdef'
   ;


ERRNDEF
   : '.errndef'
   ;


ERRB
   : '.errb'
   ;


ERRNB
   : '.errnb'
   ;


ERRIDN
   : '.erridn[i]'
   ;


ERRDIF
   : '.errdif[i]'
   ;


EVEN
   : 'even'
   ;


LIST
   : '.list'
   ;


WIDTH
   : 'width'
   ;


MASK
   : 'mask'
   ;


SEQ
   : '.seq'
   ;


XLIST
   : '.xlist'
   ;


EXIT
   : '.exit'
   ;


CODE
   : '.code'
   ;

P386
   : '.386P'
   ;

MODEL
   : '.model'
   ;


BYTE
   : 'byte'
   ;


SBYTE
   : 'sbyte'
   ;


DB
   : 'db'
   ;


WORD
   : 'word'
   ;


SWORD
   : 'sword'
   ;


DW
   : 'dw'
   ;


DWORD
   : 'dword'
   ;


SDWORD
   : 'sdword'
   ;


DD
   : 'dd'
   ;


FWORD
   : 'fword'
   ;


DF
   : 'df'
   ;


QWORD
   : 'qword'
   ;


DQ
   : 'dq'
   ;


TBYTE
   : 'tbyte'
   ;


DT
   : 'dt'
   ;


REAL4
   : 'real4'
   ;


REAL8
   : 'real8'
   ;


REAL
   : 'real'
   ;


FAR
   : 'far'
   ;


NEAR
   : 'near'
   ;


PROC
   : 'proc'
   ;


QUESTION
   : '?'
   ;


TIMES
   : 'times'
   ;

OFFSET
   : 'offset'
   ;


Hexnum
   : HexDigit + ('h' | 'H')
   ;


Integer
   : (Digit +)
   ;


Octalnum
   : ('0' .. '7') + ('o' | 'O')
   ;


fragment HexDigit
   : ('0' .. '9' | 'a' .. 'f' | 'A' .. 'F')
   ;


FloatingPointLiteral
   : ('0' .. '9') + '.' ('0' .. '9')* Exponent? | '.' ('0' .. '9') + Exponent? | ('0' .. '9') + Exponent
   ;


fragment Exponent
   : ('e' | 'E') ('+' | '-')? ('0' .. '9') +
   ;


String
   : ' \'' ('\\' . | ~ ('\\' | '\''))* '\''
   ;


fragment Letter
   : ('a' .. 'z' | 'A' .. 'Z')
   ;


fragment Digit
   : '0' .. '9'
   ;


Etiqueta
   : Identifier (':')
   ;


Separator
   : ','
   ;


WS
   : (' ' | '\t' | '\n' | '\r') -> skip
   ;


LINE_COMMENT
   : ';' ~ ('\n' | '\r')* '\r'? '\n' ->  channel(HIDDEN)
   ;
