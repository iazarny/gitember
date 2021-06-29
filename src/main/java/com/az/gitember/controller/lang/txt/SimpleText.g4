// default gramar to highling digits and string for gitemder
grammar SimpleText;


NAME
    : ( . )+?
    ;

bool
    : 'true'
    | 'false'
    ;

brakets
       : '{'
       | '}'
       | '('
       | ')'
       | '['
       | ']'
       ;

math
    : '+'
    | '-'
    | '*'
    | '/'
    | '^'
    ;

NUMBER : [0-9]+ ;
DOUBLE : NUMBER '.' NUMBER;
EXPONENT : NUMBER EXP;
EXPONENT2 : DOUBLE EXP;
fragment EXP   : ('e' | 'E' ) ('+' | '-')? [0-9] +;

DOT                 : '.';
COMA                 : ',';

*
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;

fragment
StringCharacter
	:	~["\\\r\n]
	;


WS
   : [ \t\n\r] + -> skip
   ;

