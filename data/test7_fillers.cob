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
            05  COMPANY_NAME     PIC X(15).
            05  FILLER REDEFINES COMPANY_NAME.
               10   STR1         PIC X(5).
               10   STR2         PIC X(2).
               10   FILLER       PIC X(1).
            05  ADDRESS          PIC X(25).
            05  FILLER REDEFINES ADDRESS.
               10   STR4         PIC X(10).
               10   FILLER       PIC X(20).
            05  FILL_FIELD.
               10   FILLER       PIC X(5).
               10   FILLER       PIC X(2).
            05  CONTACT_PERSON REDEFINES FILL_FIELD.
               10  FIRST_NAME    PIC X(6).
            05  AMOUNT            PIC S9(09)V99  BINARY.
