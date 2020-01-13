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

      01 RECORD-A.
         05  NAME.
            10  SHORT-NAME.
                11  NAME-CHAR-1     PIC X.
                11  SHORT-NAME-REST PIC X(9).
            10  REST                PIC X(20).
         05  ACCOUNT-NO.
            10  FIRST-6             PIC 9(6).
            10  ACCOUNT-MIDDLE      PIC 9(6).
            10  LAST-4              PIC 9(4).
