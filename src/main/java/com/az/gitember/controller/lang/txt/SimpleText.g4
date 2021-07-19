// default grammar to highling digits and string for gitemder
//Copyright (c) 2021 Igor Azarny <iazarny@yahoo.com>
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
    | '!' | '@' | '#' | '$' | '%' | '&'
    ;

SEMI : ';';
COMMA : ',';
DOT : '.';
ELLIPSIS : '...';
COLONCOLON : '::';

NUMBER : [0-9]+ ;
DOUBLE : NUMBER '.' NUMBER;
EXPONENT : NUMBER EXP;
EXPONENT2 : DOUBLE EXP;
fragment EXP   : ('e' | 'E' ) ('+' | '-')? [0-9] +;


StringLiteral
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

