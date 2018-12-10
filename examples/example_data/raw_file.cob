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
           05  ID                        PIC S9(4)  COMP.
           05  COMPANY.
               10  SHORT-NAME            PIC X(10).
               10  COMPANY-ID-NUM        PIC 9(5) COMP-3.
               10  COMPANY-ID-STR
			         REDEFINES  COMPANY-ID-NUM PIC X(3).
           05  METADATA.
               10  CLIENTID              PIC X(15).
               10  REGISTRATION-NUM      PIC X(10).
               10  NUMBER-OF-ACCTS       PIC 9(03) COMP-3.
               10  ACCOUNT.
                   12  ACCOUNT-DETAIL    OCCURS 80
                                         DEPENDING ON NUMBER-OF-ACCTS.
                      15  ACCOUNT-NUMBER     PIC X(24).
                      15  ACCOUNT-TYPE-N     PIC 9(5) COMP-3.
                      15  ACCOUNT-TYPE-X     REDEFINES
                           ACCOUNT-TYPE-N  PIC X(3).
