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

lexer grammar copybookLexer;


THRU_OR_THROUGH:
    THRU | THROUGH
    ;


// keywords
ALL : A L L;
ARE : A R E;
ASCENDING: A S C E N D I N G;
BINARY : B I N A R Y;
BLANK: B L A N K;
BY: B Y;
CHARACTER : C H A R A C T E R;
CHARACTERS : C H A R A C T E R S;
COMP : C O M P;
COMP_0 : C O M P MINUSCHAR '0';
COMP_1 : C O M P MINUSCHAR '1';
COMP_2 : C O M P MINUSCHAR '2';
COMP_3 : C O M P MINUSCHAR '3';
COMP_4 : C O M P MINUSCHAR '4';
COMP_5 : C O M P MINUSCHAR '5';
COMPUTATIONAL : C O M P U T A T I O N A L;
COMPUTATIONAL_0 : C O M P U T A T I O N A L MINUSCHAR '0';
COMPUTATIONAL_1 : C O M P U T A T I O N A L MINUSCHAR '1';
COMPUTATIONAL_2 : C O M P U T A T I O N A L MINUSCHAR '2';
COMPUTATIONAL_3 : C O M P U T A T I O N A L MINUSCHAR '3';
COMPUTATIONAL_4 : C O M P U T A T I O N A L MINUSCHAR '4';
COMPUTATIONAL_5 : C O M P U T A T I O N A L MINUSCHAR '5';
COPY : C O P Y;
DEPENDING : D E P E N D I N G;
DESCENDING: D E S C E N D I N G;
DISPLAY : D I S P L A Y;
EXTERNAL: E X T E R N A L;
FALSE: F A L S E;
FROM : F R O M;
HIGH_VALUE : H I G H MINUSCHAR V A L U E;
HIGH_VALUES : H I G H MINUSCHAR V A L U E S;
INDEXED : I N D E X E D;
IS : I S;
JUST : J U S T;
JUSTIFIED : J U S T I F I E D;
KEY: K E Y;
LEADING : L E A D I N G;
LEFT : L E F T;
LOW_VALUE : L O W MINUSCHAR V A L U E;
LOW_VALUES : L O W MINUSCHAR V A L U E S;
NULL : N U L L;
NULLS : N U L L S;
NUMBER : N U M B E R;
NUMERIC : N U M E R I C;
OCCURS : O C C U R S;
ON : O N;
PACKED_DECIMAL : P A C K E D MINUSCHAR D E C I M A L;
PIC : P I C;
PICTURE : P I C T U R E;
QUOTE : Q U O T E;
QUOTES : Q U O T E S;
REDEFINES : R E D E F I N E S;
RENAMES : R E N A M E S;
RIGHT : R I G H T;
SEPARATE : S E P A R A T E;
SKIP1: S K I P '1' ('\r' | '\n' | '\f' | '\t' | ' ')+ -> skip;
SKIP2: S K I P '2' ('\r' | '\n' | '\f' | '\t' | ' ')+ -> skip;
SKIP3: S K I P '3' ('\r' | '\n' | '\f' | '\t' | ' ')+ -> skip;
SIGN : S I G N;
SPACE : S P A C E;
SPACES : S P A C E S;
THROUGH : T H R O U G H;
THRU : T H R U;
TIMES : T I M E S;
TO : T O;
TRAILING : T R A I L I N G;
TRUE : T R U E;
USAGE : U S A G E;
USING : U S I N G;
VALUE : V A L U E;
VALUES : V A L U E S;
WHEN : W H E N;
ZERO : Z E R O;
ZEROS : Z E R O S;
ZEROES : Z E R O E S;

// symbols
DOUBLEQUOTE : '"';
COMMACHAR : ',';
DOT : '.';
LPARENCHAR : '(';
MINUSCHAR : '-';
PLUSCHAR : '+';
RPARENCHAR : ')';
SINGLEQUOTE : '\'';
SLASHCHAR : '/';
TERMINAL : '.' ('\r' | '\n' | '\f' | '\t' | ' ')+ | '.' CONTROL_Z? EOF;
COMMENT:  '*' ~( '\r' | '\n' )* -> skip;


// special cases (for the lengths)
NINES: '9'+;
A_S: A+;
P_S: P+ '9'*;
X_S: X+;
S_S: S '9'+ V? P* '9'* | S '9'* V? P* '9'+;
Z_S: Z+ '9'* P* | Z+ '9'* V P* '9'*;
V_S: V+ '9'+;

