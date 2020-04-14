      ****************************************************************************
      *                                                                          *
      * Copyright 2018 ABSA Group Limited                                        *
      *                                                                          *
      * Licensed under the Apache License, Version 2.0 (the "License");          *
      * you may not use this file except in compliance with the License.         *
      * You may obtain a copy of the License at                                  *
      *                                                                          *
      *     http://www.apache.org/licenses/LICENSE-2.0                           *
      *                                                                          *
      * Unless required by applicable law or agreed to in writing, software      *
      * distributed under the License is distributed on an "AS IS" BASIS,        *
      * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
      * See the License for the specific language governing permissions and      *
      * limitations under the License.                                           *
      *                                                                          *
      ****************************************************************************

        01  RECORD.

***********************************************************************
*******                   EDGE CASES
***********************************************************************
          10  ID                   PIC 9(7)  BINARY.

********** String
          10  STRING-VAL           PIC X(10).

********** Integral unsigned numbers formatted as strings
          10  NUM-STR-INT01        PIC 9(1).
          10  NUM-STR-INT02        PIC 9(2).
          10  NUM-STR-INT03        PIC 9(3).
          10  NUM-STR-INT04        PIC 9(4).
          10  NUM-STR-INT05        PIC 9(5).
          10  NUM-STR-INT06        PIC 9(8).
          10  NUM-STR-INT07        PIC 9(9).
          10  NUM-STR-INT08        PIC 9(10).
          10  NUM-STR-INT09        PIC 9(11).
          10  NUM-STR-INT10        PIC 9(17).
          10  NUM-STR-INT11        PIC 9(18).
          10  NUM-STR-INT12        PIC 9(19).
          10  NUM-STR-INT13        PIC 9(20).
          10  NUM-STR-INT14        PIC 9(37).

********** Integral signed numbers formatted as strings
          10  NUM-STR-SINT02       PIC S9(2).
          10  NUM-STR-SINT03       PIC S9(3).
          10  NUM-STR-SINT04       PIC S9(4).
          10  NUM-STR-SINT05       PIC S9(5).
          10  NUM-STR-SINT06       PIC S9(8).
          10  NUM-STR-SINT07       PIC S9(9).
          10  NUM-STR-SINT08       PIC S9(10).
          10  NUM-STR-SINT09       PIC S9(11).
          10  NUM-STR-SINT10       PIC S9(17).
          10  NUM-STR-SINT11       PIC S9(18).
          10  NUM-STR-SINT12       PIC S9(19).
          10  NUM-STR-SINT13       PIC S9(20).
          10  NUM-STR-SINT14       PIC S9(37).

********** Decimal numbers formatted as strings
          10  NUM-STR-DEC01       PIC 99V9.
          10  NUM-STR-DEC02       PIC 99V99.
          10  NUM-STR-DEC03       PIC 9(3)V99.
          10  NUM-STR-DEC04       PIC 9(4)V9(4).
          10  NUM-STR-DEC05       PIC 9(5)V9(4).
          10  NUM-STR-DEC06       PIC 9(5)V9(5).
          10  NUM-STR-DEC07       PIC 9(15)V99.
          10  NUM-STR-DEC08       PIC 9(16)V99.
          10  NUM-STR-DEC09       PIC 9(17)V99.
          10  NUM-STR-DEC10       PIC 9(18)V9(10).
          10  NUM-STR-SDEC01      PIC S99V9.
          10  NUM-STR-SDEC02      PIC S99V99.
          10  NUM-STR-SDEC03      PIC S9(3)V99.
          10  NUM-STR-SDEC04      PIC S9(4)V9(4).
          10  NUM-STR-SDEC05      PIC S9(5)V9(4).
          10  NUM-STR-SDEC06      PIC S9(5)V9(5).
          10  NUM-STR-SDEC07      PIC S9(15)V99.
          10  NUM-STR-SDEC08      PIC S9(16)V99.
          10  NUM-STR-SDEC09      PIC S9(17)V99.
          10  NUM-STR-SDEC10      PIC S9(18)V9(10).
