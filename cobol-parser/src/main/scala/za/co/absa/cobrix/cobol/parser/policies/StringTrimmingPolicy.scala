package za.co.absa.cobrix.cobol.parser.policies

object StringTrimmingPolicy extends Enumeration {
  type StringTrimmingPolicy = Value

  val TrimNone, TrimLeft, TrimRight, TrimBoth = Value

  def withNameOpt(s: String): Option[Value] = {
    val exactNames = values.find(_.toString == s)
    if (exactNames.isEmpty) {
      val sLowerCase = s.toLowerCase()
      if (sLowerCase == "none") {
        Some(TrimNone)
      } else if (sLowerCase == "left") {
        Some(TrimLeft)
      } else if (sLowerCase == "right") {
        Some(TrimRight)
      } else if (sLowerCase == "both") {
        Some(TrimBoth)
      } else {
        None
      }
    } else {
      exactNames
    }
  }

}
