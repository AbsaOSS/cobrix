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