P_NS: P+ '9'*;
S_NS: S '9'* V? P* '9'*;
Z_NS: Z+ '9'* P* | Z+ '9'* V P* '9'*;
V_NS: V+ '9'*;


// numbers
PRECISION_9_EXPLICIT_DOT: S? LENGTH_TYPE_9? (DOT | COMMACHAR) LENGTH_TYPE_9;
PRECISION_9_DECIMAL_SCALED: S? LENGTH_TYPE_9? V ((LENGTH_TYPE_P LENGTH_TYPE_9)? | LENGTH_TYPE_9?);
PRECISION_9_SCALED: S? LENGTH_TYPE_9 LENGTH_TYPE_P?;
PRECISION_9_SCALED_LEAD: S? LENGTH_TYPE_P LENGTH_TYPE_9;
PRECISION_Z_EXPLICIT_DOT: LENGTH_TYPE_Z LENGTH_TYPE_9? (DOT | COMMACHAR) ((LENGTH_TYPE_9 LENGTH_TYPE_Z?) | LENGTH_TYPE_Z);
PRECISION_Z_DECIMAL_SCALED: LENGTH_TYPE_Z LENGTH_TYPE_9? V ((LENGTH_TYPE_P (LENGTH_TYPE_9 | LENGTH_TYPE_Z))? | (LENGTH_TYPE_9? LENGTH_TYPE_Z?));
PRECISION_Z_SCALED: LENGTH_TYPE_Z LENGTH_TYPE_9? LENGTH_TYPE_P?;

// lengths T(x) TTTT
LENGTH_TYPE_9:
    LENGTH_TYPE_9_1+
    ;

LENGTH_TYPE_9_1:
      ('9' LPARENCHAR POSITIVELITERAL RPARENCHAR)
    | '9'+
    ;

LENGTH_TYPE_A:
    LENGTH_TYPE_A_1+
    ;

LENGTH_TYPE_A_1:
      (A LPARENCHAR POSITIVELITERAL RPARENCHAR)
    | A+
    ;

LENGTH_TYPE_P:
    LENGTH_TYPE_P_1+
    ;

LENGTH_TYPE_P_1:
      (P LPARENCHAR POSITIVELITERAL RPARENCHAR)
    | P+
    ;

LENGTH_TYPE_X:
    LENGTH_TYPE_X_1+
    ;

LENGTH_TYPE_X_1:
      (X LPARENCHAR POSITIVELITERAL RPARENCHAR)
    | X+
    ;


LENGTH_TYPE_Z:
    LENGTH_TYPE_Z_1+;

LENGTH_TYPE_Z_1:
      (Z LPARENCHAR POSITIVELITERAL RPARENCHAR)
    | Z+
    ;


// strings
STRINGLITERAL: QUOTEDLITERAL | HEXNUMBER;

fragment HEXNUMBER :
	X '"' [0-9A-F]+ '"'
	| X '\'' [0-9A-F]+ '\''
;

fragment QUOTEDLITERAL :
	'"' (~["\n\r] | '""' | '\'')* '"'
	| '\'' (~['\n\r] | '\'\'' | '"')* '\''
;


// sections
//   http://www.3480-3590-data-conversion.com/article-reading-cobol-layouts-1.html#Record%20Layouts
LEVEL_ROOT: '01';
LEVEL_REGULAR: '0' [2-9] | [1-4] [0-9];
LEVEL_NUMBER_66 : '66';
LEVEL_NUMBER_77 : '77';
LEVEL_NUMBER_88 : '88';

// numbers

INTEGERLITERAL: [0-9]+;
POSITIVELITERAL: '0'* [1-9] [0-9]*;
NUMERICLITERAL: [0-9]* DOT? [0-9]+ (E SIGN_CHAR? [0-9]+)?;

fragment SIGN_CHAR:
    PLUSCHAR | MINUSCHAR
    ;

// identifiers
SINGLE_QUOTED_IDENTIFIER: '\'' IDENTIFIER '\'' ([-_]* [a-zA-Z0-9]+)+;
IDENTIFIER: [a-zA-Z0-9:]+ ([-_]+ [:a-zA-Z0-9]+)*;

// case insensitive chars
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

// control-z EOF
CONTROL_Z: '\u001A';

// whitespace
WS : [ \n\r\t]+ -> skip ;
