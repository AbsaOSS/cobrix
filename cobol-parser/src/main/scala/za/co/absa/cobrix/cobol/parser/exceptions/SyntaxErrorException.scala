package za.co.absa.cobrix.cobol.parser.exceptions

class SyntaxErrorException (val lineNumber: Int, val field: String, val msg: String) extends Exception(s"Syntax error in the copybook at line $lineNumber, field $field: $msg")
