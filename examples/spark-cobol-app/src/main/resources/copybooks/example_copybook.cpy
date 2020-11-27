        01  COMPANY-DETAILS.
            05  SEGMENT-ID           PIC X(5).
            05  COMPANY-ID           PIC X(10).
            05  STATIC-DETAILS.
               10  COMPANY-NAME      PIC X(15).
               10  ADDRESS           PIC X(25).
               10  TAXPAYER.
                  15  TAXPAYER-TYPE  PIC X(1).
                  15  TAXPAYER-STR   PIC X(8).
                  15  TAXPAYER-NUM  REDEFINES TAXPAYER-STR
                                     PIC 9(8) COMP.
            05  CONTACTS REDEFINES STATIC-DETAILS.
               10  PHONE-NUMBER      PIC X(17).
               10  CONTACT-PERSON    PIC X(28).
