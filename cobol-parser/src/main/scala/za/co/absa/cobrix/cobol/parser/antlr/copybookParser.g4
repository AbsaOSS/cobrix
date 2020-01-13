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

parser grammar copybookParser;

options { tokenVocab=copybookLexer; }

// main rule
main:
    item+
    CONTROL_Z?
    EOF;

// literals
literal:
    STRINGLITERAL | numericLiteral | booleanLiteral | specialValues
    ;

numericLiteral:
    plusMinus? NUMERICLITERAL | ZERO | plusMinus? integerLiteral
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
      IDENTIFIER | THRU_OR_THROUGH | A_S | P_S | P_NS | X_S | S_S | S_NS | Z_S | Z_NS | V_S | V_NS
    | SINGLE_QUOTED_IDENTIFIER // is this valid?
    ;

thru:
    THRU_OR_THROUGH
    ;

// values
values:
    (VALUE IS? | VALUES ARE?) valuesFromTo (COMMACHAR? valuesFromTo)*
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

occursTo:
    TO integerLiteral
    ;

dependingOn:
    DEPENDING ON? identifier
    ;

indexedBy:
    INDEXED BY? identifier
    ;

occurs:
    OCCURS integerLiteral occursTo? TIMES? dependingOn? sorts? indexedBy?
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
      COMPUTATIONAL_0 | COMPUTATIONAL_1 | COMPUTATIONAL_2 | COMPUTATIONAL_3 | COMPUTATIONAL_4 | COMPUTATIONAL_5 | COMPUTATIONAL
    | COMP_0 | COMP_1 | COMP_2 | COMP_3 | COMP_4 | COMP_5 | COMP
    | DISPLAY
    | BINARY
    | PACKED_DECIMAL
    )
    ;

groupUsageLiteral:
    (
      COMPUTATIONAL_0 | COMPUTATIONAL_3 | COMPUTATIONAL_4 | COMPUTATIONAL_5 | COMPUTATIONAL
    | COMP_0 | COMP_3 | COMP_4 | COMP_5 | COMP
    | DISPLAY
    | BINARY
    | PACKED_DECIMAL
    )
    ;

// storage
usage:
    (USAGE IS?)? usageLiteral
    ;

usageGroup:
    (USAGE IS?)? groupUsageLiteral
    ;

separateSign:
    SIGN IS? (LEADING | TRAILING) SEPARATE? CHARACTER?
    ;

justified:
    (JUSTIFIED | JUST) RIGHT?
    ;

term:
    TERMINAL
    ;

plusMinus:
      PLUSCHAR      #plus
    | MINUSCHAR     #minus
    ;

precision9:
      NINES                         #precision9Nines
    | S_S                           #precision9Ss
    | P_S                           #precision9Ps
    | Z_S                           #precision9Zs
    | V_S                           #precision9Vs
    | PRECISION_9_EXPLICIT_DOT      #precision9ExplicitDot
    | PRECISION_9_DECIMAL_SCALED    #precision9DecimalScaled
    | PRECISION_9_SCALED            #precision9Scaled
    | PRECISION_9_SCALED_LEAD       #precision9ScaledLead
    | PRECISION_Z_EXPLICIT_DOT      #precisionZExplicitDot
    | PRECISION_Z_DECIMAL_SCALED    #precisionZDecimalScaled
    | PRECISION_Z_SCALED            #precisionZScaled
    ;

signPrecision9:
      (plusMinus? precision9)     #leadingSign
    | (precision9 plusMinus)      #trailingSign
    ;

alphaX:
      X_S
    | LENGTH_TYPE_X
    ;

alphaA:
      A_S
    | LENGTH_TYPE_A
    ;

pictureLiteral:
    PICTURE | PIC
    ;

pic:
    pictureLiteral
    (
      alphaX
    | alphaA
    | (
        signPrecision9 usage?
      | usage? signPrecision9
      )
    )
    | COMP_1
    | COMP_2
    ;

section:
      LEVEL_ROOT
    | LEVEL_REGULAR
    ;

skipLiteral:
    SKIP1 | SKIP2 | SKIP3
    ;

group:
    section identifier (redefines | usageGroup | occurs | values)* term
    ;

primitive:
    section identifier
    (justified | occurs | pic | redefines | usage | values | separateSign)*
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

