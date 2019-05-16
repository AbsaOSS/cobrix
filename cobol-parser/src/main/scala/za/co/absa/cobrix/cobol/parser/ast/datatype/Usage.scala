package za.co.absa.cobrix.cobol.parser.ast.datatype


sealed trait Usage

//case class COMP() extends Usage
//case class COMP0() extends Usage
case class COMP1() extends Usage {
  override def toString = "COMP-1"
}
case class COMP2() extends Usage {
  override def toString = "COMP-2"
}
case class COMP3() extends Usage {
  override def toString = "COMP-3"
}
case class COMP4() extends Usage {
  override def toString = "COMP-4"
}
case class COMP5() extends Usage {
  override def toString = "COMP-5"
}
case class COMP9() extends Usage {       // artificial little-endian binary
  override def toString = "COMP-9"
}
//case class DISPLAY() extends Usage {
//  override def toString = "DISPLAY"
//}
//case class BINARY() extends Usage

