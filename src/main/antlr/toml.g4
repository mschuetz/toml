grammar toml;

toml : NL* pair* object+ ;

object : header NL+ pair* NL+;

value
    : string
    | number
    | datetime
    | array
    | bool
    ;

datetime : ISO8601 ;

ISO8601 : YEAR '-' MONTH '-' DAY 'T' HOUR ':' MINUTE ':' SECOND ZULU;

fragment ZULU : [zZ] ;
fragment YEAR : DIGIT4 ;
fragment MONTH: DIGIT2 ;
fragment DAY : DIGIT2 ;
fragment HOUR : DIGIT2 ;
fragment MINUTE : DIGIT2 ;
fragment SECOND : DIGIT2 ;

fragment DIGIT4 : DIGIT DIGIT DIGIT DIGIT ;
fragment DIGIT2 : DIGIT DIGIT ;

pair : name ASSIGN value NL+;

name : ID ;

array
    : OBRACKET value (COMMA value)* CBRACKET
    | OBRACKET CBRACKET
    ;

string : STRING ;
number : NUMBER ;
bool : BOOLEAN ;

fragment COLON : ':' ;

NL : [\r\n] ;

COMMA : ',' ;

BOOLEAN
    : 'true'
    | 'false'
    ;

header : OBRACKET objectname CBRACKET;

objectname : ID ;

OBRACKET : '[' ;
CBRACKET : ']' ;

ASSIGN : '=' ;

fragment DIGIT : [0-9] ;

STRING :  '"' (ESC | ~["\\])* '"' ;

fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

NUMBER
    :   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT                // -3, 45
    ;
fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ; // \- since - means "range" inside [...]

WS  :   [ \t]+ -> skip ;

COMMENT : '#' ~[\r\n]* -> skip; // skip comments

ID : NameStartChar NameChar* ;

fragment
NameChar
   : NameStartChar
   | '0'..'9'
   | '_'
   | '\u00B7'
   | '\u0300'..'\u036F'
   | '\u203F'..'\u2040'
   ;
fragment
NameStartChar
   : 'A'..'Z' | 'a'..'z'
   | '\u00C0'..'\u00D6'
   | '\u00D8'..'\u00F6'
   | '\u00F8'..'\u02FF'
   | '\u0370'..'\u037D'
   | '\u037F'..'\u1FFF'
   | '\u200C'..'\u200D'
   | '\u2070'..'\u218F'
   | '\u2C00'..'\u2FEF'
   | '\u3001'..'\uD7FF'
   | '\uF900'..'\uFDCF'
   | '\uFDF0'..'\uFFFD'
   ;