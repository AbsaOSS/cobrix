parser grammar copybook_parser;

options { tokenVocab=copybook_lexer; }

// main rule
main:
    item+
    EOF;

// literals
literal:
    STRINGLITERAL | numericLiteral | booleanLiteral | specialValues
    ;

numericLiteral:
    plus_minus? NUMERICLITERAL | ZERO | plus_minus? integerLiteral
    ;

integerLiteral:
      INTEGERLITERAL
    | NINES
    | LEVEL_ROOT
    | LEVEL_REGULAR
    | LEVEL_NUMBER_66
    | LEVEL_NUMBER_77
    | LEVEL_NUMBER_88
    ;

booleanLiteral
   : TRUE | FALSE
   ;

identifier:
      IDENTIFIER | THRU_OR_THROUGH | A_S | P_S | X_S | S_S | Z_S
    | SINGLE_QUOTED_IDENTIFIER // is this valid?
    ;

thru:
    THRU_OR_THROUGH
    ;

// values
values:
    (VALUE IS? | VALUES ARE?)? valuesFromTo (COMMACHAR? valuesFromTo)*
    ;

valuesFromTo:
    valuesFrom valuesTo?
    ;

valuesFrom:
    literal
    ;

valuesTo:
    thru literal
    ;

specialValues:
    ALL literal | HIGH_VALUE | HIGH_VALUES | LOW_VALUE | LOW_VALUES | NULL | NULLS | QUOTE | QUOTES | SPACE | SPACES | ZERO | ZEROS | ZEROES
    ;

// occurs
sorts:
    (ASCENDING | DESCENDING) KEY? IS? identifier;

occurs_to:
    TO integerLiteral
    ;

depending_on:
    DEPENDING ON? identifier
    ;

indexed_by:
    INDEXED BY? identifier
    ;

occurs:
    OCCURS integerLiteral occurs_to? TIMES? depending_on? sorts? indexed_by?
    ;

// redefines
redefines:
    REDEFINES identifier
    ;

// renames
renames:
    RENAMES identifier (thru identifier)?
    ;

usageLiteral:
    (
      COMPUTATIONAL_1 | COMPUTATIONAL_2 | COMPUTATIONAL_3 | COMPUTATIONAL_4 | COMPUTATIONAL_5 | COMPUTATIONAL
    | COMP_1 | COMP_2 | COMP_3 | COMP_4 | COMP_5 | COMP
    | DISPLAY
    | BINARY
    | PACKED_DECIMAL
    )
    ;

// storage
usage:
    (USAGE IS?)? usageLiteral
    ;

separate_sign:
    SIGN IS? (LEADING | TRAILING) SEPARATE? CHARACTER?
    ;

justified:
    (JUSTIFIED | JUST) RIGHT?
    ;

term:
    TERMINAL
    ;

plus_minus:
      PLUSCHAR      #plus
    | MINUSCHAR     #minus
    ;

precision_9:
      NINES                         #precision_9_nines
    | S_S                           #precision_9_ss
    | Z_S                           #precision_9_zs
    | PRECISION_9_SIMPLE            #precision_9_simple
    | PRECISION_9_EXPLICIT_DOT      #precision_9_explicit_dot
    | PRECISION_9_DECIMAL_SCALED    #precision_9_decimal_scaled
    | PRECISION_9_SCALED            #precision_9_scaled
    | PRECISION_9_SCALED_LEAD       #precision_9_scaled_lead
    | PRECISION_Z_EXPLICIT_DOT      #precision_z_explicit_dot
    | PRECISION_Z_DECIMAL_SCALED    #precision_z_decimal_scaled
    | PRECISION_Z_SCALED            #precision_z_scaled
    ;

sign_precision_9:
      (plus_minus? precision_9)     #leading_sign
    | (precision_9 plus_minus?)     #trailing_sign
    ;

alpha_x:
      X_S
    | LENGTH_TYPE_X
    ;

alpha_a:
      X_S
    | LENGTH_TYPE_X
    ;

pictureLiteral:
    PICTURE | PIC
    ;

pic:
    pictureLiteral
    (
      alpha_x
    | alpha_a
    | (
        sign_precision_9 usage?
      | usage? sign_precision_9
      )
    )
    ;

section:
      LEVEL_ROOT
    | LEVEL_REGULAR
    ;

skipLiteral:
    SKIP1 | SKIP2 | SKIP3
    ;

group:
    section identifier (redefines | usage | occurs)* term
    ;

primitive:
    section identifier
    (justified | occurs | pic | redefines | usage | values | separate_sign)*
    (BLANK WHEN? ZERO)?
    term
    ;

level66statement:
    LEVEL_NUMBER_66 identifier renames term
    ;

level88statement:
    LEVEL_NUMBER_88 identifier values term
    ;

item:
      COMMENT
    | group
    | primitive
    | level66statement
    | level88statement
    | skipLiteral
    | term
    ;

