grammar Meaningful;

program
    : exp (PLUS exp)*
    ;

exp
    : simple_column
    | complex_column
    | string
    | '$' ev_exp;

ev_exp
    : simple_column
    | complex_column;

simple_column
    : COLUMN_NAME;

complex_column
    : simple_column (DOT simple_column)+
    ;

string
    : STRING_LITERAL
    ;

COLUMN_NAME
    : [a-zA-Z_] [a-zA-Z_0-9]*;

STRING_LITERAL
    : '\'' ( ~'\'' | '\'\'' )* '\''
    ;

DOT : '.';
PLUS: '+';

WS: [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines ;