********** These types are currently not supported, added for the future
          10  NUM-STR-EDEC03      PIC S9(3).99.
          10  NUM-STR-EDEC04      PIC S9(4).9(4).
          10  NUM-STR-EDEC05      PIC S9(5).9(4).
          10  NUM-STR-EDEC06      PIC S9(5).9(5).

********** Binary formatted integral numbers
          10  NUM-BIN-INT01       PIC 9(1)         COMP.
          10  NUM-BIN-INT02       PIC 9(2)         COMP.
          10  NUM-BIN-INT03       PIC 9(3)         COMP-0.
          10  NUM-BIN-INT04       PIC 9(4)         COMP-4.
          10  NUM-BIN-INT05       PIC 9(5)         COMP-5.
          10  NUM-BIN-INT06       PIC 9(8)         BINARY.
          10  NUM-BIN-INT07       PIC 9(9)         BINARY.
          10  NUM-BIN-INT08       PIC 9(10)       BINARY.
          10  NUM-BIN-INT09       PIC 9(11)       BINARY.
          10  NUM-BIN-INT10       PIC 9(17)       BINARY.
          10  NUM-BIN-INT11       PIC 9(18)       BINARY.
          10  NUM-BIN-INT12       PIC 9(19)       BINARY.
          10  NUM-BIN-INT13       PIC 9(20)       BINARY.
          10  NUM-BIN-INT14       PIC 9(37)       BINARY.
          10  NUM-SBIN-SINT01     PIC S9(1)        COMP.
          10  NUM-SBIN-SINT02     PIC S9(2)        COMP.
          10  NUM-SBIN-SINT03     PIC S9(3)        COMP.
          10  NUM-SBIN-SINT04     PIC S9(4)        COMP.
          10  NUM-SBIN-SINT05     PIC S9(5)        COMP.
          10  NUM-SBIN-SINT06     PIC S9(8)        BINARY.
          10  NUM-SBIN-SINT07     PIC S9(9)        BINARY.
          10  NUM-SBIN-SINT08     PIC S9(10)       BINARY.
          10  NUM-SBIN-SINT09     PIC S9(11)       BINARY.
          10  NUM-SBIN-SINT10     PIC S9(17)       BINARY.
          10  NUM-SBIN-SINT11     PIC S9(18)       BINARY.
          10  NUM-SBIN-SINT12     PIC S9(19)      BINARY.
          10  NUM-SBIN-SINT13     PIC S9(20)      BINARY.
          10  NUM-SBIN-SINT14     PIC S9(37)      BINARY.

********** Binary formatted decimal numbers
          10  NUM-BIN-DEC01       PIC 99V9         COMP.
          10  NUM-BIN-DEC02       PIC 99V99        COMP.
          10  NUM-BIN-DEC03       PIC 9(3)V99      COMP.
          10  NUM-BIN-DEC04       PIC 9(4)V9(4)    COMP.
          10  NUM-BIN-DEC05       PIC 9(5)V9(4)    COMP.
          10  NUM-BIN-DEC06       PIC 9(5)V9(5)   COMP.
          10  NUM-BIN-DEC07       PIC 9(15)V99    COMP.
          10  NUM-BIN-DEC08       PIC 9(16)V99    COMP.
          10  NUM-BIN-DEC09       PIC 9(17)V99    COMP.
          10  NUM-BIN-DEC10       PIC 9(18)V9(10)  COMP.
          10  NUM-SBIN-DEC01      PIC S99V9        COMP.
          10  NUM-SBIN-DEC02      PIC S99V99       COMP.
          10  NUM-SBIN-DEC03      PIC S9(3)V99     COMP.
          10  NUM-SBIN-DEC04      PIC S9(4)V9(4)   COMP.
          10  NUM-SBIN-DEC05      PIC S9(5)V9(4)   COMP.
          10  NUM-SBIN-DEC06      PIC S9(5)V9(5)   COMP.
          10  NUM-SBIN-DEC07      PIC S9(15)V99    COMP.
          10  NUM-SBIN-DEC08      PIC S9(16)V99    COMP.
          10  NUM-SBIN-DEC09      PIC S9(17)V99   COMP.
          10  NUM-SBIN-DEC10      PIC S9(18)V9(10) COMP.

