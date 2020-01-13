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

       01  ENTITY.
           05  SEGMENT-ID        PIC X(1).
           05  COMPANY.
              10  COMPANY-NAME      PIC X(20).
              10  ADDRESS           PIC X(30).
              10  TAXPAYER          PIC X(8).
           05  PERSON REDEFINES COMPANY.
              10  FIRST-NAME        PIC X(16).
              10  LAST-NAME         PIC X(16).
              10  ADDRESS           PIC X(20).
              10  PHONE-NUM         PIC X(11).
           05  PO-BOX REDEFINES COMPANY.
              10  PO-NUMBER         PIC X(12).
              10  BRANCH-ADDRESS    PIC X(20).
