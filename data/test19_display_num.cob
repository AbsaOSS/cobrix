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

       01 WS-INPUT-RECORD.
           05 WS-DATE-NUM              PIC 9(08).
           05 FILLER                   PIC X(01).
           05 WS-DATE-ALPHA            PIC X(10).
           05 FILLER                   PIC X(01).
           05 WS-ACCT-ALPHA            PIC X(08).
           05 FILLER                   PIC X(01).
           05 WS-AMOUNT-NUMERIC        PIC 9(10).
           05 FILLER                   PIC X(01).
           05 WS-AMOUNT-FRACTION       PIC 9(8)V9(2).
           05 FILLER                   PIC X(01).
           05 WS-NAME-ALPHABET         PIC A(10).
           05 FILLER                   PIC X(01).
           05 WS-AMOUNT-FRACTION2      PIC 9(8).9(2).
           05 FILLER                   PIC X(07).