********** BCD formatted integral numbers
          10  NUM-BCD-INT01       PIC 9(1)        COMP-3.
          10  NUM-BCD-INT02       PIC 9(2)        COMP-3.
          10  NUM-BCD-INT03       PIC 9(3)        COMP-3.
          10  NUM-BCD-INT04       PIC 9(4)        COMP-3.
          10  NUM-BCD-INT05       PIC 9(5)        COMP-3.
          10  NUM-BCD-INT06       PIC 9(8)        COMP-3.
          10  NUM-BCD-INT07       PIC 9(9)        COMP-3.
          10  NUM-BCD-INT08       PIC 9(10)       COMP-3.
          10  NUM-BCD-INT09       PIC 9(11)       COMP-3.
          10  NUM-BCD-INT10       PIC 9(17)       COMP-3.
          10  NUM-BCD-INT11       PIC 9(18)       COMP-3.
          10  NUM-BCD-INT12       PIC 9(19)       COMP-3.
          10  NUM-BCD-INT13       PIC 9(20)       COMP-3.
          10  NUM-BCD-INT14       PIC 9(37)       COMP-3.

          10  NUM-BCD-SINT01      PIC S9(1)       COMP-3.
          10  NUM-BCD-SINT02      PIC S9(2)       COMP-3.
          10  NUM-BCD-SINT03      PIC S9(3)       COMP-3.
          10  NUM-BCD-SINT04      PIC S9(4)       COMP-3.
          10  NUM-BCD-SINT05      PIC S9(5)       COMP-3.
          10  NUM-BCD-SINT06      PIC S9(8)       COMP-3.
          10  NUM-BCD-SINT07      PIC S9(9)       COMP-3.
          10  NUM-BCD-SINT08      PIC S9(10)      COMP-3.
          10  NUM-BCD-SINT09      PIC S9(11)      COMP-3.
          10  NUM-BCD-SINT10      PIC S9(17)      COMP-3.
          10  NUM-BCD-SINT11      PIC S9(18)      COMP-3.
          10  NUM-BCD-SINT12      PIC S9(19)      COMP-3.
          10  NUM-BCD-SINT13      PIC S9(20)      COMP-3.
          10  NUM-BCD-SINT14      PIC S9(37)      COMP-3.
		  
********** BCD formatted decimal numbers
          10  NUM-BCD-DEC01       PIC 99V9        COMP-3.
          10  NUM-BCD-DEC02       PIC 99V99       COMP-3.
          10  NUM-BCD-DEC03       PIC 9(3)V99     COMP-3.
          10  NUM-BCD-DEC04       PIC 9(4)V9(4)   COMP-3.
          10  NUM-BCD-DEC05       PIC 9(5)V9(4)   COMP-3.
          10  NUM-BCD-DEC06       PIC 9(5)V9(5)   COMP-3.
          10  NUM-BCD-DEC07       PIC 9(15)V99    COMP-3.
          10  NUM-BCD-DEC08       PIC 9(16)V99    COMP-3.
          10  NUM-BCD-DEC09       PIC 9(17)V99    COMP-3.
          10  NUM-BCD-DEC10       PIC 9(18)V9(10) COMP-3.
          10  NUM-BCD-SDEC01      PIC S99V9       COMP-3.
          10  NUM-BCD-SDEC02      PIC S99V99      COMP-3.
          10  NUM-BCD-SDEC03      PIC S9(3)V99    COMP-3.
          10  NUM-BCD-SDEC04      PIC S9(4)V9(4)  COMP-3.
          10  NUM-BCD-SDEC05      PIC S9(5)V9(4)  COMP-3.
          10  NUM-BCD-SDEC06      PIC S9(5)V9(5)  COMP-3.
          10  NUM-BCD-SDEC07      PIC S9(15)V99   COMP-3.
          10  NUM-BCD-SDEC08      PIC S9(16)V99   COMP-3.
          10  NUM-BCD-SDEC09      PIC S9(17)V99   COMP-3.
          10  NUM-BCD-SDEC10      PIC S9(18)V9(10) COMP-3.

