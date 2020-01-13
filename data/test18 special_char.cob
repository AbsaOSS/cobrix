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

      ****************************************************************************
      *    Segment IDs:                                                          *
      *    1 - Company                                                           *
      *    2 - Department                                                        *
      *    3 - Employee                                                          *
      *    4 - Office                                                            *
      *    5 - Customer                                                          *
      *    6 - Contact                                                           *
      *    7 - Contract                                                          *
      ****************************************************************************
           05  SEGMENT-ID           PIC 9(1).

           05  COMPANY.
              10  COMPANY-NAME      PIC X(20).
              10  ADDRESS           PIC X(30).
              10  TAXPAYER          PIC 9(9) BINARY.
           05  DEPT REDEFINES COMPANY.
              10  DEPT-NAME         PIC X(22).
              10  EXTENSION         PIC 9(6).
           05  EMPLOYEE REDEFINES COMPANY.
              10  FIRST-NAME        PIC X(16).
              10  LAST-NAME         PIC X(16).
              10  ROLE              PIC X(18).
              10  HOME-ADDRESS      PIC X(40).
              10  PHONE-NUM         PIC X(17).
           05  OFFICE REDEFINES COMPANY.
              10  ADDRESS           PIC X(30).
              10  FLOOR             PIC 9(3).
              10  ROOM-NUMBER       PIC 9(4).
           05  CUSTOMER REDEFINES COMPANY.
              10  CUSTOMER-NAME     PIC X(20).
              10  POSTAL-ADDRESS    PIC X(30).
              10  ZIP               PIC X(10).
           05  CONTACT REDEFINES COMPANY.
              10  FIRST-NAME        PIC X(16).
              10  LAST-NAME         PIC X(16).
              10  PHONE-NUM         PIC X(17).
           05  CONTRACT REDEFINES COMPANY.
              10  CONTRACT-NUMBER   PIC X(15).
              10  STATE             PIC X(8).
              10  DUE-DATE          PIC X(10).
              10  AMOUNT            PIC 9(10)V9(2) COMP-3.