********** sign trailing numbers
          10  NUM-SL-STR-INT01    PIC S9(9) SIGN IS
		                          LEADING SEPARATE.
          10  NUM-SL-STR-DEC01    PIC 99V99 SIGN IS
                         LEADING SEPARATE CHARACTER.
          10  NUM-ST-STR-INT01    PIC S9(9) SIGN IS
		                          TRAILING SEPARATE.
          10  NUM-ST-STR-DEC01    PIC 99V99 SIGN
                         TRAILING SEPARATE.
          10  NUM-SLI-STR-DEC01   PIC SV9(7) SIGN LEADING.
          10  NUM-STI-STR-DEC01   PIC SV9(7) SIGN TRAILING.
          10  NUM-SLI-DEBUG       PIC X(7).
          10  NUM-STI-DEBUG       PIC X(7).

***********************************************************************
*******               FLOATING POINT TYPES
***********************************************************************

          10  FLOAT-01           COMP-1.
          10  DOUBLE-01          COMP-2.

***********************************************************************
*******                   COMMON TYPES
***********************************************************************
          10  COMMON-8-BIN        PIC 9(8)        BINARY.
          10  COMMON-S3-BIN       PIC S9(3)       BINARY.
          10  COMMON-S94COMP      PIC S9(04)      COMP.
          10  COMMON-S8-BIN       PIC S9(8)       BINARY.
          10  COMMON-DDC97-BIN    PIC S9V9(7)     BINARY.
          10  COMMON-97COMP3      PIC 9(07)       COMP-3.
          10  COMMON-915COMP3     PIC 9(15)       COMP-3.
          10  COMMON-S95COMP3     PIC S9(5)       COMP-3.
          10  COMMON-S999DCCOMP3  PIC S9(09)V99   COMP-3.
          10  COMMON-S913COMP3    PIC S9(13)      COMP-3.
          10  COMMON-S913DCCOMP3  PIC S9(13)V99   COMP-3.
          10  COMMON-S911DCC2     PIC S9(11)V99   COMP-3.
          10  COMMON-S910DCC3     PIC S9(10)V999  COMP-3.
          10  COMMON-S03DDC       PIC SV9(5)      COMP-3.
          10  COMMON-U03DDC       PIC V9(5)       COMP-3.

          10  COMMON-UPC5DDC      PIC PPP9(5)     COMP-3.
          10  COMMON-SPC5DDC      PIC SPP99999    COMP-3.
          10  COMMON-UPI5DDC      PIC 9(5)PPP     COMP-3.
          10  COMMON-SPI5DDC      PIC S99999PPP   COMP-3.

          10  COMMON-UPC5DISP     PIC SPPP9(5).
          10  COMMON-UPI5DISP     PIC S9(5)PPP.

          10  COMMON-UPC1BIN      PIC SPPP9       COMP.
          10  COMMON-UPI1BIN      PIC S9PPP       COMP.
          10  COMMON-UPC3BIN      PIC SPPP9(3)    COMP.
          10  COMMON-UPI3BIN      PIC S9(3)PPP    COMP.
          10  COMMON-UPC5BIN      PIC SPPP9(5)    COMP.
          10  COMMON-UPI5BIN      PIC S9(5)PPP    COMP.
          10  COMMON-UPC10BIN     PIC SPPP9(10)   COMP.
          10  COMMON-UPI10BIN     PIC S9(10)PPP   COMP.

***********************************************************************
*******            EXOTIC AND COMPILER SPECIFIC
***********************************************************************
          10  EX-NUM-INT01        PIC +9(8).
          10  EX-NUM-INT02        PIC 9(8)+.
          10  EX-NUM-INT03        PIC -9(8).
          10  EX-NUM-INT04        PIC Z(8)-.
          10  EX-NUM-DEC01        PIC +9(6)V99.
          10  EX-NUM-DEC02        PIC Z(6)VZZ-.
          10  EX-NUM-DEC03        PIC 9(6).99-.


